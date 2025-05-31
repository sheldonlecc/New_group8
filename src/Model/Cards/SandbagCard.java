package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileState;
import Model.Tile;

/**
 * Sandbag Card
 * Used to reinforce a flooded island tile
 * After use, prevents the target tile from being flooded
 */
public class SandbagCard extends Card {
    private Tile targetTile;  // Target tile

    /**
     * Create a sandbag card
     */
    public SandbagCard() {
        super(CardType.SAND_BAG, "SandBag", "Reinforce a flooded island tile");
    }

    /**
     * Get the target tile
     * @return Target tile
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * Check if the card can be used on the target tile
     * @param targetTile Target tile
     * @return Returns true if the card can be used
     */
    public boolean canUse(Tile targetTile) {
        if (!super.canUse() || targetTile == null) {
            return false;
        }

        // Check if target tile is flooded but not sunk
        TileState state = targetTile.getState();
        return state == TileState.FLOODED;
    }

    /**
     * Use sandbag card to reinforce target tile
     * @param targetTile Target tile to reinforce
     * @return Whether the use was successful
     */
    public boolean useCard(Tile targetTile) {
        if (!canUse(targetTile)) {
            return false;
        }

        // Reinforce target tile (change state from FLOODED to NORMAL)
        targetTile.setState(TileState.NORMAL);
        this.targetTile = targetTile;
        
        // Disable card after use
        setUsable(false);
        return true;
    }

    /**
     * Override equals method
     * Two sandbag cards are considered equal if they target the same tile
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof SandbagCard)) return false;
        SandbagCard other = (SandbagCard) obj;
        return targetTile == null ? other.targetTile == null : 
               targetTile.equals(other.targetTile);
    }

    /**
     * Override hashCode method
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + (targetTile != null ? targetTile.hashCode() : 0);
    }

    /**
     * Override toString method
     */
    @Override
    public String toString() {
        return String.format("%s - Target Tile: %s", super.toString(), 
            targetTile != null ? targetTile.getName().getDisplayName() : "Not specified");
    }

    /**
     * Implement abstract method use()
     * This method is called by GameController with target tile
     */
    @Override
    public void use() {
        // Since target tile parameter is needed, this method should be called by GameController using useCard(Tile targetTile)
        throw new UnsupportedOperationException(
            "Please use useCard(Tile targetTile) method, which requires a target tile parameter");
    }
}