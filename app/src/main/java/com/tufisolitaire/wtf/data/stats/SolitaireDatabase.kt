package com.tufisolitaire.wtf.data.stats

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tufisolitaire.wtf.data.savedgame.SavedGameDao
import com.tufisolitaire.wtf.data.savedgame.SavedGameEntity

@Database(
  entities = [GameModeStatsEntity::class, SavedGameEntity::class],
  version = 2,
  exportSchema = false,
)
abstract class SolitaireDatabase : RoomDatabase() {

  abstract fun statsDao(): GameModeStatsDao
  abstract fun savedGameDao(): SavedGameDao

  companion object {
    @Volatile
    private var INSTANCE: SolitaireDatabase? = null

    fun getDatabase(context: Context): SolitaireDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          SolitaireDatabase::class.java,
          "solitaire_database",
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
