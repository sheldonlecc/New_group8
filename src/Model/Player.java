// Model/Player/Player.java
package Model;

import Model.Cards.*;
import Model.Cards.HandCard.HandCardFullException;
import Model.Enumeration.PlayerState;
import Model.Role.Role;
import Model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Player Class
 * Manages core attributes and basic operations of players
 * Including: position, role, state, hand cards and other basic attributes
 * As well as basic access and modification operations for these attributes
 */
public class Player {
    // Core attributes
    private final HandCard handCard;           // Hand card manager
    private Tile currentTile;                  // Current position
    private Role role;                         // Role
    private PlayerState state;                 // Player state
    private boolean isRescued;                 // Whether rescued

    // Event listeners (for state change notifications)
    private final List<Consumer<Player>> onStateChangeListeners;    // State change listeners
    private final List<Consumer<Player>> onCardChangeListeners;     // Card change listeners

    /**
     * Constructor
     * Initialize player's basic attributes and event listeners
     */
    public Player() {
        this.handCard = new HandCard();
        this.state = PlayerState.NORMAL;
        this.onStateChangeListeners = new ArrayList<>();
        this.onCardChangeListeners = new ArrayList<>();
    }

    // =============== Basic Attribute Access ===============

    /**
     * Get player's current position
     * @return Current tile
     */
    public Tile getCurrentTile() {
        return currentTile;
    }

    /**
     * Set player's current position
     * @param tile Target position
     */
    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
    }

    /**
     * Get player's role
     * @return Role object
     */
    public Role getRole() {
        return role;
    }

    /**
     * Set player's role
     * @param role Role object
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Get player's current state
     * @return Player state
     */
    public PlayerState getState() {
        return state;
    }

    /**
     * Set player's state
     * @param newState New state
     */
    public void setState(PlayerState newState) {
        if (this.state != newState) {
            this.state = newState;
            notifyStateChangeListeners();
        }
    }

    /**
     * Mark player as rescued
     */
    public void rescue() {
        this.isRescued = true;
        setState(PlayerState.RESCUED);
    }

    /**
     * Check if player is rescued
     * @return Returns true if player is rescued
     */
    public boolean isRescued() {
        return isRescued;
    }

    // =============== Hand Card Management ===============

    /**
     * Add card to hand
     * @param card Card to add
     * @throws HandCardFullException Thrown when hand is full
     */
    public void addCard(Card card) throws HandCardFullException {
        handCard.addCard(card);
        notifyCardChangeListeners();
    }

    /**
     * Remove card from hand
     * @param card Card to remove
     */
    public void removeCard(Card card) {
        handCard.removeCard(card);
        notifyCardChangeListeners();
    }

    /**
     * Get hand card manager
     * @return Hand card manager
     */
    public HandCard getHandCard() {
        return handCard;
    }

    /**
     * Check if player has a specific type of card
     * @param cardType Card type
     * @return Returns true if player has the card
     */
    public boolean hasCardType(Class<? extends Card> cardType) {
        return handCard.getCards().stream()
                      .anyMatch(cardType::isInstance);
    }

    // =============== Event Listener Management ===============

    /**
     * Add state change listener
     * @param listener Listener
     */
    public void addOnStateChangeListener(Consumer<Player> listener) {
        onStateChangeListeners.add(listener);
    }

    /**
     * Add card change listener
     * @param listener Listener
     */
    public void addOnCardChangeListener(Consumer<Player> listener) {
        onCardChangeListeners.add(listener);
    }

    private void notifyStateChangeListeners() {
        onStateChangeListeners.forEach(listener -> listener.accept(this));
    }

    private void notifyCardChangeListeners() {
        onCardChangeListeners.forEach(listener -> listener.accept(this));
    }

    // =============== Data Validation ===============

    /**
     * Validate if player state is valid
     * @return Returns true if player state is valid
     */
    public boolean isValid() {
        return role != null &&
               currentTile != null &&
               handCard != null &&
               state != null;
    }
}