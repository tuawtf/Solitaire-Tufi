package com.example.game.klondike

import com.example.game.CardModel
import com.example.game.Suit
import kotlin.math.roundToInt

/**
 * Immutable snapshot of the Klondike Solitaire board state.
 */
data class KlondikeGameState(
  val stock: List<CardModel> = emptyList(),
  val waste: List<CardModel> = emptyList(),
  val foundations: Map<Suit, List<CardModel>> = Suit.entries.associateWith { emptyList() },
  val tableau: List<List<CardModel>> = List(7) { emptyList() },
  val drawMode: DrawMode = DrawMode.DRAW_ONE,
  val difficulty: DifficultyMode = DifficultyMode.REGULAR,
  val moveCount: Int = 0,
  val elapsedSeconds: Long = 0L,
  val baseScore: Int = 0,
  val undosUsed: Int = 0,
  val cardsPlayedSinceStockRecycle: Int = 0,
  val isWon: Boolean = false,
  val hasNoMovesRemaining: Boolean = false,
) {
  val totalFoundationCards: Int
    get() = foundations.values.sumOf { it.size }

  val topWasteCard: CardModel?
    get() = waste.lastOrNull()

  fun topTableauCard(columnIndex: Int): CardModel? {
    if (columnIndex !in tableau.indices) return null
    return tableau[columnIndex].lastOrNull()
  }

  fun topFoundationCard(suit: Suit): CardModel? {
    return foundations[suit]?.lastOrNull()
  }

  val undosRemaining: Int?
    get() = difficulty.maxUndos?.let { max -> maxOf(0, max - undosUsed) }

  val canUseUndo: Boolean
    get() = undosRemaining == null || undosRemaining!! > 0

  val calculatedScore: Int
    get() {
      val timePenalty = (elapsedSeconds * 1.2).toInt()
      val timeBonus = if (isWon) maxOf(0, 1000 - timePenalty) else 0
      val moveBonus = if (isWon) maxOf(0, 500 - moveCount * 3) else 0
      val rawScore = maxOf(0, baseScore + timeBonus + moveBonus)
      return (rawScore * difficulty.multiplier).roundToInt()
    }

  /**
   * Auto-complete is possible when all remaining cards in the tableau are face-up
   * and game is not yet won.
   */
  val canAutoComplete: Boolean
    get() {
      if (isWon) return false
      val allTableauFaceUp = tableau.all { col -> col.all { it.isFaceUp } }
      return allTableauFaceUp && (stock.isEmpty() || totalFoundationCards >= 20)
    }

  /**
   * Checks if all 52 cards are correctly placed into foundations.
   */
  fun calculateIsWon(): Boolean {
    return foundations.values.all { it.size == 13 }
  }
}
