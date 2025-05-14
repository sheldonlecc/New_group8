// Model/Deck/Deck.java
package Model.Deck;

import Model.Cards.Card;
import java.util.Collections;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/**
 * 牌堆抽象基类
 * 提供牌堆的基本操作和状态管理
 * @param <T> 卡牌类型，必须是Card的子类
 */
public abstract class Deck<T extends Card> {
    /** 抽牌堆 */
    protected final Stack<T> drawPile;
    /** 弃牌堆 */
    protected final Stack<T> discardPile;

    /**
     * 构造函数
     * 初始化抽牌堆和弃牌堆
     */
    protected Deck() {
        this.drawPile = new Stack<>();
        this.discardPile = new Stack<>();
    }

    /**
     * 洗牌
     * 随机打乱抽牌堆中卡牌的顺序
     */
    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    /**
     * 抽牌
     * 从抽牌堆顶部抽取一张卡牌
     * 如果抽牌堆为空，则重洗弃牌堆
     * @return 抽到的卡牌，如果牌堆为空则返回null
     */
    public T draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    /**
     * 弃牌
     * 将卡牌放入弃牌堆
     * @param card 要弃置的卡牌
     */
    public void discard(T card) {
        if (card != null) {
            discardPile.push(card);
        }
    }

    /**
     * 重洗弃牌堆
     * 将弃牌堆中的所有卡牌重新加入抽牌堆并洗牌
     */
    public void reshuffleDiscardPile() {
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    /**
     * 查看牌堆顶部卡牌
     * 不改变牌堆状态
     * @return 牌堆顶部的卡牌，如果牌堆为空则返回null
     */
    public T peek() {
        return drawPile.isEmpty() ? null : drawPile.peek();
    }

    /**
     * 获取抽牌堆剩余卡牌数量
     * @return 抽牌堆中的卡牌数量
     */
    public int getDrawPileSize() {
        return drawPile.size();
    }

    /**
     * 获取弃牌堆卡牌数量
     * @return 弃牌堆中的卡牌数量
     */
    public int getDiscardPileSize() {
        return discardPile.size();
    }

    /**
     * 检查牌堆是否为空
     * @return 如果抽牌堆和弃牌堆都为空则返回true
     */
    public boolean isEmpty() {
        return drawPile.isEmpty() && discardPile.isEmpty();
    }

    /**
     * 获取抽牌堆的不可修改视图
     * @return 抽牌堆的只读列表
     */
    public List<T> getDrawPileView() {
        return Collections.unmodifiableList(new ArrayList<>(drawPile));
    }

    /**
     * 获取弃牌堆的不可修改视图
     * @return 弃牌堆的只读列表
     */
    public List<T> getDiscardPileView() {
        return Collections.unmodifiableList(new ArrayList<>(discardPile));
    }

    /**
     * 清空牌堆
     * 清空抽牌堆和弃牌堆
     */
    public void clear() {
        drawPile.clear();
        discardPile.clear();
    }

    /**
     * 检查牌堆状态是否有效
     * 子类可以重写此方法以添加特定的验证逻辑
     * @return 如果牌堆状态有效则返回true
     */
    public boolean isValid() {
        return drawPile != null && discardPile != null;
    }
}