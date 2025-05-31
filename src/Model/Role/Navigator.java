package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Navigator extends Role {
    public Navigator() {
        super("Navigator", "Can move other players up to 2 adjacent tiles per action");
    }

    @Override
    public boolean canUseAbility() {
        return true; // Navigator's ability is permanent, no special conditions required
    }

    @Override
    public void useSpecialAbility() {
        // Navigator's ability requires active use, specific implementation in Controller layer
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

        // Get all adjacent tiles
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();

        // Navigator can only move to unflooded adjacent tiles
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