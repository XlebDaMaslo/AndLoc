package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.test.ui.theme.TestTheme
import com.example.test.location.LocationAct
import com.example.test.network.WebSocketAct
import com.example.test.ui.Interface
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var locationAct: LocationAct
    private lateinit var webSocketAct: WebSocketAct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to get last known location
        locationAct = LocationAct(this, LocationServices.getFusedLocationProviderClient(this))

        webSocketAct = WebSocketAct()

        // Request permissions to access geolocation
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    locationAct.getLocation()
                }
            }

        // Checking permissions to access precise geolocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Application will adapt to full screen mode
        enableEdgeToEdge()

        setContent {
            TestTheme {
                Interface(locationAct = locationAct, webSocketAct = webSocketAct, lifecycleOwner = this)
            }
        }
    }
}
