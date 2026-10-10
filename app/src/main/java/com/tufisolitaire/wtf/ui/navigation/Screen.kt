package com.tufisolitaire.wtf.ui.navigation

sealed class Screen(val route: String) {
  data object Home : Screen("home")
  data object Game : Screen("game/{gameType}?diff={diff}&draw={draw}&resume={resume}") {
    fun createRoute(
      gameType: String,
      difficulty: String = "REGULAR",
      drawMode: String = "DRAW_ONE",
      resume: Boolean = false,
    ): String = "game/$gameType?diff=$difficulty&draw=$drawMode&resume=$resume"
  }
}
