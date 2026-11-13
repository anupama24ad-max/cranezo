package com.cranezo.ui.myprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassDateField
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.GlassRadioGroup
import com.cranezo.ui.components.GlassSelectField
import com.cranezo.ui.components.ProfileAvatarPicker
import com.cranezo.ui.components.SelectOption
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.myprofile.components.StatusBadge
import com.cranezo.ui.myprofile.model.MyProfileIntent
import com.cranezo.ui.myprofile.model.MyProfileUiState
import com.cranezo.ui.myprofile.model.NamedItem
import com.cranezo.ui.myprofile.model.PKey
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.ui.theme.*
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController

private fun isBusinessName(name: String) = name.contains("business", ignoreCase = true)

/** Known dropdown names are shown translated, anything else as the server names it. */
@Composable
private fun optionLabel(name: String): String = when {
    name.equals("Individual User", true) -> stringResource(R.string.user_type_individual_user)
    name.equals("Business User", true) -> stringResource(R.string.user_type_business_user)
    name.equals("Individual Operator", true) -> stringResource(R.string.user_type_individual_operator)
    name.equals("Owner Based", true) -> stringResource(R.string.user_type_owner_based)
    else -> name
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun PersonalInfoScreen(
    vm: MyProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(
        formReady = true,
        profile = ProfileData(role_id = 3, is_mobile_verified = true, contact = "+91 98765 43210"),
        form = mapOf(PKey.FULL_NAME to "Rahul")
    )
    val onIntent: (MyProfileIntent) -> Unit = { vm?.onIntent(it) }

    // Every time the screen opens the profile and the dropdowns are fetched again
    LaunchedEffect(Unit) { onIntent(MyProfileIntent.LoadPersonalInfo) }

    val photoPicker = rememberImagePickerController()
    LaunchedEffect(photoPicker.imageUrl) {
        val uri = photoPicker.imageUrl ?: return@LaunchedEffect
        onIntent(MyProfileIntent.PhotoSelected(uri))
        photoPicker.clear()
    }
    ImageSourceBottomSheet(photoPicker)

    MyProfileScaffold(
        title = stringResource(R.string.mp_personal_info),
        subtitle = stringResource(R.string.mp_personal_info_subtitle),
        onBack = onBack,
        bottom = if (state.formReady) {
            {
                CommonGradientButton(
                    text = stringResource(R.string.mp_save_changes),
                    onClick = { onIntent(MyProfileIntent.SaveProfile) },
                    enabled = !state.isSaving && !state.isPhotoUploading
                )
            }
        } else null
    ) {
        // the loader shows while the profile is on its way; "something went wrong" only after a load really failed
        val waitedLongState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        val waitedLong = waitedLongState.value
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(10_000)
            waitedLongState.value = true
        }
        if (!state.formReady && !state.personalLoadTried && !waitedLong) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = dimen_60), contentAlignment = Alignment.Center) {
                com.cranezo.ui.components.CranezoLoader()
            }
            return@MyProfileScaffold
        }
        if (!state.formReady) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimen_60),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // the moving "no internet" page (it is almost always the reason this form did not load)
                com.cranezo.ui.components.NoInternetState(onRetry = { onIntent(MyProfileIntent.LoadPersonalInfo) })
            }
            return@MyProfileScaffold
        }

        val profile = state.profile
        val role = UserRole.fromId(profile?.role_id) ?: UserRole.CUSTOMER

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val previewContext = androidx.compose.ui.platform.LocalContext.current
            ProfileAvatarPicker(
                onAddPhotoClick = photoPicker::openChooser,
                photoUri = state.photoUri,
                isUploading = state.isPhotoUploading,
                onPhotoClick = {
                    state.photoUri?.let { previewContext.startActivity(com.cranezo.ui.preview.FilePreviewActivity.intent(previewContext, it)) }
                }
            )
        }

        GlassCard(contentPadding = dimen_14) {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_14)) {
                BasicDetails(state, role, onIntent)
            }
        }

        when (role) {
            // individual customers: no Aadhaar / PAN section any more
            UserRole.CUSTOMER -> if (state.isBusiness) BusinessSection(state, onIntent)

            UserRole.OPERATOR -> OperatorSection(state, onIntent)
            UserRole.CRANE_OWNER -> OwnerSection(profile)
        }

        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun BasicDetails(
    state: MyProfileUiState,
    role: UserRole,
    onIntent: (MyProfileIntent) -> Unit
) {
    GlassInputField(
        label = stringResource(R.string.field_full_name),
        value = state.form[PKey.FULL_NAME].orEmpty(),
        onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.FULL_NAME, it)) },
        placeholder = stringResource(R.string.field_full_name_hint),
        leftIcon = Icons.Default.Person,
        capitalization = KeyboardCapitalization.Words
    )

    // The mobile number can never be changed
    GlassInputField(
        label = stringResource(R.string.mp_mobile_number),
        value = state.profile?.contact.orEmpty(),
        onValueChange = {},
        placeholder = "",
        leftIcon = Icons.Default.Phone,
        readOnly = true,
        rightContent = {
            if (state.profile?.is_mobile_verified == true) {
                Textview(
                    text = stringResource(R.string.common_verified),
                    size = font_11,
                    color = SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
    Textview(
        text = stringResource(R.string.mp_mobile_cannot_change),
        size = font_11,
        color = appColors.textHint
    )

    GlassInputField(
        label = stringResource(R.string.field_email),
        value = state.form[PKey.EMAIL].orEmpty(),
        onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.EMAIL, it)) },
        placeholder = stringResource(R.string.field_email_hint),
        leftIcon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
    )

    if (role == UserRole.CUSTOMER) UserTypeRadio(state, onIntent)
}

@Composable
private fun UserTypeRadio(state: MyProfileUiState, onIntent: (MyProfileIntent) -> Unit) {
    val names = state.options[11].orEmpty().map { it.name }.ifEmpty { listOf("Individual User", "Business User") }
    val selected = names.firstOrNull { isBusinessName(it) == state.isBusiness }.orEmpty()
    GlassRadioGroup(
        label = stringResource(R.string.field_user_type),
        options = names,
        selected = selected,
        onSelect = { onIntent(MyProfileIntent.UserTypeChanged(isBusinessName(it))) },
        displayName = { optionLabel(it) }
    )
}

/** A dropdown whose entries come from POST /dropdowns, keyed by the request field it fills. */
@Composable
private fun ChoiceField(
    state: MyProfileUiState,
    key: String,
    label: String,
    hint: String,
    onIntent: (MyProfileIntent) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val options: List<SelectOption> = state.options[PKey.DROPDOWN_TYPE[key]].orEmpty()
    GlassSelectField(
        label = label,
        value = state.form[key].orEmpty(),
        placeholder = hint,
        options = options,
        leftIcon = icon,
        onSelected = { onIntent(MyProfileIntent.OptionSelected(key, it)) }
    )
}

@Composable
private fun IndividualSection(
    state: MyProfileUiState,
    profile: ProfileData?,
    onIntent: (MyProfileIntent) -> Unit
) {
    val individual = profile?.customer?.individual
    GlassCard(contentPadding = dimen_14) {
        Column(verticalArrangement = Arrangement.spacedBy(dimen_14)) {
            GlassInputField(
                label = stringResource(R.string.field_aadhar),
                value = state.form[PKey.AADHAR].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.AADHAR, it.take(14))) },
                placeholder = stringResource(R.string.field_aadhar_hint),
                leftIcon = Icons.Default.Badge,
                keyboardType = KeyboardType.Number,
                rightContent = { StatusBadge(individual?.aadhar_status, serverLabel = individual?.aadhar_status_label) }
            )
            GlassInputField(
                label = stringResource(R.string.field_pan),
                value = state.form[PKey.PAN].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.PAN, it.take(10).uppercase())) },
                placeholder = stringResource(R.string.field_pan_hint),
                leftIcon = Icons.Default.CreditCard,
                capitalization = KeyboardCapitalization.Characters,
                rightContent = { StatusBadge(individual?.pan_status, serverLabel = individual?.pan_status_label) }
            )
            if (state.showReverifyNote) {
                Textview(
                    text = stringResource(R.string.mp_reverify_note),
                    size = font_11,
                    color = AmberPrimary
                )
            }
        }
    }

    GlassCard(contentPadding = dimen_14) {
        Column(verticalArrangement = Arrangement.spacedBy(dimen_14)) {
            GlassInputField(
                label = stringResource(R.string.field_city),
                value = state.form[PKey.CITY].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.CITY, it)) },
                placeholder = stringResource(R.string.field_city_hint),
                leftIcon = Icons.Default.LocationOn,
                capitalization = KeyboardCapitalization.Words
            )
            ChoiceField(
                state, PKey.STATE, stringResource(R.string.field_state),
                stringResource(R.string.field_state_select_hint), onIntent, Icons.Default.LocationCity
            )
        }
    }
}

@Composable
private fun BusinessSection(state: MyProfileUiState, onIntent: (MyProfileIntent) -> Unit) {
    GlassCard(contentPadding = dimen_14) {
        Column(verticalArrangement = Arrangement.spacedBy(dimen_14)) {
            GlassInputField(
                label = stringResource(R.string.field_company_name),
                value = state.form[PKey.COMPANY].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.COMPANY, it)) },
                placeholder = stringResource(R.string.field_company_name_hint),
                leftIcon = Icons.Default.Business,
                capitalization = KeyboardCapitalization.Words
            )
            GlassInputField(
                label = stringResource(R.string.field_company_address),
                value = state.form[PKey.ADDRESS].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.ADDRESS, it)) },
                placeholder = stringResource(R.string.field_company_address_hint),
                leftIcon = Icons.Default.LocationOn
            )
            ChoiceField(
                state, PKey.CONSTITUTION, stringResource(R.string.field_constitution),
                stringResource(R.string.field_constitution_hint), onIntent
            )
            GlassDateField(
                label = stringResource(R.string.field_established_on),
                value = state.form[PKey.ESTABLISHED].orEmpty(),
                placeholder = stringResource(R.string.field_established_on_hint),
                onDateSelected = { onIntent(MyProfileIntent.FieldChanged(PKey.ESTABLISHED, it)) },
                allowFutureDates = false
            )
            GlassInputField(
                label = stringResource(R.string.field_gst),
                value = state.form[PKey.GST].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.GST, it.take(15).uppercase())) },
                placeholder = stringResource(R.string.field_gst_hint),
                capitalization = KeyboardCapitalization.Characters
            )
            ChoiceField(
                state, PKey.NATURE, stringResource(R.string.field_nature_of_business),
                stringResource(R.string.field_nature_of_business_hint), onIntent
            )
            GlassInputField(
                label = stringResource(R.string.field_directors),
                value = state.form[PKey.DIRECTORS].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.DIRECTORS, it)) },
                placeholder = stringResource(R.string.field_directors_hint)
            )
            GlassInputField(
                label = stringResource(R.string.field_accounts_head),
                value = state.form[PKey.FINANCE_HEAD].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.FINANCE_HEAD, it)) },
                placeholder = stringResource(R.string.field_accounts_head_hint)
            )
            GlassInputField(
                label = stringResource(R.string.field_branch_offices),
                value = state.form[PKey.BRANCHES].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.BRANCHES, it)) },
                placeholder = stringResource(R.string.field_branch_offices_hint)
            )
        }
    }
}

@Composable
private fun OperatorSection(state: MyProfileUiState, onIntent: (MyProfileIntent) -> Unit) {
    val license = state.profile?.operator
    val typeName = state.form[PKey.OPERATOR_TYPE].orEmpty()
    GlassCard(contentPadding = dimen_14) {
        Column(verticalArrangement = Arrangement.spacedBy(dimen_14)) {
            GlassInputField(
                label = stringResource(R.string.field_license_number),
                value = state.form[PKey.LICENSE_NUMBER].orEmpty(),
                onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.LICENSE_NUMBER, it.uppercase())) },
                placeholder = stringResource(R.string.field_license_number_hint),
                leftIcon = Icons.Default.Badge,
                capitalization = KeyboardCapitalization.Characters
            )
            GlassDateField(
                label = stringResource(R.string.field_license_expiry),
                value = state.form[PKey.LICENSE_EXPIRY].orEmpty(),
                placeholder = stringResource(R.string.field_license_expiry_hint),
                onDateSelected = { onIntent(MyProfileIntent.FieldChanged(PKey.LICENSE_EXPIRY, it)) },
                allowPastDates = false
            )
            if (license?.license_expired == true) {
                Textview(
                    text = stringResource(R.string.mp_license_expired_note),
                    size = font_11,
                    color = Red
                )
            }
            ChoiceField(
                state, PKey.EXPERIENCE, stringResource(R.string.field_experience),
                stringResource(R.string.field_experience_hint), onIntent
            )
            ChoiceField(
                state, PKey.OPERATOR_TYPE, stringResource(R.string.field_user_type),
                stringResource(R.string.field_user_type), onIntent
            )
            // Only "Owner Based" operators belong to a crane owner
            if (typeName.contains("owner", ignoreCase = true)) {
                GlassInputField(
                    label = stringResource(R.string.field_owner_id),
                    value = state.form[PKey.OWNER_ID].orEmpty(),
                    onValueChange = { onIntent(MyProfileIntent.FieldChanged(PKey.OWNER_ID, it.uppercase())) },
                    placeholder = stringResource(R.string.field_owner_id_hint),
                    capitalization = KeyboardCapitalization.Characters
                )
            }
        }
    }
}

/** Owners only edit name, photo and email for now; their company details are shown read only. */
@Composable
private fun OwnerSection(profile: ProfileData?) {
    val owner = profile?.owner ?: return
    val rows = listOf(
        R.string.field_company_name to owner.company_name,
        R.string.field_business_address to owner.business_address,
        R.string.field_city to owner.city,
        R.string.field_state to owner.state,
        R.string.field_pincode to owner.pincode
    ).filter { !it.second.isNullOrBlank() }
    if (rows.isEmpty()) return
    GlassCard(contentPadding = dimen_14) {
        Column(verticalArrangement = Arrangement.spacedBy(dimen_10)) {
            rows.forEach { (label, value) ->
                Column {
                    Textview(text = stringResource(label), size = font_11, color = appColors.textSecondary)
                    Textview(
                        text = value.orEmpty(),
                        size = font_14,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
