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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

public class GameController {
    private final List<Player> players;
    private final List<PlayerInfoView> playerInfoViews;
    private final CardController cardController;
    private final TreasureDeck treasureDeck;
    private int currentPlayerIndex = 0;
    private static final int MAX_ACTIONS_PER_TURN = 3;
    private final Tile helicopterTile;  // 直升机场位置
    private WaterLevelView waterLevelView;  // 添加水位视图
    private int currentWaterLevel = 1;  // 初始水位设置为1
    private TilePosition tilePosition;  // 添加TilePosition对象

    public GameController(int playerCount, Tile helicopterTile, WaterLevelView waterLevelView) {
        System.out.println("\n========== 开始初始化游戏控制器 ==========");
        this.players = new ArrayList<>();
        this.playerInfoViews = new ArrayList<>();
        this.cardController = new CardController(this);
        this.treasureDeck = new TreasureDeck(helicopterTile);
        this.helicopterTile = helicopterTile;
        this.waterLevelView = waterLevelView;
        this.tilePosition = null;  // 初始化为null，将在设置MapView时更新

        System.out.println("正在初始化 " + playerCount + " 个玩家...");
        // 初始化玩家
        for (int i = 0; i < playerCount; i++) {
            Player player = new Player();
            players.add(player);
            PlayerInfoView playerInfoView = new PlayerInfoView(this);
            playerInfoView.setPlayerName("Player " + (i + 1));
            playerInfoViews.add(playerInfoView);
        }

        // 初始化水位
        waterLevelView.updateWaterLevel(currentWaterLevel);

        System.out.println("正在分配角色...");
        // 分配角色
        assignRoles();
        
        System.out.println("正在发放初始卡牌...");
        // 初始发牌
        dealInitialCards();
        
        System.out.println("正在初始化第一个玩家的回合...");
        // 初始化第一个玩家的回合
        initializeFirstTurn();
        
        System.out.println("========== 游戏控制器初始化完成 ==========\n");
    }

    private void dealInitialCards() {
        // 为每个玩家发放两张初始卡牌
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (int j = 0; j < 2; j++) {
                Card card = treasureDeck.drawInitialCard();
                if (card != null) {
                    try {
                        player.addCard(card);
                        playerInfoViews.get(i).addCard(card);
                    } catch (HandCard.HandCardFullException e) {
                        System.err.println("初始发牌时手牌已满: " + e.getMessage());
                        // 如果手牌已满，将卡牌放回牌堆
                        treasureDeck.discard(card);
                    }
                }
            }
        }
        // 初始发牌完成后标记结束
        treasureDeck.finishInitialDraw();
    }

    private void assignRoles() {
        List<Role> randomRoles = RoleManager.getRandomRoles(players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Role role = randomRoles.get(i);
            player.setRole(role);
            
            // 更新玩家信息视图中的角色显示
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
        
        // 更新角色信息
        if (player.getRole() != null) {
            view.setRole(player.getRole().getClass().getSimpleName());
        }
        
        // 更新手牌显示
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
        
        // 更新所有玩家的按钮状态
        for (int i = 0; i < playerInfoViews.size(); i++) {
            playerInfoViews.get(i).setButtonsEnabled(i == currentPlayerIndex);
        }
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

        // 判断动作类型是否消耗行动点
        boolean consumesAction = actionName.equals("Move") ||
                               actionName.equals("Shore up") ||
                               actionName.equals("Give Cards") ||
                               actionName.equals("Special Skill");

        if (!consumesAction || currentActions > 0) {
            // 如果是消耗行动点的动作，减少行动点
            if (consumesAction) {
                playerView.setActionPoints(currentActions - 1);
                currentActions--;
            }

            // 如果是Skip或行动点用完，结束回合
            if (actionName.equals("Skip") || currentActions == 0) {
                endTurn(playerIndex);
            }
        }
    }

    private void endTurn(int playerIndex) {
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
        
        startNewTurn();
    }

    // 添加设置MapView的方法
    public void setMapView(MapView mapView) {
        System.out.println("\n========== 设置MapView ==========");
        System.out.println("MapView对象: " + (mapView != null ? "非空" : "为空"));
        this.tilePosition = mapView.getTilePosition();
        System.out.println("tilePosition对象: " + (this.tilePosition != null ? "非空" : "为空"));
        if (this.tilePosition != null) {
            Map<String, int[]> positions = this.tilePosition.getAllTilePositions();
            System.out.println("可用板块数量: " + (positions != null ? positions.size() : 0));
            if (positions != null) {
                System.out.println("可用板块列表:");
                positions.forEach((name, pos) -> 
                    System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));
            }
            
            // 在设置完tilePosition后初始化玩家位置
            System.out.println("正在初始化玩家位置...");
            initializePlayerPositions();
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
    private void initializePlayerPositions() {
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
        allPositions.forEach((name, pos) -> 
            System.out.printf("  - %s: [%d, %d]\n", name, pos[0], pos[1]));

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
            
            // 创建新的Tile对象并设置玩家位置
            Tile tile = new Tile(Model.Enumeration.TileName.valueOf(tileName), position[0], position[1]);
            players.get(i).setCurrentTile(tile);
            
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
}