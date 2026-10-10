package com.tufisolitaire.wtf.game

/**
 * Solitaire suits with standard playing card symbols.
 */
enum class Suit(val symbol: String) {
  SPADES("♠"),
  HEARTS("♥"),
  DIAMONDS("♦"),
  CLUBS("♣");

  val isRed: Boolean
    get() = this == HEARTS || this == DIAMONDS
}

/**
 * Standard playing card ranks from Ace to King.
 */
enum class CardRank(val value: Int, val label: String) {
  ACE(1, "A"),
  TWO(2, "2"),
  THREE(3, "3"),
  FOUR(4, "4"),
  FIVE(5, "5"),
  SIX(6, "6"),
  SEVEN(7, "7"),
  EIGHT(8, "8"),
  NINE(9, "9"),
  TEN(10, "10"),
  JACK(11, "J"),
  QUEEN(12, "Q"),
  KING(13, "K"),
}

/**
 * Immutable Card model for Solitaire game logic.
 */
data class CardModel(
  val rank: CardRank,
  val suit: Suit,
  val isFaceUp: Boolean = false,
  val id: String = "${rank.name}_${suit.name}",
) {
  /**
   * Determines whether this card can be placed on top of [parentCard] in a tableau column.
   * If parentCard is null, only a KING can be placed on an empty column.
   * Otherwise, must alternate color and be exactly 1 rank lower.
   */
  fun canStackOnTableau(parentCard: CardModel?): Boolean {
    return if (parentCard == null) {
      rank == CardRank.KING
    } else {
      parentCard.isFaceUp &&
        parentCard.suit.isRed != this.suit.isRed &&
        parentCard.rank.value == this.rank.value + 1
    }
  }

  /**
   * Determines whether this card can be placed on top of [foundationTop] in a foundation pile.
   * If foundationTop is null, only an ACE can start the pile.
   * Otherwise, must match the suit and be exactly 1 rank higher.
   */
  fun canStackOnFoundation(foundationTop: CardModel?): Boolean {
    return if (foundationTop == null) {
      rank == CardRank.ACE
    } else {
      foundationTop.suit == this.suit &&
        this.rank.value == foundationTop.rank.value + 1
    }
  }
}

/**
 * Architectural interface separating game engine from UI layer.
 */
interface GameEngine {
  val gameType: GameType
  fun startNewGame()
  fun restartGame()
}
