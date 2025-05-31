package Model.Enumeration;

/**
 * Water Level Enumeration
 * Defines water levels in the game
 * Water level affects the number of cards drawn and tile flooding speed
 */
public enum WaterLevel {
    LEVEL_1("1", 2),  // Initial water level, draw 2 flood cards per turn
    LEVEL_2("2", 3),  // Draw 3 flood cards per turn
    LEVEL_3("3", 4),  // Draw 4 flood cards per turn
    LEVEL_4("4", 5),  // Draw 5 flood cards per turn
    LEVEL_5("5", 6);  // Highest water level, draw 6 flood cards per turn

    private final String displayName;  // Display name
    private final int floodCardCount;  // Number of cards drawn per turn

    WaterLevel(String displayName, int floodCardCount) {
        this.displayName = displayName;
        this.floodCardCount = floodCardCount;
    }

    /**
     * Get display name
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get number of cards drawn per turn
     * @return Number of cards drawn per turn
     */
    public int getFloodCardCount() {
        return floodCardCount;
    }

    /**
     * Get next water level
     * @return Next water level, returns null if already at highest level
     */
    public WaterLevel getNextLevel() {
        switch (this) {
            case LEVEL_1:
                return LEVEL_2;
            case LEVEL_2:
                return LEVEL_3;
            case LEVEL_3:
                return LEVEL_4;
            case LEVEL_4:
                return LEVEL_5;
            default:
                return null;
        }
    }
    // Example usage
} 