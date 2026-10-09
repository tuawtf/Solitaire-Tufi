package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.game.GameType
import com.example.ui.game.GamePlaceholderScreen
import com.example.ui.game.klondike.KlondikeGameRoute
import com.example.ui.game.klondike.KlondikeViewModel
import com.example.ui.home.HomeRoute
import com.example.ui.home.HomeViewModel

@Composable
fun AppNavigation(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {
  val homeViewModel: HomeViewModel = viewModel()

  NavHost(
    navController = navController,
    startDestination = Screen.Home.route,
    modifier = modifier,
  ) {
    composable(Screen.Home.route) {
      HomeRoute(
        viewModel = homeViewModel,
        onStartGame = { gameType ->
          navController.navigate(Screen.Game.createRoute(gameType.name))
        },
      )
    }

    composable(
      route = Screen.Game.route,
      arguments = listOf(
        navArgument("gameType") {
          type = NavType.StringType
          defaultValue = "KLONDIKE"
        },
      ),
    ) { backStackEntry ->
      val gameTypeKey = backStackEntry.arguments?.getString("gameType") ?: "KLONDIKE"

      if (gameTypeKey.equals(GameType.KLONDIKE.name, ignoreCase = true)) {
        val klondikeViewModel: KlondikeViewModel = viewModel()
        KlondikeGameRoute(
          viewModel = klondikeViewModel,
          onNavigateBack = {
            navController.popBackStack()
          },
        )
      } else {
        GamePlaceholderScreen(
          gameTypeKey = gameTypeKey,
          onNavigateBack = {
            navController.popBackStack()
          },
        )
      }
    }
  }
}
