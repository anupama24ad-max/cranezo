package com.cranezo.utils.imagePicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*
import com.cranezo.utils.AppMethods.Companion.createTempImageUri

/**
 * Picks an image from the camera or the gallery. Neither source needs a runtime permission:
 * - Camera: the system camera app takes the photo and hands it back (needs the FileProvider).
 * - Gallery: the system photo picker returns only the photo the user chose.
 *
 * Usage:
 * ```
 * val picker = rememberImagePickerController()
 * ImageSourceBottomSheet(picker)               // the Camera / Gallery sheet
 * avatar.clickable { picker.openChooser() }    // open it
 * LaunchedEffect(picker.imageUrl) { picker.imageUrl?.let { ... } }
 * ```
 */
@Stable
class ImagePickerController internal constructor(
    imageUrlState: MutableState<String?>,
    private val openChooserState: MutableState<Boolean>,
    private val doOpenGallery: () -> Unit,
    private val doOpenCamera: () -> Unit,
    private val doOpenFiles: () -> Unit,
    /** Adds a "Files" source (images and PDFs), used for documents. */
    val allowFiles: Boolean
) {
    /** Uri of the last picked image, or null. */
    var imageUrl: String? by imageUrlState
        internal set

    var showChooser: Boolean
        get() = openChooserState.value
        internal set(value) {
            openChooserState.value = value
        }

    fun openChooser() {
        showChooser = true
    }

    fun clear() {
        imageUrl = null
    }

    internal fun pickFromGallery() = doOpenGallery()
    internal fun pickFromCamera() = doOpenCamera()
    internal fun pickFromFiles() = doOpenFiles()
}

@Composable
fun rememberImagePickerController(allowFiles: Boolean = false): ImagePickerController {
    val context = LocalContext.current

    val imageUrlState = rememberSaveable { mutableStateOf<String?>(null) }
    val openChooserState = rememberSaveable { mutableStateOf(false) }

    // ---- Gallery: system photo picker, no permission
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUrlState.value = it.toString() }
    }

    val openGallery: () -> Unit = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // ---- Files: system document picker for images and PDFs, no permission
    val filesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { imageUrlState.value = it.toString() }
    }

    val openFiles: () -> Unit = {
        filesLauncher.launch(arrayOf("image/*", "application/pdf"))
    }

    // ---- Camera: system camera app writes into our FileProvider uri, no permission
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { imageUrlState.value = it.toString() }
        }
    }

    val openCamera: () -> Unit = {
        launchCamera(context) { uri ->
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    return remember {
        ImagePickerController(
            imageUrlState,
            openChooserState,
            openGallery,
            openCamera,
            openFiles,
            allowFiles
        )
    }
}

private fun launchCamera(context: Context, launch: (Uri) -> Unit) {
    try {
        launch(createTempImageUri(context))
    } catch (e: ActivityNotFoundException) {
        com.cranezo.ui.components.AppMessages.show(context.getString(R.string.picker_no_camera))
    }
}

/** Bottom sheet with a Camera and a Gallery option. Shows itself while the controller asks for it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceBottomSheet(
    controller: ImagePickerController,
    title: String = stringResource(R.string.picker_photo_title),
    subtitle: String = stringResource(R.string.picker_photo_subtitle)
) {
    if (!controller.showChooser) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { controller.showChooser = false },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = dimen_20, vertical = dimen_8)
        ) {
            Textview(
                text = title,
                size = font_18,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(
                text = subtitle,
                size = font_12,
                color = appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(dimen_20))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimen_12)
            ) {
                SourceOption(
                    title = stringResource(R.string.picker_camera),
                    icon = Icons.Default.PhotoCamera,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        controller.showChooser = false
                        controller.pickFromCamera()
                    }
                )
                SourceOption(
                    title = stringResource(R.string.picker_gallery),
                    icon = Icons.Default.PhotoLibrary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        controller.showChooser = false
                        controller.pickFromGallery()
                    }
                )
                if (controller.allowFiles) {
                    SourceOption(
                        title = stringResource(R.string.picker_files),
                        icon = Icons.Default.Folder,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            controller.showChooser = false
                            controller.pickFromFiles()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimen_24))
        }
    }
}

@Composable
private fun SourceOption(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimen_16))
            .background(appColors.subtleFill)
            .border(dimen_1, appColors.subtleBorder, RoundedCornerShape(dimen_16))
            .clickable { onClick() }
            .padding(vertical = dimen_20),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .size(dimen_56)
                .clip(CircleShape)
                .background(AmberPrimary.copy(alpha = 0.18f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberPrimary,
                modifier = Modifier.size(dimen_28)
            )
        }
        Spacer(modifier = Modifier.height(dimen_10))
        Textview(
            text = title,
            size = font_14,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
