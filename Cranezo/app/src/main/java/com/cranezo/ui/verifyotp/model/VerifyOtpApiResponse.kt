package com.cranezo.ui.verifyotp.model

data class VerifyOtpApiResponse(
    val user_id: String? = null,
    val member_id: String? = null,
    val phone_number: String? = null,
    val is_mobile_verified: Int? = null,
    // 0 -> user already has a role, 1 -> user does not have a role yet
    val role_id_status: Int? = null,
    val has_role: Int? = null,
    val role_id: Int? = null,
    val role_name: String? = null,
    val full_name: String? = null,
    val company_name: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val expires_in: String? = null,
    val token: String? = null
)
