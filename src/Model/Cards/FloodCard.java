// Model/Cards/FloodCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.TileType;
import Model.Tile;
import Model.Enumeration.TileState;

/**
 * 洪水卡
 * 用于淹没指定的岛屿板块
 * 当板块已经被淹没时，会使其沉没
 */
public class FloodCard extends Card {
    private final Tile targetTile; // 目标地块

    /**
     * 创建一张洪水卡
     * 
     * @param targetTile 目标地块
     */
    public FloodCard(Tile targetTile) {
        super(CardType.FLOOD, "Flood", "淹没指定地块，如果地块已被淹没则使其沉没");
        this.targetTile = targetTile;
    }

    /**
     * 获取目标地块
     * 
     * @return 目标地块
     */
    public Tile getTargetTile() {
        return targetTile;
    }

    /**
     * 检查卡牌是否可以使用
     * 
     * @return 如果目标地块存在且未被沉没则返回true
     */
    @Override
    public boolean canUse() {
        return super.canUse() &&
                targetTile != null &&
                targetTile.getState() != TileState.SUNK;
    }

    /**
     * 使用洪水卡
     * 如果地块未被淹没，则将其淹没
     * 如果地块已被淹没，则使其沉没，并从洪水牌堆中移除对应卡牌
     * 
     * @param floodDeck 洪水牌堆（用于移除沉没板块的卡牌）
     * @return 使用是否成功
     */
    public void use(Model.Deck.FloodDeck floodDeck) {
        if (!canUse()) {
            return;
        }

        Tile tile = getTargetTile();
        TileState before = tile.getState();
        if (before == TileState.NORMAL) {
            tile.setState(TileState.FLOODED);
            System.out.println(
                    "[调试] 洪水卡作用于 " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "]，状态: 正常 -> 被淹没");
        } else if (before == TileState.FLOODED) {
            tile.setState(TileState.SUNK);
            System.out.println(
                    "[调试] 洪水卡作用于 " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "]，状态: 被淹没 -> 沉没");
            if (floodDeck != null) {
                floodDeck.removeCardForSunkTile(tile);
            }
        }
    }

    @Override
    public void use() {
        use(null);
    }

    /**
     * 重写equals方法
     * 两张洪水卡如果目标地块相同则视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof FloodCard))
            return false;
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
        return String.format("%s - 目标地块: %s", super.toString(), targetTile.getName());
    }
}