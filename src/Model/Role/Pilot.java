package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.ArrayList;
import java.util.List;

public class Pilot extends Role {
    private boolean hasUsedAbility = false; // Track if ability has been used

    public Pilot() {
        super("Pilot", "Can spend 1 action to move to any tile per turn");
    }

    @Override
    public boolean canUseAbility() {
        return !hasUsedAbility; // Can only use ability once per turn
    }

    @Override
    public void useSpecialAbility() {
        hasUsedAbility = true; // Mark ability as used
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
        return isTileMovable(tile); // Can move to any non-sunken tile
    }

    @Override
    public boolean canShoreUp(Tile tile) {
        return tile != null && tile.getState() == TileState.FLOODED;
    }

    // Reset ability usage status (called at turn start)
    public void resetAbility() {
        hasUsedAbility = false;
    }
}