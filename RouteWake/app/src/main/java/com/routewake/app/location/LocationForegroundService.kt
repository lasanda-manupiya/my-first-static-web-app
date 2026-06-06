package com.routewake.app.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.routewake.app.MainActivity
import com.routewake.app.R
import com.routewake.app.alarm.AlarmPlayer
import com.routewake.app.model.AlarmSound
import com.routewake.app.model.Place
import com.routewake.app.storage.ActiveAlarmStore
import com.routewake.app.storage.SettingsRepository
import com.routewake.app.utils.Constants
import com.routewake.app.utils.DistanceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground service that listens for GPS updates using the framework
 * [LocationManager] (no Google Play Services), computes the distance to the
 * destination, and rings the alarm when the user enters the alert radius.
 */
class LocationForegroundService : Service(), LocationListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var locationManager: LocationManager
    private lateinit var alarmPlayer: AlarmPlayer
    private lateinit var settingsRepository: SettingsRepository

    private var destination: Place? = null
    private var radiusMeters: Int = 500
    private var highAccuracy: Boolean = true
    private var hasFired = false

    /** Earliest time (ms) the alarm may fire again; used for snooze. */
    private var suppressUntil: Long = 0L

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        alarmPlayer = AlarmPlayer(this)
        settingsRepository = SettingsRepository(applicationContext)
        createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_STOP -> {
                stopEverything()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE -> {
                snooze()
                return START_STICKY
            }
            else -> startTracking(intent)
        }
        return START_STICKY
    }

    private fun startTracking(intent: Intent?) {
        intent ?: return
        val lat = intent.getDoubleExtra(Constants.EXTRA_LAT, Double.NaN)
        val lon = intent.getDoubleExtra(Constants.EXTRA_LON, Double.NaN)
        if (lat.isNaN() || lon.isNaN()) {
            stopSelf()
            return
        }
        val name = intent.getStringExtra(Constants.EXTRA_NAME) ?: "Destination"
        radiusMeters = intent.getIntExtra(Constants.EXTRA_RADIUS, 500)
        highAccuracy = intent.getBooleanExtra(Constants.EXTRA_HIGH_ACCURACY, true)
        destination = Place(name, lat, lon)
        hasFired = false
        suppressUntil = 0L

        ActiveAlarmStore.startTracking(destination!!, radiusMeters)
        startForeground(name, distanceText = null)
        requestUpdates()
    }

    private fun requestUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val provider = if (highAccuracy &&
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        ) {
            LocationManager.GPS_PROVIDER
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else {
            LocationManager.GPS_PROVIDER
        }

        try {
            locationManager.requestLocationUpdates(
                provider,
                Constants.LOCATION_INTERVAL_MS,
                Constants.LOCATION_MIN_DISTANCE_M,
                this
            )
            // Seed with last known location for an immediate reading.
            locationManager.getLastKnownLocation(provider)?.let { onLocationChanged(it) }
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    override fun onLocationChanged(location: Location) {
        val dest = destination ?: return
        val distance = DistanceUtils.distanceBetween(
            location.latitude, location.longitude,
            dest.latitude, dest.longitude
        )
        val speed = if (location.hasSpeed()) location.speed else 0f

        ActiveAlarmStore.updateLocation(
            lat = location.latitude,
            lon = location.longitude,
            distanceMeters = distance,
            speedMps = speed
        )

        updateNotification(dest.name, DistanceUtils.formatDistance(distance))

        val now = System.currentTimeMillis()
        if (distance <= radiusMeters && !hasFired && now >= suppressUntil) {
            fireAlarm(dest)
        }
    }

    private fun fireAlarm(dest: Place) {
        hasFired = true
        ActiveAlarmStore.setRinging(true)
        scope.launch {
            val settings = settingsRepository.settings.first()
            alarmPlayer.start(
                sound = settings.alarmSound,
                vibrate = settings.vibrationEnabled,
                speakName = settings.speakDestinationName,
                destinationName = dest.name
            )
        }
        // Bring the alarm screen to the foreground.
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(MainActivity.EXTRA_SHOW_ALARM, true)
            }
        )
    }

    private fun snooze() {
        alarmPlayer.stop()
        ActiveAlarmStore.setRinging(false)
        hasFired = false
        suppressUntil = System.currentTimeMillis() + Constants.SNOOZE_MILLIS
    }

    private fun stopEverything() {
        alarmPlayer.stop()
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
        ActiveAlarmStore.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    // --- Notification helpers -------------------------------------------------

    private fun startForeground(destinationName: String, distanceText: String?) {
        val notification = buildNotification(destinationName, distanceText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.TRACKING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(Constants.TRACKING_NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(destinationName: String, distanceText: String?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            Constants.TRACKING_NOTIFICATION_ID,
            buildNotification(destinationName, distanceText)
        )
    }

    private fun buildNotification(destinationName: String, distanceText: String?) =
        NotificationCompat.Builder(this, Constants.TRACKING_CHANNEL_ID)
            .setContentTitle(getString(R.string.tracking_notification_title))
            .setContentText(
                if (distanceText != null) "$destinationName · $distanceText away"
                else destinationName
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .addAction(0, "Stop", stopIntent())
            .build()

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(this, 0, intent, pendingFlags())
    }

    private fun stopIntent(): PendingIntent {
        val intent = Intent(this, LocationForegroundService::class.java).apply {
            action = Constants.ACTION_STOP
        }
        return PendingIntent.getService(this, 1, intent, pendingFlags())
    }

    private fun pendingFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tracking = NotificationChannel(
            Constants.TRACKING_CHANNEL_ID,
            getString(R.string.tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.tracking_channel_desc) }

        val alarm = NotificationChannel(
            Constants.ALARM_CHANNEL_ID,
            getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = getString(R.string.alarm_channel_desc) }

        manager.createNotificationChannel(tracking)
        manager.createNotificationChannel(alarm)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        alarmPlayer.stop()
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Older API compatibility no-ops.
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    companion object {
        const val ACTION_SNOOZE = "com.routewake.app.action.SNOOZE"

        fun start(context: Context, place: Place, radiusMeters: Int, highAccuracy: Boolean) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = Constants.ACTION_START
                putExtra(Constants.EXTRA_LAT, place.latitude)
                putExtra(Constants.EXTRA_LON, place.longitude)
                putExtra(Constants.EXTRA_NAME, place.name)
                putExtra(Constants.EXTRA_RADIUS, radiusMeters)
                putExtra(Constants.EXTRA_HIGH_ACCURACY, highAccuracy)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = Constants.ACTION_STOP
            }
            context.startService(intent)
        }

        fun snooze(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_SNOOZE
            }
            context.startService(intent)
        }
    }
}
