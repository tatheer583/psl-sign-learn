package com.example.viewmodel

import android.app.Application
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LearningDataSource
import com.example.data.db.AppDatabase
import com.example.data.db.ProgressEntity
import com.example.data.db.ProgressRepository
import com.example.data.model.ActiveScreen
import com.example.data.model.GameLevel
import com.example.data.model.LearningItem
import com.example.data.model.StickerBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizQuestion(
  val targetItem: LearningItem,
  val questionPrompt: String,
  val options: List<LearningItem>,
  val questionType: QuizType
)

enum class QuizType {
  IDENTIFY_SIGN,     // Look at sign, pick letter/number
  MATCH_OBJECT,      // Look at object (e.g. 🍎), pick correct sign
  COUNTING_FINGERS   // Count items, pick number sign
}

class IrssaViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ProgressRepository = ProgressRepository(
    AppDatabase.getDatabase(application).progressDao()
  )

  val userProgress: StateFlow<ProgressEntity> = repository.progressFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = ProgressEntity()
    )

  private val _currentScreen = MutableStateFlow(ActiveScreen.HOME)
  val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

  private val _selectedItem = MutableStateFlow<LearningItem?>(LearningDataSource.alphabetItems.first())
  val selectedItem: StateFlow<LearningItem?> = _selectedItem.asStateFlow()

  private val _selectedLevel = MutableStateFlow<GameLevel>(LearningDataSource.levels.first())
  val selectedLevel: StateFlow<GameLevel> = _selectedLevel.asStateFlow()

  // Camera Studio State
  private val _cameraFacing = MutableStateFlow(CameraSelector.LENS_FACING_FRONT)
  val cameraFacing: StateFlow<Int> = _cameraFacing.asStateFlow()

  private val _isTorchOn = MutableStateFlow(false)
  val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

  private val _cameraStatusMessage = MutableStateFlow("Position your hand inside the glowing guide ✨")
  val cameraStatusMessage: StateFlow<String> = _cameraStatusMessage.asStateFlow()

  private val _cameraMatchProgress = MutableStateFlow(0f)
  val cameraMatchProgress: StateFlow<Float> = _cameraMatchProgress.asStateFlow()

  private val _isCameraMatched = MutableStateFlow(false)
  val isCameraMatched: StateFlow<Boolean> = _isCameraMatched.asStateFlow()

  // Quiz State
  private val _currentQuizQuestion = MutableStateFlow<QuizQuestion?>(null)
  val currentQuizQuestion: StateFlow<QuizQuestion?> = _currentQuizQuestion.asStateFlow()

  private val _quizSelectedOption = MutableStateFlow<LearningItem?>(null)
  val quizSelectedOption: StateFlow<LearningItem?> = _quizSelectedOption.asStateFlow()

  private val _isQuizAnswerCorrect = MutableStateFlow<Boolean?>(null)
  val isQuizAnswerCorrect: StateFlow<Boolean?> = _isQuizAnswerCorrect.asStateFlow()

  private val _showCelebration = MutableStateFlow(false)
  val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

  init {
    viewModelScope.launch {
      val progress = repository.getCurrentProgress()
      if (!progress.hasCompletedOnboarding) {
        _currentScreen.value = ActiveScreen.ONBOARDING
      }
    }
  }

  fun navigateTo(screen: ActiveScreen) {
    _currentScreen.value = screen
    if (screen == ActiveScreen.QUIZ) {
      generateNewQuizQuestion()
    }
  }

  fun selectItem(item: LearningItem) {
    _selectedItem.value = item
    _cameraMatchProgress.value = 0f
    _isCameraMatched.value = false
    _cameraStatusMessage.value = "Show sign for '${item.title}' in the guide"
  }

  fun selectLevel(level: GameLevel) {
    _selectedLevel.value = level
    val firstItemInLevel = LearningDataSource.allItems.firstOrNull { it.id in level.itemIds }
    if (firstItemInLevel != null) {
      _selectedItem.value = firstItemInLevel
    }
  }

  fun toggleCameraFacing() {
    _cameraFacing.value = if (_cameraFacing.value == CameraSelector.LENS_FACING_FRONT) {
      CameraSelector.LENS_FACING_BACK
    } else {
      CameraSelector.LENS_FACING_FRONT
    }
  }

  fun toggleTorch() {
    _isTorchOn.value = !_isTorchOn.value
  }

  fun updateCameraAnalysis(progress: Float, status: String, isMatched: Boolean) {
    _cameraMatchProgress.value = progress
    _cameraStatusMessage.value = status

    if (isMatched && !_isCameraMatched.value) {
      _isCameraMatched.value = true
      _showCelebration.value = true
      viewModelScope.launch {
        _selectedItem.value?.let {
          repository.addStars(10, it.id)
        }
      }
    }
  }

  fun resetCameraMatch() {
    _isCameraMatched.value = false
    _cameraMatchProgress.value = 0f
    _cameraStatusMessage.value = "Position your hand inside the glowing guide ✨"
  }

  fun completeOnboarding() {
    viewModelScope.launch {
      repository.setOnboardingCompleted()
      _currentScreen.value = ActiveScreen.HOME
    }
  }

  fun generateNewQuizQuestion() {
    _quizSelectedOption.value = null
    _isQuizAnswerCorrect.value = null

    val isAlphabet = kotlin.random.Random.nextBoolean()
    val pool = if (isAlphabet) LearningDataSource.alphabetItems else LearningDataSource.numberItems
    val target = pool.random()
    val otherOptions = pool
      .filter { it.id != target.id }
      .shuffled()
      .take(2)

    val options = (otherOptions + target).shuffled()

    val type = when {
      target.visualItemsCount > 0 -> QuizType.COUNTING_FINGERS
      target.category == com.example.data.model.SignCategory.ALPHABET -> QuizType.IDENTIFY_SIGN
      else -> QuizType.MATCH_OBJECT
    }

    val prompt = "Find sign for '${target.title}'"

    _currentQuizQuestion.value = QuizQuestion(
      targetItem = target,
      questionPrompt = prompt,
      options = options,
      questionType = type
    )
  }

  fun answerQuizQuestion(chosenItem: LearningItem) {
    if (_quizSelectedOption.value != null) return

    _quizSelectedOption.value = chosenItem
    val isCorrect = chosenItem.id == _currentQuizQuestion.value?.targetItem?.id
    _isQuizAnswerCorrect.value = isCorrect

    if (isCorrect) {
      _showCelebration.value = true
      viewModelScope.launch {
        repository.addStars(5, chosenItem.id)
      }
    }
  }

  fun awardCameraQuizPoints(points: Int, itemId: String) {
    viewModelScope.launch {
      repository.addStars(points, itemId)
    }
  }

  fun addSpeedMatchScore(score: Int) {
    val bonusStars = (score / 2).coerceAtLeast(5)
    viewModelScope.launch {
      repository.addStars(bonusStars)
      if (score >= 40) {
        repository.unlockBadge("badge_speed_match")
      }
    }
  }

  fun dismissCelebration() {
    _showCelebration.value = false
  }

  fun getAllBadges(): List<StickerBadge> {
    val progress = userProgress.value
    val currentStars = progress.stars
    val unlockedFromCsv = progress.unlockedBadgesCsv.split(",").toSet()

    return LearningDataSource.badges.map { badge ->
      val isUnlocked = currentStars >= badge.requiredStars || unlockedFromCsv.contains(badge.id)
      badge.copy(isUnlocked = isUnlocked)
    }
  }

  fun resetAllProgress() {
    viewModelScope.launch {
      repository.resetProgress()
      _currentScreen.value = ActiveScreen.HOME
    }
  }
}
