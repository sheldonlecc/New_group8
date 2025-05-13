package Model.Enumeration;

/**
 * 水位等级枚举
 * 定义了游戏中的水位等级
 * 水位等级会影响抽牌数量和板块淹没速度
 */
public enum WaterLevel {
    LEVEL_1("水位1", 2),  // 初始水位，每回合抽2张洪水卡
    LEVEL_2("水位2", 3),  // 每回合抽3张洪水卡
    LEVEL_3("水位3", 4),  // 每回合抽4张洪水卡
    LEVEL_4("水位4", 5),  // 每回合抽5张洪水卡
    LEVEL_5("水位5", 6);  // 最高水位，每回合抽6张洪水卡

    private final String displayName;  // 显示名称
    private final int floodCardCount;  // 每回合抽牌数量

    WaterLevel(String displayName, int floodCardCount) {
        this.displayName = displayName;
        this.floodCardCount = floodCardCount;
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取每回合抽牌数量
     * @return 每回合抽牌数量
     */
    public int getFloodCardCount() {
        return floodCardCount;
    }

    /**
     * 获取下一个水位等级
     * @return 下一个水位等级，如果已经是最高等级则返回null
     */
    public WaterLevel getNextLevel() {
        switch (this) {
            case LEVEL_1:
                return LEVEL_2;
            case LEVEL_2:
                return LEVEL_3;
            case LEVEL_3:
                return LEVEL_4;
            case LEVEL_4:
                return LEVEL_5;
            default:
                return null;
        }
    }
} 