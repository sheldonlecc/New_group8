package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Engineer extends Role {
    public Engineer() {
        super("Engineer", "Can spend 1 action to shore up 2 tiles");
    }

    @Override
    public boolean canUseAbility() {
        return true; // Engineer's ability is permanent, no special conditions required
    }

    @Override
    public void useSpecialAbility() {
        // Engineer's ability is passive, no active use required
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
        return isTileMovable(tile); // Can move to any non-sunken tile
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }
}