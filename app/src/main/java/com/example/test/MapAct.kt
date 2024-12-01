package com.example.test.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapViewComposable(context: Context, latitude: Double?, longitude: Double?, rsrp: Int?) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            val mapView = MapView(context)
            Configuration.getInstance().userAgentValue = context.packageName
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.controller.setZoom(18.0)
            mapView.isTilesScaledToDpi = true

            if (latitude != null && longitude != null) {
                val geoPoint = GeoPoint(latitude, longitude)
                mapView.controller.setCenter(geoPoint)

                val marker = Marker(mapView)
                marker.position = geoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
            }
            mapView
        },
        update = { mapView ->
            if (latitude != null && longitude != null) {
                val geoPoint = GeoPoint(latitude, longitude)
                mapView.controller.setCenter(geoPoint)
                if (mapView.overlays.size > 0) {
                    val marker = mapView.overlays[0] as Marker
                    marker.position = geoPoint
                }
            }
            mapView.invalidate()
        }
    )

    if (latitude != null && longitude != null && rsrp != null) {
        val radius = mapRsrpToRadius(rsrp)
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier
                .size(radius)
                .clip(CircleShape)
                .background(mapRsrpToColor(rsrp)))
        }
    }


}

fun mapRsrpToRadius(rsrp: Int): Dp {
    //  Масштабирование радиуса круга в зависимости от RSRP.  Подберите значения по своему усмотрению.
    return (100 - rsrp / 2).dp.coerceAtLeast(10.dp)
}


fun mapRsrpToColor(rsrp: Int): Color {
    // Масштабирование цвета круга в зависимости от RSRP. Подберите значения по своему усмотрению
    val red = (255 * rsrp / 100.0).coerceAtMost(255.0).toInt()
    val green = (255 * (100 - rsrp) / 100.0).coerceAtMost(255.0).toInt()
    return Color(red, green, 0)
}