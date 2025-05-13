// Model/Cards/Card.java
package Model.Cards;

import Model.Enumeration.CardType;
import java.io.Serializable;
import java.util.UUID;

public abstract class Card implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    
    private final String id;           // 卡牌唯一标识
    private final CardType type;       // 卡牌类型
    private final String name;         // 卡牌名称
    private final String description;  // 卡牌描述
    private boolean isUsable;          // 卡牌是否可用

    public Card(CardType type, String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.name = name;
        this.description = description;
        this.isUsable = true;
    }

    // 抽象方法：使用卡牌
    public abstract void use();

    // 验证卡牌是否可以使用
    public boolean canUse() {
        return isUsable;
    }

    // 设置卡牌是否可用
    public void setUsable(boolean usable) {
        this.isUsable = usable;
    }

    // 获取卡牌ID
    public String getId() {
        return id;
    }

    // 获取卡牌类型
    public CardType getType() {
        return type;
    }

    // 获取卡牌名称
    public String getName() {
        return name;
    }

    // 获取卡牌描述
    public String getDescription() {
        return description;
    }

    // 克隆卡牌
    @Override
    public Card clone() {
        try {
            return (Card) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    // 重写equals方法
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return id.equals(card.id);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    // 重写toString方法
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", name, type, description);
    }
}