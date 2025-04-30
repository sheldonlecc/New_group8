// Model/Cards/SandBagCard.java
package Model.Cards;

import Model.Enumeration.CardType;

public class SandBagCard extends Card {
    public SandBagCard() {
        super(CardType.SPECIAL, "SandBag", "可以加固任意一个地块");
    }

    @Override
    public void use() {
        // 在GameController中实现具体的加固逻辑
    }
}