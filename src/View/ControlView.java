package View;

import Model.Player;
import Model.Cards.Card;
import Model.Tile;

import java.util.List;
import java.util.Scanner;

public class ControlView {
    private Scanner scanner = new Scanner(System.in);

    // Display game control menu
    public void displayControlMenu() {
        // Display various operation options, such as move, shore up, use cards, etc.
    }

    // Get player's operation choice
    public int getPlayerChoice() {
        // Read player's input choice
        return scanner.nextInt();
    }

    // Display tiles that player can move to
    public void displayAvailableMoves(List<Tile> availableTiles) {
        // Display information of all movable tiles
    }

    // Display cards that player can use
    public void displayAvailableCards(List<Card> availableCards) {
        // Display information of all usable cards
    }

    // Prompt player to choose a tile to move to
    public Tile promptForMove(List<Tile> availableTiles) {
        // Prompt player to input the tile number to move to, and return the corresponding tile
        return null;
    }

    // Prompt player to choose a card to use
    public Card promptForCard(List<Card> availableCards) {
        // Prompt player to input the card number to use, and return the corresponding card
        return null;
    }
}