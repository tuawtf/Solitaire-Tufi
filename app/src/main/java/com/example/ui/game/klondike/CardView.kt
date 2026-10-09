package com.example.ui.game.klondike

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.CardModel
import com.example.game.Suit

// Classic deep navy blue card-back color token
val CardBackBlue = Color(0xFF1E3A8A)
val CardBackInnerBlue = Color(0xFF2563EB)

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
  val suitColor = if (card.suit.isRed) {
    MaterialTheme.colorScheme.error
  } else {
    MaterialTheme.colorScheme.onSurface
  }

  val borderColor = if (isHighlighted) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant
  }

  androidx.compose.material3.Card(
    modifier = modifier.size(width, height),
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    border = BorderStroke(if (isHighlighted) 2.dp else 1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 3.dp, vertical = 2.dp),
    ) {
      // Top-Left Corner Rank & Suit
      Column(
        modifier = Modifier.align(Alignment.TopStart),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = card.rank.label,
          color = suitColor,
          fontWeight = FontWeight.Bold,
          fontSize = (width.value * 0.26f).sp,
          lineHeight = (width.value * 0.28f).sp,
          textAlign = TextAlign.Center,
        )
        Text(
          text = card.suit.symbol,
          color = suitColor,
          fontSize = (width.value * 0.24f).sp,
          lineHeight = (width.value * 0.24f).sp,
          textAlign = TextAlign.Center,
        )
      }

      // Center decorative suit symbol
      Text(
        text = card.suit.symbol,
        color = suitColor.copy(alpha = 0.88f),
        fontSize = (width.value * 0.44f).sp,
        modifier = Modifier.align(Alignment.Center),
      )

      // Bottom-Right Corner (inverted)
      Column(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .rotate(180f),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = card.rank.label,
          color = suitColor,
          fontWeight = FontWeight.Bold,
          fontSize = (width.value * 0.22f).sp,
          lineHeight = (width.value * 0.24f).sp,
          textAlign = TextAlign.Center,
        )
        Text(
          text = card.suit.symbol,
          color = suitColor,
          fontSize = (width.value * 0.20f).sp,
          lineHeight = (width.value * 0.20f).sp,
          textAlign = TextAlign.Center,
        )
      }
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
  androidx.compose.material3.Card(
    modifier = modifier.size(width, height),
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(
      containerColor = CardBackBlue,
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(3.dp)
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
          shape = MaterialTheme.shapes.extraSmall,
        )
        .background(
          color = CardBackInnerBlue,
          shape = MaterialTheme.shapes.extraSmall,
        ),
      contentAlignment = Alignment.Center,
    ) {
      // Diamond decorative pattern
      Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 1.dp.toPx())
        val w = size.width
        val h = size.height

        // Inner decorative diamond
        drawCircle(
          color = Color.White.copy(alpha = 0.45f),
          radius = (w * 0.22f),
          style = stroke,
        )
        drawCircle(
          color = Color.White.copy(alpha = 0.3f),
          radius = (w * 0.35f),
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
        MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
      }
      Text(
        text = suit.symbol,
        color = symbolColor,
        fontSize = (width.value * 0.42f).sp,
        fontWeight = FontWeight.Bold,
      )
    } else if (text != null) {
      Text(
        text = text,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        fontSize = (width.value * 0.38f).sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
