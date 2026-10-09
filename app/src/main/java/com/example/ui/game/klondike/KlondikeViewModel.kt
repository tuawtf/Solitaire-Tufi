package com.example.ui.game.klondike

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.stats.GameModeStatsEntity
import com.example.data.stats.SolitaireDatabase
import com.example.data.stats.StatsRepository
import com.example.game.Suit
import com.example.game.klondike.DifficultyMode
import com.example.game.klondike.DrawMode
import com.example.game.klondike.HintInfo
import com.example.game.klondike.KlondikeEngine
import com.example.game.klondike.KlondikeGameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KlondikeViewModel(application: Application) : AndroidViewModel(application) {

  private val statsRepository = StatsRepository(
    SolitaireDatabase.getDatabase(application).statsDao(),
  )

  private val engine = KlondikeEngine(
    initialDifficulty = DifficultyMode.REGULAR,
    initialDrawMode = DrawMode.DRAW_ONE,
  )

  val gameState: StateFlow<KlondikeGameState> = engine.state

  private val _canUndo = MutableStateFlow(false)
  val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

  private val _currentHint = MutableStateFlow<HintInfo?>(null)
  val currentHint: StateFlow<HintInfo?> = _currentHint.asStateFlow()

  private val _isAutoCompleting = MutableStateFlow(false)
  val isAutoCompleting: StateFlow<Boolean> = _isAutoCompleting.asStateFlow()

  private val _showResultSheet = MutableStateFlow(false)
  val showResultSheet: StateFlow<Boolean> = _showResultSheet.asStateFlow()

  private val _isNewHighScore = MutableStateFlow(false)
  val isNewHighScore: StateFlow<Boolean> = _isNewHighScore.asStateFlow()

  val currentModeStats: StateFlow<GameModeStatsEntity?> = gameState
    .flatMapLatest { state ->
      statsRepository.getStats("KLONDIKE", state.difficulty, state.drawMode)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null,
    )

  private var timerJob: Job? = null
  private var hasRecordedGameStart = false

  init {
    startTimer()
    updateCanUndo()
    recordInitialGameStart()
  }

  private fun recordInitialGameStart() {
    if (!hasRecordedGameStart) {
      hasRecordedGameStart = true
      viewModelScope.launch {
        statsRepository.recordGamePlayed("KLONDIKE", engine.currentState.difficulty, engine.currentState.drawMode)
      }
    }
  }

  /**
   * Starts timer counting immediately upon game creation and pauses upon win.
   */
  private fun startTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (isActive) {
        delay(1000L)
        if (!engine.currentState.isWon) {
          engine.tickTimer(1L)
        }
      }
    }
  }

  private fun updateCanUndo() {
    _canUndo.value = engine.canUndo()
  }

  private fun checkWinState() {
    val state = engine.currentState
    if (state.isWon && !_showResultSheet.value) {
      _showResultSheet.value = true
      viewModelScope.launch {
        val newRecord = statsRepository.recordGameWon(
          gameType = "KLONDIKE",
          difficulty = state.difficulty,
          drawMode = state.drawMode,
          score = state.calculatedScore,
          timeSeconds = state.elapsedSeconds,
          moves = state.moveCount,
        )
        _isNewHighScore.value = newRecord
      }
    }
  }

  fun requestHint() {
    if (!engine.currentState.difficulty.hasHints) return
    _currentHint.value = engine.findHint()
  }

  fun clearHint() {
    _currentHint.value = null
  }

  fun dismissResultSheet() {
    _showResultSheet.value = false
  }

  fun startAutoComplete() {
    if (_isAutoCompleting.value || !engine.currentState.canAutoComplete) return

    _isAutoCompleting.value = true
    viewModelScope.launch {
      while (isActive && !engine.currentState.isWon) {
        val moved = engine.performAutoCompleteStep()
        updateCanUndo()
        if (!moved) break
        delay(110L)
      }
      _isAutoCompleting.value = false
      checkWinState()
    }
  }

  fun setDifficulty(difficulty: DifficultyMode) {
    _currentHint.value = null
    _showResultSheet.value = false
    _isNewHighScore.value = false
    engine.startNewGame(difficulty = difficulty, drawMode = difficulty.defaultDrawMode)
    updateCanUndo()
    startTimer()
    viewModelScope.launch {
      statsRepository.recordGamePlayed("KLONDIKE", difficulty, difficulty.defaultDrawMode)
    }
  }

  fun setDrawMode(drawMode: DrawMode) {
    _currentHint.value = null
    _showResultSheet.value = false
    _isNewHighScore.value = false
    engine.startNewGame(difficulty = engine.currentState.difficulty, drawMode = drawMode)
    updateCanUndo()
    startTimer()
    viewModelScope.launch {
      statsRepository.recordGamePlayed("KLONDIKE", engine.currentState.difficulty, drawMode)
    }
  }

  fun startNewGame() {
    _currentHint.value = null
    _showResultSheet.value = false
    _isNewHighScore.value = false
    val diff = engine.currentState.difficulty
    val draw = engine.currentState.drawMode
    engine.startNewGame(difficulty = diff, drawMode = draw)
    updateCanUndo()
    startTimer()
    viewModelScope.launch {
      statsRepository.recordGamePlayed("KLONDIKE", diff, draw)
    }
  }

  fun restartGame() {
    _currentHint.value = null
    _showResultSheet.value = false
    _isNewHighScore.value = false
    engine.restartGame()
    updateCanUndo()
    startTimer()
  }

  fun drawFromStock(): Boolean {
    clearHint()
    val success = engine.drawFromStock()
    updateCanUndo()
    checkWinState()
    return success
  }

  fun undo(): Boolean {
    clearHint()
    val success = engine.undo()
    updateCanUndo()
    return success
  }

  fun autoPlayCardFromTableau(columnIndex: Int): Boolean {
    clearHint()
    val success = engine.autoPlayCardFromTableau(columnIndex)
    updateCanUndo()
    checkWinState()
    return success
  }

  fun autoPlayCardFromWaste(): Boolean {
    clearHint()
    val success = engine.autoPlayCardFromWaste()
    updateCanUndo()
    checkWinState()
    return success
  }

  fun moveWasteToTableau(toColumn: Int): Boolean {
    clearHint()
    val success = engine.moveWasteToTableau(toColumn)
    updateCanUndo()
    checkWinState()
    return success
  }

  fun moveWasteToFoundation(): Boolean {
    clearHint()
    val success = engine.moveWasteToFoundation()
    updateCanUndo()
    checkWinState()
    return success
  }

  fun moveTableauToFoundation(fromColumn: Int): Boolean {
    clearHint()
    val success = engine.moveTableauToFoundation(fromColumn)
    updateCanUndo()
    checkWinState()
    return success
  }

  fun moveTableauToTableau(fromColumn: Int, cardIndex: Int, toColumn: Int): Boolean {
    clearHint()
    val success = engine.moveTableauToTableau(fromColumn, cardIndex, toColumn)
    updateCanUndo()
    checkWinState()
    return success
  }

  fun moveFoundationToTableau(suit: Suit, toColumn: Int): Boolean {
    clearHint()
    val success = engine.moveFoundationToTableau(suit, toColumn)
    updateCanUndo()
    checkWinState()
    return success
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
