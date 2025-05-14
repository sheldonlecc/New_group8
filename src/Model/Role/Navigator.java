package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Navigator extends Role {
    public Navigator() {
        super("领航员", "每个行动可以将其他玩家移动到最多2个相邻板块");
    }

    @Override
    public boolean canUseAbility() {
        return true;  // 领航员的能力是永久的，不需要特殊条件
    }

    @Override
    public void useSpecialAbility() {
        // 领航员的能力需要主动使用，具体实现在Controller层
    }

    @Override
    public List<Tile> getMovableTiles() {
        List<Tile> movableTiles = new ArrayList<>();
        Player player = getPlayer();
        if (player == null) return movableTiles;

        Tile currentTile = player.getCurrentTile();
        if (currentTile == null) return movableTiles;

        // 获取所有相邻瓦片
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();
        
        // 领航员只能移动到未被淹没的相邻瓦片
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