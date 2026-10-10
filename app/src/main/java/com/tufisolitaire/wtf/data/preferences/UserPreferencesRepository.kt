package com.tufisolitaire.wtf.data.preferences

import android.content.Context
import com.tufisolitaire.wtf.game.klondike.DifficultyMode
import com.tufisolitaire.wtf.game.klondike.DrawMode

class UserPreferencesRepository(context: Context) {

  private val prefs = context.getSharedPreferences("solitaire_user_preferences", Context.MODE_PRIVATE)

  fun getLastDifficulty(): DifficultyMode {
    val name = prefs.getString(KEY_DIFFICULTY, DifficultyMode.REGULAR.name) ?: DifficultyMode.REGULAR.name
    return try {
      DifficultyMode.valueOf(name)
    } catch (_: Exception) {
      DifficultyMode.REGULAR
    }
  }

  fun setLastDifficulty(difficulty: DifficultyMode) {
    prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
  }

  fun getLastDrawMode(): DrawMode {
    val name = prefs.getString(KEY_DRAW_MODE, DrawMode.DRAW_ONE.name) ?: DrawMode.DRAW_ONE.name
    return try {
      DrawMode.valueOf(name)
    } catch (_: Exception) {
      DrawMode.DRAW_ONE
    }
  }

  fun setLastDrawMode(drawMode: DrawMode) {
    prefs.edit().putString(KEY_DRAW_MODE, drawMode.name).apply()
  }

  companion object {
    private const val KEY_DIFFICULTY = "pref_klondike_difficulty"
    private const val KEY_DRAW_MODE = "pref_klondike_draw_mode"
  }
}
