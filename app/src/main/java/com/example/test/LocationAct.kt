package com.example.test.location

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow

class LocationAct(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    val latitude = MutableStateFlow<Double?>(null)
    val longitude = MutableStateFlow<Double?>(null)

    // Получение последней известной локации (одиночный запрос)
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

    // Обновление геолокации в реальном времени
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // Удаляем вызов super.onLocationResult(locationResult), т.к. он не нужен
            locationResult.lastLocation?.let { location ->
                latitude.value = location.latitude
                longitude.value = location.longitude
            }
        }
    }

    // Запуск обновления координат
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Интервал обновлений 5 секунд
            fastestInterval = 3000 // Самый быстрый интервал (для оптимизации)
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

    // Остановка обновлений координат
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
