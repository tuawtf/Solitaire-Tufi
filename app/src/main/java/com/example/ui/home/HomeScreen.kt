package com.example.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.game.GameStats
import com.example.game.GameType

@Composable
fun HomeRoute(
  viewModel: HomeViewModel,
  onStartGame: (GameType) -> Unit,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  HomeScreen(
    uiState = uiState,
    onSelectGameType = viewModel::selectGameType,
    onToggleFavorite = viewModel::toggleFavorite,
    onStartGame = onStartGame,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: HomeUiState,
  onSelectGameType: (GameType) -> Unit,
  onToggleFavorite: () -> Unit,
  onStartGame: (GameType) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

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
            onClick = onToggleFavorite,
            modifier = Modifier.testTag("favorite_button"),
          ) {
            Icon(
              imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
              contentDescription = stringResource(R.string.action_favorite),
              tint = if (uiState.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState),
    ) {
      // Big "New Game" FilledTonalButton
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp),
      ) {
        FilledTonalButton(
          onClick = { onStartGame(uiState.selectedGameType) },
          shape = MaterialTheme.shapes.large,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .testTag("new_game_button"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(28.dp),
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

      // Game Tabs (Klondike, Spider, FreeCell, Pyramid) with stats per tab
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
                  text = stats.formattedRatio,
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

      // Selected Game Mode Detail & Statistics Card
      SelectedGameDetails(
        gameType = uiState.selectedGameType,
        stats = uiState.currentStats,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
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
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
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

      // Stats row
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
