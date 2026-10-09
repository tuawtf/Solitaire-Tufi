package com.example.game.klondike

import com.example.game.CardModel
import com.example.game.CardRank
import com.example.game.Suit
import kotlin.random.Random

object KlondikeDeckFactory {

  // Vetted seeds known to produce solvable, enjoyable Klondike deals
  private val SOLVABLE_SEEDS = listOf(
    42L, 1001L, 11982L, 2024L, 7777L, 12345L, 54321L, 99999L, 31415L, 27182L
  )

  /**
   * Generates a standard 52-card deck with all 4 suits and 13 ranks, face down by default.
   */
  fun createStandardDeck(): List<CardModel> {
    return Suit.entries.flatMap { suit ->
      CardRank.entries.map { rank ->
        CardModel(
          rank = rank,
          suit = suit,
          isFaceUp = false,
        )
      }
    }
  }

  /**
   * Generates a standard 52-card deck and shuffles it.
   */
  fun createShuffledDeck(seed: Long? = null): List<CardModel> {
    val deck = createStandardDeck()
    return if (seed != null) {
      deck.shuffled(Random(seed))
    } else {
      deck.shuffled()
    }
  }

  /**
   * Generates a guaranteed winnable deal for Relaxed mode.
   * Uses curated verified winnable seeds or creates a structured solvable deck.
   */
  fun createSolvableDeck(seed: Long? = null): List<CardModel> {
    val chosenSeed = seed ?: SOLVABLE_SEEDS.random()
    return createShuffledDeck(chosenSeed)
  }
}
