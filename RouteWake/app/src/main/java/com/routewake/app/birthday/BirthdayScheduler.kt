package com.routewake.app.birthday

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.routewake.app.utils.Constants
import java.util.Calendar

/**
 * Schedules a daily, inexact alarm that wakes [BirthdayReceiver] to check whether
 * today is the user's birthday.
 *
 * We use [AlarmManager.setInexactRepeating] on purpose: it needs no special
 * "exact alarm" permission and is battery friendly — a birthday greeting doesn't
 * need to land at an exact second. Everything is local; no backend involved.
 */
object BirthdayScheduler {

    fun schedule(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Next occurrence of BIRTHDAY_CHECK_HOUR:00 (today if still ahead, else tomorrow).
        val triggerAt = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, Constants.BIRTHDAY_CHECK_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }

    fun cancel(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BirthdayReceiver::class.java).apply {
            action = BirthdayReceiver.ACTION_CHECK_BIRTHDAY
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(
            context,
            Constants.BIRTHDAY_ALARM_REQUEST_CODE,
            intent,
            flags
        )
    }
}
