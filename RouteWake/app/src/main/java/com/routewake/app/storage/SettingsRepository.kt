package com.routewake.app.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.routewake.app.model.AlarmSound
import com.routewake.app.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Single DataStore instance for the whole app.
private val Context.dataStore by preferencesDataStore(name = "routewake_settings")

/**
 * Persists [AppSettings] using Jetpack DataStore (Preferences).
 * No database — just key/value pairs.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_RADIUS = intPreferencesKey("default_radius")
        val ALARM_SOUND = stringPreferencesKey("alarm_sound")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val SPEAK_NAME = booleanPreferencesKey("speak_destination_name")
        val HIGH_ACCURACY = booleanPreferencesKey("high_accuracy_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            defaultRadiusMeters = prefs[Keys.DEFAULT_RADIUS] ?: 500,
            alarmSound = AlarmSound.fromName(prefs[Keys.ALARM_SOUND]),
            vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
            speakDestinationName = prefs[Keys.SPEAK_NAME] ?: false,
            highAccuracyMode = prefs[Keys.HIGH_ACCURACY] ?: true,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false
        )
    }

    suspend fun setDefaultRadius(meters: Int) =
        context.dataStore.edit { it[Keys.DEFAULT_RADIUS] = meters }

    suspend fun setAlarmSound(sound: AlarmSound) =
        context.dataStore.edit { it[Keys.ALARM_SOUND] = sound.name }

    suspend fun setVibration(enabled: Boolean) =
        context.dataStore.edit { it[Keys.VIBRATION] = enabled }

    suspend fun setSpeakName(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SPEAK_NAME] = enabled }

    suspend fun setHighAccuracy(enabled: Boolean) =
        context.dataStore.edit { it[Keys.HIGH_ACCURACY] = enabled }

    suspend fun setKeepScreenOn(enabled: Boolean) =
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
}
