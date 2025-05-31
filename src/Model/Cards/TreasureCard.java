// Model/Cards/TreasureCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TreasureType;
import Model.Tile;
import Model.Player;
import java.util.List;

/**
 * Treasure Card
 * Used to collect corresponding treasures
 * Requires 4 cards of the same treasure type to collect the treasure
 */
public class TreasureCard extends Card {
    private final TreasureType treasureType;  // Treasure type
    private Tile targetTile;  // Target treasure location

    /**
     * Create a treasure card
     * @param treasureType Treasure type
     */
    public TreasureCard(TreasureType treasureType) {
        super(CardType.TREASURE, 
              String.format("%s", treasureType.getDisplayName()),
              String.format("Used to collect %s", treasureType.getDisplayName()));
        this.treasureType = treasureType;
    }

    /**
     * Get the treasure type
     * @return Treasure type
     */
    public TreasureType getTreasureType() {
        return treasureType;
    }

    /**
     * Get the target treasure location
     * @return Target treasure location
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * Check if the card can be used
     * @param players Player list
     * @param targetTile Target treasure location
     * @return Returns true if the card can be used
     */
    public boolean canUse(List<Player> players, Tile targetTile) {
        if (!super.canUse() || players == null || targetTile == null) {
            return false;
        }

        // Check if any player is at the target location
        boolean hasPlayerAtTarget = players.stream()
                                         .anyMatch(player -> player.getCurrentTile().equals(targetTile));
        
        // Check if target location is the corresponding treasure location
        // Note: Specific treasure location check logic should be implemented in GameController
        // Here we only check basic conditions
        return hasPlayerAtTarget;
    }

    /**
     * Use the treasure card
     * @param players Player list
     * @param targetTile Target treasure location
     * @return Whether the use was successful
     */
    public boolean use(List<Player> players, Tile targetTile) {
        if (!canUse(players, targetTile)) {
            return false;
        }

        // Record target location
        this.targetTile = targetTile;
        
        // Disable card after use
        setUsable(false);
        return true;
    }

    /**
     * Override equals method
     * Two treasure cards are considered equal if they are of the same treasure type
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof TreasureCard)) return false;
        TreasureCard other = (TreasureCard) obj;
        return treasureType == other.treasureType;
    }

    /**
     * Override hashCode method
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + treasureType.hashCode();
    }

    /**
     * Override toString method
     */
    @Override
    public String toString() {
        return String.format("%s - Treasure Type: %s", super.toString(), 
            treasureType.getDisplayName());
    }

    /**
     * Implement abstract method use()
     * This method is called by GameController with player list and target location
     */
    @Override
    public void use() {
        // Since player list and target location parameters are needed, this method should be called by GameController using use(List<Player> players, Tile targetTile)
        throw new UnsupportedOperationException(
            "Please use use(List<Player> players, Tile targetTile) method, which requires player list and target location parameters");
    }
}