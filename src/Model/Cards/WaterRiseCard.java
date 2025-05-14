// Model/Cards/WaterRiseCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import Model.Enumeration.WaterLevel;

/**
 * 水位上升卡
 * 用于提高水位等级
 * 每次使用会提高水位等级，影响抽牌数量和板块淹没速度
 */
public class WaterRiseCard extends Card {
    private WaterLevel currentWaterLevel;  // 当前水位等级

    /**
     * 创建水位上升卡
     */
    public WaterRiseCard() {
        super(CardType.WATER_RISE, "WaterRise", "提高水位等级，增加游戏难度");
    }

    /**
     * 获取当前水位等级
     * @return 当前水位等级
     */
    public WaterLevel getCurrentWaterLevel() {
        return currentWaterLevel;
    }

    /**
     * 设置当前水位等级
     * @param waterLevel 新的水位等级
     */
    public void setCurrentWaterLevel(WaterLevel waterLevel) {
        this.currentWaterLevel = waterLevel;
    }

    /**
     * 检查卡牌是否可以使用
     * @return 如果卡牌可以使用则返回true
     */
    public boolean canUse() {
        if (!super.canUse() || currentWaterLevel == null) {
            return false;
        }

        // 检查水位是否已经达到最高等级
        return currentWaterLevel.getNextLevel() != null;
    }

    /**
     * 使用水位上升卡
     * @return 是否成功使用
     */
    public boolean useCard() {
        if (!canUse()) {
            return false;
        }

        // 提高水位等级
        WaterLevel nextLevel = currentWaterLevel.getNextLevel();
        if (nextLevel == null) {
            return false;
        }
        
        currentWaterLevel = nextLevel;
        
        // 使用后禁用卡牌
        setUsable(false);
        return true;
    }

    /**
     * 重写equals方法
     * 两张水位上升卡如果当前水位等级相同则视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof WaterRiseCard)) return false;
        WaterRiseCard other = (WaterRiseCard) obj;
        return currentWaterLevel == other.currentWaterLevel;
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + (currentWaterLevel != null ? currentWaterLevel.hashCode() : 0);
    }

    /**
     * 重写toString方法
     */
    @Override
    public String toString() {
        return String.format("%s - 当前水位: %s", super.toString(), 
            currentWaterLevel != null ? currentWaterLevel.getDisplayName() : "未设置");
    }

    /**
     * 实现抽象方法use()
     * 这个方法由GameController调用
     */
    @Override
    public void use() {
        if (!useCard()) {
            throw new IllegalStateException("无法使用水位上升卡");
        }
    }
}