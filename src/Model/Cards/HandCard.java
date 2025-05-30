// Model/Cards/HandCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Hand Card Management Class
 * Responsible for managing player's hand card collection, including adding, removing, viewing operations
 * Also responsible for controlling hand card limit and hand card status
 */
public class HandCard implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Card> cards; // Hand card list
    private static final int MAX_CARDS = 5; // Hand card limit (as per rulebook)
    private static final int INITIAL_CARDS = 2; // Initial hand card count
    private boolean enforceLimit = true; // Whether to enforce hand card limit

    // Event listener lists
    private final List<Consumer<Card>> onCardAddedListeners = new ArrayList<>();
    private final List<Consumer<Card>> onCardRemovedListeners = new ArrayList<>();
    private final List<Consumer<HandCard>> onHandCardChangedListeners = new ArrayList<>();

    /**
     * Create hand card manager
     */
    public HandCard() {
        this.cards = new ArrayList<>();
    }

    /**
     * Set whether to enforce hand card limit
     * 
     * @param enforce Whether to enforce limit
     */
    public void setEnforceLimit(boolean enforce) {
        this.enforceLimit = enforce;
    }

    /**
     * Add card added event listener
     * 
     * @param listener Listener
     */
    public void addOnCardAddedListener(Consumer<Card> listener) {
        onCardAddedListeners.add(listener);
    }

    /**
     * Add card removed event listener
     * 
     * @param listener Listener
     */
    public void addOnCardRemovedListener(Consumer<Card> listener) {
        onCardRemovedListeners.add(listener);
    }

    /**
     * Add hand card changed event listener
     * 
     * @param listener Listener
     */
    public void addOnHandCardChangedListener(Consumer<HandCard> listener) {
        onHandCardChangedListeners.add(listener);
    }

    /**
     * Add card to hand without checking limit
     * 
     * @param card Card to add
     */
    public void addCardWithoutCheck(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        cards.add(card);
        notifyCardAdded(card);
        notifyHandCardChanged();
    }

    /**
     * Add card to hand
     * 
     * @param card Card to add
     * @throws HandCardFullException Thrown when hand is full and limit is enforced
     */
    public void addCard(Card card) throws HandCardFullException {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        if (enforceLimit && cards.size() >= MAX_CARDS) {
            throw new HandCardFullException("Hand is full, maximum " + MAX_CARDS + " cards allowed");
        }
        cards.add(card);
        notifyCardAdded(card);
        notifyHandCardChanged();
    }

    /**
     * Add multiple cards
     * 
     * @param cardsToAdd List of cards to add
     * @throws HandCardFullException Thrown when adding would exceed limit and limit is enforced
     */
    public void addCards(List<Card> cardsToAdd) throws HandCardFullException {
        if (cardsToAdd == null) {
            throw new IllegalArgumentException("Card list cannot be null");
        }
        if (enforceLimit && cards.size() + cardsToAdd.size() > MAX_CARDS) {
            throw new HandCardFullException("Adding would exceed limit of " + MAX_CARDS);
        }
        for (Card card : cardsToAdd) {
            addCard(card);
        }
    }

    /**
     * Remove card from hand
     * 
     * @param card Card to remove
     * @return Whether removal was successful
     */
    public boolean removeCard(Card card) {
        if (card == null) {
            return false;
        }
        boolean removed = cards.remove(card);
        if (removed) {
            notifyCardRemoved(card);
            notifyHandCardChanged();
        }
        return removed;
    }

    /**
     * Remove multiple cards
     * 
     * @param cardsToRemove List of cards to remove
     * @return Number of cards successfully removed
     */
    public int removeCards(List<Card> cardsToRemove) {
        if (cardsToRemove == null) {
            return 0;
        }
        int removedCount = 0;
        for (Card card : cardsToRemove) {
            if (removeCard(card)) {
                removedCount++;
            }
        }
        return removedCount;
    }

    /**
     * Get copy of hand card list
     * 
     * @return Copy of hand card list
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Get list of cards of specified type
     * 
     * @param type Card type
     * @return List of cards of specified type
     */
    public List<Card> getCardsByType(CardType type) {
        return cards.stream()
                .filter(card -> card.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get all available card types
     * 
     * @return Set of card types present in current hand
     */
    public Set<CardType> getAvailableCardTypes() {
        return cards.stream()
                .map(Card::getType)
                .collect(Collectors.toSet());
    }

    /**
     * Check if hand contains card of specified type
     * 
     * @param type Card type to check
     * @return True if contains card of specified type
     */
    public boolean hasCardType(CardType type) {
        return cards.stream()
                .anyMatch(card -> card.getType() == type);
    }

    /**
     * Get count of cards of specified type
     * 
     * @param type Card type
     * @return Count of cards of specified type
     */
    public int getCardTypeCount(CardType type) {
        return (int) cards.stream()
                .filter(card -> card.getType() == type)
                .count();
    }

    /**
     * Get hand card count
     * 
     * @return Current hand card count
     */
    public int getCardCount() {
        return cards.size();
    }

    /**
     * Check if hand is full
     * 
     * @return True if hand count has reached limit
     */
    public boolean isFull() {
        return enforceLimit && cards.size() >= MAX_CARDS;
    }

    /**
     * Get count statistics for each card type in hand
     * 
     * @return Map of card types and their counts
     */
    public Map<CardType, Integer> getCardTypeCount() {
        return cards.stream()
                .collect(Collectors.groupingBy(
                        Card::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    /**
     * Clear hand
     */
    public void clear() {
        List<Card> removedCards = new ArrayList<>(cards);
        cards.clear();
        for (Card card : removedCards) {
            notifyCardRemoved(card);
        }
        notifyHandCardChanged();
    }

    /**
     * Check if hand contains specified card
     * 
     * @param card Card to check
     * @return True if contains specified card
     */
    public boolean contains(Card card) {
        return cards.contains(card);
    }

    /**
     * Get initial hand card count
     * 
     * @return Initial hand card count
     */
    public static int getInitialCardCount() {
        return INITIAL_CARDS;
    }

    /**
     * Get hand card limit
     * 
     * @return Hand card limit
     */
    public static int getMaxCards() {
        return MAX_CARDS;
    }

    /**
     * Validate if hand is legal
     * 
     * @return True if hand is legal
     */
    public boolean isValid() {
        return !enforceLimit || cards.size() <= MAX_CARDS;
    }

    /**
     * Hand Card Full Exception
     * Thrown when attempting to add card but hand is full
     */
    public static class HandCardFullException extends Exception {
        public HandCardFullException(String message) {
            super(message);
        }
    }

    // Private method: Notify card added event
    private void notifyCardAdded(Card card) {
        for (Consumer<Card> listener : onCardAddedListeners) {
            listener.accept(card);
        }
    }

    // Private method: Notify card removed event
    private void notifyCardRemoved(Card card) {
        for (Consumer<Card> listener : onCardRemovedListeners) {
            listener.accept(card);
        }
    }

    // Private method: Notify hand card changed event
    private void notifyHandCardChanged() {
        for (Consumer<HandCard> listener : onHandCardChangedListeners) {
            listener.accept(this);
        }
    }

    /**
     * Override toString method
     * 
     * @return String representation of hand card information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand Card Count: ").append(cards.size()).append("/").append(MAX_CARDS).append("\n");
        Map<CardType, Integer> typeCount = getCardTypeCount();
        for (Map.Entry<CardType, Integer> entry : typeCount.entrySet()) {
            sb.append(entry.getKey().name()).append(": ").append(entry.getValue()).append(" cards\n");
        }
        return sb.toString();
    }
}