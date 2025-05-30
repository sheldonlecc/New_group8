// Model/Deck/FloodDeck.java
package Model.Deck;

import Model.Cards.FloodCard;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 洪水牌堆类
 * 管理游戏中的洪水卡牌堆，处理洪水卡的特殊规则
 * 新机制：每次从所有地点中抽取6张作为当前FloodDeck，抽完后重新抽取6张
 */
public class FloodDeck extends Deck<FloodCard> {
    /** 主牌堆 - 包含所有可用的洪水卡 */
    private final List<FloodCard> masterDeck;
    /** 当前活跃的6张卡牌 */
    private final List<FloodCard> activeDeck;
    /** 当前活跃牌堆的索引 */
    private int currentIndex;
    
    /**
     * 构造函数
     * 根据初始瓦片列表创建洪水卡牌堆
     * 
     * @param initialTiles 初始瓦片列表，每个瓦片对应一张洪水卡
     */
    public FloodDeck(List<Tile> initialTiles) {
        super();
        this.masterDeck = new ArrayList<>();
        this.activeDeck = new ArrayList<>();
        this.currentIndex = 0;
        
        if (initialTiles != null) {
            initialTiles.forEach(tile -> masterDeck.add(new FloodCard(tile)));
            refillActiveDeck();
        }
    }
    
    /**
     * 重新填充活跃牌堆
     * 从主牌堆中随机抽取6张卡牌作为新的活跃牌堆
     */
    private void refillActiveDeck() {
        activeDeck.clear();
        currentIndex = 0;
        
        // 获取所有可用的卡牌（对应未沉没的瓦片）
        List<FloodCard> availableCards = masterDeck.stream()
                .filter(card -> card.getTargetTile().getState() != TileState.SUNK)
                .collect(Collectors.toList());
        
        if (availableCards.isEmpty()) {
            System.out.println("[警告] 没有可用的洪水卡！");
            return;
        }
        
        // 随机选择最多6张卡牌
        Collections.shuffle(availableCards);
        int cardCount = Math.min(6, availableCards.size());
        
        for (int i = 0; i < cardCount; i++) {
            activeDeck.add(availableCards.get(i));
        }
        
        System.out.println("[日志] 重新填充FloodDeck，当前活跃卡牌数量: " + activeDeck.size());
    }
    
    /**
     * 抽牌
     * 从当前活跃的6张卡牌中按顺序抽取
     * 当6张卡牌抽完后，重新填充活跃牌堆
     * 
     * @return 抽到的卡牌，如果没有可抽取的卡牌则返回null
     */
    @Override
    public FloodCard draw() {
        // 如果当前活跃牌堆已抽完，重新填充
        if (currentIndex >= activeDeck.size()) {
            refillActiveDeck();
            if (activeDeck.isEmpty()) {
                return null;
            }
        }
        
        FloodCard card = activeDeck.get(currentIndex);
        currentIndex++;
        
        System.out.println("[日志] 从FloodDeck抽取卡牌: " + card.getTargetTile().getName() + 
                          " (剩余: " + (activeDeck.size() - currentIndex) + "/" + activeDeck.size() + ")");
        
        return card;
    }
    
    /**
     * 获取所有被淹没的瓦片
     * 
     * @return 当前状态为FLOODED的瓦片列表
     */
    public List<Tile> getFloodedTiles() {
        return masterDeck.stream()
                .map(FloodCard::getTargetTile)
                .filter(tile -> tile.getState() == TileState.FLOODED)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有未被淹没的瓦片
     * 
     * @return 当前在主牌堆中对应未沉没瓦片的列表
     */
    public List<Tile> getUnfloodedTiles() {
        return masterDeck.stream()
                .map(FloodCard::getTargetTile)
                .filter(tile -> tile.getState() != TileState.SUNK)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查指定瓦片是否被淹没
     * 
     * @param tile 要检查的瓦片
     * @return 如果瓦片状态为FLOODED则返回true
     */
    public boolean isTileFlooded(Tile tile) {
        return tile.getState() == TileState.FLOODED;
    }
    
    /**
     * 获取指定瓦片对应的洪水卡
     * 
     * @param tile 目标瓦片
     * @return 对应的洪水卡，如果不存在则返回null
     */
    public FloodCard getFloodCardForTile(Tile tile) {
        return masterDeck.stream()
                .filter(card -> card.getTargetTile().equals(tile))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 检查是否所有瓦片都被淹没
     * 
     * @return 如果所有瓦片都处于SUNK状态则返回true
     */
    public boolean areAllTilesFlooded() {
        return masterDeck.stream()
                .map(FloodCard::getTargetTile)
                .allMatch(tile -> tile.getState() == TileState.SUNK);
    }
    
    /**
     * 重置洪水卡状态
     * 重新填充活跃牌堆
     */
    public void resetFloodCards() {
        refillActiveDeck();
    }
    
    /**
     * 移除指定瓦片对应的洪水卡（当瓦片沉没时）
     * 
     * @param tile 沉没的瓦片
     */
    public void removeCardForSunkTile(Tile tile) {
        masterDeck.removeIf(card -> card.getTargetTile().equals(tile));
        activeDeck.removeIf(card -> card.getTargetTile().equals(tile));
        // 如果活跃牌堆中的卡牌被移除，需要调整索引
        if (currentIndex > activeDeck.size()) {
            currentIndex = activeDeck.size();
        }
    }
    
    /**
     * 获取当前活跃牌堆剩余卡牌数量
     * 
     * @return 活跃牌堆中剩余的卡牌数量
     */
    public int getActiveDeckRemainingCount() {
        return Math.max(0, activeDeck.size() - currentIndex);
    }
    
    /**
     * 获取主牌堆总卡牌数量
     * 
     * @return 主牌堆中的卡牌总数
     */
    public int getMasterDeckSize() {
        return masterDeck.size();
    }
    
    /**
     * 强制重新填充活跃牌堆
     * 用于特殊情况下的重置
     */
    public void forceRefillActiveDeck() {
        refillActiveDeck();
    }
    
    @Override
    public boolean isValid() {
        return masterDeck != null && activeDeck != null &&
                masterDeck.stream().allMatch(card -> card != null && card.getTargetTile() != null) &&
                activeDeck.stream().allMatch(card -> card != null && card.getTargetTile() != null);
    }
    
    // 以下方法保持兼容性，但在新机制下不再使用
    @Override
    public void discard(FloodCard card) {
        // 在新机制下，不需要弃牌堆
        // 保留此方法以保持兼容性
    }
    
    @Override
    public void reshuffleDiscardPile() {
        // 在新机制下，通过refillActiveDeck实现类似功能
        refillActiveDeck();
    }
    
    @Override
    public void shuffle() {
        // 在新机制下，每次refillActiveDeck时会自动洗牌
        Collections.shuffle(masterDeck);
    }
}