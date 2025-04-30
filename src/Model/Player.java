// Model/Player/Player.java
package Model;

import Model.Cards.Card;
import Model.Cards.HandCard;
import Model.Cards.HandCard.HandCardFullException;
import Model.Role.Role;
import Model.Tile;

public class Player {
    private final HandCard handCard = new HandCard();
    private Tile currentTile;
    private Role role;

    public void addCard(Card card) {
        try {
            handCard.addCard(card);
        } catch (HandCardFullException e) {
            System.err.println(e.getMessage());
            // 触发弃牌事件（由Controller处理）
        }
    }

    public void removeCard(Card card) {
        handCard.removeCard(card);
    }

    public HandCard getHandCard() {
        return handCard;
    }

    public void moveTo(Tile tile) {
        currentTile = tile;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}