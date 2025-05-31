package Model.Enumeration;

/**
 * Player State Enumeration
 * Defines possible states for players
 */
public enum PlayerState {
    /**
     * Normal State
     * Player can perform all actions normally
     */
    NORMAL,

    /**
     * Drowned State
     * Player's current tile is flooded, must immediately move to an adjacent unflooded tile
     * If unable to move, player dies
     */
    DROWNED,

    /**
     * Rescued State
     * Player has been rescued by helicopter to the helipad
     * Rescued players no longer participate in the game
     */
    RESCUED,

    /**
     * Dead State
     * Player has died due to drowning or other reasons
     * Dead players no longer participate in the game
     */
    DEAD,

    /**
     * Restricted State
     * Player's actions are limited due to special card effects or other reasons
     * Example: Movement restricted by sandbag card
     */
    RESTRICTED
} 