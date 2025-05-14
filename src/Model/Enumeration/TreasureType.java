package Model.Enumeration;

public enum TreasureType {
    EARTH("Earth"),      // 大地
    WIND("Wind"),       // 风息
    FIRE("Fire"),       // 火焰
    WATER("Water");      // 海洋

    private final String displayName;

    TreasureType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}