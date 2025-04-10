// Model/Cards/Card.java
package Model.Cards;

import Model.Enumeration.CardType;

public abstract class Card {
    private final CardType type;
    private final String name;
    private final String description;

    public Card(CardType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public abstract void use();

    public CardType getType() { return type; }
    public String getName() { return name; }
}