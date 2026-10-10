package com.tufisolitaire.wtf.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.tufisolitaire.wtf.data.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(context: Context) {

  private val scope = CoroutineScope(Dispatchers.Default)

  fun playCardFlip(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.cardSounds) return
    scope.launch {
      playTone(startFreq = 800f, endFreq = 300f, durationMs = 45, volume = settings.soundVolume * 0.6f)
    }
  }

  fun playCardMove(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.cardSounds) return
    scope.launch {
      playTone(startFreq = 400f, endFreq = 180f, durationMs = 60, volume = settings.soundVolume * 0.8f)
    }
  }

  fun playInvalidMove(settings: AppSettings) {
    if (!settings.soundEnabled) return
    scope.launch {
      playTone(startFreq = 160f, endFreq = 120f, durationMs = 90, volume = settings.soundVolume * 0.7f)
    }
  }

  fun playWinSound(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.winSound) return
    scope.launch {
      val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f) // C5, E5, G5, C6, E6
      val vol = settings.soundVolume * 0.9f
      for (freq in notes) {
        playTone(startFreq = freq, endFreq = freq, durationMs = 100, volume = vol)
      }
    }
  }

  private fun playTone(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
    try {
      val sampleRate = 22050
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
      val sample = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val progress = i.toDouble() / numSamples
        val currentFreq = startFreq + (endFreq - startFreq) * progress
        val angle = 2.0 * Math.PI * currentFreq * t
        // Linear fade out to prevent clicks
        val envelope = 1.0 - progress
        val value = sin(angle) * envelope * volume * 32767
        sample[i] = value.toInt().coerceIn(-32768, 32767).toShort()
      }

      val bufferSize = numSamples * 2
      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(sample, 0, sample.size)
      audioTrack.play()
      Thread.sleep(durationMs.toLong() + 20)
      audioTrack.release()
    } catch (_: Exception) {
      // Graceful fallback if audio is unavailable
    }
  }
}
