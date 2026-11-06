package com.cranezo.ui.cranes

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassFieldBox
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.GlassSelectField
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.SelectOption
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.cranes.components.CraneScreenHeader
import com.cranezo.ui.cranes.components.NumberedSection
import com.cranezo.ui.cranes.components.OperatorPickerSheet
import com.cranezo.ui.cranes.model.CraneFormOptionsResponse
import com.cranezo.ui.cranes.model.CranesIntent
import com.cranezo.ui.cranes.model.CranesUiState
import com.cranezo.ui.cranes.model.FormMode
import com.cranezo.ui.cranes.model.FormOption
import com.cranezo.ui.cranes.model.UploadItem
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.fetchCurrentLocation
import com.cranezo.utils.formatDisplayDate
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private const val MAX_PHOTOS = 10

/** Opens the system date picker and reports a yyyy-MM-dd date. */
fun showDatePicker(context: Context, onSelected: (String) -> Unit) {
    val now = Calendar.getInstance()
    DatePickerDialog(
        context, com.cranezo.R.style.Theme_Cranezo_Picker,
        { _, year, month, day ->
            onSelected(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun List<FormOption>?.asSelectOptions(): List<SelectOption> =
    orEmpty().mapNotNull { o -> o.id?.let { SelectOption(it, o.name.orEmpty()) } }

/** Name of the chosen option. The crane's own name is used until the dropdown has loaded. */
private fun nameOf(list: List<FormOption>?, id: Int?, fallback: String?): String =
    list?.firstOrNull { it.id == id }?.name ?: if (id != null) fallback.orEmpty() else ""

/**
 * Add Crane (mode ADD) and Edit Crane (mode EDIT) share this form. When editing, documents are
 * managed on the Upload Document screen instead, so the documents section becomes a link to it.
 */
@Composable
fun CraneFormScreen(
    state: CranesUiState,
    onIntent: (CranesIntent) -> Unit,
    onBack: () -> Unit,
    onManageDocuments: () -> Unit = {},
    // Closes the operator picker once an operator has been added and selected
    operatorAddedSignal: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val form = state.form
    val options = state.options
    val isEdit = state.mode == FormMode.EDIT

    LaunchedEffect(Unit) { onIntent(CranesIntent.LoadOptions) }

    // ---- Photos: camera / gallery
    val photoPicker = rememberImagePickerController()
    LaunchedEffect(photoPicker.imageUrl) {
        val uri = photoPicker.imageUrl ?: return@LaunchedEffect
        onIntent(CranesIntent.PhotoPicked(uri))
        photoPicker.clear()
    }
    ImageSourceBottomSheet(
        controller = photoPicker,
        title = stringResource(R.string.crane_picker_photo_title),
        subtitle = stringResource(R.string.crane_picker_photo_subtitle)
    )

    // ---- Documents: camera / gallery / files. Remembers which tile opened the picker.
    val documentPicker = rememberImagePickerController(allowFiles = true)
    var pendingDocument by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(documentPicker.imageUrl) {
        val uri = documentPicker.imageUrl ?: return@LaunchedEffect
        pendingDocument?.let { onIntent(CranesIntent.DocumentPicked(it, uri)) }
        pendingDocument = null
        documentPicker.clear()
    }
    ImageSourceBottomSheet(
        controller = documentPicker,
        title = stringResource(R.string.picker_document_title),
        subtitle = stringResource(R.string.picker_document_subtitle)
    )

    // ---- GPS button of the location field
    var isLocating by rememberSaveable { mutableStateOf(false) }
    val findLocation: () -> Unit = {
        isLocating = true
        scope.launch {
            val fix = fetchCurrentLocation(context)
            isLocating = false
            if (fix == null) {
                com.cranezo.ui.components.AppMessages.show(context.getString(R.string.crane_msg_location_unavailable))
            } else {
                onIntent(CranesIntent.GpsLocationFound(fix.address, fix.latitude, fix.longitude))
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) findLocation()
        else com.cranezo.ui.components.AppMessages.show(context.getString(R.string.crane_msg_location_denied))
    }

    var showOperatorSheet by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(operatorAddedSignal) { if (operatorAddedSignal > 0) showOperatorSheet = false }

    // Editing a crane that goes back to review is confirmed first (inline, at the Save button)
    var showReverifyDialog by rememberSaveable { mutableStateOf(false) }

    if (showOperatorSheet) {
        OperatorPickerSheet(
            operators = state.operators,
            selectedId = form.operator?.id,
            isLoading = state.operatorsLoading,
            isAdding = state.isAddingOperator,
            onSearch = { onIntent(CranesIntent.SearchOperators(it)) },
            onSelect = {
                onIntent(CranesIntent.OperatorSelected(it))
                showOperatorSheet = false
            },
            onAdd = { name, mobile, email -> onIntent(CranesIntent.AddOperator(name, mobile, email)) },
            onDismiss = { showOperatorSheet = false }
        )
    }

    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            CraneScreenHeader(
                title = stringResource(if (isEdit) R.string.crane_edit_title else R.string.crane_add_title),
                subtitle = stringResource(if (isEdit) R.string.crane_edit_subtitle else R.string.crane_add_subtitle),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimen_16),
                verticalArrangement = Arrangement.spacedBy(dimen_12)
            ) {
                // 1. Crane information
                NumberedSection(1, stringResource(R.string.crane_section_info)) {
                    Column(verticalArrangement = Arrangement.spacedBy(dimen_12)) {
                        // bottom-aligned so both boxes line up even when one label wraps
                        Row(horizontalArrangement = Arrangement.spacedBy(dimen_10), verticalAlignment = Alignment.Bottom) {
                            GlassSelectField(
                                label = stringResource(R.string.crane_field_type),
                                value = nameOf(options?.crane_types, form.craneTypeId, state.original?.crane_type_name),
                                placeholder = stringResource(R.string.crane_field_type_hint),
                                options = options?.crane_types.asSelectOptions(),
                                onSelected = { onIntent(CranesIntent.CraneTypeChanged(it.id)) },
                                isRequired = true,
                                onOpen = { if (options?.crane_types.isNullOrEmpty()) onIntent(CranesIntent.RetryOptionList(6)) },
                                modifier = Modifier.weight(1f)
                            )
                            GlassSelectField(
                                label = stringResource(R.string.crane_field_capacity),
                                value = nameOf(options?.capacities, form.capacityId, state.original?.capacity_name),
                                placeholder = stringResource(R.string.crane_field_capacity_hint),
                                options = options?.capacities.asSelectOptions(),
                                onSelected = { onIntent(CranesIntent.CapacityChanged(it.id)) },
                                isRequired = true,
                                onOpen = { if (options?.capacities.isNullOrEmpty()) onIntent(CranesIntent.RetryOptionList(7)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        GlassSelectField(
                            label = stringResource(R.string.crane_field_fuel),
                            value = nameOf(options?.fuel_types, form.fuelTypeId, state.original?.fuel_type_name),
                            placeholder = stringResource(R.string.crane_field_fuel_hint),
                            options = options?.fuel_types.asSelectOptions(),
                            onSelected = { onIntent(CranesIntent.FuelTypeChanged(it.id)) },
                            onOpen = { if (options?.fuel_types.isNullOrEmpty()) onIntent(CranesIntent.RetryOptionList(8)) }
                        )
                        GlassInputField(
                            label = stringResource(R.string.crane_field_model),
                            value = form.modelName,
                            onValueChange = { onIntent(CranesIntent.ModelChanged(it)) },
                            placeholder = stringResource(R.string.crane_field_model_hint),
                            capitalization = KeyboardCapitalization.Words
                        )
                        GlassInputField(
                            label = stringResource(R.string.crane_field_registration),
                            value = form.registration,
                            onValueChange = { onIntent(CranesIntent.RegistrationChanged(it)) },
                            placeholder = stringResource(R.string.crane_field_registration_hint),
                            isRequired = true,
                            capitalization = KeyboardCapitalization.Characters
                        )
                    }
                }

                // 2. Photos
                NumberedSection(
                    2, stringResource(R.string.crane_section_photos),
                    subtitle = stringResource(R.string.crane_section_photos_subtitle)
                ) {
                    PhotosRow(
                        photos = form.photos,
                        onAdd = photoPicker::openChooser,
                        onRemove = { onIntent(CranesIntent.PhotoRemoved(it)) },
                        onRetry = { onIntent(CranesIntent.PhotoRetry(it)) }
                    )
                }

                // 3. Documents
                NumberedSection(
                    3, stringResource(R.string.crane_section_documents),
                    subtitle = stringResource(if (isEdit) R.string.crane_section_documents_edit else R.string.crane_section_documents_subtitle)
                ) {
                    if (isEdit) {
                        ManageDocumentsRow(onClick = onManageDocuments)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                            state.documentTypes.forEach { type ->
                                val typeId = type.id ?: return@forEach
                                val entry = form.documents[typeId]
                                DocumentRow(
                                    title = type.name.orEmpty(),
                                    upload = entry?.upload,
                                    expiry = entry?.expiry.orEmpty(),
                                    onPick = {
                                        pendingDocument = typeId
                                        documentPicker.openChooser()
                                    },
                                    onRetry = { onIntent(CranesIntent.DocumentRetry(typeId)) },
                                    onRemove = { onIntent(CranesIntent.DocumentRemoved(typeId)) },
                                    onExpiry = { onIntent(CranesIntent.DocumentExpiryChanged(typeId, it)) }
                                )
                            }
                            if (state.documentTypes.isEmpty()) {
                                LoadingHint(state.optionsLoading)
                            }
                        }
                    }
                }

                // 4. Location
                NumberedSection(4, stringResource(R.string.crane_section_location)) {
                    GlassInputField(
                        label = stringResource(R.string.crane_field_location),
                        value = form.location,
                        onValueChange = { onIntent(CranesIntent.LocationChanged(it)) },
                        placeholder = stringResource(R.string.crane_field_location_hint),
                        leftIcon = Icons.Default.LocationOn,
                        isRequired = true,
                        capitalization = KeyboardCapitalization.Words,
                        rightContent = {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    color = AmberPrimary,
                                    strokeWidth = dimen_2,
                                    modifier = Modifier.size(dimen_20)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = stringResource(R.string.crane_use_gps),
                                    tint = if (form.latitude != null) AmberPrimary else appColors.textSecondary,
                                    modifier = Modifier
                                        .size(dimen_24)
                                        .clickable {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                )
                            }
                        }
                    )
                }

                // 5. Rate
                NumberedSection(5, stringResource(R.string.crane_section_rate)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(dimen_10), verticalAlignment = Alignment.Bottom) {
                        GlassInputField(
                            label = stringResource(R.string.crane_field_rate),
                            value = form.hourlyRate,
                            onValueChange = { v ->
                                // Digits and one decimal point
                                val cleaned = v.filter { it.isDigit() || it == '.' }
                                if (cleaned.count { it == '.' } <= 1) onIntent(CranesIntent.HourlyRateChanged(cleaned))
                            },
                            placeholder = stringResource(R.string.crane_field_rate_hint),
                            isRequired = true,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        GlassInputField(
                            label = stringResource(R.string.crane_field_min_hours),
                            value = form.minimumHours,
                            onValueChange = { onIntent(CranesIntent.MinimumHoursChanged(it.filter { c -> c.isDigit() }.take(3))) },
                            placeholder = stringResource(R.string.crane_field_min_hours_hint),
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // No operator here: the owner chooses the operator for each job after a customer books
                // 6. Availability
                NumberedSection(6, stringResource(R.string.crane_section_availability)) {
                    AvailabilityChoice(
                        options = options,
                        selected = form.availability,
                        onSelect = { onIntent(CranesIntent.AvailabilityChanged(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(dimen_4))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_16)
                    .padding(top = dimen_8, bottom = dimen_12)
            ) {
                // Editing a crane that goes back to review asks once more, here on the page (no popup)
                if (showReverifyDialog) {
                    com.cranezo.ui.components.InlineConfirm(
                        message = stringResource(R.string.crane_reverify_message),
                        confirmText = stringResource(R.string.crane_reverify_confirm),
                        danger = false,
                        onConfirm = {
                            showReverifyDialog = false
                            onIntent(CranesIntent.SaveEdit)
                        },
                        onCancel = { showReverifyDialog = false }
                    )
                } else
                // Not clickable while a file is still uploading or the request is running
                CommonGradientButton(
                    text = stringResource(if (isEdit) R.string.crane_save_changes else R.string.crane_submit),
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Color.Black,
                    enabled = !state.isUploading && !state.isSubmitting,
                    onClick = {
                        if (isEdit) {
                            if (state.needsReverification) showReverifyDialog = true
                            else onIntent(CranesIntent.SaveEdit)
                        } else {
                            onIntent(CranesIntent.SubmitCrane)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingHint(loading: Boolean) {
    Box(modifier = Modifier.fillMaxWidth().padding(dimen_8), contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_20))
        } else {
            Textview(
                text = stringResource(R.string.crane_documents_unavailable),
                size = font_12,
                color = appColors.textSecondary
            )
        }
    }
}

// -------------------------------------------------------------------------------------------
// Photos
// -------------------------------------------------------------------------------------------

@Composable
private fun PhotosRow(
    photos: List<UploadItem>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    onRetry: (Long) -> Unit
) {
    if (photos.isEmpty()) {
        // Empty state of the design: a dashed box with a camera icon
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimen_110)
                .clip(RoundedCornerShape(dimen_14))
                .border(dimen_1, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(dimen_14))
                .background(AmberPrimary.copy(alpha = 0.05f))
                .clickable { onAdd() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(dimen_44)
                    .clip(RoundedCornerShape(dimen_12))
                    .background(AmberPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, null, tint = AmberPrimary, modifier = Modifier.size(dimen_24))
            }
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(
                text = stringResource(R.string.crane_upload_photos),
                size = font_12,
                color = AmberPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        return
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(dimen_8)
    ) {
        photos.forEachIndexed { index, photo ->
            PhotoThumb(photo, isCover = index == 0, onRemove = { onRemove(photo.id) }, onRetry = { onRetry(photo.id) })
        }
        if (photos.size < MAX_PHOTOS) {
            Box(
                modifier = Modifier
                    .size(dimen_88)
                    .clip(RoundedCornerShape(dimen_12))
                    .border(dimen_1, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(dimen_12))
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.crane_add_photo), tint = AmberPrimary, modifier = Modifier.size(dimen_28))
            }
        }
    }
}

@Composable
private fun PhotoThumb(
    photo: UploadItem,
    isCover: Boolean,
    onRemove: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(dimen_88)
            .clip(RoundedCornerShape(dimen_12))
            .background(appColors.subtleFill)
    ) {
        ShimmerImage(
            model = photo.displayUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        when {
            photo.isUploading -> Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_24))
            }

            photo.failed -> Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable { onRetry() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, stringResource(R.string.common_retry), tint = Red, modifier = Modifier.size(dimen_26))
            }
        }
        if (isCover) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimen_4)
                    .clip(RoundedCornerShape(dimen_6))
                    .background(AmberPrimary)
                    .padding(horizontal = dimen_6, vertical = dimen_1)
            ) {
                Textview(text = stringResource(R.string.crane_cover), size = font_10, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(dimen_4)
                .size(dimen_20)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, stringResource(R.string.crane_remove), tint = Color.White, modifier = Modifier.size(dimen_14))
        }
    }
}

// -------------------------------------------------------------------------------------------
// Documents
// -------------------------------------------------------------------------------------------

@Composable
private fun DocumentRow(
    title: String,
    upload: UploadItem?,
    expiry: String,
    onPick: () -> Unit,
    onRetry: () -> Unit,
    onRemove: () -> Unit,
    onExpiry: (String) -> Unit
) {
    val context = LocalContext.current
    val uploaded = upload?.path != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(
                shape = RoundedCornerShape(dimen_12),
                borderColor = if (uploaded) AmberPrimary.copy(alpha = 0.8f) else Color.Unspecified,
                inset = true
            )
            .padding(dimen_10)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = upload == null) { onPick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimen_32)
                    .clip(RoundedCornerShape(dimen_8))
                    .background(AmberPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, null, tint = AmberPrimary, modifier = Modifier.size(dimen_18))
            }
            Spacer(modifier = Modifier.width(dimen_10))
            Column(modifier = Modifier.weight(1f)) {
                Textview(text = title, size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Textview(
                    text = stringResource(
                        when {
                            upload?.isUploading == true -> R.string.crane_doc_uploading
                            upload?.failed == true -> R.string.crane_doc_failed
                            uploaded -> R.string.crane_doc_uploaded
                            else -> R.string.crane_doc_upload_file
                        }
                    ),
                    size = font_11,
                    color = when {
                        upload?.failed == true -> Red
                        uploaded -> SuccessGreen
                        else -> appColors.textSecondary
                    }
                )
            }
            when {
                upload?.isUploading == true -> CircularProgressIndicator(
                    color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_20)
                )

                upload?.failed == true -> Row {
                    Icon(
                        Icons.Default.Refresh, stringResource(R.string.common_retry), tint = AmberPrimary,
                        modifier = Modifier.size(dimen_22).clickable { onRetry() }
                    )
                    Spacer(modifier = Modifier.width(dimen_8))
                    Icon(
                        Icons.Default.Close, stringResource(R.string.crane_remove), tint = appColors.textSecondary,
                        modifier = Modifier.size(dimen_22).clickable { onRemove() }
                    )
                }

                uploaded -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(dimen_20))
                    Spacer(modifier = Modifier.width(dimen_8))
                    Icon(
                        Icons.Default.Close, stringResource(R.string.crane_remove), tint = appColors.textSecondary,
                        modifier = Modifier.size(dimen_20).clickable { onRemove() }
                    )
                }

                else -> Icon(Icons.Default.Add, null, tint = AmberPrimary, modifier = Modifier.size(dimen_22))
            }
        }

        if (uploaded) {
            // Expiry date is optional
            Row(
                modifier = Modifier
                    .padding(top = dimen_8)
                    .clickable { showDatePicker(context, onExpiry) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_14))
                Spacer(modifier = Modifier.width(dimen_6))
                Textview(
                    text = if (expiry.isEmpty()) stringResource(R.string.crane_doc_set_expiry)
                    else stringResource(R.string.crane_doc_expires, formatDisplayDate(expiry)),
                    size = font_11,
                    color = if (expiry.isEmpty()) AmberPrimary else appColors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun ManageDocumentsRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(RoundedCornerShape(dimen_12), inset = true)
            .clickable { onClick() }
            .padding(dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, null, tint = AmberPrimary, modifier = Modifier.size(dimen_22))
        Spacer(modifier = Modifier.width(dimen_10))
        Textview(
            text = stringResource(R.string.crane_manage_documents),
            size = font_13,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_20))
    }
}

// -------------------------------------------------------------------------------------------
// Availability
// -------------------------------------------------------------------------------------------

@Composable
private fun AvailabilityChoice(
    options: CraneFormOptionsResponse?,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    // The API lists the choices; the two known ones are the fallback while it has not loaded
    val choices = options?.availability_options.orEmpty().mapNotNull { o -> o.id?.let { it to o.name.orEmpty() } }
        .ifEmpty {
            listOf(
                0 to stringResource(R.string.crane_available),
                1 to stringResource(R.string.crane_not_available)
            )
        }

    Column(verticalArrangement = Arrangement.spacedBy(dimen_6)) {
        Textview(
            text = stringResource(R.string.crane_field_availability),
            size = font_14,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
            choices.forEach { (id, name) ->
                val isSelected = id == selected
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .glassEffect(
                            shape = RoundedCornerShape(dimen_12),
                            backgroundColor = if (isSelected) AmberPrimary.copy(alpha = 0.12f) else Color.Unspecified,
                            borderColor = if (isSelected) AmberPrimary else Color.Unspecified
                        )
                        .clickable { onSelect(id) }
                        .padding(horizontal = dimen_12, vertical = dimen_14),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimen_18)
                            .border(dimen_1 * 1.5f, if (isSelected) AmberPrimary else appColors.textHint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(modifier = Modifier.size(dimen_10).clip(CircleShape).background(AmberPrimary))
                        }
                    }
                    Spacer(modifier = Modifier.width(dimen_8))
                    Textview(
                        text = name,
                        size = font_13,
                        color = if (isSelected) appColors.textPrimary else appColors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
