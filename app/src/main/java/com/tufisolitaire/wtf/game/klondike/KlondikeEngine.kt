package com.tufisolitaire.wtf.game.klondike

import com.tufisolitaire.wtf.game.CardModel
import com.tufisolitaire.wtf.game.CardRank
import com.tufisolitaire.wtf.game.GameEngine
import com.tufisolitaire.wtf.game.GameType
import com.tufisolitaire.wtf.game.Suit
import java.util.ArrayDeque
import kotlin.math.min
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HintInfo(
  val message: String,
  val card: CardModel?,
)

/**
 * Pure Kotlin Klondike Solitaire Engine with Difficulty Modes, Scoring,
 * Hints, and Auto-Completion.
 */
class KlondikeEngine(
  private val initialDifficulty: DifficultyMode = DifficultyMode.REGULAR,
  private val initialDrawMode: DrawMode = initialDifficulty.defaultDrawMode,
) : GameEngine {

  constructor(initialDrawMode: DrawMode) : this(
    initialDifficulty = DifficultyMode.REGULAR,
    initialDrawMode = initialDrawMode,
  )

  override val gameType: GameType = GameType.KLONDIKE

  private val _state = MutableStateFlow(
    KlondikeGameState(
      difficulty = initialDifficulty,
      drawMode = initialDrawMode,
    ),
  )
  val state: StateFlow<KlondikeGameState> = _state.asStateFlow()

  val currentState: KlondikeGameState
    get() = _state.value

  private val undoStack = ArrayDeque<KlondikeGameState>()
  private val moveHistory = mutableListOf<KlondikeMove>()

  init {
    startNewGame()
  }

  fun startNewGame(
    difficulty: DifficultyMode = currentState.difficulty,
    drawMode: DrawMode = difficulty.defaultDrawMode,
    seed: Long? = null,
    customDeck: List<CardModel>? = null,
  ) {
    undoStack.clear()
    moveHistory.clear()

    val deck = when {
      customDeck != null -> customDeck
      difficulty == DifficultyMode.RELAXED -> KlondikeDeckFactory.createSolvableDeck(seed)
      else -> KlondikeDeckFactory.createShuffledDeck(seed)
    }
    require(deck.size == 52) { "A standard deck must contain exactly 52 cards." }

    var cardIndex = 0

    val newTableau = (0..6).map { colIndex ->
      val count = colIndex + 1
      val columnCards = deck.subList(cardIndex, cardIndex + count).mapIndexed { idx, card ->
        card.copy(isFaceUp = idx == count - 1)
      }
      cardIndex += count
      columnCards
    }

    val newStock = deck.subList(cardIndex, deck.size).map { it.copy(isFaceUp = false) }
    val initialFoundations = Suit.entries.associateWith { emptyList<CardModel>() }

    val initialState = KlondikeGameState(
      stock = newStock,
      waste = emptyList(),
      foundations = initialFoundations,
      tableau = newTableau,
      drawMode = drawMode,
      difficulty = difficulty,
      moveCount = 0,
      elapsedSeconds = 0L,
      baseScore = 0,
      undosUsed = 0,
      cardsPlayedSinceStockRecycle = 0,
      isWon = false,
      hasNoMovesRemaining = false,
    )

    _state.value = initialState.copy(
      hasNoMovesRemaining = detectNoMovesRemaining(initialState),
    )
  }

  override fun startNewGame() {
    startNewGame(difficulty = currentState.difficulty, drawMode = currentState.drawMode)
  }

  override fun restartGame() {
    startNewGame(difficulty = currentState.difficulty, drawMode = currentState.drawMode)
  }

  fun setDifficulty(difficulty: DifficultyMode) {
    startNewGame(difficulty = difficulty, drawMode = difficulty.defaultDrawMode)
  }

  fun setDrawMode(drawMode: DrawMode) {
    if (currentState.drawMode != drawMode) {
      startNewGame(difficulty = currentState.difficulty, drawMode = drawMode)
    }
  }

  fun tickTimer(deltaSeconds: Long = 1L) {
    if (!currentState.isWon && currentState.elapsedSeconds + deltaSeconds >= 0) {
      _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + deltaSeconds) }
    }
  }

  fun getUndoHistory(): List<KlondikeGameState> = undoStack.toList()

  fun restoreState(savedState: KlondikeGameState, savedUndoStack: List<KlondikeGameState>) {
    undoStack.clear()
    undoStack.addAll(savedUndoStack)
    moveHistory.clear()
    _state.value = savedState
  }

  fun canUndo(): Boolean {
    return undoStack.isNotEmpty() && currentState.canUseUndo
  }

  fun undo(): Boolean {
    if (!canUndo()) return false
    val previousState = undoStack.removeLast()
    if (moveHistory.isNotEmpty()) {
      moveHistory.removeAt(moveHistory.lastIndex)
    }
    _state.value = previousState.copy(
      undosUsed = currentState.undosUsed + 1,
    )
    return true
  }

  fun drawFromStock(): Boolean {
    val current = currentState
    if (current.stock.isEmpty() && current.waste.isEmpty()) return false

    pushUndoState()

    if (current.stock.isNotEmpty()) {
      val drawCount = min(current.drawMode.count, current.stock.size)
      val drawnCards = current.stock.takeLast(drawCount).map { it.copy(isFaceUp = true) }
      val updatedStock = current.stock.dropLast(drawCount)
      val updatedWaste = current.waste + drawnCards

      commitMove(
        move = KlondikeMove.Draw(drawCount),
        newState = current.copy(
          stock = updatedStock,
          waste = updatedWaste,
          moveCount = current.moveCount + 1,
        ),
      )
      return true
    } else {
      val recycledStock = current.waste.reversed().map { it.copy(isFaceUp = false) }
      val penalty = if (current.difficulty == DifficultyMode.HARD) -20 else 0
      commitMove(
        move = KlondikeMove.RecycleStock,
        newState = current.copy(
          stock = recycledStock,
          waste = emptyList(),
          cardsPlayedSinceStockRecycle = 0,
          baseScore = maxOf(0, current.baseScore + penalty),
          moveCount = current.moveCount + 1,
        ),
      )
      return true
    }
  }

  fun moveWasteToFoundation(): Boolean {
    val current = currentState
    val card = current.topWasteCard ?: return false

    val foundationPile = current.foundations[card.suit] ?: emptyList()
    val foundationTop = foundationPile.lastOrNull()

    if (!card.canStackOnFoundation(foundationTop)) return false

    pushUndoState()

    val updatedWaste = current.waste.dropLast(1)
    val updatedFoundations = current.foundations.toMutableMap()
    updatedFoundations[card.suit] = foundationPile + card

    commitMove(
      move = KlondikeMove.WasteToFoundation(card.suit),
      newState = current.copy(
        waste = updatedWaste,
        foundations = updatedFoundations,
        baseScore = current.baseScore + 15,
        cardsPlayedSinceStockRecycle = current.cardsPlayedSinceStockRecycle + 1,
        moveCount = current.moveCount + 1,
      ),
    )
    return true
  }

  fun moveWasteToTableau(toColumn: Int): Boolean {
    val current = currentState
    if (toColumn !in current.tableau.indices) return false

    val card = current.topWasteCard ?: return false
    val targetColumn = current.tableau[toColumn]
    val targetTop = targetColumn.lastOrNull()

    if (!card.canStackOnTableau(targetTop)) return false

    pushUndoState()

    val updatedWaste = current.waste.dropLast(1)
    val updatedTableau = current.tableau.mapIndexed { idx, col ->
      if (idx == toColumn) col + card else col
    }

    commitMove(
      move = KlondikeMove.WasteToTableau(toColumn),
      newState = current.copy(
        waste = updatedWaste,
        tableau = updatedTableau,
        baseScore = current.baseScore + 5,
        cardsPlayedSinceStockRecycle = current.cardsPlayedSinceStockRecycle + 1,
        moveCount = current.moveCount + 1,
      ),
    )
    return true
  }

  fun moveTableauToFoundation(fromColumn: Int): Boolean {
    val current = currentState
    if (fromColumn !in current.tableau.indices) return false

    val sourceColumn = current.tableau[fromColumn]
    val card = sourceColumn.lastOrNull() ?: return false
    if (!card.isFaceUp) return false

    val foundationPile = current.foundations[card.suit] ?: emptyList()
    val foundationTop = foundationPile.lastOrNull()

    if (!card.canStackOnFoundation(foundationTop)) return false

    pushUndoState()

    var revealed = false
    val remainingCards = sourceColumn.dropLast(1).toMutableList()
    if (remainingCards.isNotEmpty() && !remainingCards.last().isFaceUp) {
      val lastIdx = remainingCards.lastIndex
      remainingCards[lastIdx] = remainingCards[lastIdx].copy(isFaceUp = true)
      revealed = true
    }

    val updatedTableau = current.tableau.mapIndexed { idx, col ->
      if (idx == fromColumn) remainingCards else col
    }

    val updatedFoundations = current.foundations.toMutableMap()
    updatedFoundations[card.suit] = foundationPile + card

    val revealBonus = if (revealed) 10 else 0

    commitMove(
      move = KlondikeMove.TableauToFoundation(fromColumn, card.suit, revealed),
      newState = current.copy(
        tableau = updatedTableau,
        foundations = updatedFoundations,
        baseScore = current.baseScore + 15 + revealBonus,
        cardsPlayedSinceStockRecycle = current.cardsPlayedSinceStockRecycle + 1,
        moveCount = current.moveCount + 1,
      ),
    )
    return true
  }

  fun moveTableauToTableau(fromColumn: Int, cardIndex: Int, toColumn: Int): Boolean {
    val current = currentState
    if (fromColumn !in current.tableau.indices || toColumn !in current.tableau.indices) return false
    if (fromColumn == toColumn) return false

    val sourceCol = current.tableau[fromColumn]
    if (cardIndex !in sourceCol.indices) return false

    val movingCards = sourceCol.subList(cardIndex, sourceCol.size)
    val firstMovingCard = movingCards.first()
    if (!firstMovingCard.isFaceUp) return false

    val targetCol = current.tableau[toColumn]
    val targetTop = targetCol.lastOrNull()

    if (!firstMovingCard.canStackOnTableau(targetTop)) return false

    pushUndoState()

    var revealed = false
    val remainingSource = sourceCol.take(cardIndex).toMutableList()
    if (remainingSource.isNotEmpty() && !remainingSource.last().isFaceUp) {
      val lastIdx = remainingSource.lastIndex
      remainingSource[lastIdx] = remainingSource[lastIdx].copy(isFaceUp = true)
      revealed = true
    }

    val updatedTarget = targetCol + movingCards

    val updatedTableau = current.tableau.mapIndexed { idx, col ->
      when (idx) {
        fromColumn -> remainingSource
        toColumn -> updatedTarget
        else -> col
      }
    }

    val revealBonus = if (revealed) 10 else 0

    commitMove(
      move = KlondikeMove.TableauToTableau(fromColumn, movingCards.size, toColumn, revealed),
      newState = current.copy(
        tableau = updatedTableau,
        baseScore = current.baseScore + 5 + revealBonus,
        cardsPlayedSinceStockRecycle = current.cardsPlayedSinceStockRecycle + 1,
        moveCount = current.moveCount + 1,
      ),
    )
    return true
  }

  fun moveTableauToTableau(fromColumn: Int, toColumn: Int): Boolean {
    val col = currentState.tableau.getOrNull(fromColumn) ?: return false
    val firstFaceUpIndex = col.indexOfFirst { it.isFaceUp }
    if (firstFaceUpIndex == -1) return false

    for (idx in firstFaceUpIndex until col.size) {
      if (moveTableauToTableau(fromColumn, idx, toColumn)) return true
    }
    return false
  }

  fun moveFoundationToTableau(suit: Suit, toColumn: Int): Boolean {
    val current = currentState
    if (toColumn !in current.tableau.indices) return false

    val foundationPile = current.foundations[suit] ?: return false
    val card = foundationPile.lastOrNull() ?: return false

    val targetCol = current.tableau[toColumn]
    val targetTop = targetCol.lastOrNull()

    if (!card.canStackOnTableau(targetTop)) return false

    pushUndoState()

    val updatedFoundation = foundationPile.dropLast(1)
    val updatedFoundations = current.foundations.toMutableMap()
    updatedFoundations[suit] = updatedFoundation

    val updatedTableau = current.tableau.mapIndexed { idx, col ->
      if (idx == toColumn) col + card else col
    }

    commitMove(
      move = KlondikeMove.FoundationToTableau(suit, toColumn),
      newState = current.copy(
        foundations = updatedFoundations,
        tableau = updatedTableau,
        baseScore = maxOf(0, current.baseScore - 15),
        moveCount = current.moveCount + 1,
      ),
    )
    return true
  }

  fun autoPlayCardFromTableau(columnIndex: Int): Boolean {
    val col = currentState.tableau.getOrNull(columnIndex) ?: return false
    if (col.isEmpty()) return false

    if (moveTableauToFoundation(columnIndex)) return true

    val firstFaceUpIndex = col.indexOfFirst { it.isFaceUp }
    if (firstFaceUpIndex == -1) return false

    for (targetCol in currentState.tableau.indices) {
      if (targetCol != columnIndex && currentState.tableau[targetCol].isNotEmpty()) {
        for (idx in firstFaceUpIndex until col.size) {
          if (moveTableauToTableau(columnIndex, idx, targetCol)) return true
        }
      }
    }

    if (firstFaceUpIndex > 0) {
      for (targetCol in currentState.tableau.indices) {
        if (targetCol != columnIndex && currentState.tableau[targetCol].isEmpty()) {
          if (moveTableauToTableau(columnIndex, firstFaceUpIndex, targetCol)) return true
        }
      }
    }
    return false
  }

  fun autoPlayCardFromWaste(): Boolean {
    if (moveWasteToFoundation()) return true

    for (targetCol in currentState.tableau.indices) {
      if (currentState.tableau[targetCol].isNotEmpty()) {
        if (moveWasteToTableau(targetCol)) return true
      }
    }
    for (targetCol in currentState.tableau.indices) {
      if (currentState.tableau[targetCol].isEmpty()) {
        if (moveWasteToTableau(targetCol)) return true
      }
    }
    return false
  }

  /**
   * Performs one auto-complete step by moving any available card to its foundation.
   */
  fun performAutoCompleteStep(): Boolean {
    if (currentState.isWon) return false

    // 1. Try waste to foundation
    if (moveWasteToFoundation()) return true

    // 2. Try each tableau top card to foundation
    for (col in currentState.tableau.indices) {
      if (moveTableauToFoundation(col)) return true
    }

    // 3. If stock has cards, draw one
    if (currentState.stock.isNotEmpty()) {
      if (drawFromStock()) return true
    }

    return false
  }

  /**
   * Generates a context-aware move hint for Relaxed mode.
   */
  fun findHint(): HintInfo? {
    val state = currentState
    if (state.isWon) return null

    // 1. Can waste top card move to foundation?
    val wasteCard = state.topWasteCard
    if (wasteCard != null) {
      val fTop = state.foundations[wasteCard.suit]?.lastOrNull()
      if (wasteCard.canStackOnFoundation(fTop)) {
        return HintInfo(
          message = "Waste'teki ${wasteCard.rank.label} ${wasteCard.suit.symbol} foundation yuvasına taşınabilir.",
          card = wasteCard,
        )
      }
    }

    // 2. Can tableau top card move to foundation?
    for (col in state.tableau) {
      val top = col.lastOrNull() ?: continue
      if (top.isFaceUp) {
        val fTop = state.foundations[top.suit]?.lastOrNull()
        if (top.canStackOnFoundation(fTop)) {
          return HintInfo(
            message = "${top.rank.label} ${top.suit.symbol} foundation yuvasına taşınabilir.",
            card = top,
          )
        }
      }
    }

    // 3. Can a tableau card/sequence move to uncover a face-down card?
    for ((fromIdx, fromCol) in state.tableau.withIndex()) {
      val firstFaceUp = fromCol.indexOfFirst { it.isFaceUp }
      if (firstFaceUp > 0) { // there is a face-down card underneath
        val movingCard = fromCol[firstFaceUp]
        for ((toIdx, toCol) in state.tableau.withIndex()) {
          if (fromIdx == toIdx) continue
          if (movingCard.canStackOnTableau(toCol.lastOrNull())) {
            return HintInfo(
              message = "${movingCard.rank.label} ${movingCard.suit.symbol} taşınarak alttaki kapalı kart açılabilir.",
              card = movingCard,
            )
          }
        }
      }
    }

    // 4. Can waste card move to tableau?
    if (wasteCard != null) {
      for (col in state.tableau) {
        if (wasteCard.canStackOnTableau(col.lastOrNull())) {
          return HintInfo(
            message = "Waste'teki ${wasteCard.rank.label} ${wasteCard.suit.symbol} sütuna taşınabilir.",
            card = wasteCard,
          )
        }
      }
    }

    // 5. Any other tableau build move
    for ((fromIdx, fromCol) in state.tableau.withIndex()) {
      val firstFaceUp = fromCol.indexOfFirst { it.isFaceUp }
      if (firstFaceUp == -1) continue
      for (idx in firstFaceUp until fromCol.size) {
        val movingCard = fromCol[idx]
        for ((toIdx, toCol) in state.tableau.withIndex()) {
          if (fromIdx == toIdx) continue
          val toTop = toCol.lastOrNull()
          if (movingCard.canStackOnTableau(toTop)) {
            if (movingCard.rank == CardRank.KING && toTop == null && idx == 0) continue
            return HintInfo(
              message = "${movingCard.rank.label} ${movingCard.suit.symbol} sütun ${toIdx + 1}'e taşınabilir.",
              card = movingCard,
            )
          }
        }
      }
    }

    // 6. Draw from stock
    if (state.stock.isNotEmpty()) {
      return HintInfo(
        message = "Desteden yeni kart çekebilirsiniz.",
        card = null,
      )
    }

    if (state.waste.isNotEmpty() && state.cardsPlayedSinceStockRecycle > 0) {
      return HintInfo(
        message = "Desteyi geri dönüştürerek kartları yeniden gözden geçirebilirsiniz.",
        card = null,
      )
    }

    return null
  }

  private fun pushUndoState() {
    undoStack.addLast(currentState)
  }

  private fun commitMove(move: KlondikeMove, newState: KlondikeGameState) {
    moveHistory.add(move)
    val won = newState.calculateIsWon()
    val noMovesLeft = if (won) false else detectNoMovesRemaining(newState)

    _state.value = newState.copy(
      isWon = won,
      hasNoMovesRemaining = noMovesLeft,
    )
  }

  fun detectNoMovesRemaining(state: KlondikeGameState): Boolean {
    if (state.calculateIsWon()) return false
    if (state.stock.isNotEmpty()) return false
    if (state.waste.isNotEmpty() && state.cardsPlayedSinceStockRecycle > 0) return false

    val topWaste = state.topWasteCard
    if (topWaste != null) {
      val fTop = state.foundations[topWaste.suit]?.lastOrNull()
      if (topWaste.canStackOnFoundation(fTop)) return false
      for (col in state.tableau) {
        if (topWaste.canStackOnTableau(col.lastOrNull())) return false
      }
    }

    for (col in state.tableau) {
      val topCard = col.lastOrNull() ?: continue
      if (topCard.isFaceUp) {
        val fTop = state.foundations[topCard.suit]?.lastOrNull()
        if (topCard.canStackOnFoundation(fTop)) return false
      }
    }

    for ((fromIdx, fromCol) in state.tableau.withIndex()) {
      val firstFaceUp = fromCol.indexOfFirst { it.isFaceUp }
      if (firstFaceUp == -1) continue

      for (cardIdx in firstFaceUp until fromCol.size) {
        val movingCard = fromCol[cardIdx]
        for ((toIdx, toCol) in state.tableau.withIndex()) {
          if (fromIdx == toIdx) continue
          val toTop = toCol.lastOrNull()
          if (movingCard.canStackOnTableau(toTop)) {
            if (movingCard.rank == CardRank.KING && toTop == null && cardIdx == 0) continue
            return false
          }
        }
      }
    }

    for ((suit, fCards) in state.foundations) {
      val fTop = fCards.lastOrNull() ?: continue
      for (col in state.tableau) {
        if (fTop.canStackOnTableau(col.lastOrNull())) return false
      }
    }

    return true
  }
}
