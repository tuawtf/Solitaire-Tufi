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
import com.example.game.klondike.DifficultyMode
import com.example.game.klondike.DrawMode
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
        onStartNewGame = { gameType, difficulty, drawMode ->
          navController.navigate(
            Screen.Game.createRoute(
              gameType = gameType.name,
              difficulty = difficulty.name,
              drawMode = drawMode.name,
              resume = false,
            ),
          )
        },
        onResumeGame = { gameType ->
          navController.navigate(
            Screen.Game.createRoute(
              gameType = gameType.name,
              resume = true,
            ),
          )
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
        navArgument("diff") {
          type = NavType.StringType
          defaultValue = "REGULAR"
        },
        navArgument("draw") {
          type = NavType.StringType
          defaultValue = "DRAW_ONE"
        },
        navArgument("resume") {
          type = NavType.BoolType
          defaultValue = false
        },
      ),
    ) { backStackEntry ->
      val gameTypeKey = backStackEntry.arguments?.getString("gameType") ?: "KLONDIKE"
      val diffKey = backStackEntry.arguments?.getString("diff") ?: "REGULAR"
      val drawKey = backStackEntry.arguments?.getString("draw") ?: "DRAW_ONE"
      val resumeFlag = backStackEntry.arguments?.getBoolean("resume") ?: false

      if (gameTypeKey.equals(GameType.KLONDIKE.name, ignoreCase = true)) {
        val difficulty = try {
          DifficultyMode.valueOf(diffKey)
        } catch (_: Exception) {
          DifficultyMode.REGULAR
        }
        val drawMode = try {
          DrawMode.valueOf(drawKey)
        } catch (_: Exception) {
          DrawMode.DRAW_ONE
        }

        val klondikeViewModel: KlondikeViewModel = viewModel()
        KlondikeGameRoute(
          viewModel = klondikeViewModel,
          difficulty = difficulty,
          drawMode = drawMode,
          resume = resumeFlag,
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
