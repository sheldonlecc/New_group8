// Model/Deck/TreasureDeck.java
package Model.Deck;

import Model.Cards.*;
import Model.Enumeration.TreasureType;
import Model.Tile;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Treasure Deck Class
 * Manages treasure cards and special cards in the game
 * Includes: Treasure Cards, Helicopter Rescue Cards, Sandbag Cards, Water Rise Cards
 */
public class TreasureDeck extends Deck<Card> {
    private boolean isFirstDraw; // Whether in initial draw phase
    private final Tile helicopterTile; // Helipad location
    private final Map<TreasureType, Integer> collectedTreasures; // Number of collected treasures

    /**
     * Constructor
     * 
     * @param helicopterTile Helipad location, used for creating helicopter rescue cards
     */
    public TreasureDeck(Tile helicopterTile) {
        super();
        this.helicopterTile = helicopterTile;
        this.isFirstDraw = true;
        this.collectedTreasures = new EnumMap<>(TreasureType.class);
        initializeCards();
        shuffle();
    }

    /**
     * Initialize deck
     * Create all treasure cards and special cards
     */
    private void initializeCards() {
        // Add treasure cards (5 of each type)
        Arrays.stream(TreasureType.values()).forEach(type -> {
            for (int i = 0; i < 5; i++) {
                drawPile.push(new TreasureCard(type));
            }
        });

        // Add special cards
        for (int i = 0; i < 3; i++) {
            drawPile.push(new HelicopterCard(helicopterTile));
            drawPile.push(new WaterRiseCard());
            drawPile.push(new SandbagCard());
        }
    }

    /**
     * Draw a card
     * Override parent class method, add special card handling logic
     * 
     * @return Drawn card, returns null if deck is empty
     */
    @Override
    public Card draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    /**
     * Initial draw
     * Special handling: If a water rise card is drawn, move it to the bottom of the deck
     * 
     * @return Drawn card, returns null if deck is empty
     */
    public Card drawInitialCard() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        if (drawPile.isEmpty()) {
            return null;
        }

        Card topCard = drawPile.peek();
        if (topCard instanceof WaterRiseCard && isFirstDraw) {
            moveCardToBottom(topCard);
            return drawInitialCard();
        }
        return drawPile.pop();
    }

    /**
     * Move card to bottom of deck
     * 
     * @param card Card to move
     */
    private void moveCardToBottom(Card card) {
        drawPile.pop(); // Remove top card
        Stack<Card> tempStack = new Stack<>();
        while (!drawPile.isEmpty()) {
            tempStack.push(drawPile.pop());
        }
        drawPile.push(card);
        while (!tempStack.isEmpty()) {
            drawPile.push(tempStack.pop());
        }
    }

    /**
     * End initial draw phase
     */
    public void finishInitialDraw() {
        isFirstDraw = false;
    }

    /**
     * Record treasure collection
     * 
     * @param type Treasure type
     */
    public void recordTreasureCollection(TreasureType type) {
        collectedTreasures.merge(type, 1, Integer::sum);
    }

    /**
     * Check if treasure is collected
     * 
     * @param type Treasure type
     * @return Returns true if 4 treasures of this type have been collected
     */
    public boolean isTreasureCollected(TreasureType type) {
        return collectedTreasures.getOrDefault(type, 0) >= 1;
    }

    /**
     * Get number of collected treasures
     * 
     * @param type Treasure type
     * @return Number of collected treasures
     */
    public int getCollectedTreasureCount(TreasureType type) {
        return collectedTreasures.getOrDefault(type, 0);
    }

    /**
     * Get all collected treasure types
     * 
     * @return Set of completed treasure types
     */
    public Set<TreasureType> getCollectedTreasures() {
        return collectedTreasures.entrySet().stream()
                .filter(entry -> entry.getValue() >= 4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Get count of cards of specified type
     * 
     * @param cardType Card type
     * @return Number of cards of this type
     */
    public int getCardTypeCount(Class<? extends Card> cardType) {
        return (int) Stream.concat(drawPile.stream(), discardPile.stream())
                .filter(cardType::isInstance)
                .count();
    }

    /**
     * Check if all treasures are collected
     * 
     * @return Returns true if 4 treasures of each type have been collected
     */
    public boolean areAllTreasuresCollected() {
        return Arrays.stream(TreasureType.values())
                .allMatch(this::isTreasureCollected);
    }

    /**
     * Check if all treasures are collected (external interface)
     * 
     * @return Returns true if 4 treasures of each type have been collected
     */
    public boolean allTreasuresCollected() {
        return areAllTreasuresCollected();
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                helicopterTile != null &&
                drawPile.stream().allMatch(card -> card != null) &&
                discardPile.stream().allMatch(card -> card != null);
    }
}