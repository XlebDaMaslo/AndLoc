package com.example.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.test.map.MapViewComposable
import androidx.compose.runtime.collectAsState
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private lateinit var locationAct: LocationAct
    private lateinit var webSocketAct: WebSocketAct
    private lateinit var cellInfoAct: CellInfoAct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // Needed to get last known location
        locationAct = LocationAct(this, LocationServices.getFusedLocationProviderClient(this))
        webSocketAct = WebSocketAct(this)
        cellInfoAct = CellInfoAct(this)

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
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                                selected = false,
                                onClick = { navController.navigate("location") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                                selected = false,
                                onClick = { navController.navigate("map") }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "location",
                        Modifier.padding(innerPadding)
                    ) {
                        composable("location") {
                            Interface(
                                locationAct = locationAct,
                                webSocketAct = webSocketAct,
                                lifecycleOwner = this@MainActivity
                            )
                        }
                        composable("map") {
                            MapScreen(locationAct = locationAct, context = this@MainActivity)
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun MapScreen(locationAct: LocationAct, context: Context) {
        val latitude = locationAct.latitude.collectAsState().value
        val longitude = locationAct.longitude.collectAsState().value
        val rsrp = cellInfoAct.getRsrp()

        MapViewComposable(context = context, latitude = latitude, longitude = longitude, rsrp = rsrp)
    }
}

