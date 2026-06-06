package com.routewake.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.GreenLight
import com.routewake.app.ui.theme.Red
import com.routewake.app.ui.theme.TextPrimary
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.ui.theme.White
import com.routewake.app.utils.DistanceUtils
import com.routewake.app.viewmodel.MainViewModel

@Composable
fun AlarmScreen(
    viewModel: MainViewModel,
    onDismissed: () -> Unit
) {
    val state by viewModel.alarmState.collectAsStateWithLifecycle()

    // Pulsing animation for the bell.
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ALARM!",
            color = Green,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.destination?.name ?: "You have arrived",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(96.dp)
            )
        }
        Spacer(Modifier.height(32.dp))

        state.distanceMeters?.let {
            Text(
                "${DistanceUtils.formatDistance(it)} to destination",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(48.dp))
        Button(
            onClick = {
                viewModel.stopAlarm()
                onDismissed()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = White)
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Text("  Stop Alarm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { viewModel.snoozeAlarm(); onDismissed() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Snooze, contentDescription = null, tint = Green)
            Text("  Snooze 5 min", fontSize = 16.sp, color = TextPrimary)
        }
    }
}
