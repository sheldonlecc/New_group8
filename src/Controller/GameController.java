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

    // 辅助方法：判断指定名称的板块是否沉没
    private boolean isTileSunk(String tileName) {
        for (Tile tile : mapController.getMapView().getAllTiles()) {
            if (tile.getName().name().equals(tileName)) {
                return tile.getState() == TileState.SUNK;
            }
        }
        return false;
    }

    // 游戏失败处理
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
                actionName.equals("Shore up"); // 添加加固操作到消耗行动点的操作列表中

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
                    // 先尝试给牌，如果成功，后续流程在CardController里处理行动点
                    actionSuccess = requestGiveCard(playerIndex);
                    // 不在这里消耗行动点
                    break;
                case "Special":
                    // 特殊技能不在这里消耗行动点，而是在技能完成后消耗
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
     * 处理玩家移动
     * 
     * @param playerIndex 玩家索引
     */
    private void handleMove(int playerIndex) {
        System.out.println("\n========== 处理玩家移动 ==========");
        // 进入移动模式，等待玩家点击目标位置
        mapController.enterMoveMode(playerIndex);
        System.out.println("========== 移动处理完成 ==========\n");
    }

    /**
     * 处理获取宝物
     * 
     * @param playerIndex 玩家索引
     */
    private void handleGetTreasure(int playerIndex) {
        System.out.println("\n========== 处理获取宝物 ==========");
        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();

        // 计算玩家拥有的每种宝物卡的数量
        Map<TreasureType, Integer> treasureCardCounts = countTreasureCards(player);

        // 检查玩家是否在对应的宝物地点
        TreasureType matchingTreasureType = getTreasureTypeForTile(currentTile.getName());

        if (matchingTreasureType == null) {
            System.out.println("当前位置不是宝物地点");
            JOptionPane.showMessageDialog(null, "当前位置不是宝物地点，无法获取宝物！");
            return;
        }

        // 检查是否有足够的宝物卡
        Integer cardCount = treasureCardCounts.getOrDefault(matchingTreasureType, 0);
        if (cardCount < 4) {
            System.out.println("没有足够的宝物卡，需要4张" + matchingTreasureType.getDisplayName() + "宝物卡，当前只有" + cardCount + "张");
            JOptionPane.showMessageDialog(null,
                    "没有足够的宝物卡，需要4张" + matchingTreasureType.getDisplayName() + "宝物卡，当前只有" + cardCount + "张");
            return;
        }

        // 移除4张宝物卡
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

        // 从玩家手牌中移除这些卡，不再丢弃到弃牌堆（直接销毁）
        for (Card card : cardsToRemove) {
            player.getHandCard().removeCard(card);
            playerInfoViews.get(playerIndex).removeCard(card);
            // 不再调用 treasureDeck.discard(card);
        }

        // 记录宝物收集
        treasureDeck.recordTreasureCollection(matchingTreasureType);

        // 更新宝物视图
        int treasureIndex = getTreasureIndex(matchingTreasureType);
        updateTreasureViewStatus(treasureIndex, true);

        System.out.println("成功获取宝物：" + matchingTreasureType.getDisplayName());
        JOptionPane.showMessageDialog(null, "成功获取宝物：" + matchingTreasureType.getDisplayName() + "！");

        // 检查是否收集齐所有宝物
        if (treasureDeck.allTreasuresCollected()) {
            System.out.println("已收集齐全部宝物！");
            JOptionPane.showMessageDialog(null, "恭喜！已收集齐全部宝物！现在前往直升机场逃离岛屿吧！");
        }

        // 消耗一个行动点
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        playerView.setActionPoints(currentActions - 1);

        // 新增：如果行动点为0，自动切换回合
        if (currentActions - 1 == 0) {
            endTurn(playerIndex);
        }

        System.out.println("========== 获取宝物处理完成 ==========\n");
    }

    /**
     * 统计玩家拥有的每种宝物卡的数量
     * 
     * @param player 玩家
     * @return 每种宝物卡的数量
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
     * 根据瓦片名称获取对应的宝物类型
     * 
     * @param tileName 瓦片名称
     * @return 对应的宝物类型，如果不是宝物地点则返回null
     */
    private TreasureType getTreasureTypeForTile(TileName tileName) {
        switch (tileName) {
            case TEMPLE_OF_THE_MOON:
            case TEMPLE_OF_THE_SUN:
                return TreasureType.EARTH; // 地球宝藏 - 神庙
            case WHISPERING_GARDEN:
            case HOWLING_GARDEN:
                return TreasureType.WIND; // 风之宝藏 - 花园
            case CAVE_OF_SHADOWS:
            case CAVE_OF_EMBERS:
                return TreasureType.FIRE; // 火焰宝藏 - 洞穴
            case CORAL_PALACE:
            case TIDAL_PALACE:
                return TreasureType.WATER; // 水之宝藏 - 宫殿
            default:
                return null; // 不是宝物地点
        }
    }

    /**
     * 获取宝物类型对应的宝物视图索引
     * 
     * @param treasureType 宝物类型
     * @return 宝物视图索引
     */
    private int getTreasureIndex(TreasureType treasureType) {
        switch (treasureType) {
            case EARTH:
                return 0; // Earth索引为0
            case FIRE:
                return 1; // Fire索引为1
            case WIND:
                return 2; // Wind索引为2
            case WATER:
                return 3; // Water索引为3
            default:
                return -1;
        }
    }

    /**
     * 更新宝物视图的状态
     * 
     * @param treasureIndex 宝物索引
     * @param found         是否已找到宝物
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
     * 移动玩家到指定位置
     * 
     * @param playerIndex 玩家索引
     * @param row         目标行
     * @param col         目标列
     */
    public void movePlayer(int playerIndex, int row, int col) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }

        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        System.out.printf("玩家 %d 当前位置: %s [%d, %d]\n",
                playerIndex + 1,
                currentTile.getName(),
                currentTile.getRow(),
                currentTile.getCol());

        // 获取目标板块对象（唯一Tile）
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile != null) {
            // 隐藏原位置的玩家图像
            mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(), playerIndex);

            // 更新玩家位置
            player.setCurrentTile(targetTile);

            // 显示新位置的玩家图像
            String roleName = player.getRole().getClass().getSimpleName().toLowerCase();
            String playerImagePath = "src/resources/Player/" + roleName + "2.png";
            mapController.getMapView().showPlayerImage(row, col, playerImagePath, playerIndex);

            // 减少行动点数
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
            playerView.setActionPoints(currentActions - 1);

            System.out.printf("玩家 %d 移动到: %s [%d, %d]\n",
                    playerIndex + 1,
                    targetTile.getName(),
                    targetTile.getRow(),
                    targetTile.getCol());

            // 检查行动点数是否为0，如果是则自动结束回合
            if (currentActions - 1 == 0) {
                endTurn(playerIndex);
            }
        }
    }

    public void endTurn(int playerIndex) {
        Player currentPlayer = players.get(playerIndex);

        // 在回合结束时抽两张宝藏卡
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Card card = treasureDeck.draw();
            if (card != null) {
                drawnCards.add(card);
                // 直接添加卡牌到玩家手中，不检查手牌上限
                currentPlayer.getHandCard().addCardWithoutCheck(card);
                PlayerInfoView playerInfoView = playerInfoViews.get(playerIndex);
                cardController.addCard(playerInfoView, card);
            }
        }

        // 检查是否有WaterRise卡牌并立即使用
        List<Card> cards = currentPlayer.getHandCard().getCards();
        for (Card card : new ArrayList<>(cards)) {
            if (card instanceof WaterRiseCard) {
                ((WaterRiseCard) card).useCard();
                currentPlayer.getHandCard().removeCard(card);
                playerInfoViews.get(playerIndex).removeCard(card);
                treasureDeck.discard(card);
                // 更新水位
                currentWaterLevel++;
                waterLevelView.updateWaterLevel(currentWaterLevel);
                JOptionPane.showMessageDialog(null, "水位上升了一格！当前水位：" + currentWaterLevel);
            }
        }

        // 检查手牌数量，如果超过5张，进入弃牌阶段
        int cardCount = currentPlayer.getHandCard().getCards().size();
        System.out.println("当前玩家手牌数量: " + cardCount); // 调试信息

        if (cardCount > 5) {
            int cardsToDiscard = cardCount - 5;
            System.out.println("需要弃掉 " + cardsToDiscard + " 张卡牌"); // 调试信息
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);

            // 禁用所有动作按钮，只允许选择弃牌
            playerView.setButtonsEnabled(false);

            // 启用卡牌选择模式
            System.out.println("准备进入弃牌模式，当前玩家: " + playerIndex); // 调试信息
            cardController.enableDiscardMode(playerView, cardsToDiscard);
            System.out.println("已进入弃牌模式"); // 调试信息
            return; // 不开始新回合，等待玩家选择弃牌
        }

        // 检查行动点数是否为0，如果是则开始新回合
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

        if (currentActions == 0) {
            // 只有在不需要弃牌时才直接开始新回合
            startNewTurn();
        }
    }

    // 添加设置MapView的方法
    public void setMapView(MapView mapView) {
        System.out.println("\n========== 设置MapView ==========");
        System.out.println("MapView对象: " + (mapView != null ? "非空" : "为空"));
        this.tilePosition = mapView.getTilePosition();
        this.mapController = new MapController(this, mapView);

        // 每次设置地图时都重新初始化洪水牌堆
        List<Tile> allTiles = mapView.getAllTiles();
        this.floodDeck = new FloodDeck(allTiles);
        System.out.println("tilePosition对象: " + (this.tilePosition != null ? "非空" : "为空"));
        if (this.tilePosition != null) {
            Map<String, int[]> positions = this.tilePosition.getAllTilePositions();
            System.out.println("可用板块数量: " + (positions != null ? positions.size() : 0));
            if (positions != null) {
                System.out.println("可用板块列表:");
                positions.forEach((name, pos) -> System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));
            }
            // 在设置完tilePosition后初始化玩家位置
            System.out.println("正在初始化玩家位置...");
            initializePlayerPositions(mapView);
        }
        System.out.println("========== MapView设置完成 ==========\n");
    }

    // 获取特定板块的位置
    public int[] getTilePosition(String tileName) {
        if (tilePosition != null) {
            return tilePosition.getTilePosition(tileName);
        }
        return null;
    }

    // 获取所有板块位置信息
    public Map<String, int[]> getAllTilePositions() {
        if (tilePosition != null) {
            return tilePosition.getAllTilePositions();
        }
        return null;
    }

    /**
     * 初始化玩家位置
     * 将玩家随机分配到不同的板块上
     */
    private void initializePlayerPositions(MapView mapView) {
        System.out.println("\n========== 开始初始化玩家位置 ==========");
        System.out.println("当前玩家数量: " + players.size());

        if (tilePosition == null) {
            System.err.println("错误：tilePosition未初始化");
            System.out.println("tilePosition对象: " + (tilePosition != null ? "非空" : "为空"));
            System.out.println("========== 玩家位置初始化失败 ==========\n");
            return;
        }

        // 获取所有可用的板块位置
        Map<String, int[]> allPositions = tilePosition.getAllTilePositions();
        System.out.println("获取到的板块位置信息: " + (allPositions != null ? "非空" : "为空"));

        if (allPositions == null || allPositions.isEmpty()) {
            System.err.println("错误：没有可用的板块位置");
            System.out.println("可用板块数量: " + (allPositions != null ? allPositions.size() : 0));
            System.out.println("========== 玩家位置初始化失败 ==========\n");
            return;
        }

        System.out.println("可用板块数量: " + allPositions.size());
        System.out.println("可用板块列表:");
        allPositions.forEach((name, pos) -> System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));

        // 将板块位置转换为列表，方便随机选择
        List<String> availableTiles = new ArrayList<>(allPositions.keySet());
        System.out.println("随机打乱前的板块顺序:");
        availableTiles.forEach(tile -> System.out.println("  - " + tile));

        java.util.Collections.shuffle(availableTiles); // 随机打乱顺序

        System.out.println("随机打乱后的板块顺序:");
        availableTiles.forEach(tile -> System.out.println("  - " + tile));

        // 为每个玩家分配位置
        System.out.println("\n开始为玩家分配位置:");
        for (int i = 0; i < players.size(); i++) {
            if (i >= availableTiles.size()) {
                System.err.println("警告：可用板块数量不足");
                break;
            }

            String tileName = availableTiles.get(i);
            int[] position = allPositions.get(tileName);

            System.out.printf("正在为玩家 %d 分配位置:\n", i + 1);
            System.out.printf("  选择的板块: %s\n", tileName);
            System.out.printf("  板块位置: [%d, %d]\n", position[0], position[1]);

            // 用MapView的Tile对象
            Tile tile = mapView.getTile(position[0], position[1]);
            players.get(i).setCurrentTile(tile);

            // 显示玩家图像
            String roleName = players.get(i).getRole().getClass().getSimpleName().toLowerCase();
            String playerImagePath = "src/resources/Player/" + roleName + "2.png";
            mapView.showPlayerImage(position[0], position[1], playerImagePath, i);

            System.out.printf("  玩家 %d 位置设置完成\n", i + 1);
        }

        // 显示所有玩家的位置信息
        displayPlayerPositions();
        System.out.println("========== 玩家位置初始化完成 ==========\n");
    }

    /**
     * 显示所有玩家的位置信息
     */
    private void displayPlayerPositions() {
        System.out.println("\n========== 玩家位置信息 ==========");
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Tile currentTile = player.getCurrentTile();
            if (currentTile != null) {
                System.out.printf("玩家 %d (%s):\n",
                        i + 1,
                        player.getRole() != null ? player.getRole().getClass().getSimpleName() : "未分配角色");
                System.out.printf("  板块: %s\n", currentTile.getName());
                System.out.printf("  位置: [%d, %d]\n", currentTile.getRow(), currentTile.getCol());
                System.out.printf("  状态: %s\n", currentTile.getState());
            } else {
                System.out.printf("玩家 %d: 未分配位置\n", i + 1);
            }
        }
        System.out.println("================================\n");
    }

    /**
     * 检查玩家是否可以加固指定瓦片
     * 
     * @param playerIndex 玩家索引
     * @param targetTile  目标瓦片
     * @return 如果可以加固则返回true
     */
    public boolean canShoreUpTile(int playerIndex, Tile targetTile) {
        if (playerIndex != currentPlayerIndex || targetTile == null) {
            return false;
        }
        Player player = players.get(playerIndex);
        Role role = player.getRole();
        Tile currentTile = player.getCurrentTile();

        // 检查是否有沙袋卡
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }

        // 如果有沙袋卡，可以加固任意被淹没的板块
        if (hasSandbag && targetTile.getState() == TileState.FLOODED) {
            return true;
        }

        // 检查是否是探险家
        boolean isExplorer = role instanceof Model.Role.Explorer;

        // 计算曼哈顿距离
        int distance = Math.abs(currentTile.getRow() - targetTile.getRow()) +
                Math.abs(currentTile.getCol() - targetTile.getCol());

        // 如果是探险家，允许斜向加固（距离为2）
        boolean isAdjacent = isExplorer ? (Math.abs(currentTile.getRow() - targetTile.getRow()) <= 1 &&
                Math.abs(currentTile.getCol() - targetTile.getCol()) <= 1)
                : (currentTile.isAdjacentTo(targetTile) || currentTile.equals(targetTile));

        boolean isShoreable = targetTile.isShoreable();
        return isAdjacent && isShoreable;
    }

    /**
     * 检查玩家是否可以给卡
     * 
     * @param fromPlayerIndex 给卡玩家索引
     * @param toPlayerIndex   收卡玩家索引
     * @param card            要给的卡牌
     * @return 如果可以给卡则返回true
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
     * 检查玩家是否可以使用特殊技能
     * 
     * @param playerIndex 玩家索引
     * @return 如果可以使用特殊技能则返回true
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
     * View层调用此方法发起给牌流程
     * 
     * @return 如果成功发起给牌流程返回true，否则返回false
     */
    public boolean requestGiveCard(int fromPlayerIndex) {
        Player fromPlayer = players.get(fromPlayerIndex);
        List<Integer> candidateIndexes = new ArrayList<>();
        boolean sameLocation = false;

        // 构建玩家选项列表
        List<String> playerOptionsList = new ArrayList<>();

        // 遍历所有玩家
        for (int i = 0; i < players.size(); i++) {
            if (i != fromPlayerIndex) {
                Player targetPlayer = players.get(i);
                // 检查是否在同一位置
                sameLocation = fromPlayer.getCurrentTile().equals(targetPlayer.getCurrentTile());
                // 如果是信使或者在同一位置，则可以给牌
                if (fromPlayer.getRole() instanceof Model.Role.Messenger || sameLocation) {
                    candidateIndexes.add(i);
                    String location = sameLocation ? "(同一位置)" : "(不同位置)";
                    playerOptionsList.add(String.format("玩家%d - %s %s",
                            i + 1,
                            targetPlayer.getRole().getClass().getSimpleName(),
                            location));
                }
            }
        }

        // 添加取消选项
        playerOptionsList.add("取消");

        if (candidateIndexes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "没有可以给牌的玩家！");
            return false;
        }

        // 将List转换为数组
        String[] playerOptions = playerOptionsList.toArray(new String[0]);

        // 让玩家选择目标玩家
        int selectedOption = JOptionPane.showOptionDialog(
                null,
                "请选择要给牌的玩家：",
                "选择目标玩家",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]);

        if (selectedOption == -1 || selectedOption == playerOptions.length - 1) {
            System.out.println("[日志] 玩家取消了选择目标玩家。");
            return false;
        }

        // 获取选中的目标玩家索引
        int toPlayerIndex = candidateIndexes.get(selectedOption);
        System.out.println("[日志] 玩家选择了目标玩家: " + (toPlayerIndex + 1));

        // 获取当前玩家的手牌
        List<Card> handCards = fromPlayer.getHandCard().getCards();
        if (handCards.isEmpty()) {
            JOptionPane.showMessageDialog(null, "您没有可以给牌的卡牌！");
            return false;
        }

        // 构建卡牌选项
        String[] cardOptions = new String[handCards.size() + 1];
        for (int i = 0; i < handCards.size(); i++) {
            Card card = handCards.get(i);
            if (card instanceof TreasureCard) {
                TreasureCard treasureCard = (TreasureCard) card;
                cardOptions[i] = treasureCard.getTreasureType().getDisplayName() + "宝藏卡";
            } else {
                cardOptions[i] = card.getClass().getSimpleName();
            }
        }
        cardOptions[handCards.size()] = "取消";

        // 让玩家选择要给的卡牌
        int selectedCard = JOptionPane.showOptionDialog(
                null,
                "请选择要给的卡牌：",
                "选择卡牌",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                cardOptions,
                cardOptions[0]);

        if (selectedCard == -1 || selectedCard == cardOptions.length - 1) {
            System.out.println("[日志] 玩家取消了选择卡牌。");
            return false;
        }

        // 执行给牌操作
        Card selectedCardObj = handCards.get(selectedCard);
        cardController.pendingGiveCardPlayerIndex = fromPlayerIndex;
        return cardController.giveCard(fromPlayerIndex, toPlayerIndex, selectedCardObj);
    }

    /**
     * 处理加固操作
     * 
     * @param playerIndex 玩家索引
     * @return 如果成功发起加固操作返回true，否则返回false
     */
    public boolean handleShoreUp(int playerIndex) {
        Player player = players.get(playerIndex);
        Role role = player.getRole();

        // 检查玩家是否有沙袋卡
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }

        // 如果没有沙袋卡，检查是否有足够的行动点
        if (!hasSandbag) {
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());

            if (currentActions <= 0) {
                System.out.println("[日志] 玩家没有足够的行动点进行加固");
                JOptionPane.showMessageDialog(null, "你没有足够的行动点进行加固！");
                return false;
            }
        }

        // 如果是工程师，可以加固两个板块
        if (role instanceof Model.Role.Engineer) {
            System.out.println("[日志] 工程师可以加固两个板块");
            JOptionPane.showMessageDialog(null, "作为工程师，你可以连续加固最多两个板块！");
            engineerShoreUpCount = 0;
            isEngineerShoreUpMode = true;
            engineerSandbagConsumed = false;
        } else {
            isEngineerShoreUpMode = false;
        }

        // 进入加固模式，等待玩家选择要加固的瓦片
        System.out.println("[日志] 进入加固模式，请选择要加固的瓦片");
        mapController.enterShoreUpMode(playerIndex);
        return true;
    }

    /**
     * 处理特殊技能
     * 
     * @param playerIndex 玩家索引
     */
    private void handleSpecialSkill(int playerIndex) {
        Player player = players.get(playerIndex);
        Role role = player.getRole();

        if (role == null) {
            System.out.println("[日志] 玩家没有角色，无法使用特殊技能");
            return;
        }

        // 检查是否可以使用特殊技能
        if (!role.canUseAbility()) {
            System.out.println("[日志] 当前无法使用特殊技能");
            JOptionPane.showMessageDialog(null, "当前无法使用特殊技能！");
            return;
        }

        // 根据角色类型处理特殊技能
        if (role instanceof Model.Role.Pilot) {
            // 飞行员可以飞到任何位置
            System.out.println("[日志] 飞行员可以使用飞行能力");
            mapController.enterMoveMode(playerIndex);
            role.useSpecialAbility();

            // 飞行员使用特殊技能后立即消耗一个行动点
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            String actionText = playerView.getActionPointsLabel().getText();
            int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
            playerView.setActionPoints(currentActions - 1);

            // 如果行动点用完，结束回合
            if (currentActions - 1 == 0) {
                endTurn(playerIndex);
            }
        } else if (role instanceof Model.Role.Navigator) {
            // 领航员可以移动其他玩家
            System.out.println("[日志] 领航员可以使用移动其他玩家的能力");
            handleNavigatorAbility(playerIndex);
        } else {
            System.out.println("[日志] 该角色没有需要主动使用的特殊技能");
            JOptionPane.showMessageDialog(null, "该角色没有需要主动使用的特殊技能！");
        }
    }

    /**
     * 处理领航员的特殊能力
     * 
     * @param navigatorIndex 领航员玩家索引
     */
    private void handleNavigatorAbility(int navigatorIndex) {
        // 获取所有其他玩家
        List<Player> otherPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (i != navigatorIndex) {
                otherPlayers.add(players.get(i));
            }
        }

        if (otherPlayers.isEmpty()) {
            System.out.println("[日志] 没有其他玩家可以移动");
            JOptionPane.showMessageDialog(null, "没有其他玩家可以移动！");
            return;
        }

        // 创建玩家选择对话框
        String[] playerOptions = new String[otherPlayers.size()];
        for (int i = 0; i < otherPlayers.size(); i++) {
            Player p = otherPlayers.get(i);
            playerOptions[i] = "玩家 " + (players.indexOf(p) + 1) + " (" + p.getRole().getClass().getSimpleName() + ")";
        }

        // 显示玩家选择对话框
        int selectedPlayerIndex = JOptionPane.showOptionDialog(
                null,
                "选择要移动的玩家：",
                "领航员能力",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]);

        if (selectedPlayerIndex == -1) {
            System.out.println("[日志] 玩家取消了选择");
            return;
        }

        // 获取选中的玩家
        Player targetPlayer = otherPlayers.get(selectedPlayerIndex);
        int targetPlayerIndex = players.indexOf(targetPlayer);

        // 设置目标玩家的移动次数为2
        PlayerInfoView targetPlayerView = playerInfoViews.get(targetPlayerIndex);
        targetPlayerView.setActionPoints(2);

        // 使用领航员的能力
        Role navigatorRole = players.get(navigatorIndex).getRole();
        if (navigatorRole != null) {
            navigatorRole.useSpecialAbility();
        }

        // 进入移动模式，但移动的是目标玩家
        System.out.println("[日志] 进入领航员移动模式，移动玩家 " + (targetPlayerIndex + 1));
        mapController.enterNavigatorMoveMode(navigatorIndex, targetPlayerIndex);

        // 显示提示信息
        JOptionPane.showMessageDialog(null,
                "玩家 " + (targetPlayerIndex + 1) + " 现在可以移动两次！\n" +
                        "完成两次移动后，将消耗领航员的一个行动点。",
                "领航员能力",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 移动其他玩家到指定位置（领航员使用）
     * 
     * @param navigatorIndex    领航员玩家索引
     * @param targetPlayerIndex 目标玩家索引
     * @param row               目标行
     * @param col               目标列
     */
    public void moveOtherPlayer(int navigatorIndex, int targetPlayerIndex, int row, int col) {
        if (navigatorIndex < 0 || navigatorIndex >= players.size() ||
                targetPlayerIndex < 0 || targetPlayerIndex >= players.size() ||
                navigatorIndex == targetPlayerIndex) {
            return;
        }

        Player targetPlayer = players.get(targetPlayerIndex);
        Tile currentTile = targetPlayer.getCurrentTile();
        System.out.printf("玩家 %d 当前位置: %s [%d, %d]\n",
                targetPlayerIndex + 1,
                currentTile.getName(),
                currentTile.getRow(),
                currentTile.getCol());

        // 获取目标板块对象
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile != null) {
            // 检查移动是否合法
            if (isValidNavigatorMove(targetPlayer, targetTile)) {
                // 隐藏原位置的玩家图像
                mapController.getMapView().hidePlayerImage(currentTile.getRow(), currentTile.getCol(),
                        targetPlayerIndex);

                // 更新玩家位置
                targetPlayer.setCurrentTile(targetTile);

                // 显示新位置的玩家图像
                String roleName = targetPlayer.getRole().getClass().getSimpleName().toLowerCase();
                String playerImagePath = "src/resources/Player/" + roleName + "2.png";
                mapController.getMapView().showPlayerImage(row, col, playerImagePath, targetPlayerIndex);

                System.out.printf("领航员移动玩家 %d 到: %s [%d, %d]\n",
                        targetPlayerIndex + 1,
                        targetTile.getName(),
                        targetTile.getRow(),
                        targetTile.getCol());

                // 减少目标玩家的移动次数
                PlayerInfoView targetPlayerView = playerInfoViews.get(targetPlayerIndex);
                String actionText = targetPlayerView.getActionPointsLabel().getText();
                int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
                targetPlayerView.setActionPoints(currentActions - 1);

                // 如果目标玩家没有剩余移动次数，消耗领航员的一个行动点并退出移动模式
                if (currentActions - 1 == 0) {
                    // 退出领航员移动模式
                    mapController.exitNavigatorMoveMode();

                    // 显示提示信息
                    JOptionPane.showMessageDialog(null,
                            "目标玩家已完成两次移动！\n" +
                                    "已消耗领航员的一个行动点。",
                            "领航员能力",
                            JOptionPane.INFORMATION_MESSAGE);

                    // 消耗领航员的一个行动点
                    PlayerInfoView navigatorView = playerInfoViews.get(navigatorIndex);
                    String navigatorActionText = navigatorView.getActionPointsLabel().getText();
                    int navigatorActions = Integer.parseInt(navigatorActionText.split(":")[1].trim());
                    navigatorView.setActionPoints(navigatorActions - 1);

                    // 如果领航员没有剩余行动点，结束回合
                    if (navigatorActions - 1 == 0) {
                        endTurn(navigatorIndex);
                    }
                } else {
                    // 询问是否继续第二次移动
                    int option = JOptionPane.showConfirmDialog(null,
                            "是否继续进行第二次移动？\n选择'否'将直接消耗领航员的一个行动点并结束本次能力。",
                            "领航员能力",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.NO_OPTION) {
                        // 玩家选择不继续第二次移动
                        mapController.exitNavigatorMoveMode();
                        PlayerInfoView navigatorView = playerInfoViews.get(navigatorIndex);
                        String navigatorActionText = navigatorView.getActionPointsLabel().getText();
                        int navigatorActions = Integer.parseInt(navigatorActionText.split(":")[1].trim());
                        navigatorView.setActionPoints(navigatorActions - 1);
                        if (navigatorActions - 1 == 0) {
                            endTurn(navigatorIndex);
                        }
                    } else {
                        // 玩家选择继续，提示还可以移动一次
                        JOptionPane.showMessageDialog(null,
                                "玩家 " + (targetPlayerIndex + 1) + " 还可以移动 1 次！",
                                "领航员能力",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                System.out.println("[日志] 非法移动：目标位置不可到达");
                JOptionPane.showMessageDialog(null, "非法移动：目标位置不可到达", "移动错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 检查领航员移动其他玩家是否合法
     * 
     * @param targetPlayer 目标玩家
     * @param targetTile   目标板块
     * @return 如果移动合法则返回true
     */
    private boolean isValidNavigatorMove(Player targetPlayer, Tile targetTile) {
        // 只检查是否沉没，被淹没的瓦片可以移动
        if (targetTile.getState() == TileState.SUNK) {
            System.out.println("目标板块已沉没，无法移动");
            return false;
        }

        Tile currentTile = targetPlayer.getCurrentTile();
        if (currentTile == null) {
            System.out.println("无法获取当前板块");
            return false;
        }

        // 检查目标玩家是否是飞行员
        boolean isPilot = targetPlayer.getRole() instanceof Model.Role.Pilot;
        if (isPilot) {
            return true;
        }

        // 检查目标玩家是否是潜水员
        boolean isDiver = targetPlayer.getRole().getClass().getSimpleName().equals("Diver");
        if (isDiver) {
            // 复用MapController的BFS判定
            return mapController != null && mapController.isDiverReachable(currentTile, targetTile);
        }

        // 检查目标玩家是否是探险家
        boolean isExplorer = targetPlayer.getRole() instanceof Model.Role.Explorer;
        int rowDistance = Math.abs(currentTile.getRow() - targetTile.getRow());
        int colDistance = Math.abs(currentTile.getCol() - targetTile.getCol());
        if (isExplorer) {
            return rowDistance <= 1 && colDistance <= 1;
        }
        // 其他玩家只能移动到相邻格子
        return (rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1);
    }

    /**
     * 加固指定瓦片
     *
     * @param playerIndex 玩家索引
     * @param row         目标行
     * @param col         目标列
     */
    public void shoreUpTile(int playerIndex, int row, int col) {
        System.out.println("\n========== 开始加固板块 ==========");
        System.out.println("玩家索引: " + playerIndex);
        System.out.println("目标位置: [" + row + "," + col + "]");

        Player player = players.get(playerIndex);
        System.out.println("玩家角色: " + player.getRole().getClass().getSimpleName());
        System.out.println("是否是工程师: " + (player.getRole() instanceof Model.Role.Engineer));

        Tile targetTile = mapController.getMapView().getTile(row, col);
        System.out.println("目标板块: " + targetTile.getName());
        System.out.println("目标板块状态: " + targetTile.getState());

        if (canShoreUpTile(playerIndex, targetTile)) {
            System.out.println("可以加固该板块");
            targetTile.setState(TileState.NORMAL);
            System.out.println("板块已加固，新状态: " + targetTile.getState());

            // 检查是否是工程师的第二次加固
            if (player.getRole() instanceof Model.Role.Engineer) {
                System.out.println("玩家是工程师，检查是否可以继续加固");
                System.out.println("当前加固次数: " + engineerShoreUpCount);

                // 检查周围是否还有其他可加固的板块
                boolean hasMoreShoreableTiles = false;
                Tile currentTile = player.getCurrentTile();
                for (Tile adjacentTile : currentTile.getAdjacentTiles()) {
                    if (adjacentTile != targetTile && canShoreUpTile(playerIndex, adjacentTile)) {
                        hasMoreShoreableTiles = true;
                        break;
                    }
                }

                if (engineerShoreUpCount < 1 && hasMoreShoreableTiles) {
                    System.out.println("工程师还可以继续加固");
                    engineerShoreUpCount++;
                    System.out.println("加固次数更新为: " + engineerShoreUpCount);
                    return;
                }
            }

            System.out.println("加固动作结束");
            endShoreUpAction();
        } else {
            System.out.println("无法加固该板块");
        }
        System.out.println("========== 加固板块结束 ==========\n");
    }

    private void endShoreUpAction() {
        System.out.println("\n========== 结束加固动作 ==========");
        System.out.println("当前加固次数: " + engineerShoreUpCount);
        System.out.println("当前玩家角色: " + players.get(currentPlayerIndex).getRole().getClass().getSimpleName());
        System.out.println("是否是工程师: " + (players.get(currentPlayerIndex).getRole() instanceof Model.Role.Engineer));

        // 更新玩家信息视图
        PlayerInfoView playerView = playerInfoViews.get(currentPlayerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        playerView.setActionPoints(currentActions - 1);
        System.out.println("已更新玩家行动点数: " + (currentActions - 1));

        // 重置工程师加固次数
        engineerShoreUpCount = 0;
        System.out.println("加固次数已重置为: " + engineerShoreUpCount);

        mapController.exitShoreUpMode();
        System.out.println("已退出加固模式");

        // 如果行动点用完，结束回合
        if (currentActions - 1 == 0) {
            System.out.println("行动点已用完，结束回合");
            endTurn(currentPlayerIndex);
        }

        System.out.println("========== 加固动作结束完成 ==========\n");
    }

    /**
     * 使用沙袋卡加固任意被淹没的板块
     */
    public void sandbagShoreUpTile(int playerIndex, int row, int col) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }
        Player player = players.get(playerIndex);
        Tile targetTile = mapController.getMapView().getTile(row, col);
        if (targetTile == null || targetTile.getState() != Model.Enumeration.TileState.FLOODED) {
            JOptionPane.showMessageDialog(null, "只能加固被淹没的板块！");
            return;
        }
        // 查找一张沙袋卡
        Model.Cards.Card sandbagCard = null;
        for (Model.Cards.Card card : player.getHandCard().getCards()) {
            if (card instanceof Model.Cards.SandbagCard) {
                sandbagCard = card;
                break;
            }
        }
        if (sandbagCard == null) {
            JOptionPane.showMessageDialog(null, "你没有沙袋卡，无法使用！");
            return;
        }
        // 执行加固
        targetTile.setState(Model.Enumeration.TileState.NORMAL);
        player.getHandCard().removeCard(sandbagCard);
        playerInfoViews.get(playerIndex).removeCard(sandbagCard);
        treasureDeck.discard(sandbagCard);
        JOptionPane.showMessageDialog(null, "成功使用沙袋卡修复板块！");
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
        JOptionPane.showMessageDialog(null, "恭喜！你们集齐宝物并成功逃脱，获得胜利！");
        System.out.println("========== 游戏胜利 ==========");
        // 返回主菜单
        View.MainView mainView = View.MainView.getInstance();
        if (mainView != null) {
            mainView.showStartScreen();
        }
    }

    // 添加设置BoardView的方法
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
     * 检查游戏胜利条件
     * 当所有宝物都被收集且所有玩家都在直升机场时，游戏胜利
     */
    public void checkGameWin() {
        // 检查是否收集齐所有宝物
        if (!treasureDeck.allTreasuresCollected()) {
            return;
        }

        // 检查所有玩家是否都在直升机场
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
        // 检查A是否还有行动点
        PlayerInfoView playerView = playerInfoViews.get(playerIndex);
        String actionText = playerView.getActionPointsLabel().getText();
        int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
        if (currentActions <= 0) {
            // 没有行动点，直接切换到下一个玩家
            startNewTurn();
            return;
        }
        // 恢复A的按钮和行动点显示，不切换回合
        for (int i = 0; i < playerInfoViews.size(); i++) {
            playerInfoViews.get(i).setButtonsEnabled(i == playerIndex);
        }
        updatePlayerView(playerIndex);
    }
}