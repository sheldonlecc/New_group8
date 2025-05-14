package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileState;
import Model.Tile;

/**
 * 沙袋卡
 * 用于加固一个即将被淹没的岛屿板块
 * 使用后可以防止目标板块被淹没
 */
public class SandbagCard extends Card {
    private Tile targetTile;  // 目标板块

    /**
     * 创建沙袋卡
     */
    public SandbagCard() {
        super(CardType.SAND_BAG, "沙袋卡", "加固一个即将被淹没的岛屿板块");
    }

    /**
     * 获取目标板块
     * @return 目标板块
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * 检查卡牌是否可以在目标板块使用
     * @param targetTile 目标板块
     * @return 如果卡牌可以使用则返回true
     */
    public boolean canUse(Tile targetTile) {
        if (!super.canUse() || targetTile == null) {
            return false;
        }

        // 检查目标板块是否被淹没但未沉没
        TileState state = targetTile.getState();
        return state == TileState.FLOODED;
    }

    /**
     * 使用沙袋卡加固目标板块
     * @param targetTile 要加固的目标板块
     * @return 是否成功使用
     */
    public boolean useCard(Tile targetTile) {
        if (!canUse(targetTile)) {
            return false;
        }

        // 加固目标板块（将状态从FLOODED改为NORMAL）
        targetTile.setState(TileState.NORMAL);
        this.targetTile = targetTile;
        
        // 使用后禁用卡牌
        setUsable(false);
        return true;
    }

    /**
     * 重写equals方法
     * 两张沙袋卡如果目标板块相同则视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof SandbagCard)) return false;
        SandbagCard other = (SandbagCard) obj;
        return targetTile == null ? other.targetTile == null : 
               targetTile.equals(other.targetTile);
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + (targetTile != null ? targetTile.hashCode() : 0);
    }

    /**
     * 重写toString方法
     */
    @Override
    public String toString() {
        return String.format("%s - 目标板块: %s", super.toString(), 
            targetTile != null ? targetTile.getName().getDisplayName() : "未指定");
    }

    /**
     * 实现抽象方法use()
     * 这个方法由GameController调用，传入目标板块
     */
    @Override
    public void use() {
        // 由于需要目标板块参数，这个方法应该由GameController调用useCard(Tile targetTile)
        throw new UnsupportedOperationException(
            "请使用useCard(Tile targetTile)方法，该方法需要目标板块参数");
    }
}
//asdasdasdad
