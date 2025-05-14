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
 * 玩家类
 * 管理玩家的核心属性和基本操作
 * 包括：位置、角色、状态、手牌等基本属性
 * 以及对这些属性的基本访问和修改操作
 */
public class Player {
    // 核心属性
    private final HandCard handCard;           // 手牌管理器
    private Tile currentTile;                  // 当前位置
    private Role role;                         // 角色
    private PlayerState state;                 // 玩家状态
    private boolean isRescued;                 // 是否已获救

    // 事件监听器（用于通知状态变化）
    private final List<Consumer<Player>> onStateChangeListeners;    // 状态变化监听器
    private final List<Consumer<Player>> onCardChangeListeners;     // 卡牌变化监听器

    /**
     * 构造函数
     * 初始化玩家基本属性
     */
    public Player() {
        this.handCard = new HandCard();
        this.state = PlayerState.NORMAL;
        this.onStateChangeListeners = new ArrayList<>();
        this.onCardChangeListeners = new ArrayList<>();
    }

    // =============== 基本属性访问 ===============

    /**
     * 获取玩家当前位置
     * @return 当前瓦片
     */
    public Tile getCurrentTile() {
        return currentTile;
    }

    /**
     * 设置玩家当前位置
     * @param tile 目标位置
     */
    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
    }

    /**
     * 获取玩家角色
     * @return 角色对象
     */
    public Role getRole() {
        return role;
    }

    /**
     * 设置玩家角色
     * @param role 角色对象
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * 获取玩家当前状态
     * @return 玩家状态
     */
    public PlayerState getState() {
        return state;
    }

    /**
     * 设置玩家状态
     * @param newState 新状态
     */
    public void setState(PlayerState newState) {
        if (this.state != newState) {
            this.state = newState;
            notifyStateChangeListeners();
        }
    }

    /**
     * 标记玩家为已获救状态
     */
    public void rescue() {
        this.isRescued = true;
        setState(PlayerState.RESCUED);
    }

    /**
     * 检查玩家是否已获救
     * @return 如果玩家已获救则返回true
     */
    public boolean isRescued() {
        return isRescued;
    }

    // =============== 手牌管理 ===============

    /**
     * 添加卡牌到手牌
     * @param card 要添加的卡牌
     * @throws HandCardFullException 当手牌已满时抛出
     */
    public void addCard(Card card) throws HandCardFullException {
        handCard.addCard(card);
        notifyCardChangeListeners();
    }

    /**
     * 从手牌移除卡牌
     * @param card 要移除的卡牌
     */
    public void removeCard(Card card) {
        handCard.removeCard(card);
        notifyCardChangeListeners();
    }

    /**
     * 获取手牌管理器
     * @return 手牌管理器
     */
    public HandCard getHandCard() {
        return handCard;
    }

    /**
     * 检查是否有指定类型的卡牌
     * @param cardType 卡牌类型
     * @return 如果有则返回true
     */
    public boolean hasCardType(Class<? extends Card> cardType) {
        return handCard.getCards().stream()
                      .anyMatch(cardType::isInstance);
    }

    // =============== 事件监听器管理 ===============

    /**
     * 添加状态变化监听器
     * @param listener 监听器
     */
    public void addOnStateChangeListener(Consumer<Player> listener) {
        onStateChangeListeners.add(listener);
    }

    /**
     * 添加卡牌变化监听器
     * @param listener 监听器
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

    // =============== 数据验证 ===============

    /**
     * 验证玩家状态是否有效
     * @return 如果玩家状态有效则返回true
     */
    public boolean isValid() {
        return role != null &&
               currentTile != null &&
               handCard != null &&
               state != null;
    }
}