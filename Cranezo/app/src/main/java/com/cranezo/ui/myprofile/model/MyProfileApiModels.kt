package com.cranezo.ui.myprofile.model

/**
 * Response models of the My Profile APIs. Every field is nullable: the server sends null for what
 * the user has not filled in, and the app must not crash on it.
 */

data class NamedItem(val id: Int? = null, val name: String? = null)

data class ProfileData(
    val id: String? = null,
    val member_id: String? = null,
    val role_id: Int? = null,
    val role_name: String? = null,
    val designation: String? = null,
    val full_name: String? = null,
    val company_name: String? = null,
    val user_email: String? = null,
    val country_code: String? = null,
    val mobile_number: String? = null,
    val contact: String? = null,
    val is_mobile_verified: Boolean? = null,
    val mobile_can_change: Boolean? = null,
    val profile_image: String? = null,
    // system | light | dark
    val appearance: String? = null,
    val language: LanguageRef? = null,
    val customer: CustomerBlock? = null,
    val operator: OperatorBlock? = null,
    val owner: OwnerBlock? = null
)

data class LanguageRef(val id: Int? = null, val name: String? = null, val description: String? = null)

// ---- Customer ----

data class CustomerBlock(
    val customer_type: NamedItem? = null,
    val is_business: Boolean? = null,
    val individual: IndividualBlock? = null,
    val business: BusinessBlock? = null
)

data class IndividualBlock(
    val aadhar_number: String? = null,
    val aadhar_status: Int? = null,
    val aadhar_status_label: String? = null,
    val pan_number: String? = null,
    val pan_status: Int? = null,
    val pan_status_label: String? = null,
    val city: String? = null,
    val state_id: Int? = null,
    val state: String? = null
)

data class BusinessBlock(
    val company_name: String? = null,
    val business_address: String? = null,
    val constitution: NamedItem? = null,
    val company_date_establishment: String? = null,
    val gst_number: String? = null,
    val nature_of_business: NamedItem? = null,
    val directors_details: String? = null,
    val finance_head_details: String? = null,
    val branch_offices_details: String? = null
)

// ---- Operator ----

data class OperatorBlock(
    val operator_type: NamedItem? = null,
    val years_of_experience: NamedItem? = null,
    val license_number: String? = null,
    val license_expiry_date: String? = null,
    val license_expired: Boolean? = null,
    val owner: OperatorOwner? = null,
    val assigned_cranes_count: Int? = null,
    val assigned_crane: AssignedCrane? = null,
    val documents: List<OperatorDocument>? = null
)

data class OperatorOwner(
    val member_id: String? = null,
    val full_name: String? = null,
    val company_name: String? = null
)

data class AssignedCrane(
    val id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val model_name: String? = null,
    val capacity_name: String? = null,
    val crane_type_name: String? = null,
    val fuel_type_name: String? = null,
    val primary_photo: String? = null,
    val location: String? = null,
    val approval_status: Int? = null,
    val approval_label: String? = null
)

data class OperatorDocument(
    val key: String? = null,
    val label: String? = null,
    val subtitle: String? = null,
    val valid_till: String? = null,
    val expired: Boolean? = null,
    val file_url: String? = null,
    val file_url_back: String? = null,
    val uploaded: Boolean? = null,
    // 1 Verified, 0 Pending, 2 Rejected, null Not Uploaded
    val status: Int? = null,
    val status_label: String? = null
)

// ---- Owner ----

data class OwnerBlock(
    val company_name: String? = null,
    val business_address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null
)

// ---- Language / Appearance / Notifications ----

data class LanguageOption(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val selected: Boolean? = null
)

data class AppearanceOption(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val selected: Boolean? = null
)

data class NotificationGroup(
    val group: String? = null,
    val items: List<NotificationItem>? = null
)

data class NotificationItem(
    val key: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val enabled: Boolean? = null
)

// ---- Terms & Conditions / Privacy Policy ----

data class LegalContent(
    val type: String? = null,
    val title: String? = null,
    val intro: String? = null,
    val last_updated: String? = null,
    val sections: List<LegalSection>? = null
)

data class LegalSection(
    val number: Int? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val body: String? = null
)
