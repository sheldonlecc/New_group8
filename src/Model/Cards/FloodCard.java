// Model/Cards/FloodCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileType;
import Model.Tile;
import Model.Enumeration.TileState;

/**
 * Flood Card
 * Used to flood specified island tiles
 * When a tile is already flooded, it will sink
 */
public class FloodCard extends Card {
    private final Tile targetTile; // Target tile

    /**
     * Create a flood card
     * 
     * @param targetTile Target tile
     */
    public FloodCard(Tile targetTile) {
        super(CardType.FLOOD, "Flood", "Flood the specified tile, if the tile is already flooded it will sink");
        this.targetTile = targetTile;
    }

    /**
     * Get the target tile
     * 
     * @return Target tile
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * Check if the card can be used
     * 
     * @return Returns true if the target tile exists and is not sunk
     */
    @Override
    public boolean canUse() {
        return super.canUse() &&
                targetTile != null &&
                targetTile.getState() != TileState.SUNK;
    }

    /**
     * Use the flood card
     * If the tile is not flooded, flood it
     * If the tile is already flooded, sink it and remove the corresponding card from the flood deck
     * 
     * @param floodDeck Flood deck (used to remove cards for sunk tiles)
     * @return Whether the use was successful
     */
    public void use(Model.Deck.FloodDeck floodDeck) {
        if (!canUse()) {
            return;
        }

        Tile tile = getTargetTile();
        TileState before = tile.getState();
        if (before == TileState.NORMAL) {
            tile.setState(TileState.FLOODED);
            System.out.println(
                    "[Debug] Flood card applied to " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "], State: Normal -> Flooded");
        } else if (before == TileState.FLOODED) {
            tile.setState(TileState.SUNK);
            System.out.println(
                    "[Debug] Flood card applied to " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "], State: Flooded -> Sunk");
            if (floodDeck != null) {
                floodDeck.removeCardForSunkTile(tile);
            }
        }
    }

    @Override
    public void use() {
        use(null);
    }

    /**
     * Override equals method
     * Two flood cards are considered equal if they target the same tile
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof FloodCard))
            return false;
        FloodCard other = (FloodCard) obj;
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
        return String.format("%s - Target Tile: %s", super.toString(), targetTile.getName());
    }
}