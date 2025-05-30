package Model.Enumeration;

/**
 * Treasure Type Enumeration
 * Defines the types of treasures that can be collected in the game
 */
public enum TreasureType {
    EARTH("Earth"),      // Earth Crystal
    WIND("Wind"),       // Wind Statue
    FIRE("Fire"),       // Fire Gem
    WATER("Water");      // Ocean Chalice

    private final String displayName;

    TreasureType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the display name of the treasure
     * @return The display name of the treasure
     */
    public String getDisplayName() {
        return displayName;
    }
}