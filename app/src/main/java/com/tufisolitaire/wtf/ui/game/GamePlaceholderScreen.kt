package com.tufisolitaire.wtf.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tufisolitaire.wtf.R
import com.tufisolitaire.wtf.game.GameType
import com.tufisolitaire.wtf.ui.theme.SolitaireTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlaceholderScreen(
  gameTypeKey: String?,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onNavigateBack)

  val gameType = GameType.entries.find { it.name.equals(gameTypeKey, ignoreCase = true) } ?: GameType.KLONDIKE

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = gameType.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_back),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.testTag("game_top_bar"),
      )
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 640.dp)
          .padding(24.dp),
      ) {
        ElevatedCard(
          shape = MaterialTheme.shapes.extraLarge,
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("game_table_placeholder_card"),
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
          ) {
            // Preview of card outlines indicating card slot positions
            Row(
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth(),
            ) {
              repeat(4) {
                Box(
                  modifier = Modifier
                    .size(width = 44.dp, height = 62.dp)
                    .padding(horizontal = 4.dp)
                    .border(
                      width = 1.dp,
                      color = MaterialTheme.colorScheme.outlineVariant,
                      shape = MaterialTheme.shapes.small,
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Style,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline,
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Icon(
              imageVector = Icons.Outlined.DashboardCustomize,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = stringResource(R.string.game_ready_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = stringResource(R.string.game_ready_desc),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("return_home_button"),
              ) {
                Text(stringResource(R.string.action_back))
              }

              FilledTonalButton(
                onClick = { /* Ready for game start in next phase */ },
                modifier = Modifier.testTag("ready_button"),
              ) {
                Text("Hazır")
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Previews: Phone, Phone Landscape, Foldable, Tablet (1.0x & 2.0x fontScale)
// -------------------------------------------------------------

@Preview(name = "Placeholder Phone - Font 1.0x", device = "spec:width=411dp,height=891dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Placeholder Phone - Font 2.0x", device = "spec:width=411dp,height=891dp", fontScale = 2.0f, showBackground = true)
@Preview(name = "Placeholder Landscape - Font 1.0x", device = "spec:width=891dp,height=411dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Placeholder Landscape - Font 2.0x", device = "spec:width=891dp,height=411dp", fontScale = 2.0f, showBackground = true)
@Preview(name = "Placeholder Foldable - Font 1.0x", device = "spec:width=673dp,height=841dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Placeholder Foldable - Font 2.0x", device = "spec:width=673dp,height=841dp", fontScale = 2.0f, showBackground = true)
@Preview(name = "Placeholder Tablet - Font 1.0x", device = "spec:width=1280dp,height=800dp", fontScale = 1.0f, showBackground = true)
@Preview(name = "Placeholder Tablet - Font 2.0x", device = "spec:width=1280dp,height=800dp", fontScale = 2.0f, showBackground = true)
@Composable
private fun GamePlaceholderScreenPreview() {
  SolitaireTheme {
    GamePlaceholderScreen(
      gameTypeKey = "SPIDER",
      onNavigateBack = {},
    )
  }
}
