package Model.Enumeration;

/**
 * 水位状态枚举
 * 定义水位可能的状态
 */
public enum WaterLevelState {
    NORMAL,     // 正常状态
    DANGEROUS,  // 危险状态（水位等级 >= 7）
    CRITICAL    // 临界状态（水位等级达到最高）
} 