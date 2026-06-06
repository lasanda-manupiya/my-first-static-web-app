package com.routewake.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.GreenLight
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.ui.theme.White
import com.routewake.app.utils.Constants

/**
 * Horizontal chip selector for the alert radius (100m … 5km).
 */
@Composable
fun RadiusSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(Constants.RADIUS_OPTIONS) { meters ->
            val isSelected = meters == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(meters) },
                label = { Text(Constants.radiusLabel(meters)) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Green else TextSecondary.copy(alpha = 0.3f)
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = White,
                    labelColor = TextSecondary,
                    selectedContainerColor = GreenLight,
                    selectedLabelColor = Green
                )
            )
        }
    }
}
