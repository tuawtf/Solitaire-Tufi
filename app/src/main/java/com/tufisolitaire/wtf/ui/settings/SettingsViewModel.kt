package com.tufisolitaire.wtf.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tufisolitaire.wtf.data.settings.AppSettings
import com.tufisolitaire.wtf.data.settings.CardBackStyle
import com.tufisolitaire.wtf.data.settings.SettingsRepository
import com.tufisolitaire.wtf.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = SettingsRepository(application)

  val settings: StateFlow<AppSettings> = repository.settingsFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AppSettings(),
    )

  fun setTapToPlace(enabled: Boolean) {
    viewModelScope.launch { repository.setTapToPlace(enabled) }
  }

  fun setKeepScreenOn(enabled: Boolean) {
    viewModelScope.launch { repository.setKeepScreenOn(enabled) }
  }

  fun setSoundEnabled(enabled: Boolean) {
    viewModelScope.launch { repository.setSoundEnabled(enabled) }
  }

  fun setSoundVolume(volume: Float) {
    viewModelScope.launch { repository.setSoundVolume(volume) }
  }

  fun setCardSounds(enabled: Boolean) {
    viewModelScope.launch { repository.setCardSounds(enabled) }
  }

  fun setWinSound(enabled: Boolean) {
    viewModelScope.launch { repository.setWinSound(enabled) }
  }

  fun setHapticsEnabled(enabled: Boolean) {
    viewModelScope.launch { repository.setHapticsEnabled(enabled) }
  }

  fun setHapticsInvalidMove(enabled: Boolean) {
    viewModelScope.launch { repository.setHapticsInvalidMove(enabled) }
  }

  fun setThemeMode(mode: ThemeMode) {
    viewModelScope.launch { repository.setThemeMode(mode) }
  }

  fun setDynamicColor(enabled: Boolean) {
    viewModelScope.launch { repository.setDynamicColor(enabled) }
  }

  fun setCardBack(style: CardBackStyle) {
    viewModelScope.launch { repository.setCardBack(style) }
  }

  fun setAnimationsEnabled(enabled: Boolean) {
    viewModelScope.launch { repository.setAnimationsEnabled(enabled) }
  }

  fun resetStats() {
    viewModelScope.launch {
      val database = com.tufisolitaire.wtf.data.stats.SolitaireDatabase.getDatabase(getApplication())
      val statsRepository = com.tufisolitaire.wtf.data.stats.StatsRepository(database.statsDao())
      statsRepository.resetAllStats()
    }
  }
}
