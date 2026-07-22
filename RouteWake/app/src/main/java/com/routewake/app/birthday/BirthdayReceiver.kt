package com.routewake.app.birthday

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.routewake.app.MainActivity
import com.routewake.app.R
import com.routewake.app.storage.UserProfileRepository
import com.routewake.app.utils.Constants
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * Woken daily by [BirthdayScheduler]. If today matches the user's birthday (and
 * we haven't already wished them this year), it posts a birthday notification.
 */
class BirthdayReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        try {
            val repo = UserProfileRepository(context.applicationContext)
            // DataStore reads are quick here; runBlocking inside goAsync is fine.
            val profile = runBlocking { repo.current() }
            if (!profile.onboarded || !profile.hasBirthday) return

            val now = Calendar.getInstance()
            if (!profile.isBirthday(now)) return

            val year = now.get(Calendar.YEAR)
            val alreadyWished = runBlocking { repo.lastWishedYear() } == year
            if (alreadyWished) return

            showNotification(context, profile.firstName, profile.turningAge(year))
            runBlocking { repo.setLastWishedYear(year) }
        } finally {
            pending.finish()
        }
    }

    private fun showNotification(context: Context, firstName: String, age: Int?) {
        createChannel(context)

        val title = if (firstName.isBlank()) {
            "Happy Birthday! 🎉"
        } else {
            "Happy Birthday, $firstName! 🎉"
        }
        val body = if (age != null) {
            "Wishing you a wonderful ${age}th birthday from RouteWake. Have a safe journey today!"
        } else {
            "Wishing you a wonderful day from RouteWake. Have a safe journey!"
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(context, Constants.BIRTHDAY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        // On Android 13+ posting requires POST_NOTIFICATIONS; skip silently if not granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context)
            .notify(Constants.BIRTHDAY_NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            Constants.BIRTHDAY_CHANNEL_ID,
            "Birthday wishes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "A friendly birthday greeting on your special day." }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_CHECK_BIRTHDAY = "com.routewake.app.action.CHECK_BIRTHDAY"
    }
}
