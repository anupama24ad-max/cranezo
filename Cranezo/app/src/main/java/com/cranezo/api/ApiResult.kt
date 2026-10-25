package com.cranezo.api


//This is the common return response type from backend team
data class ApiResult<T>(
    var status: Int? = null,
    var message: String? = null,
    var data: T? = null)
