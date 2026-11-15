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
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
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
import com.cranezo.ui.mainhome.components.SuccessGreen
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
    // the plan running now stays locked until it is due soon (red), so nobody pays twice by mistake
    val running = info?.state == "active" && info.level != "red"
    monthly?.let { PlanCard(it, stringResource(R.string.sub_unit_month), null, has, state.busy, onBuy, current = running && info?.plan == it.plan) }
    yearly?.let {
        // what the owner saves compared with paying monthly for a year
        val save = if (monthly?.price != null && monthly.price > 0 && it.price != null) (((monthly.price * 12 - it.price) / (monthly.price * 12)) * 100).toInt() else 0
        PlanCard(it, stringResource(R.string.sub_unit_year), if (save > 0) stringResource(R.string.sub_save_percent, save) else null, has, state.busy, onBuy, current = running && info?.plan == it.plan)
    }
    Textview(stringResource(R.string.sub_fee_note), size = font_11, color = appColors.textHint)
}

private fun formatPrice(value: Double?): String = com.cranezo.utils.formatAmount(value ?: 0.0)

@Composable
private fun PlanCard(q: PlanQuote, unit: String, badge: String?, renew: Boolean, busy: Boolean, onBuy: (String) -> Unit, current: Boolean = false) {
    // the plan that saves money is highlighted, the other one is quiet
    val highlight = badge != null || current
    GlassCard(contentPadding = dimen_16, borderColor = if (highlight) AmberPrimary else Color.Unspecified) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(q.label.orEmpty(), size = font_15, color = appColors.textSecondary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
            if (badge != null) com.cranezo.ui.booking.components.BookingTag(badge, SuccessGreen)
        }
        Spacer(modifier = Modifier.height(dimen_6))
        // the price, big, with its unit next to it on the same line
        Row(verticalAlignment = Alignment.Bottom) {
            Textview(rupees(q.price), size = font_32, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.width(dimen_6))
            Textview(unit, size = font_14, color = appColors.textSecondary, modifier = Modifier.padding(bottom = dimen_6), maxLines = 1)
        }
        Spacer(modifier = Modifier.height(dimen_10))
        androidx.compose.material3.HorizontalDivider(color = appColors.subtleFill)
        Spacer(modifier = Modifier.height(dimen_10))
        // the bill, one line per part, amounts lined up on the right
        BillRow(stringResource(R.string.sub_row_plan), rupees(q.price))
        if ((q.gst_amount ?: 0.0) > 0) {
            BillRow(stringResource(R.string.sub_row_gst, formatPrice(q.gst_percent)), rupees(q.gst_amount))
        }
        if ((q.gateway_fee ?: 0.0) > 0) {
            BillRow(stringResource(R.string.sub_row_fee, formatPrice(q.gateway_fee_percent)), rupees(q.gateway_fee))
        }
        Spacer(modifier = Modifier.height(dimen_6))
        BillRow(stringResource(R.string.sub_row_total), rupees(q.total), strong = true)
        Spacer(modifier = Modifier.height(dimen_14))
        CommonGradientButton(
            // the string has its own rupee sign: pass the number only (it used to show "Pay ₹₹16,000")
            text = if (current) stringResource(R.string.sub_current_plan)
            else stringResource(if (renew) R.string.sub_pay_extend else R.string.sub_pay, formatPrice(q.total)),
            enabled = !busy && !current,
            onClick = { q.plan?.let(onBuy) }
        )
    }
}

/** One line of the bill: the name on the left, the amount on the right */
@Composable
private fun BillRow(label: String, amount: String, strong: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_2), verticalAlignment = Alignment.CenterVertically) {
        Textview(
            label, size = if (strong) font_15 else font_13, modifier = Modifier.weight(1f),
            color = if (strong) appColors.textPrimary else appColors.textSecondary,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )
        Textview(
            amount, size = if (strong) font_16 else font_13, maxLines = 1,
            color = if (strong) appColors.primaryText else appColors.textPrimary,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Medium
        )
    }
}
