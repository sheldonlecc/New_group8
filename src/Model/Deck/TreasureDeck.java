// Model/Deck/TreasureDeck.java
package Model.Deck;

import Model.Cards.*;
import Model.Enumeration.TreasureType;
import java.util.Arrays;
import java.util.Stack;

public class TreasureDeck extends Deck<Card> {
    private boolean isFirstDraw = true;

    public TreasureDeck() {
        initializeCards();
        shuffle();
    }

    @Override
    public Card draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        if (drawPile.isEmpty()) {
            return null;
        }

        Card drawnCard = drawPile.pop();
        return drawnCard;
    }

    public Card drawInitialCard() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        if (drawPile.isEmpty()) {
            return null;
        }

        // 检查顶部的卡是否是WaterRise卡
        Card topCard = drawPile.peek();
        if (topCard instanceof WaterRiseCard && isFirstDraw) {
            // 如果是初始发牌且是WaterRise卡，将其移到牌堆底部
            drawPile.pop();
            Stack<Card> tempStack = new Stack<>();
            // 将所有卡移到临时堆
            while (!drawPile.isEmpty()) {
                tempStack.push(drawPile.pop());
            }
            // 将WaterRise卡放在底部
            drawPile.push(topCard);
            // 将其他卡放回
            while (!tempStack.isEmpty()) {
                drawPile.push(tempStack.pop());
            }
            // 递归调用drawInitialCard直到抽到非WaterRise卡
            return drawInitialCard();
        }
        Card drawnCard = drawPile.pop();
        return drawnCard;
    }

    public void finishInitialDraw() {
        isFirstDraw = false;
    }
    

    private void initializeCards() {
        // 添加宝藏卡（每个类型5张）
        Arrays.stream(TreasureType.values()).forEach(type -> {
            for (int i = 0; i < 5; i++) {
                drawPile.push(new TreasureCard(type));
            }
        });

        // 添加特殊卡（3张直升机卡和3张水位上升卡）
        for (int i = 0; i < 3; i++) {
            drawPile.push(new HelicopterCard());
            drawPile.push(new WaterRiseCard());
        }
    }
}