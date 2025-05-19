package Controller;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Cards.FloodCard;
import Model.Cards.SandbagCard;
import Model.Cards.HelicopterCard;
import Model.Cards.WaterRiseCard;
import Model.Player;
import Model.Tile;
import View.CardView;
import View.PlayerInfoView;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.util.List;

public class CardController implements ActionListener {
    private static final int MAX_CARDS = 7;
    private final GameController gameController;
    private boolean isDiscardMode = false;
    private int cardsToDiscard = 0;
    private int cardsDiscarded = 0;
    private PlayerInfoView currentDiscardingPlayer = null;

    public CardController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof CardView) {
            CardView cardView = (CardView) source;
            System.out.println("卡牌被点击 - 是否在弃牌模式: " + isDiscardMode); // 调试信息
            if (isDiscardMode) {
                System.out.println("当前正在弃牌模式，处理卡牌点击"); // 调试信息
                handleCardClick(cardView.getCard());
            } else {
                System.out.println("不在弃牌模式，忽略卡牌点击"); // 调试信息
            }
        }
    }

    private void handleCardClick(Card card) {
        System.out.println("处理卡牌点击 - 卡牌类型: " + (card != null ? card.getClass().getSimpleName() : "null")); // 调试信息
        if (isDiscardMode && currentDiscardingPlayer != null) {
            if (card != null) {
                handleDiscardCard(card);
            }
        } else {
            if (card instanceof SandbagCard) {
                handleSandbagCard((SandbagCard) card);
            } else if (card instanceof HelicopterCard) {
                handleHelicopterCard((HelicopterCard) card);
            }
        }
    }

    private void handleSandbagCard(SandbagCard card) {
        // 处理沙袋卡点击逻辑
        // gameController.handleSandbagUse(card);
    }

    private void handleHelicopterCard(HelicopterCard card) {
        // 处理直升机卡点击逻辑
        // gameController.handleHelicopterUse(card);
    }

    public void addCard(PlayerInfoView playerInfoView, Card card) {
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        if (cardsPanel.getComponentCount() < MAX_CARDS) {
            int playerCount = gameController.getPlayerInfoViews().size();
            CardView cardView = new CardView(card, playerCount);
            cardView.addActionListener(this); // 添加点击事件监听器
            cardsPanel.add(cardView);
            cardsPanel.revalidate();
            cardsPanel.repaint();
        }
    }

    public void removeCard(PlayerInfoView playerInfoView, Card card) {
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        Component[] components = cardsPanel.getComponents();
        for (Component component : components) {
            if (component instanceof CardView) {
                CardView cardView = (CardView) component;
                if (cardView.getCard().equals(card)) {
                    cardsPanel.remove(cardView);
                    cardsPanel.revalidate();
                    cardsPanel.repaint();
                    break;
                }
            }
        }
    }

    public void clearCards(PlayerInfoView playerInfoView) {
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        cardsPanel.removeAll();
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public void enableDiscardMode(PlayerInfoView playerInfoView, int numCardsToDiscard) {
        // 如果不需要弃牌，直接返回
        if (numCardsToDiscard <= 0) {
            System.out.println("不需要弃牌，跳过弃牌模式"); // 调试信息
            return;
        }

        System.out.println("进入弃牌模式 - 需要弃掉 " + numCardsToDiscard + " 张卡牌"); // 调试信息
        isDiscardMode = true;
        cardsToDiscard = numCardsToDiscard;
        cardsDiscarded = 0;
        currentDiscardingPlayer = playerInfoView;

        // 启用所有卡牌的点击事件
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        System.out.println("当前卡牌面板中的组件数量: " + cardsPanel.getComponentCount()); // 调试信息

        for (Component component : cardsPanel.getComponents()) {
            if (component instanceof CardView) {
                CardView cardView = (CardView) component;
                cardView.setEnabled(true);
                cardView.setToolTipText("点击弃掉此卡牌");
                System.out.println("启用卡牌点击事件: " + cardView.getCard().getClass().getSimpleName()); // 调试信息
            }
        }

        // 显示弃牌提示
        JOptionPane.showMessageDialog(null,
                "您的手牌超过了5张，请选择" + cardsToDiscard + "张卡牌弃掉",
                "弃牌阶段",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDiscardCard(Card card) {
        System.out.println("处理弃牌 - 当前已弃掉 " + cardsDiscarded + "/" + cardsToDiscard + " 张卡牌"); // 调试信息
        if (cardsDiscarded < cardsToDiscard) {
            // 获取正在弃牌的玩家
            int discardingPlayerIndex = gameController.getPlayerInfoViews().indexOf(currentDiscardingPlayer);
            Player discardingPlayer = gameController.getPlayers().get(discardingPlayerIndex);
            // 从该玩家手中移除卡牌
            discardingPlayer.getHandCard().removeCard(card);
            removeCard(currentDiscardingPlayer, card);
            cardsDiscarded++;
            System.out.println("成功弃掉一张卡牌，还剩 " + (cardsToDiscard - cardsDiscarded) + " 张需要弃掉"); // 调试信息

            if (cardsDiscarded == cardsToDiscard) {
                System.out.println("弃牌完成，退出弃牌模式"); // 调试信息
                isDiscardMode = false;
                gameController.updatePlayerView(discardingPlayerIndex);
                // 恢复当前弃牌玩家的按钮
                currentDiscardingPlayer.setButtonsEnabled(true);
                currentDiscardingPlayer = null;
                // 不再调用gameController.startNewTurn();
            } else {
                JOptionPane.showMessageDialog(null,
                        "还需要弃掉" + (cardsToDiscard - cardsDiscarded) + "张卡牌",
                        "弃牌阶段",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 实际执行给牌操作
     */
    public boolean giveCard(int fromPlayerIndex, int toPlayerIndex, Card card) {
        GameController gc = this.gameController;
        Player fromPlayer = gc.getPlayers().get(fromPlayerIndex);
        Player toPlayer = gc.getPlayers().get(toPlayerIndex);

        // 检查条件
        if (!fromPlayer.getCurrentTile().equals(toPlayer.getCurrentTile())) {
            System.out.println("[日志] 两个玩家不在同一位置，不能给牌。");
            return false;
        }
        if (!fromPlayer.getHandCard().getCards().contains(card)) {
            System.out.println("[日志] 给牌玩家没有这张卡。");
            return false;
        }

        // 执行转移
        fromPlayer.removeCard(card);
        toPlayer.getHandCard().addCardWithoutCheck(card);
        // 更新视图
        gc.updatePlayerView(fromPlayerIndex);
        gc.updatePlayerView(toPlayerIndex);

        // 检查对方是否超限，超限则进入弃牌模式
        int cardCount = toPlayer.getHandCard().getCards().size();
        if (cardCount > 5) {
            int cardsToDiscard = cardCount - 5;
            System.out.println("[日志] 收牌玩家手牌超限，需弃掉 " + cardsToDiscard + " 张卡牌");
            PlayerInfoView playerView = gc.getPlayerInfoView(toPlayerIndex);
            playerView.setButtonsEnabled(false);
            this.enableDiscardMode(playerView, cardsToDiscard);
        }
        return true;
    }

    /**
     * 使用沙袋卡加固指定瓦片
     * 
     * @param playerIndex 玩家索引
     * @param targetTile  目标瓦片
     * @return 是否加固成功
     */
    public boolean useSandbagCard(int playerIndex, Tile targetTile) {
        Player player = gameController.getPlayers().get(playerIndex);
        Card sandbagCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                sandbagCard = card;
                break;
            }
        }
        if (sandbagCard == null) {
            System.out.println("[日志] 玩家没有沙袋卡，无法加固");
            return false;
        }
        if (((SandbagCard) sandbagCard).useCard(targetTile)) {
            player.getHandCard().removeCard(sandbagCard);
            gameController.getPlayerInfoView(playerIndex).removeCard(sandbagCard);
            gameController.getTreasureDeck().discard(sandbagCard);
            System.out.println("[日志] 成功用沙袋卡加固瓦片：" + targetTile.getName() + " [坐标: " + targetTile.getRow() + ","
                    + targetTile.getCol() + "]");
            return true;
        } else {
            System.out.println("[日志] 沙袋卡加固失败");
            return false;
        }
    }

    /**
     * 使用直升机救援卡尝试获胜
     * 
     * @param playerIndex 玩家索引
     * @return 是否胜利
     */
    public boolean useHelicopterCardForWin(int playerIndex) {
        // 1. 检查是否集齐四个宝物
        if (!gameController.getTreasureDeck().allTreasuresCollected()) {
            System.out.println("[日志] 宝物未集齐，不能使用直升机卡获胜。");
            JOptionPane.showMessageDialog(null, "还没有集齐所有宝物，不能逃脱！");
            return false;
        }
        // 2. 检查所有玩家是否都在愚人码头
        List<Player> players = gameController.getPlayers();
        boolean allAtFoolsLanding = players.stream().allMatch(
                p -> p.getCurrentTile() != null && p.getCurrentTile().getName().name().equals("FOOLS_LANDING"));
        if (!allAtFoolsLanding) {
            System.out.println("[日志] 并非所有玩家都在愚人码头，不能使用直升机卡获胜。");
            JOptionPane.showMessageDialog(null, "所有玩家必须都在愚人码头才能逃脱！");
            return false;
        }
        // 3. 检查玩家是否有直升机卡
        Player player = players.get(playerIndex);
        Card heliCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                heliCard = card;
                break;
            }
        }
        if (heliCard == null) {
            System.out.println("[日志] 没有直升机卡，不能获胜。");
            JOptionPane.showMessageDialog(null, "你没有直升机救援卡！");
            return false;
        }
        // 4. 弃掉直升机卡
        player.getHandCard().removeCard(heliCard);
        gameController.getPlayerInfoView(playerIndex).removeCard(heliCard);
        gameController.getTreasureDeck().discard(heliCard);

        // 5. 游戏胜利
        System.out.println("[日志] 使用直升机救援卡，所有玩家逃脱，游戏胜利！");
        JOptionPane.showMessageDialog(null, "所有玩家乘坐直升机逃脱，游戏胜利！");
        gameController.endGameWithWin();
        return true;
    }
}