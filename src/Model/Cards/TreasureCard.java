// Model/Cards/TreasureCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TreasureType;

public class TreasureCard extends Card {
    private final TreasureType treasureType;

    public TreasureCard(TreasureType type) {
        super(CardType.TREASURE, type.name() + "之证", "收集" + type + "宝藏的凭证");
        this.treasureType = type;
    }

    public TreasureType getTreasureType() {
        return treasureType;
    }

    @Override
    public void use() {
        // 收集宝藏逻辑在Controller实现
    }
}