package com.tufisolitaire.wtf.data.settings

enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK,
}

enum class CardBackStyle {
  PRIMARY,
  SECONDARY,
  TERTIARY,
}

data class AppSettings(
  val tapToPlace: Boolean = false,
  val keepScreenOn: Boolean = true,
  val soundEnabled: Boolean = true,
  val soundVolume: Float = 0.7f,
  val cardSounds: Boolean = true,
  val winSound: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val hapticsInvalidMove: Boolean = true,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val dynamicColor: Boolean = true,
  val cardBack: CardBackStyle = CardBackStyle.PRIMARY,
  val animationsEnabled: Boolean = true,
)
