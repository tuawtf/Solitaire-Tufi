package com.tufisolitaire.wtf.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HapticsSettingsScreen(
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
            text = "Titreşim",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("haptics_settings_back_button"),
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
        // Master Switch: Titreşim geri bildirimi
        ListItem(
          headlineContent = {
            Text(
              text = "Titreşim geri bildirimi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Dokunma ve kart taşıma işlemlerinde dokunsal geri bildirim.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.hapticsEnabled,
              onCheckedChange = viewModel::setHapticsEnabled,
              modifier = Modifier.testTag("switch_haptics_enabled"),
            )
          },
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        val subAlpha = if (settings.hapticsEnabled) 1f else 0.38f

        // Switch: Geçersiz hamle titreşimi
        ListItem(
          headlineContent = {
            Text(
              text = "Geçersiz hamle titreşimi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Geçersiz bir yere kart bırakılmaya çalışıldığında uyarı titreşimi verilir.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.hapticsInvalidMove,
              onCheckedChange = viewModel::setHapticsInvalidMove,
              enabled = settings.hapticsEnabled,
              modifier = Modifier.testTag("switch_haptics_invalid_move"),
            )
          },
          modifier = Modifier.alpha(subAlpha),
        )
      }
    }
  }
}
