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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routewake.app.ui.components.OsmMapView
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.GreenLight
import com.routewake.app.ui.theme.Red
import com.routewake.app.ui.theme.TextPrimary
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.ui.theme.White
import com.routewake.app.utils.Constants
import com.routewake.app.utils.DistanceUtils
import com.routewake.app.viewmodel.MainViewModel

@Composable
fun TrackingScreen(
    viewModel: MainViewModel,
    onStopped: () -> Unit
) {
    val state by viewModel.alarmState.collectAsStateWithLifecycle()
    val destination = state.destination

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top destination banner.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Green)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Tracking your destination", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            destination?.name ?: "—",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Map filling the middle.
            Box(modifier = Modifier.weight(1f)) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    destination = destination,
                    radiusMeters = state.radiusMeters,
                    currentLat = state.currentLatitude,
                    currentLon = state.currentLongitude,
                    showRouteLine = true
                )
            }

            // Telemetry + stop button.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.LocationOn,
                        label = "Distance",
                        value = state.distanceMeters?.let { DistanceUtils.formatDistance(it) }
                            ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Filled.Schedule,
                        label = "ETA",
                        value = DistanceUtils.formatEta(state.etaSeconds),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.Speed,
                        label = "Speed",
                        value = DistanceUtils.formatSpeed(state.speedMps),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Filled.Bolt,
                        label = "Radius",
                        value = Constants.radiusLabel(state.radiusMeters),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.stopAlarm()
                        onStopped()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red,
                        contentColor = White
                    )
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                    Text("  Stop Alarm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Green)
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
