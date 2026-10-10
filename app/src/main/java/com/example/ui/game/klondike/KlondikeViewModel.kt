package com.example.ui.game.klondike

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.savedgame.GameStateSerializer
import com.example.data.savedgame.SavedGameEntity
import com.example.data.savedgame.SavedGameRepository
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

  private val database = SolitaireDatabase.getDatabase(application)
  private val statsRepository = StatsRepository(database.statsDao())
  private val savedGameRepository = SavedGameRepository(database.savedGameDao())

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

  private val _isCelebrating = MutableStateFlow(false)
  val isCelebrating: StateFlow<Boolean> = _isCelebrating.asStateFlow()

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

  init {
    // Note: Do NOT record game play here. Games are only recorded as played when a New Game starts.
    updateCanUndo()
  }

  fun initializeGame(difficulty: DifficultyMode, drawMode: DrawMode, resume: Boolean) {
    viewModelScope.launch {
      if (resume) {
        val saved = savedGameRepository.getSavedGameDirect("KLONDIKE")
        if (saved != null) {
          try {
            val restoredState = GameStateSerializer.deserializeState(saved.stateJson)
            val restoredUndo = GameStateSerializer.deserializeUndoStack(saved.undoStackJson)
            engine.restoreState(restoredState, restoredUndo)
            updateCanUndo()
            startTimer()
            return@launch
          } catch (_: Exception) {
            // If failed to deserialize, start new game below
          }
        }
      }
      // Start brand new game
      startNewGame(difficulty, drawMode)
    }
  }

  fun startNewGame(
    difficulty: DifficultyMode = engine.currentState.difficulty,
    drawMode: DrawMode = engine.currentState.drawMode,
  ) {
    _currentHint.value = null
    _showResultSheet.value = false
    _isNewHighScore.value = false
    engine.startNewGame(difficulty = difficulty, drawMode = drawMode)
    updateCanUndo()
    startTimer()

    // Increment played count ONLY when a new game starts
    viewModelScope.launch {
      savedGameRepository.deleteSavedGame("KLONDIKE")
      statsRepository.recordGamePlayed("KLONDIKE", difficulty, drawMode)
    }
  }

  fun restartGame() {
    _currentHint.value = null
    _showResultSheet.value = false
    _isNewHighScore.value = false
    engine.restartGame()
    updateCanUndo()
    startTimer()
    viewModelScope.launch {
      savedGameRepository.deleteSavedGame("KLONDIKE")
      statsRepository.recordGamePlayed("KLONDIKE", engine.currentState.difficulty, engine.currentState.drawMode)
    }
  }

  fun pauseTimerAndSave() {
    pauseTimer()
    saveCurrentGame()
  }

  fun resumeTimer() {
    if (!engine.currentState.isWon) {
      startTimer()
    }
  }

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

  private fun pauseTimer() {
    timerJob?.cancel()
    timerJob = null
  }

  fun saveCurrentGame() {
    val state = engine.currentState
    if (state.moveCount > 0 && !state.isWon) {
      viewModelScope.launch {
        savedGameRepository.saveGame("KLONDIKE", state, engine.getUndoHistory())
      }
    }
  }

  fun giveUpGame() {
    pauseTimer()
    viewModelScope.launch {
      savedGameRepository.deleteSavedGame("KLONDIKE")
    }
  }

  private fun updateCanUndo() {
    _canUndo.value = engine.canUndo()
  }

  private fun checkWinState() {
    val state = engine.currentState
    if (state.isWon) {
      pauseTimer()
      viewModelScope.launch {
        savedGameRepository.deleteSavedGame("KLONDIKE")
      }
      if (!_showResultSheet.value && !_isCelebrating.value) {
        _isCelebrating.value = true
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
          // Allow victory bouncing animation to play before presenting result bottom sheet
          delay(2400L)
          _isCelebrating.value = false
          _showResultSheet.value = true
        }
      }
    } else {
      saveCurrentGame()
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
    saveCurrentGame()
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
    pauseTimer()
  }
}
