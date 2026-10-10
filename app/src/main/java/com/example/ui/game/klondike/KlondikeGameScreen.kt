package com.example.ui.game.klondike

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
  difficulty: DifficultyMode = DifficultyMode.REGULAR,
  drawMode: DrawMode = DrawMode.DRAW_ONE,
  resume: Boolean = false,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(difficulty, drawMode, resume) {
    viewModel.initializeGame(difficulty, drawMode, resume)
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {
          viewModel.resumeTimer()
        }
        Lifecycle.Event.ON_PAUSE,
        Lifecycle.Event.ON_STOP -> {
          viewModel.pauseTimerAndSave()
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      viewModel.pauseTimerAndSave()
    }
  }

  val gameState by viewModel.gameState.collectAsStateWithLifecycle()
  val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
  val currentHint by viewModel.currentHint.collectAsStateWithLifecycle()
  val isAutoCompleting by viewModel.isAutoCompleting.collectAsStateWithLifecycle()
  val showResultSheet by viewModel.showResultSheet.collectAsStateWithLifecycle()
  val isCelebrating by viewModel.isCelebrating.collectAsStateWithLifecycle()
  val isNewHighScore by viewModel.isNewHighScore.collectAsStateWithLifecycle()
  val modeStats by viewModel.currentModeStats.collectAsStateWithLifecycle()

  KlondikeGameScreen(
    gameState = gameState,
    canUndo = canUndo,
    currentHint = currentHint,
    isAutoCompleting = isAutoCompleting,
    showResultSheet = showResultSheet,
    isCelebrating = isCelebrating,
    isNewHighScore = isNewHighScore,
    modeStats = modeStats,
    onDrawFromStock = viewModel::drawFromStock,
    onUndo = viewModel::undo,
    onRestartGame = viewModel::restartGame,
    onStartNewGame = { viewModel.startNewGame(difficulty, drawMode) },
    onGiveUp = viewModel::giveUpGame,
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
    onExitSaveAndNavigateBack = {
      viewModel.pauseTimerAndSave()
      onNavigateBack()
    },
    onNavigateBack = {
      viewModel.pauseTimerAndSave()
      onNavigateBack()
    },
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
  isCelebrating: Boolean = false,
  isNewHighScore: Boolean,
  modeStats: GameModeStatsEntity?,
  onDrawFromStock: () -> Boolean,
  onUndo: () -> Boolean,
  onRestartGame: () -> Unit,
  onStartNewGame: () -> Unit,
  onGiveUp: () -> Unit,
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
  onExitSaveAndNavigateBack: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showGiveUpDialog by remember { mutableStateOf(false) }
  var showExitConfirmDialog by remember { mutableStateOf(false) }

  val handleBackRequest = {
    if (gameState.moveCount > 0 && !gameState.isWon) {
      showExitConfirmDialog = true
    } else {
      onNavigateBack()
    }
  }

  BackHandler(onBack = handleBackRequest)

  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  // Drag and Drop Controller and State
  val controller = remember { DragDropController() }
  var draggedData by remember { mutableStateOf<DraggedCardData?>(null) }
  var fingerRootPosition by remember { mutableStateOf(Offset.Zero) }
  var startDragRootPosition by remember { mutableStateOf(Offset.Zero) }
  var isSnapBackAnimating by remember { mutableStateOf(false) }
  var boardOriginRoot by remember { mutableStateOf(Offset.Zero) }

  val handleTableauCardTap: (Int, Int) -> Unit = { colIndex, cardIndex ->
    val col = gameState.tableau.getOrNull(colIndex)
    if (col != null && cardIndex in col.indices) {
      val card = col[cardIndex]
      if (card.isFaceUp) {
        val isLastCardInCol = cardIndex == col.lastIndex
        var moved = false

        // 1. If it's the last card in the column, try moving to foundation first
        if (isLastCardInCol) {
          val foundationPile = gameState.foundations[card.suit] ?: emptyList()
          if (card.canStackOnFoundation(foundationPile.lastOrNull())) {
            moved = onMoveTableauToFoundation(colIndex)
          }
        }

        // 2. If not moved to foundation, try moving to another tableau column
        if (!moved) {
          // Try non-empty target columns first
          for (targetCol in gameState.tableau.indices) {
            if (targetCol == colIndex) continue
            val targetCards = gameState.tableau[targetCol]
            if (targetCards.isNotEmpty()) {
              val targetTop = targetCards.last()
              if (card.canStackOnTableau(targetTop)) {
                moved = onMoveTableauToTableau(colIndex, cardIndex, targetCol)
                if (moved) break
              }
            }
          }

          // If still not moved and card is a KING, only move to empty column if NOT already at index 0
          // (moving a King that is already the root card of a column to an empty column is a meaningless move)
          if (!moved && card.rank == com.example.game.CardRank.KING && cardIndex > 0) {
            for (targetCol in gameState.tableau.indices) {
              if (targetCol == colIndex) continue
              val targetCards = gameState.tableau[targetCol]
              if (targetCards.isEmpty()) {
                moved = onMoveTableauToTableau(colIndex, cardIndex, targetCol)
                if (moved) break
              }
            }
          }
        }

        if (moved) {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
      }
    }
  }

  val handleEndDrag: () -> Unit = {
    if (!isSnapBackAnimating) {
      val currentDrag = draggedData
      if (currentDrag != null) {
        val dropTarget = controller.findDropTarget(fingerRootPosition)

        val isSameTableauColumn = currentDrag.source is DragSource.Tableau &&
          dropTarget is DropTarget.Tableau &&
          currentDrag.source.columnIndex == dropTarget.columnIndex

        val moveSuccessful = if (isSameTableauColumn) {
          false
        } else {
          executeDrop(
            draggedData = currentDrag,
            dropTarget = dropTarget,
            onMoveWasteToFoundation = onMoveWasteToFoundation,
            onMoveWasteToTableau = onMoveWasteToTableau,
            onMoveTableauToFoundation = onMoveTableauToFoundation,
            onMoveTableauToTableau = onMoveTableauToTableau,
            onMoveFoundationToTableau = onMoveFoundationToTableau,
            haptic = haptic,
          )
        }

        if (moveSuccessful) {
          draggedData = null
        } else {
          // Smooth snap back animation to original position
          coroutineScope.launch {
            isSnapBackAnimating = true
            val anim = Animatable(fingerRootPosition, Offset.VectorConverter)
            anim.animateTo(
              targetValue = startDragRootPosition,
              animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            ) {
              fingerRootPosition = value
            }
            draggedData = null
            isSnapBackAnimating = false
          }
        }
      }
    }
  }

  val handleCancelDrag: () -> Unit = {
    if (!isSnapBackAnimating && draggedData != null) {
      coroutineScope.launch {
        isSnapBackAnimating = true
        val anim = Animatable(fingerRootPosition, Offset.VectorConverter)
        anim.animateTo(
          targetValue = startDragRootPosition,
          animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        ) {
          fingerRootPosition = value
        }
        draggedData = null
        isSnapBackAnimating = false
      }
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  val minutes = gameState.elapsedSeconds / 60
  val seconds = gameState.elapsedSeconds % 60
  val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.title_klondike),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = handleBackRequest,
            modifier = Modifier.testTag("back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_back),
            )
          }
        },
        actions = {
          Row(
            modifier = Modifier.padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = formattedTime,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
              ),
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
              text = "Moves: ${gameState.moveCount}",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
              ),
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.testTag("klondike_top_bar"),
      )
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) {
      // --- ACTION BUTTONS: Undo, Hint, Auto-Complete, Give Up (Equal weights) ---
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
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
          Text(undoLabel, fontSize = 11.sp, maxLines = 1)
        }

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
            Text(stringResource(R.string.action_hint), fontSize = 11.sp, maxLines = 1)
          }
        }

        if (gameState.canAutoComplete) {
          FilledTonalButton(
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
              text = if (isAutoCompleting) "..." else stringResource(R.string.action_auto_complete),
              fontSize = 11.sp,
              maxLines = 1,
            )
          }
        }

        FilledTonalButton(
          onClick = { showGiveUpDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("give_up_button"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(stringResource(R.string.action_give_up), fontSize = 11.sp, maxLines = 1)
        }
      }

      // --- MODE & DRAW INFO LABEL (Non-clickable) ---
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = MaterialTheme.shapes.extraSmall,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ) {
          Text(
            text = "${gameState.difficulty.displayName} · ${if (gameState.drawMode == DrawMode.DRAW_ONE) "Draw 1" else "Draw 3"}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }
      }

      // --- HINT BANNER ---
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
              .padding(horizontal = 10.dp, vertical = 2.dp),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
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
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = currentHint.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                  fontWeight = FontWeight.Medium,
                )
              }
              IconButton(
                onClick = onClearHint,
                modifier = Modifier.size(22.dp),
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

      // --- BOARD CONTAINER: Takes remaining area with weight(1f), subtle vertical gradient ---
      val boardGradient = Brush.verticalGradient(
        colors = listOf(
          MaterialTheme.colorScheme.surface,
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f),
        ),
      )

      BoxWithConstraints(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(boardGradient)
          .padding(horizontal = 4.dp)
          .onGloballyPositioned { coords ->
            boardOriginRoot = coords.positionInRoot()
          },
      ) {
        val configuration = LocalConfiguration.current
        val isExpandedLayout = configuration.screenWidthDp >= 600
        val spacing = 4.dp
        val maxAllowedCardWidth = if (isExpandedLayout) 84.dp else 72.dp
        val cardWidth = ((maxWidth - (spacing * 6)) / 7).coerceAtMost(maxAllowedCardWidth)
        val cardHeight = cardWidth * 1.4f
        val tableauHeight = (maxHeight - cardHeight - 12.dp).coerceAtLeast(100.dp)

        val draggedCardIds = remember(draggedData) {
          draggedData?.cards?.map { it.id }?.toSet() ?: emptySet()
        }

        Box(modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.fillMaxSize()) {
            // --- TOP ROW: 7 equal cells aligned with 7 tableau columns ---
            TopBoardRow(
              gameState = gameState,
              cardWidth = cardWidth,
              cardHeight = cardHeight,
              spacing = spacing,
              controller = controller,
              draggedCardIds = draggedCardIds,
              draggedData = draggedData,
              currentHint = currentHint,
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
              onStartDragWaste = { card, grabOffset, startRootPos ->
                if (!isSnapBackAnimating) {
                  startDragRootPosition = startRootPos
                  draggedData = DraggedCardData(
                    cards = listOf(card),
                    source = DragSource.Waste,
                    grabOffset = grabOffset,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                  )
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
              },
              onUpdateDragPosition = { rootPos ->
                if (!isSnapBackAnimating) {
                  fingerRootPosition = rootPos
                }
              },
              onEndDrag = handleEndDrag,
              onCancelDrag = handleCancelDrag,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- BOTTOM ROW: 7 Tableau columns with full height boxes ---
            TableauRow(
              tableau = gameState.tableau,
              cardWidth = cardWidth,
              cardHeight = cardHeight,
              tableauHeight = tableauHeight,
              spacing = spacing,
              controller = controller,
              draggedCardIds = draggedCardIds,
              draggedData = draggedData,
              currentHint = currentHint,
              onCardTap = handleTableauCardTap,
              onStartDragTableau = { colIndex, cardIndex, cards, grabOffset, startRootPos ->
                if (!isSnapBackAnimating) {
                  startDragRootPosition = startRootPos
                  draggedData = DraggedCardData(
                    cards = cards,
                    source = DragSource.Tableau(colIndex, cardIndex),
                    grabOffset = grabOffset,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                  )
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
              },
              onUpdateDragPosition = { rootPos ->
                if (!isSnapBackAnimating) {
                  fingerRootPosition = rootPos
                }
              },
              onEndDrag = handleEndDrag,
              onCancelDrag = handleCancelDrag,
            )
          }

          // --- VISUAL OVERLAY: NO pointerInput, only renders floating cards with scale 1.05 and deep shadow ---
          val activeDrag = draggedData
          if (activeDrag != null) {
            val dragCards = activeDrag.cards
            val cardW = activeDrag.cardWidth
            val cardH = activeDrag.cardHeight
            val verticalSpacing = cardH * 0.32f

            Box(
              modifier = Modifier
                .offset {
                  val x = (fingerRootPosition.x - activeDrag.grabOffset.x - boardOriginRoot.x).roundToInt()
                  val y = (fingerRootPosition.y - activeDrag.grabOffset.y - boardOriginRoot.y).roundToInt()
                  IntOffset(x, y)
                }
                .scale(if (isSnapBackAnimating) 1.0f else 1.05f)
                .shadow(if (isSnapBackAnimating) 6.dp else 16.dp, MaterialTheme.shapes.extraSmall),
            ) {
              Column {
                dragCards.forEachIndexed { idx, card ->
                  Box(
                    modifier = Modifier.offset(y = if (idx > 0) -verticalSpacing * 0.3f else 0.dp),
                  ) {
                    CardView(
                      card = card,
                      width = cardW,
                      height = cardH,
                      elevation = 8.dp,
                      isHighlighted = true,
                    )
                  }
                }
              }
            }
          }

          // --- VICTORY CELEBRATION ANIMATION OVERLAY ---
          if (isCelebrating) {
            VictoryCelebrationOverlay(
              cardWidth = cardWidth,
              cardHeight = cardHeight,
            )
          }
        }
      }

      // --- NO MOVES REMAINING NOTICE ---
      AnimatedVisibility(
        visible = gameState.hasNoMovesRemaining && !gameState.isWon,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        ElevatedCard(
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
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
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = stringResource(R.string.dialog_no_moves_message),
                style = MaterialTheme.typography.bodySmall,
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

  // --- EXIT CONFIRMATION DIALOG ---
  if (showExitConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showExitConfirmDialog = false },
      title = {
        Text("Oyundan Çıkış", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("Oyundan çıkmak istiyor musunuz? Oyun kaydedilecek ve Ana Menü'den Devam Et ile sürdürebilirsiniz.")
      },
      confirmButton = {
        Button(
          onClick = {
            showExitConfirmDialog = false
            onExitSaveAndNavigateBack()
          },
        ) {
          Text("Çık")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExitConfirmDialog = false }) {
          Text("Vazgeç")
        }
      },
    )
  }

  // --- GIVE UP CONFIRMATION DIALOG ---
  if (showGiveUpDialog) {
    AlertDialog(
      onDismissRequest = { showGiveUpDialog = false },
      title = {
        Text(stringResource(R.string.dialog_give_up_title), fontWeight = FontWeight.Bold)
      },
      text = {
        Text(stringResource(R.string.dialog_give_up_message))
      },
      confirmButton = {
        Button(
          onClick = {
            showGiveUpDialog = false
            onGiveUp()
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
              onGiveUp()
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

  // --- END GAME RESULT MODAL BOTTOM SHEET ---
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

        Text(
          text = "${gameState.difficulty.displayName} • ${gameState.drawMode.name.replace("_", " ")} (x${gameState.difficulty.multiplier})",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

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
 * Top Board Row containing 7 cells aligned with tableau columns:
 * Cell 0: Stock, Cell 1: Waste, Cell 2: Empty, Cell 3..6: Foundations (♥, ♦, ♠, ♣)
 */
@Composable
private fun TopBoardRow(
  gameState: KlondikeGameState,
  cardWidth: Dp,
  cardHeight: Dp,
  spacing: Dp,
  controller: DragDropController,
  draggedCardIds: Set<String>,
  draggedData: DraggedCardData? = null,
  currentHint: HintInfo? = null,
  onDrawFromStock: () -> Unit,
  onWasteTap: () -> Unit,
  onStartDragWaste: (CardModel, Offset, Offset) -> Unit,
  onUpdateDragPosition: (Offset) -> Unit,
  onEndDrag: () -> Unit,
  onCancelDrag: () -> Unit,
) {
  val viewConfiguration = LocalViewConfiguration.current
  val currentOnDrawFromStock by rememberUpdatedState(onDrawFromStock)
  val currentOnWasteTap by rememberUpdatedState(onWasteTap)
  val currentOnStartDragWaste by rememberUpdatedState(onStartDragWaste)
  val currentOnUpdateDragPosition by rememberUpdatedState(onUpdateDragPosition)
  val currentOnEndDrag by rememberUpdatedState(onEndDrag)
  val currentOnCancelDrag by rememberUpdatedState(onCancelDrag)
  val currentTopWaste by rememberUpdatedState(gameState.topWasteCard)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(spacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Cell 0: Stock
    Box(
      modifier = Modifier
        .size(cardWidth, cardHeight)
        .testTag("stock_pile")
        .pointerInput(gameState.stock.size, gameState.waste.size) {
          detectTapGestures(onTap = { currentOnDrawFromStock() })
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
          FaceDownCard(width = cardWidth, height = cardHeight)
        }
      } else if (gameState.waste.isNotEmpty()) {
        EmptySlotView(width = cardWidth, height = cardHeight)
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

    // Cell 1: Waste - Supports fanning out up to 3 visible cards in Draw 3 mode
    val topWaste = gameState.topWasteCard
    Box(
      modifier = Modifier
        .size(cardWidth, cardHeight)
        .testTag("waste_pile"),
    ) {
      if (topWaste != null) {
        val wasteCards = gameState.waste
        val isDrawThree = gameState.drawMode == DrawMode.DRAW_THREE
        val visibleWasteCount = if (isDrawThree) minOf(3, wasteCards.size) else 1
        val visibleWasteCards = wasteCards.takeLast(visibleWasteCount)

        // Draw underneath cards as subtle horizontal fanned stack
        if (visibleWasteCards.size > 1) {
          val fanOffset = (cardWidth * 0.16f)
          visibleWasteCards.dropLast(1).forEachIndexed { index, card ->
            val xOffset = fanOffset * (index.toFloat() - (visibleWasteCards.size - 1).toFloat() * 0.5f)
            Box(modifier = Modifier.offset(x = xOffset)) {
              CardView(card = card, width = cardWidth, height = cardHeight)
            }
          }
        }

        var wasteCardRootPos by remember { mutableStateOf(Offset.Zero) }
        val isCardHidden = draggedCardIds.contains(topWaste.id)
        val isWasteHinted = currentHint?.card?.id == topWaste.id

        Box(
          modifier = Modifier
            .fillMaxSize()
            .alpha(if (isCardHidden) 0f else 1f)
            .onGloballyPositioned { wasteCardRootPos = it.positionInRoot() }
            .pointerInput(topWaste.id, gameState.waste.size) {
              detectTapAndDrag(
                touchSlop = viewConfiguration.touchSlop,
                onTap = { currentOnWasteTap() },
                onDragStart = { localOffset ->
                  val cardToDrag = currentTopWaste ?: topWaste
                  val startRootPos = wasteCardRootPos + localOffset
                  currentOnStartDragWaste(cardToDrag, localOffset, startRootPos)
                  currentOnUpdateDragPosition(startRootPos)
                },
                onDrag = { _, currentLocalPos ->
                  currentOnUpdateDragPosition(wasteCardRootPos + currentLocalPos)
                },
                onDragEnd = { currentOnEndDrag() },
                onDragCancel = { currentOnCancelDrag() },
              )
            },
        ) {
          CardView(
            card = topWaste,
            width = cardWidth,
            height = cardHeight,
            isHighlighted = isWasteHinted,
          )
        }
      } else {
        EmptySlotView(width = cardWidth, height = cardHeight)
      }
    }

    // Cell 2: Empty Spacer
    Spacer(modifier = Modifier.size(cardWidth, cardHeight))

    // Cells 3 to 6: 4 Foundations in order: ♥, ♦, ♠, ♣
    val foundationSuits = listOf(Suit.HEARTS, Suit.DIAMONDS, Suit.SPADES, Suit.CLUBS)
    foundationSuits.forEach { suit ->
      val pile = gameState.foundations[suit] ?: emptyList()
      val topCard = pile.lastOrNull()
      val isValidTarget = controller.isValidFoundationTarget(draggedData, suit, pile)

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
            isHighlighted = isValidTarget,
          )
        } else {
          EmptySlotView(
            width = cardWidth,
            height = cardHeight,
            suit = suit,
            isTargetHighlight = isValidTarget,
          )
        }
      }
    }
  }
}

/**
 * Bottom Tableau Row containing 7 columns. Each column Box spans the full tableauHeight.
 */
@Composable
private fun TableauRow(
  tableau: List<List<CardModel>>,
  cardWidth: Dp,
  cardHeight: Dp,
  tableauHeight: Dp,
  spacing: Dp,
  controller: DragDropController,
  draggedCardIds: Set<String>,
  draggedData: DraggedCardData? = null,
  currentHint: HintInfo? = null,
  onCardTap: (Int, Int) -> Unit,
  onStartDragTableau: (Int, Int, List<CardModel>, Offset, Offset) -> Unit,
  onUpdateDragPosition: (Offset) -> Unit,
  onEndDrag: () -> Unit,
  onCancelDrag: () -> Unit,
) {
  val viewConfiguration = LocalViewConfiguration.current
  val currentTableau by rememberUpdatedState(tableau)
  val currentOnCardTap by rememberUpdatedState(onCardTap)
  val currentOnStartDragTableau by rememberUpdatedState(onStartDragTableau)
  val currentOnUpdateDragPosition by rememberUpdatedState(onUpdateDragPosition)
  val currentOnEndDrag by rememberUpdatedState(onEndDrag)
  val currentOnCancelDrag by rememberUpdatedState(onCancelDrag)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(tableauHeight),
    horizontalArrangement = Arrangement.spacedBy(spacing),
  ) {
    tableau.forEachIndexed { colIndex, columnCards ->
      val isValidTarget = controller.isValidTableauTarget(draggedData, colIndex, columnCards)

      Box(
        modifier = Modifier
          .width(cardWidth)
          .height(tableauHeight)
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
            isTargetHighlight = isValidTarget,
          )
        } else {
          // Calculate spacing with automatic compression for tall stacks
          val faceDownCount = columnCards.count { !it.isFaceUp }
          val faceUpCount = columnCards.count { it.isFaceUp }
          val baseFaceDownStep = cardHeight * 0.22f
          val baseFaceUpStep = cardHeight * 0.36f

          val estimatedHeight = cardHeight + (baseFaceDownStep * faceDownCount.toFloat()) +
            (baseFaceUpStep * maxOf(0, faceUpCount - 1).toFloat())

          val scaleFactor = if (estimatedHeight > tableauHeight && estimatedHeight > cardHeight) {
            ((tableauHeight.value - cardHeight.value) / (estimatedHeight.value - cardHeight.value)).coerceIn(0.60f, 1.0f)
          } else {
            1.0f
          }

          val faceDownStep = baseFaceDownStep * scaleFactor
          val faceUpStep = baseFaceUpStep * scaleFactor

          var currentYOffset = 0.dp
          columnCards.forEachIndexed { cardIndex, card ->
            val cardY = currentYOffset
            var cardRootPos by remember { mutableStateOf(Offset.Zero) }
            val isHidden = draggedCardIds.contains(card.id)
            val isHinted = currentHint?.card?.id == card.id
            val isLastCardInCol = cardIndex == columnCards.lastIndex
            val isHighlighted = isHinted || (isValidTarget && isLastCardInCol)

            val currentCardIndex by rememberUpdatedState(cardIndex)
            val currentColIndex by rememberUpdatedState(colIndex)
            val currentCard by rememberUpdatedState(card)

            Box(
              modifier = Modifier
                .offset(y = cardY)
                .alpha(if (isHidden) 0f else 1f)
                .onGloballyPositioned { coords ->
                  cardRootPos = coords.positionInRoot()
                }
                .then(
                  if (card.isFaceUp) {
                    Modifier.pointerInput(card.id, cardIndex, columnCards.size) {
                      detectTapAndDrag(
                        touchSlop = viewConfiguration.touchSlop,
                        onTap = {
                          currentOnCardTap(currentColIndex, currentCardIndex)
                        },
                        onDragStart = { localOffset ->
                          val currentColCards = currentTableau.getOrNull(currentColIndex) ?: emptyList()
                          val movingCards = if (currentCardIndex in currentColCards.indices) {
                            currentColCards.drop(currentCardIndex)
                          } else {
                            listOf(currentCard)
                          }
                          val startRootPos = cardRootPos + localOffset
                          currentOnStartDragTableau(currentColIndex, currentCardIndex, movingCards, localOffset, startRootPos)
                          currentOnUpdateDragPosition(startRootPos)
                        },
                        onDrag = { _, currentLocalPos ->
                          currentOnUpdateDragPosition(cardRootPos + currentLocalPos)
                        },
                        onDragEnd = { currentOnEndDrag() },
                        onDragCancel = { currentOnCancelDrag() },
                      )
                    }
                  } else {
                    Modifier
                  },
                ),
            ) {
              CardView(
                card = card,
                width = cardWidth,
                height = cardHeight,
                isHighlighted = isHighlighted,
              )
            }

            val step = if (card.isFaceUp) faceUpStep else faceDownStep
            currentYOffset += step
          }
        }
      }
    }
  }
}

/**
 * Handles drop target detection and move execution.
 */
private fun executeDrop(
  draggedData: DraggedCardData?,
  dropTarget: DropTarget?,
  onMoveWasteToFoundation: () -> Boolean,
  onMoveWasteToTableau: (Int) -> Boolean,
  onMoveTableauToFoundation: (Int) -> Boolean,
  onMoveTableauToTableau: (Int, Int, Int) -> Boolean,
  onMoveFoundationToTableau: (Suit, Int) -> Boolean,
  haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
): Boolean {
  if (draggedData == null || dropTarget == null) return false
  var moveSuccessful = false

  when (val src = draggedData.source) {
    is DragSource.Waste -> {
      when (dropTarget) {
        is DropTarget.Foundation -> moveSuccessful = onMoveWasteToFoundation()
        is DropTarget.Tableau -> moveSuccessful = onMoveWasteToTableau(dropTarget.columnIndex)
      }
    }
    is DragSource.Tableau -> {
      when (dropTarget) {
        is DropTarget.Foundation -> {
          if (draggedData.cards.size == 1) {
            moveSuccessful = onMoveTableauToFoundation(src.columnIndex)
          }
        }
        is DropTarget.Tableau -> {
          if (src.columnIndex != dropTarget.columnIndex) {
            moveSuccessful = onMoveTableauToTableau(src.columnIndex, src.cardIndex, dropTarget.columnIndex)
          }
        }
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
  return moveSuccessful
}

/**
 * Custom single gesture detector: handles instant tap without delay, or drag when movement exceeds touch slop.
 */
private suspend fun PointerInputScope.detectTapAndDrag(
  touchSlop: Float,
  onTap: () -> Unit,
  onDragStart: (Offset) -> Unit,
  onDrag: (Offset, Offset) -> Unit,
  onDragEnd: () -> Unit,
  onDragCancel: () -> Unit,
) {
  val slopThreshold = maxOf(touchSlop, 12.dp.toPx())
  awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    val startPos = down.position
    var isDragging = false

    while (true) {
      val event = awaitPointerEvent()
      val change = event.changes.firstOrNull { it.id == down.id }

      if (change == null || !change.pressed) {
        if (isDragging) {
          onDragEnd()
        } else {
          onTap()
        }
        break
      } else {
        val delta = change.position - startPos
        if (!isDragging && delta.getDistance() > slopThreshold) {
          isDragging = true
          onDragStart(startPos)
        }
        if (isDragging) {
          change.consume()
          onDrag(delta, change.position)
        }
      }
    }
  }
}

/**
 * Delightful victory celebration animation: cards bounce enthusiastically from the foundations
 * across the screen before the result summary sheet is revealed.
 */
@Composable
private fun VictoryCelebrationOverlay(
  cardWidth: Dp,
  cardHeight: Dp,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "victory_bounce")
  val bounceOffset1 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -35f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "bounce1",
  )
  val bounceOffset2 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -45f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "bounce2",
  )
  val bounceOffset3 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -30f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "bounce3",
  )
  val bounceOffset4 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -50f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "bounce4",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.25f)),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.offset(y = bounceOffset1.dp)) {
        FaceDownCard(width = cardWidth, height = cardHeight, elevation = 12.dp)
      }
      Box(modifier = Modifier.offset(y = bounceOffset2.dp)) {
        CardView(
          card = CardModel(com.example.game.CardRank.KING, Suit.HEARTS, isFaceUp = true),
          width = cardWidth,
          height = cardHeight,
          elevation = 14.dp,
        )
      }
      Box(modifier = Modifier.offset(y = bounceOffset3.dp)) {
        CardView(
          card = CardModel(com.example.game.CardRank.ACE, Suit.SPADES, isFaceUp = true),
          width = cardWidth,
          height = cardHeight,
          elevation = 14.dp,
        )
      }
      Box(modifier = Modifier.offset(y = bounceOffset4.dp)) {
        FaceDownCard(width = cardWidth, height = cardHeight, elevation = 12.dp)
      }
    }
  }
}
