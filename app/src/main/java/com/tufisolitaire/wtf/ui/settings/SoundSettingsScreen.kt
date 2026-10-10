package com.tufisolitaire.wtf.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = "Ses",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("sound_settings_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Geri",
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
      )
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 640.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 8.dp),
      ) {
        // Master Switch: Ses Efektleri
        ListItem(
          headlineContent = {
            Text(
              text = "Ses efektleri",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Tüm oyun içi ses efektlerini açar veya kapatır.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.soundEnabled,
              onCheckedChange = viewModel::setSoundEnabled,
              modifier = Modifier.testTag("switch_sound_enabled"),
            )
          },
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        val subControlsAlpha = if (settings.soundEnabled) 1f else 0.38f

        // Slider: Ses Seviyesi
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(subControlsAlpha),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Ses seviyesi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = "%${(settings.soundVolume * 100).roundToInt()}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Slider(
            value = settings.soundVolume,
            onValueChange = viewModel::setSoundVolume,
            enabled = settings.soundEnabled,
            valueRange = 0f..1f,
            modifier = Modifier.testTag("slider_sound_volume"),
          )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Switch: Kart Sesleri
        ListItem(
          headlineContent = {
            Text(
              text = "Kart sesleri",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Kart çekme, çevirme ve dağıtma sesleri.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.cardSounds,
              onCheckedChange = viewModel::setCardSounds,
              enabled = settings.soundEnabled,
              modifier = Modifier.testTag("switch_card_sounds"),
            )
          },
          modifier = Modifier.alpha(subControlsAlpha),
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Switch: Kazanma Sesi
        ListItem(
          headlineContent = {
            Text(
              text = "Kazanma sesi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Oyun kazanıldığında çalan kutlama sesi.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.winSound,
              onCheckedChange = viewModel::setWinSound,
              enabled = settings.soundEnabled,
              modifier = Modifier.testTag("switch_win_sound"),
            )
          },
          modifier = Modifier.alpha(subControlsAlpha),
        )
      }
    }
  }
}
