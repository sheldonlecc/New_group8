package Model;

import java.util.HashMap;
import java.util.Map;

public class TilePosition {
    private Map<String, int[]> tilePositions; // 存储板块名称和对应的坐标

    public TilePosition() {
        this.tilePositions = new HashMap<>();
    }

    // 添加板块位置信息
    public void addTilePosition(String tileName, int x, int y) {
        tilePositions.put(tileName, new int[]{x, y});
    }

    // 获取特定板块的位置
    public int[] getTilePosition(String tileName) {
        return tilePositions.get(tileName);
    }

    // 获取所有板块位置信息
    public Map<String, int[]> getAllTilePositions() {
        return tilePositions;
    }

    // 检查是否包含特定板块
    public boolean containsTile(String tileName) {
        return tilePositions.containsKey(tileName);
    }

    // 清除所有位置信息
    public void clear() {
        tilePositions.clear();
    }

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