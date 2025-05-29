package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Diver extends Role {
    public Diver() {
        super("Diver", "Can spend 1 action to move through one or more adjacent missing and/or flooded tiles");
    }

    @Override
    public boolean canUseAbility() {
        return true; // Diver's ability is permanent, no special conditions required
    }

    @Override
    public void useSpecialAbility() {
        // Diver's ability is passive, no active use required
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

        // 潜水员可以移动到任何相邻的瓦片，包括被淹没的和缺失的
        for (Tile tile : adjacentTiles) {
            if (tile.getState() != TileState.SUNK) { // Cannot move to sunken tiles
                movableTiles.add(tile);
            }
        }

        return movableTiles;
    }

    @Override
    public boolean canMoveTo(Tile tile) {
        if (tile == null)
            return false;
        return tile.getState() != TileState.SUNK; // 不能移动到已沉没的瓦片
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }
}