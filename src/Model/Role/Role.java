package Model.Role;

import Model.Player;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.List;

public abstract class Role {
    private String name;
    private String ability;
    private Player player;

    public Role(String name, String ability) {
        this.name = name;
        this.ability = ability;
    }

    public String getName() {
        return name;
    }

    public String getAbility() {
        return ability;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    // Check if special ability can be used
    public abstract boolean canUseAbility();

    // Use special ability
    public abstract void useSpecialAbility();

    // Get list of movable tiles
    public abstract List<Tile> getMovableTiles();

    // Check if can move to specified tile
    public abstract boolean canMoveTo(Tile tile);

    // Check if can shore up specified tile
    public abstract boolean canShoreUp(Tile tile);

    // Check if tile is movable (basic check)
    protected boolean isTileMovable(Tile tile) {
        if (tile == null) {
            return false;
        }
        // Only check if sunk, flooded tiles can be moved to
        return tile.getState() != TileState.SUNK;
    }
}