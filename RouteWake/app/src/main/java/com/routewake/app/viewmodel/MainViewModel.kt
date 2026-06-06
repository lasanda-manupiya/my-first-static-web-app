package com.routewake.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.routewake.app.location.LocationForegroundService
import com.routewake.app.model.AlarmState
import com.routewake.app.model.AppSettings
import com.routewake.app.model.Place
import com.routewake.app.storage.ActiveAlarmStore
import com.routewake.app.storage.PlacesRepository
import com.routewake.app.storage.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Home / Map / Tracking / Alarm / Saved screens.
 * All alarm state lives in [ActiveAlarmStore] (in-memory); persisted places and
 * settings come from DataStore.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val placesRepository = PlacesRepository(app)
    private val settingsRepository = SettingsRepository(app)

    // Live alarm telemetry, surfaced straight from the in-memory store.
    val alarmState: StateFlow<AlarmState> = ActiveAlarmStore.state

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val recentPlaces: StateFlow<List<Place>> = placesRepository.recentPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPlaces: StateFlow<List<Place>> = placesRepository.savedPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection made on Home/Map before tracking begins.
    private val _selectedDestination = MutableStateFlow<Place?>(null)
    val selectedDestination: StateFlow<Place?> = _selectedDestination.asStateFlow()

    private val _selectedRadius = MutableStateFlow(500)
    val selectedRadius: StateFlow<Int> = _selectedRadius.asStateFlow()

    init {
        // Default the radius selector to the user's saved preference.
        viewModelScope.launch {
            settingsRepository.settings.collect { s ->
                if (_selectedDestination.value == null) {
                    _selectedRadius.value = s.defaultRadiusMeters
                }
            }
        }
    }

    fun selectDestination(place: Place) {
        _selectedDestination.value = place
    }

    fun selectRadius(meters: Int) {
        _selectedRadius.value = meters
    }

    fun saveCurrentSelection() {
        val place = _selectedDestination.value ?: return
        viewModelScope.launch { placesRepository.savePlace(place) }
    }

    fun savePlace(place: Place) {
        viewModelScope.launch { placesRepository.savePlace(place) }
    }

    fun removeSaved(place: Place) {
        viewModelScope.launch { placesRepository.removeSaved(place) }
    }

    /** Starts the foreground tracking service for the current selection. */
    fun startAlarm() {
        val place = _selectedDestination.value ?: return
        val radius = _selectedRadius.value
        viewModelScope.launch { placesRepository.addRecent(place) }
        LocationForegroundService.start(
            context = getApplication(),
            place = place,
            radiusMeters = radius,
            highAccuracy = settings.value.highAccuracyMode
        )
    }

    fun stopAlarm() {
        LocationForegroundService.stop(getApplication())
    }

    fun snoozeAlarm() {
        LocationForegroundService.snooze(getApplication())
    }
}
