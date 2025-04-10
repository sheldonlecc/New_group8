// Model/Deck/FloodDeck.java
package Model.Deck;

import Model.Cards.FloodCard;
import Model.Tile;

import java.util.List;

public class FloodDeck extends Deck<FloodCard> {
    public FloodDeck(List<Tile> initialTiles) {
        initialTiles.forEach(tile -> drawPile.push(new FloodCard(tile)));
        shuffle();
    }
}