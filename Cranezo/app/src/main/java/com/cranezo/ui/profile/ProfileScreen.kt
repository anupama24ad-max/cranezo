package com.cranezo.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.ActionPill
import com.cranezo.ui.components.CommonBackButton
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.DocumentUploadTile
import com.cranezo.ui.components.GlassDateField
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.GlassRadioGroup
import com.cranezo.ui.components.GlassSectionHeader
import com.cranezo.ui.components.GlassSelectField
import com.cranezo.ui.components.ProfileAvatarPicker
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.VerifiedBadge
import com.cranezo.ui.profile.model.FieldKind
import com.cranezo.ui.profile.model.ProfileDocument
import com.cranezo.ui.profile.model.ProfileField
import com.cranezo.ui.profile.model.ProfileFormItem
import com.cranezo.ui.profile.model.ProfileIntent
import com.cranezo.ui.profile.model.ProfileUiState
import com.cranezo.ui.profile.model.UserTypes
import com.cranezo.ui.theme.*
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController

@Preview(showBackground = true)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel? = null,
    onBackClick: () -> Unit = {}
) {
    val uiState = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: ProfileUiState(mobileNumber = "+91 98765 43210")

    // Profile photo: camera / gallery, no runtime permission needed
    val photoPicker = rememberImagePickerController()
    LaunchedEffect(photoPicker.imageUrl) {
        val uri = photoPicker.imageUrl ?: return@LaunchedEffect
        vm?.onIntent(ProfileIntent.PhotoSelected(uri))
        photoPicker.clear()
    }
    ImageSourceBottomSheet(photoPicker)

    // Documents: camera / gallery / files. Remember which tile opened the picker.
    val documentPicker = rememberImagePickerController(allowFiles = true)
    var pendingDocument by rememberSaveable { mutableStateOf<ProfileDocument?>(null) }
    LaunchedEffect(documentPicker.imageUrl) {
        val uri = documentPicker.imageUrl ?: return@LaunchedEffect
        pendingDocument?.let { vm?.onIntent(ProfileIntent.DocumentSelected(it, uri)) }
        pendingDocument = null
        documentPicker.clear()
    }
    ImageSourceBottomSheet(
        controller = documentPicker,
        title = stringResource(R.string.picker_document_title),
        subtitle = stringResource(R.string.picker_document_subtitle)
    )

    ProfileContent(
        uiState = uiState,
        onIntent = { vm?.onIntent(it) },
        onBackClick = onBackClick,
        onAddPhotoClick = photoPicker::openChooser,
        onDocumentClick = { document ->
            pendingDocument = document
            documentPicker.openChooser()
        }
    )
}

/**
 * Stateless profile form. The fields shown depend on [ProfileUiState.role] (and, for customers and
 * operators, on the chosen user type): see [com.cranezo.ui.profile.model.ProfileFormSchema].
 */
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onBackClick: () -> Unit = {},
    onAddPhotoClick: () -> Unit = {},
    onDocumentClick: (ProfileDocument) -> Unit = {}
) {
    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            CommonBackButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = dimen_20, top = dimen_14)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(dimen_24))

                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimen_20)
                ) {
                    Textview(
                        text = stringResource(R.string.profile_heading_1),
                        size = font_32,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(dimen_2))
                    Textview(
                        text = stringResource(R.string.profile_heading_2),
                        size = font_32,
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(dimen_10))
                    Textview(
                        text = stringResource(uiState.subtitleRes),
                        size = font_14,
                        color = appColors.textSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(dimen_26))

                // Profile photo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Textview(
                        text = stringResource(R.string.profile_photo_optional),
                        size = font_14,
                        color = appColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(dimen_14))
                    ProfileAvatarPicker(
                        onAddPhotoClick = onAddPhotoClick,
                        photoUri = uiState.photo?.displayUri,
                        isUploading = uiState.photo?.isUploading == true
                    )
                }

                Spacer(modifier = Modifier.height(dimen_26))

                // Form: common fields first, then whatever the role's schema asks for
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimen_20),
                    verticalArrangement = Arrangement.spacedBy(dimen_18)
                ) {
                    GlassInputField(
                        label = stringResource(R.string.field_full_name),
                        value = uiState.value(ProfileField.FULL_NAME),
                        onValueChange = { onIntent(ProfileIntent.FieldChanged(ProfileField.FULL_NAME, it)) },
                        placeholder = stringResource(R.string.field_full_name_hint),
                        leftIcon = Icons.Default.Person,
                        isRequired = true,
                        capitalization = KeyboardCapitalization.Words
                    )

                    GlassInputField(
                        label = stringResource(R.string.login_mobile_label),
                        value = uiState.mobileNumber,
                        onValueChange = {},
                        placeholder = stringResource(R.string.field_mobile_hint),
                        leftIcon = Icons.Default.Phone,
                        isRequired = true,
                        readOnly = true,
                        rightContent = { VerifiedBadge() }
                    )

                    uiState.formItems.forEach { item ->
                        ProfileFormItemView(
                            item = item,
                            uiState = uiState,
                            onIntent = onIntent,
                            onDocumentClick = onDocumentClick
                        )
                    }

                    Spacer(modifier = Modifier.height(dimen_2))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20)
                    .padding(bottom = dimen_24, top = dimen_8)
            ) {
                // Enabled once everything required for the role and user type is filled
                CommonGradientButton(
                    text = stringResource(R.string.common_continue),
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Color.Black,
                    enabled = uiState.canContinue,
                    onClick = { onIntent(ProfileIntent.SaveProfile) }
                )
            }
        }
    }
}

@Composable
private fun ProfileFormItemView(
    item: ProfileFormItem,
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onDocumentClick: (ProfileDocument) -> Unit
) {
    when (item) {
        is ProfileFormItem.Section -> GlassSectionHeader(
            title = stringResource(item.title),
            icon = item.icon,
            subtitle = item.subtitle?.let { stringResource(it) }
        )

        is ProfileFormItem.Choice -> {
            val loaded = uiState.options[item.source].orEmpty()
            GlassRadioGroup(
                label = stringResource(item.label),
                displayName = { userTypeLabel(it) },
                options = loaded.map { it.name }.ifEmpty { item.fallback },
                selected = uiState.userType,
                onSelect = {
                    // Options that failed to load are fetched again, as Continue needs their ids
                    if (loaded.isEmpty()) onIntent(ProfileIntent.RetryOptions(item.source))
                    onIntent(ProfileIntent.UserTypeChanged(it))
                }
            )
        }

        is ProfileFormItem.Documents -> {
            val (required, optional) = item.documents.partition { it.required }
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                listOf(required, optional).filter { it.isNotEmpty() }.forEach { group ->
                    Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                        group.forEach { document ->
                            val upload = uiState.documents[document]
                            DocumentUploadTile(
                                title = stringResource(document.titleRes),
                                subtitle = stringResource(document.subtitleRes),
                                isUploaded = upload?.path != null,
                                isUploading = upload?.isUploading == true,
                                onClick = { onDocumentClick(document) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        is ProfileFormItem.Input -> when (item.kind) {
            FieldKind.SELECT -> {
                val options = uiState.options[item.source].orEmpty()
                GlassSelectField(
                    label = stringResource(item.label),
                    value = uiState.value(item.field),
                    placeholder = stringResource(item.placeholder),
                    options = options,
                    onSelected = { onIntent(ProfileIntent.OptionSelected(item.field, it)) },
                    leftIcon = item.icon,
                    isRequired = item.required,
                    // The options may have failed to load earlier: try again when the user opens it
                    onOpen = { if (options.isEmpty()) onIntent(ProfileIntent.RetryOptions(item.source)) }
                )
            }

            FieldKind.DATE -> GlassDateField(
                label = stringResource(item.label),
                value = uiState.value(item.field),
                placeholder = stringResource(item.placeholder),
                onDateSelected = { onIntent(ProfileIntent.FieldChanged(item.field, it)) },
                isRequired = item.required,
                allowPastDates = item.allowPastDates,
                allowFutureDates = item.allowFutureDates
            )

            FieldKind.TEXT, FieldKind.NUMBER, FieldKind.MULTILINE, FieldKind.EMAIL -> {
                val isNumber = item.kind == FieldKind.NUMBER
                val isMultiline = item.kind == FieldKind.MULTILINE
                GlassInputField(
                    label = stringResource(item.label),
                    value = uiState.value(item.field),
                    onValueChange = { input ->
                        val cleaned = if (isNumber) input.filter { it.isDigit() } else input
                        onIntent(ProfileIntent.FieldChanged(item.field, cleaned.take(maxLengthOf(item.field))))
                    },
                    placeholder = stringResource(item.placeholder),
                    leftIcon = item.icon,
                    isRequired = item.required,
                    singleLine = !isMultiline,
                    minHeight = if (isMultiline) dimen_80 else dimen_52,
                    keyboardType = when {
                        isNumber -> KeyboardType.Number
                        item.kind == FieldKind.EMAIL -> KeyboardType.Email
                        else -> KeyboardType.Text
                    },
                    capitalization = when (item.field) {
                        ProfileField.GST_NUMBER, ProfileField.PAN,
                        ProfileField.LICENSE_NUMBER, ProfileField.OWNER_ID -> KeyboardCapitalization.Characters

                        ProfileField.EMAIL -> KeyboardCapitalization.None

                        else -> KeyboardCapitalization.Sentences
                    },
                    // Verification isn't connected to an API yet: the button is visible but disabled
                    rightContent = if (item.verifiable) {
                        { ActionPill(text = stringResource(R.string.common_verify), onClick = {}, enabled = false) }
                    } else null
                )
            }
        }
    }
}

/** Known user types are shown translated, any other option as the server names it. */
@Composable
private fun userTypeLabel(name: String): String = when (name) {
    UserTypes.INDIVIDUAL_USER -> stringResource(R.string.user_type_individual_user)
    UserTypes.BUSINESS_USER -> stringResource(R.string.user_type_business_user)
    UserTypes.INDIVIDUAL_OPERATOR -> stringResource(R.string.user_type_individual_operator)
    UserTypes.OWNER_BASED -> stringResource(R.string.user_type_owner_based)
    else -> name
}

private fun maxLengthOf(field: ProfileField): Int = when (field) {
    ProfileField.AADHAR -> 12
    ProfileField.PAN -> 10
    ProfileField.GST_NUMBER -> 15
    ProfileField.PINCODE -> 6
    else -> Int.MAX_VALUE
}

@Preview(name = "Owner - Dark", showBackground = true, heightDp = 800)
@Composable
private fun ProfileOwnerDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ProfileContent(
            uiState = ProfileUiState(role = UserRole.CRANE_OWNER, mobileNumber = "+91 98765 43210"),
            onIntent = {}
        )
    }
}

@Preview(name = "Customer (Business) - Light", showBackground = true, heightDp = 1000)
@Composable
private fun ProfileCustomerLightPreview() {
    CranezoTheme(darkTheme = false) {
        ProfileContent(
            uiState = ProfileUiState(
                role = UserRole.CUSTOMER,
                userType = UserTypes.BUSINESS_USER,
                mobileNumber = "+91 98765 43210"
            ),
            onIntent = {}
        )
    }
}

@Preview(name = "Customer (Individual) - Dark", showBackground = true, heightDp = 800)
@Composable
private fun ProfileCustomerIndividualPreview() {
    CranezoTheme(darkTheme = true) {
        ProfileContent(
            uiState = ProfileUiState(
                role = UserRole.CUSTOMER,
                userType = UserTypes.INDIVIDUAL_USER,
                mobileNumber = "+91 98765 43210"
            ),
            onIntent = {}
        )
    }
}

@Preview(name = "Operator (Owner Based) - Dark", showBackground = true, heightDp = 1100)
@Composable
private fun ProfileOperatorDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ProfileContent(
            uiState = ProfileUiState(
                role = UserRole.OPERATOR,
                userType = UserTypes.OWNER_BASED,
                mobileNumber = "+91 98765 43210"
            ),
            onIntent = {}
        )
    }
}
