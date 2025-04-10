// Model/Deck/TreasureDeck.java
package Model.Deck;

import Model.Cards.*;
import Model.Enumeration.TreasureType;
import java.util.Arrays;

public class TreasureDeck extends Deck<Card> {
    public TreasureDeck() {
        initializeCards();
        shuffle();
    }

    private void initializeCards() {
        // 添加宝藏卡（每个类型5张）
        Arrays.stream(TreasureType.values()).forEach(type -> {
            for (int i = 0; i < 5; i++) {
                drawPile.push(new TreasureCard(type));
            }
        });

        // 添加特殊卡（3张直升机卡）
        for (int i = 0; i < 3; i++) {
            drawPile.push(new HelicopterCard());
        }
    }
}