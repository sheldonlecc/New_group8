// Model/Deck/FloodDeck.java
package Model.Deck;

import Model.Cards.FloodCard;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flood Deck Class
 * Manages the flood card deck in the game, handles special rules for flood cards
 * New mechanism: Draw 6 cards from all locations as current FloodDeck, refill with 6 new cards when empty
 */
public class FloodDeck extends Deck<FloodCard> {
    /** Master deck - contains all available flood cards */
    private final List<FloodCard> masterDeck;
    /** Current active 6 cards */
    private final List<FloodCard> activeDeck;
    /** Current active deck index */
    private int currentIndex;
    
    /**
     * Constructor
     * Creates flood card deck based on initial tile list
     * 
     * @param initialTiles Initial tile list, each tile corresponds to a flood card
     */
    public FloodDeck(List<Tile> initialTiles) {
        super();
        this.masterDeck = new ArrayList<>();
        this.activeDeck = new ArrayList<>();
        this.currentIndex = 0;
        
        if (initialTiles != null) {
            initialTiles.forEach(tile -> masterDeck.add(new FloodCard(tile)));
            refillActiveDeck();
        }
    }
    
    /**
     * Refill active deck
     * Randomly draws 6 cards from master deck as new active deck
     */
    private void refillActiveDeck() {
        activeDeck.clear();
        currentIndex = 0;
        
        // Get all available cards (corresponding to unsunk tiles)
        List<FloodCard> availableCards = masterDeck.stream()
                .filter(card -> card.getTargetTile().getState() != TileState.SUNK)
                .collect(Collectors.toList());
        
        if (availableCards.isEmpty()) {
            System.out.println("[Warning] No available flood cards!");
            return;
        }
        
        // Randomly select up to 6 cards
        Collections.shuffle(availableCards);
        int cardCount = Math.min(6, availableCards.size());
        
        for (int i = 0; i < cardCount; i++) {
            activeDeck.add(availableCards.get(i));
        }
        
        System.out.println("[Log] Refilled FloodDeck, current active cards: " + activeDeck.size());
    }
    
    /**
     * Draw a card
     * Draws sequentially from current active 6 cards
     * When all 6 cards are drawn, refills the active deck
     * 
     * @return Drawn card, returns null if no cards available
     */
    @Override
    public FloodCard draw() {
        // If current active deck is empty, refill
        if (currentIndex >= activeDeck.size()) {
            refillActiveDeck();
            if (activeDeck.isEmpty()) {
                return null;
            }
        }
        
        FloodCard card = activeDeck.get(currentIndex);
        currentIndex++;
        
        System.out.println("[Log] Drew card from FloodDeck: " + card.getTargetTile().getName() + 
                          " (Remaining: " + (activeDeck.size() - currentIndex) + "/" + activeDeck.size() + ")");
        
        return card;
    }
    
    /**
     * Get all flooded tiles
     * 
     * @return List of tiles currently in FLOODED state
     */
    public List<Tile> getFloodedTiles() {
        return masterDeck.stream()
                .map(FloodCard::getTargetTile)
                .filter(tile -> tile.getState() == TileState.FLOODED)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unflooded tiles
     * 
     * @return List of tiles currently in master deck corresponding to unsunk tiles
     */
    public List<Tile> getUnfloodedTiles() {
        return masterDeck.stream()
                .map(FloodCard::getTargetTile)
                .filter(tile -> tile.getState() != TileState.SUNK)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if specified tile is flooded
     * 
     * @param tile Tile to check
     * @return Returns true if tile state is FLOODED
     */
    public boolean isTileFlooded(Tile tile) {
        return tile.getState() == TileState.FLOODED;
    }
    
    /**
     * Get flood card for specified tile
     * 
     * @param tile Target tile
     * @return Corresponding flood card, returns null if not found
     */
    public FloodCard getFloodCardForTile(Tile tile) {
        return masterDeck.stream()
                .filter(card -> card.getTargetTile().equals(tile))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if all tiles are flooded
     * 
     * @return Returns true if all tiles are in SUNK state
     */
    public boolean areAllTilesFlooded() {
        return masterDeck.stream()
                .map(FloodCard::getTargetTile)
                .allMatch(tile -> tile.getState() == TileState.SUNK);
    }
    
    /**
     * Reset flood card state
     * Refills active deck
     */
    public void resetFloodCards() {
        refillActiveDeck();
    }
    
    /**
     * Remove flood card for specified tile (when tile is sunk)
     * 
     * @param tile Sunk tile
     */
    public void removeCardForSunkTile(Tile tile) {
        masterDeck.removeIf(card -> card.getTargetTile().equals(tile));
        activeDeck.removeIf(card -> card.getTargetTile().equals(tile));
        // If cards are removed from active deck, adjust index
        if (currentIndex > activeDeck.size()) {
            currentIndex = activeDeck.size();
        }
    }
    
    /**
     * Get remaining cards in current active deck
     * 
     * @return Number of remaining cards in active deck
     */
    public int getActiveDeckRemainingCount() {
        return Math.max(0, activeDeck.size() - currentIndex);
    }
    
    /**
     * Get total number of cards in master deck
     * 
     * @return Total number of cards in master deck
     */
    public int getMasterDeckSize() {
        return masterDeck.size();
    }
    
    /**
     * Force refill active deck
     * Used for reset in special cases
     */
    public void forceRefillActiveDeck() {
        refillActiveDeck();
    }
    
    @Override
    public boolean isValid() {
        return masterDeck != null && activeDeck != null &&
                masterDeck.stream().allMatch(card -> card != null && card.getTargetTile() != null) &&
                activeDeck.stream().allMatch(card -> card != null && card.getTargetTile() != null);
    }
    
    // The following methods maintain compatibility but are not used in new mechanism
    @Override
    public void discard(FloodCard card) {
        // In new mechanism, discard pile is not needed
        // Kept for compatibility
    }
    
    @Override
    public void reshuffleDiscardPile() {
        // In new mechanism, similar functionality is implemented through refillActiveDeck
        refillActiveDeck();
    }
    
    @Override
    public void shuffle() {
        // In new mechanism, shuffling happens automatically during refillActiveDeck
        Collections.shuffle(masterDeck);
    }
}