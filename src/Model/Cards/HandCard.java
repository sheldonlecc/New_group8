// Model/Cards/HandCard.java
package Model.Cards;

import java.util.ArrayList;
import java.util.List;

public class HandCard {
    private final List<Card> cards;
    private static final int MAX_SIZE = 7;

    public HandCard() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card) throws HandCardFullException {
        if (cards.size() >= MAX_SIZE) {
            throw new HandCardFullException("手牌已满，最多持有 " + MAX_SIZE + " 张卡牌");
        }
        cards.add(card);
    }

    public boolean removeCard(Card card) {
        return cards.remove(card);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public static class HandCardFullException extends Exception {
        public HandCardFullException(String message) {
            super(message);
        }
    }
}