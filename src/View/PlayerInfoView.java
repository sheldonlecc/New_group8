package View;

import Model.Cards.Card;
import Controller.GameController;

import javax.swing.*;
import java.awt.*;

public class PlayerInfoView extends JPanel {
    private JLabel playerNameLabel;
    private JLabel roleLabel;
    private JLabel actionPointsLabel;
    private JPanel cardsPanel;
    private GameController gameController;
    private static final int MAX_CARDS = 7;
    private JButton sandbagButton;
    private JButton helicopterButton;
    private JLabel roleIconLabel;
    private int playerCount; // Add player count field
    private JButton[] actionButtons; // Add this declaration

    public PlayerInfoView(GameController gameController) {
        this.gameController = gameController;
        this.playerCount = gameController.getPlayerCount(); // Get player count
        initializeUI();
        setPlayerName("Player");
    }

    private void initializeUI() {
        setLayout(new BorderLayout(3, 3));
        setBorder(BorderFactory.createTitledBorder(""));
    
        // Dynamically set panel size based on player count
        int panelWidth, panelHeight;
        if (playerCount <= 2) {
            panelWidth = 750;
            panelHeight = 170;
        } else {
            // Reduce width for 3-4 player games
            panelWidth = 750;
            panelHeight = 165; // Slightly reduce height
        }
        
        setMaximumSize(new Dimension(panelWidth, panelHeight));
        setPreferredSize(new Dimension(panelWidth - 20, panelHeight - 20));

        // Left area: Player information
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        // Hide roleLabel and playerNameLabel, only show in title
        roleLabel = new JLabel("Role");
        roleLabel.setVisible(false);

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setVisible(false);

        // Only show action points
        actionPointsLabel = new JLabel("Actions: 3");
        actionPointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(actionPointsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add "Use Sandbag Card" button
        sandbagButton = new JButton("Sandbag");
        sandbagButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sandbagButton.setPreferredSize(new Dimension(90, 25)); // Uniform size
        sandbagButton.setBackground(new Color(255, 204, 102));
        sandbagButton.setFont(new Font("Arial", Font.BOLD, 11)); // Reduce font size
        sandbagButton.setFocusPainted(false);
        sandbagButton.setEnabled(true);

        // Add "Use Helicopter Card" button
        helicopterButton = new JButton("Helicopter");
        helicopterButton.setPreferredSize(new Dimension(90, 25)); // Keep uniform size
        helicopterButton.setBackground(new Color(102, 204, 255));
        helicopterButton.setFont(new Font("Arial", Font.BOLD, 11)); // Reduce font size
        helicopterButton.setFocusPainted(false);
        helicopterButton.setEnabled(true);
        helicopterButton.addActionListener(e -> {
            System.out.println("\n========== Helicopter button clicked ==========");
            System.out.println("Current player index: " + gameController.getPlayerInfoViews().indexOf(this));
            System.out.println("Helicopter button state: " + (helicopterButton.isEnabled() ? "Enabled" : "Disabled"));
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
        JPanel actionButtonsPanel = new JPanel(new GridLayout(3, 2, 2, 2)); // Reduce spacing
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtonsPanel.setMaximumSize(new Dimension(200, 100)); // Increase width

        // Create and add action buttons
        String[] actionNames = { "Move", "Shore up", "Give Cards", "Special", "Treasure", "Skip" }; // Shorten text
        actionButtons = new JButton[actionNames.length];
        for (int i = 0; i < actionNames.length; i++) {
            actionButtons[i] = createActionButton(actionNames[i]);
            actionButtonsPanel.add(actionButtons[i]);
        }

        leftPanel.add(actionButtonsPanel);
        add(leftPanel, BorderLayout.WEST);

        // Middle area: Cards - Adjust card area size based on player count
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

        // Set final size
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(new Dimension(panelWidth, panelHeight));

        // Adjust card area size
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
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
        cardsPanelWidth = (playerCount <= 2) ? 160 : 140;  // Increase from 140/120 to 160/140
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 135));  // Also slightly increase height
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

        // Set final size
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(new Dimension(panelWidth, panelHeight));

        // Adjust card area size
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 120));
    }
    
    public void setPlayerName(String name) {
        playerNameLabel.setText(name);
        updateTitle();
    }
    
    public void setRole(String role) {
        roleLabel.setText(role);
        updateTitle();
        updateRoleIcon(role); // Update role icon
    }
    
    // New method: Update role icon
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
                System.err.println("Failed to load role icon: " + role);
                roleIconLabel.setIcon(null);
                roleIconLabel.setText(role);
            }
        } else {
            roleIconLabel.setIcon(null);
            roleIconLabel.setText("No Role");
        }
    }

    public void setActionPoints(int points) {
        actionPointsLabel.setText("Actions: " + points);
    }

    public JPanel getCardsPanel() {
        return cardsPanel;
    }

    public void addCard(Card card) {
        gameController.getCardController().addCard(this, card);
    }

    public void removeCard(Card card) {
        gameController.getCardController().removeCard(this, card);
    }

    public void clearCards() {
        gameController.getCardController().clearCards(this);
    }

    public JLabel getActionPointsLabel() {
        return actionPointsLabel;
    }

    private void updateTitle() {
        String playerName = playerNameLabel.getText();
        String role = roleLabel.getText();
        if (!role.equals("Role") && !role.equals("Not Assigned")) {
            setBorder(BorderFactory.createTitledBorder(playerName + ": " + role));
        } else {
            setBorder(BorderFactory.createTitledBorder(playerName));
        }
    }

    private JButton createActionButton(String actionName) {
        JButton button = new JButton(actionName);
        button.setPreferredSize(new Dimension(90, 30)); // Uniform size
        button.setFocusPainted(false);
        button.setBackground(new Color(51, 122, 183));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12)); // Reduce font size to fit button
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 109, 164), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4))); // Reduce padding
        button.addActionListener(e -> performAction(actionName));
        return button;
    }

    private void performAction(String actionName) {
        // Get current player's index
        int currentPlayerIndex = gameController.getPlayerInfoViews().indexOf(this);
        // Call GameController's performAction method
        gameController.performAction(currentPlayerIndex, actionName);
    }

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

    public JButton getSandbagButton() {
        return sandbagButton;
    }

    public JButton getHelicopterButton() {
        return helicopterButton;
    }
}
