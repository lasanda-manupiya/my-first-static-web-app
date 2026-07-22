package com.routewake.app.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.routewake.app.model.Gender
import com.routewake.app.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persists the [UserProfile] (name + date of birth + onboarding flag) in the
 * shared DataStore. No database, no cloud — it never leaves the device.
 */
class UserProfileRepository(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val DOB_YEAR = intPreferencesKey("user_dob_year")
        val DOB_MONTH = intPreferencesKey("user_dob_month")
        val DOB_DAY = intPreferencesKey("user_dob_day")
        val GENDER = stringPreferencesKey("user_gender")
        val ONBOARDED = booleanPreferencesKey("user_onboarded")
        // Year the last birthday wish was shown, to avoid duplicate notifications.
        val LAST_WISHED_YEAR = intPreferencesKey("last_wished_year")
    }

    val profile: Flow<UserProfile> = context.appDataStore.data.map { prefs ->
        UserProfile(
            name = prefs[Keys.NAME] ?: "",
            birthYear = prefs[Keys.DOB_YEAR] ?: 0,
            birthMonth = prefs[Keys.DOB_MONTH] ?: 0,
            birthDay = prefs[Keys.DOB_DAY] ?: 0,
            gender = Gender.fromName(prefs[Keys.GENDER]),
            onboarded = prefs[Keys.ONBOARDED] ?: false
        )
    }

    /** One-shot read, handy for receivers running outside a composition. */
    suspend fun current(): UserProfile = profile.first()

    suspend fun saveProfile(name: String, year: Int, month: Int, day: Int, gender: Gender) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.NAME] = name.trim()
            prefs[Keys.DOB_YEAR] = year
            prefs[Keys.DOB_MONTH] = month
            prefs[Keys.DOB_DAY] = day
            prefs[Keys.GENDER] = gender.name
            prefs[Keys.ONBOARDED] = true
        }
    }

    suspend fun lastWishedYear(): Int =
        context.appDataStore.data.first()[Keys.LAST_WISHED_YEAR] ?: 0

    suspend fun setLastWishedYear(year: Int) {
        context.appDataStore.edit { it[Keys.LAST_WISHED_YEAR] = year }
    }
}
