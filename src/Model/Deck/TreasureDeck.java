// Model/Deck/TreasureDeck.java
package Model.Deck;

import Model.Cards.*;
import Model.Enumeration.TreasureType;
import Model.Tile;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 宝藏牌堆类
 * 管理游戏中的宝藏卡和特殊卡牌堆
 * 包括：宝藏卡、直升机救援卡、沙袋卡、水位上升卡
 */
public class TreasureDeck extends Deck<Card> {
    private boolean isFirstDraw; // 是否是初始发牌阶段
    private final Tile helicopterTile; // 直升机场位置
    private final Map<TreasureType, Integer> collectedTreasures; // 已收集的宝藏数量

    /**
     * 构造函数
     * 
     * @param helicopterTile 直升机场位置，用于创建直升机救援卡
     */
    public TreasureDeck(Tile helicopterTile) {
        super();
        this.helicopterTile = helicopterTile;
        this.isFirstDraw = true;
        this.collectedTreasures = new EnumMap<>(TreasureType.class);
        initializeCards();
        shuffle();
    }

    /**
     * 初始化牌堆
     * 创建所有宝藏卡和特殊卡
     */
    private void initializeCards() {
        // 添加宝藏卡（每个类型5张）
        Arrays.stream(TreasureType.values()).forEach(type -> {
            for (int i = 0; i < 5; i++) {
                drawPile.push(new TreasureCard(type));
            }
        });

        // 添加特殊卡
        for (int i = 0; i < 3; i++) {
            drawPile.push(new HelicopterCard(helicopterTile));
            drawPile.push(new WaterRiseCard());
            drawPile.push(new SandbagCard());
        }
    }

    /**
     * 抽牌
     * 重写父类方法，添加特殊卡处理逻辑
     * 
     * @return 抽到的卡牌，如果牌堆为空则返回null
     */
    @Override
    public Card draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    /**
     * 初始发牌
     * 特殊处理：如果抽到水位上升卡，将其移到牌堆底部
     * 
     * @return 抽到的卡牌，如果牌堆为空则返回null
     */
    public Card drawInitialCard() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        if (drawPile.isEmpty()) {
            return null;
        }

        Card topCard = drawPile.peek();
        if (topCard instanceof WaterRiseCard && isFirstDraw) {
            moveCardToBottom(topCard);
            return drawInitialCard();
        }
        return drawPile.pop();
    }

    /**
     * 将卡牌移到牌堆底部
     * 
     * @param card 要移动的卡牌
     */
    private void moveCardToBottom(Card card) {
        drawPile.pop(); // 移除顶部卡牌
        Stack<Card> tempStack = new Stack<>();
        while (!drawPile.isEmpty()) {
            tempStack.push(drawPile.pop());
        }
        drawPile.push(card);
        while (!tempStack.isEmpty()) {
            drawPile.push(tempStack.pop());
        }
    }

    /**
     * 结束初始发牌阶段
     */
    public void finishInitialDraw() {
        isFirstDraw = false;
    }

    /**
     * 记录宝藏收集
     * 
     * @param type 宝藏类型
     */
    public void recordTreasureCollection(TreasureType type) {
        collectedTreasures.merge(type, 1, Integer::sum);
    }

    /**
     * 检查宝藏是否已收集完成
     * 
     * @param type 宝藏类型
     * @return 如果该类型宝藏已收集4个则返回true
     */
    public boolean isTreasureCollected(TreasureType type) {
        return collectedTreasures.getOrDefault(type, 0) >= 4;
    }

    /**
     * 获取已收集的宝藏数量
     * 
     * @param type 宝藏类型
     * @return 已收集的宝藏数量
     */
    public int getCollectedTreasureCount(TreasureType type) {
        return collectedTreasures.getOrDefault(type, 0);
    }

    /**
     * 获取所有已收集的宝藏类型
     * 
     * @return 已收集完成的宝藏类型集合
     */
    public Set<TreasureType> getCollectedTreasures() {
        return collectedTreasures.entrySet().stream()
                .filter(entry -> entry.getValue() >= 4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * 获取指定类型的卡牌数量
     * 
     * @param cardType 卡牌类型
     * @return 该类型卡牌的数量
     */
    public int getCardTypeCount(Class<? extends Card> cardType) {
        return (int) Stream.concat(drawPile.stream(), discardPile.stream())
                .filter(cardType::isInstance)
                .count();
    }

    /**
     * 检查是否所有宝藏都已收集
     * 
     * @return 如果所有宝藏类型都已收集4个则返回true
     */
    public boolean areAllTreasuresCollected() {
        return Arrays.stream(TreasureType.values())
                .allMatch(this::isTreasureCollected);
    }

    /**
     * 检查是否所有宝藏都已收集（对外接口）
     * 
     * @return 如果所有宝藏类型都已收集4个则返回true
     */
    public boolean allTreasuresCollected() {
        return areAllTreasuresCollected();
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                helicopterTile != null &&
                drawPile.stream().allMatch(card -> card != null) &&
                discardPile.stream().allMatch(card -> card != null);
    }
}