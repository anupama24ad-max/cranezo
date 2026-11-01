package com.cranezo.ui.login.model

data class RegisterOrLoginApiResponse(
    val user_id: String? = null,
    val country_code: String? = null,
    val phone_number: String? = null,
    val member_id: String? = null,
    val role_id: Int? = null,
    val otp: String? = null,
    val is_new_user: Boolean? = null
)
