package com.cranezo.ui.cranes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneScreenHeader
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.cranes.components.CraneStatusBadge
import com.cranezo.ui.cranes.components.approvalLabel
import com.cranezo.ui.cranes.components.craneSummaryLine
import com.cranezo.ui.cranes.model.CranesIntent
import com.cranezo.ui.cranes.model.CranesUiState
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatDisplayDate
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController
import com.cranezo.utils.isDateExpired

/**
 * Upload Document: lists the crane's documents. View opens the file, Replace picks and uploads a
 * new file right away, and Save Document sends only the replaced ones in one request.
 * The crane then goes back to Under Review.
 */
@Composable
fun CraneDocumentsScreen(
    state: CranesUiState,
    onIntent: (CranesIntent) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val crane = state.detail

    LaunchedEffect(Unit) {
        onIntent(CranesIntent.LoadOptions)
        onIntent(CranesIntent.StartDocEdit)
    }

    val picker = rememberImagePickerController(allowFiles = true)
    var pendingType by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(picker.imageUrl) {
        val uri = picker.imageUrl ?: return@LaunchedEffect
        pendingType?.let { onIntent(CranesIntent.DocEditPicked(it, uri)) }
        pendingType = null
        picker.clear()
    }
    ImageSourceBottomSheet(
        controller = picker,
        title = stringResource(R.string.picker_document_title),
        subtitle = stringResource(R.string.picker_document_subtitle)
    )

    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            CraneScreenHeader(
                title = stringResource(R.string.crane_docs_title),
                subtitle = stringResource(R.string.crane_docs_subtitle),
                onBack = onBack
            )

            if (crane == null) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    com.cranezo.ui.components.CranezoLoader()
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimen_16),
                    verticalArrangement = Arrangement.spacedBy(dimen_12)
                ) {
                    // Which crane this is
                    GlassCard(shape = RoundedCornerShape(dimen_16), contentPadding = dimen_10) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CraneImage(
                                path = crane.primary_photo ?: crane.photoUrls.firstOrNull(),
                                modifier = Modifier.size(dimen_56)
                            )
                            Spacer(modifier = Modifier.width(dimen_10))
                            Column(modifier = Modifier.weight(1f)) {
                                Textview(
                                    text = crane.model_name ?: crane.crane_type_name.orEmpty(),
                                    size = font_14,
                                    color = appColors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Textview(
                                    text = craneSummaryLine(crane) + crane.registration_number?.let { "  |  $it" }.orEmpty(),
                                    size = font_11,
                                    color = appColors.textSecondary,
                                    maxLines = 1
                                )
                                Textview(text = crane.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                            }
                            CraneStatusBadge(crane.approval_status, approvalLabel(crane))
                        }
                    }

                    Textview(
                        text = stringResource(R.string.crane_documents),
                        size = font_15,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    // One row per document type the API knows, plus any the crane already has
                    val types = (state.documentTypes.mapNotNull { t -> t.id?.let { it to t.name.orEmpty() } } +
                            crane.documents.orEmpty().mapNotNull { d -> d.document_type?.let { it to d.document_type_name.orEmpty() } })
                        .distinctBy { it.first }
                    types.forEach { (typeId, typeName) ->
                        val current = crane.documents.orEmpty().firstOrNull { it.document_type == typeId }
                        val edit = state.docEdits[typeId]
                        val upload = edit?.upload
                        // A replaced document shows the new date, a kept one the date on file
                        val expiry = if (edit != null) edit.expiry else current?.expiry_date.orEmpty()
                        val expired = isDateExpired(expiry)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassEffect(
                                    shape = RoundedCornerShape(dimen_14),
                                    borderColor = when {
                                        expired -> Red.copy(alpha = 0.8f)
                                        upload?.path != null -> AmberPrimary
                                        else -> Color.Unspecified
                                    }
                                )
                                .padding(dimen_12)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, tint = AmberPrimary, modifier = Modifier.size(dimen_24))
                                Spacer(modifier = Modifier.width(dimen_10))
                                Column(modifier = Modifier.weight(1f)) {
                                    Textview(
                                        text = typeName.ifBlank { current?.document_type_name.orEmpty() } +
                                            if (state.documentTypes.firstOrNull { it.id == typeId }?.required == false) " · " + stringResource(R.string.crane_doc_optional) else "",
                                        size = font_14,
                                        color = appColors.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Textview(
                                        text = when {
                                            upload?.isUploading == true -> stringResource(R.string.crane_doc_uploading)
                                            upload?.failed == true -> stringResource(R.string.crane_doc_failed)
                                            upload?.path != null -> stringResource(R.string.crane_doc_ready_to_save)
                                            current == null -> stringResource(R.string.crane_doc_not_uploaded)
                                            expiry.isBlank() -> stringResource(R.string.crane_doc_no_expiry)
                                            expired -> stringResource(R.string.crane_doc_expired, formatDisplayDate(expiry))
                                            else -> stringResource(R.string.crane_doc_expires, formatDisplayDate(expiry))
                                        },
                                        size = font_11,
                                        color = when {
                                            expired || upload?.failed == true -> Red
                                            upload?.path != null -> SuccessGreen
                                            else -> appColors.textSecondary
                                        },
                                        maxLines = 1
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(dimen_6), horizontalAlignment = Alignment.End) {
                                    if (current?.file_url != null) {
                                        CraneSmallButton(
                                            text = stringResource(R.string.crane_view),
                                            icon = Icons.Default.Visibility,
                                            onClick = { openFile(context, current.file_url) }
                                        )
                                    }
                                    when {
                                        upload?.isUploading == true -> CircularProgressIndicator(
                                            color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_20)
                                        )

                                        upload?.failed == true -> CraneSmallButton(
                                            text = stringResource(R.string.common_retry),
                                            icon = Icons.Default.Refresh,
                                            onClick = { onIntent(CranesIntent.DocEditRetry(typeId)) }
                                        )

                                        else -> CraneSmallButton(
                                            text = stringResource(if (current == null) R.string.crane_upload else R.string.crane_replace),
                                            icon = Icons.Default.Replay,
                                            onClick = {
                                                pendingType = typeId
                                                picker.openChooser()
                                            }
                                        )
                                    }
                                }
                            }
                            if (upload?.path != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = dimen_8)
                                        .clickable {
                                            showDatePicker(context) { onIntent(CranesIntent.DocEditExpiryChanged(typeId, it)) }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DateRange, null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_14))
                                    Spacer(modifier = Modifier.width(dimen_6))
                                    Textview(
                                        text = if (edit?.expiry.isNullOrEmpty()) stringResource(R.string.crane_doc_set_expiry)
                                        else stringResource(R.string.crane_doc_expires, formatDisplayDate(edit?.expiry)),
                                        size = font_11,
                                        color = AmberPrimary
                                    )
                                }
                            }
                        }
                    }

                    Textview(
                        text = stringResource(R.string.crane_docs_review_note),
                        size = font_11,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(dimen_4))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimen_16)
                        .padding(top = dimen_8, bottom = dimen_12)
                ) {
                    CommonGradientButton(
                        text = stringResource(R.string.crane_save_document),
                        gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                        textColor = Color.Black,
                        rightImage = R.drawable.ic_arrow_right,
                        rightIconTint = Color.Black,
                        enabled = state.docEdits.isNotEmpty() && !state.isUploadingDocs && !state.isSavingDocs,
                        onClick = { onIntent(CranesIntent.SaveDocuments) }
                    )
                }
            }
        }
    }
}
