package Model.Cards;

import Model.Enumeration.TreasureType;
import Model.Enumeration.TileName;
import Model.Enumeration.TileState;
import Model.Tile;
import Model.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Card functionality test class
 * Used to test the basic functionality of all types of cards
 */
public class CardTest {
    private static final List<String> testResults = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("Starting card functionality tests...\n");
        
        // Test treasure cards
        testTreasureCards();
        
        // Test special cards
        testSpecialCards();
        
        // Test flood cards
        testFloodCards();
        
        // Print test results
        System.out.println("\nTest Results Summary:");
        for (String result : testResults) {
            System.out.println(result);
        }
    }
    
    private static void testTreasureCards() {
        System.out.println("=== Testing Treasure Cards ===");
        
        // Test all types of treasure cards
        for (TreasureType type : TreasureType.values()) {
            TreasureCard card = new TreasureCard(type);
            
            // Test basic info
            testBasicCardInfo(card, "Treasure Card");
            
            // Test treasure type
            if (card.getTreasureType() == type) {
                addTestResult("✓ Treasure Card " + type.getDisplayName() + " type is correct");
            } else {
                addTestResult("✗ Treasure Card " + type.getDisplayName() + " type is incorrect");
            }
            
            // Test usage condition
            Tile testTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
            List<Player> players = new ArrayList<>();
            Player player = new Player();
            player.setCurrentTile(testTile);  // Set player position using setCurrentTile method
            players.add(player);
            
            if (card.canUse(players, testTile)) {
                addTestResult("✓ Treasure Card " + type.getDisplayName() + " usage condition check is correct");
            } else {
                addTestResult("✗ Treasure Card " + type.getDisplayName() + " usage condition check is incorrect");
            }
        }
    }
    
    private static void testSpecialCards() {
        System.out.println("\n=== Testing Special Cards ===");
        
        // Test helicopter card
        Tile helicopterTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
        HelicopterCard helicopterCard = new HelicopterCard(helicopterTile);
        testBasicCardInfo(helicopterCard, "Helicopter Rescue Card");
        
        // Test sandbag card
        SandbagCard sandbagCard = new SandbagCard();
        testBasicCardInfo(sandbagCard, "Sandbag Card");
        
        // Test sandbag card usage
        Tile floodedTile = new Tile(TileName.CORAL_PALACE, 1, 1);
        floodedTile.setState(TileState.FLOODED); // Set tile to flooded state
        if (sandbagCard.canUse(floodedTile)) {
            addTestResult("✓ Sandbag card can be used on flooded tile");
            if (sandbagCard.useCard(floodedTile)) {
                addTestResult("✓ Sandbag card successfully shored up the tile");
                if (floodedTile.getState() == TileState.NORMAL) {
                    addTestResult("✓ Tile successfully restored to dry state");
                } else {
                    addTestResult("✗ Tile state not updated correctly");
                }
            } else {
                addTestResult("✗ Sandbag card usage failed");
            }
        } else {
            addTestResult("✗ Sandbag card cannot be used on flooded tile");
        }
        
        // Test water rise card
        WaterRiseCard waterRiseCard = new WaterRiseCard();
        testBasicCardInfo(waterRiseCard, "Water Rise Card");
    }
    
    private static void testFloodCards() {
        System.out.println("\n=== Testing Flood Cards ===");
        
        // Test flood card
        Tile targetTile = new Tile(TileName.CORAL_PALACE, 1, 1);
        FloodCard floodCard = new FloodCard(targetTile);
        testBasicCardInfo(floodCard, "Flood Card");
        
        // Test flood card usage
        if (floodCard.canUse()) {
            addTestResult("✓ Flood card can be used");
            floodCard.use();
            if (targetTile.getState() == TileState.FLOODED) {
                addTestResult("✓ Flood card successfully flooded the target tile");
            } else {
                addTestResult("✗ Flood card did not correctly flood the target tile");
            }
        } else {
            addTestResult("✗ Flood card cannot be used");
        }
    }
    
    private static void testBasicCardInfo(Card card, String expectedType) {
        // Test card type
        if (card.getType() != null) {
            addTestResult("✓ " + card.getName() + " type is correct");
        } else {
            addTestResult("✗ " + card.getName() + " type is null");
        }
        
        // Test card name
        if (card.getName() != null && !card.getName().isEmpty()) {
            addTestResult("✓ " + card.getName() + " name is correct");
        } else {
            addTestResult("✗ " + card.getName() + " name is null");
        }
        
        // Test card description
        if (card.getDescription() != null && !card.getDescription().isEmpty()) {
            addTestResult("✓ " + card.getName() + " description is correct");
        } else {
            addTestResult("✗ " + card.getName() + " description is null");
        }
        
        // Test card usability
        if (card.canUse()) {
            addTestResult("✓ " + card.getName() + " is usable initially");
        } else {
            addTestResult("✗ " + card.getName() + " is not usable initially");
        }
    }
    
    private static void addTestResult(String result) {
        testResults.add(result);
        System.out.println(result);
    }
} 