// Model/Deck/FloodDeck.java
package Model.Deck;

import Model.Cards.FloodCard;
import Model.Tile;
import Model.Enumeration.TileState;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 洪水牌堆类
 * 管理游戏中的洪水卡牌堆，处理洪水卡的特殊规则
 */
public class FloodDeck extends Deck<FloodCard> {
    /**
     * 构造函数
     * 根据初始瓦片列表创建洪水卡牌堆
     * @param initialTiles 初始瓦片列表，每个瓦片对应一张洪水卡
     */
    public FloodDeck(List<Tile> initialTiles) {
        super();
        if (initialTiles != null) {
            initialTiles.forEach(tile -> drawPile.push(new FloodCard(tile)));
            shuffle();
        }
    }

    /**
     * 获取所有被淹没的瓦片
     * @return 当前状态为FLOODED的瓦片列表
     */
    public List<Tile> getFloodedTiles() {
        return Stream.concat(drawPile.stream(), discardPile.stream())
                .map(FloodCard::getTargetTile)
                .filter(tile -> tile.getState() == TileState.FLOODED)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有未被淹没的瓦片
     * @return 当前在抽牌堆中的洪水卡对应的瓦片列表
     */
    public List<Tile> getUnfloodedTiles() {
        return drawPile.stream()
                .map(FloodCard::getTargetTile)
                .collect(Collectors.toList());
    }

    /**
     * 检查指定瓦片是否被淹没
     * @param tile 要检查的瓦片
     * @return 如果瓦片对应的洪水卡在弃牌堆中则返回true
     */
    public boolean isTileFlooded(Tile tile) {
        return discardPile.stream()
                .anyMatch(card -> card.getTargetTile().equals(tile));
    }

    /**
     * 获取指定瓦片对应的洪水卡
     * @param tile 目标瓦片
     * @return 对应的洪水卡，如果不存在则返回null
     */
    public FloodCard getFloodCardForTile(Tile tile) {
        return Stream.concat(drawPile.stream(), discardPile.stream())
                .filter(card -> card.getTargetTile().equals(tile))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查是否所有瓦片都被淹没
     * @return 如果抽牌堆为空且弃牌堆不为空则返回true
     */
    public boolean areAllTilesFlooded() {
        return drawPile.isEmpty() && !discardPile.isEmpty();
    }

    /**
     * 重置洪水卡状态
     * 将所有弃牌堆中的卡牌重新加入抽牌堆并洗牌
     * 用于游戏重置或特殊事件
     */
    public void resetFloodCards() {
        reshuffleDiscardPile();
    }

    /**
     * 获取可抽取的洪水卡
     * 只返回与未被沉没的地块对应的卡牌
     * @return 可抽取的洪水卡列表
     */
    private List<FloodCard> getDrawableCards() {
        return drawPile.stream()
                .filter(card -> card.getTargetTile().getState() != TileState.SUNK)
                .collect(Collectors.toList());
    }

    /**
     * 抽牌
     * 重写父类方法，每次抽牌前都进行洗牌
     * @return 抽到的卡牌，如果没有可抽取的卡牌则返回null
     */
    @Override
    public FloodCard draw() {
        // 每次抽牌前都进行洗牌
        reshuffleDiscardPile();

        List<FloodCard> drawableCards = getDrawableCards();
        if (drawableCards.isEmpty()) {
            return null;
        }

        // 从可抽取的卡牌中随机选择一张
        int randomIndex = (int) (Math.random() * drawableCards.size());
        FloodCard selectedCard = drawableCards.get(randomIndex);
        drawPile.remove(selectedCard);
        return selectedCard;
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                drawPile.stream().allMatch(card -> card != null && card.getTargetTile() != null) &&
                discardPile.stream().allMatch(card -> card != null && card.getTargetTile() != null);
    }
}