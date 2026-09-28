package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtil {

  fun playSuccess(context: Context) {
    vibrate(context, longArrayOf(0, 100, 80, 200), intArrayOf(0, 200, 0, 255))
  }

  fun playTap(context: Context) {
    vibrate(context, longArrayOf(0, 40), intArrayOf(0, 180))
  }

  fun playCelebration(context: Context) {
    vibrate(
      context,
      longArrayOf(0, 80, 50, 80, 50, 150, 50, 300),
      intArrayOf(0, 150, 0, 180, 0, 220, 0, 255)
    )
  }

  fun playPromptAlert(context: Context) {
    vibrate(context, longArrayOf(0, 150, 100, 150), intArrayOf(0, 200, 0, 200))
  }

  private fun vibrate(context: Context, timings: LongArray, amplitudes: IntArray) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        if (vibrator?.hasVibrator() == true) {
          val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
          vibrator.vibrate(effect)
        }
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
          } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
          }
        }
      }
    } catch (_: Exception) {
      // Gracefully handle devices where vibrator permission or service is unavailable
    }
  }
}
