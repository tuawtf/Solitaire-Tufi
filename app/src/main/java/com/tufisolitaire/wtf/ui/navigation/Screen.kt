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
  data object Settings : Screen("settings")
  data object SettingsGameplay : Screen("settings/gameplay")
  data object SettingsSound : Screen("settings/sound")
  data object SettingsAppearance : Screen("settings/appearance")
  data object SettingsLanguage : Screen("settings/language")
  data object SettingsAbout : Screen("settings/about")
}
