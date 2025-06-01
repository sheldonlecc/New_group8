package View;

import Model.Cards.Card;
import Controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * PlayerInfoView class represents the UI panel for displaying player information
 * including player name, role, action points, cards, and action buttons.
 * This panel is part of the game's user interface.
 */
public class PlayerInfoView extends JPanel {
    // UI components for player information display
    private JLabel playerNameLabel;
    private JLabel roleLabel;
    private JLabel actionPointsLabel;
    private JPanel cardsPanel;
    private GameController gameController;
    private static final int MAX_CARDS = 7; // Maximum number of cards a player can hold
    private JButton sandbagButton;
    private JButton helicopterButton;
    private JLabel roleIconLabel;
    private int playerCount;
    private JButton[] actionButtons;

    /**
     * Constructor for PlayerInfoView
     * @param gameController The game controller that manages game logic
     */
    public PlayerInfoView(GameController gameController) {
        this.gameController = gameController;
        this.playerCount = gameController.getPlayerCount();
        initializeUI();
        setPlayerName("Player");
    }

    /**
     * Initialize the user interface components and layout
     */
    private void initializeUI() {
        setLayout(new BorderLayout(3, 3));
        setBorder(BorderFactory.createTitledBorder(""));
    
        // Set panel dimensions based on player count
        int panelWidth, panelHeight;
        if (playerCount <= 2) {
            panelWidth = 750;
            panelHeight = 170;
        } else {
            panelWidth = 750;
            panelHeight = 165;
        }
        
        setMaximumSize(new Dimension(panelWidth, panelHeight));
        setPreferredSize(new Dimension(panelWidth - 20, panelHeight - 20));

        // Left panel: Player information area
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        // Hide roleLabel and playerNameLabel, only display in title
        roleLabel = new JLabel("Role");
        roleLabel.setVisible(false);

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setVisible(false);

        // Display only action points
        actionPointsLabel = new JLabel("Actions: 3");
        actionPointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(actionPointsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add "Use Sandbag Card" button
        sandbagButton = new JButton("Sandbag");
        sandbagButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sandbagButton.setPreferredSize(new Dimension(90, 25)); 
        sandbagButton.setBackground(new Color(255, 204, 102));
        sandbagButton.setFont(new Font("Arial", Font.BOLD, 11)); 
        sandbagButton.setFocusPainted(false);
        sandbagButton.setEnabled(true);

        // Add "Use Helicopter Card" button
        helicopterButton = new JButton("Helicopter");
        helicopterButton.setPreferredSize(new Dimension(90, 25)); 
        helicopterButton.setBackground(new Color(102, 204, 255));
        helicopterButton.setFont(new Font("Arial", Font.BOLD, 11));
        helicopterButton.setFocusPainted(false);
        helicopterButton.setEnabled(true);
        helicopterButton.addActionListener(e -> {
            System.out.println("\n========== Helicopter button clicked ==========");
            System.out.println("Current player index: " + gameController.getPlayerInfoViews().indexOf(this));
            System.out.println("Helicopter button status: " + (helicopterButton.isEnabled() ? "Enabled" : "Disabled"));
            System.out.println("Checking helicopter card...");
            gameController.handleHelicopterCard(gameController.getPlayerInfoViews().indexOf(this));
            System.out.println("========== Helicopter button click event ended ==========\n");
        });

        // Create a horizontal layout panel for these two buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(sandbagButton);
        buttonPanel.add(helicopterButton);
        leftPanel.add(buttonPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add action buttons panel
        JPanel actionButtonsPanel = new JPanel(new GridLayout(3, 2, 2, 2)); 
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtonsPanel.setMaximumSize(new Dimension(200, 100));

        // Create and add action buttons
        String[] actionNames = { "Move", "Shore up", "Give Cards", "Special", "Treasure", "Skip" }; 
        actionButtons = new JButton[actionNames.length];
        for (int i = 0; i < actionNames.length; i++) {
            actionButtons[i] = createActionButton(actionNames[i]);
            actionButtonsPanel.add(actionButtons[i]);
        }

        leftPanel.add(actionButtonsPanel);
        add(leftPanel, BorderLayout.WEST);

        // Center area: Cards - Adjust card area size based on player count
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        cardsPanel.setBorder(BorderFactory.createTitledBorder("Cards"));
        cardsPanel.setBackground(new Color(240, 240, 240));
        
        int cardsPanelWidth = (playerCount <= 2) ? 120 : 100; // Reduce card area for 3-4 players
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 100));
        add(cardsPanel, BorderLayout.CENTER);
        
        // Right area: Role icon - Adjust icon area size based on player count
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Role"));
        
        int iconPanelWidth = (playerCount <= 2) ? 90 : 80; // Reduce icon area for 3-4 players
        rightPanel.setPreferredSize(new Dimension(iconPanelWidth, 100));
        
        roleIconLabel = new JLabel();
        roleIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        int iconSize = (playerCount <= 2) ? 60 : 50; // Reduce icon size for 3-4 players
        roleIconLabel.setPreferredSize(new Dimension(iconSize, 80));
        
        rightPanel.add(roleIconLabel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Set final dimensions
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(new Dimension(panelWidth, panelHeight));

        // Adjust card area size with custom painting
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background for cards panel
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(226, 232, 240)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        cardsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Increase card panel width to accommodate larger cards
        cardsPanelWidth = (playerCount <= 2) ? 160 : 140;  
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 135)); 
        add(cardsPanel, BorderLayout.CENTER);
        
        // Right area: Role icon - Adjust icon area size based on player count
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Role"));
        
        iconPanelWidth = (playerCount <= 2) ? 90 : 80; // Reduce icon area for 3-4 players
        rightPanel.setPreferredSize(new Dimension(iconPanelWidth, 100));
        
        roleIconLabel = new JLabel();
        roleIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        iconSize = (playerCount <= 2) ? 60 : 50; // Reduce icon size for 3-4 players
        roleIconLabel.setPreferredSize(new Dimension(iconSize, 80));
        
        rightPanel.add(roleIconLabel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Set final dimensions
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(new Dimension(panelWidth, panelHeight));

        // Adjust card area size
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 120));
    }
    
    /**
     * Set the player name and update the display
     * @param name The player's name
     */
    public void setPlayerName(String name) {
        playerNameLabel.setText(name);
        updateTitle();
    }
    
    /**
     * Set the player's role and update the display
     * @param role The player's role
     */
    public void setRole(String role) {
        roleLabel.setText(role);
        updateTitle();
        updateRoleIcon(role); // Update role icon
    }
    
    /**
     * Update the role icon based on the player's role
     * @param role The player's role
     */
    private void updateRoleIcon(String role) {
        if (role != null && !role.equals("Role") && !role.equals("Not Assigned")) {
            try {
                String iconPath = "src/resources/Player/" + role + "_Icon.png";
                ImageIcon icon = new ImageIcon(iconPath);
                // Scale icon to appropriate size
                Image scaledImage = icon.getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH);
                roleIconLabel.setIcon(new ImageIcon(scaledImage));
                roleIconLabel.setText("");
            } catch (Exception e) {
                System.err.println("Unable to load role icon: " + role);
                roleIconLabel.setIcon(null);
                roleIconLabel.setText(role);
            }
        } else {
            roleIconLabel.setIcon(null);
            roleIconLabel.setText("No Role");
        }
    }

    /**
     * Set the number of action points for the player
     * @param points The number of action points
     */
    public void setActionPoints(int points) {
        actionPointsLabel.setText("Actions: " + points);
    }

    /**
     * Get the cards panel
     * @return The JPanel containing the player's cards
     */
    public JPanel getCardsPanel() {
        return cardsPanel;
    }

    /**
     * Add a card to the player's hand
     * @param card The card to add
     */
    public void addCard(Card card) {
        gameController.getCardController().addCard(this, card);
    }

    /**
     * Remove a card from the player's hand
     * @param card The card to remove
     */
    public void removeCard(Card card) {
        gameController.getCardController().removeCard(this, card);
    }

    /**
     * Clear all cards from the player's hand
     */
    public void clearCards() {
        gameController.getCardController().clearCards(this);
    }

    /**
     * Get the action points label
     * @return The JLabel displaying action points
     */
    public JLabel getActionPointsLabel() {
        return actionPointsLabel;
    }

    /**
     * Update the panel title with player name and role
     */
    private void updateTitle() {
        String playerName = playerNameLabel.getText();
        String role = roleLabel.getText();
        if (!role.equals("Role") && !role.equals("Not Assigned")) {
            setBorder(BorderFactory.createTitledBorder(playerName + ": " + role));
        } else {
            setBorder(BorderFactory.createTitledBorder(playerName));
        }
    }

    /**
     * Create an action button with specified styling
     * @param actionName The name/text of the action button
     * @return The created JButton
     */
    private JButton createActionButton(String actionName) {
        JButton button = new JButton(actionName);
        button.setPreferredSize(new Dimension(90, 30)); 
        button.setFocusPainted(false);
        button.setBackground(new Color(51, 122, 183));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12)); 
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 109, 164), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4))); 
        button.addActionListener(e -> performAction(actionName));
        return button;
    }

    /**
     * Perform the specified action when an action button is clicked
     * @param actionName The name of the action to perform
     */
    private void performAction(String actionName) {
        // Get current player's index
        int currentPlayerIndex = gameController.getPlayerInfoViews().indexOf(this);
        // Call GameController's performAction method
        gameController.performAction(currentPlayerIndex, actionName);
    }

    /**
     * Enable or disable all action buttons
     * @param enabled Whether to enable or disable the buttons
     */
    public void setButtonsEnabled(boolean enabled) {
        if (actionButtons != null) {
            for (JButton button : actionButtons) {
                button.setEnabled(enabled);
                if (enabled) {
                    button.setBackground(new Color(51, 122, 183));
                } else {
                    button.setBackground(new Color(180, 180, 180));
                }
            }
        }
    }

    /**
     * Get the sandbag button
     * @return The sandbag JButton
     */
    public JButton getSandbagButton() {
        return sandbagButton;
    }

    /**
     * Get the helicopter button
     * @return The helicopter JButton
     */
    public JButton getHelicopterButton() {
        return helicopterButton;
    }
}
