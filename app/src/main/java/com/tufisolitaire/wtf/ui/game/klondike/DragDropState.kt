package com.tufisolitaire.wtf.ui.game.klondike

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tufisolitaire.wtf.game.CardModel
import com.tufisolitaire.wtf.game.Suit

sealed interface DragSource {
  data class Tableau(val columnIndex: Int, val cardIndex: Int) : DragSource
  data object Waste : DragSource
  data class Foundation(val suit: Suit) : DragSource
}

sealed interface DropTarget {
  data class Tableau(val columnIndex: Int) : DropTarget
  data class Foundation(val suit: Suit) : DropTarget
}

data class DraggedCardData(
  val cards: List<CardModel>,
  val source: DragSource,
  val grabOffset: Offset,
  val cardWidth: Dp = 48.dp,
  val cardHeight: Dp = 67.dp,
  val stepHeight: Dp = 0.dp,
)

class DragDropController {
  val tableauBounds = mutableMapOf<Int, Rect>()
  val foundationBounds = mutableMapOf<Suit, Rect>()

  fun registerTableauBounds(columnIndex: Int, bounds: Rect) {
    tableauBounds[columnIndex] = bounds
  }

  fun registerFoundationBounds(suit: Suit, bounds: Rect) {
    foundationBounds[suit] = bounds
  }

  fun findDropTarget(position: Offset): DropTarget? {
    // 1. Check foundation bounds (slightly inflated for smooth target detection)
    for ((suit, bounds) in foundationBounds) {
      if (bounds.inflate(14f).contains(position)) {
        return DropTarget.Foundation(suit)
      }
    }

    // 2. Check tableau columns (expand horizontally to eliminate gap dead zones and vertically downwards)
    for ((colIndex, bounds) in tableauBounds) {
      val expandedBounds = Rect(
        left = bounds.left - 6f,
        top = bounds.top - 12f,
        right = bounds.right + 6f,
        bottom = bounds.bottom + 1200f,
      )
      if (expandedBounds.contains(position)) {
        return DropTarget.Tableau(colIndex)
      }
    }

    return null
  }

  /**
   * Evaluates if a given tableau column is a valid drop target for the currently dragged card(s).
   */
  fun isValidTableauTarget(draggedData: DraggedCardData?, targetColumnIndex: Int, targetColumnCards: List<CardModel>): Boolean {
    if (draggedData == null) return false
    val firstDraggedCard = draggedData.cards.firstOrNull() ?: return false

    // Prevent dropping onto the same tableau column at the same position
    if (draggedData.source is DragSource.Tableau && draggedData.source.columnIndex == targetColumnIndex) {
      return false
    }

    val topTargetCard = targetColumnCards.lastOrNull()
    return firstDraggedCard.canStackOnTableau(topTargetCard)
  }

  /**
   * Evaluates if a given foundation pile is a valid drop target for the currently dragged card.
   */
  fun isValidFoundationTarget(draggedData: DraggedCardData?, suit: Suit, foundationCards: List<CardModel>): Boolean {
    if (draggedData == null) return false
    if (draggedData.cards.size != 1) return false
    val card = draggedData.cards.first()
    if (card.suit != suit) return false

    val topCard = foundationCards.lastOrNull()
    return card.canStackOnFoundation(topCard)
  }
}
