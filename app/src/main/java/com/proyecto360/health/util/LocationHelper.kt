package com.proyecto360.health.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class CapturedLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String
)

object LocationHelper {
    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun capture(context: Context): CapturedLocation? = withContext(Dispatchers.IO) {
        if (!hasPermission(context)) return@withContext null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cancellation = CancellationTokenSource()
        val location = try {
            client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellation.token
            ).await()
        } catch (_: Exception) {
            null
        } ?: try {
            client.lastLocation.await()
        } catch (_: Exception) {
            null
        } ?: return@withContext null

        val label = resolveLabel(context, location.latitude, location.longitude)
            ?: "%.5f, %.5f".format(Locale.US, location.latitude, location.longitude)

        CapturedLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            label = label
        )
    }

    private fun resolveLabel(context: Context, lat: Double, lon: Double): String? {
        return try {
            @Suppress("DEPRECATION")
            val results = Geocoder(context, Locale.getDefault())
                .getFromLocation(lat, lon, 1)
            val address = results?.firstOrNull() ?: return null
            listOfNotNull(
                address.thoroughfare,
                address.locality ?: address.subAdminArea,
                address.adminArea,
                address.countryName
            ).distinct().joinToString(", ").ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }
}
