package com.tufisolitaire.wtf.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tufisolitaire.wtf.data.preferences.UserPreferencesRepository
import com.tufisolitaire.wtf.data.savedgame.SavedGameRepository
import com.tufisolitaire.wtf.data.stats.SolitaireDatabase
import com.tufisolitaire.wtf.data.stats.StatsRepository
import com.tufisolitaire.wtf.game.GameStats
import com.tufisolitaire.wtf.game.GameType
import com.tufisolitaire.wtf.game.klondike.DifficultyMode
import com.tufisolitaire.wtf.game.klondike.DrawMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

  private val database = SolitaireDatabase.getDatabase(application)
  private val statsRepository = StatsRepository(database.statsDao())
  private val savedGameRepository = SavedGameRepository(database.savedGameDao())
  private val userPrefsRepository = UserPreferencesRepository(application)

  private val _uiState = MutableStateFlow(
    HomeUiState(
      dialogDifficulty = userPrefsRepository.getLastDifficulty(),
      dialogDrawMode = userPrefsRepository.getLastDrawMode(),
    ),
  )
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    // Observe room stats
    viewModelScope.launch {
      statsRepository.getAllStatsForGame("KLONDIKE").collect { statsList ->
        val totalPlayed = statsList.sumOf { it.gamesPlayed }
        val totalWon = statsList.sumOf { it.gamesWon }
        _uiState.update { current ->
          val updatedMap = current.statsMap.toMutableMap()
          updatedMap[GameType.KLONDIKE] = GameStats(played = totalPlayed, won = totalWon)
          current.copy(statsMap = updatedMap)
        }
      }
    }

    // Observe saved game
    viewModelScope.launch {
      savedGameRepository.getSavedGame("KLONDIKE").collect { saved ->
        _uiState.update { it.copy(savedKlondikeGame = saved) }
      }
    }
  }

  fun selectGameType(gameType: GameType) {
    _uiState.update { it.copy(selectedGameType = gameType) }
  }

  fun toggleFavorite() {
    _uiState.update { it.copy(isFavorite = !it.isFavorite) }
  }

  fun onNewGameClicked() {
    val saved = _uiState.value.savedKlondikeGame
    if (saved != null) {
      _uiState.update { it.copy(showConfirmOverwriteDialog = true) }
    } else {
      _uiState.update { it.copy(showNewGameDialog = true) }
    }
  }

  fun confirmOverwriteAndShowDialog() {
    _uiState.update {
      it.copy(
        showConfirmOverwriteDialog = false,
        showNewGameDialog = true,
      )
    }
  }

  fun dismissOverwriteDialog() {
    _uiState.update { it.copy(showConfirmOverwriteDialog = false) }
  }

  fun dismissNewGameDialog() {
    _uiState.update { it.copy(showNewGameDialog = false) }
  }

  fun setDialogDifficulty(mode: DifficultyMode) {
    userPrefsRepository.setLastDifficulty(mode)
    val targetDraw = if (mode == DifficultyMode.HARD) {
      userPrefsRepository.setLastDrawMode(DrawMode.DRAW_THREE)
      DrawMode.DRAW_THREE
    } else {
      _uiState.value.dialogDrawMode
    }
    _uiState.update {
      it.copy(
        dialogDifficulty = mode,
        dialogDrawMode = targetDraw,
      )
    }
  }

  fun setDialogDrawMode(draw: DrawMode) {
    userPrefsRepository.setLastDrawMode(draw)
    _uiState.update { it.copy(dialogDrawMode = draw) }
  }
}
