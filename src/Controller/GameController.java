package Controller;

import Model.Player;
import Model.Role.Role;
import View.PlayerInfoView;
import View.WaterLevelView;
import View.MapView;
import Model.Deck.TreasureDeck;
import Model.Cards.Card;
import Model.Cards.WaterRiseCard;
import Model.Tile;
import Model.TilePosition;
import Model.Enumeration.TileName;
import Model.Enumeration.TileType;
import Model.Cards.HandCard;
import Model.Enumeration.TileState;
import Model.Deck.FloodDeck;
import Model.Cards.FloodCard;
import Model.Cards.SandbagCard;
import Model.Cards.TreasureCard;
import Model.Cards.HelicopterCard;
import Model.Enumeration.TreasureType;
import View.BoardView;
import View.TreasureView;
import Model.WaterLevel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameController {
    private final List<Player> players;
    private final List<PlayerInfoView> playerInfoViews;
    private final CardController cardController;
    private final TreasureDeck treasureDeck;
    private int currentPlayerIndex = 0;
    private static final int MAX_ACTIONS_PER_TURN = 3;
    private final Tile helicopterTile; // Helicopter landing position
    private WaterLevelView waterLevelView; // Water level view
    private int currentWaterLevel; // Initial water level set through constructor
    private TilePosition tilePosition; // TilePosition object
    private MapController mapController; // MapController member variable
    private FloodDeck floodDeck;
    private BoardView boardView; // BoardView reference
    private final int playerCount; // Player count field

    // ========== Engineer shore up twice mechanism ==========
    private int engineerShoreUpCount = 0;
    private boolean isEngineerShoreUpMode = false;
    private boolean engineerSandbagConsumed = false;

    // Emergency move queue
    private List<Integer> emergencyMoveQueue = new ArrayList<>();
    private boolean isHandlingEmergencyMoves = false;

    public GameController(int playerCount, Tile helicopterTile, WaterLevelView waterLevelView, int initialWaterLevel) {
        System.out.println("\n========== Starting Game Controller Initialization ==========");
        this.playerCount = playerCount; // Initialize player count
        this.players = new ArrayList<>();
        this.playerInfoViews = new ArrayList<>();
        this.cardController = new CardController(this);
        this.treasureDeck = new TreasureDeck(helicopterTile);
        this.helicopterTile = helicopterTile;
        this.waterLevelView = waterLevelView;
        this.currentWaterLevel = initialWaterLevel; // Use the passed initial water level
        this.tilePosition = null;
        this.mapController = null;
        // floodDeck not initialized here, no new Tile created

        // Set initial water level for WaterLevel singleton
        WaterLevel waterLevelInstance = WaterLevel.getInstance();
        // Need to add a method to set initial water level
        waterLevelInstance.setCurrentLevel(initialWaterLevel);

        System.out.println("Initializing " + playerCount + " players...");
        // Initialize players
        for (int i = 0; i < playerCount; i++) {
            Player player = new Player();
            players.add(player);
            PlayerInfoView playerInfoView = new PlayerInfoView(this);
            playerInfoView.setPlayerName("Player " + (i + 1));
            playerInfoViews.add(playerInfoView);
        }

        // Initialize water level
        waterLevelView.updateWaterLevel(currentWaterLevel);

        System.out.println("Assigning roles...");
        // Assign roles
        assignRoles();

        System.out.println("Dealing initial cards...");
        // Deal initial cards
        dealInitialCards();

        System.out.println("Initializing first player's turn...");
        // Initialize first player's turn
        initializeFirstTurn();

        System.out.println("========== Game Controller Initialization Complete ==========\n");

        // Add listeners for each player's sandbag button
        for (int i = 0; i < playerInfoViews.size(); i++) {
            final int playerIndex = i;
            PlayerInfoView view = playerInfoViews.get(i);
            view.getSandbagButton().addActionListener(e -> {
                Player player = players.get(playerIndex);
                boolean hasSandbag = false;
                for (Model.Cards.Card card : player.getHandCard().getCards()) {
                    if (card instanceof Model.Cards.SandbagCard) {
                        hasSandbag = true;
                        break;
                    }
                }
                if (hasSandbag) {
                    if (mapController != null) {
                        mapController.enterSandbagMode(playerIndex);
                    } else {
                        JOptionPane.showMessageDialog(null, "Map not initialized, cannot use sandbag card!");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "You don't have a sandbag card, cannot use it!");
                }
            });
        }
    }

    private void dealInitialCards() {
        // Deal two initial cards to each player
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (int j = 0; j < 2; j++) {
                Card card = treasureDeck.drawInitialCard();
                if (card != null) {
                    try {
                        player.addCard(card);
                        playerInfoViews.get(i).addCard(card);
                    } catch (HandCard.HandCardFullException e) {
                        System.err.println("Hand full during initial card dealing: " + e.getMessage());
                        // If hand is full, return card to deck
                        treasureDeck.discard(card);
                    }
                }
            }
        }
        // Mark end of initial card dealing
        treasureDeck.finishInitialDraw();
    }

    private void assignRoles() {
        RoleManager.assignRolesToPlayers(players);
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Role role = player.getRole();
            PlayerInfoView playerInfoView = playerInfoViews.get(i);
            playerInfoView.setRole(role.getClass().getSimpleName());
        }
    }

    public List<PlayerInfoView> getPlayerInfoViews() {
        return playerInfoViews;
    }

    public CardController getCardController() {
        return cardController;
    }

    public void updatePlayerView(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }
        Player player = players.get(playerIndex);
        PlayerInfoView view = playerInfoViews.get(playerIndex);

        // Update role information
        if (player.getRole() != null) {
            view.setRole(player.getRole().getClass().getSimpleName());
        }

        // Update hand card display
        view.clearCards();
        for (Card card : player.getHandCard().getCards()) {
            view.addCard(card);
        }
    }

    private void initializeFirstTurn() {
        currentPlayerIndex = 0;
        PlayerInfoView currentPlayerView = playerInfoViews.get(currentPlayerIndex);
        currentPlayerView.setActionPoints(MAX_ACTIONS_PER_TURN);
        updatePlayerView(currentPlayerIndex);

        // Update button states for all players
        for (int i = 0; i < playerInfoViews.size(); i++) {
            final int playerIndex = i;
            playerInfoViews.get(i).setButtonsEnabled(i == currentPlayerIndex);
            // Set click event for helicopter card button
            playerInfoViews.get(i).getHelicopterButton().addActionListener(e -> handleHelicopterCard(playerIndex));
        }
    }

    public void handleHelicopterCard(int playerIndex) {
        System.out.println("\n========== Handling Helicopter Card ==========");
        System.out.println("Player index: " + playerIndex);

        Player player = players.get(playerIndex);
        // Check if player has helicopter card
        boolean hasHelicopterCard = false;
        HelicopterCard helicopterCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                hasHelicopterCard = true;
                helicopterCard = (HelicopterCard) card;
                break;
            }
        }

        System.out.println("Has helicopter card: " + hasHelicopterCard);
        if (!hasHelicopterCard) {
            System.out.println("Player doesn't have a helicopter card!");
            JOptionPane.showMessageDialog(null, "You don't have a helicopter card!");
            return;
        }

        System.out.println("Entering helicopter card usage mode");
        // Enter helicopter card usage mode, wait for player to click target position
        mapController.enterHelicopterMode(playerIndex);
        System.out.println("========== Helicopter Card Handling Complete ==========\n");
    }

    /**
     * Handle helicopter card movement
     * 
     * @param playerIndex Player using the helicopter card
     * @param row Target row
     * @param col Target column
     */
    public void handleHelicopterMove(int playerIndex, int row, int col) {
        Player player = players.get(playerIndex);
        Tile targetTile = mapController.getMapView().getTile(row, col);

        if (targetTile == null || targetTile.getState() == TileState.SUNK) {
            JOptionPane.showMessageDialog(null, "Cannot move to a sunken tile!");
            return;
        }

        // Find player's helicopter card
        HelicopterCard helicopterCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                helicopterCard = (HelicopterCard) card;
                break;
            }
        }

        if (helicopterCard == null) {
            JOptionPane.showMessageDialog(null, "You don't have a helicopter card!");
            return;
        }

        // Use helicopter card to move player
        List<Player> playersToMove = new ArrayList<>();
        playersToMove.add(player);
        if (helicopterCard.useForMovement(playersToMove, targetTile)) {
            // Remove helicopter card from player's hand
            player.getHandCard().removeCard(helicopterCard);
            playerInfoViews.get(playerIndex).removeCard(helicopterCard);
            treasureDeck.discard(helicopterCard);

            // Update player view
            updatePlayerView(playerIndex);

            // Show success message
            JOptionPane.showMessageDialog(null, "Successfully used helicopter card to move to " + targetTile.getName());
        } else {
            JOptionPane.showMessageDialog(null, "Helicopter card usage failed!");
        }

        // Exit helicopter card usage mode
        mapController.exitHelicopterMode();
    }

    /**
     * Handle player emergency movement
     * When a player's tile sinks, they must immediately move to the nearest available tile
     * 
     * @param playerIndex Index of player needing emergency movement
     * @return true if movement successful, false if unable to move
     */
    private boolean handleEmergencyMove(int playerIndex) {
        // This method no longer directly enters emergency move mode, but is managed by queue
        if (!emergencyMoveQueue.contains(playerIndex)) {
            emergencyMoveQueue.add(playerIndex);
        }
        if (!isHandlingEmergencyMoves) {
            isHandlingEmergencyMoves = true;
            processNextEmergencyMove();
        }
        return true;
    }

    // Process emergency moves in queue
    public void processNextEmergencyMove() {
        if (emergencyMoveQueue.isEmpty()) {
            isHandlingEmergencyMoves = false;
            // All emergency moves complete, continue game
            return;
        }
        int playerIndex = emergencyMoveQueue.remove(0);
        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        if (currentTile == null || currentTile.getState() != TileState.SUNK) {
            processNextEmergencyMove();
            return;
        }
        // Get all available adjacent tiles
        List<Tile> availableTiles = new ArrayList<>();
        boolean isExplorer = player.getRole() instanceof Model.Role.Explorer;
        for (Tile tile : mapController.getMapView().getAllTiles()) {
            if (tile.getState() == TileState.SUNK)
                continue;
            boolean isReachable;
            if (isExplorer) {
                int rowDistance = Math.abs(currentTile.getRow() - tile.getRow());
                int colDistance = Math.abs(currentTile.getCol() - tile.getCol());
                isReachable = rowDistance <= 1 && colDistance <= 1;
            } else {
                isReachable = currentTile.isAdjacentTo(tile);
            }
            if (isReachable) {
                availableTiles.add(tile);
            }
        }
        if (availableTiles.isEmpty()) {
            endGameWithLose("Player " + (playerIndex + 1) + " is on a sunken tile and cannot move to another tile, game over!");
            isHandlingEmergencyMoves = false;
            emergencyMoveQueue.clear();
            return;
        }
        JOptionPane.showMessageDialog(null,
                "Player " + (playerIndex + 1) + "'s tile has sunk!\nPlease click an adjacent available tile to move.",
                "Emergency Move",
                JOptionPane.WARNING_MESSAGE);
        mapController.enterEmergencyMoveMode(playerIndex, availableTiles);
    }

    // performEmergencyMove完成后，自动处理下一个
    public boolean performEmergencyMove(int playerIndex, Tile targetTile) {
        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        if (currentTile == null || currentTile.getState() != TileState.SUNK) {
            return true;
        }
        boolean isExplorer = player.getRole() instanceof Model.Role.Explorer;
        boolean isValidTarget = false;
        if (isExplorer) {
            int rowDistance = Math.abs(currentTile.getRow() - targetTile.getRow());
            int colDistance = Math.abs(currentTile.getCol() - targetTile.getCol());
            isValidTarget = rowDistance <= 1 && colDistance <= 1;
        } else {
            isValidTarget = currentTile.isAdjacentTo(targetTile);
        }
        if (!isValidTarget || targetTile.getState() == TileState.SUNK) {
            JOptionPane.showMessageDialog(null, "Cannot move to this tile!");
            return false;
        }
        mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(), playerIndex);
        player.setCurrentTile(targetTile);
        String roleName = player.getRole().getClass().getSimpleName().toLowerCase();
        String playerImagePath = "src/resources/Player/" + roleName + "2.png";
        mapController.getMapView().showPlayerImage(targetTile.getRow(), targetTile.getCol(), playerImagePath,
                playerIndex);
        System.out.println("Emergency move completed");
        return true;
    }

    /**
     * 检查并处理所有需要紧急移动的玩家
     * 
     * @return 如果所有玩家都成功移动返回true，否则返回false
     */
    private boolean checkAndHandleEmergencyMoves() {
        System.out.println("\n========== Checking Emergency Moves ==========");
        boolean allSuccess = true;

        // 收集所有需要紧急移动的玩家
        List<Integer> playersToMove = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getCurrentTile() != null && player.getCurrentTile().getState() == TileState.SUNK) {
                playersToMove.add(i);
            }
        }

        // 逐个处理需要移动的玩家
        for (int playerIndex : playersToMove) {
            if (!handleEmergencyMove(playerIndex)) {
                allSuccess = false;
                break;
            }
        }

        System.out.println("========== Emergency Move Check Complete ==========\n");
        return allSuccess;
    }

    public void startNewTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        PlayerInfoView currentPlayerView = playerInfoViews.get(currentPlayerIndex);
        currentPlayerView.setActionPoints(MAX_ACTIONS_PER_TURN);
        updatePlayerView(currentPlayerIndex);

        // 更新所有玩家的按钮状态
        for (int i = 0; i < playerInfoViews.size(); i++) {
            playerInfoViews.get(i).setButtonsEnabled(i == currentPlayerIndex);
        }

        // 检查水位是否达到10
        if (currentWaterLevel >= 10) {
            System.out.println("\n========== Game Over ==========");
            System.out.println("Water level has reached 10, game over!");
            JOptionPane.showMessageDialog(null, "Water level has reached 10, game over!");
            endGameWithLose("Water level has reached 10, game over!");
            return;
        }

        System.out.println("\n========== Drawing Flood Cards ==========");
        int floodCardCount;
        // 根据水位决定抽取的洪水卡数量
        if (currentWaterLevel <= 2) {
            floodCardCount = 2;
        } else if (currentWaterLevel <= 5) {
            floodCardCount = 3;
        } else if (currentWaterLevel <= 7) {
            floodCardCount = 4;
        } else {
            floodCardCount = 5;
        }

        System.out.println("Current water level: " + currentWaterLevel + ", need to draw " + floodCardCount + " flood cards");

        for (int i = 0; i < floodCardCount; i++) {
            FloodCard card = floodDeck.draw();
            if (card != null) {
                Tile targetTile = card.getTargetTile();
                TileState beforeState = targetTile.getState();
                card.use(floodDeck);
                floodDeck.discard(card);

                String stateMsg = "";
                switch (targetTile.getState()) {
                    case FLOODED:
                        stateMsg = "flooded";
                        break;
                    case SUNK:
                        stateMsg = "sunken";
                        break;
                    default:
                        stateMsg = "normal";
                        break;
                }

                String stateChange = "";
                if (beforeState == TileState.NORMAL && targetTile.getState() == TileState.FLOODED) {
                    stateChange = "normal -> flooded";
                } else if (beforeState == TileState.FLOODED && targetTile.getState() == TileState.SUNK) {
                    stateChange = "flooded -> sunken";
                }

                System.out.println("[Log] Flood card drawn: " + targetTile.getName() +
                        " [Coordinates: " + targetTile.getRow() + "," + targetTile.getCol() + "]" +
                        ", State change: " + stateChange +
                        ", Current state: " + stateMsg);

                // 如果板块沉没，立即检查是否有玩家需要紧急移动
                if (targetTile.getState() == TileState.SUNK) {
                    System.out.println("[Log] Tile " + targetTile.getName() + " has sunk, checking if players need emergency movement");
                    for (int j = 0; j < players.size(); j++) {
                        Player player = players.get(j);
                        if (player.getCurrentTile().equals(targetTile)) {
                            System.out.println("[Log] Player " + (j + 1) + " is on sunken tile, needs emergency movement");
                            if (!handleEmergencyMove(j)) {
                                System.out.println("[Log] Player " + (j + 1) + " cannot move, game over");
                                endGameWithLose("Player " + (j + 1) + " is on a sunken tile and cannot move to another tile, game over!");
                                return;
                            }
                        }
                    }
                }
            } else {
                System.out.println("[Warning] Flood deck is empty!");
            }
        }
        System.out.println("========== Flood Card Drawing Complete ==========");

        // 检查所有失败条件
        checkGameOver();
    }

    // 游戏失败判定
    private void checkGameOver() {
        // 1. Fool's Landing sinks
        Tile foolsLanding = null;
        for (Tile tile : mapController.getMapView().getAllTiles()) {
            if (tile.getName().name().equals("FOOLS_LANDING")) {
                foolsLanding = tile;
                break;
            }
        }
        if (foolsLanding == null || foolsLanding.getState() == TileState.SUNK) {
            endGameWithLose("Fool's Landing has sunk, game over!");
            return;
        }

        // 2. Treasure tiles all sink and corresponding treasures not collected
        // Temple (Earth treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.EARTH)) {
            boolean temple1Sunk = isTileSunk("TEMPLE_OF_THE_MOON");
            boolean temple2Sunk = isTileSunk("TEMPLE_OF_THE_SUN");
            if (temple1Sunk && temple2Sunk) {
                endGameWithLose("All temples have sunk and Earth treasure not collected, game over!");
                return;
            }
        }
        // Cave (Fire treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.FIRE)) {
            boolean cave1Sunk = isTileSunk("CAVE_OF_SHADOWS");
            boolean cave2Sunk = isTileSunk("CAVE_OF_EMBERS");
            if (cave1Sunk && cave2Sunk) {
                endGameWithLose("All caves have sunk and Fire treasure not collected, game over!");
                return;
            }
        }
        // Garden (Wind treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.WIND)) {
            boolean garden1Sunk = isTileSunk("WHISPERING_GARDEN");
            boolean garden2Sunk = isTileSunk("HOWLING_GARDEN");
            if (garden1Sunk && garden2Sunk) {
                endGameWithLose("All gardens have sunk and Wind treasure not collected, game over!");
                return;
            }
        }
        // Palace (Water treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.WATER)) {
            boolean palace1Sunk = isTileSunk("CORAL_PALACE");
            boolean palace2Sunk = isTileSunk("TIDAL_PALACE");
            if (palace1Sunk && palace2Sunk) {
                endGameWithLose("All palaces have sunk and Water treasure not collected, game over!");
                return;
            }
        }

        // 3. Player's tile sinks and no adjacent tiles to move to
        for (Player player : players) {
            Tile tile = player.getCurrentTile();
            if (tile != null && tile.getState() == TileState.SUNK) {
                boolean canEscape = false;
                for (Tile adj : tile.getAdjacentTiles()) {
                    if (adj.getState() != TileState.SUNK) {
                        canEscape = true;
                        break;
                    }
                }
                if (!canEscape) {
                    endGameWithLose("Player's tile has sunk and no adjacent tiles to move to, game over!");
                    return;
                }
            }
        }
    }

    // Helper method: Check if a tile with the specified name is sunk
    private boolean isTileSunk(String tileName) {
        for (Tile tile : mapController.getMapView().getAllTiles()) {
            if (tile.getName().name().equals(tileName)) {
                return tile.getState() == TileState.SUNK;
            }
        }
        return false;
    }

    // Game over handling for lose condition
    public void endGameWithLose(String reason) {
        for (PlayerInfoView view : playerInfoViews) {
            view.setButtonsEnabled(false);
        }
        JOptionPane.showMessageDialog(null, reason);
        System.out.println("========== Game Over ==========");
        System.exit(0);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public PlayerInfoView getPlayerInfoView(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < playerInfoViews.size()) {
            return playerInfoViews.get(playerIndex);
        }
        return null;
    }

    public void performAction(int playerIndex, String actionName) {
        if (playerIndex != currentPlayerIndex) {
            return;
        }
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

        boolean consumesAction = actionName.equals("Move") ||
                actionName.equals("Give Cards") ||
                actionName.equals("Treasure") ||
                actionName.equals("Shore up"); // Added shore up to action point consuming operations

        if (!consumesAction || currentActions > 0) {
            boolean actionSuccess = true;

            switch (actionName) {
                case "Move":
                    handleMove(playerIndex);
                    break;
                case "Shore up":
                    handleShoreUp(playerIndex);
                    break;
                case "Give Cards":
                    // First try to give cards, if successful, action points are handled in CardController
                    actionSuccess = requestGiveCard(playerIndex);
                    // Don't consume action points here
                    break;
                case "Special":
                    // Special skills don't consume action points here, but after skill completion
                    handleSpecialSkill(playerIndex);
                    break;
                case "Treasure":
                    handleGetTreasure(playerIndex);
                    break;
                case "Skip":
                    playerView.setActionPoints(0);
                    currentActions = 0;
                    endTurn(playerIndex);
                    return;
            }

            if (currentActions == 0) {
                endTurn(playerIndex);
            }
        }
    }

    /**
     * Handle player movement
     * 
     * @param playerIndex Player index
     */
    private void handleMove(int playerIndex) {
        System.out.println("\n========== Handling player movement ==========");
        // Enter move mode, wait for player to click target position
        mapController.enterMoveMode(playerIndex);
        System.out.println("========== Movement handling completed ==========\n");
    }

    /**
     * Handle treasure acquisition
     * 
     * @param playerIndex Player index
     */
    private void handleGetTreasure(int playerIndex) {
        System.out.println("\n========== Handling treasure acquisition ==========");
        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();

        // Count the number of each treasure card the player has
        Map<TreasureType, Integer> treasureCardCounts = countTreasureCards(player);

        // Check if player is at corresponding treasure location
        TreasureType matchingTreasureType = getTreasureTypeForTile(currentTile.getName());

        if (matchingTreasureType == null) {
            System.out.println("Current position is not a treasure location");
            JOptionPane.showMessageDialog(null, "Current position is not a treasure location, cannot acquire treasure!");
            return;
        }

        // Check if player has enough treasure cards
        Integer cardCount = treasureCardCounts.getOrDefault(matchingTreasureType, 0);
        if (cardCount < 4) {
            System.out.println("Not enough treasure cards, need 4 " + matchingTreasureType.getDisplayName() + " treasure cards, currently only have " + cardCount);
            JOptionPane.showMessageDialog(null,
                    "Not enough treasure cards, need 4 " + matchingTreasureType.getDisplayName() + " treasure cards, currently only have " + cardCount);
            return;
        }

        // Remove 4 treasure cards
        List<Card> cardsToRemove = new ArrayList<>();
        int removed = 0;

        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof TreasureCard) {
                TreasureCard treasureCard = (TreasureCard) card;
                if (treasureCard.getTreasureType() == matchingTreasureType && removed < 4) {
                    cardsToRemove.add(card);
                    removed++;
                }
            }
        }

        // Remove these cards from player's hand (directly destroy, not discard)
        for (Card card : cardsToRemove) {
            player.getHandCard().removeCard(card);
            playerInfoViews.get(playerIndex).removeCard(card);
            // No longer call treasureDeck.discard(card);
        }

        // Record treasure collection
        treasureDeck.recordTreasureCollection(matchingTreasureType);

        // Update treasure view
        int treasureIndex = getTreasureIndex(matchingTreasureType);
        updateTreasureViewStatus(treasureIndex, true);

        System.out.println("Successfully acquired treasure: " + matchingTreasureType.getDisplayName());
        JOptionPane.showMessageDialog(null, "Successfully acquired treasure: " + matchingTreasureType.getDisplayName() + "!");

        // Check if all treasures have been collected
        if (treasureDeck.allTreasuresCollected()) {
            System.out.println("All treasures collected!");
            JOptionPane.showMessageDialog(null, "Congratulations! All treasures collected! Now head to the helipad to escape the island!");
        }

        // Consume one action point
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        playerView.setActionPoints(currentActions - 1);

        // New: If action points reach 0, automatically switch turns
        if (currentActions - 1 == 0) {
            endTurn(playerIndex);
        }

        System.out.println("========== Treasure acquisition handling completed ==========\n");
    }

    /**
     * Count the number of each treasure card a player has
     * 
     * @param player The player
     * @return Count of each treasure card type
     */
    private Map<TreasureType, Integer> countTreasureCards(Player player) {
        Map<TreasureType, Integer> treasureCardCounts = new HashMap<>();

        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof TreasureCard) {
                TreasureCard treasureCard = (TreasureCard) card;
                TreasureType type = treasureCard.getTreasureType();
                treasureCardCounts.put(type, treasureCardCounts.getOrDefault(type, 0) + 1);
            }
        }

        return treasureCardCounts;
    }

    /**
     * Get the treasure type corresponding to a tile name
     * 
     * @param tileName Tile name
     * @return Corresponding treasure type, or null if not a treasure location
     */
    private TreasureType getTreasureTypeForTile(TileName tileName) {
        switch (tileName) {
            case TEMPLE_OF_THE_MOON:
            case TEMPLE_OF_THE_SUN:
                return TreasureType.EARTH; // Earth treasure - temples
            case WHISPERING_GARDEN:
            case HOWLING_GARDEN:
                return TreasureType.WIND; // Wind treasure - gardens
            case CAVE_OF_SHADOWS:
            case CAVE_OF_EMBERS:
                return TreasureType.FIRE; // Fire treasure - caves
            case CORAL_PALACE:
            case TIDAL_PALACE:
                return TreasureType.WATER; // Water treasure - palaces
            default:
                return null; // Not a treasure location
        }
    }

    /**
     * Get the view index for a treasure type
     * 
     * @param treasureType Treasure type
     * @return Treasure view index
     */
    private int getTreasureIndex(TreasureType treasureType) {
        switch (treasureType) {
            case EARTH:
                return 0; // Earth index is 0
            case FIRE:
                return 1; // Fire index is 1
            case WIND:
                return 2; // Wind index is 2
            case WATER:
                return 3; // Water index is 3
            default:
                return -1;
        }
    }

    /**
     * Update treasure view status
     * 
     * @param treasureIndex Treasure index
     * @param found Whether treasure has been found
     */
    private void updateTreasureViewStatus(int treasureIndex, boolean found) {
        if (boardView != null) {
            TreasureView treasureView = boardView.getTreasureView();
            if (treasureView != null) {
                treasureView.updateTreasureStatus(treasureIndex, found);
                return;
            }
        }
        System.out.println("Unable to find TreasureView, treasure status update failed");
    }

    /**
     * Move player to specified position
     * 
     * @param playerIndex Player index
     * @param row Target row
     * @param col Target column
     */
    public void movePlayer(int playerIndex, int row, int col) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }

        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        System.out.printf("Player %d current position: %s [%d, %d]\n",
                playerIndex + 1,
                currentTile.getName(),
                currentTile.getRow(),
                currentTile.getCol());

        // Get target tile object (unique Tile)
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile != null) {
            // Hide player image at original position
            mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(), playerIndex);

            // Update player position
            player.setCurrentTile(targetTile);

            // Show player image at new position
            String roleName = player.getRole().getClass().getSimpleName().toLowerCase();
            String playerImagePath = "src/resources/Player/" + roleName + "2.png";
            mapController.getMapView().showPlayerImage(row, col, playerImagePath, playerIndex);

            // Decrease action points
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
            playerView.setActionPoints(currentActions - 1);

            System.out.printf("Player %d moved to: %s [%d, %d]\n",
                    playerIndex + 1,
                    targetTile.getName(),
                    targetTile.getRow(),
                    targetTile.getCol());

            // Check if action points reached 0, if so automatically end turn
            if (currentActions - 1 == 0) {
                endTurn(playerIndex);
            }
        }
    }

    public void endTurn(int playerIndex) {
        Player currentPlayer = players.get(playerIndex);

        // Draw two treasure cards at end of turn
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Card card = treasureDeck.draw();
            if (card != null) {
                drawnCards.add(card);
                // Add cards directly to player's hand without checking hand limit
                currentPlayer.getHandCard().addCardWithoutCheck(card);
                PlayerInfoView playerInfoView = playerInfoViews.get(playerIndex);
                cardController.addCard(playerInfoView, card);
            }
        }

        // Check for WaterRise cards and use them immediately
        List<Card> cards = currentPlayer.getHandCard().getCards();
        for (Card card : new ArrayList<>(cards)) {
            if (card instanceof WaterRiseCard) {
                ((WaterRiseCard) card).useCard();
                currentPlayer.getHandCard().removeCard(card);
                playerInfoViews.get(playerIndex).removeCard(card);
                treasureDeck.discard(card);
                // Update water level
                currentWaterLevel++;
                waterLevelView.updateWaterLevel(currentWaterLevel);
                JOptionPane.showMessageDialog(null, "Water level has risen by one! Current water level: " + currentWaterLevel);
            }
        }

        // Check hand size, if over 5 cards, enter discard phase
        int cardCount = currentPlayer.getHandCard().getCards().size();
        System.out.println("Current player hand size: " + cardCount); // Debug info

        if (cardCount > 5) {
            int cardsToDiscard = cardCount - 5;
            System.out.println("Need to discard " + cardsToDiscard + " cards"); // Debug info
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);

            // Disable all action buttons, only allow card selection
            playerView.setButtonsEnabled(false);

            // Enable card discard mode
            System.out.println("Preparing to enter discard mode, current player: " + playerIndex); // Debug info
            cardController.enableDiscardMode(playerView, cardsToDiscard);
            System.out.println("Entered discard mode"); // Debug info
            return; // Don't start new turn, wait for player to discard cards
        }

        // Check if action points are 0, if so start new turn
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

        if (currentActions == 0) {
            // Only start new turn directly if no discarding is needed
            startNewTurn();
        }
    }

    // Add method to set MapView
    public void setMapView(MapView mapView) {
        System.out.println("\n========== Setting MapView ==========");
        System.out.println("MapView object: " + (mapView != null ? "not null" : "null"));
        this.tilePosition = mapView.getTilePosition();
        this.mapController = new MapController(this, mapView);

        // Reinitialize flood deck each time map is set
        List<Tile> allTiles = mapView.getAllTiles();
        this.floodDeck = new FloodDeck(allTiles);
        System.out.println("tilePosition object: " + (this.tilePosition != null ? "not null" : "null"));
        if (this.tilePosition != null) {
            Map<String, int[]> positions = this.tilePosition.getAllTilePositions();
            System.out.println("Available tiles count: " + (positions != null ? positions.size() : 0));
            if (positions != null) {
                System.out.println("Available tiles list:");
                positions.forEach((name, pos) -> System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));
            }
            // Initialize player positions after setting tilePosition
            System.out.println("Initializing player positions...");
            initializePlayerPositions(mapView);
        }
        System.out.println("========== MapView setup completed ==========\n");
    }

    // Get position of specific tile
    public int[] getTilePosition(String tileName) {
        if (tilePosition != null) {
            return tilePosition.getTilePosition(tileName);
        }
        return null;
    }

    // Get all tile position information
    public Map<String, int[]> getAllTilePositions() {
        if (tilePosition != null) {
            return tilePosition.getAllTilePositions();
        }
        return null;
    }

    /**
     * Initialize player positions
     * Randomly assign players to different tiles
     */
    private void initializePlayerPositions(MapView mapView) {
        System.out.println("\n========== Starting player position initialization ==========");
        System.out.println("Current player count: " + players.size());

        if (tilePosition == null) {
            System.err.println("Error: tilePosition not initialized");
            System.out.println("tilePosition object: " + (tilePosition != null ? "not null" : "null"));
            System.out.println("========== Player position initialization failed ==========\n");
            return;
        }

        // Get all available tile positions
        Map<String, int[]> allPositions = tilePosition.getAllTilePositions();
        System.out.println("Retrieved tile position info: " + (allPositions != null ? "not null" : "null"));

        if (allPositions == null || allPositions.isEmpty()) {
            System.err.println("Error: No available tile positions");
            System.out.println("Available tiles count: " + (allPositions != null ? allPositions.size() : 0));
            System.out.println("========== Player position initialization failed ==========\n");
            return;
        }

        System.out.println("Available tiles count: " + allPositions.size());
        System.out.println("Available tiles list:");
        allPositions.forEach((name, pos) -> System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));

        // Convert tile positions to list for random selection
        List<String> availableTiles = new ArrayList<>(allPositions.keySet());
        System.out.println("Tile order before shuffling:");
        availableTiles.forEach(tile -> System.out.println("  - " + tile));

        java.util.Collections.shuffle(availableTiles); // Random shuffle

        System.out.println("Tile order after shuffling:");
        availableTiles.forEach(tile -> System.out.println("  - " + tile));

        // Assign positions to each player
        System.out.println("\nStarting player position assignment:");
        for (int i = 0; i < players.size(); i++) {
            if (i >= availableTiles.size()) {
                System.err.println("Warning: Not enough available tiles");
                break;
            }

            String tileName = availableTiles.get(i);
            int[] position = allPositions.get(tileName);

            System.out.printf("Assigning position to player %d:\n", i + 1);
            System.out.printf("  Selected tile: %s\n", tileName);
            System.out.printf("  Tile position: [%d, %d]\n", position[0], position[1]);

            // Use MapView's Tile object
            Tile tile = mapView.getTile(position[0], position[1]);
            players.get(i).setCurrentTile(tile);

            // Show player image
            String roleName = players.get(i).getRole().getClass().getSimpleName().toLowerCase();
            String playerImagePath = "src/resources/Player/" + roleName + "2.png";
            mapView.showPlayerImage(position[0], position[1], playerImagePath, i);

            System.out.printf("  Player %d position set\n", i + 1);
        }

        // Display all player position information
        displayPlayerPositions();
        System.out.println("========== Player position initialization completed ==========\n");
    }

    /**
     * Display all player position information
     */
    private void displayPlayerPositions() {
        System.out.println("\n========== Player Position Information ==========");
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Tile currentTile = player.getCurrentTile();
            if (currentTile != null) {
                System.out.printf("Player %d (%s):\n",
                        i + 1,
                        player.getRole() != null ? player.getRole().getClass().getSimpleName() : "No role assigned");
                System.out.printf("  Tile: %s\n", currentTile.getName());
                System.out.printf("  Position: [%d, %d]\n", currentTile.getRow(), currentTile.getCol());
                System.out.printf("  State: %s\n", currentTile.getState());
            } else {
                System.out.printf("Player %d: No position assigned\n", i + 1);
            }
        }
        System.out.println("================================\n");
    }

    /**
     * Check if player can shore up specified tile
     * 
     * @param playerIndex Player index
     * @param targetTile  Target tile
     * @return Returns true if can shore up
     */
    public boolean canShoreUpTile(int playerIndex, Tile targetTile) {
        if (playerIndex != currentPlayerIndex || targetTile == null) {
            return false;
        }
        Player player = players.get(playerIndex);
        Role role = player.getRole();
        Tile currentTile = player.getCurrentTile();

        // Check if player has sandbag card
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }

        // If has sandbag card, can shore up any flooded tile
        if (hasSandbag && targetTile.getState() == TileState.FLOODED) {
            return true;
        }

        // Check if is Explorer
        boolean isExplorer = role instanceof Model.Role.Explorer;

        // Calculate Manhattan distance
        int distance = Math.abs(currentTile.getRow() - targetTile.getRow()) +
                Math.abs(currentTile.getCol() - targetTile.getCol());

        // If Explorer, allow diagonal shore up (distance 2)
        boolean isAdjacent = isExplorer ? (Math.abs(currentTile.getRow() - targetTile.getRow()) <= 1 &&
                Math.abs(currentTile.getCol() - targetTile.getCol()) <= 1)
                : (currentTile.isAdjacentTo(targetTile) || currentTile.equals(targetTile));

        boolean isShoreable = targetTile.isShoreable();
        return isAdjacent && isShoreable;
    }

    /**
     * Check if player can give card
     * 
     * @param fromPlayerIndex Giving player index
     * @param toPlayerIndex   Receiving player index
     * @param card            Card to give
     * @return Returns true if can give card
     */
    public boolean canGiveCard(int fromPlayerIndex, int toPlayerIndex, Card card) {
        if (fromPlayerIndex != currentPlayerIndex ||
                toPlayerIndex < 0 || toPlayerIndex >= players.size() ||
                fromPlayerIndex == toPlayerIndex || card == null) {
            return false;
        }
        Player fromPlayer = players.get(fromPlayerIndex);
        Player toPlayer = players.get(toPlayerIndex);
        boolean sameLocation = fromPlayer.getCurrentTile().equals(toPlayer.getCurrentTile());
        boolean hasCard = fromPlayer.getHandCard().getCards().contains(card);
        boolean handNotFull = !toPlayer.getHandCard().isFull();
        return sameLocation && hasCard && handNotFull;
    }

    /**
     * Check if player can use special skill
     * 
     * @param playerIndex Player index
     * @return Returns true if can use special skill
     */
    public boolean canUseSpecialSkill(int playerIndex) {
        if (playerIndex != currentPlayerIndex) {
            return false;
        }
        Player player = players.get(playerIndex);
        Role role = player.getRole();
        if (role == null) {
            return false;
        }
        String roleName = role.getClass().getSimpleName();
        switch (roleName) {
            case "Pilot":
                return true;
            case "Navigator":
                return players.size() > 1;
            case "Engineer":
                return true;
            case "Explorer":
                return true;
            case "Diver":
                return true;
            case "Messenger":
                return true;
            default:
                return false;
        }
    }

    /**
     * View layer calls this method to initiate card giving process
     * 
     * @return Returns true if successfully initiated card giving process, otherwise false
     */
    public boolean requestGiveCard(int fromPlayerIndex) {
        Player fromPlayer = players.get(fromPlayerIndex);
        List<Integer> candidateIndexes = new ArrayList<>();
        boolean sameLocation = false;

        // Build player options list
        List<String> playerOptionsList = new ArrayList<>();

        // Iterate through all players
        for (int i = 0; i < players.size(); i++) {
            if (i != fromPlayerIndex) {
                Player targetPlayer = players.get(i);
                // Check if at same location
                sameLocation = fromPlayer.getCurrentTile().equals(targetPlayer.getCurrentTile());
                // If Messenger or at same location, can give card
                if (fromPlayer.getRole() instanceof Model.Role.Messenger || sameLocation) {
                    candidateIndexes.add(i);
                    String location = sameLocation ? "(Same location)" : "(Different location)";
                    playerOptionsList.add(String.format("Player %d - %s %s",
                            i + 1,
                            targetPlayer.getRole().getClass().getSimpleName(),
                            location));
                }
            }
        }

        // Add cancel option
        playerOptionsList.add("Cancel");

        if (candidateIndexes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No players available to give cards to!");
            return false;
        }

        // Convert List to array
        String[] playerOptions = playerOptionsList.toArray(new String[0]);

        // Let player select target player
        int selectedOption = JOptionPane.showOptionDialog(
                null,
                "Please select player to give cards to:",
                "Select Target Player",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]);

        if (selectedOption == -1 || selectedOption == playerOptions.length - 1) {
            System.out.println("[Log] Player canceled target player selection.");
            return false;
        }

        // Get selected target player index
        int toPlayerIndex = candidateIndexes.get(selectedOption);
        System.out.println("[Log] Player selected target player: " + (toPlayerIndex + 1));

        // Get current player's hand cards
        List<Card> handCards = fromPlayer.getHandCard().getCards();
        if (handCards.isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have no cards to give!");
            return false;
        }

        // Build card options
        String[] cardOptions = new String[handCards.size() + 1];
        for (int i = 0; i < handCards.size(); i++) {
            Card card = handCards.get(i);
            if (card instanceof TreasureCard) {
                TreasureCard treasureCard = (TreasureCard) card;
                cardOptions[i] = treasureCard.getTreasureType().getDisplayName() + " Treasure Card";
            } else {
                cardOptions[i] = card.getClass().getSimpleName();
            }
        }
        cardOptions[handCards.size()] = "Cancel";

        // Let player select card to give
        int selectedCard = JOptionPane.showOptionDialog(
                null,
                "Please select card to give:",
                "Select Card",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                cardOptions,
                cardOptions[0]);

        if (selectedCard == -1 || selectedCard == cardOptions.length - 1) {
            System.out.println("[Log] Player canceled card selection.");
            return false;
        }

        // Execute card giving
        Card selectedCardObj = handCards.get(selectedCard);
        cardController.pendingGiveCardPlayerIndex = fromPlayerIndex;
        return cardController.giveCard(fromPlayerIndex, toPlayerIndex, selectedCardObj);
    }

    /**
     * Handle shore up action
     * 
     * @param playerIndex Player index
     * @return Returns true if successfully initiated shore up action, otherwise false
     */
    public boolean handleShoreUp(int playerIndex) {
        Player player = players.get(playerIndex);
        Role role = player.getRole();

        // Check if player has sandbag card
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }

        // If no sandbag card, check if has enough action points
        if (!hasSandbag) {
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

            if (currentActions <= 0) {
                System.out.println("[Log] Player doesn't have enough action points to shore up");
                JOptionPane.showMessageDialog(null, "You don't have enough action points to shore up!");
                return false;
            }
        }

        // If Engineer, can shore up two tiles
        if (role instanceof Model.Role.Engineer) {
            System.out.println("[Log] Engineer can shore up two tiles");
            JOptionPane.showMessageDialog(null, "As Engineer, you can shore up two tiles consecutively!");
            engineerShoreUpCount = 0;
            isEngineerShoreUpMode = true;
            engineerSandbagConsumed = false;
        } else {
            isEngineerShoreUpMode = false;
        }

        // Enter shore up mode, wait for player to select tile to shore up
        System.out.println("[Log] Entering shore up mode, please select tile to shore up");
        mapController.enterShoreUpMode(playerIndex);
        return true;
    }

    /**
     * Handle special skill
     * 
     * @param playerIndex Player index
     */
    private void handleSpecialSkill(int playerIndex) {
        Player player = players.get(playerIndex);
        Role role = player.getRole();

        if (role == null) {
            System.out.println("[Log] Player has no role, cannot use special skill");
            return;
        }

        // Check if can use special skill
        if (!role.canUseAbility()) {
            System.out.println("[Log] Cannot use special skill currently");
            JOptionPane.showMessageDialog(null, "Cannot use special skill currently!");
            return;
        }

        // Handle special skill based on role type
        if (role instanceof Model.Role.Pilot) {
            // Pilot can fly to any location
            System.out.println("[Log] Pilot can use flying ability");
            mapController.enterMoveMode(playerIndex);
            role.useSpecialAbility();

            // Pilot consumes one action point immediately after using special skill
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
            playerView.setActionPoints(currentActions - 1);

            // If action points exhausted, end turn
            if (currentActions - 1 == 0) {
                endTurn(playerIndex);
            }
        } else if (role instanceof Model.Role.Navigator) {
            // Navigator can move other players
            System.out.println("[Log] Navigator can use ability to move other players");
            handleNavigatorAbility(playerIndex);
        } else {
            System.out.println("[Log] This role has no active special skill to use");
            JOptionPane.showMessageDialog(null, "This role has no active special skill to use!");
        }
    }

    /**
     * Handle Navigator's special ability
     * 
     * @param navigatorIndex Navigator player index
     */
    private void handleNavigatorAbility(int navigatorIndex) {
        // Get all other players
        List<Player> otherPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (i != navigatorIndex) {
                otherPlayers.add(players.get(i));
            }
        }

        if (otherPlayers.isEmpty()) {
            System.out.println("[Log] No other players to move");
            JOptionPane.showMessageDialog(null, "No other players to move!");
            return;
        }

        // Create player selection dialog
        String[] playerOptions = new String[otherPlayers.size()];
        for (int i = 0; i < otherPlayers.size(); i++) {
            Player p = otherPlayers.get(i);
            playerOptions[i] = "Player " + (players.indexOf(p) + 1) + " (" + p.getRole().getClass().getSimpleName() + ")";
        }

        // Show player selection dialog
        int selectedPlayerIndex = JOptionPane.showOptionDialog(
                null,
                "Select player to move:",
                "Navigator Ability",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]);

        if (selectedPlayerIndex == -1) {
            System.out.println("[Log] Player canceled selection");
            return;
        }

        // Get selected player
        Player targetPlayer = otherPlayers.get(selectedPlayerIndex);
        int targetPlayerIndex = players.indexOf(targetPlayer);

        // Set target player's move count to 2
        PlayerInfoView targetPlayerView = playerInfoViews.get(targetPlayerIndex);
        targetPlayerView.setActionPoints(2);

        // Use Navigator's ability
        Role navigatorRole = players.get(navigatorIndex).getRole();
        if (navigatorRole != null) {
            navigatorRole.useSpecialAbility();
        }

        // Enter move mode, but moving target player
        System.out.println("[Log] Entering Navigator move mode, moving player " + (targetPlayerIndex + 1));
        mapController.enterNavigatorMoveMode(navigatorIndex, targetPlayerIndex);

        // Show message
        JOptionPane.showMessageDialog(null,
                "Player " + (targetPlayerIndex + 1) + " can now move twice!\n" +
                        "After completing two moves, will consume one action point from Navigator.",
                "Navigator Ability",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Move other player to specified position (used by Navigator)
     * 
     * @param navigatorIndex    Navigator player index
     * @param targetPlayerIndex Target player index
     * @param row               Target row
     * @param col               Target column
     */
    public void moveOtherPlayer(int navigatorIndex, int targetPlayerIndex, int row, int col) {
        if (navigatorIndex < 0 || navigatorIndex >= players.size() ||
                targetPlayerIndex < 0 || targetPlayerIndex >= players.size() ||
                navigatorIndex == targetPlayerIndex) {
            return;
        }

        Player targetPlayer = players.get(targetPlayerIndex);
        Tile currentTile = targetPlayer.getCurrentTile();
        System.out.printf("Player %d current position: %s [%d, %d]\n",
                targetPlayerIndex + 1,
                currentTile.getName(),
                currentTile.getRow(),
                currentTile.getCol());

        // Get target tile object
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile != null) {
            // Check if move is valid
            if (isValidNavigatorMove(targetPlayer, targetTile)) {
                // Hide player image at original position
                mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(),
                        targetPlayerIndex);

                // Update player position
                targetPlayer.setCurrentTile(targetTile);

                // Show player image at new position
                String roleName = targetPlayer.getRole().getClass().getSimpleName().toLowerCase();
                String playerImagePath = "src/resources/Player/" + roleName + "2.png";
                mapController.getMapView().showPlayerImage(row, col, playerImagePath, targetPlayerIndex);

                System.out.printf("Navigator moved player %d to: %s [%d, %d]\n",
                        targetPlayerIndex + 1,
                        targetTile.getName(),
                        targetTile.getRow(),
                        targetTile.getCol());

                // Decrease target player's move count
                PlayerInfoView targetPlayerView = playerInfoViews.get(targetPlayerIndex);
                String actionText = targetPlayerView.getActionPointsLabel().getText();
                int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
                targetPlayerView.setActionPoints(currentActions - 1);

                // If target player has no remaining moves, consume one action point from Navigator and exit move mode
                if (currentActions - 1 == 0) {
                    // Exit Navigator move mode
                    mapController.exitNavigatorMoveMode();

                    // Show message
                    JOptionPane.showMessageDialog(null,
                            "Target player has completed two moves!\n" +
                                    "Consumed one action point from Navigator.",
                            "Navigator Ability",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Consume one action point from Navigator
                    PlayerInfoView navigatorView = playerInfoViews.get(navigatorIndex);
                    String navigatorActionText = navigatorView.getActionPointsLabel().getText();
                    int navigatorActions = Integer.parseInt(navigatorActionText.split(":")[1].trim());
                    navigatorView.setActionPoints(navigatorActions - 1);

                    // If Navigator has no remaining action points, end turn
                    if (navigatorActions - 1 == 0) {
                        endTurn(navigatorIndex);
                    }
                } else {
                    // Ask if want to continue second move
                    int option = JOptionPane.showConfirmDialog(null,
                            "Continue with second move?\nSelect 'No' will directly consume one action point from Navigator and end this ability.",
                            "Navigator Ability",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.NO_OPTION) {
                        // Player chose not to continue second move
                        mapController.exitNavigatorMoveMode();
                        PlayerInfoView navigatorView = playerInfoViews.get(navigatorIndex);
                        String navigatorActionText = navigatorView.getActionPointsLabel().getText();
                        int navigatorActions = Integer.parseInt(navigatorActionText.split(":")[1].trim());
                        navigatorView.setActionPoints(navigatorActions - 1);
                        if (navigatorActions - 1 == 0) {
                            endTurn(navigatorIndex);
                        }
                    } else {
                        // Player chose to continue, notify can move once more
                        JOptionPane.showMessageDialog(null,
                                "Player " + (targetPlayerIndex + 1) + " can move 1 more time!",
                                "Navigator Ability",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                System.out.println("[Log] Invalid move: Target position unreachable");
                JOptionPane.showMessageDialog(null, "Invalid move: Target position unreachable", "Move Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Check if Navigator moving other player is valid
     * 
     * @param targetPlayer Target player
     * @param targetTile   Target tile
     * @return Returns true if move is valid
     */
    private boolean isValidNavigatorMove(Player targetPlayer, Tile targetTile) {
        // Only check if sunk, flooded tiles can be moved to
        if (targetTile.getState() == TileState.SUNK) {
            System.out.println("Target tile is sunk, cannot move");
            return false;
        }

        Tile currentTile = targetPlayer.getCurrentTile();
        if (currentTile == null) {
            System.out.println("Cannot get current tile");
            return false;
        }

        // Check if target player is Pilot
        boolean isPilot = targetPlayer.getRole() instanceof Model.Role.Pilot;
        if (isPilot) {
            return true;
        }

        // Check if target player is Diver
        boolean isDiver = targetPlayer.getRole().getClass().getSimpleName().equals("Diver");
        if (isDiver) {
            // Reuse MapController's BFS check
            return mapController != null && mapController.isDiverReachable(currentTile, targetTile);
        }

        // Check if target player is Explorer
        boolean isExplorer = targetPlayer.getRole() instanceof Model.Role.Explorer;
        int rowDistance = Math.abs(currentTile.getRow() - targetTile.getRow());
        int colDistance = Math.abs(currentTile.getCol() - targetTile.getCol());
        if (isExplorer) {
            return rowDistance <= 1 && colDistance <= 1;
        }
        // Other players can only move to adjacent tiles
        return (rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1);
    }

    /**
     * Shore up specified tile
     *
     * @param playerIndex Player index
     * @param row         Target row
     * @param col         Target column
     */
    public void shoreUpTile(int playerIndex, int row, int col) {
        System.out.println("\n========== Starting tile shoring up ==========");
        System.out.println("Player index: " + playerIndex);
        System.out.println("Target position: [" + row + "," + col + "]");

        Player player = players.get(playerIndex);
        System.out.println("Player role: " + player.getRole().getClass().getSimpleName());
        System.out.println("Is Engineer: " + (player.getRole() instanceof Model.Role.Engineer));

        Tile targetTile = mapController.getMapView().getTile(row, col);
        System.out.println("Target tile: " + targetTile.getName());
        System.out.println("Target tile state: " + targetTile.getState());

        if (canShoreUpTile(playerIndex, targetTile)) {
            System.out.println("Can shore up this tile");
            targetTile.setState(TileState.NORMAL);
            System.out.println("Tile shored up, new state: " + targetTile.getState());

            // Check if Engineer's second shore up
            if (player.getRole() instanceof Model.Role.Engineer) {
                System.out.println("Player is Engineer, check if can continue shoring up");
                System.out.println("Current shore up count: " + engineerShoreUpCount);

                // Check if more shoreable tiles nearby
                boolean hasMoreShoreableTiles = false;
                Tile currentTile = player.getCurrentTile();
                for (Tile adjacentTile : currentTile.getAdjacentTiles()) {
                    if (adjacentTile != targetTile && canShoreUpTile(playerIndex, adjacentTile)) {
                        hasMoreShoreableTiles = true;
                        break;
                    }
                }

                if (engineerShoreUpCount < 1 && hasMoreShoreableTiles) {
                    System.out.println("Engineer can continue shoring up");
                    engineerShoreUpCount++;
                    System.out.println("Shore up count updated to: " + engineerShoreUpCount);
                    return;
                }
            }

            System.out.println("Shore up action complete");
            endShoreUpAction();
        } else {
            System.out.println("Cannot shore up this tile");
        }
        System.out.println("========== Tile shoring up complete ==========\n");
    }

    private void endShoreUpAction() {
        System.out.println("\n========== Ending shore up action ==========");
        System.out.println("Current shore up count: " + engineerShoreUpCount);
        System.out.println("Current player role: " + players.get(currentPlayerIndex).getRole().getClass().getSimpleName());
        System.out.println("Is Engineer: " + (players.get(currentPlayerIndex).getRole() instanceof Model.Role.Engineer));

        // Update player info view
        PlayerInfoView playerView = playerInfoViews.get(currentPlayerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        playerView.setActionPoints(currentActions - 1);
        System.out.println("Updated player action points: " + (currentActions - 1));

        // Reset Engineer shore up count
        engineerShoreUpCount = 0;
        System.out.println("Shore up count reset to: " + engineerShoreUpCount);

        mapController.exitShoreUpMode();
        System.out.println("Exited shore up mode");

        // If action points exhausted, end turn
        if (currentActions - 1 == 0) {
            System.out.println("Action points exhausted, ending turn");
            endTurn(currentPlayerIndex);
        }

        System.out.println("========== Shore up action complete ==========\n");
    }

    /**
     * Use sandbag card to shore up any flooded tile
     */
    public void sandbagShoreUpTile(int playerIndex, int row, int col) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }
        Player player = players.get(playerIndex);
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile == null || targetTile.getState() != Model.Enumeration.TileState.FLOODED) {
            JOptionPane.showMessageDialog(null, "Can only shore up flooded tiles!");
            return;
        }
        // Find a sandbag card
        Model.Cards.Card sandbagCard = null;
        for (Model.Cards.Card card : player.getHandCard().getCards()) {
            if (card instanceof Model.Cards.SandbagCard) {
                sandbagCard = card;
                break;
            }
        }
        if (sandbagCard == null) {
            JOptionPane.showMessageDialog(null, "You don't have a sandbag card, cannot use!");
            return;
        }
        // Execute shore up
        targetTile.setState(Model.Enumeration.TileState.NORMAL);
        player.getHandCard().removeCard(sandbagCard);
        playerInfoViews.get(playerIndex).removeCard(sandbagCard);
        treasureDeck.discard(sandbagCard);
        JOptionPane.showMessageDialog(null, "Successfully used sandbag card to repair tile!");
    }

    public List<Player> getPlayers() {
        return players;
    }

    public TreasureDeck getTreasureDeck() {
        return treasureDeck;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void endGameWithWin() {
        for (PlayerInfoView view : playerInfoViews) {
            view.setButtonsEnabled(false);
        }
        JOptionPane.showMessageDialog(null, "Congratulations! You collected all treasures and escaped, you win!");
        System.out.println("========== Game Victory ==========");
        // Return to main menu
        View.MainView mainView = View.MainView.getInstance();
        if (mainView != null) {
            mainView.showStartScreen();
        }
    }

    // Add method to set BoardView
    public void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    public MapController getMapController() {
        return mapController;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Check game win condition
     * When all treasures are collected and all players are at helipad, game is won
     */
    public void checkGameWin() {
        // Check if all treasures collected
        if (!treasureDeck.allTreasuresCollected()) {
            return;
        }

        // Check if all players at helipad
        boolean allPlayersAtHelicopter = true;
        for (Player player : players) {
            Tile currentTile = player.getCurrentTile();
            if (currentTile == null || !currentTile.getName().equals(TileName.FOOLS_LANDING)) {
                allPlayersAtHelicopter = false;
                break;
            }
        }

        if (allPlayersAtHelicopter) {
            endGameWithWin();
        }
    }

    public void resumeGiveCardTurn(int playerIndex) {
        // Check if A still has action points
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        if (currentActions <= 0) {
            // No action points, directly switch to next player
            startNewTurn();
            return;
        }
        // Restore A's buttons and action points display, don't switch turn
        for (int i = 0; i < playerInfoViews.size(); i++) {
            playerInfoViews.get(i).setButtonsEnabled(i == playerIndex);
        }
        updatePlayerView(playerIndex);
    }
}