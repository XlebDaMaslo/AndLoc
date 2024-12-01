package com.example.test.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapViewComposable(context: Context, latitude: Double?, longitude: Double?) {

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

                if (mapView.overlays.isEmpty()) { // Add marker only if it doesn't exist
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marker)
                } else { // Update existing marker position
                    val marker = mapView.overlays[0] as Marker
                    marker.position = geoPoint
                }

                mapView.invalidate() // Refresh the map to show changes
            }
        }
    )
}