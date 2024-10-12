package com.example.test.location

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.flow.MutableStateFlow

class LocationAct(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    val latitude = MutableStateFlow<Double?>(null)
    val longitude = MutableStateFlow<Double?>(null)
    val rsrp = MutableStateFlow<Int?>(null)

    // Getting the current geolocation of the device
    fun getLocation() {
        // Checking for Permissions
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude.value = location.latitude
                    longitude.value = location.longitude
                    rsrp.value = 0
                }
            }
        }
    }
}
