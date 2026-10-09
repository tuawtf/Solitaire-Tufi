package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.stats.SolitaireDatabase
import com.example.data.stats.StatsRepository
import com.example.game.GameStats
import com.example.game.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

  private val statsRepository = StatsRepository(
    SolitaireDatabase.getDatabase(application).statsDao(),
  )

  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
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
  }

  fun selectGameType(gameType: GameType) {
    _uiState.update { it.copy(selectedGameType = gameType) }
  }

  fun toggleFavorite() {
    _uiState.update { it.copy(isFavorite = !it.isFavorite) }
  }
}
