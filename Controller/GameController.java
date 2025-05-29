package Controller;

import Model.*;
import View.*;
import javax.swing.*;
import java.util.*;

public class GameController {
    private GameView gameView;
    private Player currentPlayer;
    // ... other fields ...

    public GameController(GameView gameView) {
        this.gameView = gameView;
        // ... other initialization ...
    }

    private void requestGiveCard() {
        // 获取当前玩家的手牌
        List<Card> hand = currentPlayer.getHand();
        if (hand.isEmpty()) {
            gameView.showMessage("你没有卡牌可以给予");
            return;
        }

        // 创建选项数组
        String[] options = new String[hand.size()];
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            String cardName = card.getName();
            // 确保卡牌名称不为空
            options[i] = (cardName != null) ? cardName : "未知卡牌";
        }

        // 显示选择对话框
        int choice = JOptionPane.showOptionDialog(
                gameView,
                "选择要给予的卡牌",
                "给予卡牌",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice >= 0 && choice < options.length) {
            Card selectedCard = hand.get(choice);
            // 处理选中的卡牌
            // ... existing code ...
        }
    }
}