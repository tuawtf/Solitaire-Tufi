package com.example.game

data class GameStats(
  val played: Int = 0,
  val won: Int = 0,
) {
  val winRatePercentage: Int
    get() = if (played > 0) ((won.toDouble() / played) * 100).toInt() else 0

  val formattedRatio: String
    get() = "$won/$played"
}
