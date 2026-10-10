package com.tufisolitaire.wtf.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplaySettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  var showResetDialog by remember { mutableStateOf(false) }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = {
        Text("İstatistikleri Sıfırla")
      },
      text = {
        Text("Tüm kazanma/oynanma sayıları silinecek. Devam eden oyun etkilenmez.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.resetStats()
            showResetDialog = false
          },
          modifier = Modifier.testTag("confirm_reset_stats_button"),
        ) {
          Text("Sıfırla", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showResetDialog = false },
          modifier = Modifier.testTag("cancel_reset_stats_button"),
        ) {
          Text("İptal")
        }
      },
    )
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = "Oynanış",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("gameplay_settings_back_button"),
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
        // Switch 1: Dokunarak yerleştir
        ListItem(
          headlineContent = {
            Text(
              text = "Dokunarak yerleştir",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Açıkken bir karta dokunduğunda kart uygun yere otomatik taşınır. Kapalıyken kartları yalnızca sürükleyerek yerleştirirsin.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.tapToPlace,
              onCheckedChange = viewModel::setTapToPlace,
              modifier = Modifier.testTag("switch_tap_to_place"),
            )
          },
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Switch 2: Ekranı açık tut
        ListItem(
          headlineContent = {
            Text(
              text = "Ekranı açık tut",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Oyun sırasında ekranın kapanmasını önler.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.keepScreenOn,
              onCheckedChange = viewModel::setKeepScreenOn,
              modifier = Modifier.testTag("switch_keep_screen_on"),
            )
          },
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Item 3: İstatistikleri sıfırla
        ListItem(
          headlineContent = {
            Text(
              text = "İstatistikleri sıfırla",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.error,
            )
          },
          supportingContent = {
            Text(
              text = "Tüm kazanma ve oynanma verilerini kalıcı olarak siler.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          modifier = Modifier
            .clickable { showResetDialog = true }
            .testTag("reset_stats_item"),
        )
      }
    }
  }
}
