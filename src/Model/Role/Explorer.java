package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Explorer extends Role {
    public Explorer() {
        super("探险家", "可以斜向移动和加固板块");
    }

    @Override
    public boolean canUseAbility() {
        return true;  // 探险家的能力是永久的，不需要特殊条件
    }

    @Override
    public void useSpecialAbility() {
        // 探险家的能力是被动的，不需要主动使用
    }

    @Override
    public List<Tile> getMovableTiles() {
        List<Tile> movableTiles = new ArrayList<>();
        Player player = getPlayer();
        if (player == null) return movableTiles;

        Tile currentTile = player.getCurrentTile();
        if (currentTile == null) return movableTiles;

        // 获取所有相邻瓦片（包括斜向）
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();
        
        // 探险家可以移动到任何未被淹没的相邻瓦片（包括斜向）
        for (Tile tile : adjacentTiles) {
            if (tile.getState() == TileState.NORMAL) {
                movableTiles.add(tile);
            }
        }

        return movableTiles;
    }

    @Override
    public boolean canMoveTo(Tile tile) {
        if (tile == null) return false;
        return tile.getState() == TileState.NORMAL;  // 只能移动到未被淹没的瓦片
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }
}