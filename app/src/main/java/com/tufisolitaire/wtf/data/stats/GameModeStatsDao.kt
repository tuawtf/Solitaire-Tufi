package com.tufisolitaire.wtf.data.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameModeStatsDao {

  @Query("SELECT * FROM game_mode_stats WHERE id = :id")
  fun getStats(id: String): Flow<GameModeStatsEntity?>

  @Query("SELECT * FROM game_mode_stats WHERE id = :id")
  suspend fun getStatsDirect(id: String): GameModeStatsEntity?

  @Query("SELECT * FROM game_mode_stats WHERE gameType = :gameType")
  fun getStatsForGame(gameType: String): Flow<List<GameModeStatsEntity>>

  @Query("SELECT * FROM game_mode_stats")
  fun getAllStats(): Flow<List<GameModeStatsEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(stats: GameModeStatsEntity)
}
