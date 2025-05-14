package Model.Cards;

import Model.Enumeration.TreasureType;
import Model.Enumeration.TileName;
import Model.Enumeration.TileState;
import Model.Tile;
import Model.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌功能测试类
 * 用于测试所有类型卡牌的基本功能
 */
public class CardTest {
    private static final List<String> testResults = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("开始卡牌功能测试...\n");
        
        // 测试宝藏卡
        testTreasureCards();
        
        // 测试特殊卡
        testSpecialCards();
        
        // 测试洪水卡
        testFloodCards();
        
        // 打印测试结果
        System.out.println("\n测试结果汇总：");
        for (String result : testResults) {
            System.out.println(result);
        }
    }
    
    private static void testTreasureCards() {
        System.out.println("=== 测试宝藏卡 ===");
        
        // 测试所有类型的宝藏卡
        for (TreasureType type : TreasureType.values()) {
            TreasureCard card = new TreasureCard(type);
            
            // 测试基本信息
            testBasicCardInfo(card, "宝藏卡");
            
            // 测试宝藏类型
            if (card.getTreasureType() == type) {
                addTestResult("✓ 宝藏卡 " + type.getDisplayName() + " 类型正确");
            } else {
                addTestResult("✗ 宝藏卡 " + type.getDisplayName() + " 类型错误");
            }
            
            // 测试使用条件
            Tile testTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
            List<Player> players = new ArrayList<>();
            Player player = new Player();
            player.setCurrentTile(testTile);  // 使用setCurrentTile方法设置玩家位置
            players.add(player);
            
            if (card.canUse(players, testTile)) {
                addTestResult("✓ 宝藏卡 " + type.getDisplayName() + " 使用条件检查正确");
            } else {
                addTestResult("✗ 宝藏卡 " + type.getDisplayName() + " 使用条件检查错误");
            }
        }
    }
    
    private static void testSpecialCards() {
        System.out.println("\n=== 测试特殊卡 ===");
        
        // 测试直升机卡
        Tile helicopterTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
        HelicopterCard helicopterCard = new HelicopterCard(helicopterTile);
        testBasicCardInfo(helicopterCard, "直升机救援卡");
        
        // 测试沙袋卡
        SandbagCard sandbagCard = new SandbagCard();
        testBasicCardInfo(sandbagCard, "沙袋卡");
        
        // 测试沙袋卡使用功能
        Tile floodedTile = new Tile(TileName.CORAL_PALACE, 1, 1);
        floodedTile.setState(TileState.FLOODED); // 将瓦片设置为被淹没状态
        if (sandbagCard.canUse(floodedTile)) {
            addTestResult("✓ 沙袋卡可以用于被淹没的瓦片");
            if (sandbagCard.useCard(floodedTile)) {
                addTestResult("✓ 沙袋卡成功加固瓦片");
                if (floodedTile.getState() == TileState.NORMAL) {
                    addTestResult("✓ 瓦片成功恢复干燥状态");
                } else {
                    addTestResult("✗ 瓦片状态未正确更新");
                }
            } else {
                addTestResult("✗ 沙袋卡使用失败");
            }
        } else {
            addTestResult("✗ 沙袋卡无法用于被淹没的瓦片");
        }
        
        // 测试水位上升卡
        WaterRiseCard waterRiseCard = new WaterRiseCard();
        testBasicCardInfo(waterRiseCard, "水位上升卡");
    }
    
    private static void testFloodCards() {
        System.out.println("\n=== 测试洪水卡 ===");
        
        // 测试洪水卡
        Tile targetTile = new Tile(TileName.CORAL_PALACE, 1, 1);
        FloodCard floodCard = new FloodCard(targetTile);
        testBasicCardInfo(floodCard, "洪水卡");
        
        // 测试洪水卡使用功能
        if (floodCard.canUse()) {
            addTestResult("✓ 洪水卡可以使用");
            floodCard.use();
            if (targetTile.getState() == TileState.FLOODED) {
                addTestResult("✓ 洪水卡成功淹没目标瓦片");
            } else {
                addTestResult("✗ 洪水卡未能正确淹没目标瓦片");
            }
        } else {
            addTestResult("✗ 洪水卡无法使用");
        }
    }
    
    private static void testBasicCardInfo(Card card, String expectedType) {
        // 测试卡牌类型
        if (card.getType() != null) {
            addTestResult("✓ " + card.getName() + " 类型正确");
        } else {
            addTestResult("✗ " + card.getName() + " 类型为空");
        }
        
        // 测试卡牌名称
        if (card.getName() != null && !card.getName().isEmpty()) {
            addTestResult("✓ " + card.getName() + " 名称正确");
        } else {
            addTestResult("✗ " + card.getName() + " 名称为空");
        }
        
        // 测试卡牌描述
        if (card.getDescription() != null && !card.getDescription().isEmpty()) {
            addTestResult("✓ " + card.getName() + " 描述正确");
        } else {
            addTestResult("✗ " + card.getName() + " 描述为空");
        }
        
        // 测试卡牌可用性
        if (card.canUse()) {
            addTestResult("✓ " + card.getName() + " 初始状态可用");
        } else {
            addTestResult("✗ " + card.getName() + " 初始状态不可用");
        }
    }
    
    private static void addTestResult(String result) {
        testResults.add(result);
        System.out.println(result);
    }
} 