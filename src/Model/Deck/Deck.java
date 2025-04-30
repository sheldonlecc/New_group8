// Model/Deck/Deck.java
package Model.Deck;

import Model.Cards.Card;
import java.util.Collections;
import java.util.Stack;

public abstract class Deck<T extends Card> {
    protected Stack<T> drawPile = new Stack<>();
    protected Stack<T> discardPile = new Stack<>();

    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    public T draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    public void discard(T card) {
        discardPile.push(card);
    }

    public void reshuffleDiscardPile() {
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }
}