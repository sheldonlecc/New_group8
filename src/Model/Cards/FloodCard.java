// Model/Cards/FloodCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileType;
import Model.Tile;

/**
 * 洪水卡
 * 用于淹没指定的岛屿板块
 * 当板块已经被淹没时，会使其沉没
 */
public class FloodCard extends Card {
    private final Tile targetTile;  // 目标地块

    /**
     * 创建一张洪水卡
     * @param targetTile 目标地块
     */
    public FloodCard(Tile targetTile) {
        super(CardType.FLOOD, "洪水卡", "淹没指定地块，如果地块已被淹没则使其沉没");
        this.targetTile = targetTile;
    }

    /**
     * 获取目标地块
     * @return 目标地块
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * 检查卡牌是否可以使用
     * @return 如果目标地块存在且未被沉没则返回true
     */
    @Override
    public boolean canUse() {
        return super.canUse() && 
               targetTile != null && 
               targetTile.getState() != TileType.SUNKEN;
    }

    /**
     * 使用洪水卡
     * 如果地块未被淹没，则将其淹没
     * 如果地块已被淹没，则使其沉没
     * @return 使用是否成功
     */
    @Override
    public void use() {
        if (!canUse()) {
            return;
        }

        // 根据地块当前状态执行相应操作
        switch (targetTile.getState()) {
            case DRY:
                // 如果地块是干燥的，将其淹没
                targetTile.flood();
                break;
            case FLOODED:
                // 如果地块已被淹没，使其沉没
                targetTile.flood();  // 再次调用flood()使其沉没
                break;
            default:
                // 如果地块已经沉没，不做任何操作
                break;
        }

        // 使用后禁用卡牌
        setUsable(false);
    }

    /**
     * 重写equals方法
     * 两张洪水卡如果目标地块相同则视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof FloodCard)) return false;
        FloodCard other = (FloodCard) obj;
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
        return String.format("%s - 目标地块: %s", super.toString(), targetTile.getTileName());
    }
}