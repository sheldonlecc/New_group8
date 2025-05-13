// Model/Cards/TreasureCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TreasureType;
import Model.Tile;
import Model.Player;
import java.util.List;

/**
 * 宝藏卡
 * 用于收集对应的宝藏
 * 需要4张相同类型的宝藏卡才能收集对应的宝藏
 */
public class TreasureCard extends Card {
    private final TreasureType treasureType;  // 宝藏类型
    private Tile targetTile;  // 目标宝藏位置

    /**
     * 创建宝藏卡
     * @param treasureType 宝藏类型
     */
    public TreasureCard(TreasureType treasureType) {
        super(CardType.TREASURE, 
              String.format("%s宝藏卡", treasureType.getDisplayName()),
              String.format("用于收集%s", treasureType.getDisplayName()));
        this.treasureType = treasureType;
    }

    /**
     * 获取宝藏类型
     * @return 宝藏类型
     */
    public TreasureType getTreasureType() {
        return treasureType;
    }

    /**
     * 获取目标宝藏位置
     * @return 目标宝藏位置
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * 检查卡牌是否可以使用
     * @param players 玩家列表
     * @param targetTile 目标宝藏位置
     * @return 如果卡牌可以使用则返回true
     */
    public boolean canUse(List<Player> players, Tile targetTile) {
        if (!super.canUse() || players == null || targetTile == null) {
            return false;
        }

        // 检查是否有玩家在目标位置
        boolean hasPlayerAtTarget = players.stream()
                                         .anyMatch(player -> player.getCurrentTile().equals(targetTile));
        
        // 检查目标位置是否是对应的宝藏位置
        // 注意：具体的宝藏位置检查逻辑应该在GameController中实现
        // 这里只检查基本条件
        return hasPlayerAtTarget;
    }

    /**
     * 使用宝藏卡
     * @param players 玩家列表
     * @param targetTile 目标宝藏位置
     * @return 是否成功使用
     */
    public boolean use(List<Player> players, Tile targetTile) {
        if (!canUse(players, targetTile)) {
            return false;
        }

        // 记录目标位置
        this.targetTile = targetTile;
        
        // 使用后禁用卡牌
        setUsable(false);
        return true;
    }

    /**
     * 重写equals方法
     * 两张宝藏卡如果宝藏类型相同则视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof TreasureCard)) return false;
        TreasureCard other = (TreasureCard) obj;
        return treasureType == other.treasureType;
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + treasureType.hashCode();
    }

    /**
     * 重写toString方法
     */
    @Override
    public String toString() {
        return String.format("%s - 宝藏类型: %s", super.toString(), 
            treasureType.getDisplayName());
    }

    /**
     * 实现抽象方法use()
     * 这个方法由GameController调用，传入玩家列表和目标位置
     */
    @Override
    public void use() {
        // 由于需要玩家列表和目标位置参数，这个方法应该由GameController调用use(List<Player> players, Tile targetTile)
        throw new UnsupportedOperationException(
            "请使用use(List<Player> players, Tile targetTile)方法，该方法需要玩家列表和目标位置参数");
    }
}