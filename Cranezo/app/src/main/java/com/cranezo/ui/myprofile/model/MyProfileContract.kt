package com.cranezo.ui.myprofile.model

import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.components.SelectOption

/** Request keys of POST /profile/update, also used as the keys of the Personal Information form. */
object PKey {
    const val FULL_NAME = "full_name"
    const val EMAIL = "user_email"

    // Customer: individual
    const val CUSTOMER_TYPE = "customer_type"
    const val AADHAR = "aadhar_number"
    const val PAN = "pan_number"
    const val CITY = "city"
    const val STATE = "state_id"

    // Customer: business
    const val COMPANY = "company_name"
    const val ADDRESS = "business_address"
    const val CONSTITUTION = "constitution"
    const val ESTABLISHED = "company_date_establishment"
    const val GST = "gst_number"
    const val NATURE = "nature_of_business"
    const val DIRECTORS = "directors_details"
    const val FINANCE_HEAD = "finance_head_details"
    const val BRANCHES = "branch_offices_details"

    // Operator
    const val LICENSE_NUMBER = "operator_license_number"
    const val LICENSE_EXPIRY = "license_expiry_date"
    const val EXPERIENCE = "years_of_experience"
    const val OPERATOR_TYPE = "operator_type"
    const val OWNER_ID = "owner_id"

    // Operator documents: sent alone, after the file was uploaded
    const val LICENSE_FRONT = "operator_license_front"
    const val LICENSE_BACK = "operator_license_back"
    const val VOTER_FILE = "voter_id_file"
    const val AADHAR_FILE = "aadhar_card_file"
    const val PAN_FILE = "pan_card_file"
    const val OTHER_FILE = "other_document_file"

    const val PROFILE_IMAGE = "profile_image"

    val INDIVIDUAL = listOf(AADHAR, PAN, CITY, STATE)
    val INDIVIDUAL_REQUIRED = INDIVIDUAL
    val BUSINESS = listOf(COMPANY, ADDRESS, CONSTITUTION, ESTABLISHED, GST, NATURE, DIRECTORS, FINANCE_HEAD, BRANCHES)
    val BUSINESS_REQUIRED = listOf(COMPANY, ADDRESS, CONSTITUTION, ESTABLISHED, GST, NATURE)

    /** Dropdown (POST /dropdowns type) behind each choice field. */
    val DROPDOWN_TYPE = mapOf(
        CUSTOMER_TYPE to 11,
        CONSTITUTION to 12,
        NATURE to 13,
        STATE to 14,
        EXPERIENCE to 3,
        OPERATOR_TYPE to 10
    )
}

/** Status of a document, Aadhar or PAN (section "Status badges" of the API). */
object DocStatus {
    const val PENDING = 0
    const val VERIFIED = 1
    const val REJECTED = 2
}

data class MyProfileUiState(
    val profile: ProfileData? = null,

    // Personal Information form. Values are what the user sees; ids are the dropdown ids to send.
    val formReady: Boolean = false,
    // the first load of the form has finished (so "something went wrong" may show; until then the loader does)
    val personalLoadTried: Boolean = false,
    val form: Map<String, String> = emptyMap(),
    val formIds: Map<String, Int> = emptyMap(),
    val isBusiness: Boolean = false,
    val options: Map<Int, List<SelectOption>> = emptyMap(),
    val photoUri: String? = null,
    val isPhotoUploading: Boolean = false,
    val isSaving: Boolean = false,
    // Field of the operator document being uploaded
    val uploadingDocument: String? = null,
    // True once Aadhar or PAN was edited: they go back to Pending after saving
    val showReverifyNote: Boolean = false,

    // Language
    val languages: List<LanguageOption> = emptyList(),
    val selectedLanguageId: Int? = null,

    // Appearance
    val appearances: List<AppearanceOption> = emptyList(),
    val selectedAppearance: String? = null,

    // Notification settings: the list as the server sent it and the toggles as the user set them
    val notificationGroups: List<NotificationGroup> = emptyList(),
    val notificationPrefs: Map<String, Boolean> = emptyMap(),

    // Terms & Conditions / Privacy Policy
    val legal: LegalContent? = null,
    val legalLoaded: Boolean = false,

    // Operator: the crane behind "View Details"
    val assignedCrane: Crane? = null,
    val craneLoaded: Boolean = false
)

sealed interface MyProfileIntent {
    // Personal Information
    data object LoadPersonalInfo : MyProfileIntent
    data class FieldChanged(val key: String, val value: String) : MyProfileIntent
    data class OptionSelected(val key: String, val option: SelectOption) : MyProfileIntent
    data class UserTypeChanged(val isBusiness: Boolean) : MyProfileIntent
    data class PhotoSelected(val uri: String) : MyProfileIntent
    data object SaveProfile : MyProfileIntent

    // Operator documents. [field] is one of the PKey document keys.
    data object LoadDocuments : MyProfileIntent
    data class DocumentSelected(val field: String, val uri: String) : MyProfileIntent

    // Language
    data object LoadLanguages : MyProfileIntent
    data class SelectLanguage(val id: Int) : MyProfileIntent
    data object SaveLanguage : MyProfileIntent

    // Appearance
    data object LoadAppearance : MyProfileIntent
    data class SelectAppearance(val id: String) : MyProfileIntent
    data object SaveAppearance : MyProfileIntent
    // Left the screen without saving: undo the preview
    data object CancelAppearance : MyProfileIntent

    // Notification settings
    data object LoadNotifications : MyProfileIntent
    data class ToggleNotification(val key: String, val enabled: Boolean) : MyProfileIntent
    data object SaveNotifications : MyProfileIntent

    // "terms" or "privacy"
    data class LoadLegal(val type: String) : MyProfileIntent

    data class LoadAssignedCrane(val craneId: String) : MyProfileIntent
}

sealed interface MyProfileEffect {
    // Something was saved: leave the screen
    data object Saved : MyProfileEffect
    // The language changed: leave, and the screen behind restarts to show it
    data object LanguageSaved : MyProfileEffect
}
