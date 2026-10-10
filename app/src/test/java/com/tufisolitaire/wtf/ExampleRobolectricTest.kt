package com.tufisolitaire.wtf

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tufisolitaire.wtf.data.stats.SolitaireDatabase
import com.tufisolitaire.wtf.data.stats.StatsRepository
import com.tufisolitaire.wtf.game.GameType
import com.tufisolitaire.wtf.game.klondike.DifficultyMode
import com.tufisolitaire.wtf.game.klondike.DrawMode
import com.tufisolitaire.wtf.ui.game.klondike.KlondikeViewModel
import com.tufisolitaire.wtf.ui.home.HomeViewModel
import com.tufisolitaire.wtf.ui.settings.SettingsViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Solitaire", appName)
  }

  @Test
  fun `homeViewModel initial state and tab selection`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = HomeViewModel(app)
    val initialState = viewModel.uiState.value

    assertEquals(GameType.KLONDIKE, initialState.selectedGameType)

    viewModel.selectGameType(GameType.SPIDER)
    assertEquals(GameType.SPIDER, viewModel.uiState.value.selectedGameType)
  }

  @Test
  fun `settingsViewModel manages tap_to_place default false`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SettingsViewModel(app)
    val settings = viewModel.settings.value

    assertFalse("tapToPlace must default to false", settings.tapToPlace)
  }

  @Test
  fun `klondikeViewModel draws card and updates undo state`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KlondikeViewModel(app)
    val stateBefore = viewModel.gameState.value

    assertEquals(24, stateBefore.stock.size)
    assertFalse(viewModel.canUndo.value)

    val drewCard = viewModel.drawFromStock()
    assertTrue(drewCard)

    val stateAfter = viewModel.gameState.value
    assertEquals(23, stateAfter.stock.size)
    assertEquals(1, stateAfter.waste.size)
    assertTrue(viewModel.canUndo.value)

    val undone = viewModel.undo()
    assertTrue(undone)
    assertEquals(24, viewModel.gameState.value.stock.size)
    assertEquals(0, viewModel.gameState.value.waste.size)
  }

  @Test
  fun `statsRepository records game play and victory`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dao = SolitaireDatabase.getDatabase(context).statsDao()
    val repo = StatsRepository(dao)

    repo.recordGamePlayed("KLONDIKE", DifficultyMode.HARD, DrawMode.DRAW_THREE)
    val isNewRecord = repo.recordGameWon(
      gameType = "KLONDIKE",
      difficulty = DifficultyMode.HARD,
      drawMode = DrawMode.DRAW_THREE,
      score = 850,
      timeSeconds = 120,
      moves = 65,
    )

    assertTrue(isNewRecord)
    val directStats = dao.getStatsDirect("KLONDIKE_HARD_DRAW_THREE")
    assertEquals(1, directStats?.gamesWon)
    assertEquals(850, directStats?.bestScore)
    assertEquals(120L, directStats?.bestTimeSeconds)
  }
}
