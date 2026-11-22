package com.cranezo.ui.owneroperators

/** How an owner pays an operator: "monthly", "daily" or "per_job", with an optional daily allowance (batta) */
data class PayTerms(
    val salary_type: String = "monthly",
    val monthly_salary: Double? = null,
    val daily_wage: Double? = null,
    val batta: Double? = null
)

/** One operator in the owner's team (POST owner/operators) */
data class TeamOperator(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val profile_image: String? = null,
    val is_online: Boolean? = null,
    val is_mobile_verified: Boolean? = null,
    val source: String? = null,
    val salary_type: String? = null,
    val monthly_salary: Double? = null,
    val daily_wage: Double? = null,
    val batta: Double? = null,
    // only in the reply to "add a new operator": an OTP was sent to their mobile (otp is the fixed test code, never a real one)
    val needs_otp: Boolean? = null,
    val otp: String? = null
)

data class OperatorBrief(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val profile_image: String? = null,
    // only on a search result: free, yours, other_owner, requested or blocked
    val status: String? = null
)

data class OwnerBrief(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val company_name: String? = null,
    val contact: String? = null
)

/** A request an owner sent (owner side) or got (operator side) */
data class TeamInvite(
    val id: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val message: String? = null,
    val salary_type: String? = null,
    val monthly_salary: Double? = null,
    val daily_wage: Double? = null,
    val batta: Double? = null,
    val created_at: String? = null,
    val operator: OperatorBrief? = null,
    val owner: OwnerBrief? = null
)

data class ReceivedInvites(
    val waiting: Int? = null,
    val items: List<TeamInvite>? = null
)
