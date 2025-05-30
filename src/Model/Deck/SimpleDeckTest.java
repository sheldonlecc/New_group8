package Model.Deck;

import Model.Cards.*;
import Model.Tile;
import Model.Enumeration.TreasureType;
import Model.Enumeration.TileState;
import Model.Enumeration.TileName;

import java.util.List;
import java.util.ArrayList;

public class SimpleDeckTest {

    public static void main(String[] args) {
        System.out.println("--- Starting Simple Deck Tests ---");

        // 创建测试用的瓦片
        Tile helicopterTile = new Tile(TileName.FOOLS_LANDING, 0, 0);
        List<Tile> initialTiles = new ArrayList<>();
        initialTiles.add(new Tile(TileName.TEMPLE_OF_THE_SUN, 0, 0));
        initialTiles.add(new Tile(TileName.TEMPLE_OF_THE_MOON, 0, 1));
        initialTiles.add(new Tile(TileName.CORAL_PALACE, 0, 2));
        initialTiles.add(new Tile(TileName.CAVE_OF_EMBERS, 1, 0));
        initialTiles.add(new Tile(TileName.CAVE_OF_SHADOWS, 1, 1));
        initialTiles.add(new Tile(TileName.OBSERVATORY, 1, 2));

        // 初始化牌堆
        TreasureDeck treasureDeck = new TreasureDeck(helicopterTile);
        FloodDeck floodDeck = new FloodDeck(initialTiles);

        System.out.println("\nTesting TreasureDeck Initialization:");
        if (treasureDeck != null && treasureDeck.getDiscardPileSize() == 0 && treasureDeck.getDrawPileSize() > 0) {
            System.out.println("TreasureDeck Initialization Test Passed.");
        } else {
            System.out.println("TreasureDeck Initialization Test Failed.");
        }

        System.out.println("\nTesting FloodDeck Initialization:");
        if (floodDeck != null && floodDeck.getMasterDeckSize() == 6 && floodDeck.getActiveDeckRemainingCount() == 6) {
            System.out.println("FloodDeck Initialization Test Passed.");
        } else {
            System.out.println("FloodDeck Initialization Test Failed.");
        }

        System.out.println("\nTesting TreasureDeck Draw:");
        Card drawnTreasureCard = treasureDeck.draw();
        if (drawnTreasureCard != null) {
            System.out.println("TreasureDeck Draw Test Passed. Drawn card: " + drawnTreasureCard.getName());
        } else {
            System.out.println("TreasureDeck Draw Test Failed. Drawn card is null.");
        }

        System.out.println("\nTesting FloodDeck Draw:");
        FloodCard drawnFloodCard = floodDeck.draw();
        if (drawnFloodCard != null) {
            System.out.println("FloodDeck Draw Test Passed. Drawn card for tile: " + drawnFloodCard.getTargetTile().getName());
            if (floodDeck.getActiveDeckRemainingCount() == 5) {
                System.out.println("FloodDeck Remaining Count Test Passed.");
            } else {
                System.out.println("FloodDeck Remaining Count Test Failed. Expected 5, got " + floodDeck.getActiveDeckRemainingCount());
            }
        } else {
            System.out.println("FloodDeck Draw Test Failed. Drawn card is null.");
        }

        System.out.println("\nTesting TreasureDeck Discard:");
        Card cardToDiscard = treasureDeck.draw(); // Draw another card to discard
        if (cardToDiscard != null) {
            treasureDeck.discard(cardToDiscard);
            if (treasureDeck.getDiscardPileSize() == 1) {
                System.out.println("TreasureDeck Discard Test Passed.");
            } else {
                System.out.println("TreasureDeck Discard Test Failed. Expected discard size 1, got " + treasureDeck.getDiscardPileSize());
            }
        } else {
             System.out.println("TreasureDeck Discard Test Skipped. Could not draw card to discard.");
        }


        // 您可以根据需要添加更多测试...
        System.out.println("\n--- Simple Deck Tests Finished ---");
    }
} 