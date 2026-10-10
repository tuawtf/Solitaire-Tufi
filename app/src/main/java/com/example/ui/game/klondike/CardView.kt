package com.example.ui.game.klondike

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.CardModel
import com.example.game.CardRank
import com.example.game.Suit

// Colors for playing card faces
val CardRed = Color(0xFFC62828) // Vibrant warm playing card crimson
val CardBlack = Color(0xFF263238) // Deep charcoal dark suit color
val CardWarmWhite = Color(0xFFFCFDFD) // Soft warm playing card white

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
  val density = LocalDensity.current

  // Font sizes scaled proportionally from card width, invariant to system font scaling
  val cornerFontSize = ((width.value * 0.26f) / density.fontScale).sp
  val cornerIconSize = (width.value * 0.22f).dp
  val centerWatermarkSize = (width.value * 0.65f).dp
  val centerRankFontSize = ((width.value * 0.48f) / density.fontScale).sp
  val isCourtCard = card.rank == CardRank.ACE || card.rank == CardRank.JACK ||
    card.rank == CardRank.QUEEN || card.rank == CardRank.KING

  val borderColor = if (isHighlighted) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
  }

  Card(
    modifier = modifier.size(width, height),
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(
      containerColor = CardWarmWhite,
    ),
    border = BorderStroke(if (isHighlighted) 2.dp else 1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 3.dp, vertical = 2.dp),
    ) {
      // 1. Top-Left Corner: Rank on top + SuitIcon right below or beside
      Column(
        modifier = Modifier.align(Alignment.TopStart),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = card.rank.label,
          color = suitColor,
          fontWeight = FontWeight.ExtraBold,
          fontSize = cornerFontSize,
          lineHeight = cornerFontSize,
        )
        Spacer(modifier = Modifier.height(1.dp))
        SuitIcon(
          suit = card.suit,
          size = cornerIconSize,
          tint = suitColor,
        )
      }

      // 2. Center Content: If court card (A, J, Q, K), display large bold letter with soft suit watermark behind it;
      // otherwise, display prominent center SuitIcon
      Box(
        modifier = Modifier.align(Alignment.Center),
        contentAlignment = Alignment.Center,
      ) {
        if (isCourtCard) {
          // Soft watermark suit icon
          SuitIcon(
            suit = card.suit,
            size = centerWatermarkSize,
            tint = suitColor.copy(alpha = 0.18f),
          )
          // Sharp bold court letter
          Text(
            text = card.rank.label,
            color = suitColor,
            fontWeight = FontWeight.Black,
            fontSize = centerRankFontSize,
            lineHeight = centerRankFontSize,
          )
        } else {
          // Number card: large crisp center SuitIcon
          SuitIcon(
            suit = card.suit,
            size = (width.value * 0.48f).dp,
            tint = suitColor,
          )
        }
      }

      // 3. Bottom-Right Corner: Inverted (180° rotated) Rank & SuitIcon for authentic playing card symmetry
      Column(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .rotate(180f),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = card.rank.label,
          color = suitColor,
          fontWeight = FontWeight.ExtraBold,
          fontSize = cornerFontSize,
          lineHeight = cornerFontSize,
        )
        Spacer(modifier = Modifier.height(1.dp))
        SuitIcon(
          suit = card.suit,
          size = cornerIconSize,
          tint = suitColor,
        )
      }
    }
  }
}

/**
 * Premium card back pattern designed with MaterialTheme dynamic tokens.
 * Features an inner inset border and an intricate geometric diamond lattice.
 */
@Composable
fun FaceDownCard(
  width: Dp,
  height: Dp,
  elevation: Dp = 2.dp,
  modifier: Modifier = Modifier,
) {
  val primary = MaterialTheme.colorScheme.primary
  val container = MaterialTheme.colorScheme.primaryContainer
  val onPrimary = MaterialTheme.colorScheme.onPrimary

  Card(
    modifier = modifier.size(width, height),
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(
      containerColor = CardWarmWhite,
    ),
    border = BorderStroke(1.dp, primary.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(2.5.dp)
        .background(color = primary, shape = MaterialTheme.shapes.extraSmall)
        .border(width = 1.dp, color = onPrimary.copy(alpha = 0.6f), shape = MaterialTheme.shapes.extraSmall)
        .padding(2.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.dp.toPx())

        // Inner dashed framing
        drawRoundRect(
          color = container.copy(alpha = 0.7f),
          size = size,
          cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
          style = stroke,
        )

        // Geometric diamond lattice
        val step = w / 4f
        var x = 0f
        while (x <= w + step) {
          drawLine(
            color = onPrimary.copy(alpha = 0.25f),
            start = Offset(x, 0f),
            end = Offset(x - h * 0.7f, h),
            strokeWidth = 1f,
          )
          drawLine(
            color = onPrimary.copy(alpha = 0.25f),
            start = Offset(x, 0f),
            end = Offset(x + h * 0.7f, h),
            strokeWidth = 1f,
          )
          x += step
        }

        // Concentric central diamond medallion
        val cx = w * 0.5f
        val cy = h * 0.5f
        val diaR1 = w * 0.24f
        val diaR2 = w * 0.14f

        drawCircle(color = container.copy(alpha = 0.85f), radius = diaR1, center = Offset(cx, cy), style = stroke)
        drawCircle(color = onPrimary.copy(alpha = 0.5f), radius = diaR2, center = Offset(cx, cy))
      }
    }
  }
}

/**
 * Dashed-border empty slot for foundations or empty tableau columns.
 * Employs vector SuitIcon or King indicator with proper highlight state.
 */
@Composable
fun EmptySlotView(
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier,
  suit: Suit? = null,
  text: String? = null,
  isTargetHighlight: Boolean = false,
) {
  val highlightColor = MaterialTheme.colorScheme.primary
  val defaultOutlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
  val outlineColor = if (isTargetHighlight) highlightColor else defaultOutlineColor

  val density = LocalDensity.current
  val symbolFontSize = ((width.value * 0.44f) / density.fontScale).sp
  val iconSize = (width.value * 0.44f).dp

  Box(
    modifier = modifier
      .size(width, height)
      .clip(MaterialTheme.shapes.extraSmall)
      .drawBehind {
        val stroke = Stroke(
          width = if (isTargetHighlight) 2.5.dp.toPx() else 1.5.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f),
        )
        drawRoundRect(
          color = outlineColor,
          size = size,
          cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
          style = stroke,
        )
      }
      .background(
        color = if (isTargetHighlight) highlightColor.copy(alpha = 0.12f) else Color.Transparent,
        shape = MaterialTheme.shapes.extraSmall,
      ),
    contentAlignment = Alignment.Center,
  ) {
    if (suit != null) {
      val tintColor = if (suit.isRed) {
        CardRed.copy(alpha = if (isTargetHighlight) 0.6f else 0.30f)
      } else {
        CardBlack.copy(alpha = if (isTargetHighlight) 0.5f else 0.25f)
      }
      SuitIcon(
        suit = suit,
        size = iconSize,
        tint = tintColor,
      )
    } else if (text != null) {
      Text(
        text = text,
        color = if (isTargetHighlight) highlightColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        fontSize = symbolFontSize,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
