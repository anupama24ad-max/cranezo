package com.cranezo.ui.booking

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    const val ACTIVE_TRACKING = "active_tracking"
}

/**
 * Hosts Book a Crane for customers: the 5 steps, the list of available cranes, crane details, review,
 * payment, the wait for the owner, Booking Confirmed, Track My Crane and My Bookings.
 */
@AndroidEntryPoint
class BookingActivity :
    BaseVMComposeActivity<BookingViewModel>(BookingViewModel::class.java) {

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

        // Back to an earlier step of the same flow
        val backTo: (String) -> Unit = { route -> nav.popBackStack(route, inclusive = false) }

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
                DateTimeStep(vm, onBack = leave, onNext = { nav.navigate(BookingRoutes.DURATION) })
            }
            composable(BookingRoutes.DURATION) {
                // The search opens the list of cranes when it has an answer
                DurationStep(vm, onBack = leave, onNext = { vm.onIntent(BookingIntent.Search) })
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
                    onReceipt = { nav.navigate(BookingRoutes.PAID) }
                )
            }
            composable(BookingRoutes.EXTRA) { ExtraTimeRequestScreen(vm, onBack = leave) }
            composable(BookingRoutes.EXTRA_STATUS) { ExtraTimeStatusScreen(vm, onBack = leave) }
            composable(BookingRoutes.PAY) { FinalPaymentScreen(vm, onBack = leave) }
            composable(BookingRoutes.PAID) { PaymentSuccessfulScreen(vm, onBack = leave) }
            composable(BookingRoutes.ACTIVE_TRACKING) { TrackMyCranesScreen(vm, onBack = leave) }
        }
    }
}
