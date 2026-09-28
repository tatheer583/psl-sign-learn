package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SignTtsHelper(context: Context) {
  private var tts: TextToSpeech? = null
  private var isInitialized = false

  init {
    try {
      tts = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
          tts?.language = Locale.US
          tts?.setSpeechRate(0.85f) // Friendly, clear pace for children
          tts?.setPitch(1.15f)     // Warm, encouraging tone
          isInitialized = true
        }
      }
    } catch (_: Exception) {
      // Graceful fallback if TTS service is not available
    }
  }

  fun speak(text: String) {
    if (isInitialized && text.isNotBlank()) {
      try {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sign_tts_${System.currentTimeMillis()}")
      } catch (_: Exception) {}
    }
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
      tts = null
    } catch (_: Exception) {}
  }
}
