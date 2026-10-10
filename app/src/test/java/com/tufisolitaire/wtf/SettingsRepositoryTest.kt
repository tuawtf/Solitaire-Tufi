package com.tufisolitaire.wtf

import com.tufisolitaire.wtf.data.settings.AppSettings
import com.tufisolitaire.wtf.data.settings.CardBackStyle
import com.tufisolitaire.wtf.data.settings.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

  @Test
  fun appSettings_defaultValues_areCorrect() {
    val defaultSettings = AppSettings()

    // Verify tap_to_place defaults to false as required
    assertFalse("tap_to_place default must be false", defaultSettings.tapToPlace)

    // Verify other defaults
    assertTrue("keep_screen_on default must be true", defaultSettings.keepScreenOn)
    assertTrue("sound_enabled default must be true", defaultSettings.soundEnabled)
    assertEquals("sound_volume default must be 0.7f", 0.7f, defaultSettings.soundVolume, 0.001f)
    assertTrue("card_sounds default must be true", defaultSettings.cardSounds)
    assertTrue("win_sound default must be true", defaultSettings.winSound)
    assertTrue("haptics_enabled default must be true", defaultSettings.hapticsEnabled)
    assertTrue("haptics_invalid_move default must be true", defaultSettings.hapticsInvalidMove)
    assertEquals("theme_mode default must be SYSTEM", ThemeMode.SYSTEM, defaultSettings.themeMode)
    assertTrue("dynamic_color default must be true", defaultSettings.dynamicColor)
    assertEquals("card_back default must be PRIMARY", CardBackStyle.PRIMARY, defaultSettings.cardBack)
    assertTrue("animations_enabled default must be true", defaultSettings.animationsEnabled)
  }
}
