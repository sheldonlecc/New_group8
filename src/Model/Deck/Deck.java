// Model/Deck/Deck.java
package Model.Deck;

import Model.Cards.Card;
import java.util.Collections;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/**
 * Deck Abstract Base Class
 * Provides basic operations and state management for card decks
 * 
 * @param <T> Card type, must be a subclass of Card
 */
public abstract class Deck<T extends Card> {
    /** Draw pile */
    protected final Stack<T> drawPile;
    /** Discard pile */
    protected final Stack<T> discardPile;

    /**
     * Constructor
     * Initializes draw pile and discard pile
     */
    protected Deck() {
        this.drawPile = new Stack<>();
        this.discardPile = new Stack<>();
    }

    /**
     * Shuffle
     * Randomly shuffles the cards in the draw pile
     */
    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    /**
     * Draw a card
     * Draws a card from the top of the draw pile
     * If the draw pile is empty, reshuffles the discard pile
     * 
     * @return The drawn card, or null if the deck is empty
     */
    public T draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    /**
     * Discard a card
     * Places a card in the discard pile
     * 
     * @param card The card to discard
     */
    public void discard(T card) {
        if (card != null) {
            discardPile.push(card);
        }
    }

    /**
     * Reshuffle discard pile
     * Moves all cards from the discard pile back to the draw pile and shuffles
     */
    public void reshuffleDiscardPile() {
        while (!discardPile.isEmpty()) {
            drawPile.push(discardPile.pop());
        }
        shuffle();
    }

    /**
     * Peek at the top card
     * Does not modify the deck state
     * 
     * @return The top card of the draw pile, or null if empty
     */
    public T peek() {
        return drawPile.isEmpty() ? null : drawPile.peek();
    }

    /**
     * Get the number of cards remaining in the draw pile
     * 
     * @return Number of cards in the draw pile
     */
    public int getDrawPileSize() {
        return drawPile.size();
    }

    /**
     * Get the number of cards in the discard pile
     * 
     * @return Number of cards in the discard pile
     */
    public int getDiscardPileSize() {
        return discardPile.size();
    }

    /**
     * Check if the deck is empty
     * 
     * @return true if both draw pile and discard pile are empty
     */
    public boolean isEmpty() {
        return drawPile.isEmpty() && discardPile.isEmpty();
    }

    /**
     * Get an unmodifiable view of the draw pile
     * 
     * @return Read-only list of the draw pile
     */
    public List<T> getDrawPileView() {
        return Collections.unmodifiableList(new ArrayList<>(drawPile));
    }

    /**
     * Get an unmodifiable view of the discard pile
     * 
     * @return Read-only list of the discard pile
     */
    public List<T> getDiscardPileView() {
        return Collections.unmodifiableList(new ArrayList<>(discardPile));
    }

    /**
     * Clear the deck
     * Empties both draw pile and discard pile
     */
    public void clear() {
        drawPile.clear();
        discardPile.clear();
    }

    /**
     * Check if the deck state is valid
     * Subclasses can override this method to add specific validation logic
     * 
     * @return true if the deck state is valid
     */
    public boolean isValid() {
        return drawPile != null && discardPile != null;
    }
}