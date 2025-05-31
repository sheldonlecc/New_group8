package Controller;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Cards.FloodCard;
import Model.Cards.SandbagCard;
import Model.Cards.HelicopterCard;
import Model.Cards.WaterRiseCard;
import Model.Player;
import Model.Tile;
import View.CardView;
import View.PlayerInfoView;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.util.List;
import java.util.ArrayList;

public class CardController implements ActionListener {
    private static final int MAX_CARDS = 7;
    private final GameController gameController;
    private boolean isDiscardMode = false;
    private int cardsToDiscard = 0;
    private int cardsDiscarded = 0;
    private PlayerInfoView currentDiscardingPlayer = null;
    public Integer pendingGiveCardPlayerIndex = null; // Record the card giver

    public CardController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof CardView) {
            CardView cardView = (CardView) source;
            System.out.println("Card clicked - Is in discard mode: " + isDiscardMode); // Debug info
            if (isDiscardMode) {
                System.out.println("Currently in discard mode, handling card click"); // Debug info
                handleCardClick(cardView.getCard());
            } else {
                System.out.println("Not in discard mode, ignoring card click"); // Debug info
            }
        }
    }

    private void handleCardClick(Card card) {
        System.out.println("Handling card click - Card type: " + (card != null ? card.getClass().getSimpleName() : "null")); // Debug info
        if (isDiscardMode && currentDiscardingPlayer != null) {
            if (card != null) {
                handleDiscardCard(card);
            }
        } else {
            if (card instanceof SandbagCard) {
                handleSandbagCard((SandbagCard) card);
            } else if (card instanceof HelicopterCard) {
                handleHelicopterCard((HelicopterCard) card);
            }
        }
    }

    private void handleSandbagCard(SandbagCard card) {
        // Handle sandbag card click logic
        gameController.handleShoreUp(gameController.getCurrentPlayerIndex());
    }

    private void handleHelicopterCard(HelicopterCard card) {
        // Get current player
        int currentPlayerIndex = gameController.getCurrentPlayerIndex();
        Player currentPlayer = gameController.getPlayers().get(currentPlayerIndex);

        // Check if victory conditions are met
        if (card.canUseForVictory(gameController.getPlayers())) {
            if (useHelicopterCardForWin(currentPlayerIndex)) {
                return;
            }
        }

        // If victory conditions are not met, use movement function
        // Get all players
        List<Player> players = gameController.getPlayers();

        // Create player selection dialog
        String[] playerOptions = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            playerOptions[i] = "Player " + (i + 1) + " (" + p.getRole().getClass().getSimpleName() + ")";
        }

        // Show player selection dialog (multiple choice)
        List<Integer> selectedPlayers = new ArrayList<>();
        while (true) {
            // Update options display, add selection count information
            String[] currentOptions = new String[playerOptions.length];
            for (int i = 0; i < playerOptions.length; i++) {
                int selectedCount = 0;
                for (int selected : selectedPlayers) {
                    if (selected == i)
                        selectedCount++;
                }
                currentOptions[i] = playerOptions[i]
                        + (selectedCount > 0 ? String.format(" (Selected %d times)", selectedCount) : "");
            }

            // Create player selection panel
            JPanel playerPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            JButton[] playerButtons = new JButton[currentOptions.length];
            for (int i = 0; i < currentOptions.length; i++) {
                final int index = i;
                playerButtons[i] = new JButton(currentOptions[i]);
                playerButtons[i].addActionListener(e -> {
                    selectedPlayers.add(index);
                    System.out.println(String.format("[Log] Selected player: %s", playerOptions[index]));
                    // Update button text
                    int selectedCount = 0;
                    for (int selected : selectedPlayers) {
                        if (selected == index)
                            selectedCount++;
                    }
                    playerButtons[index].setText(playerOptions[index] + String.format(" (Selected %d times)", selectedCount));
                });
                playerPanel.add(playerButtons[i]);
            }

            // Create operation button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton confirmButton = new JButton("Confirm");
            JButton cancelButton = new JButton("Cancel");

            // Create a flag to track if dialog is confirmed
            final boolean[] confirmed = { false };

            confirmButton.addActionListener(e -> {
                if (selectedPlayers.isEmpty()) {
                    System.out.println("[Log] Player did not select any players.");
                    JOptionPane.showMessageDialog(null, "Please select at least one player!");
                } else {
                    confirmed[0] = true;
                    Window window = SwingUtilities.getWindowAncestor(buttonPanel);
                    if (window != null) {
                        window.dispose();
                    }
                }
            });

            cancelButton.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(buttonPanel);
                if (window != null) {
                    window.dispose();
                }
            });

            buttonPanel.add(confirmButton);
            buttonPanel.add(cancelButton);

            // Create main panel
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(playerPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Show dialog
            JOptionPane.showOptionDialog(
                    null,
                    mainPanel,
                    "Select players to move",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[] {},
                    null);

            if (!confirmed[0]) {
                return; // User cancelled selection
            }

            // Get selected players
            List<Player> selectedPlayersList = new ArrayList<>();
            for (int index : selectedPlayers) {
                selectedPlayersList.add(players.get(index));
            }

            // Check if all selected players are on the same tile
            if (!card.canUseForMovement(selectedPlayersList)) {
                JOptionPane.showMessageDialog(null, "All selected players must be on the same tile!");
                return;
            }

            // Enter movement mode, wait for player to select target location
            gameController.getMapController().enterHelicopterMoveMode(currentPlayerIndex, selectedPlayersList, card);
            break;
        }
    }

    public void addCard(PlayerInfoView playerInfoView, Card card) {
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        if (cardsPanel.getComponentCount() < MAX_CARDS) {
            int playerCount = gameController.getPlayerInfoViews().size();
            CardView cardView = new CardView(card, playerCount);
            cardView.addActionListener(this); // Add click event listener
            cardsPanel.add(cardView);
            cardsPanel.revalidate();
            cardsPanel.repaint();
        }
    }

    public void removeCard(PlayerInfoView playerInfoView, Card card) {
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        Component[] components = cardsPanel.getComponents();
        for (Component component : components) {
            if (component instanceof CardView) {
                CardView cardView = (CardView) component;
                if (cardView.getCard().equals(card)) {
                    cardsPanel.remove(cardView);
                    cardsPanel.revalidate();
                    cardsPanel.repaint();
                    break;
                }
            }
        }
    }

    public void clearCards(PlayerInfoView playerInfoView) {
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        cardsPanel.removeAll();
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public void enableDiscardMode(PlayerInfoView playerInfoView, int numCardsToDiscard) {
        // If no need to discard, return directly
        if (numCardsToDiscard <= 0) {
            System.out.println("No need to discard, skipping discard mode"); // Debug info
            return;
        }

        System.out.println("Entering discard mode - Need to discard " + numCardsToDiscard + " cards"); // Debug info
        isDiscardMode = true;
        cardsToDiscard = numCardsToDiscard;
        cardsDiscarded = 0;
        currentDiscardingPlayer = playerInfoView;

        // Enable click events for all cards
        JPanel cardsPanel = playerInfoView.getCardsPanel();
        System.out.println("Current number of components in card panel: " + cardsPanel.getComponentCount()); // Debug info

        for (Component component : cardsPanel.getComponents()) {
            if (component instanceof CardView) {
                CardView cardView = (CardView) component;
                cardView.setEnabled(true);
                cardView.setToolTipText("Click to discard this card");
                System.out.println("Enabling card click event: " + cardView.getCard().getClass().getSimpleName()); // Debug info
            }
        }

        // Show discard prompt
        JOptionPane.showMessageDialog(null,
                "Your hand exceeds 5 cards, please select " + cardsToDiscard + " cards to discard",
                "Discard Phase",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDiscardCard(Card card) {
        System.out.println("Handling discard - Currently discarded " + cardsDiscarded + "/" + cardsToDiscard + " cards"); // Debug info
        if (cardsDiscarded < cardsToDiscard) {
            // Get the player who is discarding
            int discardingPlayerIndex = gameController.getPlayerInfoViews().indexOf(currentDiscardingPlayer);
            Player discardingPlayer = gameController.getPlayers().get(discardingPlayerIndex);

            // Check if it's a special card
            if (card instanceof SandbagCard || card instanceof HelicopterCard) {
                // Create selection window
                String cardType = card instanceof SandbagCard ? "Sandbag Card" : "Helicopter Card";
                int choice = JOptionPane.showOptionDialog(
                        null,
                        "You selected to discard a " + cardType + ", do you want to use its function?",
                        "Special Card Selection",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] { "Use Function", "Discard Directly" },
                        "Use Function");

                if (choice == 0) { // Choose to use function
                    // Record this card as discarded
                    cardsDiscarded++;
                    if (card instanceof SandbagCard) {
                        // If it's a sandbag card, enter sandbag mode
                        gameController.getMapController().enterSandbagMode(discardingPlayerIndex);
                    } else {
                        // If it's a helicopter card, enter helicopter mode
                        gameController.getMapController().enterHelicopterMode(discardingPlayerIndex);
                    }
                    return; // Wait for player to use special card before continuing discard
                } else { // Choose to discard directly
                    // Execute discard operation directly
                    discardingPlayer.getHandCard().removeCard(card);
                    removeCard(currentDiscardingPlayer, card);
                    cardsDiscarded++;
                    gameController.getTreasureDeck().discard(card); // Ensure special card goes to discard pile
                }
            } else {
                // If not a special card, continue normal discard process
                discardingPlayer.getHandCard().removeCard(card);
                removeCard(currentDiscardingPlayer, card);
                cardsDiscarded++;
                gameController.getTreasureDeck().discard(card); // Ensure treasure card goes to discard pile
            }

            // Check if discard is complete
            if (cardsDiscarded == cardsToDiscard) {
                System.out.println("Discard complete, exiting discard mode"); // Debug info
                isDiscardMode = false;
                gameController.updatePlayerView(discardingPlayerIndex);
                // Restore current discarding player's buttons
                currentDiscardingPlayer.setButtonsEnabled(true);
                currentDiscardingPlayer = null;
                // Check if discard was caused by giving card
                if (pendingGiveCardPlayerIndex != null) {
                    int aIndex = pendingGiveCardPlayerIndex;
                    PlayerInfoView aView = gameController.getPlayerInfoView(aIndex);
                    String actionText = aView.getActionPointsLabel().getText();
                    int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
                    aView.setActionPoints(currentActions - 1);
                    currentActions--;
                    if (currentActions <= 0) {
                        gameController.startNewTurn();
                    } else {
                        gameController.resumeGiveCardTurn(aIndex);
                    }
                    pendingGiveCardPlayerIndex = null;
                } else {
                    // Normal discard, switch to new turn
                    gameController.startNewTurn();
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Still need to discard " + (cardsToDiscard - cardsDiscarded) + " cards",
                        "Discard Phase",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Actually execute the card giving operation
     */
    public boolean giveCard(int fromPlayerIndex, int toPlayerIndex, Card card) {
        GameController gc = this.gameController;
        Player fromPlayer = gc.getPlayers().get(fromPlayerIndex);
        Player toPlayer = gc.getPlayers().get(toPlayerIndex);

        // Check conditions
        boolean isMessenger = fromPlayer.getRole() instanceof Model.Role.Messenger;
        boolean sameLocation = fromPlayer.getCurrentTile().equals(toPlayer.getCurrentTile());

        if (!sameLocation && !isMessenger) {
            System.out.println("[Log] Players are not in the same location and not a messenger, cannot give card.");
            return false;
        }

        if (!fromPlayer.getHandCard().getCards().contains(card)) {
            System.out.println("[Log] Giving player does not have this card.");
            return false;
        }

        // Execute transfer
        fromPlayer.removeCard(card);
        toPlayer.getHandCard().addCardWithoutCheck(card);
        // Update view
        gc.updatePlayerView(fromPlayerIndex);
        gc.updatePlayerView(toPlayerIndex);

        // Check if receiver exceeds limit, if so enter discard mode
        int cardCount = toPlayer.getHandCard().getCards().size();
        if (cardCount > 5) {
            int cardsToDiscard = cardCount - 5;
            System.out.println("[Log] Receiver's hand exceeds limit, need to discard " + cardsToDiscard + " cards");
            PlayerInfoView playerView = gc.getPlayerInfoView(toPlayerIndex);
            playerView.setButtonsEnabled(false);
            this.pendingGiveCardPlayerIndex = fromPlayerIndex; // Record A
            this.enableDiscardMode(playerView, cardsToDiscard);
        } else {
            // When not exceeding limit, still consume A's action points
            if (pendingGiveCardPlayerIndex != null) {
                int aIndex = pendingGiveCardPlayerIndex;
                PlayerInfoView aView = gameController.getPlayerInfoView(aIndex);
                String actionText = aView.getActionPointsLabel().getText();
                int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
                aView.setActionPoints(currentActions - 1);
                currentActions--;
                if (currentActions <= 0) {
                    gameController.startNewTurn();
                } else {
                    gameController.resumeGiveCardTurn(aIndex);
                }
                pendingGiveCardPlayerIndex = null;
            }
        }
        return true;
    }

    /**
     * Use sandbag card to shore up specified tile
     * 
     * @param playerIndex Player index
     * @param targetTile Target tile
     * @return Whether shoring up was successful
     */
    public boolean useSandbagCard(int playerIndex, Tile targetTile) {
        Player player = gameController.getPlayers().get(playerIndex);
        Card sandbagCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                sandbagCard = card;
                break;
            }
        }
        if (sandbagCard == null) {
            System.out.println("[Log] Player does not have a sandbag card, cannot shore up");
            return false;
        }
        if (((SandbagCard) sandbagCard).useCard(targetTile)) {
            player.getHandCard().removeCard(sandbagCard);
            gameController.getPlayerInfoView(playerIndex).removeCard(sandbagCard);
            gameController.getTreasureDeck().discard(sandbagCard);
            System.out.println("[Log] Successfully used sandbag card to shore up tile: " + targetTile.getName() + " [Coordinates: " + targetTile.getRow() + ","
                    + targetTile.getCol() + "]");
            return true;
        } else {
            System.out.println("[Log] Failed to shore up with sandbag card");
            return false;
        }
    }

    /**
     * Use helicopter rescue card to try to win
     * 
     * @param playerIndex Player index
     * @return Whether victory was achieved
     */
    public boolean useHelicopterCardForWin(int playerIndex) {
        // 1. Check if all four treasures are collected
        if (!gameController.getTreasureDeck().allTreasuresCollected()) {
            System.out.println("[Log] Treasures not all collected, cannot use helicopter card to win.");
            JOptionPane.showMessageDialog(null, "All treasures must be collected before escaping!");
            return false;
        }
        // 2. Check if all players are at Fool's Landing
        List<Player> players = gameController.getPlayers();
        boolean allAtFoolsLanding = players.stream().allMatch(
                p -> p.getCurrentTile() != null && p.getCurrentTile().getName().name().equals("FOOLS_LANDING"));
        if (!allAtFoolsLanding) {
            System.out.println("[Log] Not all players are at Fool's Landing, cannot use helicopter card to win.");
            JOptionPane.showMessageDialog(null, "All players must be at Fool's Landing to escape!");
            return false;
        }
        // 3. Check if player has helicopter card
        Player player = players.get(playerIndex);
        Card heliCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                heliCard = card;
                break;
            }
        }
        if (heliCard == null) {
            System.out.println("[Log] No helicopter card, cannot win.");
            JOptionPane.showMessageDialog(null, "You don't have a helicopter rescue card!");
            return false;
        }
        // 4. Discard helicopter card
        player.getHandCard().removeCard(heliCard);
        gameController.getPlayerInfoView(playerIndex).removeCard(heliCard);
        gameController.getTreasureDeck().discard(heliCard);

        // 5. Game victory
        System.out.println("[Log] Used helicopter rescue card, all players escaped, game victory!");
        JOptionPane.showMessageDialog(null, "All players escaped by helicopter, game victory!");
        gameController.endGameWithWin();
        return true;
    }

    /**
     * Check if in discard mode
     * 
     * @return true if in discard mode
     */
    public boolean isInDiscardMode() {
        return isDiscardMode;
    }

    /**
     * Continue discard process
     * Call this method after special card use is complete to continue discarding
     */
    public void continueDiscardMode() {
        if (isDiscardMode && currentDiscardingPlayer != null) {
            // Get the player who is discarding
            int discardingPlayerIndex = gameController.getPlayerInfoViews().indexOf(currentDiscardingPlayer);
            Player discardingPlayer = gameController.getPlayers().get(discardingPlayerIndex);

            // Check if need to continue discarding
            if (cardsDiscarded < cardsToDiscard) {
                JOptionPane.showMessageDialog(null,
                        "Still need to discard " + (cardsToDiscard - cardsDiscarded) + " cards",
                        "Discard Phase",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // If no need to continue discarding, end discard mode
                isDiscardMode = false;
                gameController.updatePlayerView(discardingPlayerIndex);
                currentDiscardingPlayer.setButtonsEnabled(true);
                currentDiscardingPlayer = null;
                // Fix: If there is pendingGiveCardPlayerIndex, consume A's action points and determine whether to return to A or switch turns
                if (pendingGiveCardPlayerIndex != null) {
                    int aIndex = pendingGiveCardPlayerIndex;
                    PlayerInfoView aView = gameController.getPlayerInfoView(aIndex);
                    String actionText = aView.getActionPointsLabel().getText();
                    int currentActions = Integer.parseInt(actionText.split(":")[1].trim());
                    aView.setActionPoints(currentActions - 1);
                    currentActions--;
                    if (currentActions <= 0) {
                        gameController.startNewTurn();
                    } else {
                        gameController.resumeGiveCardTurn(aIndex);
                    }
                    pendingGiveCardPlayerIndex = null;
                } else {
                    gameController.startNewTurn();
                }
            }
        }
    }
}