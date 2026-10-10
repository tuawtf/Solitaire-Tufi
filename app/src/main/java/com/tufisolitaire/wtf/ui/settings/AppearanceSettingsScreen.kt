package com.tufisolitaire.wtf.ui.settings

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tufisolitaire.wtf.data.settings.CardBackStyle
import com.tufisolitaire.wtf.data.settings.ThemeMode
import com.tufisolitaire.wtf.ui.game.klondike.FaceDownCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = "Görünüm",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("appearance_settings_back_button"),
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
        // 1. Tema Modu (Segmented Button Row)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
          Text(
            text = "Tema",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Uygulama renk temasını belirleyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(10.dp))

          val modes = listOf(
            ThemeMode.SYSTEM to "Sistem",
            ThemeMode.LIGHT to "Açık",
            ThemeMode.DARK to "Karanlık",
          )

          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, (mode, label) ->
              val isSelected = settings.themeMode == mode
              SegmentedButton(
                selected = isSelected,
                onClick = { viewModel.setThemeMode(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                icon = { SegmentedButtonDefaults.Icon(active = isSelected) },
                label = { Text(label, fontSize = 13.sp) },
                modifier = Modifier.testTag("theme_button_${mode.name.lowercase()}"),
              )
            }
          }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 2. Dinamik Renk
        ListItem(
          headlineContent = {
            Text(
              text = "Dinamik renk",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = if (isDynamicColorSupported) {
                "Duvar kağıdınızdan türetilen Material You renklerini kullanır."
              } else {
                "Android 12 ve üzeri gereklidir."
              },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.dynamicColor && isDynamicColorSupported,
              onCheckedChange = viewModel::setDynamicColor,
              enabled = isDynamicColorSupported,
              modifier = Modifier.testTag("switch_dynamic_color"),
            )
          },
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 3. Kart Arkası Deseni Seçimi
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
          Text(
            text = "Kart arkası deseni",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Kapalı kartların arka yüz tasarımını seçin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            val cardBacks = listOf(
              CardBackStyle.PRIMARY to "Birincil",
              CardBackStyle.SECONDARY to "İkincil",
              CardBackStyle.TERTIARY to "Üçüncül",
            )

            cardBacks.forEach { (style, label) ->
              val isSelected = settings.cardBack == style
              val borderModifier = if (isSelected) {
                Modifier.border(
                  width = 2.5.dp,
                  color = MaterialTheme.colorScheme.primary,
                  shape = MaterialTheme.shapes.medium,
                )
              } else {
                Modifier
              }

              OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                  .then(borderModifier)
                  .clickable { viewModel.setCardBack(style) }
                  .testTag("card_back_option_${style.name.lowercase()}"),
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  FaceDownCard(
                    width = 54.dp,
                    height = 75.dp,
                    cardBackStyle = style,
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 4. Animasyonlar
        ListItem(
          headlineContent = {
            Text(
              text = "Animasyonlar",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          },
          supportingContent = {
            Text(
              text = "Kart hareketi ve görsel geçiş animasyonlarını açar.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingContent = {
            Switch(
              checked = settings.animationsEnabled,
              onCheckedChange = viewModel::setAnimationsEnabled,
              modifier = Modifier.testTag("switch_animations_enabled"),
            )
          },
        )
      }
    }
  }
}
