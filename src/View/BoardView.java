package View;

import javax.swing.*;
import java.awt.*;
import Controller.GameController;
import Model.WaterLevel;
import Model.Tile;
import Model.Enumeration.TileName;

public class BoardView extends JPanel {
    private MapView mapView;
    private TreasureView treasureView;
    private WaterLevelView waterLevelView;
    private PlayerInfoView[] playerInfoViews;
    private int playerCount;
    private GameController gameController;
    private int initialWaterLevel; // Add member variable to save initial water level

    // Add new constructor in BoardView class
    public BoardView(int playerCount, String mapType, int initialWaterLevel) {
        this.playerCount = playerCount;
        this.initialWaterLevel = initialWaterLevel; // Save initial water level
        // Create helicopter tile (use FOOLS_LANDING as helicopter pad)
        Tile helicopterTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
        
        // Create WaterLevelView instance first
        this.waterLevelView = new WaterLevelView();
        
        // Create and initialize MapView first
        this.mapView = new MapView();
        this.mapView.setMapType(mapType);
        
        // Pass initial water level when creating GameController
        gameController = new GameController(playerCount, helicopterTile, waterLevelView, initialWaterLevel);
        
        // Set MapView to GameController
        this.gameController.setMapView(mapView);
        
        // Set BoardView to GameController
        this.gameController.setBoardView(this);
        
        // Get PlayerInfoView instances from GameController
        this.playerInfoViews = new PlayerInfoView[playerCount];
        for (int i = 0; i < playerCount; i++) {
            this.playerInfoViews[i] = gameController.getPlayerInfoViews().get(i);
        }
        initializeUI(mapType);
    }

    private void initializeUI(String mapType) {
        setLayout(new BorderLayout());
        
        // Create a new panel to contain other components, using GridBagLayout
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 10, 3, 10);
        gbc.fill = GridBagConstraints.BOTH;
    
        // Add exit button to top
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topControlPanel.setOpaque(false);
        JButton exitButton = new JButton("Exit Game");
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 12));
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the game?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                // Return to main menu
                MainView.getInstance().showStartScreen();
            }
        });
        topControlPanel.add(exitButton);
        
        // Add exit button panel to top
        add(topControlPanel, BorderLayout.NORTH);
    
        // Top player area (Player1 and Player2)
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        // Player1
        if (playerCount > 0) {
            topPanel.add(playerInfoViews[0]);
        } else {
            topPanel.add(new JPanel());
        }
        // Player2
        if (playerCount > 1) {
            topPanel.add(playerInfoViews[1]);
        } else {
            topPanel.add(new JPanel());
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15; // Reduced from 0.2 to 0.15
        contentPanel.add(topPanel, gbc);

        // Middle area (treasure status, map, water level) - increase map weight
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.7; // Increased from 0.6 to 0.7

        // Left treasure status - reduce width weight
        treasureView = new TreasureView();
        gbc.gridx = 0;
        gbc.weightx = 0.15; // Reduced from 0.2 to 0.15
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(treasureView, gbc);

        // Middle map - increase width weight
        gbc.gridx = 1;
        gbc.weightx = 0.7; // Increased from 0.6 to 0.7
        JPanel mapCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20)); // Reduce vertical spacing
        mapCenterPanel.add(mapView);
        contentPanel.add(mapCenterPanel, gbc);

        // Right water level - reduce width weight
        waterLevelView.updateWaterLevel(initialWaterLevel); // Modified here
        WaterLevel.setWaterLevelView(waterLevelView);
        gbc.gridx = 2;
        gbc.weightx = 0.15; // Reduced from 0.2 to 0.15
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(waterLevelView, gbc);

        // Bottom player area (Player3 and Player4) - reduce height weight
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        // Player3
        if (playerCount > 2) {
            bottomPanel.add(playerInfoViews[2]);
        } else {
            bottomPanel.add(new JPanel());
        }
        // Player4
        if (playerCount > 3) {
            bottomPanel.add(playerInfoViews[3]);
        } else {
            bottomPanel.add(new JPanel());
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15; // Reduced from 0.2 to 0.15
        contentPanel.add(bottomPanel, gbc);

        // Add contentPanel to main panel
        add(contentPanel, BorderLayout.CENTER);

        // Reduce margins
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    public MapView getMapView() {
        return mapView;
    }

    public TreasureView getTreasureView() {
        return treasureView;
    }

    public WaterLevelView getWaterLevelView() {
        return waterLevelView;
    }

    public PlayerInfoView getPlayerInfoView(int index) {
        if (index >= 0 && index < playerCount) {
            return playerInfoViews[index];
        }
        return null;
    }
}
