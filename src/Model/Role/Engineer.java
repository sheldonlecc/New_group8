package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Engineer extends Role {
    public Engineer() {
        super("工程师", "花费1个行动可以加固2个板块");
    }

    @Override
    public boolean canUseAbility() {
        return true; // 工程师的能力是永久的，不需要特殊条件
    }

    @Override
    public void useSpecialAbility() {
        // 工程师的能力是被动的，不需要主动使用
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

        // 工程师只能移动到未被淹没的相邻瓦片
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
}