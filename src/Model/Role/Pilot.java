package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Pilot extends Role {
    private boolean hasUsedAbility = false; // 记录是否已经使用过能力

    public Pilot() {
        super("飞行员", "每轮可以花费1个行动移动到任意板块");
    }

    @Override
    public boolean canUseAbility() {
        return !hasUsedAbility; // 每回合只能使用一次能力
    }

    @Override
    public void useSpecialAbility() {
        hasUsedAbility = true; // 标记已使用能力
    }

    @Override
    public List<Tile> getMovableTiles() {
        List<Tile> movableTiles = new ArrayList<>();
        Player player = getPlayer();
        if (player == null)
            return movableTiles;

        Tile currentTile = player.getCurrentTile();
        if (currentTile == null)
            return movableTiles;

        // 获取所有相邻瓦片
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();

        // 飞行员可以移动到任何未被淹没的相邻瓦片
        for (Tile tile : adjacentTiles) {
            if (tile.getState() == TileState.NORMAL) {
                movableTiles.add(tile);
            }
        }

        return movableTiles;
    }

    @Override
    public boolean canMoveTo(Tile tile) {
        return isTileMovable(tile); // 可以移动到任何未被沉没的瓦片
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }

    // 重置能力使用状态（在回合开始时调用）
    public void resetAbility() {
        hasUsedAbility = false;
    }
}