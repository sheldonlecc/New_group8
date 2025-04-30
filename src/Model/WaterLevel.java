// Model/WaterLevel/WaterLevel.java
package Model;

import View.WaterLevelView;

public class WaterLevel {
    private static WaterLevel instance = new WaterLevel();
    private int level = 1;
    private static final int MAX_LEVEL = 10;
    private static WaterLevelView waterLevelView;

    private WaterLevel() {}

    public static WaterLevel getInstance() {
        return instance;
    }

    public static void setWaterLevelView(WaterLevelView view) {
        waterLevelView = view;
    }

    public static void increase() {
        if (instance.level < MAX_LEVEL) {
            instance.level++;
            if (waterLevelView != null) {
                waterLevelView.updateWaterLevel(instance.level);
            }
        }
    }

    public static int getFloodCardsPerTurn() {
        return instance.level >= 6 ? 3 : instance.level >= 3 ? 2 : 1;
    }

    public static int getCurrentLevel() {
        return instance.level;
    }
}