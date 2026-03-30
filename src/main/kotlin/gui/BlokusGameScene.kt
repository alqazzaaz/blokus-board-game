package gui

import entity.Bot
import entity.Color
import entity.MultiControlledParticipant
import entity.NetworkParticipant
import entity.Participant
import entity.Piece
import service.ConnectionState
import service.RootService
import service.Refreshable
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * The main game scene for Blokus.
 *
 * Layout:
 * - Center: the game board (20x20 grid of colored cells)
 * - Left: control buttons (Bot Speed, Undo, Redo, Skip to End, Rotate, Flip)
 * - Right: Save Game, selected piece preview, Show Pieces
 * - Bottom: current player's remaining pieces (clickable to select)
 *
 * The board supports two display modes:
 * - Normal mode: large board with the current player's pieces below
 * - Overview mode: smaller board with ALL players' pieces listed below
 *
 * Piece placement flow:
 * 1. Click a piece in the bottom area to select it
 * 2. Hover over the board to see a preview (green = legal, red = illegal)
 * 3. Click on the board to place the piece
 * 4. Use R key or ROTATE button to rotate, M key or FLIP button to mirror
 * 5. Press ESC to deselect
 */
class BlokusGameScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {

    /** Cell size in pixels for the normal (large) board */
    private val normalCellSize = 34

    /** Y position of the board in normal mode */
    private val normalBoardY = 90

    /** Maximum number of cells in one board dimension */
    private var maxBoardSize = 20

    /** X position of the left button panel */
    private val leftPanelX = 50

    /** Width of buttons */
    private val buttonWidth = 180

    /** Height of standard buttons */
    private val buttonHeight = 55

    /** Cell size for the current player's piece list at the bottom */
    private val previewCellSize = 22

    /** Maximum pieces per row in the bottom piece list */
    private val maxPiecesPerRow = 11

    /** Current cell size (changes between normal and overview mode) */
    private val cellSize = normalCellSize

    /** Current cell step (cell size + 1px grid line) */
    private var cellStep = cellSize + 1

    /** Current board size in pixels */
    private var boardPixels = maxBoardSize * cellStep - 1

    /** Current Y position of the board */
    private val boardY = normalBoardY

    /** Current left edge X of the board */
    private var boardLeftX = 960 - boardPixels / 2

    /** Current right edge X of the board */
    private var boardRightX = boardLeftX + boardPixels

    /** X position of the right panel (to the right of the board) */
    private var rightPanelX = boardRightX + 120

    /** Y position of the piece area below the board */
    private var pieceAreaY = boardY + boardPixels + 20

    /** Height of one player card */
    private val playerCardHeight = 160

    /** Gap between player cards */
    private val playerCardGap = 10

    /** Width of the player panel */
    private val playerPanelWidth = 340

    /** The pane containing all player cards */
    private val playerListPane = Pane<ComponentView>(
        posX = 0, posY = 0, width = playerPanelWidth, height = 600
    )

    /** The pane showing a selected player's pieces on the right */
    private val playerPiecesPane = Pane<ComponentView>(
        posX = 0, posY = 0, width = playerPanelWidth, height = 400
    )

    /** Index of the player whose pieces are currently shown (null = none) */
    private var inspectedPlayerIndex: Int? = null

    /** The currently selected piece, or null if no piece is selected */
    private var selectedPiece: Piece? = null

    /** The index of the selected piece in the current participant's piece list */
    private var selectedPieceIndex: Int = -1

    /** The board cell (row, col) the mouse is currently hovering over */
    private var hoverBoardCell: Pair<Int, Int>? = null

    /** List of preview TokenViews currently displayed on the board */
    private val previewTokens = mutableListOf<TokenView>()

    /** Invisible event-catcher cells. Always present on the board to handle mouse input. */
    private var eventGrid: Array<Array<TokenView>> = emptyArray()

    /** Colored cells for placed pieces. Null if the cell is empty. */
    private val placedCells = Array<Array<Pane<ComponentView>?>>(maxBoardSize) {
        arrayOfNulls(maxBoardSize)
    }

    private val sceneBackgroundColor = tools.aqua.bgw.core.Color(30, 30, 30)

    /**
     * The main board pane. Contains:
     * - Event grid cells (invisible, always present, handle mouse events)
     * - Colored cells for placed pieces (added/removed by [renderBoard])
     * - Preview cells (added/removed by [drawPreview] / [clearPreview])
     */
    private val boardPane = Pane<ComponentView>(
        posX = 0, posY = 0, width = 1, height = 1,
    ).apply {
        visual = ColorVisual(tools.aqua.bgw.core.Color.BLACK)
    }

    /** Shows the current player's name and color above the board */
    private val currentPlayerLabel = Label(
        posX = 0, posY = 0, width = 1, height = 36,
        text = "",
        font = Font(22, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.DARK_GRAY)
    )

    /** Shows status messages (e.g. "Legal move", "Illegal move") below the player label */
    private val statusLabel = Label(
        posX = 0, posY = 0, width = 1, height = 36,
        text = "",
        font = Font(20, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.DARK_GRAY)
    )

    private val botSpeedLabel = Label(
        posX = leftPanelX, posY = normalBoardY,
        width = buttonWidth, height = 30,
        text = "Bot Speed",
        font = Font(16, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.WHITE)
    )

    private val botSpeedValueLabel = Label(
        posX = leftPanelX, posY = normalBoardY + 28,
        width = buttonWidth, height = 30,
        text = "1s",
        font = Font(18, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.WHITE)
    )

    private val botSpeedDownButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 60,
        width = buttonWidth / 2 - 5, height = 40,
        text = "◀ Slower"
    ) { adjustBotSpeed(1) }

    private val botSpeedUpButton = styledButton(
        posX = leftPanelX + buttonWidth / 2 + 5, posY = normalBoardY + 60,
        width = buttonWidth / 2 - 5, height = 40,
        text = "Faster ▶"
    ) { adjustBotSpeed(-1) }

    val undoButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 140,
        text = "Undo"
    ) { rootService.playerActionService.undo() }
    val redoButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 210,
        text = "Redo"
    ) { rootService.playerActionService.redo() }

    val skipToEndButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 300,
        height = 70,
        text = "Skip\nto End"
    ) { rootService.gameService.score() }.apply {
        isDisabled = true
    }

    val rotateButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 430,
        text = "ROTATE (R)"
    ) {
        selectedPiece?.let {
            rootService.playerActionService.rotate(it)
            refreshPreview()
        }
    }

    val flipButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 500,
        text = "FLIP (M)"
    ) {
        selectedPiece?.let {
            rootService.playerActionService.flip(it)
            refreshPreview()
        }
    }

    val saveGameButton = styledButton(
        posX = leftPanelX, posY = normalBoardY + 850,
        text = "Save Game"
    ) {}

    /** Container for the current player's pieces displayed below the board */
    private val pieceDisplayPane = Pane<ComponentView>(
        posX = 0, posY = 0, width = 1, height = 1
    )

    init {
        background = ColorVisual(sceneBackgroundColor)

        // Calculate initial layout
        recalculateLayout()
        rebuildBoardPane()
        repositionLabels()
        repositionPieceDisplay()

        playerListPane.posX = (boardRightX + 250).toDouble()
        playerListPane.posY = boardY.toDouble()
        playerPiecesPane.posX = (boardRightX + 250).toDouble()
        playerPiecesPane.posY = (boardY + 600).toDouble()

        addComponents(
            boardPane,
            currentPlayerLabel,
            statusLabel,
            botSpeedLabel,
            botSpeedValueLabel,
            botSpeedDownButton,
            botSpeedUpButton,
            undoButton,
            redoButton,
            skipToEndButton,
            rotateButton,
            flipButton,
            saveGameButton,
            pieceDisplayPane,
            playerListPane,
            playerPiecesPane,
        )

        // Register keyboard shortcuts
        onKeyPressed = { event ->
            when (event.keyCode) {
                tools.aqua.bgw.event.KeyCode.R -> {
                    selectedPiece?.let {
                        rootService.playerActionService.rotate(it)
                        refreshPreview()
                    }
                }

                tools.aqua.bgw.event.KeyCode.M -> {
                    selectedPiece?.let {
                        rootService.playerActionService.flip(it)
                        refreshPreview()
                    }
                }

                tools.aqua.bgw.event.KeyCode.ESCAPE -> {
                    deselectPiece()
                }

                else -> {}
            }
        }
    }

    private fun styledButton(
        posX: Int, posY: Int,
        width: Int = buttonWidth, height: Int = buttonHeight,
        text: String,
        onClick: () -> Unit
    ): Button {
        val normalColor = tools.aqua.bgw.core.Color(55, 55, 65)
        val hoverColor = tools.aqua.bgw.core.Color(75, 75, 85)

        return Button(
            posX = posX, posY = posY,
            width = width, height = height,
            text = text,
            font = Font(18, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.WHITE),
            visual = ColorVisual(normalColor)
        ).apply {
            onMousePressed = { onClick() }
            onMouseEntered = { visual = ColorVisual(hoverColor) }
            onMouseExited = { visual = ColorVisual(normalColor) }
        }
    }

    /**
     * Renders the player list panel to the right of the board.
     * Each player gets a card showing their info.
     * Clicking a card shows that player's remaining pieces below the panel.
     */
    private fun renderPlayerList() {
        playerListPane.clear()

        val game = rootService.game ?: return
        val state = game.currentGameState
        val players = state.participantList
        val currentIndex = state.currentParticipantIndex

        playerListPane.posX = (boardRightX + 250).toDouble()
        playerListPane.posY = boardY.toDouble()
        playerListPane.width = playerPanelWidth.toDouble()
        playerListPane.height = ((playerCardHeight + playerCardGap) * players.size).toDouble()

        for ((index, participant) in players.withIndex()) {
            val isCurrent = (index == currentIndex)
            val isInspected = (index == inspectedPlayerIndex)
            val cardY = index * (playerCardHeight + playerCardGap)

            val cardColor = when {
                isCurrent -> tools.aqua.bgw.core.Color(55, 58, 75)
                isInspected -> tools.aqua.bgw.core.Color(48, 48, 60)
                else -> tools.aqua.bgw.core.Color(38, 38, 48)
            }
            val hoverColor = tools.aqua.bgw.core.Color(65, 65, 80)

            val cardPane = Pane<ComponentView>(
                posX = 0, posY = cardY,
                width = playerPanelWidth, height = playerCardHeight
            ).apply {
                visual = ColorVisual(cardColor)
                onMouseEntered = { visual = ColorVisual(hoverColor) }
                onMouseExited = { visual = ColorVisual(cardColor) }
                onMouseClicked = {
                    inspectedPlayerIndex = if (inspectedPlayerIndex == index) null else index
                    renderPlayerList()
                    renderInspectedPieces()
                }
            }

            // ── Left accent bar in player color ──
            val accentBar = TokenView(
                posX = 0, posY = 0,
                width = 5, height = playerCardHeight,
                visual = ColorVisual(faceColor(participant.color))
            ).apply { isDisabled = true }
            cardPane.addAll(accentBar)

            // ── Active arrow ──
            if (isCurrent) {
                val arrow = Label(
                    posX = 12, posY = 0,
                    width = 20, height = playerCardHeight,
                    text = "▶",
                    font = Font(14, color = tools.aqua.bgw.core.Color(100, 220, 130))
                )
                cardPane.addAll(arrow)
            }

            val contentX = if (isCurrent) 34 else 18

            // ── Color square ──
            val colorSquare = TokenView(
                posX = contentX, posY = 16,
                width = 22, height = 22,
                visual = ColorVisual(faceColor(participant.color))
            ).apply { isDisabled = true }
            cardPane.addAll(colorSquare)

            // ── Name ──
            val displayName = if (participant is MultiControlledParticipant)
                participant.controlledBy[0].name
            else
                participant.name

            val nameLabel = Label(
                posX = contentX + 30, posY = 12,
                width = playerPanelWidth - contentX - 60, height = 24,
                text = displayName,
                font = Font(16, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.WHITE),
                alignment = Alignment.CENTER_LEFT
            )
            cardPane.addAll(nameLabel)

            // ── Type badge ──
            val typeText = getParticipantType(participant)
            val typeColor = when {
                typeText.contains("Hard") -> tools.aqua.bgw.core.Color(240, 110, 110)
                typeText.contains("Easy") -> tools.aqua.bgw.core.Color(100, 210, 140)
                else -> tools.aqua.bgw.core.Color(150, 150, 170)
            }
            val typeLabel = Label(
                posX = contentX + 30, posY = 34,
                width = playerPanelWidth - contentX - 60, height = 18,
                text = typeText,
                font = Font(12, color = typeColor),
                alignment = Alignment.CENTER_LEFT
            )
            cardPane.addAll(typeLabel)

            // ── Pieces count ──
            val piecesCount = participant.pieceList.size
            val countLabel = Label(
                posX = contentX, posY = 62,
                width = playerPanelWidth - contentX - 40, height = 20,
                text = "$piecesCount pieces remaining",
                font = Font(13, fontWeight = Font.FontWeight.BOLD,
                    color = tools.aqua.bgw.core.Color(180, 180, 195)),
                alignment = Alignment.CENTER_LEFT
            )
            cardPane.addAll(countLabel)

            val moveStatusText: String
            val moveStatusColor: tools.aqua.bgw.core.Color

            if (!participant.actionAvailable) {
                moveStatusText = "⚠ No moves left"
                moveStatusColor = tools.aqua.bgw.core.Color(240, 110, 110)
            } else {
                moveStatusText = "● Can play"
                moveStatusColor = tools.aqua.bgw.core.Color(100, 210, 140)
            }

            val moveStatusLabel = Label(
                posX = contentX, posY = 80,
                width = playerPanelWidth - contentX - 40, height = 18,
                text = moveStatusText,
                font = Font(11, fontWeight = Font.FontWeight.BOLD, color = moveStatusColor),
                alignment = Alignment.CENTER_LEFT
            )
            cardPane.addAll(moveStatusLabel)

            val score = calculateScore(participant)
            val game2 = rootService.game ?: return
            val scoreDisplay = if (game2.isBasicScoring) {
                "$score squares left"
            } else {
                if (score >= 0) "+$score pts" else "$score pts"
            }

            val scoreColor = if (game2.isBasicScoring) {
                // Basic: lower is better → green for low, red for high
                when {
                    score <= 20 -> tools.aqua.bgw.core.Color(100, 210, 140)
                    score <= 50 -> tools.aqua.bgw.core.Color(250, 210, 75)
                    else -> tools.aqua.bgw.core.Color(240, 110, 110)
                }
            } else {
                // Advanced: higher is better
                when {
                    score > 0 -> tools.aqua.bgw.core.Color(100, 210, 140)
                    score == 0 -> tools.aqua.bgw.core.Color(200, 200, 210)
                    else -> tools.aqua.bgw.core.Color(240, 110, 110)
                }
            }

            val scoreLabel = Label(
                posX = contentX, posY = 96,
                width = playerPanelWidth - contentX - 40, height = 20,
                text = "Score: $scoreDisplay",
                font = Font(13, fontWeight = Font.FontWeight.BOLD, color = scoreColor),
                alignment = Alignment.CENTER_LEFT
            )
            cardPane.addAll(scoreLabel)

            // ── Pieces progress bar ──
            val maxPieces = 21
            val barWidth = playerPanelWidth - contentX - 40
            val filledWidth = ((piecesCount.toDouble() / maxPieces) * barWidth).toInt()

            val barBg = TokenView(
                posX = contentX, posY = 120,
                width = barWidth, height = 6,
                visual = ColorVisual(tools.aqua.bgw.core.Color(30, 30, 38))
            ).apply { isDisabled = true }

            cardPane.addAll(barBg)

            if (filledWidth > 0) {
                val barFill = TokenView(
                    posX = contentX, posY = 120,
                    width = filledWidth, height = 6,
                    visual = ColorVisual(faceColor(participant.color))
                ).apply { isDisabled = true }
                cardPane.addAll(barFill)
            }

            if (game2.isNetwork) {
                val netLabel = Label(
                    posX = playerPanelWidth - 70, posY = 10,
                    width = 60, height = 18,
                    text = "🌐 NET",
                    font = Font(10, color = tools.aqua.bgw.core.Color(100, 160, 230)),
                    alignment = Alignment.CENTER_RIGHT
                )
                cardPane.addAll(netLabel)
            }

            // ── Expand indicator (top right) ──
            val expandLabel = Label(
                posX = playerPanelWidth - 30, posY = 0,
                width = 24, height = playerCardHeight,
                text = if (isInspected) "▲" else "▼",
                font = Font(11, color = tools.aqua.bgw.core.Color(120, 120, 140))
            )
            cardPane.addAll(expandLabel)

            playerListPane.addAll(cardPane)
        }
    }

    /**
     * Returns a display string for the participant type.
     */
    private fun getParticipantType(participant: Participant): String {
        return when {
            participant is MultiControlledParticipant -> "MCP (" + participant.controlledBy[0].name + ")"
            participant is Bot && participant.isEasy-> "🤖 Easy Bot"
            participant is Bot && !participant.isEasy -> "🤖 Hard Bot"
            participant is NetworkParticipant -> "🌐 Network (Bot or Player)"
            else -> "\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uFE0F Player"
        }
    }

    /**
     * Renders the pieces of the inspected player below the player list panel.
     */
    private fun renderInspectedPieces() {
        playerPiecesPane.clear()

        val idx = inspectedPlayerIndex ?: return
        val game = rootService.game ?: return
        val state = game.currentGameState

        if (idx < 0 || idx >= state.participantList.size) return

        val participant = state.participantList[idx]
        val pieces = participant.pieceList
        val color = participant.color

        playerPiecesPane.posX = playerListPane.posX
        playerPiecesPane.posY = playerListPane.posY + playerListPane.height + 15
        playerPiecesPane.width = playerPanelWidth.toDouble()

        val cellSz = 12
        val gapX = 8
        val gapY = 6
        val pieceSlotWidth = 5 * cellSz + gapX   // max 5 cells wide + gap
        val cols = (playerPanelWidth / pieceSlotWidth).coerceAtLeast(1)
        val rowHeight = 5 * cellSz + gapY

        for ((i, piece) in pieces.withIndex()) {
            val gridCol = i % cols
            val gridRow = i / cols
            val baseX = gridCol * pieceSlotWidth
            val baseY = gridRow * rowHeight

            val figure = piece.figure
            for (row in figure.indices) {
                for (col in figure[row].indices) {
                    if (figure[row][col] == 1) {
                        playerPiecesPane.addAll(
                            createColoredCell(
                                x = baseX + col * cellSz,
                                y = baseY + row * cellSz,
                                size = cellSz,
                                color = color
                            )
                        )
                    }
                }
            }
        }

        val totalRows = (pieces.size + cols - 1) / cols
        playerPiecesPane.height = (totalRows * rowHeight).toDouble()
    }

    /**
     * Returns true if it is currently this client's turn in a network game,
     * or if the game is not a network game (offline = always allowed).
     */
    private fun isMyTurn(): Boolean {
        val game = rootService.game ?: return false
        if (!game.isNetwork) return true

        print(rootService.networkService.connectionState.name)

        return rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN
    }

    /**
     * Enables or disables action buttons and piece interaction based on
     * whether it is currently this client's turn in a network game.
     * In offline games, controls are always enabled.
     */
    private fun updateNetworkControls() {
        val game = rootService.game ?: return
        val state = game.currentGameState
        val currentParticipant = state.participantList[state.currentParticipantIndex]

        val isBotThinking = !game.isNetwork && currentParticipant is Bot

        val myTurn = if (game.isNetwork) {
            rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN
        } else {
            !isBotThinking
        }

        updateButtonState(rotateButton, myTurn) {
            selectedPiece?.let {
                rootService.playerActionService.rotate(it)
                refreshPreview()
            }
        }
        updateButtonState(flipButton, myTurn) {
            selectedPiece?.let {
                rootService.playerActionService.flip(it)
                refreshPreview()
            }
        }

        val check = game.currentGameState.participantList.size == 4
                && game.currentGameState.participantList[3] is MultiControlledParticipant
                && !game.currentGameState.participantList[0].actionAvailable
                && !game.currentGameState.participantList[1].actionAvailable
                && !game.currentGameState.participantList[2].actionAvailable

        if (check) {
            updateButtonState(skipToEndButton, true) { rootService.gameService.score() }
        } else {
            updateButtonState(skipToEndButton, false) {}
        }

        if (game.isNetwork) {
            updateButtonState(undoButton, false) { rootService.playerActionService.undo() }
            updateButtonState(redoButton, false) { rootService.playerActionService.redo() }
            updateButtonState(saveGameButton, false) {}
        } else {
            updateButtonState(undoButton, state.previousGameState != null)
            { rootService.playerActionService.undo() }
            updateButtonState(redoButton, state.nextGameState != null)
            { rootService.playerActionService.redo() }
            updateButtonState(saveGameButton, true) {
                val fileChooser = javax.swing.JFileChooser().apply {
                    dialogTitle = "Spielstand speichern"
                    fileSelectionMode = javax.swing.JFileChooser.FILES_ONLY
                    selectedFile = java.io.File("blokus_save.ser")
                    fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                        "Blokus Save (*.ser)", "ser"
                    )
                }
                val result = fileChooser.showSaveDialog(null)
                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    var file = fileChooser.selectedFile
                    if (!file.name.endsWith(".ser")) {
                        file = java.io.File(file.absolutePath + ".ser")
                    }
                    try {
                        rootService.ioService.saveGame(file.absolutePath)
                        statusLabel.text = "✅ Gespeichert: ${file.name}"
                        statusLabel.font = Font(size = 18, fontWeight = Font.FontWeight.BOLD,
                            color = tools.aqua.bgw.core.Color(100, 210, 140))
                    } catch (e: IllegalStateException) {
                        statusLabel.text = "⛔ Fehler beim Speichern"
                        statusLabel.font = Font(size = 18, fontWeight = Font.FontWeight.BOLD,
                            color = tools.aqua.bgw.core.Color(240, 90, 80))
                        println("Fehler beim Speichern: ${e.message}")
                    }
                }
            }
        }

        if (game.isNetwork && !myTurn) {
            deselectPiece()
            statusLabel.text = "Warte auf Gegner..."
            statusLabel.font = Font(
                size = 20,
                fontWeight = Font.FontWeight.BOLD,
                color = tools.aqua.bgw.core.Color(150, 150, 150)
            )
        } else if (game.isNetwork) {
            statusLabel.text = "✨ My turn!"
            statusLabel.font = Font(
                size = 20,
                fontWeight = Font.FontWeight.BOLD,
                color = tools.aqua.bgw.core.Color(100, 210, 140)
            )
        } else if (isBotThinking) {
            deselectPiece()
            statusLabel.text = "🤖 Bot denkt nach..."
            statusLabel.font = Font(
                size = 20,
                fontWeight = Font.FontWeight.BOLD,
                color = tools.aqua.bgw.core.Color(200, 180, 100)
            )
        } else {
            statusLabel.text = ""
        }

        renderPieceList()
    }

    private fun updateButtonState(button: Button, enabled: Boolean, onClick: () -> Unit) {
        val normalColor = tools.aqua.bgw.core.Color(55, 55, 65)
        val hoverColor = tools.aqua.bgw.core.Color(75, 75, 85)
        val disabledColor = tools.aqua.bgw.core.Color(40, 40, 45)
        val disabledFont = Font(18, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color(80, 80, 90))
        val enabledFont = Font(18, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color.WHITE)

        button.isDisabled = false

        if (enabled) {
            button.visual = ColorVisual(normalColor)
            button.font = enabledFont
            button.onMouseEntered = { button.visual = ColorVisual(hoverColor) }
            button.onMouseExited = { button.visual = ColorVisual(normalColor) }
            button.onMousePressed = { onClick() }
        } else {
            button.visual = ColorVisual(disabledColor)
            button.font = disabledFont
            button.onMouseEntered = {}
            button.onMouseExited = {}
            button.onMousePressed = {}
        }
    }

    /**
     * Called when the mouse enters a board cell.
     * Clamps the piece position so it stays within the board
     *
     * @param row the board row the mouse entered
     * @param col the board column the mouse entered
     */
    private fun onCellHover(row: Int, col: Int) {
        val piece = selectedPiece ?: return
        val game = rootService.game ?: return
        val fieldSize = game.currentGameState.field.size
        val figure = piece.figure

        // Clamp so the piece stays fully within the board
        val clampedRow = row.coerceIn(0, maxOf(0, fieldSize - figure.size))
        val clampedCol = col.coerceIn(0, maxOf(0, fieldSize - figure[0].size))
        val newHover = Pair(clampedRow, clampedCol)

        // Skip if the hover position hasn't changed
        if (newHover == hoverBoardCell) {
            return
        }

        clearPreview()
        hoverBoardCell = newHover

        if (selectedPiece != null) {
            drawPreview()
        }
    }

    /**
     * Called when a board cell is clicked.
     * Attempts to place the selected piece at the current hover position.
     */
    private fun onCellClick() {
        // Block placement if it's not my turn in a network game
        if (!isMyTurn()) return

        val piece = selectedPiece ?: return
        val hover = hoverBoardCell ?: return

        try {
            rootService.playerActionService.place(piece, hover)
            deselectPiece()
        } catch (e: IllegalArgumentException) {
            statusLabel.text = "⛔ Illegaler Zug!"
            statusLabel.font = Font(
                size = 20,
                fontWeight = Font.FontWeight.BOLD,
                color = tools.aqua.bgw.core.Color(204, 36, 29)
            )
            println(e.message)
        } catch (e: IllegalStateException) {
            println(e.message)
        }
    }

    /**
     * Draws semi-transparent preview cells on the board showing where the
     * selected piece would be placed. Color is green if the move is legal,
     * red if illegal.
     */
    private fun drawPreview() {
        val piece = selectedPiece ?: return
        val hover = hoverBoardCell ?: return
        val game = rootService.game ?: return
        val fieldSize = game.currentGameState.field.size
        val figure = piece.figure

        // Check if the placement is legal
        val isLegal = try {
            rootService.gameService.isLegalMove(piece, hover)
        } catch (_: Exception) {
            false
        }

        val previewColor = if (isLegal) {
            tools.aqua.bgw.core.Color(100, 210, 120, 170)
        } else {
            tools.aqua.bgw.core.Color(240, 90, 80, 170)
        }

        // Add preview cells for each filled cell of the piece
        for (row in figure.indices) {
            for (col in figure[row].indices) {
                if (figure[row][col] == 1) {
                    val boardRow = hover.first + row
                    val boardCol = hover.second + col

                    if (boardRow in 0 until fieldSize && boardCol in 0 until fieldSize) {
                        val previewCell = createPreviewCell(
                            x = boardCol * cellStep,
                            y = boardRow * cellStep,
                            size = cellSize,
                            tintColor = previewColor,
                            boardRow = boardRow,
                            boardCol = boardCol
                        )
                        previewTokens.add(previewCell)
                        boardPane.addAll(previewCell)
                    }
                }
            }
        }

        if (isLegal) {
            statusLabel.text = "✅ Hier platzierbar!"
            statusLabel.font = Font(
                size = 20,
                fontWeight = Font.FontWeight.BOLD,
                color = tools.aqua.bgw.core.Color(46, 125, 50)
            )
        } else {
            statusLabel.text = "⛔ Nicht platzierbar"
            statusLabel.font = Font(
                size = 20,
                fontWeight = Font.FontWeight.BOLD,
                color = tools.aqua.bgw.core.Color(204, 36, 29)
            )
        }
    }

    /**
     * Removes all preview cells from the board.
     * Uses a copy of the list to avoid concurrent modification.
     */
    private fun clearPreview() {
        val copy = previewTokens.toList()
        previewTokens.clear()

        for (token in copy) {
            boardPane.remove(token)
        }
    }

    /**
     * Clears and redraws the preview, and updates the selected piece preview on the right.
     * Called after rotating or flipping the selected piece.
     */
    private fun refreshPreview() {
        clearPreview()
        drawPreview()
    }

    /**
     * Selects a piece from the current player's piece list.
     * Updates the preview pane on the right and sets the status label.
     *
     * @param index the index of the piece in the current participant's piece list
     */
    private fun selectPiece(index: Int) {
        val game = rootService.game ?: return
        val participant = game.currentGameState.participantList[game.currentGameState.currentParticipantIndex]

        if (index < 0 || index >= participant.pieceList.size) {
            return
        }

        clearPreview()

        selectedPieceIndex = index
        selectedPiece = participant.pieceList[index]

        statusLabel.text = "${selectedPiece?.id} – bewege Maus über das Board"
        statusLabel.font = Font(
            size = 18,
            fontWeight = Font.FontWeight.BOLD,
            color = tools.aqua.bgw.core.Color.DARK_GRAY
        )
    }

    /**
     * Deselects the currently selected piece.
     * Clears the preview, resets the status label, and stops the hover timer.
     */
    private fun deselectPiece() {
        clearPreview()
        selectedPiece = null
        selectedPieceIndex = -1
        hoverBoardCell = null

        val game = rootService.game
        if (game == null || !game.isNetwork) {
            statusLabel.text = ""
        }
    }

    /**
     * Redraws the entire board from the current game state.
     * Removes all existing placed cells and creates new ones for occupied positions.
     * Empty cells remain as the event grid background.
     */
    private fun renderBoard() {
        val game = rootService.game ?: return
        val field = game.currentGameState.field
        val fieldSize = field.size

        // Remove all existing placed cells
        for (row in 0 until maxBoardSize) {
            for (col in 0 until maxBoardSize) {
                val existingCell = placedCells[row][col]
                if (existingCell != null) {
                    boardPane.remove(existingCell)
                    placedCells[row][col] = null
                }

                // Show/hide event grid cells based on field size
                if (row < eventGrid.size && col < eventGrid[0].size) {
                    eventGrid[row][col].isVisible = (row < fieldSize && col < fieldSize)
                }
            }
        }

        // Add colored cells for occupied positions
        for (row in 0 until fieldSize) {
            for (col in 0 until fieldSize) {
                val color = field[row][col] ?: continue

                val cell = createColoredCell(
                    x = col * cellStep,
                    y = row * cellStep,
                    size = cellSize,
                    color = color,
                    onEnter = { onCellHover(row, col) },
                    onClick = { onCellClick() }
                )

                placedCells[row][col] = cell
                boardPane.addAll(cell)
            }
        }

        previewTokens.clear()
    }

    /**
     * Updates the current player label to show whose turn it is.
     */
    private fun updateCurrentPlayerLabel() {
        val game = rootService.game ?: return
        val state = game.currentGameState
        val participant = state.participantList[state.currentParticipantIndex]
        val colorName = participant.color.name.lowercase().replaceFirstChar { it.uppercase() }

        if(participant is MultiControlledParticipant)
            currentPlayerLabel.text = "${participant.controlledBy[0].name}'s turn ($colorName)"
        else
            currentPlayerLabel.text = "${participant.name}'s turn ($colorName)"

        currentPlayerLabel.font = Font(
            size = 22,
            fontWeight = Font.FontWeight.BOLD,
            color = tools.aqua.bgw.core.Color.WHITE
        )
    }

    /**
     * Renders the current participant's remaining pieces in the bottom area.
     * Pieces are displayed in up to 2 centered rows.
     * Clicking a piece selects it for placement.
     */
    private fun renderPieceList() {
        pieceDisplayPane.clear()

        val game = rootService.game ?: return
        val participant = game.currentGameState.participantList[game.currentGameState.currentParticipantIndex]
        val pieces = participant.pieceList

        if (pieces.isEmpty()) {
            return
        }

        val gap = 8
        val rowHeight = previewCellSize * 5 + 10
        val paneWidth = pieceDisplayPane.width.toInt()

        // Split pieces into two rows
        val firstRow = pieces.take(maxPiecesPerRow)
        val secondRow = pieces.drop(maxPiecesPerRow)

        /**
         * Calculates the total pixel width of a row of pieces.
         */
        fun totalRowWidth(rowPieces: List<Piece>): Int {
            if (rowPieces.isEmpty()) {
                return 0
            }
            val piecesWidth = rowPieces.sumOf { it.figure[0].size * previewCellSize }
            val gapsWidth = (rowPieces.size - 1) * gap
            return piecesWidth + gapsWidth
        }

        /**
         * Renders a single row of pieces, horizontally centered in the pane.
         */
        fun renderRow(rowPieces: List<Piece>, globalStartIndex: Int, offsetY: Int) {
            val totalWidth = totalRowWidth(rowPieces)
            var cursorX = (paneWidth - totalWidth) / 2

            for ((localIndex, piece) in rowPieces.withIndex()) {
                val figure = piece.figure
                val cols = figure[0].size
                val pieceIndex = globalStartIndex + localIndex

                for (row in figure.indices) {
                    for (col in 0 until cols) {
                        if (figure[row][col] == 1) {
                            val cellX = cursorX + col * previewCellSize
                            val cellY = offsetY + row * previewCellSize

                            pieceDisplayPane.addAll(
                                createColoredCell(
                                    x = cellX,
                                    y = cellY,
                                    size = previewCellSize,
                                    color = participant.color,
                                    onClick = {
                                        // Only allow piece selection when it's my turn
                                        if (isMyTurn()) {
                                            selectPiece(pieceIndex)
                                        }
                                    }
                                )
                            )
                        }
                    }
                }

                cursorX += cols * previewCellSize + gap
            }
        }

        renderRow(firstRow, globalStartIndex = 0, offsetY = 0)

        if (secondRow.isNotEmpty()) {
            renderRow(secondRow, globalStartIndex = maxPiecesPerRow, offsetY = rowHeight)
        }
    }

    /**
     * Adjusts the bot speed by the given [delta] and updates the label.
     * Speed is clamped between 1 and 10.
     */
    private fun adjustBotSpeed(delta: Int) {
        val game = rootService.game ?: return
        val newSpeed = (game.botSpeed + delta).coerceIn(1, 10)
        rootService.gameService.setBotSpeed(newSpeed)
        botSpeedValueLabel.text = "${newSpeed}s"
    }

    /** Repositions the piece display pane based on the current board position */
    private fun repositionPieceDisplay() {
        pieceDisplayPane.posX = (boardLeftX - 100).toDouble()
        pieceDisplayPane.posY = pieceAreaY.toDouble()
        pieceDisplayPane.width = (boardPixels + 200).toDouble()
        pieceDisplayPane.height = (previewCellSize * 5 * 2 + 20).toDouble()
    }

    /**
     * Returns the main face color for the given player [color].
     * Used for board cells and piece displays.
     */
    private fun faceColor(color: Color): tools.aqua.bgw.core.Color = when (color) {
        Color.BLUE -> tools.aqua.bgw.core.Color(50, 120, 220)
        Color.RED -> tools.aqua.bgw.core.Color(225, 60, 50)
        Color.GREEN -> tools.aqua.bgw.core.Color(80, 175, 90)
        Color.YELLOW -> tools.aqua.bgw.core.Color(250, 210, 75)
    }

    /**
     * Recalculates all layout positions based on the current [cellSize] and [boardY].
     * Must be called whenever [cellSize] or [boardY] changes.
     */
    private fun recalculateLayout() {
        cellStep = cellSize + 1
        boardPixels = maxBoardSize * cellStep - 1
        boardLeftX = 960 - boardPixels / 2
        boardRightX = boardLeftX + boardPixels
        rightPanelX = boardRightX + 120
        pieceAreaY = boardY + boardPixels + 20
    }

    /** Repositions both labels based on the current board position */
    private fun repositionLabels() {
        currentPlayerLabel.posX = boardLeftX.toDouble()
        currentPlayerLabel.posY = (boardY - 55).toDouble()
        currentPlayerLabel.width = boardPixels.toDouble()

        statusLabel.posX = boardLeftX.toDouble()
        statusLabel.posY = (boardY - 28).toDouble()
        statusLabel.width = boardPixels.toDouble()
    }

    /**
     * Rebuilds the board pane from scratch
     * Creates a new event grid and positions the board pane correctly.
     */
    private fun rebuildBoardPane() {
        boardPane.clear()
        boardPane.posX = boardLeftX.toDouble()
        boardPane.posY = boardY.toDouble()
        boardPane.width = boardPixels.toDouble()
        boardPane.height = boardPixels.toDouble()

        // Create new event grid
        eventGrid = Array(maxBoardSize) { row ->
            Array(maxBoardSize) { col ->
                TokenView(
                    posX = col * cellStep,
                    posY = row * cellStep,
                    width = cellSize,
                    height = cellSize,
                    visual = ColorVisual.BLACK
                ).apply {
                    onMouseEntered = { onCellHover(row, col) }
                    onMouseClicked = { onCellClick() }
                }
            }
        }

        // Add all event grid cells to the board
        for (row in 0 until maxBoardSize) {
            for (col in 0 until maxBoardSize) {
                boardPane.addAll(eventGrid[row][col])
            }
        }

        // Clear placed cell references
        for (row in 0 until maxBoardSize) {
            for (col in 0 until maxBoardSize) {
                placedCells[row][col] = null
            }
        }
    }

    /**
     * Calculates the current score for a participant without ending the game.
     * Returns the score value based on the current scoring mode.
     */
    private fun calculateScore(participant: Participant): Int {
        val game = rootService.game ?: return 0

        if (game.isBasicScoring) {
            // Basic: count remaining squares (lower = better)
            var total = 0
            for (piece in participant.pieceList) {
                piece.figure.forEach { total += it.count { it == 1 } }
            }
            return total
        } else {
            // Advanced: negative remaining squares, bonuses for completion
            var total = 0
            for (piece in participant.pieceList) {
                piece.figure.forEach { total -= it.count { it == 1 } }
            }
            if (total == 0) total = 15

            val lastPiece = participant.lastPlacedPiece
            if (lastPiece != null && lastPiece.id == "O1") {
                total += 5
            }
            return total
        }
    }

    /**
     * Creates a colored cell with a dark border for the given player [color].
     *
     * Structure: a [Pane] containing two [TokenView]s:
     * - A full-size background in the border color
     * - A slightly smaller foreground in the face color (inset by 2px on all sides)
     *
     * @param x the X position within the parent container
     * @param y the Y position within the parent container
     * @param size the total cell size in pixels
     * @param color the player color
     * @param onClick optional click handler
     * @param onEnter optional mouse-enter handler
     * @return a [Pane] representing the colored cell
     */
    private fun createColoredCell(
        x: Int,
        y: Int,
        size: Int,
        color: Color,
        onClick: (() -> Unit)? = null,
        onEnter: (() -> Unit)? = null
    ): Pane<ComponentView> {
        val cellPixels = size - 1
        val borderWidth = 2

        val faceLayer = TokenView(
            posX = borderWidth, posY = borderWidth,
            width = cellPixels - 2 * borderWidth, height = cellPixels - 2 * borderWidth,
            visual = ColorVisual(faceColor(color))
        ).apply {
            isDisabled = true
        }

        return Pane<ComponentView>(posX = x, posY = y, width = cellPixels, height = cellPixels).apply {
            addAll(faceLayer)

            if (onClick != null) {
                onMouseClicked = { onClick() }
            }
            if (onEnter != null) {
                onMouseEntered = { onEnter() }
            }
        }
    }

    /**
     * Creates a transparent preview cell for showing where a piece would be placed.
     * Also handles mouse events so the preview doesn't block interaction with the board.
     *
     * @param x the X position within the board pane
     * @param y the Y position within the board pane
     * @param size the cell size in pixels
     * @param tintColor the preview color (green for legal, red for illegal)
     * @param boardRow the board row this preview cell covers
     * @param boardCol the board column this preview cell covers
     * @return a [TokenView] representing the preview cell
     */
    private fun createPreviewCell(
        x: Int,
        y: Int,
        size: Int,
        tintColor: tools.aqua.bgw.core.Color,
        boardRow: Int,
        boardCol: Int
    ): TokenView {
        val cellPixels = size - 1

        return TokenView(
            posX = x, posY = y,
            width = cellPixels, height = cellPixels,
            visual = ColorVisual(tintColor)
        ).apply {
            onMouseEntered = { onCellHover(boardRow, boardCol) }
            onMouseClicked = { onCellClick() }
        }
    }

    override fun refreshAfterInitializeGame() {
        val game = this.rootService.game

        if(game != null && game.currentGameState.participantList.size == 2){
            this.maxBoardSize = 14
        }

        deselectPiece()
        recalculateLayout()
        rebuildBoardPane()
        repositionLabels()
        repositionPieceDisplay()
        renderBoard()
        updateCurrentPlayerLabel()
        renderPieceList()
        renderPlayerList()
        inspectedPlayerIndex = null
        updateNetworkControls()
    }

    override fun refreshAfterPlace() {
        deselectPiece()
        renderBoard()
        renderPlayerList()
        inspectedPlayerIndex = null
        updateNetworkControls()
    }

    override fun refreshAfterUndo() {
        deselectPiece()
        renderBoard()
        updateCurrentPlayerLabel()
        renderPlayerList()
        inspectedPlayerIndex = null
        updateNetworkControls()
    }

    override fun refreshAfterRedo() {
        deselectPiece()
        renderBoard()
        updateCurrentPlayerLabel()
        renderPlayerList()
        inspectedPlayerIndex = null
        updateNetworkControls()
    }

    override fun refreshAfterNextParticipant() {
        deselectPiece()
        updateCurrentPlayerLabel()
        renderPlayerList()
        inspectedPlayerIndex = null

        updateNetworkControls()
    }
}