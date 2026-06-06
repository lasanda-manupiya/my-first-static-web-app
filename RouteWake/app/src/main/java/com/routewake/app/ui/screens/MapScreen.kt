package com.routewake.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routewake.app.model.Place
import com.routewake.app.ui.components.OsmMapView
import com.routewake.app.ui.components.RadiusSelector
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.TextPrimary
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.ui.theme.White
import com.routewake.app.utils.Constants
import com.routewake.app.utils.GeocoderHelper
import com.routewake.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onStartAlarm: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val destination by viewModel.selectedDestination.collectAsStateWithLifecycle()
    val radius by viewModel.selectedRadius.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            destination = destination,
            radiusMeters = radius,
            onMapTap = { geoPoint ->
                // Drop a pin immediately, then resolve a friendly name in the
                // background via the local geocoder.
                val provisional = Place(
                    name = "Dropped pin",
                    latitude = geoPoint.latitude,
                    longitude = geoPoint.longitude
                )
                viewModel.selectDestination(provisional)
                scope.launch {
                    val name = GeocoderHelper.reverse(
                        context, geoPoint.latitude, geoPoint.longitude
                    )
                    viewModel.selectDestination(provisional.copy(name = name))
                }
            }
        )

        // Back button (floating over the map).
        FilledIconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = White,
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Hint when nothing selected yet.
        if (destination == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Text(
                    "Tap the map to drop a pin",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = TextPrimary
                )
            }
        }

        // Bottom destination details card.
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Green)
                    Spacer(Modifier.height(0.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            destination?.name ?: "No destination selected",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        destination?.let {
                            Text(
                                "%.4f, %.4f".format(it.latitude, it.longitude),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Alert radius", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                RadiusSelector(selected = radius, onSelect = viewModel::selectRadius)

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.startAlarm()
                        onStartAlarm()
                    },
                    enabled = destination != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = White
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(
                        "  Start Alarm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
