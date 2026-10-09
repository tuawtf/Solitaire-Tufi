package com.example.ui.home

import com.example.game.GameStats
import com.example.game.GameType

data class HomeUiState(
  val selectedGameType: GameType = GameType.KLONDIKE,
  val isFavorite: Boolean = false,
  val statsMap: Map<GameType, GameStats> = GameType.entries.associateWith { GameStats(played = 0, won = 0) },
) {
  val currentStats: GameStats
    get() = statsMap[selectedGameType] ?: GameStats(played = 0, won = 0)
}
