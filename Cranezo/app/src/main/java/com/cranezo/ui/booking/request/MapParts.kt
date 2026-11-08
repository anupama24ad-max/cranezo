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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cranezo.R
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.MarkerComposable

/**
 * The map at the top of the booking screens, in the style of a ride app:
 *  - the job site as a green "Pickup point" pin,
 *  - an optional search radius (a green disc inside a green ring),
 *  - nearby cranes as small crane icons,
 *  - the crane that is coming, turned the way it is driving, with a yellow route along the roads.
 * Without a Maps key it shows a plain location card.
 */
@Composable
fun SiteMap(
    latitude: Double?,
    longitude: Double?,
    address: String?,
    modifier: Modifier = Modifier,
    radiusKm: Double? = null,
    craneLatitude: Double? = null,
    craneLongitude: Double? = null,
    craneTitle: String? = null,
    route: List<LatLng> = emptyList(),
    nearbyCranes: List<LatLng> = emptyList()
) {
    if (!BuildConfig.HAS_MAPS_KEY || latitude == null || longitude == null) {
        LocationCard(address, modifier)
        return
    }
    val site = LatLng(latitude, longitude)
    val zoom = when {
        radiusKm != null && radiusKm >= 15 -> 10f
        radiusKm != null -> 11.5f
        nearbyCranes.isNotEmpty() -> 13f
        else -> 14f
    }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(site, zoom) }
    val crane = if (craneLatitude != null && craneLongitude != null) LatLng(craneLatitude, craneLongitude) else null
    var loaded by remember { mutableStateOf(false) }
    // when a crane position is known, the map frames the crane and the site together (and the route)
    // and glides to the new position every time it moves
    LaunchedEffect(crane, loaded) {
        if (crane == null || !loaded) return@LaunchedEffect
        runCatching {
            val bounds = LatLngBounds.builder().include(site).include(crane).build()
            camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, 140), 900)
        }
    }
    val pickupLabel = stringResource(R.string.bk_pickup_point)
    GoogleMap(
        modifier = modifier,
        cameraPositionState = camera,
        onMapLoaded = { loaded = true },
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false)
    ) {
        if (radiusKm != null) {
            // outer ring and a stronger disc in the middle, like the "searching" look of ride apps
            Circle(
                center = site, radius = radiusKm * 1000,
                fillColor = SuccessGreen.copy(alpha = 0.10f), strokeColor = SuccessGreen.copy(alpha = 0.45f), strokeWidth = 3f
            )
            Circle(
                center = site, radius = radiusKm * 1000 * 0.35,
                fillColor = SuccessGreen.copy(alpha = 0.18f), strokeColor = SuccessGreen.copy(alpha = 0.0f), strokeWidth = 0f
            )
        }
        // the road route in yellow, with a dark edge so it stands out on the map
        if (route.size > 1) {
            Polyline(points = route, color = Color(0xFF1F2937).copy(alpha = 0.55f), width = 22f, jointType = JointType.ROUND, startCap = RoundCap(), endCap = RoundCap(), zIndex = 1f)
            Polyline(points = route, color = Color(0xFFFFC107), width = 14f, jointType = JointType.ROUND, startCap = RoundCap(), endCap = RoundCap(), zIndex = 2f)
        } else if (crane != null) {
            Polyline(points = listOf(crane, site), color = Color(0xFF6B7280), width = 8f)
        }
        nearbyCranes.forEachIndexed { i, spot ->
            CraneMarker(position = spot, bearing = (i * 53f) % 360f, title = null)
        }
        MarkerComposable(state = MarkerState(position = site), title = address, zIndex = 3f) { PickupPin(pickupLabel) }
        if (crane != null) {
            val next = route.firstOrNull { distanceBetween(crane, it) > 25 } ?: site
            CraneMarker(position = crane, bearing = bearingTo(crane, next), title = craneTitle, zIndex = 4f)
        }
    }
}

/** A crane icon on the map; the picture is drawn facing up, so [bearing] (0 = north) turns it the way it drives */
@Composable
private fun CraneMarker(position: LatLng, bearing: Float, title: String?, zIndex: Float = 2f) {
    MarkerComposable(
        state = MarkerState(position = position), title = title, rotation = bearing, flat = true,
        anchor = Offset(0.5f, 0.5f), zIndex = zIndex
    ) {
        Image(painter = painterResource(R.drawable.ic_crane_top), contentDescription = null, modifier = Modifier.size(width = 24.dp, height = 62.dp))
    }
}

/** The green "Pickup point" bubble over a green pin (the tip of the pin is the exact spot) */
@Composable
fun PickupPin(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(SuccessGreen).padding(horizontal = 12.dp, vertical = 6.dp)) {
            androidx.compose.material3.Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(34.dp))
    }
}

private fun distanceBetween(a: LatLng, b: LatLng): Double =
    com.cranezo.utils.distanceKm(a.latitude, a.longitude, b.latitude, b.longitude) * 1000

/** Compass direction from one point to another, in degrees clockwise from north */
private fun bearingTo(from: LatLng, to: LatLng): Float {
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val y = Math.sin(dLon) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
    return ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toFloat()
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
