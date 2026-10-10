package com.tufisolitaire.wtf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tufisolitaire.wtf.data.settings.AppSettings
import com.tufisolitaire.wtf.data.settings.SettingsRepository
import com.tufisolitaire.wtf.ui.navigation.AppNavigation
import com.tufisolitaire.wtf.ui.theme.SolitaireTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val settingsRepository = SettingsRepository(applicationContext)

    setContent {
      val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())

      SolitaireTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor,
      ) {
        AppNavigation()
      }
    }
  }
}
