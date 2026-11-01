package com.cranezo.api

/** `data` of POST /api/upload when a single file is sent (type = 1). */
data class UploadApiResponse(
    // Relative to the server root, e.g. "/public/uploads/profile-1792900000.png"
    val file_url: String? = null,
    val file_name: String? = null,
    val stored_name: String? = null,
    val file_path: String? = null,
    val file_size: Long? = null,
    val mime_type: String? = null,
    val upload_type: Int? = null,
    val timestamp: String? = null
)
