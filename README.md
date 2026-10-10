# Solitaire (Klondike)

Modern, akıcı animasyonlara ve Material Design 3 tasarım diline sahip klasik Klondike Solitaire Android oyunu.

## Özellikler
- **Klondike Kuralları**: Draw 1 ve Draw 3 kart çekme modları.
- **Zorluk Seviyeleri**: Kolay, Normal ve Zor dağıtımlar.
- **Akıcı Oynanış**: Sürükle-bırak desteği, tek dokunuşla akıllı kart taşıma ve geri alma (Undo).
- **Modern Görsel Tasarım**: Vektörel takım simgeleri (SuitIcon), dinamik renk paleti ve kart açılma animasyonları.
- **İpuçları & Skor**: Otomatik hamle tespiti, süre ve hamle sayacı, yerel oyun istatistikleri (Room DB).
- **Çevrimdışı & Güvenli**: İnternet bağlantısı gerektirmez, yalnızca titreşim izni kullanır.

## Proje Yapısı
```text
app/src/main/java/com/tufisolitaire/wtf/
├── MainActivity.kt
├── data/              # Room veritabanı (SavedGame, Stats) & DataStore
├── game/              # Klondike oyun motoru, deste ve kurallar
└── ui/
    ├── game/klondike/ # Klondike oyun ekranı, kart bileşenleri ve jestler
    ├── home/          # Ana menü, mod seçimi ve istatistik paneli
    ├── navigation/    # Jetpack Navigation rotaları
    └── theme/         # Material 3 tema, renk ve tipografi
```

## Derleme ve Kurulum
Gereksinimler: Android SDK 36 (minSdk 24), JDK 17, Gradle.

```bash
# Debug APK derleme
gradle assembleDebug

# Birim testleri çalıştırma
gradle testDebugUnitTest
```

## Sürüm Yayınlama (GitHub Actions)
Yeni bir sürüm yayınlamak için:
1. `git tag v1.0.0 && git push origin v1.0.0` komutuyla sürüm etiketi gönderin veya
2. GitHub Actions sekmesinden **Build & Release** workflow'unu `workflow_dispatch` ile sürüm numarasını girerek başlatın.
3. Ana dala (`main`/`master`) yapılan push'lar release oluşturmaz; yalnızca APK/AAB çıktılarını 14 gün saklanan artifact olarak derler.
