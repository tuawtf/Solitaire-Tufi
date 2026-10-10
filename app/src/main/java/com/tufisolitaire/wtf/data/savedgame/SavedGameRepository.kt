package com.tufisolitaire.wtf.data.savedgame

import com.tufisolitaire.wtf.game.klondike.KlondikeGameState
import kotlinx.coroutines.flow.Flow

class SavedGameRepository(private val dao: SavedGameDao) {

  fun getSavedGame(gameType: String = "KLONDIKE"): Flow<SavedGameEntity?> {
    return dao.getSavedGame(gameType)
  }

  suspend fun getSavedGameDirect(gameType: String = "KLONDIKE"): SavedGameEntity? {
    return dao.getSavedGameDirect(gameType)
  }

  suspend fun saveGame(
    gameType: String = "KLONDIKE",
    state: KlondikeGameState,
    undoStack: List<KlondikeGameState>,
  ) {
    // Only save games with at least 1 move that are not already won
    if (state.moveCount <= 0 || state.isWon) return

    val stateJson = GameStateSerializer.serializeState(state)
    val undoStackJson = GameStateSerializer.serializeUndoStack(undoStack)

    val entity = SavedGameEntity(
      gameType = gameType,
      difficulty = state.difficulty.name,
      drawMode = state.drawMode.name,
      moveCount = state.moveCount,
      elapsedSeconds = state.elapsedSeconds,
      stateJson = stateJson,
      undoStackJson = undoStackJson,
      timestamp = System.currentTimeMillis(),
    )
    dao.saveGame(entity)
  }

  suspend fun deleteSavedGame(gameType: String = "KLONDIKE") {
    dao.deleteSavedGame(gameType)
  }
}
