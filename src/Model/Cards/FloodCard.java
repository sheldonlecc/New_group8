// Model/Cards/FloodCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Tile;

public class FloodCard extends Card {
    private final Tile tile;

    public FloodCard(Tile tile) {
        super(CardType.FLOOD, "洪水卡", "淹没指定地块");
        this.tile = tile;
    }

    @Override
    public void use() {
        tile.flood();
    }
}