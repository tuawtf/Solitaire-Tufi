package com.example.data.stats

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameModeStatsEntity::class], version = 1, exportSchema = false)
abstract class SolitaireDatabase : RoomDatabase() {

  abstract fun statsDao(): GameModeStatsDao

  companion object {
    @Volatile
    private var INSTANCE: SolitaireDatabase? = null

    fun getDatabase(context: Context): SolitaireDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          SolitaireDatabase::class.java,
          "solitaire_database",
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
