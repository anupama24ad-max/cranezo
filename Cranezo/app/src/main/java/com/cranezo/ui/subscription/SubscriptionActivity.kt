package com.cranezo.ui.subscription

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.profile.ProfileActivity
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.rupees
import dagger.hilt.android.AndroidEntryPoint

/**
 * The owner's subscription: the free first month, the monthly and the yearly plan (paid with Razorpay,
 * the online payment fee is shown and added). Opened right after an owner chooses the owner role
 * ([signup] = true: then the usual sign-up continues), and later from Home or the profile to renew.
 */
@AndroidEntryPoint
class SubscriptionActivity :
    BaseVMComposeActivity<SubscriptionViewModel>(SubscriptionViewModel::class.java),
    com.razorpay.PaymentResultWithDataListener {

    private val signup: Boolean get() = intent.getBooleanExtra(EXTRA_SIGNUP, false)

    /** Sign-up screens are always dark, like the others in that flow */
    override val forceDarkTheme: Boolean? get() = if (signup) true else null

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: com.razorpay.PaymentData?) {
        viewModel.onPaymentSuccess(paymentData?.orderId, paymentData?.paymentId ?: razorpayPaymentId, paymentData?.signature)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: com.razorpay.PaymentData?) {
        viewModel.onPaymentError(paymentData?.orderId ?: lastOrderId)
    }

    private var lastOrderId: String? = null

    private fun openCheckout(order: SubOrder) {
        lastOrderId = order.order_id
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
        }.onFailure { viewModel.onPaymentError(order.order_id) }
    }

    private fun continueSignup() {
        if (signup) startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    @Composable
    override fun Content(vm: SubscriptionViewModel) {
        val state by vm.state.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { vm.load() }
        LaunchedEffect(state.order) { vm.consumeOrder()?.let { openCheckout(it) } }
        LaunchedEffect(state.done) { if (state.done) continueSignup() }

        MyProfileScaffold(title = stringResource(R.string.sub_title), onBack = { finish() }) {
            SubscriptionContent(
                state = state, signup = signup,
                onTrial = { vm.startTrial() }, onBuy = { vm.buy(it) }, onContinue = { continueSignup() }
            )
        }
    }

    companion object {
        private const val EXTRA_SIGNUP = "signup"

        fun intent(context: Context, signup: Boolean = false): Intent =
            Intent(context, SubscriptionActivity::class.java).putExtra(EXTRA_SIGNUP, signup)
    }
}

@Composable
private fun SubscriptionContent(
    state: SubscriptionState,
    signup: Boolean,
    onTrial: () -> Unit,
    onBuy: (String) -> Unit,
    onContinue: () -> Unit
) {
    val info = state.info
    val has = info != null && info.state != null && info.state != "none"
    val offer = info?.offer

    // where the owner stands now
    if (has) {
        val red = info?.level == "red"
        GlassCard(contentPadding = dimen_14, borderColor = if (red) Red else AmberPrimary) {
            Textview(stringResource(R.string.sub_current), size = font_11, color = appColors.textSecondary)
            Textview(
                stringResource(R.string.sub_plan_line, info?.plan_label.orEmpty()), size = font_16, color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimen_8))
            Row {
                Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                    Textview(stringResource(R.string.sub_from), size = font_11, color = appColors.textSecondary)
                    Textview(formatServerDate(info?.period_start), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                    Textview(stringResource(R.string.sub_due), size = font_11, color = appColors.textSecondary)
                    Textview(formatServerDate(info?.due_at), size = font_13, color = if (red) Red else appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            info?.message?.let {
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(it, size = font_12, color = if (red) Red else appColors.textSecondary, fontWeight = if (red) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
        if (signup) CommonGradientButton(text = stringResource(R.string.common_continue), onClick = onContinue)
    }

    Textview(stringResource(R.string.sub_choose), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)

    // the free first month: only for an owner who has not started yet
    if (!has) {
        GlassCard(contentPadding = dimen_14, borderColor = SuccessGreen) {
            Textview(stringResource(R.string.sub_trial_title), size = font_18, color = SuccessGreen, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.sub_trial_sub, offer?.trial_days ?: 30), size = font_12, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_10))
            CommonGradientButton(text = stringResource(R.string.sub_start_trial), enabled = !state.busy, onClick = onTrial)
        }
    }

    val monthly = offer?.plans?.firstOrNull { it.plan == "monthly" }
    val yearly = offer?.plans?.firstOrNull { it.plan == "yearly" }
    monthly?.let { PlanCard(it, stringResource(R.string.sub_per_month, formatPrice(it.price)), null, has, state.busy, onBuy) }
    yearly?.let {
        // what the owner saves compared with paying monthly for a year
        val save = if (monthly?.price != null && monthly.price > 0 && it.price != null) (((monthly.price * 12 - it.price) / (monthly.price * 12)) * 100).toInt() else 0
        PlanCard(it, stringResource(R.string.sub_per_year, formatPrice(it.price)), if (save > 0) stringResource(R.string.sub_save_percent, save) else null, has, state.busy, onBuy)
    }
    Textview(stringResource(R.string.sub_fee_note), size = font_11, color = appColors.textHint)
}

private fun formatPrice(value: Double?): String = com.cranezo.utils.formatAmount(value ?: 0.0)

@Composable
private fun PlanCard(q: PlanQuote, title: String, badge: String?, renew: Boolean, busy: Boolean, onBuy: (String) -> Unit) {
    GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(q.label.orEmpty(), size = font_14, color = appColors.textSecondary)
                Textview(title, size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            }
            if (badge != null) com.cranezo.ui.booking.components.BookingTag(badge, SuccessGreen)
        }
        Spacer(modifier = Modifier.height(dimen_6))
        // the bill: price, GST when there is any, the online payment fee, and the total
        if ((q.gst_amount ?: 0.0) > 0) {
            Textview(stringResource(R.string.sub_gst_line, formatPrice(q.gst_amount), formatPrice(q.gst_percent)), size = font_12, color = appColors.textSecondary)
        }
        if ((q.gateway_fee ?: 0.0) > 0) {
            Textview(stringResource(R.string.sub_fee_line, formatPrice(q.gateway_fee), formatPrice(q.gateway_fee_percent)), size = font_12, color = appColors.textSecondary)
        }
        Spacer(modifier = Modifier.height(dimen_10))
        CommonGradientButton(
            text = stringResource(if (renew) R.string.sub_pay_extend else R.string.sub_pay, rupees(q.total)),
            enabled = !busy,
            onClick = { q.plan?.let(onBuy) }
        )
    }
}
