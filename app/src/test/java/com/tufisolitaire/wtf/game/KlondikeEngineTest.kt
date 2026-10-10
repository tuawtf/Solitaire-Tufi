package com.tufisolitaire.wtf.game

import com.tufisolitaire.wtf.game.klondike.DrawMode
import com.tufisolitaire.wtf.game.klondike.KlondikeDeckFactory
import com.tufisolitaire.wtf.game.klondike.KlondikeEngine
import com.tufisolitaire.wtf.game.klondike.KlondikeGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KlondikeEngineTest {

  @Test
  fun testInitialBoardSetup() {
    val engine = KlondikeEngine(DrawMode.DRAW_ONE)
    val state = engine.currentState

    // Check tableau structure: 7 columns with 1..7 cards
    assertEquals(7, state.tableau.size)
    for (i in 0..6) {
      val col = state.tableau[i]
      assertEquals("Column $i should have ${i + 1} cards", i + 1, col.size)
      // Check face up / down statuses
      for (cardIdx in 0 until col.size - 1) {
        assertFalse("Column $i card $cardIdx should be face down", col[cardIdx].isFaceUp)
      }
      assertTrue("Column $i top card should be face up", col.last().isFaceUp)
    }

    // Tableau total cards = 1 + 2 + 3 + 4 + 5 + 6 + 7 = 28 cards
    val tableauCardCount = state.tableau.sumOf { it.size }
    assertEquals(28, tableauCardCount)

    // Stock should have 52 - 28 = 24 cards, all face down
    assertEquals(24, state.stock.size)
    assertTrue(state.stock.all { !it.isFaceUp })

    // Waste and foundations should initially be empty
    assertTrue(state.waste.isEmpty())
    assertEquals(4, state.foundations.size)
    assertTrue(state.foundations.values.all { it.isEmpty() })

    assertEquals(0, state.moveCount)
    assertEquals(0L, state.elapsedSeconds)
    assertFalse(state.isWon)
  }

  @Test
  fun testDrawOneFromStockAndRecycle() {
    val engine = KlondikeEngine(DrawMode.DRAW_ONE)
    val initialStockCount = engine.currentState.stock.size

    // Draw 1 card
    val drawSuccess = engine.drawFromStock()
    assertTrue(drawSuccess)
    assertEquals(initialStockCount - 1, engine.currentState.stock.size)
    assertEquals(1, engine.currentState.waste.size)
    assertTrue(engine.currentState.waste.last().isFaceUp)
    assertEquals(1, engine.currentState.moveCount)

    // Draw remaining 23 cards from stock
    for (i in 1 until initialStockCount) {
      engine.drawFromStock()
    }
    assertEquals(0, engine.currentState.stock.size)
    assertEquals(24, engine.currentState.waste.size)

    // Next draw triggers stock recycling from waste
    val recycleSuccess = engine.drawFromStock()
    assertTrue(recycleSuccess)
    assertEquals(24, engine.currentState.stock.size)
    assertTrue(engine.currentState.waste.isEmpty())
    // Recycled stock cards must all be face down
    assertTrue(engine.currentState.stock.all { !it.isFaceUp })
    assertEquals(initialStockCount + 1, engine.currentState.moveCount)
  }

  @Test
  fun testDrawThreeFromStock() {
    val engine = KlondikeEngine(DrawMode.DRAW_THREE)
    val initialStockCount = engine.currentState.stock.size // 24

    val drawSuccess = engine.drawFromStock()
    assertTrue(drawSuccess)
    assertEquals(initialStockCount - 3, engine.currentState.stock.size)
    assertEquals(3, engine.currentState.waste.size)
    assertTrue(engine.currentState.waste.all { it.isFaceUp })
  }

  @Test
  fun testCardStackingRules() {
    val blackKing = CardModel(CardRank.KING, Suit.SPADES, isFaceUp = true)
    val redQueen = CardModel(CardRank.QUEEN, Suit.HEARTS, isFaceUp = true)
    val blackJack = CardModel(CardRank.JACK, Suit.CLUBS, isFaceUp = true)
    val redJack = CardModel(CardRank.JACK, Suit.DIAMONDS, isFaceUp = true)

    // Tableau rules: alternating color, descending rank
    assertTrue("Red Queen should stack on Black King", redQueen.canStackOnTableau(blackKing))
    assertFalse("Red Jack should NOT stack on Black King", redJack.canStackOnTableau(blackKing))
    assertFalse("Black Jack should NOT stack on Black King", blackJack.canStackOnTableau(blackKing))
    assertTrue("Black Jack should stack on Red Queen", blackJack.canStackOnTableau(redQueen))

    // Empty tableau column allows ONLY King
    assertTrue("King can be placed on empty tableau column", blackKing.canStackOnTableau(null))
    assertFalse("Queen CANNOT be placed on empty tableau column", redQueen.canStackOnTableau(null))
    assertFalse("Ace CANNOT be placed on empty tableau column", CardModel(CardRank.ACE, Suit.HEARTS, true).canStackOnTableau(null))

    // Foundation rules: same suit, ascending from Ace
    val heartAce = CardModel(CardRank.ACE, Suit.HEARTS, isFaceUp = true)
    val spadeAce = CardModel(CardRank.ACE, Suit.SPADES, isFaceUp = true)
    val heartTwo = CardModel(CardRank.TWO, Suit.HEARTS, isFaceUp = true)
    val heartThree = CardModel(CardRank.THREE, Suit.HEARTS, isFaceUp = true)

    assertTrue("Heart Ace can start empty Heart foundation", heartAce.canStackOnFoundation(null))
    assertFalse("Heart Two CANNOT start empty foundation", heartTwo.canStackOnFoundation(null))
    assertTrue("Heart Two can stack on Heart Ace", heartTwo.canStackOnFoundation(heartAce))
    assertFalse("Spade Ace cannot stack on Heart Ace", spadeAce.canStackOnFoundation(heartAce))
    assertFalse("Heart Three cannot stack on Heart Ace", heartThree.canStackOnFoundation(heartAce))
  }

  @Test
  fun testTableauToTableauMoveAndCardReveal() {
    // Construct a deterministic test setup
    val redEight = CardModel(CardRank.EIGHT, Suit.HEARTS, isFaceUp = true)
    val blackSeven = CardModel(CardRank.SEVEN, Suit.SPADES, isFaceUp = true)
    val blackNine = CardModel(CardRank.NINE, Suit.CLUBS, isFaceUp = true)
    val facedownCard = CardModel(CardRank.TWO, Suit.DIAMONDS, isFaceUp = false)

    val customDeck = KlondikeDeckFactory.createStandardDeck().toMutableList()
    val engine = KlondikeEngine()

    // Manually test moving a stack of cards between tableaus
    // Col 0: [blackNine]
    // Col 1: [facedownCard, redEight, blackSeven]
    val col0 = listOf(blackNine)
    val col1 = listOf(facedownCard, redEight, blackSeven)

    val updatedTableau = engine.currentState.tableau.toMutableList()
    updatedTableau[0] = col0
    updatedTableau[1] = col1

    val customState = engine.currentState.copy(tableau = updatedTableau)
    // Directly replace state via reflection or testing methods
    engine.startNewGame(customDeck = customDeck)

    // Test moving tableau cards:
    // Move redEight and blackSeven from Col 1 to Col 0 on top of blackNine
    // Let's create an engine with a controlled board
    val riggedEngine = createCustomEngine(
      tableau = listOf(
        listOf(blackNine),
        listOf(facedownCard, redEight, blackSeven),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
      )
    )

    // Move stack starting at index 1 (redEight) from col 1 to col 0
    val moveSuccess = riggedEngine.moveTableauToTableau(fromColumn = 1, cardIndex = 1, toColumn = 0)
    assertTrue("Moving red 8 stack to black 9 should succeed", moveSuccess)

    val finalState = riggedEngine.currentState
    assertEquals(3, finalState.tableau[0].size)
    assertEquals(blackNine, finalState.tableau[0][0])
    assertEquals(redEight, finalState.tableau[0][1])
    assertEquals(blackSeven, finalState.tableau[0][2])

    // In Col 1, facedownCard should now be revealed (face up)!
    assertEquals(1, finalState.tableau[1].size)
    assertTrue("The uncovered card in column 1 should be flipped face up", finalState.tableau[1][0].isFaceUp)
    assertEquals(CardRank.TWO, finalState.tableau[1][0].rank)
  }

  @Test
  fun testTableauToFoundationAndWasteToFoundation() {
    val heartAce = CardModel(CardRank.ACE, Suit.HEARTS, isFaceUp = true)
    val heartTwo = CardModel(CardRank.TWO, Suit.HEARTS, isFaceUp = true)

    val engine = createCustomEngine(
      tableau = listOf(
        listOf(heartAce),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
      ),
      waste = listOf(heartTwo)
    )

    // Move Ace from tableau col 0 to foundation
    val aceSuccess = engine.moveTableauToFoundation(0)
    assertTrue(aceSuccess)
    assertEquals(1, engine.currentState.foundations[Suit.HEARTS]?.size)
    assertEquals(heartAce, engine.currentState.foundations[Suit.HEARTS]?.last())
    assertTrue(engine.currentState.tableau[0].isEmpty())

    // Move Two from waste to foundation
    val twoSuccess = engine.moveWasteToFoundation()
    assertTrue(twoSuccess)
    assertEquals(2, engine.currentState.foundations[Suit.HEARTS]?.size)
    assertEquals(heartTwo, engine.currentState.foundations[Suit.HEARTS]?.last())
    assertTrue(engine.currentState.waste.isEmpty())
  }

  @Test
  fun testUndoHistory() {
    val heartAce = CardModel(CardRank.ACE, Suit.HEARTS, isFaceUp = true)
    val engine = createCustomEngine(
      tableau = listOf(
        listOf(heartAce),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
      ),
      stock = listOf(CardModel(CardRank.FIVE, Suit.CLUBS, false))
    )

    assertFalse("Initial state should not be undoable", engine.canUndo())

    // Perform a move
    engine.moveTableauToFoundation(0)
    assertTrue("State should be undoable after move", engine.canUndo())
    assertEquals(1, engine.currentState.moveCount)
    assertTrue(engine.currentState.tableau[0].isEmpty())

    // Undo the move
    val undoResult = engine.undo()
    assertTrue(undoResult)
    assertEquals(0, engine.currentState.moveCount)
    assertEquals(1, engine.currentState.tableau[0].size)
    assertEquals(heartAce, engine.currentState.tableau[0][0])
    assertTrue(engine.currentState.foundations[Suit.HEARTS]!!.isEmpty())
    assertFalse("Undo stack should now be empty", engine.canUndo())
  }

  @Test
  fun testWinCondition() {
    val completedFoundations = Suit.entries.associateWith { suit ->
      CardRank.entries.map { rank -> CardModel(rank, suit, isFaceUp = true) }
    }

    val engine = createCustomEngine(
      foundations = completedFoundations,
      tableau = List(7) { emptyList() },
      stock = emptyList(),
      waste = emptyList()
    )

    // Trigger state check
    assertTrue("Game should be recognized as won when all foundations are full", engine.currentState.calculateIsWon())
  }

  @Test
  fun testNoMovesRemainingDetection() {
    // Board with no cards in stock, waste, or tableau
    val deadEngine = createCustomEngine(
      stock = emptyList(),
      waste = emptyList(),
      tableau = List(7) { emptyList() }
    )

    // Empty board is considered finished/no moves
    assertTrue(deadEngine.detectNoMovesRemaining(deadEngine.currentState))

    // Board where stock has cards is NEVER stuck (can always draw)
    val activeStockEngine = createCustomEngine(
      stock = listOf(CardModel(CardRank.NINE, Suit.SPADES, false)),
      waste = emptyList(),
      tableau = List(7) { emptyList() }
    )
    assertFalse("Game with stock cards should have moves remaining", activeStockEngine.detectNoMovesRemaining(activeStockEngine.currentState))
  }

  @Test
  fun testTimerTick() {
    val engine = KlondikeEngine()
    assertEquals(0L, engine.currentState.elapsedSeconds)

    engine.tickTimer(1)
    assertEquals(1L, engine.currentState.elapsedSeconds)

    engine.tickTimer(5)
    assertEquals(6L, engine.currentState.elapsedSeconds)
  }

  @Test
  fun testHardModeUndoLimit() {
    val engine = KlondikeEngine(initialDifficulty = com.tufisolitaire.wtf.game.klondike.DifficultyMode.HARD)
    assertEquals(3, engine.currentState.undosRemaining)

    // Make 4 moves
    for (i in 0 until 4) {
      engine.drawFromStock()
    }

    // 1st Undo
    assertTrue(engine.undo())
    assertEquals(2, engine.currentState.undosRemaining)

    // 2nd Undo
    assertTrue(engine.undo())
    assertEquals(1, engine.currentState.undosRemaining)

    // 3rd Undo
    assertTrue(engine.undo())
    assertEquals(0, engine.currentState.undosRemaining)

    // 4th Undo should fail because limit is reached
    assertFalse(engine.undo())
    assertFalse(engine.canUndo())
  }

  @Test
  fun testRelaxedModeHintsAndSolvableDeck() {
    val engine = KlondikeEngine(initialDifficulty = com.tufisolitaire.wtf.game.klondike.DifficultyMode.RELAXED)
    assertTrue(engine.currentState.difficulty.hasHints)
    assertNull(engine.currentState.undosRemaining) // unlimited

    val hint = engine.findHint()
    assertNotNull("Relaxed mode should be able to produce a hint", hint)
  }

  @Test
  fun testScoreCalculationWithDifficultyMultiplier() {
    val ace = CardModel(CardRank.ACE, Suit.HEARTS, isFaceUp = true)
    val engine = createCustomEngine(
      waste = listOf(ace)
    )
    engine.moveWasteToFoundation()
    // Base score: 15
    assertEquals(15, engine.currentState.baseScore)

    // In Regular mode (1.5x multiplier), score = 15 * 1.5 = 23
    assertEquals(23, engine.currentState.calculatedScore)
  }

  // --- Helper to build specific test state ---
  private fun createCustomEngine(
    tableau: List<List<CardModel>> = List(7) { emptyList() },
    stock: List<CardModel> = emptyList(),
    waste: List<CardModel> = emptyList(),
    foundations: Map<Suit, List<CardModel>> = Suit.entries.associateWith { emptyList() },
  ): KlondikeEngine {
    val engine = KlondikeEngine()
    // Inject custom board state
    val customState = KlondikeGameState(
      tableau = tableau,
      stock = stock,
      waste = waste,
      foundations = foundations,
      drawMode = DrawMode.DRAW_ONE,
      moveCount = 0,
      elapsedSeconds = 0L,
      cardsPlayedSinceStockRecycle = 0,
      isWon = foundations.values.all { it.size == 13 },
      hasNoMovesRemaining = false,
    )
    // Use reflection to set _state or reset
    val stateField = KlondikeEngine::class.java.getDeclaredField("_state")
    stateField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val flow = stateField.get(engine) as kotlinx.coroutines.flow.MutableStateFlow<KlondikeGameState>
    flow.value = customState.copy(
      hasNoMovesRemaining = engine.detectNoMovesRemaining(customState)
    )
    return engine
  }
}
