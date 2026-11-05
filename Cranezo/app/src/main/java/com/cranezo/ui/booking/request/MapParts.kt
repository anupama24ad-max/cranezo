package com.cranezo.ui.booking.request

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cranezo.BuildConfig
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * The map at the top of the booking screens: the job site, an optional search radius and an optional
 * crane position joined to the site by a straight line. Without a Maps key it shows a plain location card.
 */
@Composable
fun SiteMap(
    latitude: Double?,
    longitude: Double?,
    address: String?,
    modifier: Modifier = Modifier,
    radiusKm: Double? = null,
    craneLatitude: Double? = null,
    craneLongitude: Double? = null
) {
    if (!BuildConfig.HAS_MAPS_KEY || latitude == null || longitude == null) {
        LocationCard(address, modifier)
        return
    }
    val site = LatLng(latitude, longitude)
    val zoom = when {
        radiusKm != null && radiusKm >= 15 -> 10f
        radiusKm != null -> 11.5f
        else -> 14f
    }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(site, zoom) }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = camera,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false)
    ) {
        Marker(state = MarkerState(position = site), title = address)
        if (radiusKm != null) {
            Circle(
                center = site,
                radius = radiusKm * 1000,
                fillColor = AmberPrimary.copy(alpha = 0.10f),
                strokeColor = AmberPrimary.copy(alpha = 0.6f),
                strokeWidth = 3f
            )
        }
        if (craneLatitude != null && craneLongitude != null) {
            val crane = LatLng(craneLatitude, craneLongitude)
            Marker(state = MarkerState(position = crane))
            Polyline(points = listOf(crane, site), color = Color(0xFF6B7280), width = 8f)
        }
    }
}

/** Shown instead of the map when no Maps key is set: a soft card with the pin and the address */
@Composable
private fun LocationCard(address: String?, modifier: Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(AmberPrimary.copy(alpha = 0.22f), AmberPrimary.copy(alpha = 0.04f)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = dimen_32)) {
            Box(
                modifier = Modifier.size(dimen_56).clip(CircleShape).background(AmberPrimary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_32)) }
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(address.orEmpty(), size = font_13, color = appColors.textPrimary, textAlign = TextAlign.Center, maxLines = 3)
        }
    }
}

/** Expanding rings over the map while owners are being asked (like a radar) */
@Composable
fun RadarPulse(modifier: Modifier = Modifier, color: Color = AmberPrimary) {
    val t = rememberInfiniteTransition(label = "radar")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "p")
    Canvas(modifier = modifier) {
        val max = size.minDimension / 2f
        listOf(p, (p + 0.5f) % 1f).forEach { f ->
            drawCircle(color = color.copy(alpha = (1f - f) * 0.45f), radius = max * f, center = Offset(size.width / 2f, size.height / 2f))
        }
        drawCircle(color = color, radius = max * 0.08f, center = Offset(size.width / 2f, size.height / 2f))
    }
}

/**
 * Rapido-style page: map on top, a rounded sheet below with the content and a fixed bottom button area.
 */
@Composable
fun MapSheetPage(
    map: @Composable (Modifier) -> Unit,
    onBack: () -> Unit,
    bottom: (@Composable () -> Unit)? = null,
    overlay: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(appColors.screenBottom)) {
        val mapHeight = maxHeight * 0.40f
        map(Modifier.fillMaxWidth().height(mapHeight + dimen_24))
        if (overlay != null) Box(modifier = Modifier.fillMaxWidth().height(mapHeight), contentAlignment = Alignment.Center) { overlay() }
        // back button floating on the map
        Box(
            modifier = Modifier.statusBarsPadding().padding(dimen_16).size(dimen_40).shadow(dimen_4, CircleShape)
                .clip(CircleShape).background(appColors.screenBottom).clickable { onBack() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = appColors.textPrimary) }
        // the sheet
        Column(
            modifier = Modifier.fillMaxSize().padding(top = mapHeight)
                .shadow(dimen_12, RoundedCornerShape(topStart = dimen_24, topEnd = dimen_24))
                .clip(RoundedCornerShape(topStart = dimen_24, topEnd = dimen_24))
                .background(appColors.screenBottom)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = dimen_8), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.width(dimen_40).height(dimen_4).clip(CircleShape).background(appColors.subtleBorder))
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).imePadding()
                    .padding(horizontal = dimen_16, vertical = dimen_12),
                verticalArrangement = Arrangement.spacedBy(dimen_12),
                content = content
            )
            if (bottom != null) {
                Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = dimen_16, vertical = dimen_12)) { bottom() }
            }
        }
    }
}


/** Rapido "0 of 36 captains didn't accept your ride" style header with a thin progress bar */
@Composable
fun SearchingHeader(title: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
        Textview(title, size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth().height(dimen_8).clip(CircleShape).background(appColors.subtleFill)) {
            Box(
                modifier = Modifier.fillMaxWidth(progress.coerceIn(0.03f, 1f)).height(dimen_8).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(AmberPrimary, Color(0xFFFF7A00))))
            )
        }
    }
}
