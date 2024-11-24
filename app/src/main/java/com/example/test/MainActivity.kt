package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.test.cellinfo.CellInfoAct
import com.example.test.location.LocationAct
import com.example.test.network.WebSocketAct
import com.example.test.ui.Interface
import com.example.test.ui.theme.TestTheme
import com.google.android.gms.location.LocationServices


class MainActivity : ComponentActivity() {
    private lateinit var locationAct: LocationAct
    private lateinit var webSocketAct: WebSocketAct
    private lateinit var cellInfoAct: CellInfoAct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationAct = LocationAct(this, LocationServices.getFusedLocationProviderClient(this))
        webSocketAct = WebSocketAct()
        cellInfoAct = CellInfoAct(this)


        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false


                if (fineLocationGranted || coarseLocationGranted) {
                    locationAct.getLocation()
                }
                if(phoneStateGranted){
                    cellInfoAct.getCellInfo()
                }

            }

        val permissionsToRequest = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )

        if (!permissionsToRequest.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }

        enableEdgeToEdge()

        setContent {
            TestTheme {
                Interface(
                    locationAct = locationAct,
                    webSocketAct = webSocketAct,
                    cellInfoAct = cellInfoAct,
                    lifecycleOwner = this
                )
            }
        }
    }
}