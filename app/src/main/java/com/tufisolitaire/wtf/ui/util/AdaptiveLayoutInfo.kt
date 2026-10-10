package com.tufisolitaire.wtf.ui.util

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Adaptive screen configuration providing layout metrics and window size classes
 * to help screens render responsively across phone (portrait/landscape), foldable, and tablet.
 */
@Immutable
data class AdaptiveLayoutInfo(
  val widthSizeClass: WindowWidthSizeClass,
  val heightSizeClass: WindowHeightSizeClass,
  val screenWidthDp: Dp,
  val screenHeightDp: Dp,
  val isLandscape: Boolean,
  val isTabletOrExpanded: Boolean,
  val maxContentWidth: Dp = 640.dp,
)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
  val configuration = LocalConfiguration.current
  val widthDp = configuration.screenWidthDp.dp
  val heightDp = configuration.screenHeightDp.dp
  val isLandscape = widthDp > heightDp

  val windowSizeClass = WindowSizeClass.calculateFromSize(
    size = DpSize(widthDp, heightDp),
  )

  return remember(windowSizeClass, widthDp, heightDp, isLandscape) {
    val widthClass = windowSizeClass.widthSizeClass
    val isTablet = widthClass == WindowWidthSizeClass.Expanded ||
      (widthClass == WindowWidthSizeClass.Medium && !isLandscape)
    AdaptiveLayoutInfo(
      widthSizeClass = widthClass,
      heightSizeClass = windowSizeClass.heightSizeClass,
      screenWidthDp = widthDp,
      screenHeightDp = heightDp,
      isLandscape = isLandscape,
      isTabletOrExpanded = isTablet,
      maxContentWidth = 640.dp,
    )
  }
}
