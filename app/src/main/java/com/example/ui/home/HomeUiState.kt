package com.example.ui.home

import com.example.data.savedgame.SavedGameEntity
import com.example.game.GameStats
import com.example.game.GameType
import com.example.game.klondike.DifficultyMode
import com.example.game.klondike.DrawMode

data class HomeUiState(
  val selectedGameType: GameType = GameType.KLONDIKE,
  val isFavorite: Boolean = false,
  val statsMap: Map<GameType, GameStats> = GameType.entries.associateWith { GameStats(played = 0, won = 0) },
  val savedKlondikeGame: SavedGameEntity? = null,
  val showNewGameDialog: Boolean = false,
  val showConfirmOverwriteDialog: Boolean = false,
  val dialogDifficulty: DifficultyMode = DifficultyMode.REGULAR,
  val dialogDrawMode: DrawMode = DrawMode.DRAW_ONE,
) {
  val currentStats: GameStats
    get() = statsMap[selectedGameType] ?: GameStats(played = 0, won = 0)
}
