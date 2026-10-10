package com.example.data.savedgame

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_game")
data class SavedGameEntity(
  @PrimaryKey val gameType: String = "KLONDIKE",
  val difficulty: String,
  val drawMode: String,
  val moveCount: Int,
  val elapsedSeconds: Long,
  val stateJson: String,
  val undoStackJson: String,
  val timestamp: Long = System.currentTimeMillis(),
)
