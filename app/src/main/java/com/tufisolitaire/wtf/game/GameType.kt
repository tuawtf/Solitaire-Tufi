package com.tufisolitaire.wtf.game

enum class GameType(
  val displayName: String,
  val subtitle: String,
  val description: String,
) {
  KLONDIKE(
    displayName = "Klondike",
    subtitle = "Klasik 7 Sütun",
    description = "Standart 52 kartlık deste ile en bilinen klasik Solitaire deneyimi. Kartları dönüşümlü renklerle azalan sırada dizin.",
  ),
  SPIDER(
    displayName = "Spider",
    subtitle = "8 Sütunlu Strateji",
    description = "Sütunları Papaz'dan As'a aynı seriden tamamlayarak masayı temizleyin.",
  ),
  FREECELL(
    displayName = "FreeCell",
    subtitle = "4 Serbest Hücre",
    description = "Neredeyse her elin çözülebildiği, serbest hücreleri stratejik olarak kullandığınız zeka oyunu.",
  ),
  PYRAMID(
    displayName = "Pyramid",
    subtitle = "Toplamı 13 Olan Çiftler",
    description = "Toplamı 13 eden kart çiftlerini eşleştirerek piramidi ortadan kaldırın.",
  );
}
