package com.cranezo.ui.profile.model

data class UpdateProfileApiResponse(
    val user: Any? = null,
    val member_id: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val expires_in: String? = null,
    val token: String? = null
)
