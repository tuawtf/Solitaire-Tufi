package com.tufisolitaire.wtf.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.tufisolitaire.wtf.data.settings.AppSettings

class HapticsManager(private val context: Context) {

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun performMove(haptic: HapticFeedback?, settings: AppSettings) {
    if (!settings.hapticsEnabled) return
    haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun performInvalidMove(haptic: HapticFeedback?, settings: AppSettings) {
    if (!settings.hapticsEnabled || !settings.hapticsInvalidMove) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
      vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
    } else {
      haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }
}
