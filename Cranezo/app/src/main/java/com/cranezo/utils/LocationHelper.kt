package com.cranezo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/** The device position, and the "City, State" text for it when it could be worked out. */
data class GpsFix(val latitude: Double, val longitude: Double, val address: String?)

/**
 * Current device position. The caller must hold the location permission. Returns null when the
 * position is not available (location switched off, no fix).
 */
@SuppressLint("MissingPermission")
suspend fun fetchCurrentLocation(context: Context): GpsFix? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val tokenSource = CancellationTokenSource()
    val location = suspendCancellableCoroutine<android.location.Location?> { cont ->
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
        cont.invokeOnCancellation { tokenSource.cancel() }
    } ?: return null

    val address = withContext(Dispatchers.IO) {
        runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
        }.getOrNull()
    }?.let { a ->
        listOfNotNull(a.locality ?: a.subAdminArea, a.adminArea).filter { it.isNotBlank() }
            .joinToString(", ").ifBlank { null }
    }
    return GpsFix(location.latitude, location.longitude, address)
}


/**
 * The phone's position every [intervalMs] while collected: high accuracy, no address lookup, so a moving crane
 * (or a mock location app) gives a steady stream of points. The caller checks the location permission first.
 */
@SuppressLint("MissingPermission")
fun locationUpdates(context: Context, intervalMs: Long = 2_000L): kotlinx.coroutines.flow.Flow<GpsFix> = kotlinx.coroutines.flow.callbackFlow {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val request = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
        .setMinUpdateIntervalMillis(intervalMs / 2)
        .setMinUpdateDistanceMeters(0f)
        .build()
    val callback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
            result.lastLocation?.let { trySend(GpsFix(it.latitude, it.longitude, null)) }
        }
    }
    try {
        client.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())
    } catch (e: SecurityException) {
        close(e)
    }
    kotlinx.coroutines.channels.awaitClose { client.removeLocationUpdates(callback) }
}
