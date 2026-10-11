package com.tufisolitaire.wtf.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tufisolitaire.wtf.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
  onNavigateToSection: (String) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("settings_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_back),
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.testTag("settings_top_bar"),
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
        // Yeni bölüm sırası: Oynanış, Ses ve Titreşim, Görünüm, Dil, Hakkında.
        // Sadece sol ikon + başlık + sağ chevron kalsın (özet/summary kaldırıldı).

        // 1. Oynanış
        SettingsSectionRow(
          title = stringResource(R.string.settings_gameplay),
          icon = Icons.Outlined.SportsEsports,
          testTag = "settings_item_gameplay",
          onClick = { onNavigateToSection("settings/gameplay") },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 2. Ses ve Titreşim
        SettingsSectionRow(
          title = stringResource(R.string.settings_sound_and_haptics),
          icon = Icons.Outlined.VolumeUp,
          testTag = "settings_item_sound_and_haptics",
          onClick = { onNavigateToSection("settings/sound") },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 3. Görünüm
        SettingsSectionRow(
          title = stringResource(R.string.settings_appearance),
          icon = Icons.Outlined.Palette,
          testTag = "settings_item_appearance",
          onClick = { onNavigateToSection("settings/appearance") },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 4. Dil
        SettingsSectionRow(
          title = stringResource(R.string.settings_language),
          icon = Icons.Outlined.Language,
          testTag = "settings_item_language",
          onClick = { onNavigateToSection("settings/language") },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 5. Hakkında
        SettingsSectionRow(
          title = stringResource(R.string.settings_about),
          icon = Icons.Outlined.Info,
          testTag = "settings_item_about",
          onClick = { onNavigateToSection("settings/about") },
        )
      }
    }
  }
}

@Composable
private fun SettingsSectionRow(
  title: String,
  icon: ImageVector,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ListItem(
    headlineContent = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
    },
    leadingContent = {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    },
    trailingContent = {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    colors = ListItemDefaults.colors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag(testTag),
  )
}
