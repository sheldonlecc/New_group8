package Model.Enumeration;

/**
 * Water Level State Enumeration
 * Defines possible states for water level
 */
public enum WaterLevelState {
    NORMAL,     // Normal state
    DANGEROUS,  // Dangerous state (water level >= 7)
    CRITICAL    // Critical state (water level at maximum)
} 