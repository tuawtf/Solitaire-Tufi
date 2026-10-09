package com.example.ui.game.klondike

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.CardModel
import com.example.game.Suit

// Colors for playing card faces
val CardRed = Color(0xFFB71C1C)
val CardBlack = Color(0xFF212121)

fun getSuitGlyph(suit: Suit): String = "${suit.symbol}\uFE0E"

@Composable
fun CardView(
  card: CardModel,
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier,
  elevation: Dp = 2.dp,
  isHighlighted: Boolean = false,
) {
  if (card.isFaceUp) {
    FaceUpCard(
      card = card,
      width = width,
      height = height,
      elevation = elevation,
      isHighlighted = isHighlighted,
      modifier = modifier,
    )
  } else {
    FaceDownCard(
      width = width,
      height = height,
      elevation = elevation,
      modifier = modifier,
    )
  }
}

@Composable
fun FaceUpCard(
  card: CardModel,
  width: Dp,
  height: Dp,
  elevation: Dp = 2.dp,
  isHighlighted: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val suitColor = if (card.suit.isRed) CardRed else CardBlack
  val suitGlyph = getSuitGlyph(card.suit)

  val density = LocalDensity.current
  // Calculate font sizes proportional to card width, invariant to system font scaling
  val cornerFontSize = ((width.value * 0.28f) / density.fontScale).sp
  val bottomSuitFontSize = ((width.value * 0.44f) / density.fontScale).sp

  val borderColor = if (isHighlighted) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant
  }

  Card(
    modifier = modifier.size(width, height),
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(
      containerColor = Color.White,
    ),
    border = BorderStroke(if (isHighlighted) 2.dp else 1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
      // Top row: Value + Suit on a single line so it stays visible in cascading stacks
      Row(
        modifier = Modifier.align(Alignment.TopStart),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = card.rank.label,
          color = suitColor,
          fontWeight = FontWeight.Bold,
          fontSize = cornerFontSize,
          lineHeight = cornerFontSize,
        )
        Text(
          text = " $suitGlyph",
          color = suitColor,
          fontWeight = FontWeight.Bold,
          fontSize = cornerFontSize,
          lineHeight = cornerFontSize,
        )
      }

      // Large suit symbol in bottom-right corner
      Text(
        text = suitGlyph,
        color = suitColor,
        fontSize = bottomSuitFontSize,
        lineHeight = bottomSuitFontSize,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 2.dp, bottom = 2.dp),
      )
    }
  }
}

@Composable
fun FaceDownCard(
  width: Dp,
  height: Dp,
  elevation: Dp = 2.dp,
  modifier: Modifier = Modifier,
) {
  val primary = MaterialTheme.colorScheme.primary
  val container = MaterialTheme.colorScheme.primaryContainer
  val onContainer = MaterialTheme.colorScheme.onPrimaryContainer

  Card(
    modifier = modifier.size(width, height),
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(
      containerColor = primary,
    ),
    border = BorderStroke(1.dp, onContainer.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(3.dp)
        .border(
          width = 1.dp,
          color = container.copy(alpha = 0.8f),
          shape = MaterialTheme.shapes.extraSmall,
        )
        .background(
          color = primary.copy(alpha = 0.9f),
          shape = MaterialTheme.shapes.extraSmall,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 1.2.dp.toPx())
        val w = size.width
        val h = size.height

        // Decorative concentric diamond shapes on card back
        drawCircle(
          color = container.copy(alpha = 0.5f),
          radius = (w * 0.22f),
          style = stroke,
        )
        drawCircle(
          color = container.copy(alpha = 0.35f),
          radius = (w * 0.36f),
          style = stroke,
        )
      }
    }
  }
}

/**
 * Dashed-border empty slot for foundations or empty tableau columns.
 */
@Composable
fun EmptySlotView(
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier,
  suit: Suit? = null,
  text: String? = null,
) {
  val outlineColor = MaterialTheme.colorScheme.outlineVariant
  val density = LocalDensity.current
  val symbolFontSize = ((width.value * 0.40f) / density.fontScale).sp

  Box(
    modifier = modifier
      .size(width, height)
      .clip(MaterialTheme.shapes.extraSmall)
      .drawBehind {
        val stroke = Stroke(
          width = 1.5.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f),
        )
        drawRoundRect(
          color = outlineColor,
          size = size,
          cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
          style = stroke,
        )
      },
    contentAlignment = Alignment.Center,
  ) {
    if (suit != null) {
      val symbolColor = if (suit.isRed) {
        CardRed.copy(alpha = 0.35f)
      } else {
        CardBlack.copy(alpha = 0.28f)
      }
      Text(
        text = getSuitGlyph(suit),
        color = symbolColor,
        fontSize = symbolFontSize,
        fontWeight = FontWeight.Bold,
      )
    } else if (text != null) {
      Text(
        text = text,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        fontSize = symbolFontSize,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
