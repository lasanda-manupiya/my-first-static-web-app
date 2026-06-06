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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routewake.app.ui.components.PlaceCard
import com.routewake.app.ui.components.PlaceTrailing
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.TextPrimary
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.viewmodel.MainViewModel

@Composable
fun SavedPlacesScreen(
    viewModel: MainViewModel,
    onPlaceSelected: () -> Unit
) {
    val savedPlaces by viewModel.savedPlaces.collectAsStateWithLifecycle()
    val selected by viewModel.selectedDestination.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Saved Places",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            // Save the currently selected destination, if any.
            IconButton(
                onClick = { selected?.let { viewModel.savePlace(it) } },
                enabled = selected != null
            ) {
                Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save current", tint = Green)
            }
        }

        if (savedPlaces.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.BookmarkAdd,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.height(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No saved places yet", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Select a destination, then tap the bookmark to save it here.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(savedPlaces) { place ->
                    PlaceCard(
                        place = place,
                        onClick = {
                            viewModel.selectDestination(place)
                            onPlaceSelected()
                        },
                        trailing = PlaceTrailing.Remove,
                        onTrailingClick = { viewModel.removeSaved(place) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
