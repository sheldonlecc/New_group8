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

        // Get all adjacent tiles (including diagonal)
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();

        // Explorer can move to any unflooded adjacent tile (including diagonal)
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