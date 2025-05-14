package Controller;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Cards.FloodCard;
import Model.Cards.SandbagCard;
import Model.Cards.HelicopterCard;
import Model.Cards.WaterRiseCard;
import Model.Player;
import View.CardView;
import View.PlayerInfoView;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
        //gameController.handleSandbagUse(card);
    }

    private void handleHelicopterCard(HelicopterCard card) {
        // 处理直升机卡点击逻辑
        //gameController.handleHelicopterUse(card);
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
            // 获取当前玩家
            Player currentPlayer = gameController.getCurrentPlayer();
            // 从玩家手中移除卡牌
            currentPlayer.getHandCard().removeCard(card);
            removeCard(currentDiscardingPlayer, card);
            cardsDiscarded++;
            System.out.println("成功弃掉一张卡牌，还剩 " + (cardsToDiscard - cardsDiscarded) + " 张需要弃掉"); // 调试信息

            if (cardsDiscarded == cardsToDiscard) {
                System.out.println("弃牌完成，退出弃牌模式"); // 调试信息
                // 弃牌完成，退出弃牌模式
                isDiscardMode = false;
                // 更新玩家手牌显示
                gameController.updatePlayerView(gameController.getPlayerInfoViews().indexOf(currentDiscardingPlayer));
                currentDiscardingPlayer = null;
                // 开始新回合
                gameController.startNewTurn();
            } else {
                // 显示还需要弃掉多少张牌
                JOptionPane.showMessageDialog(null, 
                    "还需要弃掉" + (cardsToDiscard - cardsDiscarded) + "张卡牌",
                    "弃牌阶段",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}