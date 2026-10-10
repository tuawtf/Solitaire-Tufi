package com.tufisolitaire.wtf.ui.home

import com.tufisolitaire.wtf.data.savedgame.SavedGameEntity
import com.tufisolitaire.wtf.game.GameStats
import com.tufisolitaire.wtf.game.GameType
import com.tufisolitaire.wtf.game.klondike.DifficultyMode
import com.tufisolitaire.wtf.game.klondike.DrawMode

data class HomeUiState(
  val selectedGameType: GameType = GameType.KLONDIKE,
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
