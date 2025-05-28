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
 * 功能1：可以将所有玩家带离岛屿（胜利条件）
 * 功能2：可以将任意数量的玩家从一个板块移动到另一个任意板块
 * 使用条件：
 * 1. 胜利条件：所有玩家必须在同一位置（直升机场）
 * 2. 移动功能：所有被移动的玩家必须在同一个板块上
 */
public class HelicopterCard extends Card {
    private final Tile targetTile;  // 目标位置（直升机场）

    /**
     * 创建直升机救援卡
     * @param targetTile 目标位置（直升机场）
     */
    public HelicopterCard(Tile targetTile) {
        super(CardType.HELICOPTER, "Helicopter", "将所有玩家带离岛屿或移动到任意板块");
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
     * 检查卡牌是否可以使用（胜利条件）
     * @param players 玩家列表
     * @return 如果所有玩家都在目标位置则返回true
     */
    public boolean canUseForVictory(List<Player> players) {
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
     * 检查卡牌是否可以使用（移动功能）
     * @param players 要移动的玩家列表
     * @return 如果所有玩家都在同一个板块则返回true
     */
    public boolean canUseForMovement(List<Player> players) {
        if (!super.canUse() || players == null || players.isEmpty()) {
            return false;
        }

        // 检查所有玩家是否都在同一个板块
        Tile firstPlayerTile = players.get(0).getCurrentTile();
        return players.stream()
                     .allMatch(player -> player.getCurrentTile().equals(firstPlayerTile));
    }

    /**
     * 使用直升机救援卡（胜利条件）
     * 将所有玩家带离岛屿
     * @param players 要救援的玩家列表
     * @return 是否成功使用
     */
    public boolean useForVictory(List<Player> players) {
        if (!canUseForVictory(players)) {
            return false;
        }

        // 将所有玩家标记为已获救
        players.forEach(Player::rescue);
        
        // 使用后禁用卡牌
        setUsable(false);
        return true;
    }

    /**
     * 使用直升机救援卡（移动功能）
     * 将玩家移动到目标板块
     * @param players 要移动的玩家列表
     * @param targetTile 目标板块
     * @return 是否成功使用
     */
    public boolean useForMovement(List<Player> players, Tile targetTile) {
        if (!canUseForMovement(players) || targetTile == null) {
            return false;
        }

        // 将所有玩家移动到目标板块
        players.forEach(player -> player.setCurrentTile(targetTile));
        
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
        // 由于需要玩家列表参数，这个方法应该由GameController调用useForVictory或useForMovement
        throw new UnsupportedOperationException(
            "请使用useForVictory(List<Player> players)或useForMovement(List<Player> players, Tile targetTile)方法");
    }
}