package Model.Enumeration;

public enum TreasureType {
    EARTH("大地宝石"),      // 大地
    WIND("风之雕像"),       // 风息
    FIRE("火焰水晶"),       // 火焰
    WATER("海洋圣杯");      // 海洋

    private final String displayName;

    TreasureType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}