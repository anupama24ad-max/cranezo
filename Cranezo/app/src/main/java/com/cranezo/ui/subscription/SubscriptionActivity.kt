package com.cranezo.ui.subscription

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

    private fun openCheckout(order: SubOrder, method: com.cranezo.utils.PayMethod? = null) {
        lastOrderId = order.order_id
        val options = org.json.JSONObject()
            .put("name", order.name ?: "Cranezo")
            .put("description", order.description.orEmpty())
            .put("order_id", order.order_id)
            .put("currency", order.currency ?: "INR")
            .put("amount", order.amount_paise ?: 0L)
            .put(
                "prefill",
                org.json.JSONObject().apply {
                    order.prefill?.name?.let { put("name", it) }
                    order.prefill?.contact?.let { put("contact", it) }
                }
            )
        runCatching {
            com.cranezo.utils.CheckoutStyle.open(this, order.key_id, options, method)
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
        // the owner picks UPI / card / net banking in the app, then Checkout opens on it
        var pickOrder by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<SubOrder?>(null) }
        LaunchedEffect(state.order) { vm.consumeOrder()?.let { pickOrder = it } }
        pickOrder?.let { order ->
            com.cranezo.ui.components.PaymentMethodSheet(
                amountText = com.cranezo.utils.rupees((order.amount_paise ?: 0L) / 100.0),
                onPick = { method -> pickOrder = null; openCheckout(order, method) },
                onDismiss = { pickOrder = null }
            )
        }
        LaunchedEffect(state.done) { if (state.done) continueSignup() }

        // one alert for the result of a payment: Payment successful (then the screen moves on) or Payment failed (stay and try again)
        state.result?.let { result ->
            val ok = result == "success"
            androidx.compose.ui.window.Dialog(onDismissRequest = {
                vm.clearResult()
                if (ok) continueSignup()
            }) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(dimen_24))
                        .background(com.cranezo.ui.theme.appColors.screenBottom)
                        .padding(dimen_24),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (ok) com.cranezo.ui.components.SuccessTick(size = dimen_80)
                    else androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(dimen_80).clip(androidx.compose.foundation.shape.CircleShape).background(Red.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Close, contentDescription = null,
                            tint = Red, modifier = Modifier.size(dimen_44)
                        )
                    }
                    Spacer(modifier = Modifier.height(dimen_14))
                    Textview(
                        stringResource(if (ok) R.string.sub_pay_success_title else R.string.sub_pay_failed_title),
                        size = font_20, color = if (ok) SuccessGreen else Red, fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(dimen_6))
                    Textview(
                        if (ok) stringResource(R.string.sub_pay_success_msg, formatServerDate(state.info?.due_at))
                        else stringResource(R.string.sub_pay_failed_msg),
                        size = font_13, color = appColors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(dimen_20))
                    CommonGradientButton(
                        text = stringResource(if (ok) R.string.common_continue else R.string.sub_try_again),
                        onClick = {
                            vm.clearResult()
                            if (ok) continueSignup()
                        }
                    )
                }
            }
        }

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
            CommonGradientButton(text = stringResource(if (state.busyKey == "trial") R.string.sub_please_wait else R.string.sub_start_trial), onClick = onTrial)
        }
    }

    val monthly = offer?.plans?.firstOrNull { it.plan == "monthly" }
    val yearly = offer?.plans?.firstOrNull { it.plan == "yearly" }
    // One subscription at a time: while a plan (or the free trial) is running nothing can be bought or changed.
    // A plan can be bought again only after its due date; until then the other plan says when it becomes available.
    val locked = has && info?.can_buy == false
    val lockedUntil = if (locked) formatServerDate(info?.due_at) else null
    monthly?.let { PlanCard(it, stringResource(R.string.sub_unit_month), null, has, state.busyKey == "monthly", onBuy, current = locked && info?.plan == it.plan, lockedUntil = lockedUntil) }
    yearly?.let {
        // what the owner saves compared with paying monthly for a year
        val save = if (monthly?.price != null && monthly.price > 0 && it.price != null) (((monthly.price * 12 - it.price) / (monthly.price * 12)) * 100).toInt() else 0
        PlanCard(it, stringResource(R.string.sub_unit_year), if (save > 0) stringResource(R.string.sub_save_percent, save) else null, has, state.busyKey == "yearly", onBuy, current = locked && info?.plan == it.plan, lockedUntil = lockedUntil)
    }
}

private fun formatPrice(value: Double?): String = com.cranezo.utils.formatAmount(value ?: 0.0)

@Composable
private fun PlanCard(q: PlanQuote, unit: String, badge: String?, renew: Boolean, waiting: Boolean, onBuy: (String) -> Unit, current: Boolean = false, lockedUntil: String? = null) {
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
            else if (waiting) stringResource(R.string.sub_please_wait)
            else if (lockedUntil != null) stringResource(R.string.sub_available_after, lockedUntil)
            else stringResource(if (renew) R.string.sub_pay_renew else R.string.sub_pay, formatPrice(q.total)),
            enabled = !current && lockedUntil == null,
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
