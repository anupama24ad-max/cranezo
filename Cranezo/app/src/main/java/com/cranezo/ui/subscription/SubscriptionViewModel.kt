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
    /** The free trial started or the payment went through */
    val done: Boolean = false
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

    /** Razorpay says it was paid: the server checks the signature before the plan changes */
    fun onPaymentSuccess(orderId: String?, paymentId: String?, signature: String?) {
        val body = JSONObject()
            .put("razorpay_order_id", orderId.orEmpty())
            .put("razorpay_payment_id", paymentId.orEmpty())
            .put("razorpay_signature", signature.orEmpty())
        call("owner/subscription/verify", body, SubscriptionInfo::class.java) { info ->
            _state.update { it.copy(info = info, done = true) }
        }
    }

    /** The payment screen was closed or failed: ask Razorpay in case the money was taken anyway */
    fun onPaymentError(orderId: String?) {
        if (orderId.isNullOrBlank()) {
            showError(string(R.string.sub_payment_failed))
            return
        }
        call("owner/subscription/sync", JSONObject().put("order_id", orderId), SubscriptionInfo::class.java) { info ->
            val paid = info?.state == "active"
            _state.update { it.copy(info = info ?: it.info, done = paid) }
            if (!paid) showError(string(R.string.sub_payment_failed))
        }
    }
}
