package com.routewake.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routewake.app.ui.components.PlaceCard
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
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenMap: () -> Unit,
    onAlarmSet: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val recentPlaces by viewModel.recentPlaces.collectAsStateWithLifecycle()
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()
    val selectedRadius by viewModel.selectedRadius.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun runSearch() {
        if (query.isBlank()) return
        searching = true
        error = null
        scope.launch {
            val results = GeocoderHelper.search(context, query)
            searching = false
            if (results.isEmpty()) {
                error = "No places found for \"$query\""
            } else {
                viewModel.selectDestination(results.first())
                query = results.first().name
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Route",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Wake",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Green
                )
            }
            Text(
                text = "Wake up, right on time.",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Where do you want to be alerted?") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searching) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(12.dp),
                            strokeWidth = 2.dp,
                            color = Green
                        )
                    } else {
                        IconButton(onClick = { runSearch() }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Green)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green,
                    cursorColor = Green
                )
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }

        item {
            OutlinedButtonRow(onOpenMap = onOpenMap)
        }

        if (selectedDestination != null) {
            item {
                Text("Selected destination", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                PlaceCard(place = selectedDestination!!, onClick = onOpenMap)
            }
        }

        item {
            Text("Recent places", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        if (recentPlaces.isEmpty()) {
            item {
                Text(
                    "Places you set alarms for will show up here.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            items(recentPlaces) { place ->
                PlaceCard(
                    place = place,
                    onClick = { viewModel.selectDestination(place); query = place.name }
                )
            }
        }

        item {
            Text("Alert radius", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            RadiusSelector(
                selected = selectedRadius,
                onSelect = viewModel::selectRadius
            )
            Text(
                "Alarm rings within ${Constants.radiusLabel(selectedRadius)} of your destination.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    viewModel.startAlarm()
                    onAlarmSet()
                },
                enabled = selectedDestination != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = White
                )
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(
                    "  Set Alarm",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OutlinedButtonRow(onOpenMap: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onOpenMap,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Filled.Map, contentDescription = null, tint = Green)
        Text("  Pick on map", color = TextPrimary)
    }
}
