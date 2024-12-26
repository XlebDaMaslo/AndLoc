package com.example.test.map

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import java.io.File
import java.io.FileWriter
import java.io.IOException
import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import com.example.test.network.WebSocketAct
import kotlinx.coroutines.delay
import kotlin.math.pow

data class SignalPoint(val latitude: Double, val longitude: Double, val rsrp: Int)

@Composable
fun MapViewComposable(context: Context, latitude: Double?, longitude: Double?, rsrp: Int?, webSocketAct: WebSocketAct) {
    val mapView = remember { MapView(context) }
    val signalPoints = remember { mutableStateListOf<SignalPoint>() }
    val savedSignalPoints = remember { mutableStateListOf<SignalPoint>() }
    val maxPoints = 128
    var isInitialSetup by remember { mutableStateOf(true) }
    var showToast by remember { mutableStateOf(false) }
    var lastLocation by remember { mutableStateOf<Pair<Double?, Double?>>(null to null) }
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

    LaunchedEffect(latitude, longitude, rsrp) {
        if (latitude != null && longitude != null && rsrp != null) {
            val newPoint = SignalPoint(latitude, longitude, rsrp)
            if (lastLocation != latitude to longitude) {
                signalPoints.add(0, newPoint)
                lastLocation = latitude to longitude
                if (signalPoints.size > maxPoints) {
                    signalPoints.removeLast()
                }
                // Отправляем данные сразу после добавления маркера
                webSocketAct.sendMapData(newPoint)
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = {
                Configuration.getInstance().userAgentValue = context.packageName
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.controller.setZoom(18.0)
                mapView.isTilesScaledToDpi = true
                mapView.controller.setCenter(GeoPoint(latitude ?: 0.0, longitude ?: 0.0))
                mapView.setMultiTouchControls(true)

                mapView.addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        return false
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        event?.let {
                            updateMarkersSize(mapView.zoomLevelDouble, mapView, signalPoints)
                        }
                        return true
                    }
                })

                mapView
            },
            update = {
                if (isInitialSetup && latitude != null && longitude != null) {
                    mapView.controller.setCenter(GeoPoint(latitude, longitude))
                    isInitialSetup = false
                }


                if (latitude != null && longitude != null) {
                    val geoPoint = GeoPoint(latitude, longitude)
                    // Удаляем старый маркер положения
                    mapView.overlays.removeAll { it is Marker && it.title == "Текущее Положение" }
                    val locationMarker = Marker(mapView)
                    locationMarker.position = geoPoint
                    locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    locationMarker.title = "Текущее Положение"
                    //Добавляем маркер в конец
                    mapView.overlays.add(locationMarker)
                }
                updateMarkersSize(mapView.zoomLevelDouble, mapView, signalPoints)
            }
        )
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ){
            Button(onClick = {
                savedSignalPoints.addAll(signalPoints)
                saveMapDataToFile(savedSignalPoints)
                // Убираем очистку списка
                showToast = true

            }) {
                Text(text = "Save map data", fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (showToast) {
            Text("Data saved to file.", color = Color.Green, fontSize = 18.sp)
            LaunchedEffect(Unit) {
                delay(3000)
                showToast = false
            }
        }
    }
}

fun updateMarkersSize(zoomLevel: Double, mapView: MapView, signalPoints: List<SignalPoint>) {
    // Получаем текущий список, фильтруя маркер текущего положения
    val currentOverlays = mapView.overlays.filter { it !is Marker || it.title != "Текущее Положение" }
    // Удаляем старые маркеры силы сигнала
    mapView.overlays.removeAll(currentOverlays)

    // Добавляем новые маркеры силы сигнала в начало
    signalPoints.forEach { point ->
        val marker = Marker(mapView)
        marker.position = GeoPoint(point.latitude, point.longitude)

        val scaleFactor = 2.0.pow(zoomLevel - 20)
        marker.icon = mapRsrpToDrawable(point.rsrp, scaleFactor)

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Signal Strength: ${point.rsrp}"
        mapView.overlays.add(0, marker)
    }

    mapView.invalidate()

}
fun mapRsrpToRadius(rsrp: Int): Dp {
    return (100 - rsrp / 2).dp.coerceAtLeast(10.dp)
}

fun mapRsrpToColor(rsrp: Int): Color {
    val red = (255 * rsrp / 100.0).coerceAtMost(255.0).toInt()
    val green = (255 * (100 - rsrp) / 100.0).coerceAtMost(255.0).toInt()
    return Color(red, green, 0)
}
fun mapRsrpToDrawable(rsrp: Int, scaleFactor: Double): android.graphics.drawable.Drawable {
    val color = mapRsrpToColor(rsrp)
    val baseRadius = mapRsrpToRadius(rsrp).value.toInt()

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

fun saveMapDataToFile(signalPoints: List<SignalPoint>) {
    if (signalPoints.isEmpty()) return

    val fileName = "map_data.txt"
    val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MapData")
    if (!directory.exists()){
        directory.mkdirs()
    }
    val file = File(directory, fileName)

    try {
        val writer = FileWriter(file)
        signalPoints.forEach { point ->
            writer.append("${point.latitude},${point.longitude},${point.rsrp}\n")
        }
        writer.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}