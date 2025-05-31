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

        // Get all adjacent tiles
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();

        // Diver can move to any adjacent tile, including flooded and missing ones
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
        return tile.getState() != TileState.SUNK; // Cannot move to sunken tiles
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }
}