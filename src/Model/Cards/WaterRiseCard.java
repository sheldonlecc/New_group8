// Model/Cards/WaterRiseCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.WaterLevel;

/**
 * Water Rise Card
 * Used to increase the water level
 * Each use increases the water level, affecting the number of cards drawn and tile flooding speed
 */
public class WaterRiseCard extends Card {
    private WaterLevel currentWaterLevel;  // Current water level

    /**
     * Create a water rise card
     */
    public WaterRiseCard() {
        super(CardType.WATER_RISE, "WaterRise", "Increase water level, increasing game difficulty");
    }

    /**
     * Get the current water level
     * @return Current water level
     */
    public WaterLevel getCurrentWaterLevel() {
        return currentWaterLevel;
    }

    /**
     * Set the current water level
     * @param waterLevel New water level
     */
    public void setCurrentWaterLevel(WaterLevel waterLevel) {
        this.currentWaterLevel = waterLevel;
    }

    /**
     * Check if the card can be used
     * @return Returns true if the card can be used
     */
    public boolean canUse() {
        if (!super.canUse() || currentWaterLevel == null) {
            return false;
        }

        // Check if water level has reached maximum
        return currentWaterLevel.getNextLevel() != null;
    }

    /**
     * Use the water rise card
     * @return Whether the use was successful
     */
    public boolean useCard() {
        if (!canUse()) {
            return false;
        }

        // Increase water level
        WaterLevel nextLevel = currentWaterLevel.getNextLevel();
        if (nextLevel == null) {
            return false;
        }
        
        currentWaterLevel = nextLevel;
        
        // Disable card after use
        setUsable(false);
        return true;
    }

    /**
     * Override equals method
     * Two water rise cards are considered equal if they have the same current water level
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof WaterRiseCard)) return false;
        WaterRiseCard other = (WaterRiseCard) obj;
        return currentWaterLevel == other.currentWaterLevel;
    }

    /**
     * Override hashCode method
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + (currentWaterLevel != null ? currentWaterLevel.hashCode() : 0);
    }

    /**
     * Override toString method
     */
    @Override
    public String toString() {
        return String.format("%s - Current Water Level: %s", super.toString(), 
            currentWaterLevel != null ? currentWaterLevel.getDisplayName() : "Not set");
    }

    /**
     * Implement abstract method use()
     * This method is called by GameController
     */
    @Override
    public void use() {
        if (!useCard()) {
            throw new IllegalStateException("Cannot use water rise card");
        }
    }
}