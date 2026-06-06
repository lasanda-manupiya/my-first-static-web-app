package com.routewake.app

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

/**
 * Application entry point. Configures OSMDroid (OpenStreetMap) once at startup.
 */
class RouteWakeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // OSMDroid requires a user agent and a place to cache tiles.
        val prefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().apply {
            load(this@RouteWakeApp, prefs)
            userAgentValue = packageName
        }
    }
}
