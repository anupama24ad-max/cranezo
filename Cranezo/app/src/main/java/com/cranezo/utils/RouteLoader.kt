package com.cranezo.utils

import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

/**
 * The driving route between two points, along the roads (drawn as the yellow line on the map).
 * An empty list when it is not available: the map then falls back to a straight line.
 */
suspend fun loadRoutePoints(
    repo: Repository,
    headers: Map<String, String>,
    fromLat: Double, fromLng: Double, toLat: Double, toLng: Double
): List<LatLng> {
    var out = emptyList<LatLng>()
    val body = JSONObject()
        .put("from_latitude", fromLat).put("from_longitude", fromLng)
        .put("to_latitude", toLat).put("to_longitude", toLng)
    repo.placesRouteApi(headers, body).isRequestCallSuspendSuccess(success = { r ->
        out = r?.data?.points.orEmpty().mapNotNull { p ->
            val la = p.latitude ?: return@mapNotNull null
            val lo = p.longitude ?: return@mapNotNull null
            LatLng(la, lo)
        }
    })
    return out
}

/** Straight-line distance in km between two points */
fun distanceKm(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(bLat - aLat)
    val dLng = Math.toRadians(bLng - aLng)
    val h = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(aLat)) * Math.cos(Math.toRadians(bLat)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return 2 * r * Math.asin(Math.sqrt(h))
}

/** "350 m" under a kilometre, "2.4 km" above */
fun distanceLabel(km: Double): String =
    if (km < 1.0) "${Math.round(km * 1000)} m" else String.format(java.util.Locale.US, "%.1f km", km)
