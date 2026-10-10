package com.tufisolitaire.wtf.data.savedgame

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedGameDao {

  @Query("SELECT * FROM saved_game WHERE gameType = :gameType")
  fun getSavedGame(gameType: String): Flow<SavedGameEntity?>

  @Query("SELECT * FROM saved_game WHERE gameType = :gameType")
  suspend fun getSavedGameDirect(gameType: String): SavedGameEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveGame(game: SavedGameEntity)

  @Query("DELETE FROM saved_game WHERE gameType = :gameType")
  suspend fun deleteSavedGame(gameType: String)
}
