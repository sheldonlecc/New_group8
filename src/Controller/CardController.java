package Controller;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Cards.FloodCard;
import Model.Cards.SandBagCard;
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
            handleCardClick(cardView.getCard());
        }
    }

    private void handleCardClick(Card card) {
        if (isDiscardMode && currentDiscardingPlayer != null) {
            if (card != null) {
                handleDiscardCard(card);
            }
        } else {
            if (card instanceof SandBagCard) {
                handleSandBagCard((SandBagCard) card);
            } else if (card instanceof HelicopterCard) {
                handleHelicopterCard((HelicopterCard) card);
            } 
        }
    }

    private void handleSandBagCard(SandBagCard card) {
        // 处理沙袋卡点击逻辑
        //gameController.handleSandBagUse(card);
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
        isDiscardMode = true;
        cardsToDiscard = numCardsToDiscard;
        cardsDiscarded = 0;
        currentDiscardingPlayer = playerInfoView;
        // 显示弃牌提示
        JOptionPane.showMessageDialog(null, "请选择" + cardsToDiscard + "张卡牌弃掉");
    }

    private void handleDiscardCard(Card card) {
        if (cardsDiscarded < cardsToDiscard) {
            // 获取当前玩家
            Player currentPlayer = gameController.getCurrentPlayer();
            // 从玩家手中移除卡牌
            currentPlayer.getHandCard().removeCard(card);
            removeCard(currentDiscardingPlayer, card);
            cardsDiscarded++;

            if (cardsDiscarded == cardsToDiscard) {
                // 弃牌完成，退出弃牌模式
                isDiscardMode = false;
                // 更新玩家手牌显示
                gameController.updatePlayerView(gameController.getPlayerInfoViews().indexOf(currentDiscardingPlayer));
                currentDiscardingPlayer = null;
                // 开始新回合
                gameController.startNewTurn();
            } else {
                // 显示还需要弃掉多少张牌
                JOptionPane.showMessageDialog(null, "还需要弃掉" + (cardsToDiscard - cardsDiscarded) + "张卡牌");
            }
        }
    }
}