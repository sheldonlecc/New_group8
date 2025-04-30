// Model/Tile/Tile.java
package Model;

import Model.Enumeration.TileType;
import Model.Enumeration.TileName;

public class Tile {
    private TileType state = TileType.DRY;
    private final int x;
    private final int y;
    private TileName tileName;
    private String imagePath;

    public Tile(int x, int y, TileName tileName) {
        this.x = x;
        this.y = y;
        this.tileName = tileName;
        this.imagePath = "src/resources/Tiles/" + tileName.getDisplayName() + ".png";
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

    public TileName getTileName() {
        return tileName;
    }

    public String getImagePath() {
        if (state == TileType.SUNKEN) {
            return "src/resources/Tiles/Sea.png";
        }
        return imagePath;
    }
}