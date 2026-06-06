package com.routewake.app.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.routewake.app.model.AlarmSound
import java.util.Locale

/**
 * Plays the alarm sound, vibrates, and optionally speaks the destination name.
 * Uses only local device features (RingtoneManager, Vibrator, TextToSpeech).
 */
class AlarmPlayer(private val context: Context) {

    private var ringtone: Ringtone? = null
    private var tts: TextToSpeech? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun start(
        sound: AlarmSound,
        vibrate: Boolean,
        speakName: Boolean,
        destinationName: String?
    ) {
        playSound(sound)
        if (vibrate) startVibration()
        if (speakName && !destinationName.isNullOrBlank()) speak(destinationName)
    }

    private fun playSound(sound: AlarmSound) {
        val type = when (sound) {
            AlarmSound.DEFAULT -> RingtoneManager.TYPE_ALARM
            AlarmSound.NOTIFICATION -> RingtoneManager.TYPE_NOTIFICATION
            AlarmSound.RINGTONE -> RingtoneManager.TYPE_RINGTONE
        }
        var uri = RingtoneManager.getDefaultUri(type)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (uri == null) return

        ringtone = RingtoneManager.getRingtone(context, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
            }
            play()
        }
    }

    private fun startVibration() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val pattern = longArrayOf(0, 600, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(pattern, 0)
        }
    }

    private fun speak(name: String) {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.speak(
                    "You are arriving at $name",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "routewake_arrival"
                )
            }
        }
    }

    fun stop() {
        ringtone?.let { if (it.isPlaying) it.stop() }
        ringtone = null
        vibrator?.cancel()
        tts?.let {
            it.stop()
            it.shutdown()
        }
        tts = null
    }
}
