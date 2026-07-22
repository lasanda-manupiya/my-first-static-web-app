package com.routewake.app.birthday

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.routewake.app.storage.UserProfileRepository
import kotlinx.coroutines.runBlocking

/**
 * Re-registers the daily birthday check after a device reboot (inexact repeating
 * alarms are cleared on reboot). Only reschedules if the user has onboarded.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        try {
            val profile = runBlocking {
                UserProfileRepository(context.applicationContext).current()
            }
            if (profile.onboarded && profile.hasBirthday) {
                BirthdayScheduler.schedule(context.applicationContext)
            }
        } finally {
            pending.finish()
        }
    }
}
