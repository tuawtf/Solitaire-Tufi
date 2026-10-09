package com.example.ui.navigation

sealed class Screen(val route: String) {
  data object Home : Screen("home")
  data object Game : Screen("game/{gameType}") {
    fun createRoute(gameType: String): String = "game/$gameType"
  }
}
