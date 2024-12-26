package com.example.test.map

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import androidx.compose.ui.zIndex
import androidx.compose.runtime.*
import kotlin.math.pow


@Composable
fun MapLoadAct(context: Context) {
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    val allSignalPoints = remember { mutableStateListOf<SignalPoint>() }
    var showMap by remember { mutableStateOf(false) }
    var displayedMarkersCount by remember { mutableIntStateOf(0) }
    var currentMarkerIndex by remember { mutableIntStateOf(0) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        fileUri = uri
        if (uri != null) {
            loadMapDataFromFile(context, uri, allSignalPoints)
            displayedMarkersCount = allSignalPoints.size
            currentMarkerIndex = displayedMarkersCount
            showMap = true
        }
    }
    val currentMarkers = remember(currentMarkerIndex,allSignalPoints){
        if (currentMarkerIndex > 0 && currentMarkerIndex <= allSignalPoints.size){
            allSignalPoints.subList(0, currentMarkerIndex).toList()
        } else {
            emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (showMap) {
            LoadedMapViewComposable(context = context, signalPoints = currentMarkers)
        }
        Column(modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .align(Alignment.TopStart)
            .background(Color(0xFFB0BEC5).copy(alpha = 1.0f), shape = RoundedCornerShape(16.dp))
        )
        {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(
                    modifier = Modifier.zIndex(2f),
                    onClick = {
                        currentMarkerIndex = (currentMarkerIndex - 1).coerceAtLeast(0)

                    }) {
                    Text(text = "<", fontSize = 24.sp)
                }

                Text(text = "$currentMarkerIndex / $displayedMarkersCount", Modifier.padding(horizontal = 8.dp))
                Button(
                    modifier = Modifier.zIndex(2f),
                    onClick = {
                        currentMarkerIndex = (currentMarkerIndex + 1).coerceAtMost(displayedMarkersCount)
                    }) {
                    Text(text = ">", fontSize = 24.sp)
                }
            }
            Button(
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 8.dp),
                onClick = { filePickerLauncher.launch(arrayOf("text/*")) }) {
                Text(text = "Load map data", fontSize = 18.sp)
            }
        }
    }
}
fun loadMapDataFromFile(context: Context, uri: Uri, signalPoints: MutableList<SignalPoint>) {
    signalPoints.clear()
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line?.split(",")
                    if (parts?.size == 3) {
                        try {
                            val latitude = parts[0].toDouble()
                            val longitude = parts[1].toDouble()
                            val rsrp = parts[2].toInt()
                            signalPoints.add(SignalPoint(latitude, longitude, rsrp))
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun LoadedMapViewComposable(context: Context, signalPoints: List<SignalPoint>) {
    val mapView = remember { MapView(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mapView.onResume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                mapView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.controller.setZoom(12.0)
            mapView.isTilesScaledToDpi = true
            if (signalPoints.isNotEmpty()) {
                val firstPoint = signalPoints.first()
                mapView.controller.setCenter(GeoPoint(firstPoint.latitude, firstPoint.longitude))
            }
            mapView.setMultiTouchControls(true)
            mapView.addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    event?.let {
                        updateMarkersSizeLoad(mapView.zoomLevelDouble, mapView, signalPoints)
                    }
                    return true
                }
            })
            mapView
        },
        update = {
            updateMarkersSizeLoad(mapView.zoomLevelDouble, mapView, signalPoints)
        }
    )
}

fun updateMarkersSizeLoad(zoomLevel: Double, mapView: MapView, signalPoints: List<SignalPoint>) {
    mapView.overlays.removeAll { it is Marker }

    signalPoints.forEach { point ->
        val marker = Marker(mapView)
        marker.position = GeoPoint(point.latitude, point.longitude)

        val scaleFactor = 2.0.pow(zoomLevel - 20)
        marker.icon = mapRsrpToDrawableLoad(point.rsrp, scaleFactor)

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Signal Strength: ${point.rsrp}"
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}

fun mapRsrpToRadiusLoad(rsrp: Int): Dp {
    return (100 - rsrp / 2).dp.coerceAtLeast(10.dp)
}

fun mapRsrpToColorLoad(rsrp: Int): Color {
    val red = (255 * rsrp / 100.0).coerceAtMost(255.0).toInt()
    val green = (255 * (100 - rsrp) / 100.0).coerceAtMost(255.0).toInt()
    return Color(red, green, 0)
}

fun mapRsrpToDrawableLoad(rsrp: Int, scaleFactor: Double): android.graphics.drawable.Drawable {
    val color = mapRsrpToColorLoad(rsrp)
    val baseRadius = mapRsrpToRadiusLoad(rsrp).value.toInt()
    val radius = (baseRadius * scaleFactor).toInt().coerceAtLeast(10)

    val oval = OvalShape()
    val shapeDrawable = ShapeDrawable(oval).apply {
        paint.color = color.toArgb()
        paint.alpha = 50
        intrinsicWidth = radius * 2
        intrinsicHeight = radius * 2
    }
    return shapeDrawable
}