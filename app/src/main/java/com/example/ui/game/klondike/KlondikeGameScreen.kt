package com.example.ui.game.klondike

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.stats.GameModeStatsEntity
import com.example.game.CardModel
import com.example.game.Suit
import com.example.game.klondike.DifficultyMode
import com.example.game.klondike.DrawMode
import com.example.game.klondike.HintInfo
import com.example.game.klondike.KlondikeGameState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun KlondikeGameRoute(
  viewModel: KlondikeViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val gameState by viewModel.gameState.collectAsStateWithLifecycle()
  val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
  val currentHint by viewModel.currentHint.collectAsStateWithLifecycle()
  val isAutoCompleting by viewModel.isAutoCompleting.collectAsStateWithLifecycle()
  val showResultSheet by viewModel.showResultSheet.collectAsStateWithLifecycle()
  val isNewHighScore by viewModel.isNewHighScore.collectAsStateWithLifecycle()
  val modeStats by viewModel.currentModeStats.collectAsStateWithLifecycle()

  KlondikeGameScreen(
    gameState = gameState,
    canUndo = canUndo,
    currentHint = currentHint,
    isAutoCompleting = isAutoCompleting,
    showResultSheet = showResultSheet,
    isNewHighScore = isNewHighScore,
    modeStats = modeStats,
    onDrawFromStock = viewModel::drawFromStock,
    onUndo = viewModel::undo,
    onRestartGame = viewModel::restartGame,
    onStartNewGame = viewModel::startNewGame,
    onSetDifficulty = viewModel::setDifficulty,
    onSetDrawMode = viewModel::setDrawMode,
    onRequestHint = viewModel::requestHint,
    onClearHint = viewModel::clearHint,
    onStartAutoComplete = viewModel::startAutoComplete,
    onDismissResultSheet = viewModel::dismissResultSheet,
    onAutoPlayFromTableau = viewModel::autoPlayCardFromTableau,
    onAutoPlayFromWaste = viewModel::autoPlayCardFromWaste,
    onMoveWasteToTableau = viewModel::moveWasteToTableau,
    onMoveWasteToFoundation = viewModel::moveWasteToFoundation,
    onMoveTableauToFoundation = viewModel::moveTableauToFoundation,
    onMoveTableauToTableau = viewModel::moveTableauToTableau,
    onMoveFoundationToTableau = viewModel::moveFoundationToTableau,
    onNavigateBack = onNavigateBack,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KlondikeGameScreen(
  gameState: KlondikeGameState,
  canUndo: Boolean,
  currentHint: HintInfo?,
  isAutoCompleting: Boolean,
  showResultSheet: Boolean,
  isNewHighScore: Boolean,
  modeStats: GameModeStatsEntity?,
  onDrawFromStock: () -> Boolean,
  onUndo: () -> Boolean,
  onRestartGame: () -> Unit,
  onStartNewGame: () -> Unit,
  onSetDifficulty: (DifficultyMode) -> Unit,
  onSetDrawMode: (DrawMode) -> Unit,
  onRequestHint: () -> Unit,
  onClearHint: () -> Unit,
  onStartAutoComplete: () -> Unit,
  onDismissResultSheet: () -> Unit,
  onAutoPlayFromTableau: (Int) -> Boolean,
  onAutoPlayFromWaste: () -> Boolean,
  onMoveWasteToTableau: (Int) -> Boolean,
  onMoveWasteToFoundation: () -> Boolean,
  onMoveTableauToFoundation: (Int) -> Boolean,
  onMoveTableauToTableau: (Int, Int, Int) -> Boolean,
  onMoveFoundationToTableau: (Suit, Int) -> Boolean,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onNavigateBack)

  val haptic = LocalHapticFeedback.current
  var showGiveUpDialog by remember { mutableStateOf(false) }
  var showDifficultyDialog by remember { mutableStateOf(false) }

  val controller = remember { DragDropController() }
  var draggedData by remember { mutableStateOf<DraggedCardData?>(null) }
  var currentDragPosition by remember { mutableStateOf(Offset.Zero) }

  val minutes = gameState.elapsedSeconds / 60
  val seconds = gameState.elapsedSeconds % 60
  val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = stringResource(R.string.title_klondike),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            // Time Chip
            Surface(
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.Timer,
                  contentDescription = null,
                  modifier = Modifier.size(13.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = formattedTime,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            // Moves Chip
            Surface(
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Text(
                text = "Moves: ${gameState.moveCount}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              )
            }

            // Score Chip
            Surface(
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.primaryContainer,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.SportsScore,
                  contentDescription = null,
                  modifier = Modifier.size(13.dp),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = gameState.calculatedScore.toString(),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_back),
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.testTag("klondike_top_bar"),
      )
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        // --- MODE SELECTION CHIPS ROW (Difficulty & Draw Mode) ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Difficulty Selector Chip
          AssistChip(
            onClick = { showDifficultyDialog = true },
            label = {
              Text(
                text = "${gameState.difficulty.displayName} (x${gameState.difficulty.multiplier})",
                fontWeight = FontWeight.SemiBold,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            modifier = Modifier.testTag("difficulty_chip"),
          )

          // Draw Mode Toggle Chip
          AssistChip(
            onClick = {
              val nextDraw = if (gameState.drawMode == DrawMode.DRAW_ONE) DrawMode.DRAW_THREE else DrawMode.DRAW_ONE
              onSetDrawMode(nextDraw)
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            label = {
              Text(
                text = if (gameState.drawMode == DrawMode.DRAW_ONE) "Draw 1" else "Draw 3",
                fontWeight = FontWeight.Medium,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            modifier = Modifier.testTag("draw_mode_chip"),
          )
        }

        // --- ACTION BUTTONS: Undo (with limit in Hard), Hint, Give Up, Auto-Complete ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Undo Button (with remaining count for Hard mode)
          val undoLabel = if (gameState.undosRemaining != null) {
            "${stringResource(R.string.action_undo)} (${gameState.undosRemaining})"
          } else {
            stringResource(R.string.action_undo)
          }

          FilledTonalButton(
            onClick = {
              if (onUndo()) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
            },
            enabled = canUndo,
            modifier = Modifier
              .weight(1f)
              .testTag("undo_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Undo,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(undoLabel, fontSize = 12.sp, maxLines = 1)
          }

          // Hint Button (Only in Relaxed mode)
          if (gameState.difficulty.hasHints) {
            FilledTonalButton(
              onClick = {
                onRequestHint()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("hint_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.tertiary,
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(stringResource(R.string.action_hint), fontSize = 12.sp, maxLines = 1)
            }
          }

          // Auto Complete Button (Visible when all tableau cards are face-up)
          if (gameState.canAutoComplete) {
            Button(
              onClick = {
                onStartAutoComplete()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              },
              enabled = !isAutoCompleting,
              modifier = Modifier
                .weight(1f)
                .testTag("auto_complete_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isAutoCompleting) "Tamamlanıyor..." else stringResource(R.string.action_auto_complete),
                fontSize = 11.sp,
                maxLines = 1,
              )
            }
          }

          // Give Up Button
          OutlinedButton(
            onClick = { showGiveUpDialog = true },
            modifier = Modifier
              .weight(1f)
              .testTag("give_up_button"),
          ) {
            Text(stringResource(R.string.action_give_up), fontSize = 12.sp, maxLines = 1)
          }
        }

        // --- HINT BANNER (when user requests a hint) ---
        AnimatedVisibility(
          visible = currentHint != null,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          if (currentHint != null) {
            ElevatedCard(
              colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = currentHint.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium,
                  )
                }
                IconButton(
                  onClick = onClearHint,
                  modifier = Modifier.size(24.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Kapat",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- BOARD LAYOUT (Scaled responsively to screen width) ---
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        ) {
          val horizontalPadding = 8.dp
          val columnSpacing = 4.dp
          val cardWidth = ((maxWidth - (horizontalPadding * 2) - (columnSpacing * 6)) / 7)
            .coerceIn(38.dp, 64.dp)
          val cardHeight = cardWidth * 1.42f

          Column(
            modifier = Modifier.fillMaxWidth(),
          ) {
            // Top Board Row: Stock, Waste, Foundations
            TopBoardRow(
              gameState = gameState,
              cardWidth = cardWidth,
              cardHeight = cardHeight,
              controller = controller,
              onDrawFromStock = {
                if (onDrawFromStock()) {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
              },
              onWasteTap = {
                if (onAutoPlayFromWaste()) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
              },
              onStartDragWaste = { card, touchPos ->
                draggedData = DraggedCardData(
                  cards = listOf(card),
                  source = DragSource.Waste,
                  dragOffset = touchPos,
                  touchOffset = touchPos,
                )
                currentDragPosition = touchPos
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tableau Row: 7 Columns
            TableauRow(
              tableau = gameState.tableau,
              cardWidth = cardWidth,
              cardHeight = cardHeight,
              controller = controller,
              onCardTap = { colIndex ->
                if (onAutoPlayFromTableau(colIndex)) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
              },
              onCardDoubleTap = { colIndex ->
                if (onMoveTableauToFoundation(colIndex) || onAutoPlayFromTableau(colIndex)) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
              },
              onStartDrag = { colIndex, cardIndex, touchPos ->
                val col = gameState.tableau[colIndex]
                val moving = col.subList(cardIndex, col.size)
                draggedData = DraggedCardData(
                  cards = moving,
                  source = DragSource.Tableau(colIndex, cardIndex),
                  dragOffset = touchPos,
                  touchOffset = touchPos,
                )
                currentDragPosition = touchPos
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              },
            )

            Spacer(modifier = Modifier.height(32.dp))
          }
        }
      }

      // --- DRAGGING OVERLAY: Follows touch coordinates ---
      val activeDrag = draggedData
      if (activeDrag != null) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(activeDrag) {
              detectDragGestures(
                onDrag = { change, dragAmount ->
                  change.consume()
                  currentDragPosition += dragAmount
                },
                onDragEnd = {
                  val dropTarget = controller.findDropTarget(currentDragPosition)
                  var moveSuccessful = false

                  when (val src = activeDrag.source) {
                    is DragSource.Waste -> {
                      when (dropTarget) {
                        is DropTarget.Foundation -> {
                          moveSuccessful = onMoveWasteToFoundation()
                        }
                        is DropTarget.Tableau -> {
                          moveSuccessful = onMoveWasteToTableau(dropTarget.columnIndex)
                        }
                        null -> {}
                      }
                    }
                    is DragSource.Tableau -> {
                      when (dropTarget) {
                        is DropTarget.Foundation -> {
                          if (activeDrag.cards.size == 1) {
                            moveSuccessful = onMoveTableauToFoundation(src.columnIndex)
                          }
                        }
                        is DropTarget.Tableau -> {
                          moveSuccessful = onMoveTableauToTableau(
                            src.columnIndex,
                            src.cardIndex,
                            dropTarget.columnIndex,
                          )
                        }
                        null -> {}
                      }
                    }
                    is DragSource.Foundation -> {
                      if (dropTarget is DropTarget.Tableau) {
                        moveSuccessful = onMoveFoundationToTableau(src.suit, dropTarget.columnIndex)
                      }
                    }
                  }

                  if (moveSuccessful) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  } else {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  }

                  draggedData = null
                },
                onDragCancel = {
                  draggedData = null
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
              )
            },
        ) {
          val cardWidth = 52.dp
          val cardHeight = cardWidth * 1.42f
          val verticalSpacing = 24.dp

          Column(
            modifier = Modifier
              .offset {
                IntOffset(
                  x = (currentDragPosition.x - cardWidth.toPx() / 2).roundToInt(),
                  y = (currentDragPosition.y - cardHeight.toPx() / 2).roundToInt(),
                )
              }
              .shadow(10.dp, MaterialTheme.shapes.extraSmall),
          ) {
            activeDrag.cards.forEachIndexed { idx, card ->
              Box(
                modifier = Modifier.offset(y = if (idx > 0) -verticalSpacing else 0.dp),
              ) {
                CardView(
                  card = card,
                  width = cardWidth,
                  height = cardHeight,
                  elevation = 6.dp,
                  isHighlighted = true,
                )
              }
            }
          }
        }
      }

      // --- NO MOVES REMAINING NOTICE ---
      AnimatedVisibility(
        visible = gameState.hasNoMovesRemaining && !gameState.isWon,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(16.dp),
      ) {
        ElevatedCard(
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = stringResource(R.string.dialog_no_moves_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
            }
            TextButton(
              onClick = { onUndo() },
              enabled = canUndo,
            ) {
              Text(
                stringResource(R.string.action_undo),
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }

  // --- DIFFICULTY SELECTION DIALOG ---
  if (showDifficultyDialog) {
    AlertDialog(
      onDismissRequest = { showDifficultyDialog = false },
      title = {
        Text(
          text = "Zorluk Modu Seçin",
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DifficultyMode.entries.forEach { mode ->
            val isSelected = mode == gameState.difficulty
            Surface(
              shape = MaterialTheme.shapes.medium,
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              onClick = {
                showDifficultyDialog = false
                onSetDifficulty(mode)
              },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "${mode.displayName} (x${mode.multiplier})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                  )
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Filled.AutoAwesome,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = mode.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDifficultyDialog = false }) {
          Text(stringResource(R.string.dialog_cancel))
        }
      },
    )
  }

  // --- GIVE UP CONFIRMATION DIALOG ---
  if (showGiveUpDialog) {
    AlertDialog(
      onDismissRequest = { showGiveUpDialog = false },
      title = {
        Text(
          text = stringResource(R.string.dialog_give_up_title),
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Text(text = stringResource(R.string.dialog_give_up_message))
      },
      confirmButton = {
        Button(
          onClick = {
            showGiveUpDialog = false
            onRestartGame()
          },
        ) {
          Text(stringResource(R.string.dialog_new_game))
        }
      },
      dismissButton = {
        Row {
          TextButton(
            onClick = {
              showGiveUpDialog = false
              onNavigateBack()
            },
          ) {
            Text(stringResource(R.string.dialog_return_home))
          }
          Spacer(modifier = Modifier.width(8.dp))
          TextButton(
            onClick = { showGiveUpDialog = false },
          ) {
            Text(stringResource(R.string.dialog_cancel))
          }
        }
      },
    )
  }

  // --- END GAME RESULT SCREEN (ModalBottomSheet) ---
  if (showResultSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
      onDismissRequest = onDismissResultSheet,
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
          imageVector = Icons.Outlined.EmojiEvents,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(56.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = stringResource(R.string.result_title),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )

        if (isNewHighScore) {
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small,
          ) {
            Text(
              text = "🏆 ${stringResource(R.string.result_new_record)}",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode details header
        Text(
          text = "${gameState.difficulty.displayName} • ${gameState.drawMode.name.replace("_", " ")} (x${gameState.difficulty.multiplier})",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Match Performance Grid Card
        ElevatedCard(
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround,
            ) {
              ResultStatColumn(label = stringResource(R.string.score_title), value = gameState.calculatedScore.toString())
              ResultStatColumn(label = "Süre", value = formattedTime)
              ResultStatColumn(label = "Hamle", value = gameState.moveCount.toString())
            }

            if (modeStats != null) {
              Spacer(modifier = Modifier.height(16.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "Bu Modun Rekorları",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
              )

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
              ) {
                ResultStatColumn(label = "Kazanma", value = modeStats.formattedRatio)
                ResultStatColumn(label = "En Yüksek", value = modeStats.bestScore.toString())
                ResultStatColumn(
                  label = "En Hızlı",
                  value = String.format(Locale.getDefault(), "%02d:%02d", modeStats.bestTimeSeconds / 60, modeStats.bestTimeSeconds % 60),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = {
              onDismissResultSheet()
              onNavigateBack()
            },
            modifier = Modifier.weight(1f),
          ) {
            Text(stringResource(R.string.dialog_return_home))
          }

          Button(
            onClick = {
              onDismissResultSheet()
              onStartNewGame()
            },
            modifier = Modifier.weight(1f),
          ) {
            Text(stringResource(R.string.dialog_new_game))
          }
        }
      }
    }
  }
}

@Composable
private fun ResultStatColumn(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
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

/**
 * Top Board Row containing Stock, Waste, and 4 Foundation slots.
 */
@Composable
private fun TopBoardRow(
  gameState: KlondikeGameState,
  cardWidth: Dp,
  cardHeight: Dp,
  controller: DragDropController,
  onDrawFromStock: () -> Unit,
  onWasteTap: () -> Unit,
  onStartDragWaste: (CardModel, Offset) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left side: Stock & Waste
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Stock Pile
      Box(
        modifier = Modifier
          .size(cardWidth, cardHeight)
          .testTag("stock_pile")
          .pointerInput(gameState.stock.size) {
            detectTapGestures(
              onTap = { onDrawFromStock() },
            )
          },
      ) {
        if (gameState.stock.isNotEmpty()) {
          BadgedBox(
            badge = {
              Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
              ) {
                Text(gameState.stock.size.toString(), fontSize = 10.sp)
              }
            },
          ) {
            FaceDownCard(
              width = cardWidth,
              height = cardHeight,
            )
          }
        } else if (gameState.waste.isNotEmpty()) {
          EmptySlotView(
            width = cardWidth,
            height = cardHeight,
          )
          Icon(
            imageVector = Icons.Outlined.Refresh,
            contentDescription = "Geri Dönüştür",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .size(24.dp)
              .align(Alignment.Center),
          )
        } else {
          EmptySlotView(width = cardWidth, height = cardHeight)
        }
      }

      // Waste Pile
      Box(
        modifier = Modifier
          .size(cardWidth, cardHeight)
          .testTag("waste_pile"),
      ) {
        val topWaste = gameState.topWasteCard
        if (topWaste != null) {
          Box(
            modifier = Modifier
              .pointerInput(topWaste) {
                detectTapGestures(
                  onTap = { onWasteTap() },
                  onDoubleTap = { onWasteTap() },
                )
              }
              .pointerInput(topWaste) {
                detectDragGestures(
                  onDragStart = { offset ->
                    onStartDragWaste(topWaste, offset)
                  },
                  onDrag = { _, _ -> },
                )
              },
          ) {
            CardView(
              card = topWaste,
              width = cardWidth,
              height = cardHeight,
            )
          }
        } else {
          EmptySlotView(width = cardWidth, height = cardHeight)
        }
      }
    }

    // Right side: 4 Foundation Slots (♠, ♥, ♣, ♦)
    val suits = listOf(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS)
    Row(
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      suits.forEach { suit ->
        val pile = gameState.foundations[suit] ?: emptyList()
        val topCard = pile.lastOrNull()

        Box(
          modifier = Modifier
            .size(cardWidth, cardHeight)
            .testTag("foundation_${suit.name.lowercase()}")
            .onGloballyPositioned { coords ->
              controller.registerFoundationBounds(suit, coords.boundsInRoot())
            },
        ) {
          if (topCard != null) {
            CardView(
              card = topCard,
              width = cardWidth,
              height = cardHeight,
            )
          } else {
            EmptySlotView(
              width = cardWidth,
              height = cardHeight,
              suit = suit,
            )
          }
        }
      }
    }
  }
}

/**
 * Bottom Tableau Row containing 7 cascading columns.
 */
@Composable
private fun TableauRow(
  tableau: List<List<CardModel>>,
  cardWidth: Dp,
  cardHeight: Dp,
  controller: DragDropController,
  onCardTap: (Int) -> Unit,
  onCardDoubleTap: (Int) -> Unit,
  onStartDrag: (Int, Int, Offset) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    tableau.forEachIndexed { colIndex, columnCards ->
      Box(
        modifier = Modifier
          .width(cardWidth)
          .testTag("tableau_col_$colIndex")
          .onGloballyPositioned { coords ->
            controller.registerTableauBounds(colIndex, coords.boundsInRoot())
          },
      ) {
        if (columnCards.isEmpty()) {
          EmptySlotView(
            width = cardWidth,
            height = cardHeight,
            text = "K",
          )
        } else {
          var currentYOffset = 0.dp
          columnCards.forEachIndexed { cardIndex, card ->
            val cardY = currentYOffset

            Box(
              modifier = Modifier
                .offset(y = cardY)
                .pointerInput(card.id) {
                  if (card.isFaceUp) {
                    detectTapGestures(
                      onTap = { onCardTap(colIndex) },
                      onDoubleTap = { onCardDoubleTap(colIndex) },
                    )
                  }
                }
                .pointerInput(card.id) {
                  if (card.isFaceUp) {
                    detectDragGestures(
                      onDragStart = { offset ->
                        onStartDrag(colIndex, cardIndex, offset)
                      },
                      onDrag = { _, _ -> },
                    )
                  }
                },
            ) {
              CardView(
                card = card,
                width = cardWidth,
                height = cardHeight,
              )
            }

            val step = if (card.isFaceUp) (cardHeight * 0.35f) else (cardHeight * 0.22f)
            currentYOffset += step
          }
        }
      }
    }
  }
}
