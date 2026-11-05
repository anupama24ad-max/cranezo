package com.cranezo.ui.booking.request

import android.app.Application
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import javax.inject.Inject

data class RequestUiState(
    val estimate: RequestEstimate? = null,
    val estimating: Boolean = false,
    val estimateFailed: Boolean = false,
    val request: CraneRequestView? = null,
    val busy: Boolean = false,
    // "No crane found" was sent to the team
    val sentToTeam: Boolean = false,
    // Razorpay: an order waiting to be opened in Checkout, and which booking it is for
    val pendingOrder: PayOrder? = null,
    val payingBookingId: String? = null,
    // the final payment went through: the booking screens show "Payment Successful"
    val finalPaidBookingId: String? = null,
    // owner side
    val ownerList: OwnerRequestList? = null,
    val ownerLoaded: Boolean = false,
    val acceptedBookingId: String? = null
)

/** Rapido-style booking: estimate, send, raise, pay advance (customer) and accept (owner). */
@HiltViewModel
class RequestViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _ui = MutableStateFlow(RequestUiState())
    val ui: StateFlow<RequestUiState> = _ui.asStateFlow()
    private val gson = Gson()

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 3000L)

    private fun <T> parse(data: JsonElement?, cls: Class<T>): T? =
        if (data == null || data.isJsonNull) null else runCatching { gson.fromJson(data, cls) }.getOrNull()

    /** POST and hand back the data; [quiet] calls (polling) never show errors */
    private fun post(
        path: String,
        body: JSONObject,
        busy: Boolean = true,
        quiet: Boolean = false,
        success: Boolean = false,
        onFail: (String?) -> Unit = {},
        onData: (JsonElement?) -> Unit
    ) {
        if (busy) _ui.update { it.copy(busy = true) }
        runWhenOnline {
            repo.financePost(path, headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    _ui.update { it.copy(busy = false) }
                    if (success) r?.message?.let { showSuccess(it, autoHideMs = 2500L) }
                    onData(r?.data)
                },
                failure = { _, _, m ->
                    _ui.update { it.copy(busy = false) }
                    onFail(m)
                    if (!quiet) fail(m)
                }
            )
        }
    }

    private fun setRequest(data: JsonElement?) {
        parse(data, CraneRequestView::class.java)?.let { r -> _ui.update { it.copy(request = r) } }
    }

    // ======================================================================== customer

    fun estimate(body: JSONObject?) {
        if (body == null) return
        _ui.update { it.copy(estimating = true, estimateFailed = false, sentToTeam = false) }
        post("customer/crane-requests/estimate", body, busy = false, onFail = {
            _ui.update { it.copy(estimating = false, estimateFailed = true) }
        }) { d ->
            _ui.update { it.copy(estimating = false, estimate = parse(d, RequestEstimate::class.java)) }
        }
    }

    fun create(body: JSONObject?, offer: Double, autoRaise: Boolean, autoMax: Double?, onSent: () -> Unit) {
        if (body == null) return
        val b = JSONObject(body.toString()).put("offer_amount", offer).put("auto_raise", autoRaise)
        if (autoRaise && autoMax != null) b.put("auto_raise_max", autoMax)
        post("customer/crane-requests/create", b, success = true) { d ->
            setRequest(d)
            onSent()
        }
    }

    /** The request in progress (searching or waiting for the advance), to reopen it */
    fun loadActive(onFound: () -> Unit = {}) {
        post("customer/crane-requests/active", JSONObject(), busy = false, quiet = true) { d ->
            val r = parse(d, CraneRequestView::class.java)
            _ui.update { it.copy(request = r) }
            if (r != null) onFound()
        }
    }

    fun refresh() {
        val id = _ui.value.request?.request_id ?: return loadActive()
        post("customer/crane-requests/get", JSONObject().put("request_id", id), busy = false, quiet = true) { setRequest(it) }
    }

    private fun act(path: String, extra: JSONObject.() -> Unit = {}) {
        val id = _ui.value.request?.request_id ?: return
        val body = JSONObject().put("request_id", id).apply(extra)
        post(path, body, success = true) { setRequest(it) }
    }

    fun raise() = act("customer/crane-requests/raise")

    fun setAutoRaise(on: Boolean, max: Double?) = act("customer/crane-requests/auto-raise") {
        put("auto_raise", on)
        if (on && max != null) put("auto_raise_max", max)
    }

    fun cancel() = act("customer/crane-requests/cancel")

    /** Advance: online through Razorpay; the test payment only while the server has no gateway */
    fun payAdvance() {
        val b = _ui.value.request?.booking ?: return
        val bookingId = b.booking_id ?: return
        if (b.online_payment == true) {
            startOnlinePayment(bookingId, "advance")
        } else {
            post("customer/crane-requests/pay-advance", JSONObject().put("booking_id", bookingId), success = true) { setRequest(it) }
        }
    }

    /** 1. ask the server for a Razorpay order; the activity then opens Checkout with it */
    fun startOnlinePayment(bookingId: String, kind: String) {
        post("customer/payments/order", JSONObject().put("booking_id", bookingId).put("kind", kind)) { d ->
            parse(d, PayOrder::class.java)?.let { order -> _ui.update { it.copy(pendingOrder = order, payingBookingId = bookingId) } }
        }
    }

    /** Checkout is open: forget the order so it is not opened twice */
    fun consumeOrder(): PayOrder? {
        val order = _ui.value.pendingOrder
        _ui.update { it.copy(pendingOrder = null) }
        return order
    }

    /** 2. Razorpay says paid: the server checks the signature before anything changes */
    fun onPaymentSuccess(orderId: String?, paymentId: String?, signature: String?) {
        val bookingId = _ui.value.payingBookingId ?: return
        val body = JSONObject()
            .put("booking_id", bookingId)
            .put("razorpay_order_id", orderId.orEmpty())
            .put("razorpay_payment_id", paymentId.orEmpty())
            .put("razorpay_signature", signature.orEmpty())
        post("customer/payments/verify", body, success = true) { d ->
            val result = parse(d, PayResult::class.java)
            if (result?.kind == "final") {
                _ui.update { it.copy(finalPaidBookingId = bookingId, payingBookingId = null) }
            } else {
                result?.request?.let { r -> _ui.update { it.copy(request = r, payingBookingId = null) } }
            }
        }
    }

    /** Cancelled or failed in Checkout: logged on the server, nothing changes for the booking */
    fun onPaymentError(orderId: String?, message: String?) {
        val bookingId = _ui.value.payingBookingId ?: return
        post(
            "customer/payments/failed",
            JSONObject().put("booking_id", bookingId).put("razorpay_order_id", orderId.orEmpty()).put("reason", message.orEmpty().take(300)),
            busy = false, quiet = true
        ) {}
        fail(string(R.string.rq_payment_not_done))
    }

    fun consumeFinalPaid(): String? {
        val id = _ui.value.finalPaidBookingId
        _ui.update { it.copy(finalPaidBookingId = null) }
        return id
    }

    /** "No crane found": from an expired request, or straight from the review screen (with the 5 steps) */
    fun sendToTeam(note: String, requirements: JSONObject?) {
        val req = _ui.value.request
        val body = if (req?.can_send_to_team == true) {
            JSONObject().put("request_id", req.request_id)
        } else {
            requirements?.let { JSONObject(it.toString()) } ?: return
        }
        if (note.isNotBlank()) body.put("note", note.trim())
        post("customer/crane-requests/send-to-team", body, success = true) { d ->
            setRequest(d)
            _ui.update { it.copy(sentToTeam = true) }
        }
    }

    fun clearRequest() = _ui.update { it.copy(request = null, sentToTeam = false) }

    // ======================================================================== owner

    fun loadOwnerRequests(quiet: Boolean = false) {
        post("owner/crane-requests", JSONObject(), busy = false, quiet = quiet) { d ->
            _ui.update { it.copy(ownerList = parse(d, OwnerRequestList::class.java), ownerLoaded = true) }
        }
    }

    fun accept(requestId: String, craneId: String) {
        post(
            "owner/crane-requests/accept",
            JSONObject().put("request_id", requestId).put("crane_id", craneId),
            success = true,
            onFail = { loadOwnerRequests(quiet = true) }
        ) { d ->
            _ui.update { it.copy(acceptedBookingId = parse(d, AcceptedBooking::class.java)?.booking_id) }
            loadOwnerRequests(quiet = true)
        }
    }

    fun consumeAccepted(): String? {
        val id = _ui.value.acceptedBookingId
        _ui.update { it.copy(acceptedBookingId = null) }
        return id
    }
}
