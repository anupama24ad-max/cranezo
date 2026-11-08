package com.cranezo.ui.subscription

/** What one plan costs: the price, GST and the online payment fee, and the total the owner pays */
data class PlanQuote(
    val plan: String? = null,
    val label: String? = null,
    val price: Double? = null,
    val gst_percent: Double? = null,
    val gst_amount: Double? = null,
    val gateway_fee_percent: Double? = null,
    val gateway_fee: Double? = null,
    val total: Double? = null
)

data class SubscriptionOffer(
    val trial_days: Int? = null,
    val grace_days: Int? = null,
    val warn_days: Int? = null,
    val plans: List<PlanQuote>? = null
)

/**
 * POST /owner/subscription. state: none, trial, active, grace (overdue, still allowed) or expired.
 * level: ok, info (free trial) or red (ending soon, overdue or expired).
 */
data class SubscriptionInfo(
    val state: String? = null,
    val allowed: Boolean? = null,
    val level: String? = null,
    val message: String? = null,
    val plan: String? = null,
    val plan_label: String? = null,
    val started_at: String? = null,
    val period_start: String? = null,
    val due_at: String? = null,
    val days_left: Int? = null,
    val grace_days_left: Int? = null,
    val last_paid_at: String? = null,
    val offer: SubscriptionOffer? = null
)

data class SubPrefill(val name: String? = null, val contact: String? = null)

/** A Razorpay order for a plan (POST /owner/subscription/order) */
data class SubOrder(
    val key_id: String? = null,
    val order_id: String? = null,
    val amount_paise: Long? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val plan: String? = null,
    val name: String? = null,
    val description: String? = null,
    val prefill: SubPrefill? = null
)
