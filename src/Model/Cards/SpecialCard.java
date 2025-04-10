// Model/Cards/SpecialCard.java
package Model.Cards;

import Model.Enumeration.CardType;

public abstract class SpecialCard extends Card {
    public SpecialCard(String name, String description) {
        super(CardType.SPECIAL, name, description);
    }
}