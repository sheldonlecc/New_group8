// Model/Tile/Tile.java
package Model;

import Model.Enumeration.TileType;

public class Tile {
    private TileType state = TileType.DRY;
    private final int x;
    private final int y;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void flood() {
        state = (state == TileType.DRY) ? TileType.FLOODED : TileType.SUNKEN;
    }

    public void shoreUp() {
        if (state == TileType.FLOODED) {
            state = TileType.DRY;
        }
    }

    public TileType getState() {
        return state;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}