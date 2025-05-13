// Model/Cards/HelicopterCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileType;
import Model.Enumeration.TileName;
import Model.Tile;
import Model.Player;
import java.util.List;

/**
 * 直升机救援卡
 * 可以将所有玩家带离岛屿
 * 使用条件：所有玩家必须在同一位置（直升机场）
 */
public class HelicopterCard extends Card {
    private final Tile targetTile;  // 目标位置（直升机场）

    /**
     * 创建直升机救援卡
     * @param targetTile 目标位置（直升机场）
     */
    public HelicopterCard(Tile targetTile) {
        super(CardType.HELICOPTER, "直升机救援卡", "将所有玩家带离岛屿");
        this.targetTile = targetTile;
    }

    /**
     * 获取目标位置
     * @return 目标位置（直升机场）
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * 检查卡牌是否可以使用
     * @param players 玩家列表
     * @return 如果所有玩家都在目标位置则返回true
     */
    public boolean canUse(List<Player> players) {
        if (!super.canUse() || targetTile == null || players == null || players.isEmpty()) {
            return false;
        }

        // 检查目标位置是否是直升机场
        if (targetTile.getTileName() != TileName.FOOLS_LANDING) {
            return false;
        }

        // 检查所有玩家是否都在目标位置
        return players.stream()
                     .allMatch(player -> player.getCurrentTile().equals(targetTile));
    }

    /**
     * 使用直升机救援卡
     * 将所有玩家带离岛屿
     * @param players 要救援的玩家列表
     * @return 是否成功使用
     */
    public boolean use(List<Player> players) {
        if (!canUse(players)) {
            return false;
        }

        // 将所有玩家标记为已获救
        players.forEach(Player::rescue);
        
        // 使用后禁用卡牌
        setUsable(false);
        return true;
    }

    /**
     * 重写equals方法
     * 两张直升机救援卡如果目标位置相同则视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof HelicopterCard)) return false;
        HelicopterCard other = (HelicopterCard) obj;
        return targetTile.equals(other.targetTile);
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + targetTile.hashCode();
    }

    /**
     * 重写toString方法
     */
    @Override
    public String toString() {
        return String.format("%s - 目标位置: %s", super.toString(), 
            targetTile != null ? targetTile.getTileName().getDisplayName() : "未指定");
    }

    /**
     * 实现抽象方法use()
     * 这个方法由GameController调用，传入玩家列表
     */
    @Override
    public void use() {
        // 由于需要玩家列表参数，这个方法应该由GameController调用use(List<Player> players)
        throw new UnsupportedOperationException(
            "请使用use(List<Player> players)方法，该方法需要玩家列表参数");
    }
}