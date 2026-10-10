package com.tufisolitaire.wtf.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "solitaire_app_settings")

class SettingsRepository(private val context: Context) {

  private object Keys {
    val TAP_TO_PLACE = booleanPreferencesKey("tap_to_place")
    val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val SOUND_VOLUME = floatPreferencesKey("sound_volume")
    val CARD_SOUNDS = booleanPreferencesKey("card_sounds")
    val WIN_SOUND = booleanPreferencesKey("win_sound")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val HAPTICS_INVALID_MOVE = booleanPreferencesKey("haptics_invalid_move")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val CARD_BACK = stringPreferencesKey("card_back")
    val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
  }

  val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
    AppSettings(
      tapToPlace = prefs[Keys.TAP_TO_PLACE] ?: false,
      keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
      soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
      soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.7f,
      cardSounds = prefs[Keys.CARD_SOUNDS] ?: true,
      winSound = prefs[Keys.WIN_SOUND] ?: true,
      hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
      hapticsInvalidMove = prefs[Keys.HAPTICS_INVALID_MOVE] ?: true,
      themeMode = try {
        ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
      } catch (_: Exception) {
        ThemeMode.SYSTEM
      },
      dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
      cardBack = try {
        CardBackStyle.valueOf(prefs[Keys.CARD_BACK] ?: CardBackStyle.PRIMARY.name)
      } catch (_: Exception) {
        CardBackStyle.PRIMARY
      },
      animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true,
    )
  }

  suspend fun setTapToPlace(enabled: Boolean) {
    context.dataStore.edit { it[Keys.TAP_TO_PLACE] = enabled }
  }

  suspend fun setKeepScreenOn(enabled: Boolean) {
    context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
  }

  suspend fun setSoundVolume(volume: Float) {
    context.dataStore.edit { it[Keys.SOUND_VOLUME] = volume.coerceIn(0f, 1f) }
  }

  suspend fun setCardSounds(enabled: Boolean) {
    context.dataStore.edit { it[Keys.CARD_SOUNDS] = enabled }
  }

  suspend fun setWinSound(enabled: Boolean) {
    context.dataStore.edit { it[Keys.WIN_SOUND] = enabled }
  }

  suspend fun setHapticsEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
  }

  suspend fun setHapticsInvalidMove(enabled: Boolean) {
    context.dataStore.edit { it[Keys.HAPTICS_INVALID_MOVE] = enabled }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
  }

  suspend fun setDynamicColor(enabled: Boolean) {
    context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
  }

  suspend fun setCardBack(style: CardBackStyle) {
    context.dataStore.edit { it[Keys.CARD_BACK] = style.name }
  }

  suspend fun setAnimationsEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.ANIMATIONS_ENABLED] = enabled }
  }
}
