package com.routewake.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.routewake.app.model.AlarmSound
import com.routewake.app.model.AppSettings
import com.routewake.app.storage.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SettingsRepository(app)

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setDefaultRadius(meters: Int) = launch { repository.setDefaultRadius(meters) }
    fun setAlarmSound(sound: AlarmSound) = launch { repository.setAlarmSound(sound) }
    fun setVibration(enabled: Boolean) = launch { repository.setVibration(enabled) }
    fun setSpeakName(enabled: Boolean) = launch { repository.setSpeakName(enabled) }
    fun setHighAccuracy(enabled: Boolean) = launch { repository.setHighAccuracy(enabled) }
    fun setKeepScreenOn(enabled: Boolean) = launch { repository.setKeepScreenOn(enabled) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
