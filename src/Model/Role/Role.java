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

    // 检查是否可以使用特殊能力
    public abstract boolean canUseAbility();

    // 使用特殊能力
    public abstract void useSpecialAbility();

    // 获取可移动的瓦片列表
    public abstract List<Tile> getMovableTiles();

    // 检查是否可以移动到指定瓦片
    public abstract boolean canMoveTo(Tile tile);

    // 检查是否可以加固指定瓦片
    public abstract boolean canShoreUp(Tile tile);

    // 检查瓦片是否可以移动（基础检查）
    protected boolean isTileMovable(Tile tile) {
        if (tile == null) {
            return false;
        }
        // 只检查是否沉没，被淹没的瓦片可以移动
        return tile.getState() != TileState.SUNK;
    }
}