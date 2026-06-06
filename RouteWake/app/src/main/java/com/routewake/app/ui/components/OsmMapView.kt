package com.routewake.app.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.DashPathEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.routewake.app.model.Place
import com.routewake.app.utils.Constants
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

/**
 * Reusable OpenStreetMap (OSMDroid) map.
 *
 * Renders an optional destination marker, a transparent green radius circle,
 * an optional current-location marker, and an optional dotted route line.
 * Supports tap-to-drop-pin via [onMapTap].
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    destination: Place? = null,
    radiusMeters: Int = 0,
    currentLat: Double? = null,
    currentLon: Double? = null,
    showRouteLine: Boolean = false,
    initialZoom: Double = 14.0,
    onMapTap: ((GeoPoint) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            controller.setZoom(initialZoom)
            val start = destination
                ?: currentLat?.let { Place("", it, currentLon ?: Constants.DEFAULT_LON) }
            controller.setCenter(
                GeoPoint(
                    start?.latitude ?: Constants.DEFAULT_LAT,
                    start?.longitude ?: Constants.DEFAULT_LON
                )
            )
        }
    }

    // Tie the osmdroid MapView to the Compose lifecycle.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            map.overlays.clear()

            // Tap handling to drop a pin.
            if (onMapTap != null) {
                val receiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let { onMapTap(it) }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                map.overlays.add(MapEventsOverlay(receiver))
            }

            // Destination + radius circle.
            destination?.let { dest ->
                val destPoint = GeoPoint(dest.latitude, dest.longitude)

                if (radiusMeters > 0) {
                    val circle = Polygon(map).apply {
                        points = Polygon.pointsAsCircle(destPoint, radiusMeters.toDouble())
                        fillPaint.color = AndroidColor.argb(51, 46, 204, 113) // ~20% green
                        outlinePaint.color = AndroidColor.argb(200, 39, 174, 96)
                        outlinePaint.strokeWidth = 4f
                    }
                    map.overlays.add(circle)
                }

                val marker = Marker(map).apply {
                    position = destPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = dest.name
                }
                map.overlays.add(marker)
            }

            // Current location marker + dotted route line.
            if (currentLat != null && currentLon != null) {
                val here = GeoPoint(currentLat, currentLon)
                val meMarker = Marker(map).apply {
                    position = here
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "You"
                }
                map.overlays.add(meMarker)

                if (showRouteLine && destination != null) {
                    val line = Polyline(map).apply {
                        setPoints(
                            listOf(here, GeoPoint(destination.latitude, destination.longitude))
                        )
                        outlinePaint.color = AndroidColor.rgb(39, 174, 96)
                        outlinePaint.strokeWidth = 8f
                        outlinePaint.style = Paint.Style.STROKE
                        outlinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 25f), 0f)
                    }
                    map.overlays.add(line)
                }
            }

            map.invalidate()
        }
    )
}
