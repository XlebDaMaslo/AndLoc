package com.example.test.map

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.compose.ui.graphics.toArgb

data class SignalPoint(val latitude: Double, val longitude: Double, val rsrp: Int)

@Composable
fun MapViewComposable(context: Context, latitude: Double?, longitude: Double?, rsrp: Int?) {
    val signalPoints = remember { mutableStateListOf<SignalPoint>() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            val mapView = MapView(context)
            Configuration.getInstance().userAgentValue = context.packageName
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.controller.setZoom(18.0)
            mapView.isTilesScaledToDpi = true
            mapView
        },
        update = { mapView ->
            if (latitude != null && longitude != null) {
                val geoPoint = GeoPoint(latitude, longitude)
                mapView.controller.setCenter(geoPoint)

                // Обновляем позицию маркера текущего местоположения, если он есть
                if (mapView.overlays.size > 0) {
                    val marker = mapView.overlays[0] as Marker
                    marker.position = geoPoint
                } else {
                    // Если маркера нет, создаем его
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marker)
                }
            }
            if (latitude != null && longitude != null && rsrp != null) {
                if (signalPoints.size >= 10) {
                    signalPoints.removeFirst()
                }
                signalPoints.add(SignalPoint(latitude, longitude, rsrp))
            }

            mapView.overlays.removeAll { it !is Marker || (it is Marker && it.title != "Current Location")  }

            signalPoints.forEach { point ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(point.latitude, point.longitude)
                marker.icon = mapRsrpToDrawable(context, point.rsrp)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Signal Strength: ${point.rsrp}"
                mapView.overlays.add(marker)

            }
            mapView.invalidate()
        }
    )
}

fun mapRsrpToRadius(rsrp: Int): Dp {
    return (100 - rsrp / 2).dp.coerceAtLeast(10.dp)
}

fun mapRsrpToColor(rsrp: Int): Color {
    val red = (255 * rsrp / 100.0).coerceAtMost(255.0).toInt()
    val green = (255 * (100 - rsrp) / 100.0).coerceAtMost(255.0).toInt()
    return Color(red, green, 0)
}

fun mapRsrpToDrawable(context: Context, rsrp: Int): android.graphics.drawable.Drawable {
    val color = mapRsrpToColor(rsrp)
    val radius = mapRsrpToRadius(rsrp)

    val oval = OvalShape()
    val shapeDrawable = ShapeDrawable(oval).apply {
        paint.color = color.toArgb() // Set color of the circle
        intrinsicWidth = radius.value.toInt() * 2 // Set size dynamically based on RSRP
        intrinsicHeight = radius.value.toInt() * 2
    }
    return shapeDrawable
}