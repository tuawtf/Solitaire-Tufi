package com.tufisolitaire.wtf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tufisolitaire.wtf.ui.navigation.AppNavigation
import com.tufisolitaire.wtf.ui.theme.SolitaireTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SolitaireTheme {
        AppNavigation()
      }
    }
  }
}
