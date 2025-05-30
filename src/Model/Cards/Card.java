// Model/Cards/Card.java
package Model.Cards;

import Model.Enumeration.CardType;
import java.io.Serializable;
import java.util.UUID;

/**
 * Card Class
 * Abstract base class for all card types in the game
 * Defines basic card properties and behaviors
 */
public abstract class Card implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    
    private final String id;           // Unique identifier for the card
    private final CardType type;       // Card type
    private final String name;         // Card name
    private final String description;  // Card description
    private boolean isUsable;          // Whether the card is usable

    /**
     * Constructor
     * @param type Card type
     * @param name Card name
     * @param description Card description
     */
    public Card(CardType type, String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.name = name;
        this.description = description;
        this.isUsable = true;
    }

    /**
     * Abstract method: Use the card
     * Must be implemented by subclasses
     */
    public abstract void use();

    /**
     * Check if the card can be used
     * @return true if the card is usable
     */
    public boolean canUse() {
        return isUsable;
    }

    /**
     * Set whether the card is usable
     * @param usable Whether the card is usable
     */
    public void setUsable(boolean usable) {
        this.isUsable = usable;
    }

    /**
     * Get card ID
     * @return Card's unique identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Get card type
     * @return Card type
     */
    public CardType getType() {
        return type;
    }

    /**
     * Get card name
     * @return Card name
     */
    public String getName() {
        return name;
    }

    /**
     * Get card description
     * @return Card description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Clone the card
     * @return A clone of the card
     */
    @Override
    public Card clone() {
        try {
            return (Card) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Override equals method
     * Two cards are equal if they have the same ID
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return id.equals(card.id);
    }

    /**
     * Override hashCode method
     * @return Hash code based on card ID
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Override toString method
     * @return String representation of the card
     */
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", name, type, description);
    }
}