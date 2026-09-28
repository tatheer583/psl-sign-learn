package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LearningDataSource
import com.example.data.model.ActiveScreen
import com.example.ui.components.ConfettiCelebration
import com.example.ui.screens.AlphabetWordsScreen
import com.example.ui.screens.CameraPracticeScreen
import com.example.ui.screens.CameraQuizScreen
import com.example.ui.screens.GameQuizScreen
import com.example.ui.screens.GestureConverterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SpeechToSignScreen
import com.example.ui.screens.SpeedMatchScreen
import com.example.ui.screens.StickersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.IrssaViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          contentWindowInsets = WindowInsets.safeDrawing,
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          IrssaApp(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun IrssaApp(
  modifier: Modifier = Modifier,
  viewModel: IrssaViewModel = viewModel()
) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val progress by viewModel.userProgress.collectAsStateWithLifecycle()
  val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
  val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
  val cameraFacing by viewModel.cameraFacing.collectAsStateWithLifecycle()
  val isTorchOn by viewModel.isTorchOn.collectAsStateWithLifecycle()
  val matchProgress by viewModel.cameraMatchProgress.collectAsStateWithLifecycle()
  val cameraStatusMessage by viewModel.cameraStatusMessage.collectAsStateWithLifecycle()
  val isCameraMatched by viewModel.isCameraMatched.collectAsStateWithLifecycle()
  val quizQuestion by viewModel.currentQuizQuestion.collectAsStateWithLifecycle()
  val quizSelectedOption by viewModel.quizSelectedOption.collectAsStateWithLifecycle()
  val isQuizAnswerCorrect by viewModel.isQuizAnswerCorrect.collectAsStateWithLifecycle()
  val showCelebration by viewModel.showCelebration.collectAsStateWithLifecycle()

  Box(modifier = modifier.fillMaxSize()) {
    when (currentScreen) {
      ActiveScreen.ONBOARDING -> {
        OnboardingScreen(
          onComplete = {
            viewModel.completeOnboarding()
          }
        )
      }

      ActiveScreen.HOME -> {
        HomeScreen(
          progress = progress,
          onNavigate = { screen ->
            viewModel.navigateTo(screen)
          },
          onSelectItem = { item ->
            viewModel.selectItem(item)
          },
          onSelectLevel = { level ->
            viewModel.selectLevel(level)
          }
        )
      }

      ActiveScreen.LEARN -> {
        selectedItem?.let { item ->
          LearnScreen(
            selectedItem = item,
            selectedLevel = selectedLevel,
            progress = progress,
            onSelectItem = { newItem ->
              viewModel.selectItem(newItem)
            },
            onOpenPractice = {
              viewModel.navigateTo(ActiveScreen.CAMERA_STUDIO)
            },
            onBack = {
              viewModel.navigateTo(ActiveScreen.HOME)
            }
          )
        }
      }

      ActiveScreen.CAMERA_STUDIO -> {
        selectedItem?.let { item ->
          CameraPracticeScreen(
            targetItem = item,
            cameraFacing = cameraFacing,
            isTorchOn = isTorchOn,
            matchProgress = matchProgress,
            statusMessage = cameraStatusMessage,
            isMatched = isCameraMatched,
            onToggleCamera = {
              viewModel.toggleCameraFacing()
            },
            onToggleTorch = {
              viewModel.toggleTorch()
            },
            onAnalysisUpdate = { prog, status, matched ->
              viewModel.updateCameraAnalysis(prog, status, matched)
            },
            onResetMatch = {
              viewModel.resetCameraMatch()
            },
            onBack = {
              viewModel.navigateTo(ActiveScreen.HOME)
            }
          )
        }
      }

      ActiveScreen.QUIZ -> {
        GameQuizScreen(
          question = quizQuestion,
          selectedOption = quizSelectedOption,
          isAnswerCorrect = isQuizAnswerCorrect,
          progress = progress,
          onSelectAnswer = { chosen ->
            viewModel.answerQuizQuestion(chosen)
          },
          onNextQuestion = {
            viewModel.generateNewQuizQuestion()
          },
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }

      ActiveScreen.SPEECH_TO_SIGN -> {
        SpeechToSignScreen(
          onPracticeItem = { item ->
            viewModel.selectItem(item)
            viewModel.navigateTo(ActiveScreen.CAMERA_STUDIO)
          },
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }

      ActiveScreen.SPEED_MATCH -> {
        SpeedMatchScreen(
          progress = progress,
          onAddScore = { score ->
            viewModel.addSpeedMatchScore(score)
          },
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }

      ActiveScreen.STICKERS -> {
        StickersScreen(
          badges = viewModel.getAllBadges(),
          progress = progress,
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }

      ActiveScreen.LEARN_ALPHABET -> {
        selectedItem?.let { item ->
          LearnScreen(
            selectedItem = item,
            selectedLevel = selectedLevel,
            progress = progress,
            onSelectItem = { newItem ->
              viewModel.selectItem(newItem)
            },
            onOpenPractice = {
              viewModel.navigateTo(ActiveScreen.CAMERA_STUDIO)
            },
            onBack = {
              viewModel.navigateTo(ActiveScreen.HOME)
            },
            customItemList = LearningDataSource.alphabetItems,
            customTitle = "Alphabet Studio (A to Z)"
          )
        }
      }

      ActiveScreen.LEARN_NUMBERS -> {
        selectedItem?.let { item ->
          LearnScreen(
            selectedItem = item,
            selectedLevel = selectedLevel,
            progress = progress,
            onSelectItem = { newItem ->
              viewModel.selectItem(newItem)
            },
            onOpenPractice = {
              viewModel.navigateTo(ActiveScreen.CAMERA_STUDIO)
            },
            onBack = {
              viewModel.navigateTo(ActiveScreen.HOME)
            },
            customItemList = LearningDataSource.numberItems,
            customTitle = "Numbers Studio (1 to 50)"
          )
        }
      }

      ActiveScreen.ALPHABET_WORDS -> {
        AlphabetWordsScreen(
          progress = progress,
          onPracticeInCamera = { item ->
            viewModel.selectItem(item)
            viewModel.navigateTo(ActiveScreen.CAMERA_STUDIO)
          },
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }

      ActiveScreen.CAMERA_QUIZ -> {
        CameraQuizScreen(
          progress = progress,
          onAwardPoints = { points, itemId ->
            viewModel.awardCameraQuizPoints(points, itemId)
          },
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }

      ActiveScreen.GESTURE_CONVERTER -> {
        GestureConverterScreen(
          progress = progress,
          onAwardStars = { stars ->
            viewModel.addSpeedMatchScore(stars)
          },
          onBack = {
            viewModel.navigateTo(ActiveScreen.HOME)
          }
        )
      }
    }

    // Celebratory Confetti Burst
    ConfettiCelebration(
      isActive = showCelebration,
      onFinished = {
        viewModel.dismissCelebration()
      }
    )
  }
}
