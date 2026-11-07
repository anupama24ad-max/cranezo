package com.cranezo.push

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.cranezo.R

/**
 * Rings res/raw/buzzer_tone over and over, like an incoming call, until [stop] is called
 * or [maxMs] has passed. One buzzer at a time: starting again restarts it.
 */
object BuzzerPlayer {

    private const val DEFAULT_MAX_MS = 30_000L

    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stop() }

    fun start(context: Context, maxMs: Long = DEFAULT_MAX_MS) {
        stop()
        player = runCatching {
            MediaPlayer.create(
                context.applicationContext,
                R.raw.buzzer_tone,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
                0
            )
        }.getOrNull()?.apply {
            isLooping = true
            start()
        }
        handler.postDelayed(stopRunnable, maxMs)
    }

    fun stop() {
        handler.removeCallbacks(stopRunnable)
        player?.let { p ->
            runCatching { if (p.isPlaying) p.stop() }
            p.release()
        }
        player = null
    }
}
