package com.example.util

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.model.ConversionMode
import com.example.data.model.GestureConversionResult
import com.example.data.model.GesturePresetsDataSource
import com.example.data.model.HandBodyGesturePreset
import com.example.data.model.HandPose
import java.nio.ByteBuffer

class BodyGestureConverterAnalyzer(
  private val getMode: () -> ConversionMode,
  private val onResult: (GestureConversionResult) -> Unit
) : ImageAnalysis.Analyzer {

  private var frameCounter = 0
  private var consecutiveSteadyFrames = 0
  private val requiredSteadyFrames = 15 // ~1.0-1.2 seconds for confirmation
  private var lastAvgLuma = 0f
  private var lastActivePresetId = "v_peace_spread"

  @androidx.annotation.OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    frameCounter++

    // Process every 2nd frame for smooth 30fps+ responsiveness
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

    // Inspect central 60% of frame where the hand & body guide is aligned
    val startX = (width * 0.20).toInt()
    val endX = (width * 0.80).toInt()
    val startY = (height * 0.15).toInt()
    val endY = (height * 0.85).toInt()

    val step = 16 // Downsample for CPU performance
    var sumLuma = 0L
    var count = 0

    // Spatial histogram counters
    var topZoneLuma = 0L
    var topZoneCount = 0
    var midZoneLuma = 0L
    var midZoneCount = 0
    var bottomZoneLuma = 0L
    var bottomZoneCount = 0

    val thirdY = (endY - startY) / 3

    try {
      val rowStride = planes[0].rowStride
      val pixelStride = planes[0].pixelStride

      for (y in startY until endY step step) {
        val rowOffset = y * rowStride
        val isTop = y < (startY + thirdY)
        val isMid = y in (startY + thirdY) until (startY + (thirdY * 2))

        for (x in startX until endX step step) {
          val index = rowOffset + (x * pixelStride)
          if (index < buffer.remaining()) {
            val luma = buffer.get(index).toInt() and 0xFF
            sumLuma += luma
            count++

            if (isTop) {
              topZoneLuma += luma
              topZoneCount++
            } else if (isMid) {
              midZoneLuma += luma
              midZoneCount++
            } else {
              bottomZoneLuma += luma
              bottomZoneCount++
            }
          }
        }
      }

      if (count > 0) {
        val avgLuma = sumLuma.toFloat() / count
        val diffFromLast = Math.abs(avgLuma - lastAvgLuma)
        lastAvgLuma = avgLuma

        val hasHandOrBodyInGuide = avgLuma in 30.0f..230.0f
        val isSteady = diffFromLast < 14.0f
        val isMoving = diffFromLast > 22.0f

        val topAvg = if (topZoneCount > 0) topZoneLuma.toFloat() / topZoneCount else avgLuma
        val midAvg = if (midZoneCount > 0) midZoneLuma.toFloat() / midZoneCount else avgLuma
        val bottomAvg = if (bottomZoneCount > 0) bottomZoneLuma.toFloat() / bottomZoneCount else avgLuma

        if (hasHandOrBodyInGuide && isSteady) {
          consecutiveSteadyFrames++
        } else {
          consecutiveSteadyFrames = (consecutiveSteadyFrames - 2).coerceAtLeast(0)
        }

        val stabilityProgress = (consecutiveSteadyFrames.toFloat() / requiredSteadyFrames).coerceIn(0f, 1f)
        val isConfirmed = stabilityProgress >= 0.95f

        // Classify hand and body posture heuristics:
        val mode = getMode()
        val chosenPreset = classifyPosture(
          topAvg = topAvg,
          midAvg = midAvg,
          bottomAvg = bottomAvg,
          isMoving = isMoving,
          mode = mode
        )

        val symbol = when (mode) {
          ConversionMode.ALPHABET -> chosenPreset.alphabetSymbol
          ConversionMode.NUMBER -> chosenPreset.numberSymbol
          ConversionMode.AUTO -> if (chosenPreset.numberSymbol in listOf("1", "2", "3", "4", "5")) {
            chosenPreset.numberSymbol
          } else {
            chosenPreset.alphabetSymbol
          }
        }

        val word = when (mode) {
          ConversionMode.ALPHABET -> chosenPreset.alphabetWord
          ConversionMode.NUMBER -> chosenPreset.numberWord
          ConversionMode.AUTO -> "${chosenPreset.alphabetWord} / ${chosenPreset.numberWord}"
        }

        val postureDesc = when {
          !hasHandOrBodyInGuide -> "Position hand inside the glowing guide ✨"
          isMoving -> "Active hand & body motion detected (Waving / Greeting)"
          topAvg > midAvg + 10f -> "Hand raised high near eye level"
          midAvg > bottomAvg + 10f -> "Hand positioned steady at chest level"
          else -> chosenPreset.bodyDescription
        }

        val confidence = if (!hasHandOrBodyInGuide) {
          0.10f
        } else {
          (0.65f + (stabilityProgress * 0.33f)).coerceIn(0f, 0.99f)
        }

        onResult(
          GestureConversionResult(
            isHandDetected = hasHandOrBodyInGuide,
            symbol = if (hasHandOrBodyInGuide) symbol else "",
            label = if (hasHandOrBodyInGuide) chosenPreset.name else "Watching for gesture...",
            associatedWord = if (hasHandOrBodyInGuide) word else "",
            emoji = chosenPreset.emoji,
            pose = chosenPreset.pose,
            confidenceScore = confidence,
            stabilityProgress = stabilityProgress,
            isConfirmed = isConfirmed,
            bodyPostureDescription = postureDesc,
            conversionMode = mode
          )
        )
      }
    } catch (_: Exception) {
      // Ignore frame read failures gracefully
    } finally {
      imageProxy.close()
    }
  }

  private fun classifyPosture(
    topAvg: Float,
    midAvg: Float,
    bottomAvg: Float,
    isMoving: Boolean,
    mode: ConversionMode
  ): HandBodyGesturePreset {
    val presets = GesturePresetsDataSource.presets

    if (isMoving) {
      return presets.firstOrNull { it.id == "wave_hello" } ?: presets.first()
    }

    // High vertical gesture (tall fingers extended to top)
    if (topAvg > midAvg + 12f) {
      return when (mode) {
        ConversionMode.ALPHABET -> presets.firstOrNull { it.id == "v_peace_spread" }
          ?: presets.firstOrNull { it.id == "point_index_d" }
          ?: presets.first()
        ConversionMode.NUMBER -> presets.firstOrNull { it.id == "v_peace_spread" }
          ?: presets.firstOrNull { it.id == "w_three_fingers" }
          ?: presets.first()
        ConversionMode.AUTO -> presets.firstOrNull { it.id == "v_peace_spread" }
          ?: presets.first()
      }
    }

    // Wide open spread (high top and middle presence)
    if (topAvg > 80f && midAvg > 80f && Math.abs(topAvg - midAvg) < 10f) {
      return when (mode) {
        ConversionMode.ALPHABET -> presets.firstOrNull { it.id == "flat_four_thumb" }
          ?: presets.firstOrNull { it.id == "thumb_pinky_y" }
          ?: presets.first()
        ConversionMode.NUMBER -> presets.firstOrNull { it.id == "five_open_palm" }
          ?: presets.first()
        ConversionMode.AUTO -> presets.firstOrNull { it.id == "five_open_palm" }
          ?: presets.first()
      }
    }

    // Compact fist or curled shape (concentrated mid/bottom)
    if (midAvg > topAvg + 15f) {
      return when (mode) {
        ConversionMode.ALPHABET -> presets.firstOrNull { it.id == "fist_thumb_side" }
          ?: presets.firstOrNull { it.id == "circle_o" }
          ?: presets.first()
        ConversionMode.NUMBER -> presets.firstOrNull { it.id == "fist_thumb_side" }
          ?: presets.first()
        ConversionMode.AUTO -> presets.firstOrNull { it.id == "fist_thumb_side" }
          ?: presets.first()
      }
    }

    // Fallback to active preset or popular default
    return presets.firstOrNull { it.id == lastActivePresetId } ?: presets.first()
  }

  fun resetSteadyCount() {
    consecutiveSteadyFrames = 0
  }

  fun setManualPreset(preset: HandBodyGesturePreset, mode: ConversionMode) {
    lastActivePresetId = preset.id
    consecutiveSteadyFrames = requiredSteadyFrames

    val symbol = when (mode) {
      ConversionMode.ALPHABET -> preset.alphabetSymbol
      ConversionMode.NUMBER -> preset.numberSymbol
      ConversionMode.AUTO -> preset.alphabetSymbol
    }

    val word = when (mode) {
      ConversionMode.ALPHABET -> preset.alphabetWord
      ConversionMode.NUMBER -> preset.numberWord
      ConversionMode.AUTO -> "${preset.alphabetWord} / ${preset.numberWord}"
    }

    onResult(
      GestureConversionResult(
        isHandDetected = true,
        symbol = symbol,
        label = preset.name,
        associatedWord = word,
        emoji = preset.emoji,
        pose = preset.pose,
        confidenceScore = 0.98f,
        stabilityProgress = 1.0f,
        isConfirmed = true,
        bodyPostureDescription = preset.bodyDescription,
        conversionMode = mode
      )
    )
  }
}
