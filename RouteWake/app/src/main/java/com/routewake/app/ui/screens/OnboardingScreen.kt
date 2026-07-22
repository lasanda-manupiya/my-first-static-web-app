package com.routewake.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.routewake.app.model.Gender
import com.routewake.app.model.UserProfile
import com.routewake.app.ui.components.ProfileAvatar
import com.routewake.app.ui.theme.Green
import com.routewake.app.ui.theme.GreenLight
import com.routewake.app.ui.theme.TextPrimary
import com.routewake.app.ui.theme.TextSecondary
import com.routewake.app.ui.theme.White
import com.routewake.app.viewmodel.MainViewModel
import java.util.Calendar

/**
 * First-launch onboarding. Collects the user's name, gender and date of birth so
 * the app can greet them personally, show an avatar, and wish them on birthdays.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.UNSPECIFIED) }
    var dobMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dobCalendar = remember(dobMillis) {
        dobMillis?.let { Calendar.getInstance().apply { timeInMillis = it } }
    }
    val previewProfile = UserProfile(
        name = name,
        birthYear = dobCalendar?.get(Calendar.YEAR) ?: 0,
        birthMonth = dobCalendar?.let { it.get(Calendar.MONTH) + 1 } ?: 0,
        birthDay = dobCalendar?.get(Calendar.DAY_OF_MONTH) ?: 0,
        gender = gender
    )
    val canContinue = name.isNotBlank() && dobMillis != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(profile = previewProfile, size = 64.dp)
            Spacer(Modifier.height(0.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("Welcome to", color = TextSecondary, fontSize = 15.sp)
                Row {
                    Text("Route", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Wake", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Green)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Tell us a little about you so we can personalize your experience.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))
        Text("Your name", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Alex") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green,
                cursorColor = Green
            )
        )

        Spacer(Modifier.height(24.dp))
        Text("Gender", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Gender.entries.forEach { option ->
                val selected = gender == option
                FilterChip(
                    selected = selected,
                    onClick = { gender = option },
                    label = { Text(option.displayName) },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        if (selected) Green else TextSecondary.copy(alpha = 0.3f)
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

        Spacer(Modifier.height(24.dp))
        Text("Date of birth", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Green)
            Text(
                text = "  " + (dobCalendar?.let { formatDob(it) } ?: "Select your birthday"),
                color = if (dobCalendar != null) TextPrimary else TextSecondary
            )
        }

        Spacer(Modifier.height(36.dp))
        Button(
            onClick = {
                val cal = dobCalendar ?: return@Button
                viewModel.saveProfile(
                    name = name,
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH) + 1,
                    day = cal.get(Calendar.DAY_OF_MONTH),
                    gender = gender
                )
                onDone()
            },
            enabled = canContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Text("  Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDatePicker) {
        val today = System.currentTimeMillis()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = dobMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= today // no future birthdays
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dobMillis = state.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = Green) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = White)
        ) {
            DatePicker(state = state)
        }
    }
}

private fun formatDob(calendar: Calendar): String {
    val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val d = calendar.get(Calendar.DAY_OF_MONTH)
    val m = months[calendar.get(Calendar.MONTH)]
    val y = calendar.get(Calendar.YEAR)
    return "$d $m $y"
}
