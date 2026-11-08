package com.cranezo.ui.cranes.model

// Response models of the Owner Cranes APIs. Every optional field can be null, so null-check before use.

/** Crane approval_status: 0 Under Review, 1 Verified, 2 Rejected. */
object ApprovalStatus {
    const val UNDER_REVIEW = 0
    const val VERIFIED = 1
    const val REJECTED = 2
}

/** availability_status: 0 Available, 1 Not Available. */
object Availability {
    const val AVAILABLE = 0
    const val NOT_AVAILABLE = 1
}

data class Crane(
    val id: String? = null,
    val crane_code: String? = null,
    val crane_type: Int? = null,
    val crane_type_name: String? = null,
    val capacity: Int? = null,
    val capacity_name: String? = null,
    val fuel_type: Int? = null,
    val fuel_type_name: String? = null,
    val model_name: String? = null,
    val registration_number: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val hourly_rate: Double? = null,
    val minimum_hours: Int? = null,
    val availability_status: Int? = null,
    val availability_label: String? = null,
    val approval_status: Int? = null,
    val approval_label: String? = null,
    val rejection_reason: String? = null,
    val status_message: String? = null,
    val submitted_at: String? = null,
    val expected_review_by: String? = null,
    val reviewed_at: String? = null,
    val primary_photo: String? = null,
    val photos: List<CranePhoto>? = null,
    val documents: List<CraneDocument>? = null,
    val operator: CraneOperator? = null,
    val owner: CraneOwner? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    /** The cover photo first, then the rest, without repeating a url. */
    val photoUrls: List<String>
        get() = (listOfNotNull(primary_photo) + photos.orEmpty().mapNotNull { it.photo_url }).distinct()
}

data class CranePhoto(val id: Int? = null, val photo_url: String? = null)

data class CraneDocument(
    val id: Int? = null,
    val document_type: Int? = null,
    val document_type_name: String? = null,
    val file_url: String? = null,
    val expiry_date: String? = null
)

data class CraneOperator(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val profile_image: String? = null
)

data class CraneOwner(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val company_name: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null
)

/** One dropdown entry (crane type, capacity, fuel type, document type, availability). */
data class FormOption(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    // crane documents: false = optional (Fitness Certificate, Form 38, Pollution Certificate)
    val required: Boolean? = null
)

/** `data` of POST /owner/cranes/form-options with an empty body. */
data class CraneFormOptionsResponse(
    val crane_types: List<FormOption>? = null,
    val capacities: List<FormOption>? = null,
    val fuel_types: List<FormOption>? = null,
    val document_types: List<FormOption>? = null,
    val operators: List<Operator>? = null,
    val availability_options: List<FormOption>? = null
)

data class Operator(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val user_email: String? = null,
    val profile_image: String? = null,
    val owner_id: String? = null,
    // "owner_added" or "registered"
    val source: String? = null,
    val is_mobile_verified: Boolean? = null
)
