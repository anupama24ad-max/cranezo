package com.cranezo.api


//This is the common return response type from backend team
data class ApiResult<T>(
    var status: Int? = null,
    var message: String? = null,
    var data: T? = null,
    // Only the paged lists (getCranes, owner/operators) send it
    var pagination: Pagination? = null)

/** min = index of the first record, max = index after the last one (page 1 is 0..10). */
data class Pagination(
    val min: Int? = null,
    val max: Int? = null,
    val count: Int? = null,
    val total: Int? = null,
    val has_more: Boolean? = null
)
