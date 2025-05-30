// Model/WaterLevel/WaterLevel.java
package Model;

import View.WaterLevelView;
import Model.Enumeration.WaterLevelState;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

/**
 * Water Level Class
 * Manages the water level system of the game
 * Including: water level, card draw count, water level changes, etc.
 */
public class WaterLevel {
    // Water level constants
    public static final int MIN_LEVEL = 1;     // Minimum water level
    public static final int MAX_LEVEL = 10;    // Maximum water level
    public static final int START_LEVEL = 1;   // Initial water level (remains unchanged, overridden by difficulty settings)

    // Singleton instance
    private static WaterLevel instance;
    private static WaterLevelView waterLevelView;

    // Core attributes
    private int currentLevel;                  // Current water level
    private WaterLevelState state;             // Water level state
    private final List<Consumer<WaterLevel>> onLevelChangeListeners;    // Water level change listeners

    /**
     * Private constructor
     * Initialize water level system
     */
    private WaterLevel() {
        this.currentLevel = START_LEVEL;
        this.state = WaterLevelState.NORMAL;
        this.onLevelChangeListeners = new ArrayList<>();
    }

    /**
     * Get WaterLevel singleton instance
     * @return WaterLevel instance
     */
    public static WaterLevel getInstance() {
        if (instance == null) {
            instance = new WaterLevel();
        }
        return instance;
    }

    /**
     * Set water level view
     * @param view Water level view
     */
    public static void setWaterLevelView(WaterLevelView view) {
        waterLevelView = view;
    }

    // =============== Water Level Management ===============

    /**
     * Get current water level
     * @return Current water level
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Get current water level state
     * @return Water level state
     */
    public WaterLevelState getState() {
        return state;
    }

    /**
     * Get card draw count for current water level
     * @return Card draw count
     */
    public int getDrawCount() {
        // Return corresponding card draw count based on water level
        if (currentLevel <= 2) return 2;
        if (currentLevel <= 5) return 3;
        if (currentLevel <= 7) return 4;
        return 5;
    }

    /**
     * Increase water level
     * @return Whether increase was successful
     */
    public boolean increaseLevel() {
        if (currentLevel >= MAX_LEVEL) {
            return false;
        }
        currentLevel++;
        updateState();
        notifyLevelChangeListeners();
        if (waterLevelView != null) {
            waterLevelView.updateWaterLevel(currentLevel);
        }
        return true;
    }

    /**
     * Decrease water level
     * @return Whether decrease was successful
     */
    public boolean decreaseLevel() {
        if (currentLevel <= MIN_LEVEL) {
            return false;
        }
        currentLevel--;
        updateState();
        notifyLevelChangeListeners();
        if (waterLevelView != null) {
            waterLevelView.updateWaterLevel(currentLevel);
        }
        return true;
    }

    /**
     * Reset water level
     */
    public void resetLevel() {
        currentLevel = START_LEVEL;
        updateState();
        notifyLevelChangeListeners();
        if (waterLevelView != null) {
            waterLevelView.updateWaterLevel(currentLevel);
        }
    }

    // =============== State Management ===============

    /**
     * Update water level state
     * Update state based on current water level
     */
    private void updateState() {
        if (currentLevel >= MAX_LEVEL) {
            state = WaterLevelState.CRITICAL;
        } else if (currentLevel >= 7) {
            state = WaterLevelState.DANGEROUS;
        } else {
            state = WaterLevelState.NORMAL;
        }
    }

    // =============== Event Listener Management ===============

    /**
     * Add water level change listener
     * @param listener Listener
     */
    public void addOnLevelChangeListener(Consumer<WaterLevel> listener) {
        onLevelChangeListeners.add(listener);
    }

    private void notifyLevelChangeListeners() {
        onLevelChangeListeners.forEach(listener -> listener.accept(this));
    }

    // =============== Data Validation ===============

    /**
     * Validate if water level state is valid
     * @return Returns true if water level state is valid
     */
    public boolean isValid() {
        return currentLevel >= MIN_LEVEL && 
               currentLevel <= MAX_LEVEL && 
               state != null;
    }

    @Override
    public String toString() {
        return String.format("Water Level: %d, State: %s, Card Draw Count: %d", 
            currentLevel, state, getDrawCount());
    }

    /**
     * Set current water level (for initialization)
     * @param level Water level
     */
    public void setCurrentLevel(int level) {
        if (level >= MIN_LEVEL && level <= MAX_LEVEL) {
            this.currentLevel = level;
            updateState();
            notifyLevelChangeListeners();
            if (waterLevelView != null) {
                waterLevelView.updateWaterLevel(currentLevel);
            }
        }
    }
}