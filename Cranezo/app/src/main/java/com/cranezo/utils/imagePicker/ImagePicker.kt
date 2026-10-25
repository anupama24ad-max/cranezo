package com.cranezo.utils.imagePicker


import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cranezo.ui.theme.*
import com.cranezo.utils.AppMethods.Companion.createTempImageUri

@Stable
class ImagePickerController internal constructor(
    imageUrlState: MutableState<String?>,
    private val openChooserState: MutableState<Boolean>,
    private val doOpenGallery: () -> Unit,
    private val doOpenCamera: () -> Unit,
    private val doLaunchCropFor: (Uri) -> Unit
) {
    var imageUrl: String? by imageUrlState
        internal set

    var showChooser: Boolean
        get() = openChooserState.value
        internal set(value) { openChooserState.value = value }

    fun openChooser() { showChooser = true }
    fun clear() { imageUrl = null }

    internal fun pickFromGallery() = doOpenGallery()
    internal fun pickFromCamera() = doOpenCamera()
}


@Composable
fun rememberImagePickerController(): ImagePickerController {
    val ctx = LocalContext.current

    val imageUrlState = rememberSaveable { mutableStateOf<String?>(null) }
    val openChooserState = rememberSaveable { mutableStateOf(false) }

    // ---- Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUrlState.value = it.toString()
        }
    }

    val reqGalleryPerms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(ctx, "Permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    val openGallery: () -> Unit = {
        galleryLauncher.launch("image/*")
    }

    // ---- Camera Picker
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let {
                imageUrlState.value = it.toString()
            }
        }
    }

    val reqCameraPerms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(ctx)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(ctx, "Camera permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    val openCamera: () -> Unit = {
        reqCameraPerms.launch(Manifest.permission.CAMERA)
    }

    return remember {
        ImagePickerController(
            imageUrlState,
            openChooserState,
            openGallery,
            openCamera,
            doLaunchCropFor = {} // not used anymore
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceBottomSheet(controller: ImagePickerController) {
    if (!controller.showChooser) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { controller.showChooser = false },
        sheetState = sheetState,
        dragHandle = {  DragHandle(
            color = MaterialTheme.colorScheme.secondary
        )}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = dimen_16, vertical = dimen_8),
            verticalArrangement = Arrangement.spacedBy(dimen_8),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add image", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(dimen_8))

            // Gallery row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dimen_55)
                    .clickable {
                        controller.showChooser = false
                        controller.pickFromGallery()
                    }
                    .padding(horizontal = dimen_8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Call, contentDescription = null)
                Spacer(Modifier.width(dimen_12))
                Text("Gallery", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(dimen_12))
        }
    }
}

