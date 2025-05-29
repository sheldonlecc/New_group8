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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.util.List;
import java.util.ArrayList;

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
        gameController.handleShoreUp(gameController.getCurrentPlayerIndex());
    }

    private void handleHelicopterCard(HelicopterCard card) {
        // 获取当前玩家
        int currentPlayerIndex = gameController.getCurrentPlayerIndex();
        Player currentPlayer = gameController.getPlayers().get(currentPlayerIndex);

        // 检查是否满足胜利条件
        if (card.canUseForVictory(gameController.getPlayers())) {
            if (useHelicopterCardForWin(currentPlayerIndex)) {
                return;
            }
        }

        // 如果不满足胜利条件，则使用移动功能
        // 获取所有玩家
        List<Player> players = gameController.getPlayers();
        
        // 创建玩家选择对话框
        String[] playerOptions = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            playerOptions[i] = "玩家 " + (i + 1) + " (" + p.getRole().getClass().getSimpleName() + ")";
        }

        // 显示玩家选择对话框（多选）
        List<Integer> selectedPlayers = new ArrayList<>();
        while (true) {
            // 更新选项显示，添加已选择次数的信息
            String[] currentOptions = new String[playerOptions.length];
            for (int i = 0; i < playerOptions.length; i++) {
                int selectedCount = 0;
                for (int selected : selectedPlayers) {
                    if (selected == i) selectedCount++;
                }
                currentOptions[i] = playerOptions[i] + (selectedCount > 0 ? String.format(" (已选择%d次)", selectedCount) : "");
            }

            // 创建玩家选择面板
            JPanel playerPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            JButton[] playerButtons = new JButton[currentOptions.length];
            for (int i = 0; i < currentOptions.length; i++) {
                final int index = i;
                playerButtons[i] = new JButton(currentOptions[i]);
                playerButtons[i].addActionListener(e -> {
                    selectedPlayers.add(index);
                    System.out.println(String.format("[日志] 选择了玩家: %s", playerOptions[index]));
                    // 更新按钮文本
                    int selectedCount = 0;
                    for (int selected : selectedPlayers) {
                        if (selected == index) selectedCount++;
                    }
                    playerButtons[index].setText(playerOptions[index] + String.format(" (已选择%d次)", selectedCount));
                });
                playerPanel.add(playerButtons[i]);
            }

            // 创建操作按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton confirmButton = new JButton("确定");
            JButton cancelButton = new JButton("取消");

            // 创建一个标志来跟踪对话框是否被确认
            final boolean[] confirmed = {false};

            confirmButton.addActionListener(e -> {
                if (selectedPlayers.isEmpty()) {
                    System.out.println("[日志] 玩家未选择任何玩家。");
                    JOptionPane.showMessageDialog(null, "请至少选择一个玩家！");
                } else {
                    confirmed[0] = true;
                    Window window = SwingUtilities.getWindowAncestor(buttonPanel);
                    if (window != null) {
                        window.dispose();
                    }
                }
            });

            cancelButton.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(buttonPanel);
                if (window != null) {
                    window.dispose();
                }
            });

            buttonPanel.add(confirmButton);
            buttonPanel.add(cancelButton);

            // 创建主面板
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(playerPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            // 显示对话框
            JOptionPane.showOptionDialog(
                    null,
                    mainPanel,
                    "选择要移动的玩家",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{},
                    null);

            if (!confirmed[0]) {
                return; // 用户取消选择
            }

            // 获取选中的玩家
            List<Player> selectedPlayersList = new ArrayList<>();
            for (int index : selectedPlayers) {
                selectedPlayersList.add(players.get(index));
            }

            // 检查所有选中的玩家是否在同一个板块
            if (!card.canUseForMovement(selectedPlayersList)) {
                JOptionPane.showMessageDialog(null, "所有选中的玩家必须在同一个板块上！");
                return;
            }

            // 进入移动模式，等待玩家选择目标位置
            gameController.getMapController().enterHelicopterMoveMode(currentPlayerIndex, selectedPlayersList, card);
            break;
        }
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

            // 检查是否是特殊卡
            if (card instanceof SandbagCard || card instanceof HelicopterCard) {
                // 创建选择窗口
                String cardType = card instanceof SandbagCard ? "沙袋卡" : "直升机卡";
                int choice = JOptionPane.showOptionDialog(
                    null,
                    "您选择弃掉一张" + cardType + "，是否要使用它的功能？",
                    "特殊卡选择",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"使用功能", "直接弃掉"},
                    "使用功能"
                );

                if (choice == 0) { // 选择使用功能
                    // 记录这张卡被弃掉
                    cardsDiscarded++;
                    if (card instanceof SandbagCard) {
                        // 如果是沙袋卡，进入沙袋模式
                        gameController.getMapController().enterSandbagMode(discardingPlayerIndex);
                    } else {
                        // 如果是直升机卡，进入直升机模式
                        gameController.getMapController().enterHelicopterMode(discardingPlayerIndex);
                    }
                    return; // 等待玩家使用特殊卡后再继续弃牌
                } else { // 选择直接弃掉
                    // 直接执行弃牌操作
                    discardingPlayer.getHandCard().removeCard(card);
                    removeCard(currentDiscardingPlayer, card);
                    cardsDiscarded++;
                    System.out.println("成功弃掉一张卡牌，还剩 " + (cardsToDiscard - cardsDiscarded) + " 张需要弃掉"); // 调试信息
                }
            } else {
                // 如果不是特殊卡，继续正常的弃牌流程
                discardingPlayer.getHandCard().removeCard(card);
                removeCard(currentDiscardingPlayer, card);
                cardsDiscarded++;
                System.out.println("成功弃掉一张卡牌，还剩 " + (cardsToDiscard - cardsDiscarded) + " 张需要弃掉"); // 调试信息
            }

            // 检查是否完成弃牌
            if (cardsDiscarded == cardsToDiscard) {
                System.out.println("弃牌完成，退出弃牌模式"); // 调试信息
                isDiscardMode = false;
                gameController.updatePlayerView(discardingPlayerIndex);
                // 恢复当前弃牌玩家的按钮
                currentDiscardingPlayer.setButtonsEnabled(true);
                currentDiscardingPlayer = null;
                // 弃牌完成后，开始新回合
                gameController.startNewTurn();
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
        boolean isMessenger = fromPlayer.getRole() instanceof Model.Role.Messenger;
        boolean sameLocation = fromPlayer.getCurrentTile().equals(toPlayer.getCurrentTile());

        if (!sameLocation && !isMessenger) {
            System.out.println("[日志] 两个玩家不在同一位置，且不是信使，不能给牌。");
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

    /**
     * 检查是否在弃牌模式中
     * @return 如果在弃牌模式中则返回true
     */
    public boolean isInDiscardMode() {
        return isDiscardMode;
    }

    /**
     * 继续弃牌流程
     * 在特殊卡使用完成后调用此方法继续弃牌
     */
    public void continueDiscardMode() {
        if (isDiscardMode && currentDiscardingPlayer != null) {
            // 获取正在弃牌的玩家
            int discardingPlayerIndex = gameController.getPlayerInfoViews().indexOf(currentDiscardingPlayer);
            Player discardingPlayer = gameController.getPlayers().get(discardingPlayerIndex);

            // 检查是否还需要继续弃牌
            if (cardsDiscarded < cardsToDiscard) {
                JOptionPane.showMessageDialog(null,
                        "还需要弃掉" + (cardsToDiscard - cardsDiscarded) + "张卡牌",
                        "弃牌阶段",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 如果不需要继续弃牌，结束弃牌模式
                isDiscardMode = false;
                gameController.updatePlayerView(discardingPlayerIndex);
                currentDiscardingPlayer.setButtonsEnabled(true);
                currentDiscardingPlayer = null;
                gameController.startNewTurn();
            }
        }
    }
}