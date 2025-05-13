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
    private boolean isRescued = false;  // 是否已获救

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

    /**
     * 标记玩家为已获救状态
     */
    public void rescue() {
        this.isRescued = true;
    }

    /**
     * 检查玩家是否已获救
     * @return 如果玩家已获救则返回true
     */
    public boolean isRescued() {
        return isRescued;
    }
}