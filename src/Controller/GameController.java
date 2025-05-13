package Controller;

import Model.Player;
import Model.Role.Role;
import View.PlayerInfoView;
import Model.Deck.TreasureDeck;
import Model.Cards.Card;
import Model.Cards.WaterRiseCard;
import Model.Tile;
import Model.Enumeration.TileName;
import Model.Enumeration.TileType;
import Model.Cards.HandCard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class GameController {
    private final List<Player> players;
    private final List<PlayerInfoView> playerInfoViews;
    private final CardController cardController;
    private final TreasureDeck treasureDeck;
    private int currentPlayerIndex = 0;
    private static final int MAX_ACTIONS_PER_TURN = 3;
    private final Tile helicopterTile;  // 直升机场位置

    public GameController(int playerCount, Tile helicopterTile) {
        this.helicopterTile = helicopterTile;
        players = new ArrayList<>();
        playerInfoViews = new ArrayList<>();
        cardController = new CardController(this);
        treasureDeck = new TreasureDeck(helicopterTile);

        // 初始化玩家和对应的信息视图
        for (int i = 0; i < playerCount; i++) {
            Player player = new Player();
            players.add(player);
            
            PlayerInfoView playerInfoView = new PlayerInfoView(this);
            playerInfoView.setPlayerName("Player " + (i + 1));
            playerInfoViews.add(playerInfoView);
        }

        // 分配角色
        assignRoles();
        
        // 初始发牌
        dealInitialCards();
        
        // 初始化第一个玩家的回合
        initializeFirstTurn();
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
                try {
                    currentPlayer.addCard(card);
                    PlayerInfoView playerInfoView = playerInfoViews.get(playerIndex);
                    cardController.addCard(playerInfoView, card);
                } catch (HandCard.HandCardFullException e) {
                    System.err.println("回合结束时手牌已满: " + e.getMessage());
                    // 如果手牌已满，将卡牌放回牌堆
                    treasureDeck.discard(card);
                }
            }
        }

        // 检查是否有WaterRise卡牌并立即使用
        List<Card> cards = currentPlayer.getHandCard().getCards();
        for (Card card : new ArrayList<>(cards)) {
            if (card instanceof WaterRiseCard) {
                ((WaterRiseCard) card).use();
                currentPlayer.getHandCard().removeCard(card);
                playerInfoViews.get(playerIndex).removeCard(card);
                treasureDeck.discard(card);
                JOptionPane.showMessageDialog(null, "水位上升了一格！");
            }
        }

        // 检查手牌数量，如果超过5张，进入弃牌阶段
        int cardCount = currentPlayer.getHandCard().getCards().size();
        if (cardCount > 5) {
            int cardsToDiscard = cardCount - 5;
            PlayerInfoView playerView = playerInfoViews.get(playerIndex);
            // 禁用所有动作按钮，只允许选择弃牌
            playerView.setButtonsEnabled(false);
            // 启用卡牌选择模式
            cardController.enableDiscardMode(playerView, cardsToDiscard);
            return; // 不开始新回合，等待玩家选择弃牌
        }
        
        startNewTurn();
    }


}