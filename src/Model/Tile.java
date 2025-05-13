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

    /**
     * 检查瓦片是否被淹没
     * @return 如果瓦片状态为FLOODED则返回true
     */
    public boolean isFlooded() {
        return state == TileType.FLOODED;
    }

    /**
     * 检查瓦片是否已沉没
     * @return 如果瓦片状态为SUNKEN则返回true
     */
    public boolean isSunk() {
        return state == TileType.SUNKEN;
    }

    /**
     * 将瓦片恢复为干燥状态
     * 如果瓦片已被淹没，则将其恢复为干燥状态
     * 如果瓦片已沉没，则不做任何操作
     */
    public void dry() {
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