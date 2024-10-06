package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.test.ui.theme.TestTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission granted
                }
            }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        enableEdgeToEdge()

        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        fusedLocationClient = fusedLocationClient
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    fusedLocationClient: FusedLocationProviderClient?
) {
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var rsrp by remember { mutableStateOf<Int?>(null) }
    var isSending by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val client = remember { OkHttpClient() }
    val request = Request.Builder().url("https://4lagwc-2-63-201-51.ru.tuna.am ").build()
    var webSocket: WebSocket? = remember {
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Message received: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Message received: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Closing WebSocket: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket failure: ${t.message}")
            }
        })
    }

    fun sendLocationData() {
        latitude?.let { lat ->
            longitude?.let { lon ->
                rsrp?.let { rsrpValue ->
                    val jsonData = """{"rsrp": $rsrpValue, "lat": $lat, "lon": $lon}"""
                    webSocket?.send(jsonData)
                }
            }
        }
    }

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    rsrp = -85 // Example value - replace with your logic
                }
            }
        }
    }

    fun startSendingData() {
        isSending = true
        GlobalScope.launch {
            while (isSending) {
                sendLocationData()
                delay(5000)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Latitude", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.Green, shape = RoundedCornerShape(16.dp))
                        .width(150.dp)
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = latitude?.toString() ?: "0", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Longitude", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.Green, shape = RoundedCornerShape(16.dp))
                        .width(150.dp)
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = longitude?.toString() ?: "0", color = Color.Black)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            getLocation()
            if (!isSending) {
                startSendingData()
            }
        }) {
            Text(text = "Start Sending Coordinates")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TestTheme {
        Greeting(fusedLocationClient = null)
    }
}