package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Explorer extends Role {
    public Explorer() {
        super("Explorer", "Can move and shore up diagonally");
    }

    @Override
    public boolean canUseAbility() {
        return true; // Explorer's ability is permanent, no special conditions required
    }

    @Override
    public void useSpecialAbility() {
        // Explorer's ability is passive, no active use required
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
        return isTileMovable(tile); // Can move to any non-sunken tile
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }
}