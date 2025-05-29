// Model/WaterLevel/WaterLevel.java
package Model;

import View.WaterLevelView;
import Model.Enumeration.WaterLevelState;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

/**
 * 水位类
 * 管理游戏的水位系统
 * 包括：水位等级、抽牌数量、水位变化等
 */
public class WaterLevel {
    // 水位等级常量
    public static final int MIN_LEVEL = 1;     // 最低水位等级
    public static final int MAX_LEVEL = 10;    // 最高水位等级
    public static final int START_LEVEL = 1;   // 初始水位等级（保持不变，通过难度设置覆盖）

    // 单例实例
    private static WaterLevel instance;
    private static WaterLevelView waterLevelView;

    // 核心属性
    private int currentLevel;                  // 当前水位等级
    private WaterLevelState state;             // 水位状态
    private final List<Consumer<WaterLevel>> onLevelChangeListeners;    // 水位变化监听器

    /**
     * 私有构造函数
     * 初始化水位系统
     */
    private WaterLevel() {
        this.currentLevel = START_LEVEL;
        this.state = WaterLevelState.NORMAL;
        this.onLevelChangeListeners = new ArrayList<>();
    }

    /**
     * 获取WaterLevel单例实例
     * @return WaterLevel实例
     */
    public static WaterLevel getInstance() {
        if (instance == null) {
            instance = new WaterLevel();
        }
        return instance;
    }

    /**
     * 设置水位视图
     * @param view 水位视图
     */
    public static void setWaterLevelView(WaterLevelView view) {
        waterLevelView = view;
    }

    // =============== 水位管理 ===============

    /**
     * 获取当前水位等级
     * @return 当前水位等级
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 获取当前水位状态
     * @return 水位状态
     */
    public WaterLevelState getState() {
        return state;
    }

    /**
     * 获取当前水位对应的抽牌数量
     * @return 抽牌数量
     */
    public int getDrawCount() {
        // 根据水位等级返回对应的抽牌数量
        if (currentLevel <= 2) return 2;
        if (currentLevel <= 5) return 3;
        if (currentLevel <= 7) return 4;
        return 5;
    }

    /**
     * 提升水位等级
     * @return 是否成功提升
     */
    public boolean increaseLevel() {
        if (currentLevel >= MAX_LEVEL) {
            return false;
        }
        currentLevel++;
        updateState();
        notifyLevelChangeListeners();
        if (waterLevelView != null) {
            waterLevelView.updateWaterLevel(currentLevel);
        }
        return true;
    }

    /**
     * 降低水位等级
     * @return 是否成功降低
     */
    public boolean decreaseLevel() {
        if (currentLevel <= MIN_LEVEL) {
            return false;
        }
        currentLevel--;
        updateState();
        notifyLevelChangeListeners();
        if (waterLevelView != null) {
            waterLevelView.updateWaterLevel(currentLevel);
        }
        return true;
    }

    /**
     * 重置水位等级
     */
    public void resetLevel() {
        currentLevel = START_LEVEL;
        updateState();
        notifyLevelChangeListeners();
        if (waterLevelView != null) {
            waterLevelView.updateWaterLevel(currentLevel);
        }
    }

    // =============== 状态管理 ===============

    /**
     * 更新水位状态
     * 根据当前水位等级更新状态
     */
    private void updateState() {
        if (currentLevel >= MAX_LEVEL) {
            state = WaterLevelState.CRITICAL;
        } else if (currentLevel >= 7) {
            state = WaterLevelState.DANGEROUS;
        } else {
            state = WaterLevelState.NORMAL;
        }
    }

    // =============== 事件监听器管理 ===============

    /**
     * 添加水位变化监听器
     * @param listener 监听器
     */
    public void addOnLevelChangeListener(Consumer<WaterLevel> listener) {
        onLevelChangeListeners.add(listener);
    }

    private void notifyLevelChangeListeners() {
        onLevelChangeListeners.forEach(listener -> listener.accept(this));
    }

    // =============== 数据验证 ===============

    /**
     * 验证水位状态是否有效
     * @return 如果水位状态有效则返回true
     */
    public boolean isValid() {
        return currentLevel >= MIN_LEVEL && 
               currentLevel <= MAX_LEVEL && 
               state != null;
    }

    @Override
    public String toString() {
        return String.format("水位等级: %d, 状态: %s, 抽牌数量: %d", 
            currentLevel, state, getDrawCount());
    }

    /**
     * 设置当前水位等级（用于初始化）
     * @param level 水位等级
     */
    public void setCurrentLevel(int level) {
        if (level >= MIN_LEVEL && level <= MAX_LEVEL) {
            this.currentLevel = level;
            updateState();
            notifyLevelChangeListeners();
            if (waterLevelView != null) {
                waterLevelView.updateWaterLevel(currentLevel);
            }
        }
    }
}