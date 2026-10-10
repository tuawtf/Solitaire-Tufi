package com.tufisolitaire.wtf.game.klondike

enum class DifficultyMode(
  val displayName: String,
  val description: String,
  val multiplier: Float,
  val hasHints: Boolean,
  val maxUndos: Int?, // null means unlimited
  val defaultDrawMode: DrawMode,
) {
  RELAXED(
    displayName = "Relaxed",
    description = "Çözülebilir garantili dağıtım, sınırsız Geri Al, ipucu ve otomatik tamamlama.",
    multiplier = 1.0f,
    hasHints = true,
    maxUndos = null,
    defaultDrawMode = DrawMode.DRAW_ONE,
  ),
  REGULAR(
    displayName = "Regular",
    description = "Standart rastgele dağıtım, sınırsız Geri Al, klasik puanlama.",
    multiplier = 1.5f,
    hasHints = false,
    maxUndos = null,
    defaultDrawMode = DrawMode.DRAW_ONE,
  ),
  HARD(
    displayName = "Hard",
    description = "Rastgele dağıtım, sınırlı Geri Al (3 hak), Draw 3 önerilir, yüksek çarpan.",
    multiplier = 2.5f,
    hasHints = false,
    maxUndos = 3,
    defaultDrawMode = DrawMode.DRAW_THREE,
  );
}
