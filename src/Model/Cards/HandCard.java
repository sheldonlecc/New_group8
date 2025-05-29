// Model/Cards/HandCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 手牌管理类
 * 负责管理玩家的手牌集合，包括添加、移除、查看等操作
 * 同时负责控制手牌数量上限和手牌状态
 */
public class HandCard implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Card> cards; // 手牌列表
    private static final int MAX_CARDS = 5; // 手牌上限（规则书规定）
    private static final int INITIAL_CARDS = 2; // 初始手牌数量
    private boolean enforceLimit = true; // 是否强制执行手牌上限

    // 事件监听器列表
    private final List<Consumer<Card>> onCardAddedListeners = new ArrayList<>();
    private final List<Consumer<Card>> onCardRemovedListeners = new ArrayList<>();
    private final List<Consumer<HandCard>> onHandCardChangedListeners = new ArrayList<>();

    /**
     * 创建手牌管理器
     */
    public HandCard() {
        this.cards = new ArrayList<>();
    }

    /**
     * 设置是否强制执行手牌上限
     * 
     * @param enforce 是否强制执行
     */
    public void setEnforceLimit(boolean enforce) {
        this.enforceLimit = enforce;
    }

    /**
     * 添加卡牌添加事件监听器
     * 
     * @param listener 监听器
     */
    public void addOnCardAddedListener(Consumer<Card> listener) {
        onCardAddedListeners.add(listener);
    }

    /**
     * 添加卡牌移除事件监听器
     * 
     * @param listener 监听器
     */
    public void addOnCardRemovedListener(Consumer<Card> listener) {
        onCardRemovedListeners.add(listener);
    }

    /**
     * 添加手牌变化事件监听器
     * 
     * @param listener 监听器
     */
    public void addOnHandCardChangedListener(Consumer<HandCard> listener) {
        onHandCardChangedListeners.add(listener);
    }

    /**
     * 添加卡牌到手牌，不检查手牌上限
     * 
     * @param card 要添加的卡牌
     */
    public void addCardWithoutCheck(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("卡牌不能为空");
        }
        cards.add(card);
        notifyCardAdded(card);
        notifyHandCardChanged();
    }

    /**
     * 添加卡牌到手牌
     * 
     * @param card 要添加的卡牌
     * @throws HandCardFullException 当手牌已满且强制执行上限时抛出
     */
    public void addCard(Card card) throws HandCardFullException {
        if (card == null) {
            throw new IllegalArgumentException("卡牌不能为空");
        }
        if (enforceLimit && cards.size() >= MAX_CARDS) {
            throw new HandCardFullException("手牌已满，最多持有 " + MAX_CARDS + " 张卡牌");
        }
        cards.add(card);
        notifyCardAdded(card);
        notifyHandCardChanged();
    }

    /**
     * 批量添加卡牌
     * 
     * @param cardsToAdd 要添加的卡牌列表
     * @throws HandCardFullException 当添加后手牌数量超过上限且强制执行上限时抛出
     */
    public void addCards(List<Card> cardsToAdd) throws HandCardFullException {
        if (cardsToAdd == null) {
            throw new IllegalArgumentException("卡牌列表不能为空");
        }
        if (enforceLimit && cards.size() + cardsToAdd.size() > MAX_CARDS) {
            throw new HandCardFullException("添加后手牌数量将超过上限 " + MAX_CARDS);
        }
        for (Card card : cardsToAdd) {
            addCard(card);
        }
    }

    /**
     * 从手牌中移除卡牌
     * 
     * @param card 要移除的卡牌
     * @return 是否成功移除
     */
    public boolean removeCard(Card card) {
        if (card == null) {
            return false;
        }
        boolean removed = cards.remove(card);
        if (removed) {
            notifyCardRemoved(card);
            notifyHandCardChanged();
        }
        return removed;
    }

    /**
     * 批量移除卡牌
     * 
     * @param cardsToRemove 要移除的卡牌列表
     * @return 成功移除的卡牌数量
     */
    public int removeCards(List<Card> cardsToRemove) {
        if (cardsToRemove == null) {
            return 0;
        }
        int removedCount = 0;
        for (Card card : cardsToRemove) {
            if (removeCard(card)) {
                removedCount++;
            }
        }
        return removedCount;
    }

    /**
     * 获取手牌列表的副本
     * 
     * @return 手牌列表的副本
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * 获取指定类型的卡牌列表
     * 
     * @param type 卡牌类型
     * @return 指定类型的卡牌列表
     */
    public List<Card> getCardsByType(CardType type) {
        return cards.stream()
                .filter(card -> card.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的卡牌类型
     * 
     * @return 当前手牌中存在的卡牌类型集合
     */
    public Set<CardType> getAvailableCardTypes() {
        return cards.stream()
                .map(Card::getType)
                .collect(Collectors.toSet());
    }

    /**
     * 检查是否包含指定类型的卡牌
     * 
     * @param type 要检查的卡牌类型
     * @return 如果包含则返回true
     */
    public boolean hasCardType(CardType type) {
        return cards.stream()
                .anyMatch(card -> card.getType() == type);
    }

    /**
     * 获取指定类型卡牌的数量
     * 
     * @param type 卡牌类型
     * @return 该类型卡牌的数量
     */
    public int getCardTypeCount(CardType type) {
        return (int) cards.stream()
                .filter(card -> card.getType() == type)
                .count();
    }

    /**
     * 获取手牌数量
     * 
     * @return 当前手牌数量
     */
    public int getCardCount() {
        return cards.size();
    }

    /**
     * 检查手牌是否已满
     * 
     * @return 如果手牌数量达到上限则返回true
     */
    public boolean isFull() {
        return enforceLimit && cards.size() >= MAX_CARDS;
    }

    /**
     * 获取手牌中各种类型卡牌的数量统计
     * 
     * @return 卡牌类型及其数量的映射
     */
    public Map<CardType, Integer> getCardTypeCount() {
        return cards.stream()
                .collect(Collectors.groupingBy(
                        Card::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    /**
     * 清空手牌
     */
    public void clear() {
        List<Card> removedCards = new ArrayList<>(cards);
        cards.clear();
        for (Card card : removedCards) {
            notifyCardRemoved(card);
        }
        notifyHandCardChanged();
    }

    /**
     * 检查是否包含指定卡牌
     * 
     * @param card 要检查的卡牌
     * @return 如果包含则返回true
     */
    public boolean contains(Card card) {
        return cards.contains(card);
    }

    /**
     * 获取初始手牌数量
     * 
     * @return 初始手牌数量
     */
    public static int getInitialCardCount() {
        return INITIAL_CARDS;
    }

    /**
     * 获取手牌上限
     * 
     * @return 手牌上限数量
     */
    public static int getMaxCards() {
        return MAX_CARDS;
    }

    /**
     * 验证手牌是否合法
     * 
     * @return 如果手牌合法则返回true
     */
    public boolean isValid() {
        return !enforceLimit || cards.size() <= MAX_CARDS;
    }

    /**
     * 手牌已满异常
     * 当尝试添加卡牌但手牌已满时抛出
     */
    public static class HandCardFullException extends Exception {
        public HandCardFullException(String message) {
            super(message);
        }
    }

    // 私有方法：通知卡牌添加事件
    private void notifyCardAdded(Card card) {
        for (Consumer<Card> listener : onCardAddedListeners) {
            listener.accept(card);
        }
    }

    // 私有方法：通知卡牌移除事件
    private void notifyCardRemoved(Card card) {
        for (Consumer<Card> listener : onCardRemovedListeners) {
            listener.accept(card);
        }
    }

    // 私有方法：通知手牌变化事件
    private void notifyHandCardChanged() {
        for (Consumer<HandCard> listener : onHandCardChangedListeners) {
            listener.accept(this);
        }
    }

    /**
     * 重写toString方法
     * 
     * @return 手牌信息的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("手牌数量: ").append(cards.size()).append("/").append(MAX_CARDS).append("\n");
        Map<CardType, Integer> typeCount = getCardTypeCount();
        for (Map.Entry<CardType, Integer> entry : typeCount.entrySet()) {
            sb.append(entry.getKey().name()).append(": ").append(entry.getValue()).append("张\n");
        }
        return sb.toString();
    }
}