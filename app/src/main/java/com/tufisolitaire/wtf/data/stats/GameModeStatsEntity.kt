package com.tufisolitaire.wtf.data.stats

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_mode_stats")
data class GameModeStatsEntity(
  @PrimaryKey val id: String, // e.g. "KLONDIKE_REGULAR_DRAW_ONE"
  val gameType: String = "KLONDIKE",
  val difficulty: String,     // "RELAXED", "REGULAR", "HARD"
  val drawMode: String,       // "DRAW_ONE", "DRAW_THREE"
  val gamesPlayed: Int = 0,
  val gamesWon: Int = 0,
  val bestScore: Int = 0,
  val bestTimeSeconds: Long = 0L,
  val fewestMoves: Int = 0,
  val lastPlayedTimestamp: Long = System.currentTimeMillis(),
) {
  val winRatePercentage: Int
    get() = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed) * 100).toInt() else 0

  val formattedRatio: String
    get() = "$gamesWon/$gamesPlayed"

  companion object {
    fun createId(gameType: String, difficulty: String, drawMode: String): String {
      return "${gameType.uppercase()}_${difficulty.uppercase()}_${drawMode.uppercase()}"
    }
  }
}
