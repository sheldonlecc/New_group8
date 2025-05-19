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
    private final Tile helicopterTile; // 直升机场位置
    private WaterLevelView waterLevelView; // 添加水位视图
    private int currentWaterLevel = 1; // 初始水位设置为1
    private TilePosition tilePosition; // 添加TilePosition对象
    private MapController mapController; // 添加MapController成员变量
    private FloodDeck floodDeck;

    public GameController(int playerCount, Tile helicopterTile, WaterLevelView waterLevelView) {
        System.out.println("\n========== 开始初始化游戏控制器 ==========");
        this.players = new ArrayList<>();
        this.playerInfoViews = new ArrayList<>();
        this.cardController = new CardController(this);
        this.treasureDeck = new TreasureDeck(helicopterTile);
        this.helicopterTile = helicopterTile;
        this.waterLevelView = waterLevelView;
        this.tilePosition = null;
        this.mapController = null;
        this.floodDeck = new FloodDeck(new ArrayList<>());

        // 初始化洪水牌堆
        List<Tile> allTiles = new ArrayList<>();
        if (tilePosition != null) {
            Map<String, int[]> positions = tilePosition.getAllTilePositions();
            for (Map.Entry<String, int[]> entry : positions.entrySet()) {
                Tile tile = new Tile(TileName.valueOf(entry.getKey()), entry.getValue()[0], entry.getValue()[1]);
                allTiles.add(tile);
            }
        }
        this.floodDeck = new FloodDeck(allTiles);

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

        // 检查水位是否达到10
        if (currentWaterLevel >= 10) {
            System.out.println("\n========== 游戏结束 ==========");
            System.out.println("水位已达到10，游戏失败！");
            JOptionPane.showMessageDialog(null, "水位已达到10，游戏失败！");
            return;
        }

        System.out.println("\n========== 开始抽取洪水卡 ==========");
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

        System.out.println("当前水位: " + currentWaterLevel + "，需要抽取 " + floodCardCount + " 张洪水卡");

        for (int i = 0; i < floodCardCount; i++) {
            FloodCard card = floodDeck.draw();
            if (card != null) {
                card.use();
                floodDeck.discard(card);
                Tile targetTile = card.getTargetTile();
                String stateMsg = "";
                switch (targetTile.getState()) {
                    case FLOODED:
                        stateMsg = "被淹没";
                        break;
                    case SUNK:
                        stateMsg = "沉没";
                        break;
                    default:
                        stateMsg = "正常";
                        break;
                }
                System.out.println("[日志] 洪水卡抽取：" + targetTile.getName() +
                        " [坐标: " + targetTile.getRow() + "," + targetTile.getCol() + "]" +
                        "，当前状态：" + stateMsg);
            } else {
                System.out.println("[警告] 洪水牌堆已空！");
            }
        }
        System.out.println("========== 洪水卡抽取完成 ==========\n");
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
                actionName.equals("Special Skill");

        if (!consumesAction || currentActions > 0) {
            switch (actionName) {
                case "Move":
                    handleMove(playerIndex);
                    break;
                case "Shore up":
                    handleShoreUp(playerIndex);
                    break;
                case "Give Cards":
                    playerView.setActionPoints(currentActions - 1);
                    currentActions--;
                    requestGiveCard(playerIndex);
                    break;
                case "Special Skill":
                    playerView.setActionPoints(currentActions - 1);
                    currentActions--;
                    break;
                case "Skip":
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
            player.setCurrentTile(targetTile);

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
        this.mapController = new MapController(this, mapView);

        // 只用MapView的Tile对象
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
        Tile currentTile = player.getCurrentTile();
        boolean isAdjacent = currentTile.isAdjacentTo(targetTile) || currentTile.equals(targetTile);
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
     */
    public void requestGiveCard(int fromPlayerIndex) {
        Player fromPlayer = players.get(fromPlayerIndex);
        // 找到同一位置的其他玩家
        List<Integer> candidateIndexes = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (i != fromPlayerIndex && players.get(i).getCurrentTile().equals(fromPlayer.getCurrentTile())) {
                candidateIndexes.add(i);
            }
        }
        if (candidateIndexes.isEmpty()) {
            System.out.println("[日志] 没有其他玩家在同一位置，无法给牌。");
            return;
        }
        // 日志模拟选择目标玩家
        int toPlayerIndex = candidateIndexes.get(0); // 默认选第一个
        System.out.println("[日志] 选择目标玩家: 玩家" + (toPlayerIndex + 1));

        // 日志模拟选择卡牌
        List<Card> handCards = fromPlayer.getHandCard().getCards();
        if (handCards.isEmpty()) {
            System.out.println("[日志] 没有可给出的卡牌。");
            return;
        }
        Card cardToGive = handCards.get(0); // 默认选第一张
        System.out.println("[日志] 选择要给出的卡牌: " + cardToGive.getName());

        // 调用CardController执行给牌
        boolean success = cardController.giveCard(fromPlayerIndex, toPlayerIndex, cardToGive);
        if (success) {
            System.out.println("[日志] 成功将卡牌[" + cardToGive.getName() + "]从玩家" + (fromPlayerIndex + 1) + "给到玩家"
                    + (toPlayerIndex + 1));
        } else {
            System.out.println("[日志] 给牌失败！");
        }
    }

    /**
     * 处理加固操作
     * 
     * @param playerIndex 玩家索引
     */
    private void handleShoreUp(int playerIndex) {
        Player player = players.get(playerIndex);
        // 检查玩家是否有沙袋卡
        boolean hasSandbag = false;
        Card sandbagCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                sandbagCard = card;
                break;
            }
        }

        if (!hasSandbag) {
            System.out.println("[日志] 玩家没有沙袋卡，无法使用加固功能");
            JOptionPane.showMessageDialog(null, "你没有沙袋卡，无法使用加固功能！");
            return;
        }

        // 进入加固模式，等待玩家选择要加固的瓦片
        System.out.println("[日志] 进入加固模式，请选择要加固的瓦片");
        mapController.enterShoreUpMode(playerIndex);

        Tile currentTile = player.getCurrentTile();
        System.out.printf("玩家位置: [%d, %d], 当前瓦片状态: %s\n", currentTile.getRow(), currentTile.getCol(),
                currentTile.getState());
        List<Tile> shoreableTiles = currentTile.getAdjacentTiles();
        shoreableTiles.add(currentTile);
        for (Tile t : shoreableTiles) {
            System.out.printf("可加固检测: [%d, %d], 状态: %s, isShoreable: %b\n", t.getRow(), t.getCol(), t.getState(),
                    t.isShoreable());
        }
    }

    /**
     * 加固指定瓦片
     * 
     * @param playerIndex 玩家索引
     * @param row         目标行
     * @param col         目标列
     */
    public void shoreUpTile(int playerIndex, int row, int col) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }

        Player player = players.get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        Tile targetTile = mapController.getMapView().getTile(row, col);

        // 检查是否可以加固
        if (!canShoreUpTile(playerIndex, targetTile)) {
            System.out.println("[日志] 无法加固该瓦片");
            return;
        }

        // 找到并移除沙袋卡
        Card sandbagCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                sandbagCard = card;
                break;
            }
        }

        if (sandbagCard != null) {
            // 使用沙袋卡加固瓦片
            if (((SandbagCard) sandbagCard).useCard(targetTile)) {
                // 从玩家手中移除沙袋卡
                player.getHandCard().removeCard(sandbagCard);
                playerInfoViews.get(playerIndex).removeCard(sandbagCard);
                treasureDeck.discard(sandbagCard);

                System.out.println("[日志] 成功加固瓦片：" + targetTile.getName() +
                        " [坐标: " + targetTile.getRow() + "," + targetTile.getCol() + "]");
            } else {
                System.out.println("[日志] 加固瓦片失败");
            }
        }
    }

    public List<Player> getPlayers() {
        return players;
    }
}