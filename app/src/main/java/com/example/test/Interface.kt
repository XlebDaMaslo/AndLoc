package com.example.test.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.test.cellinfo.CellInfoAct
import com.example.test.location.LocationAct
import com.example.test.network.WebSocketAct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope

@Composable
fun Interface(
    modifier: Modifier = Modifier,
    locationAct: LocationAct,
    webSocketAct: WebSocketAct,
    cellInfoAct: CellInfoAct,
    lifecycleOwner: LifecycleOwner
) {
    var isSending by remember { mutableStateOf(false) }

    fun startSendingData() {
        isSending = true
        lifecycleOwner.lifecycleScope.launch {
            while (isSending) {
                locationAct.getLocation()
                cellInfoAct.getCellInfo()

                webSocketAct.sendLocationData(
                    latitude = locationAct.latitude.value,
                    longitude = locationAct.longitude.value,
                    rsrp = cellInfoAct.rsrp.value,
                    cellId = cellInfoAct.cellId.value,
                    lac = cellInfoAct.lac.value,
                    cellType = cellInfoAct.cellType.value
                )
                delay(5000)
            }
        }
    }


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationInfo(
            latitude = locationAct.latitude.collectAsState().value,
            longitude = locationAct.longitude.collectAsState().value
        )
        Spacer(modifier = Modifier.height(16.dp))
        CellInfo(
            cellId = cellInfoAct.cellId.collectAsState().value,
            lac = cellInfoAct.lac.collectAsState().value,
            rsrp = cellInfoAct.rsrp.collectAsState().value,
            cellType = cellInfoAct.cellType.collectAsState().value
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            locationAct.getLocation()
            cellInfoAct.getCellInfo()
            if (!isSending) {
                startSendingData()
            }
        }) {
            Text(text = "Start Sending Data")
        }
    }
}

@Composable
fun LocationInfo(latitude: Double?, longitude: Double?) {
    // Row to display latitude and longitude
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Column for displaying latitude
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        // Column for displaying longitude
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
}

@Composable
fun CellInfo(cellId: Int?, lac: Int?, rsrp: Int?, cellType: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Cell Info", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Cell ID: $cellId", color = Color.White)
        Text(text = "LAC: $lac", color = Color.White)
        Text(text = "RSRP: $rsrp", color = Color.White)
        Text(text = "Cell Type: $cellType", color = Color.White)

    }
}