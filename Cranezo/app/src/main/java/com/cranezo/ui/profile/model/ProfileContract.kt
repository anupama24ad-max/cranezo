package com.cranezo.ui.profile.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.cranezo.R
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.SelectOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Fields, documents and option sources
// ---------------------------------------------------------------------------------------------

/** Every field any role's profile form can show. Full name and mobile are common to all roles. */
enum class ProfileField {
    FULL_NAME, MOBILE, COMPANY_NAME,
    COMPANY_ADDRESS, CONSTITUTION, ESTABLISHED_ON, GST_NUMBER, NATURE_OF_BUSINESS,
    DIRECTORS, ACCOUNTS_HEAD, BRANCH_OFFICES,
    AADHAR, PAN, CITY, STATE,
    LICENSE_NUMBER, LICENSE_EXPIRY, EXPERIENCE, OWNER_ID,
    OWNER_TYPE, PARTNERS, OWNERSHIP_TYPE, BUSINESS_CATEGORY, PINCODE, EMAIL
}

/**
 * Documents an operator uploads. [apiField] is the updateProfile field that receives the url
 * returned by POST /api/upload.
 */
enum class ProfileDocument(
    @StringRes val titleRes: Int,
    val apiField: String,
    val required: Boolean
) {
    OPERATOR_LICENSE_FRONT(R.string.doc_license_front, "operator_license_front", true),
    OPERATOR_LICENSE_BACK(R.string.doc_license_back, "operator_license_back", true),
    VOTER_ID(R.string.doc_voter_id, "voter_id_file", true),
    AADHAAR_CARD(R.string.doc_aadhaar, "aadhar_card_file", false),
    PAN_CARD(R.string.doc_pan, "pan_card_file", false),
    OTHER(R.string.doc_other, "other_document_file", false);

    @get:StringRes val subtitleRes: Int
        get() = if (required) R.string.doc_required else R.string.doc_optional
}

enum class FieldKind { TEXT, NUMBER, MULTILINE, EMAIL, SELECT, DATE }

/**
 * Where a dropdown gets its options from: POST /api/dropdowns { "type": [type] }.
 * Ids are never hardcoded, admins can change the options at any time.
 */
enum class OptionSource(val type: Int?) {
    NONE(null),
    OWNER_TYPE(2),
    EXPERIENCE(3),
    PARTNERS(4),
    OWNERSHIP_TYPE(5),
    OPERATOR_TYPE(10),
    CUSTOMER_TYPE(11),
    CONSTITUTION(12),
    NATURE_OF_BUSINESS(13),
    STATE(14)
}

/**
 * Names of the user type options. The form logic compares names (not ids), as the ids come from
 * the server.
 */
object UserTypes {
    const val INDIVIDUAL_USER = "Individual User"
    const val BUSINESS_USER = "Business User"
    const val INDIVIDUAL_OPERATOR = "Individual Operator"
    const val OWNER_BASED = "Owner Based"
}

// ---------------------------------------------------------------------------------------------
// Form schema: what each role sees
// ---------------------------------------------------------------------------------------------

sealed interface ProfileFormItem {
    data class Section(
        @StringRes val title: Int,
        val icon: ImageVector,
        @StringRes val subtitle: Int? = null
    ) : ProfileFormItem

    data class Input(
        val field: ProfileField,
        @StringRes val label: Int,
        @StringRes val placeholder: Int,
        val icon: ImageVector,
        val kind: FieldKind = FieldKind.TEXT,
        val source: OptionSource = OptionSource.NONE,
        val required: Boolean = false,
        val verifiable: Boolean = false,
        val allowPastDates: Boolean = true,
        val allowFutureDates: Boolean = true
    ) : ProfileFormItem

    /** The user type radio. Options come from [source], [fallback] shows until they are loaded. */
    data class Choice(
        @StringRes val label: Int,
        val source: OptionSource,
        val fallback: List<String>
    ) : ProfileFormItem

    data class Documents(val documents: List<ProfileDocument>) : ProfileFormItem
}

/**
 * Describes the profile form of every role. The screen just renders whatever [items] returns,
 * so adding or changing a field for a role only needs a change here.
 */
object ProfileFormSchema {

    @StringRes
    fun subtitle(role: UserRole): Int = when (role) {
        UserRole.CRANE_OWNER -> R.string.profile_subtitle_owner
        UserRole.CUSTOMER, UserRole.OPERATOR -> R.string.profile_subtitle_default
    }

    /** Dropdown that holds the "user type" radio options of a role. */
    fun userTypeSource(role: UserRole): OptionSource = when (role) {
        UserRole.CUSTOMER -> OptionSource.CUSTOMER_TYPE
        UserRole.OPERATOR -> OptionSource.OPERATOR_TYPE
        UserRole.CRANE_OWNER -> OptionSource.NONE
    }

    fun defaultUserType(role: UserRole): String = when (role) {
        UserRole.CUSTOMER -> UserTypes.BUSINESS_USER
        UserRole.OPERATOR -> UserTypes.INDIVIDUAL_OPERATOR
        UserRole.CRANE_OWNER -> ""
    }

    private fun fallbackUserTypes(role: UserRole): List<String> = when (role) {
        UserRole.CUSTOMER -> listOf(UserTypes.INDIVIDUAL_USER, UserTypes.BUSINESS_USER)
        UserRole.OPERATOR -> listOf(UserTypes.INDIVIDUAL_OPERATOR, UserTypes.OWNER_BASED)
        UserRole.CRANE_OWNER -> emptyList()
    }

    /** Every dropdown a role's form needs. Loaded together when the screen opens. */
    fun remoteSources(role: UserRole): List<OptionSource> = when (role) {
        UserRole.CRANE_OWNER -> listOf(
            OptionSource.OWNER_TYPE, OptionSource.EXPERIENCE,
            OptionSource.PARTNERS, OptionSource.OWNERSHIP_TYPE
        )

        UserRole.CUSTOMER -> listOf(
            OptionSource.CUSTOMER_TYPE, OptionSource.CONSTITUTION,
            OptionSource.NATURE_OF_BUSINESS, OptionSource.STATE
        )

        UserRole.OPERATOR -> listOf(OptionSource.OPERATOR_TYPE, OptionSource.EXPERIENCE)
    }

    fun items(role: UserRole, userType: String): List<ProfileFormItem> = when (role) {
        UserRole.CRANE_OWNER -> ownerItems()
        UserRole.CUSTOMER -> customerItems(userType)
        UserRole.OPERATOR -> operatorItems(userType)
    }

    /** Fields visible for a role and user type, used to clear the ones that get hidden. */
    fun fields(role: UserRole, userType: String): Set<ProfileField> =
        items(role, userType).filterIsInstance<ProfileFormItem.Input>().map { it.field }.toSet()

    // Full name and mobile are drawn by the screen above these. Only the company name is required,
    // everything else is optional.
    private fun ownerItems() = listOf(
        ProfileFormItem.Input(
            ProfileField.EMAIL, R.string.field_email, R.string.field_email_hint,
            Icons.Default.Email, FieldKind.EMAIL
        ),
        ProfileFormItem.Input(
            field = ProfileField.COMPANY_NAME,
            label = R.string.field_company_name,
            placeholder = R.string.field_company_name_hint,
            icon = Icons.Default.Business,
            required = true
        ),
        ProfileFormItem.Input(
            ProfileField.OWNER_TYPE, R.string.field_owner_type, R.string.field_owner_type_hint,
            Icons.Default.Person, FieldKind.SELECT, OptionSource.OWNER_TYPE
        ),
        ProfileFormItem.Input(
            ProfileField.EXPERIENCE, R.string.field_experience, R.string.field_experience_hint,
            Icons.Default.Work, FieldKind.SELECT, OptionSource.EXPERIENCE
        ),
        ProfileFormItem.Input(
            ProfileField.PARTNERS, R.string.field_partners, R.string.field_partners_hint,
            Icons.Default.Groups, FieldKind.SELECT, OptionSource.PARTNERS
        ),
        ProfileFormItem.Input(
            ProfileField.OWNERSHIP_TYPE, R.string.field_ownership_type, R.string.field_ownership_type_hint,
            Icons.Default.AccountBalance, FieldKind.SELECT, OptionSource.OWNERSHIP_TYPE
        ),
        ProfileFormItem.Input(
            ProfileField.BUSINESS_CATEGORY, R.string.field_business_category, R.string.field_business_category_hint,
            Icons.Default.Category
        ),
        ProfileFormItem.Input(
            ProfileField.ESTABLISHED_ON, R.string.field_established_on, R.string.field_established_on_hint,
            Icons.Default.CalendarMonth, FieldKind.DATE, allowFutureDates = false
        ),
        // For owners the state is plain text (customers pick it from the state dropdown)
        ProfileFormItem.Input(
            ProfileField.STATE, R.string.field_state, R.string.field_state_text_hint,
            Icons.Default.Map
        ),
        ProfileFormItem.Input(
            ProfileField.CITY, R.string.field_city, R.string.field_city_hint,
            Icons.Default.LocationOn
        ),
        ProfileFormItem.Input(
            ProfileField.PINCODE, R.string.field_pincode, R.string.field_pincode_hint,
            Icons.Default.LocationOn, FieldKind.NUMBER
        ),
        ProfileFormItem.Input(
            ProfileField.COMPANY_ADDRESS, R.string.field_business_address, R.string.field_business_address_hint,
            Icons.Default.LocationOn, FieldKind.MULTILINE
        )
    )

    private fun customerItems(userType: String): List<ProfileFormItem> {
        val userTypeChoice = ProfileFormItem.Choice(
            label = R.string.field_user_type,
            source = OptionSource.CUSTOMER_TYPE,
            fallback = fallbackUserTypes(UserRole.CUSTOMER)
        )
        return if (userType == UserTypes.BUSINESS_USER) {
            listOf(
                userTypeChoice,
                ProfileFormItem.Input(
                    ProfileField.COMPANY_NAME, R.string.field_company_name, R.string.field_company_name_hint,
                    Icons.Default.Business, required = true
                ),
                ProfileFormItem.Input(
                    ProfileField.COMPANY_ADDRESS, R.string.field_company_address, R.string.field_company_address_hint,
                    Icons.Default.LocationOn, required = true
                ),
                ProfileFormItem.Input(
                    ProfileField.CONSTITUTION, R.string.field_constitution, R.string.field_constitution_hint,
                    Icons.Default.AccountBalance, FieldKind.SELECT, OptionSource.CONSTITUTION,
                    required = true
                ),
                ProfileFormItem.Input(
                    ProfileField.ESTABLISHED_ON, R.string.field_established_on, R.string.field_established_on_hint,
                    Icons.Default.CalendarMonth, FieldKind.DATE,
                    required = true, allowFutureDates = false
                ),
                ProfileFormItem.Input(
                    ProfileField.GST_NUMBER, R.string.field_gst, R.string.field_gst_hint,
                    Icons.Default.Description, required = true
                ),
                ProfileFormItem.Input(
                    ProfileField.NATURE_OF_BUSINESS, R.string.field_nature_of_business, R.string.field_nature_of_business_hint,
                    Icons.Default.Work, FieldKind.SELECT, OptionSource.NATURE_OF_BUSINESS,
                    required = true
                ),
                ProfileFormItem.Input(
                    ProfileField.DIRECTORS, R.string.field_directors,
                    R.string.field_directors_hint,
                    Icons.Default.Groups, FieldKind.MULTILINE
                ),
                ProfileFormItem.Input(
                    ProfileField.ACCOUNTS_HEAD, R.string.field_accounts_head,
                    R.string.field_accounts_head_hint,
                    Icons.Default.Person, FieldKind.MULTILINE
                ),
                ProfileFormItem.Input(
                    ProfileField.BRANCH_OFFICES, R.string.field_branch_offices,
                    R.string.field_branch_offices_hint,
                    Icons.Default.LocationOn, FieldKind.MULTILINE
                )
            )
        } else {
            listOf(
                userTypeChoice,
                // Aadhaar and PAN are not asked from individual customers any more
                ProfileFormItem.Input(
                    ProfileField.CITY, R.string.field_city, R.string.field_city_hint,
                    Icons.Default.LocationOn, required = true
                ),
                ProfileFormItem.Input(
                    ProfileField.STATE, R.string.field_state, R.string.field_state_select_hint,
                    Icons.Default.Map, FieldKind.SELECT, OptionSource.STATE, required = true
                )
            )
        }
    }

    private fun operatorItems(userType: String): List<ProfileFormItem> = buildList {
        add(
            ProfileFormItem.Section(
                title = R.string.section_license,
                icon = Icons.Default.Badge
            )
        )
        add(
            ProfileFormItem.Input(
                ProfileField.LICENSE_NUMBER, R.string.field_license_number, R.string.field_license_number_hint,
                Icons.Default.Badge, required = true, verifiable = true
            )
        )
        add(
            ProfileFormItem.Input(
                ProfileField.LICENSE_EXPIRY, R.string.field_license_expiry, R.string.field_license_expiry_hint,
                Icons.Default.CalendarMonth, FieldKind.DATE, required = true, allowPastDates = false
            )
        )
        add(
            ProfileFormItem.Input(
                ProfileField.EXPERIENCE, R.string.field_experience, R.string.field_experience_hint,
                Icons.Default.Work, FieldKind.SELECT, OptionSource.EXPERIENCE, required = true
            )
        )
        add(
            ProfileFormItem.Section(
                title = R.string.section_documents,
                icon = Icons.Default.UploadFile,
                subtitle = R.string.section_documents_subtitle
            )
        )
        add(ProfileFormItem.Documents(ProfileDocument.entries))
        add(
            ProfileFormItem.Choice(
                label = R.string.field_user_type,
                source = OptionSource.OPERATOR_TYPE,
                fallback = fallbackUserTypes(UserRole.OPERATOR)
            )
        )
        if (userType == UserTypes.OWNER_BASED) {
            add(
                ProfileFormItem.Input(
                    ProfileField.OWNER_ID, R.string.field_owner_id, R.string.field_owner_id_hint,
                    Icons.Default.Badge, required = true
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Validation (mirrors the server so users get instant feedback; the server stays the source of truth)
// ---------------------------------------------------------------------------------------------

object ProfileValidation {
    private val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val GST_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private const val AADHAR_LENGTH = 12

    /** Today as yyyy-MM-dd, the format of every date sent to the API. */
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun aadharDigits(raw: String): String = raw.filter { it.isDigit() }

    fun isValidAadhar(raw: String) = aadharDigits(raw).length == AADHAR_LENGTH

    fun isValidPan(raw: String) = PAN_REGEX.matches(raw.trim().uppercase())

    fun isValidGst(raw: String) = GST_REGEX.matches(raw.trim().uppercase())

    fun isValidEmail(raw: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(raw.trim()).matches()

    fun isValidPincode(raw: String) = raw.length == 6 && raw.all { it.isDigit() }

    /** Dates are yyyy-MM-dd, so comparing them as text compares them as dates. */
    fun isNotInPast(isoDate: String) = isoDate >= today()

    fun isNotInFuture(isoDate: String) = isoDate <= today()
}

// ---------------------------------------------------------------------------------------------
// MVI contract
// ---------------------------------------------------------------------------------------------

/**
 * A file the user picked and its upload progress. [displayUri] is what the avatar / tile shows,
 * [path] is the url returned by POST /api/upload (e.g. "/public/uploads/x.png"), sent to updateProfile.
 */
data class FileUpload(
    val displayUri: String,
    val path: String? = null,
    val isUploading: Boolean = false
)

data class ProfileUiState(
    val role: UserRole = UserRole.CRANE_OWNER,
    // Name of the chosen user type option (Business User, Owner Based, ...)
    val userType: String = ProfileFormSchema.defaultUserType(role),
    val mobileNumber: String = "",
    val values: Map<ProfileField, String> = emptyMap(),
    // id of the option chosen in a SELECT field (sent to the backend)
    val selectedOptionIds: Map<ProfileField, Int> = emptyMap(),
    // Options of every dropdown, filled from the dropdown API
    val options: Map<OptionSource, List<SelectOption>> = emptyMap(),
    val photo: FileUpload? = null,
    val documents: Map<ProfileDocument, FileUpload> = emptyMap()
) {
    @get:StringRes
    val subtitleRes: Int get() = ProfileFormSchema.subtitle(role)
    val formItems: List<ProfileFormItem> get() = ProfileFormSchema.items(role, userType)

    fun value(field: ProfileField): String = values[field].orEmpty()

    /** Id of the chosen user type, null until the dropdown has loaded. */
    val userTypeId: Int?
        get() = options[ProfileFormSchema.userTypeSource(role)]
            ?.firstOrNull { it.name == userType }?.id

    val isUploading: Boolean
        get() = photo?.isUploading == true || documents.values.any { it.isUploading }

    /** True when everything required for the role and user type is filled. Enables Continue. */
    val canContinue: Boolean
        get() {
            if (isUploading) return false
            if (value(ProfileField.FULL_NAME).isBlank()) return false
            if (ProfileFormSchema.userTypeSource(role) != OptionSource.NONE && userTypeId == null) return false

            val inputsFilled = formItems.filterIsInstance<ProfileFormItem.Input>()
                .filter { it.required }
                .all { value(it.field).isNotBlank() }
            if (!inputsFilled) return false

            val documentsUploaded = formItems.filterIsInstance<ProfileFormItem.Documents>()
                .flatMap { it.documents }
                .filter { it.required }
                .all { documents[it]?.path != null }
            return documentsUploaded
        }
}

sealed interface ProfileIntent {
    data class FieldChanged(val field: ProfileField, val value: String) : ProfileIntent
    data class OptionSelected(val field: ProfileField, val option: SelectOption) : ProfileIntent
    data class UserTypeChanged(val userType: String) : ProfileIntent
    data class PhotoSelected(val uri: String) : ProfileIntent
    data class DocumentSelected(val document: ProfileDocument, val uri: String) : ProfileIntent
    data class RetryOptions(val source: OptionSource) : ProfileIntent
    data object SaveProfile : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToHome : ProfileEffect
}
