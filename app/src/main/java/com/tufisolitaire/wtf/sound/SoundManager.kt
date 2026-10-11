package com.tufisolitaire.wtf.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.tufisolitaire.wtf.data.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

class SoundManager(context: Context) {

  private val scope = CoroutineScope(Dispatchers.Default)
  private val random = Random()
  private val sampleRate = 44100
  private val dt = 1.0 / sampleRate

  // Pre-synthesized variations for instantaneous, low-latency playback
  private val cardDrawVariations: List<ShortArray> = (0..2).map { v ->
    synthesizeCardDraw(variationIndex = v)
  }

  private val cardMoveVariations: List<ShortArray> = (0..2).map { v ->
    synthesizeCardDrop(variationIndex = v)
  }

  private val cardFlipSound: ShortArray = synthesizeCardFlip()
  private val invalidMoveSound: ShortArray = synthesizeInvalidMove()
  private val winSound: ShortArray = synthesizeWinArpeggio()

  /**
   * Plays a physical, muted card draw sound from the stock pile.
   */
  fun playCardDraw(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.cardSounds) return
    val variation = cardDrawVariations[random.nextInt(cardDrawVariations.size)]
    scope.launch {
      playPcm(variation, volume = settings.soundVolume)
    }
  }

  /**
   * Plays a light, crisp flick when a card is turned face-up.
   */
  fun playCardFlip(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.cardSounds) return
    scope.launch {
      playPcm(cardFlipSound, volume = settings.soundVolume * 0.75f)
    }
  }

  /**
   * Plays a soft, muted drop sound when placing a card on tableau or foundation.
   */
  fun playCardMove(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.cardSounds) return
    val variation = cardMoveVariations[random.nextInt(cardMoveVariations.size)]
    scope.launch {
      playPcm(variation, volume = settings.soundVolume * 0.85f)
    }
  }

  /**
   * Plays two subtle low tones indicating an invalid move attempt.
   */
  fun playInvalidMove(settings: AppSettings) {
    if (!settings.soundEnabled) return
    scope.launch {
      playPcm(invalidMoveSound, volume = settings.soundVolume * 0.7f)
    }
  }

  /**
   * Plays the celebratory victory arpeggio on game completion.
   */
  fun playWinSound(settings: AppSettings) {
    if (!settings.soundEnabled || !settings.winSound) return
    scope.launch {
      playPcm(winSound, volume = settings.soundVolume * 0.85f)
    }
  }

  private fun synthesizeCardDraw(variationIndex: Int): ShortArray {
    val durFactor = 1.0 + (variationIndex - 1) * 0.08
    val freqFactor = 1.0 + (variationIndex - 1) * 0.05
    val durMs = 90.0 * durFactor
    val nSamples = (sampleRate * (durMs / 1000.0)).toInt().coerceAtLeast(1)
    val rng = Random(1337L + variationIndex * 99L)

    // 1. Paper rub layer: white noise bandpassed between 800 Hz and 3000 Hz
    val rcHp = 1.0 / (2.0 * PI * 800.0)
    val alphaHp = rcHp / (rcHp + dt)
    val rcLp = 1.0 / (2.0 * PI * 3000.0)
    val alphaLp = dt / (rcLp + dt)

    val rub = DoubleArray(nSamples)
    var hpPrevIn = 0.0
    var hpPrevOut = 0.0
    var lpPrev = 0.0

    for (i in 0 until nSamples) {
      val t = i * dt
      val noise = (rng.nextDouble() * 2.0) - 1.0
      val hpOut = alphaHp * (hpPrevOut + noise - hpPrevIn)
      hpPrevIn = noise
      hpPrevOut = hpOut
      val lpOut = lpPrev + alphaLp * (hpOut - lpPrev)
      lpPrev = lpOut

      // Envelope: 5 ms fast rise, ~60 ms exponential decay
      val env = if (t < 0.005) {
        t / 0.005
      } else {
        exp(-(t - 0.005) / 0.022)
      }
      rub[i] = lpOut * env
    }

    // 2. Soft "tok" tap: sine sweeping from 140 Hz to 90 Hz, starting near end of rub
    val tTapStart = 0.038 * durFactor
    val tap = DoubleArray(nSamples)
    var phase = 0.0
    val fStart = 140.0 * freqFactor
    val fEnd = 90.0 * freqFactor
    val tapDur = 0.030

    for (i in 0 until nSamples) {
      val t = i * dt
      if (t >= tTapStart) {
        val tau = t - tTapStart
        val p = min(1.0, tau / tapDur)
        val curF = fStart + (fEnd - fStart) * p
        phase += 2.0 * PI * curF * dt
        val envTap = exp(-tau / 0.010)
        tap[i] = sin(phase) * envTap
      }
    }

    // 3. Blend: 55% rub, 45% tap, followed by 1-pole low-pass at 4000 Hz
    val rcOutLp = 1.0 / (2.0 * PI * 4000.0)
    val alphaOutLp = dt / (rcOutLp + dt)
    var outLp = 0.0
    val mix = DoubleArray(nSamples)
    var maxAmp = 0.0001

    for (i in 0 until nSamples) {
      val valSample = 0.55 * rub[i] + 0.45 * tap[i]
      outLp += alphaOutLp * (valSample - outLp)
      mix[i] = outLp
      if (abs(outLp) > maxAmp) {
        maxAmp = abs(outLp)
      }
    }

    // 4. Normalize peak to < 0.5 (target 0.45) with 2 ms fades
    val targetPeak = 0.45
    val fadeSamples = (sampleRate * 0.002).toInt().coerceAtLeast(1)
    val result = ShortArray(nSamples)

    for (i in 0 until nSamples) {
      var sampleVal = (mix[i] / maxAmp) * targetPeak
      if (i < fadeSamples) {
        sampleVal *= (i.toDouble() / fadeSamples)
      } else if (i >= nSamples - fadeSamples) {
        sampleVal *= ((nSamples - 1 - i).toDouble() / fadeSamples)
      }
      result[i] = (sampleVal * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }

    return result
  }

  private fun synthesizeCardDrop(variationIndex: Int): ShortArray {
    val durFactor = 1.0 + (variationIndex - 1) * 0.07
    val durMs = 55.0 * durFactor
    val nSamples = (sampleRate * (durMs / 1000.0)).toInt().coerceAtLeast(1)
    val rng = Random(4242L + variationIndex * 77L)

    val rcLp = 1.0 / (2.0 * PI * 2200.0)
    val alphaLp = dt / (rcLp + dt)
    var lpPrev = 0.0

    var phase = 0.0
    val fStart = 115.0
    val fEnd = 70.0
    val result = ShortArray(nSamples)
    var maxAmp = 0.0001
    val raw = DoubleArray(nSamples)

    for (i in 0 until nSamples) {
      val t = i * dt
      val noise = (rng.nextDouble() * 2.0) - 1.0
      lpPrev += alphaLp * (noise - lpPrev)
      val rubEnv = exp(-t / 0.015)

      val p = min(1.0, t / 0.04)
      val curF = fStart + (fEnd - fStart) * p
      phase += 2.0 * PI * curF * dt
      val tapEnv = exp(-t / 0.012)
      val tapVal = sin(phase) * tapEnv

      val combined = (0.35 * lpPrev * rubEnv) + (0.65 * tapVal)
      raw[i] = combined
      if (abs(combined) > maxAmp) {
        maxAmp = abs(combined)
      }
    }

    val fadeSamples = (sampleRate * 0.002).toInt().coerceAtLeast(1)
    for (i in 0 until nSamples) {
      var s = (raw[i] / maxAmp) * 0.38
      if (i < fadeSamples) s *= (i.toDouble() / fadeSamples)
      else if (i >= nSamples - fadeSamples) s *= ((nSamples - 1 - i).toDouble() / fadeSamples)
      result[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return result
  }

  private fun synthesizeCardFlip(): ShortArray {
    val durMs = 38.0
    val nSamples = (sampleRate * (durMs / 1000.0)).toInt().coerceAtLeast(1)
    val result = ShortArray(nSamples)
    var phase = 0.0
    val fadeSamples = (sampleRate * 0.002).toInt().coerceAtLeast(1)

    for (i in 0 until nSamples) {
      val t = i * dt
      val curF = 460.0 - (180.0 * (i.toDouble() / nSamples))
      phase += 2.0 * PI * curF * dt
      var s = sin(phase) * exp(-t / 0.012) * 0.30
      if (i < fadeSamples) s *= (i.toDouble() / fadeSamples)
      else if (i >= nSamples - fadeSamples) s *= ((nSamples - 1 - i).toDouble() / fadeSamples)
      result[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return result
  }

  private fun synthesizeInvalidMove(): ShortArray {
    val durMs = 120.0
    val nSamples = (sampleRate * (durMs / 1000.0)).toInt().coerceAtLeast(1)
    val result = ShortArray(nSamples)
    val p1Samples = (sampleRate * 0.040).toInt()
    val gapSamples = (sampleRate * 0.020).toInt()
    var phase = 0.0

    for (i in 0 until nSamples) {
      val t = i * dt
      var s = 0.0
      if (i < p1Samples) {
        phase += 2.0 * PI * 130.0 * dt
        s = sin(phase) * exp(-(i * dt) / 0.015) * 0.35
      } else if (i >= p1Samples + gapSamples && i < (p1Samples * 2) + gapSamples) {
        val relT = (i - (p1Samples + gapSamples)) * dt
        phase += 2.0 * PI * 115.0 * dt
        s = sin(phase) * exp(-relT / 0.015) * 0.35
      }
      result[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return result
  }

  private fun synthesizeWinArpeggio(): ShortArray {
    val noteFreqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
    val noteDur = 0.11
    val nSamples = (sampleRate * (noteDur * noteFreqs.size + 0.15)).toInt()
    val result = ShortArray(nSamples)

    for ((idx, freq) in noteFreqs.withIndex()) {
      val startIdx = (idx * noteDur * sampleRate).toInt()
      val durSamples = (sampleRate * 0.22).toInt()
      var phase = 0.0
      for (i in 0 until durSamples) {
        val targetIdx = startIdx + i
        if (targetIdx >= nSamples) break
        val t = i * dt
        phase += 2.0 * PI * freq * dt
        val env = exp(-t / 0.07)
        val s = (sin(phase) + 0.2 * sin(phase * 2)) * env * 0.25
        val cur = result[targetIdx] + (s * 32767).toInt()
        result[targetIdx] = cur.coerceIn(-32768, 32767).toShort()
      }
    }
    return result
  }

  private fun playPcm(sample: ShortArray, volume: Float) {
    if (volume <= 0.001f || sample.isEmpty()) return
    try {
      // Scale by volume
      val scaled = ShortArray(sample.size)
      val vol = volume.coerceIn(0f, 1f)
      for (i in sample.indices) {
        scaled[i] = (sample[i] * vol).toInt().coerceIn(-32768, 32767).toShort()
      }

      val bufferSize = scaled.size * 2
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

      audioTrack.write(scaled, 0, scaled.size)
      audioTrack.play()
      val playDurationMs = ((scaled.size.toDouble() / sampleRate) * 1000).toLong() + 25
      Thread.sleep(playDurationMs)
      audioTrack.release()
    } catch (_: Exception) {
      // Fallback gracefully if audio output is unavailable
    }
  }
}
