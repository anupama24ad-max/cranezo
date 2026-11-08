package com.cranezo.ui.booking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.cranezo.BuildConfig
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*
import com.cranezo.utils.fetchCurrentLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Andhra Pradesh, where Cranezo starts: the map opens here when nothing is chosen and there is no GPS */
private val DEFAULT_CENTER = LatLng(16.5, 80.6)

/**
 * A small map in the location step showing the chosen spot. It does not move (so the page can still
 * scroll); tapping it opens the full-screen picker. Without a Maps key nothing is shown.
 */
@Composable
fun LocationMapPreview(latitude: Double?, longitude: Double?, onOpen: () -> Unit) {
    if (!BuildConfig.HAS_MAPS_KEY) return
    val spot = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(spot ?: DEFAULT_CENTER, if (spot != null) 15f else 6f)
    }
    LaunchedEffect(spot) {
        if (spot != null) runCatching { camera.animate(CameraUpdateFactory.newLatLngZoom(spot, 15f), 600) }
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(dimen_16))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false, scrollGesturesEnabled = false, zoomGesturesEnabled = false,
                rotationGesturesEnabled = false, tiltGesturesEnabled = false, mapToolbarEnabled = false,
                myLocationButtonEnabled = false, compassEnabled = false
            )
        ) {
            if (spot != null) Marker(state = MarkerState(position = spot))
        }
        // a tap anywhere opens the picker
        Box(modifier = Modifier.fillMaxSize().clickable(onClick = onOpen))
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(dimen_10)
                .shadow(dimen_4, CircleShape).clip(CircleShape).background(appColors.screenBottom)
                .padding(horizontal = dimen_14, vertical = dimen_8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_16))
            Spacer(modifier = Modifier.width(dimen_6))
            Textview(
                stringResource(if (spot != null) R.string.bk_adjust_on_map else R.string.bk_pick_on_map),
                size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Full-screen map picker (like Rapido): the pin stays in the middle, the customer moves the map under it,
 * the address under the pin is looked up when the map stops, and Confirm saves that spot.
 */
@Composable
fun LocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    resolveAddress: suspend (Double, Double) -> String?,
    onConfirm: (Double, Double, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val start = if (initialLatitude != null && initialLongitude != null) LatLng(initialLatitude, initialLongitude) else DEFAULT_CENTER
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(start, if (initialLatitude != null) 16f else 6f)
    }
    var address by remember { mutableStateOf<String?>(null) }
    var finding by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }

    fun goToMe() {
        scope.launch {
            locating = true
            fetchCurrentLocation(context)?.let { fix ->
                runCatching { camera.animate(CameraUpdateFactory.newLatLngZoom(LatLng(fix.latitude, fix.longitude), 16.5f), 700) }
            }
            locating = false
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) goToMe() }
    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    // nothing chosen yet: start at the phone's position when it is allowed
    LaunchedEffect(Unit) { if (initialLatitude == null && hasPermission()) goToMe() }

    // when the map stops moving, find the address under the pin
    LaunchedEffect(camera.isMoving) {
        if (camera.isMoving) return@LaunchedEffect
        val target = camera.position.target
        finding = true
        delay(350)
        address = runCatching { resolveAddress(target.latitude, target.longitude) }.getOrNull()
        finding = false
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(modifier = Modifier.fillMaxSize().background(appColors.screenBottom)) {
            if (BuildConfig.HAS_MAPS_KEY) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camera,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false, compassEnabled = false)
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Textview(stringResource(R.string.bk_map_needs_key), size = font_14, color = appColors.textSecondary)
                }
            }
            // the pin: its tip sits exactly on the middle of the map
            Icon(
                Icons.Default.LocationOn, contentDescription = null, tint = Red,
                modifier = Modifier.align(Alignment.Center).size(44.dp).offset(y = (-22).dp)
            )
            // back
            Box(
                modifier = Modifier.statusBarsPadding().padding(dimen_16).size(dimen_40).shadow(dimen_4, CircleShape)
                    .clip(CircleShape).background(appColors.screenBottom).clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = appColors.textPrimary) }
            // bottom card
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(dimen_16),
                verticalArrangement = Arrangement.spacedBy(dimen_10)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.End).size(dimen_44).shadow(dimen_4, CircleShape)
                        .clip(CircleShape).background(appColors.screenBottom)
                        .clickable { if (hasPermission()) goToMe() else permission.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    contentAlignment = Alignment.Center
                ) {
                    if (locating) CircularProgressIndicator(modifier = Modifier.size(dimen_20), strokeWidth = 2.dp, color = AmberPrimary)
                    else Icon(Icons.Default.MyLocation, contentDescription = null, tint = AmberPrimary)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().shadow(dimen_8, RoundedCornerShape(dimen_20)).clip(RoundedCornerShape(dimen_20))
                        .background(appColors.screenBottom).padding(dimen_16),
                    verticalArrangement = Arrangement.spacedBy(dimen_10)
                ) {
                    Textview(stringResource(R.string.bk_move_map_hint), size = font_11, color = appColors.textSecondary)
                    Textview(
                        text = if (finding) stringResource(R.string.bk_finding_address) else address ?: String.format(java.util.Locale.US, "%.5f, %.5f", camera.position.target.latitude, camera.position.target.longitude),
                        size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 3
                    )
                    CommonGradientButton(
                        text = stringResource(R.string.bk_confirm_location),
                        enabled = !camera.isMoving && !finding && BuildConfig.HAS_MAPS_KEY,
                        onClick = {
                            val t = camera.position.target
                            onConfirm(t.latitude, t.longitude, address)
                        }
                    )
                }
            }
        }
    }
}
