package com.routewake.app.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * The single app-wide DataStore instance.
 *
 * All repositories (settings, places, user profile) share this one delegate.
 * A [preferencesDataStore] delegate must be declared exactly once per name, so
 * keeping it here avoids the "multiple DataStores active for the same file"
 * crash and lets every repository read/write the same store.
 */
val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "routewake")
