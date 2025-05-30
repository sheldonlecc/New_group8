// Model/Tile/Tile.java
package Model;

import Model.Enumeration.TileName;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tile Class
 * Manages the state and attributes of each tile in the game
 * Including: name, state, position, adjacent relationships, etc.
 */
public class Tile {
    // Core attributes
    private final TileName name; // Tile name (e.g., FOOLS_LANDING, TEMPLE_OF_THE_SUN, etc.)
    private TileState state; // Tile state (e.g., normal, flooded, sunk)
    private final int row; // Row coordinate
    private final int col; // Column coordinate
    private final List<Tile> adjacentTiles; // List of adjacent tiles
    private final String imagePath; // Tile image path
    private boolean highlighted = false;

    // Event listeners
    private final List<Consumer<Tile>> onStateChangeListeners; // State change listeners

    /**
     * Constructor
     *
     * @param name Tile name
     * @param row  Row coordinate
     * @param col  Column coordinate
     */
    public Tile(TileName name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.state = TileState.NORMAL;
        this.adjacentTiles = new ArrayList<>();
        this.onStateChangeListeners = new ArrayList<>();
        this.imagePath = "src/resources/Tiles/" + name.getDisplayName() + ".png";
    }

    // =============== Basic Attribute Access ===============

    /**
     * Get tile name
     *
     * @return Tile name
     */
    public TileName getName() {
        return name;
    }

    /**
     * Get tile name (for backward compatibility)
     *
     * @return Tile name
     * @deprecated Please use getName() method
     */
    @Deprecated
    public TileName getTileName() {
        return name;
    }

    /**
     * Get tile state
     *
     * @return Tile state
     */
    public TileState getState() {
        return state;
    }

    /**
     * Set tile state
     *
     * @param newState New state
     */
    public void setState(TileState newState) {
        if (this.state != newState) {
            System.out
                    .println("[Debug] Tile " + name + " [" + row + "," + col + "] State: " + this.state + " -> " + newState);
            this.state = newState;
            notifyStateChangeListeners();
        }
    }

    /**
     * Get row coordinate
     *
     * @return Row coordinate
     */
    public int getRow() {
        return row;
    }

    /**
     * Get column coordinate
     *
     * @return Column coordinate
     */
    public int getCol() {
        return col;
    }

    /**
     * Get tile image path
     *
     * @return Image path
     */
    public String getImagePath(TileState state) {
        if (state == TileState.SUNK) {
            return "src/resources/Tiles/Sea.png";
        }
        if (state == TileState.FLOODED) {
            return "src/resources/Tiles/" + name.getDisplayName() + "2.png";
        }
        return imagePath;
    }

    // =============== Adjacent Tile Management ===============

    /**
     * Add adjacent tile
     *
     * @param tile Adjacent tile
     */
    public void addAdjacentTile(Tile tile) {
        if (!adjacentTiles.contains(tile)) {
            adjacentTiles.add(tile);
        }
    }

    /**
     * Remove adjacent tile
     *
     * @param tile Adjacent tile to remove
     */
    public void removeAdjacentTile(Tile tile) {
        adjacentTiles.remove(tile);
    }

    /**
     * Get all adjacent tiles
     *
     * @return List of adjacent tiles
     */
    public List<Tile> getAdjacentTiles() {
        return new ArrayList<>(adjacentTiles);
    }

    /**
     * Check if tile is adjacent to specified tile
     *
     * @param tile Target tile
     * @return Returns true if tiles are adjacent
     */
    public boolean isAdjacentTo(Tile tile) {
        return adjacentTiles.contains(tile);
    }

    // =============== State Checks ===============

    /**
     * Check if tile is passable
     *
     * @return Returns true if tile state allows passage
     */
    public boolean isPassable() {
        return state != TileState.SUNK;
    }

    /**
     * Check if tile can be shored up
     *
     * @return Returns true if tile state allows shoring up
     */
    public boolean isShoreable() {
        return this.state == TileState.FLOODED;
    }

    // =============== Event Listener Management ===============

    /**
     * Add state change listener
     *
     * @param listener Listener
     */
    public void addOnStateChangeListener(Consumer<Tile> listener) {
        onStateChangeListeners.add(listener);
    }

    private void notifyStateChangeListeners() {
        onStateChangeListeners.forEach(listener -> listener.accept(this));
    }

    // =============== Data Validation ===============

    /**
     * Validate if tile state is valid
     *
     * @return Returns true if tile state is valid
     */
    public boolean isValid() {
        return name != null &&
                state != null &&
                row >= 0 &&
                col >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Tile tile = (Tile) obj;
        return row == tile.row && col == tile.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    /**
     * Set whether the tile is highlighted
     * @param highlighted Whether to highlight
     */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * Get whether the tile is highlighted
     * @return Whether highlighted
     */
    public boolean isHighlighted() {
        return highlighted;
    }
}