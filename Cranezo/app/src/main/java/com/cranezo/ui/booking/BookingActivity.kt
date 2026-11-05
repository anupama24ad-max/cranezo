package com.cranezo.ui.booking

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.booking.model.BookingEffect
import com.cranezo.ui.booking.model.BookingIntent
import dagger.hilt.android.AndroidEntryPoint

object BookingRoutes {
    const val TYPE = "type"
    const val CAPACITY = "capacity"
    const val LOCATION = "location"
    const val DATETIME = "datetime"
    const val DURATION = "duration"
    const val RESULTS = "results"
    const val CRANE = "crane"
    const val REVIEW = "review"
    const val PAYMENT = "payment"
    const val WAITING = "waiting"
    const val CONFIRMED = "confirmed"
    const val TRACK = "track"
    const val BOOKINGS = "bookings"
    const val DETAILS = "details"
    const val EXTRA = "extra"
    const val EXTRA_STATUS = "extra_status"
    const val PAY = "pay"
    const val PAID = "paid"
    const val RECEIPT = "receipt"
    const val ACTIVE_TRACKING = "active_tracking"
    // Rapido style: review & price, then the request (searching / pay advance)
    const val OFFER = "offer"
    const val REQUEST = "request"
}

/**
 * Hosts Book a Crane for customers: the 5 steps, the list of available cranes, crane details, review,
 * payment, the wait for the owner, Booking Confirmed, Track My Crane and My Bookings.
 */
@AndroidEntryPoint
class BookingActivity :
    BaseVMComposeActivity<BookingViewModel>(BookingViewModel::class.java),
    com.razorpay.PaymentResultWithDataListener {

    /** Crane requests and online payments (Razorpay) */
    private val requestVm by lazy {
        androidx.lifecycle.ViewModelProvider(this)[com.cranezo.ui.booking.request.RequestViewModel::class.java]
    }

    // ---- Razorpay Checkout results: the server verifies the signature before anything changes ----
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: com.razorpay.PaymentData?) {
        requestVm.onPaymentSuccess(paymentData?.orderId, paymentData?.paymentId ?: razorpayPaymentId, paymentData?.signature)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: com.razorpay.PaymentData?) {
        requestVm.onPaymentError(paymentData?.orderId, response)
    }

    /** Opens Razorpay Checkout for an order made by the server (UPI, cards, net banking) */
    private fun openCheckout(order: com.cranezo.ui.booking.request.PayOrder) {
        val options = org.json.JSONObject()
            .put("name", order.name ?: "Cranezo")
            .put("description", order.description.orEmpty())
            .put("order_id", order.order_id)
            .put("currency", order.currency ?: "INR")
            .put("amount", order.amount_paise ?: 0L)
            .put("theme", org.json.JSONObject().put("color", "#FFA000"))
            .put(
                "prefill",
                org.json.JSONObject().apply {
                    order.prefill?.name?.let { put("name", it) }
                    order.prefill?.contact?.let { put("contact", it) }
                }
            )
        runCatching {
            com.razorpay.Checkout().apply { setKeyID(order.key_id) }.open(this, options)
        }.onFailure { requestVm.onPaymentError(order.order_id, it.message) }
    }

    companion object {
        private const val EXTRA_ROUTE = "route"

        private const val EXTRA_BOOKING = "booking_id"

        /** Opens one booking (from a notification). */
        fun bookingIntent(context: Context, bookingId: String): Intent =
            intent(context, BookingRoutes.BOOKINGS).putExtra(EXTRA_BOOKING, bookingId)

        fun intent(context: Context, route: String = BookingRoutes.TYPE): Intent =
            Intent(context, BookingActivity::class.java).putExtra(EXTRA_ROUTE, route)
    }

    @Composable
    override fun Content(vm: BookingViewModel) {
        val nav = rememberNavController()
        val start = intent.getStringExtra(EXTRA_ROUTE) ?: BookingRoutes.TYPE
        // Opened from a notification: that booking's screen, by its stage
        LaunchedEffect(Unit) { intent.getStringExtra(EXTRA_BOOKING)?.let { vm.onIntent(BookingIntent.OpenBooking(it)) } }

        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    is BookingEffect.Go -> nav.navigate(effect.route) {
                        effect.popUpTo?.let { popUpTo(it) { inclusive = true } }
                    }

                    BookingEffect.Close -> finish()

                    BookingEffect.BackToResults -> {
                        nav.popBackStack(BookingRoutes.RESULTS, inclusive = false)
                        vm.onIntent(BookingIntent.Search)
                    }
                }
            }
        }

        val leave: () -> Unit = { if (!nav.popBackStack()) finish() }
        BackHandler { leave() }

        // Crane requests (send to every nearby owner, pay the advance) have their own view model
        val requestVm = this@BookingActivity.requestVm
        val payState by requestVm.ui.collectAsStateWithLifecycle()
        // a Razorpay order is ready: open Checkout once
        LaunchedEffect(payState.pendingOrder) {
            if (payState.pendingOrder != null) requestVm.consumeOrder()?.let { openCheckout(it) }
        }
        // final balance paid online: show Payment Successful
        LaunchedEffect(payState.finalPaidBookingId) {
            if (payState.finalPaidBookingId != null) {
                requestVm.consumeFinalPaid()
                vm.showPaidOnline()
            }
        }
        // "Book a crane" while a request is still running reopens it (resume)
        LaunchedEffect(Unit) {
            if (start == BookingRoutes.TYPE) {
                requestVm.loadActive {
                    nav.navigate(BookingRoutes.REQUEST) { popUpTo(BookingRoutes.TYPE) { inclusive = true } }
                }
            }
        }

        // Back to an earlier step of the same flow
        val backTo: (String) -> Unit = { route -> nav.popBackStack(route, inclusive = false) }

        // From a request (cancelled, expired or left): back to step 4 "When" with the earlier choices kept.
        // The steps are rebuilt so Back keeps working; with nothing chosen (app reopened) it starts at step 1.
        val toWhenStep: () -> Unit = {
            requestVm.clearRequest()
            val s = vm.uiState.value
            nav.navigate(BookingRoutes.TYPE) { popUpTo(BookingRoutes.REQUEST) { inclusive = true } }
            if (s.craneType != null && s.capacity != null && s.latitude != null) {
                nav.navigate(BookingRoutes.CAPACITY)
                nav.navigate(BookingRoutes.LOCATION)
                nav.navigate(BookingRoutes.DATETIME)
            }
        }

        NavHost(navController = nav, startDestination = start) {
            composable(BookingRoutes.TYPE) {
                CraneTypeStep(vm, onBack = leave, onNext = { nav.navigate(BookingRoutes.CAPACITY) })
            }
            composable(BookingRoutes.CAPACITY) {
                CapacityStep(vm, onBack = leave, onNext = { nav.navigate(BookingRoutes.LOCATION) })
            }
            composable(BookingRoutes.LOCATION) {
                LocationStep(vm, onBack = leave, onNext = { nav.navigate(BookingRoutes.DATETIME) })
            }
            composable(BookingRoutes.DATETIME) {
                // "When" now includes how long: straight to the price screen
                DateTimeStep(vm, onBack = leave, onNext = { nav.navigate(BookingRoutes.OFFER) })
            }
            composable(BookingRoutes.DURATION) {
                // No list of cranes: the customer sees the price and makes an offer to every owner nearby
                DurationStep(vm, onBack = leave, onNext = { nav.navigate(BookingRoutes.OFFER) })
            }
            composable(BookingRoutes.OFFER) {
                com.cranezo.ui.booking.request.OfferScreen(
                    bookingVm = vm,
                    vm = requestVm,
                    onBack = leave,
                    onEdit = { backTo(BookingRoutes.DATETIME) },
                    onSent = { nav.navigate(BookingRoutes.REQUEST) { popUpTo(BookingRoutes.TYPE) { inclusive = true } } },
                    onClose = { finish() }
                )
            }
            composable(BookingRoutes.REQUEST) {
                com.cranezo.ui.booking.request.RequestStatusScreen(
                    vm = requestVm,
                    onClose = { finish() },
                    onEditBooking = toWhenStep,
                    onBooked = { id ->
                        requestVm.clearRequest()
                        vm.onIntent(BookingIntent.OpenBooking(id))
                    },
                    // start again from step 4 with a fresh offer
                    onTryAgain = toWhenStep
                )
            }
            composable(BookingRoutes.RESULTS) {
                AvailableCranesScreen(vm, onBack = leave, onEditRequirements = { backTo(BookingRoutes.DURATION) })
            }
            composable(BookingRoutes.CRANE) {
                BookableCraneScreen(
                    vm,
                    onBack = leave,
                    onEditBooking = { backTo(BookingRoutes.DATETIME) },
                    onSelect = { nav.navigate(BookingRoutes.REVIEW) }
                )
            }
            composable(BookingRoutes.REVIEW) {
                ReviewBookingScreen(
                    vm,
                    onBack = leave,
                    onChangeCrane = { backTo(BookingRoutes.RESULTS) },
                    onEditBooking = { backTo(BookingRoutes.DATETIME) },
                    onConfirm = { nav.navigate(BookingRoutes.PAYMENT) }
                )
            }
            composable(BookingRoutes.PAYMENT) {
                PaymentScreen(vm, onBack = leave, onConfirm = { vm.onIntent(BookingIntent.CreateBooking) })
            }
            composable(BookingRoutes.WAITING) {
                WaitingScreen(
                    vm,
                    onConfirmed = {
                        nav.navigate(BookingRoutes.CONFIRMED) { popUpTo(BookingRoutes.WAITING) { inclusive = true } }
                    },
                    onChooseAnother = {
                        nav.popBackStack(BookingRoutes.RESULTS, inclusive = false)
                        vm.onIntent(BookingIntent.Search)
                    },
                    onClose = { finish() }
                )
            }
            composable(BookingRoutes.CONFIRMED) {
                BookingConfirmedScreen(vm, onTrack = { nav.navigate(BookingRoutes.TRACK) }, onClose = leave)
            }
            composable(BookingRoutes.TRACK) { TrackScreen(vm, onBack = leave) }
            composable(BookingRoutes.BOOKINGS) { MyJobsScreen(vm, onBack = leave) }
            composable(BookingRoutes.DETAILS) {
                JobDetailsScreen(
                    vm,
                    onBack = leave,
                    onTrack = { nav.navigate(BookingRoutes.TRACK) },
                    onExtra = { nav.navigate(BookingRoutes.EXTRA) },
                    onExtraStatus = { nav.navigate(BookingRoutes.EXTRA_STATUS) },
                    onPay = { nav.navigate(BookingRoutes.PAY) },
                    onReceipt = { nav.navigate(BookingRoutes.RECEIPT) }
                )
            }
            composable(BookingRoutes.EXTRA) { ExtraTimeRequestScreen(vm, onBack = leave) }
            composable(BookingRoutes.EXTRA_STATUS) { ExtraTimeStatusScreen(vm, onBack = leave) }
            composable(BookingRoutes.PAY) {
                // final balance: online only (Razorpay)
                FinalPaymentScreen(vm, onBack = leave, onPayOnline = {
                    vm.currentBookingId()?.let { requestVm.startOnlinePayment(it, "final") }
                })
            }
            composable(BookingRoutes.PAID) { PaymentSuccessfulScreen(vm, onBack = leave) }
            composable(BookingRoutes.RECEIPT) { ReceiptScreen(vm, onBack = leave) }
            composable(BookingRoutes.ACTIVE_TRACKING) { TrackMyCranesScreen(vm, onBack = leave) }
        }
    }
}
