package com.example.ui.game.klondike

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.Suit

/**
 * High-quality vector Suit icon rendered with Canvas and geometric Paths.
 * Guarantees zero emoji/glyph fallback across all Android devices, fonts, and OEM skins.
 */
@Composable
fun SuitIcon(
  suit: Suit,
  modifier: Modifier = Modifier,
  size: Dp = 16.dp,
  tint: Color = if (suit.isRed) CardRed else CardBlack,
) {
  Canvas(
    modifier = modifier.size(size),
  ) {
    when (suit) {
      Suit.HEARTS -> drawHeart(tint)
      Suit.DIAMONDS -> drawDiamond(tint)
      Suit.CLUBS -> drawClub(tint)
      Suit.SPADES -> drawSpade(tint)
    }
  }
}

/**
 * Draws a smooth heart using cubic bezier curves.
 */
private fun DrawScope.drawHeart(tint: Color) {
  val w = size.width
  val h = size.height
  val path = Path().apply {
    moveTo(w * 0.5f, h * 0.85f)
    // Left bottom curve up to left bulge
    cubicTo(
      w * 0.12f, h * 0.58f,
      w * 0.00f, h * 0.35f,
      w * 0.05f, h * 0.18f,
    )
    // Left top bulge
    cubicTo(
      w * 0.09f, h * 0.04f,
      w * 0.32f, h * 0.02f,
      w * 0.50f, h * 0.25f,
    )
    // Right top bulge
    cubicTo(
      w * 0.68f, h * 0.02f,
      w * 0.91f, h * 0.04f,
      w * 0.95f, h * 0.18f,
    )
    // Right bulge down to tip
    cubicTo(
      w * 1.00f, h * 0.35f,
      w * 0.88f, h * 0.58f,
      w * 0.50f, h * 0.85f,
    )
    close()
  }
  drawPath(path, color = tint)
}

/**
 * Draws a sharp diamond with subtle curvature.
 */
private fun DrawScope.drawDiamond(tint: Color) {
  val w = size.width
  val h = size.height
  val path = Path().apply {
    moveTo(w * 0.5f, h * 0.04f)
    lineTo(w * 0.96f, h * 0.5f)
    lineTo(w * 0.5f, h * 0.96f)
    lineTo(w * 0.04f, h * 0.5f)
    close()
  }
  drawPath(path, color = tint)
}

/**
 * Draws a traditional playing card spade with pointed tip, rounded lobes and pedestal stem.
 */
private fun DrawScope.drawSpade(tint: Color) {
  val w = size.width
  val h = size.height
  val path = Path().apply {
    // Top point
    moveTo(w * 0.5f, h * 0.05f)
    // Left lobe
    cubicTo(
      w * 0.85f, h * 0.35f,
      w * 0.98f, h * 0.62f,
      w * 0.72f, h * 0.76f,
    )
    cubicTo(
      w * 0.58f, h * 0.82f,
      w * 0.54f, h * 0.68f,
      w * 0.52f, h * 0.62f,
    )
    // Base/Stem right side down
    lineTo(w * 0.56f, h * 0.95f)
    lineTo(w * 0.44f, h * 0.95f)
    // Base/Stem left side up
    lineTo(w * 0.48f, h * 0.62f)
    cubicTo(
      w * 0.46f, h * 0.68f,
      w * 0.42f, h * 0.82f,
      w * 0.28f, h * 0.76f,
    )
    cubicTo(
      w * 0.02f, h * 0.62f,
      w * 0.15f, h * 0.35f,
      w * 0.50f, h * 0.05f,
    )
    close()
  }
  drawPath(path, color = tint)
}

/**
 * Draws a traditional playing card club with three clover leaves and a flared pedestal.
 */
private fun DrawScope.drawClub(tint: Color) {
  val w = size.width
  val h = size.height
  val r = w * 0.24f // Leaf radius

  // Top leaf center
  drawCircle(color = tint, radius = r, center = Offset(w * 0.5f, h * 0.30f))
  // Bottom-left leaf center
  drawCircle(color = tint, radius = r, center = Offset(w * 0.30f, h * 0.55f))
  // Bottom-right leaf center
  drawCircle(color = tint, radius = r, center = Offset(w * 0.70f, h * 0.55f))
  // Center filler circle to bind leaves together
  drawCircle(color = tint, radius = r * 0.8f, center = Offset(w * 0.5f, h * 0.48f))

  // Flared stem
  val stemPath = Path().apply {
    moveTo(w * 0.46f, h * 0.48f)
    lineTo(w * 0.54f, h * 0.48f)
    lineTo(w * 0.64f, h * 0.95f)
    lineTo(w * 0.36f, h * 0.95f)
    close()
  }
  drawPath(stemPath, color = tint)
}
