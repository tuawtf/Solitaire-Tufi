package com.example.ui.game.klondike

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.game.CardModel
import com.example.game.Suit

sealed interface DragSource {
  data class Tableau(val columnIndex: Int, val cardIndex: Int) : DragSource
  data object Waste : DragSource
  data class Foundation(val suit: Suit) : DragSource
}

data class DraggedCardData(
  val cards: List<CardModel>,
  val source: DragSource,
  val dragOffset: Offset,
  val touchOffset: Offset = Offset.Zero,
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
    // 1. Check foundations
    for ((suit, bounds) in foundationBounds) {
      if (bounds.inflate(12f).contains(position)) {
        return DropTarget.Foundation(suit)
      }
    }

    // 2. Check tableau columns (expand vertically downwards so dropping anywhere along column works)
    for ((colIndex, bounds) in tableauBounds) {
      val expandedBounds = Rect(
        left = bounds.left - 8f,
        top = bounds.top - 8f,
        right = bounds.right + 8f,
        bottom = bounds.bottom + 600f, // allow drop across cascade
      )
      if (expandedBounds.contains(position)) {
        return DropTarget.Tableau(colIndex)
      }
    }

    return null
  }
}

sealed interface DropTarget {
  data class Tableau(val columnIndex: Int) : DropTarget
  data class Foundation(val suit: Suit) : DropTarget
}
