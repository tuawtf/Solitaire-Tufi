package com.tufisolitaire.wtf.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tufisolitaire.wtf.R
import com.tufisolitaire.wtf.data.savedgame.SavedGameEntity
import com.tufisolitaire.wtf.game.GameStats
import com.tufisolitaire.wtf.game.GameType
import com.tufisolitaire.wtf.game.klondike.DifficultyMode
import com.tufisolitaire.wtf.game.klondike.DrawMode
import com.tufisolitaire.wtf.ui.theme.SolitaireTheme
import com.tufisolitaire.wtf.ui.util.rememberAdaptiveLayoutInfo
import java.util.Locale

@Composable
fun HomeRoute(
  viewModel: HomeViewModel,
  onStartNewGame: (GameType, DifficultyMode, DrawMode) -> Unit,
  onResumeGame: (GameType) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  HomeScreen(
    uiState = uiState,
    onSelectGameType = viewModel::selectGameType,
    onOpenSettings = onOpenSettings,
    onNewGameClicked = viewModel::onNewGameClicked,
    onConfirmOverwrite = viewModel::confirmOverwriteAndShowDialog,
    onDismissOverwrite = viewModel::dismissOverwriteDialog,
    onDismissNewGameDialog = viewModel::dismissNewGameDialog,
    onSetDialogDifficulty = viewModel::setDialogDifficulty,
    onSetDialogDrawMode = viewModel::setDialogDrawMode,
    onLaunchNewGame = { diff, draw ->
      viewModel.dismissNewGameDialog()
      onStartNewGame(uiState.selectedGameType, diff, draw)
    },
    onResumeGame = { onResumeGame(uiState.selectedGameType) },
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: HomeUiState,
  onSelectGameType: (GameType) -> Unit,
  onOpenSettings: () -> Unit,
  onNewGameClicked: () -> Unit,
  onConfirmOverwrite: () -> Unit,
  onDismissOverwrite: () -> Unit,
  onDismissNewGameDialog: () -> Unit,
  onSetDialogDifficulty: (DifficultyMode) -> Unit,
  onSetDialogDrawMode: (DrawMode) -> Unit,
  onLaunchNewGame: (DifficultyMode, DrawMode) -> Unit,
  onResumeGame: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  val adaptive = rememberAdaptiveLayoutInfo()

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = stringResource(R.string.title_solitaire),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        actions = {
          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button"),
          ) {
            Icon(
              imageVector = Icons.Outlined.Settings,
              contentDescription = "Ayarlar",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.testTag("home_top_bar"),
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
          .fillMaxSize()
          .widthIn(max = adaptive.maxContentWidth)
          .verticalScroll(scrollState),
      ) {
        // --- DEVAM ET CARD (Only for Klondike if a saved game exists) ---
        if (uiState.selectedGameType == GameType.KLONDIKE && uiState.savedKlondikeGame != null) {
          val saved = uiState.savedKlondikeGame
          val minutes = saved.elapsedSeconds / 60
          val seconds = saved.elapsedSeconds % 60
          val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

          // Format clean labels for chips: "Draw 1", "Draw 3" and capitalized difficulty "Relaxed", "Regular", "Hard"
          val diffLabel = saved.difficulty.lowercase().replaceFirstChar { it.uppercase() }
          val drawLabel = if (saved.drawMode.contains("THREE", ignoreCase = true) || saved.drawMode.contains("3")) {
            "Draw 3"
          } else {
            "Draw 1"
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp)
              .padding(top = 16.dp),
          ) {
            ElevatedCard(
              shape = MaterialTheme.shapes.large,
              colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("resume_game_card"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
              ) {
                // Top row: Icon + "Devam Eden Oyun" title (always unobstructed)
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "Devam Eden Oyun",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FlowRow for read-only informational chips that wrap cleanly on narrow screens / large font
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  AssistChip(
                    onClick = {},
                    enabled = true,
                    colors = AssistChipDefaults.assistChipColors(
                      containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                      labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                      leadingIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = null,
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    },
                    label = {
                      Text(
                        text = diffLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                      )
                    },
                  )

                  AssistChip(
                    onClick = {},
                    enabled = true,
                    colors = AssistChipDefaults.assistChipColors(
                      containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                      labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                      leadingIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = null,
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Outlined.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    },
                    label = {
                      Text(
                        text = drawLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                      )
                    },
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time and moves line (wraps cleanly if needed without clipping)
                Text(
                  text = "Süre $formattedTime · Hamle ${saved.moveCount}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                  onClick = onResumeGame,
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("resume_game_button"),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Devam Et", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        // --- BIG "NEW GAME" BUTTON (Only for Klondike) ---
        if (uiState.selectedGameType == GameType.KLONDIKE) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
          FilledTonalButton(
            onClick = onNewGameClicked,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 58.dp)
              .testTag("new_game_button"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = stringResource(R.string.action_new_game),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }

      // --- GAME TABS: Klondike, Spider, FreeCell, Pyramid ---
      val gameTypes = GameType.entries
      val selectedTabIndex = gameTypes.indexOf(uiState.selectedGameType).coerceAtLeast(0)

      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("game_tabs_row"),
      ) {
        gameTypes.forEachIndexed { index, gameType ->
          val isSelected = (index == selectedTabIndex)
          val stats = uiState.statsMap[gameType] ?: GameStats()

          Tab(
            selected = isSelected,
            onClick = { onSelectGameType(gameType) },
            modifier = Modifier.testTag("tab_${gameType.name.lowercase()}"),
            text = {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
              ) {
                Text(
                  text = gameType.displayName,
                  maxLines = 1,
                  softWrap = false,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = if (gameType == GameType.KLONDIKE) stats.formattedRatio else "Yakında",
                  maxLines = 1,
                  softWrap = false,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                )
              }
            },
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // --- CONTENT PER TAB: Klondike Details vs Coming Soon Placeholder ---
      if (uiState.selectedGameType == GameType.KLONDIKE) {
        SelectedGameDetails(
          gameType = uiState.selectedGameType,
          stats = uiState.currentStats,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        )
      } else {
        ComingSoonPlaceholder(
          gameType = uiState.selectedGameType,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        )
      }
    }
  }
}

  // --- OVERWRITE CONFIRMATION DIALOG ---
  if (uiState.showConfirmOverwriteDialog) {
    AlertDialog(
      onDismissRequest = onDismissOverwrite,
      title = {
        Text("Yeni Oyun Başlatılsın mı?", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("Devam eden oyun silinecek, emin misiniz?")
      },
      confirmButton = {
        Button(onClick = onConfirmOverwrite) {
          Text("Yeni Oyun")
        }
      },
      dismissButton = {
        TextButton(onClick = onDismissOverwrite) {
          Text(stringResource(R.string.dialog_cancel))
        }
      },
    )
  }

  // --- NEW GAME CONFIGURATION DIALOG ---
  if (uiState.showNewGameDialog) {
    NewGameSettingsDialog(
      difficulty = uiState.dialogDifficulty,
      drawMode = uiState.dialogDrawMode,
      onSelectDifficulty = onSetDialogDifficulty,
      onSelectDrawMode = onSetDialogDrawMode,
      onConfirm = {
        onLaunchNewGame(uiState.dialogDifficulty, uiState.dialogDrawMode)
      },
      onDismiss = onDismissNewGameDialog,
    )
  }
}

/**
 * Material 3 AlertDialog for choosing Draw Mode and Difficulty before starting a new game.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewGameSettingsDialog(
  difficulty: DifficultyMode,
  drawMode: DrawMode,
  onSelectDifficulty: (DifficultyMode) -> Unit,
  onSelectDrawMode: (DrawMode) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Klondike",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // --- Draw Mode Segmented Button ---
        Column {
          Text(
            text = "Kart Çekme Modu",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
          )
          val drawModes = listOf(DrawMode.DRAW_ONE to "Draw 1", DrawMode.DRAW_THREE to "Draw 3")
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            drawModes.forEachIndexed { index, (mode, label) ->
              val isSelected = drawMode == mode
              SegmentedButton(
                selected = isSelected,
                onClick = { onSelectDrawMode(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = drawModes.size),
                icon = { SegmentedButtonDefaults.Icon(active = isSelected) },
                label = { Text(label, fontSize = 13.sp) },
              )
            }
          }
        }

        // --- Difficulty Mode Segmented Button ---
        Column {
          Text(
            text = "Zorluk Seviyesi",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
          )
          val difficulties = DifficultyMode.entries
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            difficulties.forEachIndexed { index, mode ->
              val isSelected = difficulty == mode
              SegmentedButton(
                selected = isSelected,
                onClick = { onSelectDifficulty(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = difficulties.size),
                icon = { SegmentedButtonDefaults.Icon(active = isSelected) },
                label = { Text(mode.displayName, fontSize = 12.sp) },
              )
            }
          }
        }

        // Mode explanation
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "${difficulty.description} (Çarpan: x${difficulty.multiplier})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(10.dp),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        modifier = Modifier.testTag("dialog_confirm_new_game"),
      ) {
        Text("New Game", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Back")
      }
    },
  )
}

/**
 * Clean Material 3 Coming Soon Card for unfinished game modes (Spider, FreeCell, Pyramid).
 */
@Composable
private fun ComingSoonPlaceholder(
  gameType: GameType,
  modifier: Modifier = Modifier,
) {
  OutlinedCard(
    shape = MaterialTheme.shapes.extraLarge,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ),
    modifier = modifier.testTag("coming_soon_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = Icons.Outlined.Construction,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(44.dp),
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = gameType.displayName,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = gameType.subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(8.dp))

      SuggestionChip(
        onClick = { },
        label = { Text("Yakında", fontWeight = FontWeight.SemiBold) },
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "${gameType.description}\n\nBu mod yakında eklenecektir. Şu anda klasik Klondike modunu oynayabilirsiniz.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun SelectedGameDetails(
  gameType: GameType,
  stats: GameStats,
  modifier: Modifier = Modifier,
) {
  OutlinedCard(
    modifier = modifier.testTag("game_details_card"),
    shape = MaterialTheme.shapes.extraLarge,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = gameType.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = gameType.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = gameType.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(20.dp))

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        StatItem(
          label = stringResource(R.string.stats_played),
          value = stats.played.toString(),
          icon = Icons.Outlined.QueryStats,
        )
        StatItem(
          label = stringResource(R.string.stats_won),
          value = stats.won.toString(),
          icon = Icons.Outlined.EmojiEvents,
        )
        StatItem(
          label = stringResource(R.string.stats_win_ratio),
          value = "%${stats.winRatePercentage}",
          icon = Icons.Filled.Star,
        )
      }
    }
  }
}

@Composable
private fun StatItem(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

// -------------------------------------------------------------
// Previews: Phone, Phone Landscape, Foldable, Tablet (1.0x & 2.0x fontScale)
// -------------------------------------------------------------

@Preview(name = "Phone - Font 1.0x", device = "spec:width=411dp,height=891dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Phone - Font 2.0x", device = "spec:width=411dp,height=891dp", fontScale = 2.0f, showBackground = true)
@Preview(name = "Landscape - Font 1.0x", device = "spec:width=891dp,height=411dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Landscape - Font 2.0x", device = "spec:width=891dp,height=411dp", fontScale = 2.0f, showBackground = true)
@Preview(name = "Foldable - Font 1.0x", device = "spec:width=673dp,height=841dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Foldable - Font 2.0x", device = "spec:width=673dp,height=841dp", fontScale = 2.0f, showBackground = true)
@Preview(name = "Tablet - Font 1.0x", device = "spec:width=1280dp,height=800dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Tablet - Font 2.0x", device = "spec:width=1280dp,height=800dp", fontScale = 2.0f, showBackground = true)
@Composable
private fun HomeScreenPreview() {
  SolitaireTheme {
    HomeScreen(
      uiState = HomeUiState(
        selectedGameType = GameType.KLONDIKE,
        savedKlondikeGame = SavedGameEntity(
          gameType = "KLONDIKE",
          difficulty = "REGULAR",
          drawMode = "DRAW_ONE",
          moveCount = 1,
          elapsedSeconds = 10,
          stateJson = "",
          undoStackJson = "",
        ),
      ),
      onSelectGameType = {},
      onOpenSettings = {},
      onNewGameClicked = {},
      onConfirmOverwrite = {},
      onDismissOverwrite = {},
      onDismissNewGameDialog = {},
      onSetDialogDifficulty = {},
      onSetDialogDrawMode = {},
      onLaunchNewGame = { _, _ -> },
      onResumeGame = {},
    )
  }
}
