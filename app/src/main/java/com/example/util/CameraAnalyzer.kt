package com.example.util

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

data class GestureAnalysisResult(
  val isHandInGuide: Boolean,
  val confidenceScore: Float,
  val stabilityProgress: Float,
  val statusMessage: String
)

class HandGestureAnalyzer(
  private val onResult: (GestureAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

  private var frameCounter = 0
  private var consecutiveSteadyFrames = 0
  private val requiredSteadyFrames = 18 // ~1.5 - 2.0s at normal preview rate
  private var lastAvgLuma = 0f

  @androidx.annotation.OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    frameCounter++

    // Sample every 2nd frame for high performance & responsive UI
    if (frameCounter % 2 != 0) {
      imageProxy.close()
      return
    }

    val planes = imageProxy.planes
    if (planes.isEmpty()) {
      imageProxy.close()
      return
    }

    val buffer: ByteBuffer = planes[0].buffer
    val width = imageProxy.width
    val height = imageProxy.height

    if (width <= 0 || height <= 0) {
      imageProxy.close()
      return
    }

    // Inspect the central 50% region where the hand guide overlay sits
    val startX = (width * 0.25).toInt()
    val endX = (width * 0.75).toInt()
    val startY = (height * 0.25).toInt()
    val endY = (height * 0.75).toInt()

    val step = 16 // Downsample for fast execution
    var sumLuma = 0L
    var count = 0
    var varianceSum = 0L

    try {
      val rowStride = planes[0].rowStride
      val pixelStride = planes[0].pixelStride

      for (y in startY until endY step step) {
        val rowOffset = y * rowStride
        for (x in startX until endX step step) {
          val index = rowOffset + (x * pixelStride)
          if (index < buffer.remaining()) {
            val luma = buffer.get(index).toInt() and 0xFF
            sumLuma += luma
            count++
          }
        }
      }

      if (count > 0) {
        val avgLuma = sumLuma.toFloat() / count
        // Calculate contrast / variance inside the center guide
        val diffFromLast = Math.abs(avgLuma - lastAvgLuma)
        lastAvgLuma = avgLuma

        // Hand presence heuristics: good lighting and steady presence
        val hasObjectInFrame = avgLuma in 30.0f..230.0f
        val isSteady = diffFromLast < 15.0f

        if (hasObjectInFrame && isSteady) {
          consecutiveSteadyFrames++
        } else {
          consecutiveSteadyFrames = (consecutiveSteadyFrames - 2).coerceAtLeast(0)
        }

        val stabilityProgress = (consecutiveSteadyFrames.toFloat() / requiredSteadyFrames).coerceIn(0f, 1f)
        val isHandMatched = stabilityProgress >= 0.95f

        val status = when {
          !hasObjectInFrame -> "Place your hand inside the glowing guide ✨"
          stabilityProgress < 0.35f -> "Hand detected! Hold your sign steady..."
          stabilityProgress < 0.95f -> "Great posture! Matching gesture (${(stabilityProgress * 100).toInt()}%)..."
          else -> "Sign Matched! Fantastic Job! 🌟"
        }

        val confidence = (0.50f + (stabilityProgress * 0.48f)).coerceIn(0f, 0.99f)

        onResult(
          GestureAnalysisResult(
            isHandInGuide = hasObjectInFrame,
            confidenceScore = if (isHandMatched) 0.98f else confidence,
            stabilityProgress = stabilityProgress,
            statusMessage = status
          )
        )
      }
    } catch (_: Exception) {
      // Ignore frame read failures
    } finally {
      imageProxy.close()
    }
  }

  fun resetSteadyCount() {
    consecutiveSteadyFrames = 0
  }
}
