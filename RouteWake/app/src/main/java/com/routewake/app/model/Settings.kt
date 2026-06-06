package com.routewake.app.model

/**
 * User-configurable settings. Persisted via DataStore (see storage/SettingsRepository).
 */
data class AppSettings(
    val defaultRadiusMeters: Int = 500,
    val alarmSound: AlarmSound = AlarmSound.DEFAULT,
    val vibrationEnabled: Boolean = true,
    val speakDestinationName: Boolean = false,
    val highAccuracyMode: Boolean = true,
    val keepScreenOn: Boolean = false
)

enum class AlarmSound(val displayName: String) {
    DEFAULT("Default alarm"),
    NOTIFICATION("Notification tone"),
    RINGTONE("Ringtone");

    companion object {
        fun fromName(name: String?): AlarmSound =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
