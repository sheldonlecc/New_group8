// Model/Cards/HelicopterCard.java
package Model.Cards;

public class HelicopterCard extends SpecialCard {
    public HelicopterCard() {
        super("直升机", "移动任意玩家到安全位置");
    }

    @Override
    public void use() {
        // 具体逻辑在Controller实现
    }
}