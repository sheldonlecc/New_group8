package Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Tile Position Class
 * Manages the position information of all tiles in the game
 * Provides functionality to store, retrieve, and validate tile positions
 */
public class TilePosition {
    // Map to store tile names and their corresponding coordinates
    // Key: tile name (String)
    // Value: coordinates array [row, col]
    private Map<String, int[]> tilePositions;

    /**
     * Constructor
     * Initializes the tile positions map
     */
    public TilePosition() {
        this.tilePositions = new HashMap<>();
    }

    /**
     * Add tile position information
     * @param tileName Name of the tile
     * @param x Row coordinate
     * @param y Column coordinate
     */
    public void addTilePosition(String tileName, int x, int y) {
        tilePositions.put(tileName, new int[]{x, y});
    }

    /**
     * Get position of a specific tile
     * @param tileName Name of the tile
     * @return Array containing [row, col] coordinates, or null if tile not found
     */
    public int[] getTilePosition(String tileName) {
        return tilePositions.get(tileName);
    }

    /**
     * Get all tile positions
     * @return Map containing all tile names and their positions
     */
    public Map<String, int[]> getAllTilePositions() {
        return tilePositions;
    }

    /**
     * Check if a specific tile exists
     * @param tileName Name of the tile to check
     * @return true if the tile exists, false otherwise
     */
    public boolean containsTile(String tileName) {
        return tilePositions.containsKey(tileName);
    }

    /**
     * Clear all position information
     * Used when resetting the game
     */
    public void clear() {
        tilePositions.clear();
    }

    /**
     * Get tile name at specific coordinates
     * @param row Row coordinate
     * @param col Column coordinate
     * @return Name of the tile at the specified coordinates, or null if no tile found
     */
    public String getTileName(int row, int col) {
        for (Map.Entry<String, int[]> entry : tilePositions.entrySet()) {
            int[] pos = entry.getValue();
            if (pos[0] == row && pos[1] == col) {
                return entry.getKey();
            }
        }
        return null;
    }
} 