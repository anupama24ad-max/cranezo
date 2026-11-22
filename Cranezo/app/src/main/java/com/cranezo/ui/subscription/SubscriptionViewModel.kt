package com.cranezo.ui.subscription

import android.app.Application
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import javax.inject.Inject

data class SubscriptionState(
    val info: SubscriptionInfo? = null,
    val loaded: Boolean = false,
    val busy: Boolean = false,
    /** A Razorpay order is ready: the screen opens Checkout once and clears it */
    val order: SubOrder? = null,
    /** The free trial started: the screen moves on at once */
    val done: Boolean = false,
    /** The result of a payment, shown as one alert: "success" or "failed" (null = nothing to show) */
    val result: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()
    private val gson = Gson()

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    private fun <T> call(path: String, body: JSONObject, type: Class<T>, onOk: (T?) -> Unit) {
        _state.update { it.copy(busy = true) }
        runWhenOnline {
            repo.financePost(path, headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val data = r?.data?.takeIf { !it.isJsonNull }?.let { gson.fromJson(it, type) }
                    _state.update { it.copy(busy = false, loaded = true) }
                    onOk(data)
                },
                failure = { _, _, m ->
                    _state.update { it.copy(busy = false, loaded = true) }
                    fail(m)
                }
            )
        }
    }

    fun load() = call("owner/subscription", JSONObject(), SubscriptionInfo::class.java) { info ->
        _state.update { it.copy(info = info) }
    }

    fun startTrial() = call("owner/subscription/start-trial", JSONObject(), SubscriptionInfo::class.java) { info ->
        _state.update { it.copy(info = info, done = true) }
    }

    fun buy(plan: String) = call("owner/subscription/order", JSONObject().put("plan", plan), SubOrder::class.java) { order ->
        _state.update { it.copy(order = order) }
    }

    fun consumeOrder(): SubOrder? {
        val order = _state.value.order
        _state.update { it.copy(order = null) }
        return order
    }

    /** The alert was closed */
    fun clearResult() = _state.update { it.copy(result = null) }

    /** Razorpay says it was paid: the server checks the signature before the plan changes */
    fun onPaymentSuccess(orderId: String?, paymentId: String?, signature: String?) {
        val body = JSONObject()
            .put("razorpay_order_id", orderId.orEmpty())
            .put("razorpay_payment_id", paymentId.orEmpty())
            .put("razorpay_signature", signature.orEmpty())
        _state.update { it.copy(busy = true) }
        runWhenOnline {
            repo.financePost("owner/subscription/verify", headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val info = r?.data?.takeIf { !it.isJsonNull }?.let { gson.fromJson(it, SubscriptionInfo::class.java) }
                    _state.update { it.copy(busy = false, loaded = true, info = info ?: it.info, result = "success") }
                },
                // Razorpay said paid but the check failed: ask Razorpay itself before telling the owner anything
                failure = { _, _, _ ->
                    _state.update { it.copy(busy = false) }
                    onPaymentError(orderId)
                }
            )
        }
    }

    /** The payment screen was closed or failed: ask Razorpay in case the money was taken anyway, then show one alert */
    fun onPaymentError(orderId: String?) {
        if (orderId.isNullOrBlank()) {
            _state.update { it.copy(result = "failed") }
            return
        }
        _state.update { it.copy(busy = true) }
        runWhenOnline {
            repo.financePost("owner/subscription/sync", headers(), JSONObject().put("order_id", orderId)).isRequestCallSuspendSuccess(
                success = { r ->
                    val info = r?.data?.takeIf { !it.isJsonNull }?.let { gson.fromJson(it, SubscriptionInfo::class.java) }
                    // this payment only: an owner who already has an active plan is not "paid" after a failed attempt
                    val paid = info?.paid == true
                    _state.update { it.copy(busy = false, loaded = true, info = info ?: it.info, result = if (paid) "success" else "failed") }
                },
                // the check itself failed (e.g. "forbidden"): the owner only cancelled, so it is a plain failure, not the server's message
                failure = { _, _, _ ->
                    _state.update { it.copy(busy = false, loaded = true, result = "failed") }
                }
            )
        }
    }
}
