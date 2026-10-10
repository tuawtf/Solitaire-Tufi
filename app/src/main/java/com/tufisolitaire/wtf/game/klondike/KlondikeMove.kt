package com.tufisolitaire.wtf.game.klondike

import com.tufisolitaire.wtf.game.Suit

/**
 * Represents discrete player moves for move logging and telemetry.
 */
sealed interface KlondikeMove {
  data class Draw(val count: Int) : KlondikeMove
  data object RecycleStock : KlondikeMove
  data class WasteToFoundation(val suit: Suit) : KlondikeMove
  data class WasteToTableau(val toColumn: Int) : KlondikeMove
  data class TableauToFoundation(val fromColumn: Int, val suit: Suit, val revealedCard: Boolean) : KlondikeMove
  data class TableauToTableau(
    val fromColumn: Int,
    val cardCount: Int,
    val toColumn: Int,
    val revealedCard: Boolean,
  ) : KlondikeMove
  data class FoundationToTableau(val fromSuit: Suit, val toColumn: Int) : KlondikeMove
}
