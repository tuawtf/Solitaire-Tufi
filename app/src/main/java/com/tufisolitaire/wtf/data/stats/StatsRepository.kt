package com.tufisolitaire.wtf.data.stats

import com.tufisolitaire.wtf.game.klondike.DifficultyMode
import com.tufisolitaire.wtf.game.klondike.DrawMode
import kotlinx.coroutines.flow.Flow

class StatsRepository(private val dao: GameModeStatsDao) {

  fun getStats(
    gameType: String = "KLONDIKE",
    difficulty: DifficultyMode,
    drawMode: DrawMode,
  ): Flow<GameModeStatsEntity?> {
    val id = GameModeStatsEntity.createId(gameType, difficulty.name, drawMode.name)
    return dao.getStats(id)
  }

  fun getAllStatsForGame(gameType: String = "KLONDIKE"): Flow<List<GameModeStatsEntity>> {
    return dao.getStatsForGame(gameType)
  }

  suspend fun recordGamePlayed(
    gameType: String = "KLONDIKE",
    difficulty: DifficultyMode,
    drawMode: DrawMode,
  ) {
    val id = GameModeStatsEntity.createId(gameType, difficulty.name, drawMode.name)
    val existing = dao.getStatsDirect(id)

    val updated = if (existing != null) {
      existing.copy(
        gamesPlayed = existing.gamesPlayed + 1,
        lastPlayedTimestamp = System.currentTimeMillis(),
      )
    } else {
      GameModeStatsEntity(
        id = id,
        gameType = gameType,
        difficulty = difficulty.name,
        drawMode = drawMode.name,
        gamesPlayed = 1,
        gamesWon = 0,
      )
    }
    dao.insertOrUpdate(updated)
  }

  suspend fun recordGameWon(
    gameType: String = "KLONDIKE",
    difficulty: DifficultyMode,
    drawMode: DrawMode,
    score: Int,
    timeSeconds: Long,
    moves: Int,
  ): Boolean {
    val id = GameModeStatsEntity.createId(gameType, difficulty.name, drawMode.name)
    val existing = dao.getStatsDirect(id)

    val isNewHighScore = existing == null || score > existing.bestScore

    val updated = if (existing != null) {
      existing.copy(
        gamesWon = existing.gamesWon + 1,
        bestScore = maxOf(existing.bestScore, score),
        bestTimeSeconds = if (existing.bestTimeSeconds == 0L) timeSeconds else minOf(existing.bestTimeSeconds, timeSeconds),
        fewestMoves = if (existing.fewestMoves == 0) moves else minOf(existing.fewestMoves, moves),
        lastPlayedTimestamp = System.currentTimeMillis(),
      )
    } else {
      GameModeStatsEntity(
        id = id,
        gameType = gameType,
        difficulty = difficulty.name,
        drawMode = drawMode.name,
        gamesPlayed = 1,
        gamesWon = 1,
        bestScore = score,
        bestTimeSeconds = timeSeconds,
        fewestMoves = moves,
      )
    }
    dao.insertOrUpdate(updated)
    return isNewHighScore
  }
}
