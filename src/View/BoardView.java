package View;

import javax.swing.*;
import java.awt.*;
import Controller.GameController;
import Model.WaterLevel;
import Model.Tile;
import Model.Enumeration.TileName;

/**
 * BoardView class represents the main game board interface.
 * This class manages the overall layout of the game, including the map view,
 * treasure view, water level view, and player information panels.
 * It serves as the central hub for all game UI components.
 */
public class BoardView extends JPanel {
    // Core view components
    private MapView mapView; // The main game map display
    private TreasureView treasureView; // Panel showing treasure collection status
    private WaterLevelView waterLevelView; // Panel showing current water level
    private PlayerInfoView[] playerInfoViews; // Array of player information panels
    
    // Game configuration
    private int playerCount; // Number of players in the current game
    private GameController gameController; // Controller managing game logic
    private int initialWaterLevel; // Initial water level for the game session
    
    /**
     * Constructor for BoardView.
     * Initializes the main game board with specified parameters.
     * 
     * @param playerCount The number of players in the game (2-4)
     * @param mapType The type of map to be used for the game
     * @param initialWaterLevel The starting water level for the game
     */
    public BoardView(int playerCount, String mapType, int initialWaterLevel) {
        this.playerCount = playerCount;
        this.initialWaterLevel = initialWaterLevel; // Store initial water level for game setup
        
        // Create helicopter landing tile (using FOOLS_LANDING as the helicopter landing pad)
        Tile helicopterTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
        
        // Initialize WaterLevelView first as it's needed by GameController
        this.waterLevelView = new WaterLevelView();
        
        // Create and configure MapView with the specified map type
        this.mapView = new MapView();
        this.mapView.setMapType(mapType);
        
        // Initialize GameController with game parameters
        gameController = new GameController(playerCount, helicopterTile, waterLevelView, initialWaterLevel);
        
        // Establish bidirectional references between views and controller
        this.gameController.setMapView(mapView);
        this.gameController.setBoardView(this);
        
        // Initialize player information view array and populate with controller instances
        this.playerInfoViews = new PlayerInfoView[playerCount];
        for (int i = 0; i < playerCount; i++) {
            this.playerInfoViews[i] = gameController.getPlayerInfoViews().get(i);
        }
        
        // Set up the user interface
        initializeUI(mapType);
    }

    /**
     * Initializes the user interface layout and components.
     * Creates a responsive layout that adapts to different screen sizes.
     * 
     * @param mapType The type of map being used (affects UI configuration)
     */
    private void initializeUI(String mapType) {
        // Set main layout manager
        setLayout(new BorderLayout());
        
        // Get screen dimensions for responsive design
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Create main content panel with grid bag layout for flexible positioning
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 10, 3, 10); // Add padding between components
        gbc.fill = GridBagConstraints.BOTH; // Allow components to expand
    
        // Create top control panel with exit functionality
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topControlPanel.setOpaque(false); // Transparent background
        
        // Create and configure exit button
        JButton exitButton = new JButton("Exit Game");
        exitButton.setBackground(new Color(220, 53, 69)); // Bootstrap danger color
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 12));
        exitButton.setFocusPainted(false); // Remove focus border
        
        // Add exit confirmation dialog
        exitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the game?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                // Return to main menu when confirmed
                MainView.getInstance().showStartScreen();
            }
        });
        topControlPanel.add(exitButton);
        
        // Add exit button panel to the top of the board
        add(topControlPanel, BorderLayout.NORTH);
    
        // Configure top player area (Player 1 and Player 2)
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        // Add Player 1 info panel if exists
        if (playerCount > 0) {
            topPanel.add(playerInfoViews[0]);
        } else {
            topPanel.add(new JPanel()); // Empty panel as placeholder
        }
        
        // Add Player 2 info panel if exists
        if (playerCount > 1) {
            topPanel.add(playerInfoViews[1]);
        } else {
            topPanel.add(new JPanel()); // Empty panel as placeholder
        }
        
        // Position top panel in grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // Span across all three columns
        gbc.weightx = 1.0; // Take full width
        gbc.weighty = 0.15; // 15% of vertical space
        contentPanel.add(topPanel, gbc);

        // Configure middle area (treasure status, map, water level meter)
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Reset to single column
        gbc.weighty = 0.7; // 70% of vertical space for main game area

        // Left side: Treasure status panel
        treasureView = new TreasureView();
        gbc.gridx = 0;
        gbc.weightx = 0.15; // 15% of horizontal space
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(treasureView, gbc);

        // Center: Game map with centered positioning
        gbc.gridx = 1;
        gbc.weightx = 0.7; // 70% of horizontal space
        JPanel mapCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        mapCenterPanel.add(mapView);
        contentPanel.add(mapCenterPanel, gbc);

        // Right side: Water level meter
        // Update water level display with initial value
        waterLevelView.updateWaterLevel(initialWaterLevel);
        // Set global reference for water level updates
        WaterLevel.setWaterLevelView(waterLevelView);
        gbc.gridx = 2;
        gbc.weightx = 0.15; // 15% of horizontal space
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(waterLevelView, gbc);

        // Configure bottom player area (Player 3 and Player 4)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        // Add Player 3 info panel if exists
        if (playerCount > 2) {
            bottomPanel.add(playerInfoViews[2]);
        } else {
            bottomPanel.add(new JPanel()); // Empty panel as placeholder
        }
        
        // Add Player 4 info panel if exists
        if (playerCount > 3) {
            bottomPanel.add(playerInfoViews[3]);
        } else {
            bottomPanel.add(new JPanel()); // Empty panel as placeholder
        }
        
        // Position bottom panel in grid
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3; // Span across all three columns
        gbc.weightx = 1.0; // Take full width
        gbc.weighty = 0.15; // 15% of vertical space
        contentPanel.add(bottomPanel, gbc);

        // Add main content panel to the center of the board
        add(contentPanel, BorderLayout.CENTER);

        // Add minimal border padding to the content panel
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    /**
     * Gets the map view component.
     * 
     * @return The MapView instance used by this board
     */
    public MapView getMapView() {
        return mapView;
    }

    /**
     * Gets the treasure view component.
     * 
     * @return The TreasureView instance used by this board
     */
    public TreasureView getTreasureView() {
        return treasureView;
    }

    /**
     * Gets the water level view component.
     * 
     * @return The WaterLevelView instance used by this board
     */
    public WaterLevelView getWaterLevelView() {
        return waterLevelView;
    }

    /**
     * Gets a specific player's information view.
     * 
     * @param index The player index (0-based)
     * @return The PlayerInfoView for the specified player, or null if index is invalid
     */
    public PlayerInfoView getPlayerInfoView(int index) {
        // Validate index to prevent array out of bounds
        if (index >= 0 && index < playerCount) {
            return playerInfoViews[index];
        }
        return null; // Return null for invalid indices
    }
}
