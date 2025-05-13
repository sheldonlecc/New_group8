// Model/Tile/Tile.java
package Model;

import Model.Enumeration.TileName;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 瓦片类
 * 管理游戏中每个瓦片的状态和属性
 * 包括：名称、状态、位置、相邻关系等
 */
public class Tile {
    // 核心属性
    private final TileName name;              // 瓦片名称（如：FOOLS_LANDING、TEMPLE_OF_THE_SUN等）
    private TileState state;                  // 瓦片状态（如：正常、被淹没、沉没）
    private final int row;                    // 行坐标
    private final int col;                    // 列坐标
    private final List<Tile> adjacentTiles;   // 相邻瓦片列表
    private final String imagePath;           // 瓦片图片路径

    // 事件监听器
    private final List<Consumer<Tile>> onStateChangeListeners;    // 状态变化监听器

    /**
     * 构造函数
     * @param name 瓦片名称
     * @param row 行坐标
     * @param col 列坐标
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

    // =============== 基本属性访问 ===============

    /**
     * 获取瓦片名称
     * @return 瓦片名称
     */
    public TileName getName() {
        return name;
    }

    /**
     * 获取瓦片名称（向后兼容方法）
     * @return 瓦片名称
     * @deprecated 请使用getName()方法
     */
    @Deprecated
    public TileName getTileName() {
        return name;
    }

    /**
     * 获取瓦片状态
     * @return 瓦片状态
     */
    public TileState getState() {
        return state;
    }

    /**
     * 设置瓦片状态
     * @param newState 新状态
     */
    public void setState(TileState newState) {
        if (this.state != newState) {
            this.state = newState;
            notifyStateChangeListeners();
        }
    }

    /**
     * 获取行坐标
     * @return 行坐标
     */
    public int getRow() {
        return row;
    }

    /**
     * 获取列坐标
     * @return 列坐标
     */
    public int getCol() {
        return col;
    }

    /**
     * 获取瓦片图片路径
     * @return 图片路径
     */
    public String getImagePath() {
        if (state == TileState.SUNK) {
            return "src/resources/Tiles/Sea.png";
        }
        return imagePath;
    }

    // =============== 相邻瓦片管理 ===============

    /**
     * 添加相邻瓦片
     * @param tile 相邻瓦片
     */
    public void addAdjacentTile(Tile tile) {
        if (!adjacentTiles.contains(tile)) {
            adjacentTiles.add(tile);
        }
    }

    /**
     * 移除相邻瓦片
     * @param tile 要移除的相邻瓦片
     */
    public void removeAdjacentTile(Tile tile) {
        adjacentTiles.remove(tile);
    }

    /**
     * 获取所有相邻瓦片
     * @return 相邻瓦片列表
     */
    public List<Tile> getAdjacentTiles() {
        return new ArrayList<>(adjacentTiles);
    }

    /**
     * 检查是否与指定瓦片相邻
     * @param tile 目标瓦片
     * @return 如果相邻则返回true
     */
    public boolean isAdjacentTo(Tile tile) {
        return adjacentTiles.contains(tile);
    }

    // =============== 状态检查 ===============

    /**
     * 检查瓦片是否可通行
     * @return 如果瓦片状态允许通行则返回true
     */
    public boolean isPassable() {
        return state != TileState.SUNK;
    }

    /**
     * 检查瓦片是否可加固
     * @return 如果瓦片状态允许加固则返回true
     */
    public boolean isShoreable() {
        return state == TileState.FLOODED;
    }

    // =============== 事件监听器管理 ===============

    /**
     * 添加状态变化监听器
     * @param listener 监听器
     */
    public void addOnStateChangeListener(Consumer<Tile> listener) {
        onStateChangeListeners.add(listener);
    }

    private void notifyStateChangeListeners() {
        onStateChangeListeners.forEach(listener -> listener.accept(this));
    }

    // =============== 数据验证 ===============

    /**
     * 验证瓦片状态是否有效
     * @return 如果瓦片状态有效则返回true
     */
    public boolean isValid() {
        return name != null && 
               state != null && 
               row >= 0 && 
               col >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tile tile = (Tile) obj;
        return row == tile.row && col == tile.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}