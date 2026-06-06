package com.routewake.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routewake.app.model.AlarmSound
import com.routewake.app.ui.components.RadiusSelector
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.TextPrimary
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.ui.theme.White
import com.routewake.app.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        SettingsSection(title = "Alarm") {
            Text("Default alert radius", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            RadiusSelector(
                selected = settings.defaultRadiusMeters,
                onSelect = viewModel::setDefaultRadius
            )

            Spacer(Modifier.height(16.dp))
            AlarmSoundRow(
                current = settings.alarmSound,
                onSelect = viewModel::setAlarmSound
            )

            ToggleRow(
                title = "Vibration",
                subtitle = "Vibrate when the alarm rings",
                checked = settings.vibrationEnabled,
                onChange = viewModel::setVibration
            )
            ToggleRow(
                title = "Speak destination name",
                subtitle = "Announce the destination out loud",
                checked = settings.speakDestinationName,
                onChange = viewModel::setSpeakName
            )
        }

        Spacer(Modifier.height(16.dp))
        SettingsSection(title = "Tracking") {
            ToggleRow(
                title = "High accuracy mode",
                subtitle = "Use GPS for the most precise location",
                checked = settings.highAccuracyMode,
                onChange = viewModel::setHighAccuracy
            )
            ToggleRow(
                title = "Keep screen on",
                subtitle = "Prevent the screen from sleeping while tracking",
                checked = settings.keepScreenOn,
                onChange = viewModel::setKeepScreenOn
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "RouteWake works fully offline for alarms — no account, no cloud, " +
                "no database. Map tiles are loaded from OpenStreetMap.",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        title.uppercase(),
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Green,
                checkedBorderColor = Green
            )
        )
    }
}

@Composable
private fun AlarmSoundRow(
    current: AlarmSound,
    onSelect: (AlarmSound) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Alarm sound", color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(current.displayName, color = TextSecondary, fontSize = 13.sp)
        }
        Box {
            Text("Change", color = Green, fontWeight = FontWeight.SemiBold)
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AlarmSound.entries.forEach { sound ->
                    DropdownMenuItem(
                        text = { Text(sound.displayName) },
                        onClick = {
                            onSelect(sound)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
