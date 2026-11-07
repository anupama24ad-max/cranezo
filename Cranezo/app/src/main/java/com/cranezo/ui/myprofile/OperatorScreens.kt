package com.cranezo.ui.myprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.cranes.openFile
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.myprofile.components.StatusBadge
import com.cranezo.ui.myprofile.model.MyProfileIntent
import com.cranezo.ui.myprofile.model.MyProfileUiState
import com.cranezo.ui.myprofile.model.OperatorBlock
import com.cranezo.ui.myprofile.model.OperatorDocument
import com.cranezo.ui.myprofile.model.PKey
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatAmount
import com.cranezo.utils.formatDisplayDate
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController

/** The request field that replaces a document: the license has a front and a back. */
private fun replaceField(key: String?, back: Boolean): String? = when (key) {
    "operator_license" -> if (back) PKey.LICENSE_BACK else PKey.LICENSE_FRONT
    "voter_id" -> PKey.VOTER_FILE
    "aadhar_card" -> PKey.AADHAR_FILE
    "pan_card" -> PKey.PAN_FILE
    "other_documents" -> PKey.OTHER_FILE
    else -> null
}

/** Subtitle of a document: the license shows its expiry date, in red when it has expired. */
@Composable
fun documentSubtitle(doc: OperatorDocument): Pair<String?, Boolean> {
    if (!doc.valid_till.isNullOrBlank()) {
        return stringResource(R.string.mp_valid_till, formatDisplayDate(doc.valid_till)) to (doc.expired == true)
    }
    return doc.subtitle to false
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun OperatorDocumentsScreen(
    vm: MyProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(
        profile = ProfileData(
            role_id = 2,
            operator = OperatorBlock(
                documents = listOf(
                    OperatorDocument("operator_license", "Operator License", null, "2027-12-12", false, "/x", "/y", true, 1, "Verified"),
                    OperatorDocument("pan_card", "PAN Card", null, null, null, null, null, false, null, "Not Uploaded")
                )
            )
        )
    )
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm?.onIntent(MyProfileIntent.LoadDocuments) }

    // Camera / gallery / files. Remember which button opened the picker.
    val picker = rememberImagePickerController(allowFiles = true)
    var pendingField by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(picker.imageUrl) {
        val uri = picker.imageUrl ?: return@LaunchedEffect
        pendingField?.let { vm?.onIntent(MyProfileIntent.DocumentSelected(it, uri)) }
        pendingField = null
        picker.clear()
    }
    ImageSourceBottomSheet(
        controller = picker,
        title = stringResource(R.string.picker_document_title),
        subtitle = stringResource(R.string.picker_document_subtitle)
    )
    val replace: (String?) -> Unit = { field ->
        if (field != null) {
            pendingField = field
            picker.openChooser()
        }
    }

    MyProfileScaffold(
        title = stringResource(R.string.mp_documents),
        subtitle = stringResource(R.string.mp_documents_subtitle),
        onBack = onBack
    ) {
        state.profile?.operator?.documents.orEmpty().forEach { doc ->
            val (subtitle, expired) = documentSubtitle(doc)
            val busy = state.uploadingDocument != null &&
                    (state.uploadingDocument == replaceField(doc.key, false) || state.uploadingDocument == replaceField(doc.key, true))
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Description, accent = AmberPrimary, size = dimen_36)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(
                            text = doc.label.orEmpty(),
                            size = font_14,
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Textview(
                                text = subtitle,
                                size = font_11,
                                color = if (expired) Red else appColors.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(dimen_8))
                    StatusBadge(doc.status, serverLabel = doc.status_label)
                }
                if (expired) {
                    Spacer(modifier = Modifier.height(dimen_6))
                    Textview(text = stringResource(R.string.mp_license_expired_note), size = font_11, color = Red)
                }
                Spacer(modifier = Modifier.height(dimen_10))
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_8), verticalAlignment = Alignment.CenterVertically) {
                    if (doc.uploaded == true && !doc.file_url.isNullOrBlank()) {
                        CraneSmallButton(
                            text = stringResource(
                                if (doc.key == "operator_license") R.string.mp_view_front else R.string.mp_view
                            ),
                            icon = Icons.Default.Visibility,
                            onClick = { openFile(context, doc.file_url) }
                        )
                    }
                    if (!doc.file_url_back.isNullOrBlank()) {
                        CraneSmallButton(
                            text = stringResource(R.string.mp_view_back),
                            icon = Icons.Default.Visibility,
                            onClick = { openFile(context, doc.file_url_back) }
                        )
                    }
                    if (busy) {
                        Textview(text = stringResource(R.string.mp_uploading), size = font_11, color = appColors.primaryText)
                    } else if (doc.key == "operator_license") {
                        CraneSmallButton(
                            text = stringResource(R.string.mp_replace_front),
                            icon = Icons.Default.Refresh,
                            onClick = { replace(replaceField(doc.key, false)) }
                        )
                        CraneSmallButton(
                            text = stringResource(R.string.mp_replace_back),
                            icon = Icons.Default.Refresh,
                            onClick = { replace(replaceField(doc.key, true)) }
                        )
                    } else {
                        CraneSmallButton(
                            text = stringResource(if (doc.uploaded == true) R.string.mp_replace else R.string.mp_upload),
                            icon = Icons.Default.Refresh,
                            onClick = { replace(replaceField(doc.key, false)) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6)) {
        Textview(text = label, size = font_12, color = appColors.textSecondary, modifier = Modifier.weight(1f))
        Textview(
            text = value,
            size = font_13,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.4f)
        )
    }
}

/** "View Details" of the operator's assigned crane: POST /operator/getAssignedCranes { crane_id }. */
@Preview(showBackground = true, heightDp = 800)
@Composable
fun AssignedCraneScreen(
    craneId: String = "",
    vm: MyProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(
        assignedCrane = Crane(crane_code = "CRN001", model_name = "Liebherr LTM 1050", capacity_name = "50 Ton"),
        craneLoaded = true
    )
    LaunchedEffect(craneId) { if (craneId.isNotEmpty()) vm?.onIntent(MyProfileIntent.LoadAssignedCrane(craneId)) }

    MyProfileScaffold(title = stringResource(R.string.crane_details_title), onBack = onBack) {
        val crane = state.assignedCrane
        if (crane == null) {
            if (state.craneLoaded) {
                Textview(
                    text = stringResource(R.string.crane_msg_generic_error),
                    size = font_14,
                    color = appColors.textSecondary,
                    modifier = Modifier.padding(top = dimen_40)
                )
            }
            return@MyProfileScaffold
        }

        CraneImage(
            path = crane.primary_photo ?: crane.photoUrls.firstOrNull(),
            modifier = Modifier.fillMaxWidth().height(dimen_191),
            shape = RoundedCornerShape(dimen_16)
        )
        GlassCard(contentPadding = dimen_14) {
            Textview(
                text = crane.model_name ?: crane.crane_code.orEmpty(),
                size = font_18,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimen_8))
            InfoLine(stringResource(R.string.mp_crane_code), crane.crane_code)
            InfoLine(stringResource(R.string.mp_crane_type), crane.crane_type_name)
            InfoLine(stringResource(R.string.mp_crane_capacity), crane.capacity_name)
            InfoLine(stringResource(R.string.mp_crane_fuel), crane.fuel_type_name)
            InfoLine(stringResource(R.string.mp_crane_registration), crane.registration_number)
            InfoLine(stringResource(R.string.mp_crane_location), crane.location)
            InfoLine(
                stringResource(R.string.mp_crane_rate),
                crane.hourly_rate?.let { "₹${formatAmount(it)}" }
            )
            InfoLine(stringResource(R.string.mp_crane_min_hours), crane.minimum_hours?.toString())
            InfoLine(stringResource(R.string.mp_crane_availability), crane.availability_label)
        }
        crane.owner?.let { owner ->
            GlassCard(contentPadding = dimen_14) {
                Textview(
                    text = stringResource(R.string.mp_crane_owner),
                    size = font_14,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_8))
                InfoLine(stringResource(R.string.field_full_name), owner.full_name)
                InfoLine(stringResource(R.string.field_company_name), owner.company_name)
                InfoLine(stringResource(R.string.mp_mobile_number), owner.mobile_number)
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}
