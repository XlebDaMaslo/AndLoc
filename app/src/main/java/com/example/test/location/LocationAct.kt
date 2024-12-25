@file:Suppress("DEPRECATION")

package com.example.test.location

import android.content.Context
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel

class LocationAct(
    @SuppressLint("StaticFieldLeak") private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    val latitude = MutableStateFlow<Double?>(null)
    val longitude = MutableStateFlow<Double?>(null)

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude.value = location.latitude
                    longitude.value = location.longitude
                }
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                latitude.value = location.latitude
                longitude.value = location.longitude
            }
        }
    }

    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 sec
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}