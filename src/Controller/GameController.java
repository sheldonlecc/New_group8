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
    private WaterLevelView waterLevelView; // Add water level view
    private int currentWaterLevel; // Initial water level set through constructor
    private TilePosition tilePosition; // Add TilePosition object
    private MapController mapController; // Add MapController member variable
    private FloodDeck floodDeck;
    private BoardView boardView; // Add BoardView reference
    private final int playerCount; // Add player count field

    // ========== Engineer shore up twice mechanism ==========
    private int engineerShoreUpCount = 0;
    private boolean isEngineerShoreUpMode = false;
    private boolean engineerSandbagConsumed = false;

    // Emergency move queue
    private List<Integer> emergencyMoveQueue = new ArrayList<>();
    private boolean isHandlingEmergencyMoves = false;

    public GameController(int playerCount, Tile helicopterTile, WaterLevelView waterLevelView, int initialWaterLevel) {
        System.out.println("\n========== Starting game controller initialization ==========");
        this.playerCount = playerCount; // Initialize player count
        this.players = new ArrayList<>();
        this.playerInfoViews = new ArrayList<>();
        this.cardController = new CardController(this);
        this.treasureDeck = new TreasureDeck(helicopterTile);
        this.helicopterTile = helicopterTile;
        this.waterLevelView = waterLevelView;
        this.currentWaterLevel = initialWaterLevel; // Use provided initial water level
        this.tilePosition = null;
        this.mapController = null;
        // floodDeck not initialized here, no new Tile objects

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
        // Initial card dealing
        dealInitialCards();

        System.out.println("Initializing first player's turn...");
        // Initialize first player's turn
        initializeFirstTurn();

        System.out.println("========== Game controller initialization complete ==========\n");

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
                    JOptionPane.showMessageDialog(null, "You don't have a sandbag card, cannot use!");
                }
            });
        }
    }

    private void dealInitialCards() {
        // Distribute two initial cards to each player
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (int j = 0; j < 2; j++) {
                Card card = treasureDeck.drawInitialCard();
                if (card != null) {
                    try {
                        player.addCard(card);
                        playerInfoViews.get(i).addCard(card);
                    } catch (HandCard.HandCardFullException e) {
                        System.err
                                .println("The hand was already full when the initial deal was made: " + e.getMessage());
                        treasureDeck.discard(card);
                    }
                }
            }
        }
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

        if (player.getRole() != null) {
            view.setRole(player.getRole().getClass().getSimpleName());
        }

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

        // Update the button status of all players
        for (int i = 0; i < playerInfoViews.size(); i++) {
            final int playerIndex = i;
            playerInfoViews.get(i).setButtonsEnabled(i == currentPlayerIndex);
            playerInfoViews.get(i).getHelicopterButton().addActionListener(e -> handleHelicopterCard(playerIndex));
        }
    }

    public void handleHelicopterCard(int playerIndex) {
        System.out.println("\n========== Processing helicopter card ==========");
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
            System.out.println("Player does not have helicopter card!");
            JOptionPane.showMessageDialog(null, "You don't have a helicopter card!");
            return;
        }

        System.out.println("Entering helicopter card use mode");
        // Enter helicopter card use mode, wait for player to click target location
        mapController.enterHelicopterMode(playerIndex);
        System.out.println("========== Helicopter card processing complete ==========\n");
    }

    public void handleHelicopterMove(int playerIndex, int row, int col) {
        Player player = players.get(playerIndex);
        Tile targetTile = mapController.getMapView().getTile(row, col);

        if (targetTile == null || targetTile.getState() == TileState.SUNK) {
            JOptionPane.showMessageDialog(null, "Cannot move to a sunk tile!");
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
            JOptionPane.showMessageDialog(null, "Helicopter card use failed!");
        }

        // Exit helicopter card use mode
        mapController.exitHelicopterMode();
    }

    /**
     * Handling emergency player movement
     * When the player's current section sinks, they must immediately move to the
     * nearest available section. *
     * 
     * @param playerIndex The index of the player who needs to be moved urgently
     * @return Returns true if the movement is successful, and false if it fails
     */
    private boolean handleEmergencyMove(int playerIndex) {
        // This method no longer directly enters the emergency mobile mode; instead, it
        // is uniformly scheduled by the queue
        if (!emergencyMoveQueue.contains(playerIndex)) {
            emergencyMoveQueue.add(playerIndex);
        }
        if (!isHandlingEmergencyMoves) {
            isHandlingEmergencyMoves = true;
            processNextEmergencyMove();
        }
        return true;
    }

    // Process the emergency mobile queue in sequence
    public void processNextEmergencyMove() {
        if (emergencyMoveQueue.isEmpty()) {
            isHandlingEmergencyMoves = false;
            // All emergency movements have been completed. Continue the game
            return;
        }
        int playerIndex = emergencyMoveQueue.remove(0);
        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        if (currentTile == null || currentTile.getState() != TileState.SUNK) {
            processNextEmergencyMove();
            return;
        }
        // Obtain all available adjacent plates
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
            endGameWithLose(
                    "Player " + (playerIndex + 1) + " is on a sunk tile and cannot move to another tile, game over!");
            isHandlingEmergencyMoves = false;
            emergencyMoveQueue.clear();
            return;
        }
        JOptionPane.showMessageDialog(null,
                "Player " + (playerIndex + 1)
                        + "'s tile has sunk!\nPlease click on an adjacent available tile to move.",
                "Emergency Move",
                JOptionPane.WARNING_MESSAGE);
        mapController.enterEmergencyMoveMode(playerIndex, availableTiles);
    }

    // performEmergencyMove
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
     * Check and handle all players who require urgent relocation
     * 
     * @return If all players successfully move, return true; otherwise, return false
     */
    private boolean checkAndHandleEmergencyMoves() {
        System.out.println("\n========== Checking Emergency Moves ==========");
        boolean allSuccess = true;

        // Collect all the players who need to be moved urgently
        List<Integer> playersToMove = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getCurrentTile() != null && player.getCurrentTile().getState() == TileState.SUNK) {
                playersToMove.add(i);
            }
        }

        // Process each player who needs to be moved one by one
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

        // Update all players' button states
        for (int i = 0; i < playerInfoViews.size(); i++) {
            playerInfoViews.get(i).setButtonsEnabled(i == currentPlayerIndex);
        }

        // Check if water level has reached 10
        if (currentWaterLevel >= 10) {
            System.out.println("\n========== Game Over ==========");
            System.out.println("Water level has reached 10, game over!");
            JOptionPane.showMessageDialog(null, "Water level has reached 10, game over!");
            endGameWithLose("Water level has reached 10, game over!");
            return;
        }

        System.out.println("\n========== Starting flood card draw ==========");
        int floodCardCount;
        // Determine number of flood cards to draw based on water level
        if (currentWaterLevel <= 2) {
            floodCardCount = 2;
        } else if (currentWaterLevel <= 5) {
            floodCardCount = 3;
        } else if (currentWaterLevel <= 7) {
            floodCardCount = 4;
        } else {
            floodCardCount = 5;
        }

        System.out.println(
                "Current water level: " + currentWaterLevel + ", need to draw " + floodCardCount + " flood cards");

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
                        stateMsg = "sunk";
                        break;
                    default:
                        stateMsg = "normal";
                        break;
                }

                String stateChange = "";
                if (beforeState == TileState.NORMAL && targetTile.getState() == TileState.FLOODED) {
                    stateChange = "normal -> flooded";
                } else if (beforeState == TileState.FLOODED && targetTile.getState() == TileState.SUNK) {
                    stateChange = "flooded -> sunk";
                }

                System.out.println("[Log] Flood card drawn: " + targetTile.getName() +
                        " [Coordinates: " + targetTile.getRow() + "," + targetTile.getCol() + "]" +
                        ", State change: " + stateChange +
                        ", Current state: " + stateMsg);

                // If tile is sunk, immediately check if any players need emergency movement
                if (targetTile.getState() == TileState.SUNK) {
                    System.out.println("[Log] Tile " + targetTile.getName()
                            + " sunk, checking if players need emergency movement");
                    for (int j = 0; j < players.size(); j++) {
                        Player player = players.get(j);
                        if (player.getCurrentTile().equals(targetTile)) {
                            System.out.println("[Log] Player " + (j + 1) + " on sunk tile, needs emergency movement");
                            if (!handleEmergencyMove(j)) {
                                System.out.println("[Log] Player " + (j + 1) + " cannot move, game over");
                                endGameWithLose("Player " + (j + 1)
                                        + " is on a sunk tile and cannot move to another tile, game over!");
                                return;
                            }
                        }
                    }
                }
            } else {
                System.out.println("[Warning] Flood deck is empty!");
            }
        }
        System.out.println("========== Flood card draw complete ==========");

        // Check all failure conditions
        checkGameOver();
    }

    // Game over conditions
    private void checkGameOver() {
        // 1. Fool's Landing sunk
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

        // 2. Treasure tiles all sunk and corresponding treasures not collected
        // Temple (Earth treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.EARTH)) {
            boolean temple1Sunk = isTileSunk("TEMPLE_OF_THE_MOON");
            boolean temple2Sunk = isTileSunk("TEMPLE_OF_THE_SUN");
            if (temple1Sunk && temple2Sunk) {
                endGameWithLose("All temples sunk and Earth treasure not collected, game over!");
                return;
            }
        }
        // Cave (Fire treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.FIRE)) {
            boolean cave1Sunk = isTileSunk("CAVE_OF_SHADOWS");
            boolean cave2Sunk = isTileSunk("CAVE_OF_EMBERS");
            if (cave1Sunk && cave2Sunk) {
                endGameWithLose("All caves sunk and Fire treasure not collected, game over!");
                return;
            }
        }
        // Garden (Wind treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.WIND)) {
            boolean garden1Sunk = isTileSunk("WHISPERING_GARDEN");
            boolean garden2Sunk = isTileSunk("HOWLING_GARDEN");
            if (garden1Sunk && garden2Sunk) {
                endGameWithLose("All gardens sunk and Wind treasure not collected, game over!");
                return;
            }
        }
        // Palace (Water treasure)
        if (!treasureDeck.isTreasureCollected(Model.Enumeration.TreasureType.WATER)) {
            boolean palace1Sunk = isTileSunk("CORAL_PALACE");
            boolean palace2Sunk = isTileSunk("TIDAL_PALACE");
            if (palace1Sunk && palace2Sunk) {
                endGameWithLose("All palaces sunk and Water treasure not collected, game over!");
                return;
            }
        }

        // 3. Player piece on sunk tile with no adjacent tiles to move to
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
                    endGameWithLose("Player piece on sunk tile with no adjacent tiles to move to, game over!");
                    return;
                }
            }
        }
    }

    // Auxiliary method: Determine whether the plate with the specified name has sunk
    private boolean isTileSunk(String tileName) {
        for (Tile tile : mapController.getMapView().getAllTiles()) {
            if (tile.getName().name().equals(tileName)) {
                return tile.getState() == TileState.SUNK;
            }
        }
        return false;
    }

    // Game failure handling
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
                actionName.equals("Shore up");

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
                    // First, try to deal the cards. If successful, the subsequent process will be handled in the CardController regarding the action points
                    actionSuccess = requestGiveCard(playerIndex);
                    // Don't waste action points here
                    break;
                case "Special":
                    // Special skills do not consume action points here; instead, they are consumed after the skills are completed
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
     * Handling player movement
     * 
     * @param playerIndex
     */
    private void handleMove(int playerIndex) {
        System.out.println("\n========== Handling player movement ==========");
        // Enter the mobile mode and wait for the player to click on the target location
        mapController.enterMoveMode(playerIndex);
        System.out.println("========== Mobile processing completed ==========\n");
    }

    /**
     * Handling the acquisition of treasures
     * 
     * @param
     */
    private void handleGetTreasure(int playerIndex) {
        System.out.println("\n========== Processing treasure collection ==========");
        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();

        // Calculate number of each treasure card player has
        Map<TreasureType, Integer> treasureCardCounts = countTreasureCards(player);

        // Check if player is at corresponding treasure location
        TreasureType matchingTreasureType = getTreasureTypeForTile(currentTile.getName());

        if (matchingTreasureType == null) {
            System.out.println("Current position is not a treasure location");
            JOptionPane.showMessageDialog(null,
                    "Current position is not a treasure location, cannot collect treasure!");
            return;
        }

        // Check if player has enough treasure cards
        Integer cardCount = treasureCardCounts.getOrDefault(matchingTreasureType, 0);
        if (cardCount < 4) {
            System.out.println("Not enough treasure cards, need 4 " + matchingTreasureType.getDisplayName()
                    + " treasure cards, currently have " + cardCount);
            JOptionPane.showMessageDialog(null,
                    "Not enough treasure cards, need 4 " + matchingTreasureType.getDisplayName()
                            + " treasure cards, currently have " + cardCount);
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

        // Remove these cards from player's hand, do not discard to discard pile
        // (destroy directly)
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

        System.out.println("Successfully collected treasure: " + matchingTreasureType.getDisplayName());
        JOptionPane.showMessageDialog(null,
                "Successfully collected treasure: " + matchingTreasureType.getDisplayName() + "!");

        // Check if all treasures have been collected
        if (treasureDeck.allTreasuresCollected()) {
            System.out.println("All treasures have been collected!");
            JOptionPane.showMessageDialog(null,
                    "Congratulations! All treasures have been collected! Now head to the helicopter pad to escape the island!");
        }

        // Consume one action point
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        playerView.setActionPoints(currentActions - 1);

        // New: If action points are 0, automatically switch turns
        if (currentActions - 1 == 0) {
            endTurn(playerIndex);
        }

        System.out.println("========== Treasure collection processing complete ==========\n");
    }

    /**
     * Count the number of each type of treasure card that the players possess
     * 
     * @param player
     * @return The quantity of each treasure card
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
     * Obtain the corresponding treasure type based on the name of the tile
     * 
     * @param tileName
     * @return The corresponding type of treasure, if it is not a treasure location, then return null
     */
    private TreasureType getTreasureTypeForTile(TileName tileName) {
        switch (tileName) {
            case TEMPLE_OF_THE_MOON:
            case TEMPLE_OF_THE_SUN:
                return TreasureType.EARTH;
            case WHISPERING_GARDEN:
            case HOWLING_GARDEN:
                return TreasureType.WIND;
            case CAVE_OF_SHADOWS:
            case CAVE_OF_EMBERS:
                return TreasureType.FIRE;
            case CORAL_PALACE:
            case TIDAL_PALACE:
                return TreasureType.WATER;
            default:
                return null;
        }
    }

    /**
     * Obtain the index of the treasure view corresponding to the type of treasure
     * 
     * @param treasureType
     * @return Treasure View Index
     */
    private int getTreasureIndex(TreasureType treasureType) {
        switch (treasureType) {
            case EARTH:
                return 0;
            case FIRE:
                return 1;
            case WIND:
                return 2;
            case WATER:
                return 3;
            default:
                return -1;
        }
    }

    /**
     * Update the status of the treasure view
     * 
     * @param treasureIndex
     * @param found
     */
    private void updateTreasureViewStatus(int treasureIndex, boolean found) {
        if (boardView != null) {
            TreasureView treasureView = boardView.getTreasureView();
            if (treasureView != null) {
                treasureView.updateTreasureStatus(treasureIndex, found);
                return;
            }
        }
        System.out.println("无法找到TreasureView，宝物状态更新失败");
    }

    /**
     * Move the player to the designated position
     * 
     * @param playerIndex
     * @param row
     * @param col
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

        // Obtain the target section object (the unique Tile)
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile != null) {
            // Hidden player images of the original location
            mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(), playerIndex);

            // Update player position
            player.setCurrentTile(targetTile);

            // Display the player image at the new location
            String roleName = player.getRole().getClass().getSimpleName().toLowerCase();
            String playerImagePath = "src/resources/Player/" + roleName + "2.png";
            mapController.getMapView().showPlayerImage(row, col, playerImagePath, playerIndex);

            // Reduce the number of action points
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
            playerView.setActionPoints(currentActions - 1);

            System.out.printf("Player %d moved to: %s [%d, %d]\n",
                    playerIndex + 1,
                    targetTile.getName(),
                    targetTile.getRow(),
                    targetTile.getCol());

            // Check if the action points are 0. If so, automatically end the round
            if (currentActions - 1 == 0) {
                endTurn(playerIndex);
            }
        }
    }

    public void endTurn(int playerIndex) {
        Player currentPlayer = players.get(playerIndex);

        // Draw two treasure cards at the end of the round
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Card card = treasureDeck.draw();
            if (card != null) {
                drawnCards.add(card);
                // Directly add the cards to the player's hand without checking the hand limit
                currentPlayer.getHandCard().addCardWithoutCheck(card);
                PlayerInfoView playerInfoView = playerInfoViews.get(playerIndex);
                cardController.addCard(playerInfoView, card);
            }
        }

        // Check if there is a WaterRise card and use it immediately
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
                JOptionPane.showMessageDialog(null,
                        "The water level has risen by one notch! Current water level: " + currentWaterLevel);
            }
        }

        // Check the number of cards in your hand. If it exceeds 5, enter the discard
        // stage
        int cardCount = currentPlayer.getHandCard().getCards().size();
        System.out.println("Current number of cards in player's hand: " + cardCount); // Debug info

        if (cardCount > 5) {
            int cardsToDiscard = cardCount - 5;
            System.out.println("Need to discard " + cardsToDiscard + " cards"); // Debug info
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);

            // Disable all action buttons, only allow card selection
            playerView.setButtonsEnabled(false);

            // Enable card selection mode
            System.out.println("Preparing to enter discard mode, current player: " + playerIndex); // Debug info
            cardController.enableDiscardMode(playerView, cardsToDiscard);
            System.out.println("Entered discard mode"); // Debug info
            return; // Don't start new turn, wait for player to select cards to discard
        }

        // Check if the action points are 0. If so, automatically end the current round. If not, proceed to the next round
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

        if (currentActions == 0) {
            // Only start a new round directly when there is no need to fold
            startNewTurn();
        }
    }

    // Add the method for setting the MapView
    public void setMapView(MapView mapView) {
        System.out.println("\n========== Setting MapView ==========");
        System.out.println("MapView object: " + (mapView != null ? "not null" : "null"));
        this.tilePosition = mapView.getTilePosition();
        this.mapController = new MapController(this, mapView);

        // Each time the map is set up, the flood card deck is reinitialized
        List<Tile> allTiles = mapView.getAllTiles();
        this.floodDeck = new FloodDeck(allTiles);
        System.out.println("tilePosition object: " + (this.tilePosition != null ? "not null" : "null"));
        if (this.tilePosition != null) {
            Map<String, int[]> positions = this.tilePosition.getAllTilePositions();
            System.out.println("Available tile count: " + (positions != null ? positions.size() : 0));
            if (positions != null) {
                System.out.println("Available tile list:");
                positions.forEach((name, pos) -> System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));
            }
            // After setting the tilePosition, initialize the player's position
            System.out.println("Starting player position initialization...");
            initializePlayerPositions(mapView);
        }
        System.out.println("========== MapView setting completed ==========\n");
    }

    // Obtain the location of the specific section
    public int[] getTilePosition(String tileName) {
        if (tilePosition != null) {
            return tilePosition.getTilePosition(tileName);
        }
        return null;
    }

    // Obtain all the position information of the sections
    public Map<String, int[]> getAllTilePositions() {
        if (tilePosition != null) {
            return tilePosition.getAllTilePositions();
        }
        return null;
    }

    /**
     * Initialize player position
     * Randomly assign the players to different sections.
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
        System.out.println("Retrieved tile position information: " + (allPositions != null ? "not null" : "null"));

        if (allPositions == null || allPositions.isEmpty()) {
            System.err.println("Error: No available tile positions");
            System.out.println("Available tile count: " + (allPositions != null ? allPositions.size() : 0));
            System.out.println("========== Player position initialization failed ==========\n");
            return;
        }

        System.out.println("Available tile count: " + allPositions.size());
        System.out.println("Available tile list:");
        allPositions.forEach((name, pos) -> System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));

        // Convert tile positions to list for random selection
        List<String> availableTiles = new ArrayList<>(allPositions.keySet());
        System.out.println("Tile order before shuffling:");
        availableTiles.forEach(tile -> System.out.println("  - " + tile));

        java.util.Collections.shuffle(availableTiles); // Randomly shuffle order

        System.out.println("Tile order after shuffling:");
        availableTiles.forEach(tile -> System.out.println("  - " + tile));

        // Assign positions for each player
        System.out.println("\nStarting player position assignment:");
        for (int i = 0; i < players.size(); i++) {
            if (i >= availableTiles.size()) {
                System.err.println("Warning: Insufficient available tiles");
                break;
            }

            String tileName = availableTiles.get(i);
            int[] position = allPositions.get(tileName);

            System.out.printf("Assigning position for player %d:\n", i + 1);
            System.out.printf("  Selected tile: %s\n", tileName);
            System.out.printf("  Tile position: [%d, %d]\n", position[0], position[1]);

            // Use MapView's Tile object
            Tile tile = mapView.getTile(position[0], position[1]);
            players.get(i).setCurrentTile(tile);

            // Show player image
            String roleName = players.get(i).getRole().getClass().getSimpleName().toLowerCase();
            String playerImagePath = "src/resources/Player/" + roleName + "2.png";
            mapView.showPlayerImage(position[0], position[1], playerImagePath, i);

            System.out.printf("  Player %d position set complete\n", i + 1);
        }

        // Display all player position information
        displayPlayerPositions();
        System.out.println("========== Player position initialization complete ==========\n");
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
     * Check whether the player can reinforce the specified tiles
     * 
     * @param playerIndex
     * @param targetTile
     * @return If it can be strengthened, return true
     */
    public boolean canShoreUpTile(int playerIndex, Tile targetTile) {
        if (playerIndex != currentPlayerIndex || targetTile == null) {
            return false;
        }
        Player player = players.get(playerIndex);
        Role role = player.getRole();
        Tile currentTile = player.getCurrentTile();

        // Check if there are any sandbags stuck
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }

        // Check if there are any sandbags stuck. If there are, reinforce any submerged sections that are stuck
        if (hasSandbag && targetTile.getState() == TileState.FLOODED) {
            return true;
        }

        // Check if it is an explorer
        boolean isExplorer = role instanceof Model.Role.Explorer;

        // Calculate the Manhattan distance
        int distance = Math.abs(currentTile.getRow() - targetTile.getRow()) +
                Math.abs(currentTile.getCol() - targetTile.getCol());

        // If it is an explorer, diagonal reinforcement is allowed (with a distance of 2)
        boolean isAdjacent = isExplorer ? (Math.abs(currentTile.getRow() - targetTile.getRow()) <= 1 &&
                Math.abs(currentTile.getCol() - targetTile.getCol()) <= 1)
                : (currentTile.isAdjacentTo(targetTile) || currentTile.equals(targetTile));

        boolean isShoreable = targetTile.isShoreable();
        return isAdjacent && isShoreable;
    }

    /**
     * Check if the player can use the card
     * 
     * @param fromPlayerIndex
     * @param toPlayerIndex
     * @param card
     * @return If the card can be returned, return true
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
     * Check whether players can use special skills
     * 
     * @param playerIndex
     * @return If special skills can be used, return true
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
     * The view layer invokes this method to initiate the card distribution process
     * 
     * @return If the card distribution process is successfully initiated, return true; otherwise, return false
     */
    public boolean requestGiveCard(int fromPlayerIndex) {
        Player fromPlayer = players.get(fromPlayerIndex);
        List<Integer> candidateIndexes = new ArrayList<>();
        boolean sameLocation = false;

        // Create a list of player options
        List<String> playerOptionsList = new ArrayList<>();

        // Traverse all players
        for (int i = 0; i < players.size(); i++) {
            if (i != fromPlayerIndex) {
                Player targetPlayer = players.get(i);
                // Check if it is in the same position
                sameLocation = fromPlayer.getCurrentTile().equals(targetPlayer.getCurrentTile());
                // If it is a messenger or in the same position, then the card can be given
                if (fromPlayer.getRole() instanceof Model.Role.Messenger || sameLocation) {
                    candidateIndexes.add(i);
                    String location = sameLocation ? "(same location)" : "(different location)";
                    playerOptionsList.add(String.format("Player%d - %s %s",
                            i + 1,
                            targetPlayer.getRole().getClass().getSimpleName(),
                            location));
                }
            }
        }

        //Add the option to cancel
        playerOptionsList.add("Cancel");

        if (candidateIndexes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No player to give cards!");
            return false;
        }

        String[] playerOptions = playerOptionsList.toArray(new String[0]);

        // Allow players to select the target player
        int selectedOption = JOptionPane.showOptionDialog(
                null,
                "Select player to give cards:",
                "Select target player",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]);

        if (selectedOption == -1 || selectedOption == playerOptions.length - 1) {
            System.out.println("[Log] Player cancelled selection.");
            return false;
        }

        // Obtain the index of the selected target player
        int toPlayerIndex = candidateIndexes.get(selectedOption);
        System.out.println("[Log] Player selected target player: " + (toPlayerIndex + 1));

        // Obtain the current player's hand cards
        List<Card> handCards = fromPlayer.getHandCard().getCards();
        if (handCards.isEmpty()) {
            JOptionPane.showMessageDialog(null, "You don't have cards to give!");
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

        // Allow players to choose the cards they want to give
        int selectedCard = JOptionPane.showOptionDialog(
                null,
                "Select card to give:",
                "Select card",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                cardOptions,
                cardOptions[0]);

        if (selectedCard == -1 || selectedCard == cardOptions.length - 1) {
            System.out.println("[Log] Player cancelled selection.");
            return false;
        }

        // Perform the card-dealing operation
        Card selectedCardObj = handCards.get(selectedCard);
        cardController.pendingGiveCardPlayerIndex = fromPlayerIndex;
        return cardController.giveCard(fromPlayerIndex, toPlayerIndex, selectedCardObj);
    }

    /**
     * Carry out reinforcement operations
     * 
     * @param playerIndex
     * @return If the reinforcement operation is successfully initiated, return
     *         true; otherwise, return false
     */
    public boolean handleShoreUp(int playerIndex) {
        Player player = players.get(playerIndex);
        Role role = player.getRole();

        // Check if the player has any sandbags stuck
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }

        // If there is no sandbag jam, check if there are sufficient action points
        if (!hasSandbag) {
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

            if (currentActions <= 0) {
                System.out.println("[Log] The player does not have enough action points to reinforce");
                JOptionPane.showMessageDialog(null, "You don't have enough action points for reinforcement!");
                return false;
            }
        }

        // If it is an engineer, the two sections can be reinforced
        if (role instanceof Model.Role.Engineer) {
            System.out.println("[Log] Engineers can reinforce the two sections.");
            JOptionPane.showMessageDialog(null,
                    "As an engineer, you can reinforce a maximum of two sections in a row!");
            engineerShoreUpCount = 0;
            isEngineerShoreUpMode = true;
            engineerSandbagConsumed = false;
        } else {
            isEngineerShoreUpMode = false;
        }

        // Enter the reinforcement mode and wait for the player to select the tiles to be reinforced
        System.out.println("[Log] Entering shore up mode, please select tile to shore up");
        mapController.enterShoreUpMode(playerIndex);
        return true;
    }

    /**
     * Handling special skills
     * 
     * @param playerIndex
     */
    private void handleSpecialSkill(int playerIndex) {
        Player player = players.get(playerIndex);
        Role role = player.getRole();

        if (role == null) {
            System.out.println("[Log] The player does not have a character and thus cannot use special skills.");
            return;
        }

        // Check if the special skills can be used
        if (!role.canUseAbility()) {
            System.out.println("[Log] Currently, special skills cannot be used.");
            JOptionPane.showMessageDialog(null, "Currently, the special skills cannot be used!");
            return;
        }

        // Handle special skills based on character types
        if (role instanceof Model.Role.Pilot) {
            // Pilots can fly to any location.
            System.out.println("[Log] Pilots can utilize their flight capabilities");
            mapController.enterMoveMode(playerIndex);
            role.useSpecialAbility();

            // After using a special skill, the pilot immediately depletes one action point
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
            playerView.setActionPoints(currentActions - 1);

            // If the action points are exhausted, end the round
            if (currentActions - 1 == 0) {
                endTurn(playerIndex);
            }
        } else if (role instanceof Model.Role.Navigator) {
            // The navigator can move other players
            System.out.println("[Log] Navigator can use the ability to move other players");
            handleNavigatorAbility(playerIndex);
        } else {
            System.out.println("[Log] This character does not have a special skill to use actively");
            JOptionPane.showMessageDialog(null, "This character does not have a special skill to use actively!");
        }
    }

    /**
     * Handling the special abilities of the navigators
     * 
     * @param navigatorIndex
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
            playerOptions[i] = "Player " + (players.indexOf(p) + 1) + " (" + p.getRole().getClass().getSimpleName()
                    + ")";
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
            System.out.println("[Log] Player cancelled selection");
            return;
        }

        // Get selected player
        Player targetPlayer = otherPlayers.get(selectedPlayerIndex);
        int targetPlayerIndex = players.indexOf(targetPlayer);

        // Set target player's move count to 2
        PlayerInfoView targetPlayerView = playerInfoViews.get(targetPlayerIndex);
        targetPlayerView.setActionPoints(2);

        // Use navigator's ability
        Role navigatorRole = players.get(navigatorIndex).getRole();
        if (navigatorRole != null) {
            navigatorRole.useSpecialAbility();
        }

        // Enter move mode, but moving the target player
        System.out.println("[Log] Entering navigator move mode, moving player " + (targetPlayerIndex + 1));
        mapController.enterNavigatorMoveMode(navigatorIndex, targetPlayerIndex);

        // Show prompt message
        JOptionPane.showMessageDialog(null,
                "Player " + (targetPlayerIndex + 1) + " can now move twice!\n" +
                        "After completing two moves, one action point will be consumed from the navigator.",
                "Navigator Ability",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Move other players to the designated position (used by the navigator)
     * 
     * @param navigatorIndex
     * @param targetPlayerIndex
     * @param row
     * @param col
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

        // Obtain the target section object
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile != null) {
            // Check whether the movement is legal
            if (isValidNavigatorMove(targetPlayer, targetTile)) {
                //Hidden player images of the original location
                mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(),
                        targetPlayerIndex);

                // Update player position
                targetPlayer.setCurrentTile(targetTile);

                // Display the player image at the new location
                String roleName = targetPlayer.getRole().getClass().getSimpleName().toLowerCase();
                String playerImagePath = "src/resources/Player/" + roleName + "2.png";
                mapController.getMapView().showPlayerImage(row, col, playerImagePath, targetPlayerIndex);

                System.out.printf("Navigator moved player %d to: %s [%d, %d]\n",
                        targetPlayerIndex + 1,
                        targetTile.getName(),
                        targetTile.getRow(),
                        targetTile.getCol());

                // Reduce the number of movements of the target players
                PlayerInfoView targetPlayerView = playerInfoViews.get(targetPlayerIndex);
                String actionText = targetPlayerView.getActionPointsLabel().getText();
                int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
                targetPlayerView.setActionPoints(currentActions - 1);

                // If the target player has no remaining movement points, consume one action point of the navigator and exit the movement mode
                if (currentActions - 1 == 0) {
                    mapController.exitNavigatorMoveMode();

                    JOptionPane.showMessageDialog(null,
                            "Target player completed two moves!\n" +
                                    "One action point has been consumed from the navigator.",
                            "Navigator Ability",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Deplete one action point of the navigator
                    PlayerInfoView navigatorView = playerInfoViews.get(navigatorIndex);
                    String navigatorActionText = navigatorView.getActionPointsLabel().getText();
                    int navigatorActions = Integer.parseInt(navigatorActionText.split(":")[1].trim());
                    navigatorView.setActionPoints(navigatorActions - 1);

                    // If the navigator has no remaining action points, end the round
                    if (navigatorActions - 1 == 0) {
                        endTurn(navigatorIndex);
                    }
                } else {
                    // Asking whether to proceed with the second move
                    int option = JOptionPane.showConfirmDialog(null,
                            "Do you want to continue with the second move?\nSelecting 'No' will directly consume one action point from the navigator and end this ability.",
                            "Navigator Ability",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.NO_OPTION) {
                        // The player chose not to continue the second move
                        mapController.exitNavigatorMoveMode();
                        PlayerInfoView navigatorView = playerInfoViews.get(navigatorIndex);
                        String navigatorActionText = navigatorView.getActionPointsLabel().getText();
                        int navigatorActions = Integer.parseInt(navigatorActionText.split(":")[1].trim());
                        navigatorView.setActionPoints(navigatorActions - 1);
                        if (navigatorActions - 1 == 0) {
                            endTurn(navigatorIndex);
                        }
                    } else {
                        // The player chooses to continue, and it is indicated that they can move one more time
                        JOptionPane.showMessageDialog(null,
                                "Player " + (targetPlayerIndex + 1) + " can still move 1 time!",
                                "Navigator Ability",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                System.out.println("[Log] Illegal move: Target position unreachable");
                JOptionPane.showMessageDialog(null, "Illegal move: Target position unreachable", "Move Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Check whether the navigator's movement of other players is legal
     * 
     * @param targetPlayer
     * @param targetTile
     * @return If the movement is legal, return true
     */
    private boolean isValidNavigatorMove(Player targetPlayer, Tile targetTile) {
        // Just check if it has sunk. The submerged tiles can be moved
        if (targetTile.getState() == TileState.SUNK) {
            System.out.println("Target tile is sunk, cannot move");
            return false;
        }

        Tile currentTile = targetPlayer.getCurrentTile();
        if (currentTile == null) {
            System.out.println("Cannot get current tile");
            return false;
        }

        // Check whether the target player is a pilot
        boolean isPilot = targetPlayer.getRole() instanceof Model.Role.Pilot;
        if (isPilot) {
            return true;
        }

        // Check whether the target player is a diver
        boolean isDiver = targetPlayer.getRole().getClass().getSimpleName().equals("Diver");
        if (isDiver) {
            // Reusing the BFS determination of MapController
            return mapController != null && mapController.isDiverReachable(currentTile, targetTile);
        }

        //Check whether the target player is an explorer
        boolean isExplorer = targetPlayer.getRole() instanceof Model.Role.Explorer;
        int rowDistance = Math.abs(currentTile.getRow() - targetTile.getRow());
        int colDistance = Math.abs(currentTile.getCol() - targetTile.getCol());
        if (isExplorer) {
            return rowDistance <= 1 && colDistance <= 1;
        }
        // Other players can only move to adjacent squares
        return (rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1);
    }

    /**
     * Reinforce the specified tiles
     *
     * @param playerIndex
     * @param row
     * @param col
     */
    public void shoreUpTile(int playerIndex, int row, int col) {
        System.out.println("\n========== Starting shore up tile ==========");
        System.out.println("Player index: " + playerIndex);
        System.out.println("Target position: [" + row + "," + col + "]");

        Player player = players.get(playerIndex);
        System.out.println("Player role: " + player.getRole().getClass().getSimpleName());
        System.out.println("Is it an engineer: " + (player.getRole() instanceof Model.Role.Engineer));

        Tile targetTile = mapController.getMapView().getTile(row, col);
        System.out.println("Target tile: " + targetTile.getName());
        System.out.println("Target tile state: " + targetTile.getState());

        if (canShoreUpTile(playerIndex, targetTile)) {
            System.out.println("Can shore up this tile");
            targetTile.setState(TileState.NORMAL);
            System.out.println("Tile shore up complete, new state: " + targetTile.getState());

            // Check if it is the engineer's second reinforcement
            if (player.getRole() instanceof Model.Role.Engineer) {
                System.out.println("Player is engineer, checking if can continue shore up");
                System.out.println("Current shore up count: " + engineerShoreUpCount);

                // Check if there are any other sections around that can be reinforced
                boolean hasMoreShoreableTiles = false;
                Tile currentTile = player.getCurrentTile();
                for (Tile adjacentTile : currentTile.getAdjacentTiles()) {
                    if (adjacentTile != targetTile && canShoreUpTile(playerIndex, adjacentTile)) {
                        hasMoreShoreableTiles = true;
                        break;
                    }
                }

                if (engineerShoreUpCount < 1 && hasMoreShoreableTiles) {
                    System.out.println("Engineer can continue shore up");
                    engineerShoreUpCount++;
                    System.out.println("Shore up count updated: " + engineerShoreUpCount);
                    return;
                }
            }

            System.out.println("Shore up action completed");
            endShoreUpAction();
        } else {
            System.out.println("Cannot shore up this tile");
        }
        System.out.println("========== Shore up tile completed ==========\n");
    }

    private void endShoreUpAction() {
        System.out.println("\n========== Ending shore up action ==========");
        System.out.println("Current shore up count: " + engineerShoreUpCount);
        System.out.println(
                "Current player role: " + players.get(currentPlayerIndex).getRole().getClass().getSimpleName());
        System.out.println(
                "Is it an engineer: " + (players.get(currentPlayerIndex).getRole() instanceof Model.Role.Engineer));

        // Update player information view
        PlayerInfoView playerView = playerInfoViews.get(currentPlayerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        playerView.setActionPoints(currentActions - 1);
        System.out.println("Updated player action points: " + (currentActions - 1));

        //Reset the number of engineer reinforcement operations
        engineerShoreUpCount = 0;
        System.out.println("Shore up count reset to: " + engineerShoreUpCount);

        mapController.exitShoreUpMode();
        System.out.println("Exited shore up mode");

        // If the action points are exhausted, end the round
        if (currentActions - 1 == 0) {
            System.out.println("Action points used up, ending turn");
            endTurn(currentPlayerIndex);
        }

        System.out.println("========== Shore up action completed ==========\n");
    }

    /**
     * Use sandbags to reinforce any submerged sections
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
        // Perform shore up
        targetTile.setState(Model.Enumeration.TileState.NORMAL);
        player.getHandCard().removeCard(sandbagCard);
        playerInfoViews.get(playerIndex).removeCard(sandbagCard);
        treasureDeck.discard(sandbagCard);
        JOptionPane.showMessageDialog(null, "Successfully used sandbag card to shore up tile!");
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
        JOptionPane.showMessageDialog(null,
                "Congratulations! You have collected all treasures and successfully escaped, victory!");
        System.out.println("========== Game Victory ==========");
        // Return to main menu
        View.MainView mainView = View.MainView.getInstance();
        if (mainView != null) {
            mainView.showStartScreen();
        }
    }

    // Add the method for setting BoardView
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
     * Check the conditions for game victory
     * When all the treasures have been collected and all the players are at the helipad, the game is won
     */
    public void checkGameWin() {
        //Check if all the treasures have been collected
        if (!treasureDeck.allTreasuresCollected()) {
            return;
        }

        // Check to see if all the players are at the helipad
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
        // Check if player still has action points
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        if (currentActions <= 0) {
            // No action points, switch to next player
            startNewTurn();
            return;
        }
        // Restore player's buttons and action points display, don't switch turns
        for (int i = 0; i < playerInfoViews.size(); i++) {
            playerInfoViews.get(i).setButtonsEnabled(i == playerIndex);
        }
        updatePlayerView(playerIndex);
    }
}