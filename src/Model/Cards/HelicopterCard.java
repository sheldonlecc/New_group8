// Model/Cards/HelicopterCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileType;
import Model.Enumeration.TileName;
import Model.Tile;
import Model.Player;
import java.util.List;

/**
 * Helicopter Rescue Card
 * Function 1: Can rescue all players from the island (Victory condition)
 * Function 2: Can move any number of players from one tile to any other tile
 * Usage conditions:
 * 1. Victory condition: All players must be in the same location (Helipad)
 * 2. Movement function: All players to be moved must be on the same tile
 */
public class HelicopterCard extends Card {
    private final Tile targetTile;  // Target location (Helipad)

    /**
     * Create a helicopter rescue card
     * @param targetTile Target location (Helipad)
     */
    public HelicopterCard(Tile targetTile) {
        super(CardType.HELICOPTER, "Helicopter", "Rescue all players from the island or move to any tile");
        this.targetTile = targetTile;
    }

    /**
     * Get the target location
     * @return Target location (Helipad)
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * Check if the card can be used (Victory condition)
     * @param players Player list
     * @return Returns true if all players are at the target location
     */
    public boolean canUseForVictory(List<Player> players) {
        if (!super.canUse() || targetTile == null || players == null || players.isEmpty()) {
            return false;
        }

        // Check if target location is helipad
        if (targetTile.getTileName() != TileName.FOOLS_LANDING) {
            return false;
        }

        // Check if all players are at the target location
        return players.stream()
                     .allMatch(player -> player.getCurrentTile().equals(targetTile));
    }

    /**
     * Check if the card can be used (Movement function)
     * @param players List of players to move
     * @return Returns true if all players are on the same tile
     */
    public boolean canUseForMovement(List<Player> players) {
        if (!super.canUse() || players == null || players.isEmpty()) {
            return false;
        }

        // Check if all players are on the same tile
        Tile firstPlayerTile = players.get(0).getCurrentTile();
        return players.stream()
                     .allMatch(player -> player.getCurrentTile().equals(firstPlayerTile));
    }

    /**
     * Use helicopter rescue card (Victory condition)
     * Rescue all players from the island
     * @param players List of players to rescue
     * @return Whether the use was successful
     */
    public boolean useForVictory(List<Player> players) {
        if (!canUseForVictory(players)) {
            return false;
        }

        // Mark all players as rescued
        players.forEach(Player::rescue);
        
        // Disable card after use
        setUsable(false);
        return true;
    }

    /**
     * Use helicopter rescue card (Movement function)
     * Move players to target tile
     * @param players List of players to move
     * @param targetTile Target tile
     * @return Whether the use was successful
     */
    public boolean useForMovement(List<Player> players, Tile targetTile) {
        if (!canUseForMovement(players) || targetTile == null) {
            return false;
        }

        // Move all players to target tile
        players.forEach(player -> player.setCurrentTile(targetTile));
        
        // Disable card after use
        setUsable(false);
        return true;
    }

    /**
     * Override equals method
     * Two helicopter cards are considered equal if they target the same location
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof HelicopterCard)) return false;
        HelicopterCard other = (HelicopterCard) obj;
        return targetTile.equals(other.targetTile);
    }

    /**
     * Override hashCode method
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + targetTile.hashCode();
    }

    /**
     * Override toString method
     */
    @Override
    public String toString() {
        return String.format("%s - Target Location: %s", super.toString(), 
            targetTile != null ? targetTile.getTileName().getDisplayName() : "Not specified");
    }

    /**
     * Implement abstract method use()
     * This method is called by GameController with player list
     */
    @Override
    public void use() {
        // Since player list parameter is needed, this method should be called by GameController using useForVictory or useForMovement
        throw new UnsupportedOperationException(
            "Please use useForVictory(List<Player> players) or useForMovement(List<Player> players, Tile targetTile) method");
    }
}