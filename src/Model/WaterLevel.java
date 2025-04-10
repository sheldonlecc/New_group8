// Model/WaterLevel/WaterLevel.java
package Model;

public class WaterLevel {
    private int level = 1;
    private static final int MAX_LEVEL = 10;

    public void increase() {
        if (level < MAX_LEVEL) {
            level++;
        }
    }

    public int getFloodCardsPerTurn() {
        return level >= 6 ? 3 : level >= 3 ? 2 : 1;
    }

    public int getCurrentLevel() {
        return level;
    }
}