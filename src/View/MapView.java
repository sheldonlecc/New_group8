package View;

import Model.Enumeration.TileType;
import Model.Enumeration.TileName;
import Model.Tile;
import Model.TilePosition;
import Model.Enumeration.TileState;
import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * MapView class represents the game board view component.
 * This class manages the visual representation of the game map, including tiles,
 * player positions, and various map configurations (Classic, Advanced, Expert).
 * It extends JPanel to provide a custom UI component for the game board.
 * 
 * Features:
 * - Multiple map configurations (Classic, Advanced, Expert)
 * - Dynamic tile state management and visualization
 * - Player position tracking and display
 * - Helicopter mode for special game mechanics
 * - Tile highlighting system
 * 
 * @author Game Development Team
 * @version 1.0
 */
public class MapView extends JPanel {
    /** 2D array of buttons representing map tiles */
    private JButton[][] mapButtons;
    
    /** 2D array of tile objects containing game logic */
    private Tile[][] tiles;
    
    /** Size of the map grid (6x6) */
    private static final int MAP_SIZE = 6;
    
    /** Gap size between tiles in pixels */
    private static final int GAP_SIZE = 1;
    
    /** Size of each tile button in pixels */
    private static final int BUTTON_SIZE = 100;
    
    /** Utility class for managing tile positions */
    private TilePosition tilePosition;
    
    /** 2D array of layered panes for complex tile rendering */
    private JLayeredPane[][] layeredPanes;
    
    /** Map storing player labels for each tile position */
    private Map<String, List<JLabel>> tilePlayerLabels;
    
    /** Map storing player indices for each tile position */
    private Map<String, List<Integer>> tilePlayers;
    
    /** Map storing fixed positions for each player on tiles */
    private Map<Integer, Point> playerFixedPositions;
    
    /** Flag indicating if helicopter mode is active */
    private boolean isHelicopterMode = false;
    
    /** Index for assigning tile names during initialization */
    private int tileNameIndex = 0;
    
    /** Classic map configuration - cross-shaped layout */
    private static final List<Point> CLASSIC_MAP = Arrays.asList(
            new Point(0, 2), new Point(0, 3),
            new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4),
            new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
            new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4), new Point(3, 5),
            new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4),
            new Point(5, 2), new Point(5, 3));

    /** Advanced map configuration - rectangular layout */
    private static final List<Point> ADVANCED_MAP = Arrays.asList(
            new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4), new Point(1, 5),
            new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
            new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4), new Point(3, 5),
            new Point(4, 0), new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4), new Point(4, 5));

    /** Expert map configuration - irregular layout with gaps */
    private static final List<Point> EXPERT_MAP = Arrays.asList(
            new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 4), new Point(0, 5),
            new Point(1, 0), new Point(1, 2), new Point(1, 3), new Point(1, 5),
            new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
            new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4),
            new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4));

    /** Currently active map configuration */
    private List<Point> currentMapTiles = CLASSIC_MAP;

    /**
     * Constructor for MapView.
     * Initializes all necessary components and sets up the UI.
     */
    public MapView() {
        // Initialize arrays and collections
        this.mapButtons = new JButton[MAP_SIZE][MAP_SIZE];
        this.tiles = new Tile[MAP_SIZE][MAP_SIZE];
        this.layeredPanes = new JLayeredPane[MAP_SIZE][MAP_SIZE];
        this.tilePlayerLabels = new HashMap<>();
        this.tilePlayers = new HashMap<>();
        this.playerFixedPositions = new HashMap<>();
        tilePosition = new TilePosition();

        // Calculate and set preferred dimensions
        int preferredWidth = MAP_SIZE * (BUTTON_SIZE + GAP_SIZE * 2);
        int preferredHeight = MAP_SIZE * (BUTTON_SIZE + GAP_SIZE * 2);
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        setMinimumSize(new Dimension(preferredWidth, preferredHeight));

        // Initialize the user interface
        initializeUI();
    }

    /**
     * Updates the visual representation of a tile based on its current state.
     * This method is called when a tile's state changes.
     * 
     * @param tile The tile whose image needs to be updated
     */
    private void updateTileImage(Tile tile) {
        int row = tile.getRow();
        int col = tile.getCol();
        JButton button = mapButtons[row][col];
        try {
            // Load and scale the tile image based on current state
            ImageIcon icon = new ImageIcon(tile.getImagePath(tile.getState()));
            Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15,
                    Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(image));
            button.setText("");
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
        } catch (Exception e) {
            System.err.println("Cannot load image: " + tile.getImagePath(tile.getState()));
        }
    }

    /**
     * Initializes the user interface components.
     * Creates the grid layout, buttons, tiles, and sets up their relationships.
     */
    private void initializeUI() {
        // Set up grid bag layout for precise positioning
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GAP_SIZE, GAP_SIZE, GAP_SIZE, GAP_SIZE);
        gbc.fill = GridBagConstraints.NONE;

        // Randomly assign location names to tiles
        List<TileName> availableTileNames = new ArrayList<>(Arrays.asList(TileName.values()));
        Collections.shuffle(availableTileNames);
        
        // Create grid of tiles and buttons
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                // Create layered pane for complex rendering (tiles + players)
                layeredPanes[i][j] = new JLayeredPane();
                layeredPanes[i][j].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                layeredPanes[i][j].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                layeredPanes[i][j].setLayout(null);

                // Create tile button
                mapButtons[i][j] = new JButton();
                mapButtons[i][j].setMargin(new Insets(0, 0, 0, 0));
                mapButtons[i][j].setBounds(0, 0, BUTTON_SIZE, BUTTON_SIZE);
                mapButtons[i][j].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                mapButtons[i][j].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

                // Add button to layered pane
                layeredPanes[i][j].add(mapButtons[i][j], JLayeredPane.DEFAULT_LAYER);

                // Determine if this position should have a tile based on current map
                Point currentPoint = new Point(i, j);
                if (currentMapTiles.contains(currentPoint)) {
                    // Create active tile
                    mapButtons[i][j].setEnabled(true);
                    tiles[i][j] = new Tile(availableTileNames.get(tileNameIndex), i, j);
                    tiles[i][j].addOnStateChangeListener(this::updateTileImage);
                    tilePosition.addTilePosition(tiles[i][j].getName().name(), i, j);
                    mapButtons[i][j].setText(tiles[i][j].getName().getDisplayName());

                    // Load and set tile image
                    try {
                        ImageIcon icon = new ImageIcon(tiles[i][j].getImagePath(tiles[i][j].getState()));
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15,
                                Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                        mapButtons[i][j].setHorizontalTextPosition(SwingConstants.CENTER);
                        mapButtons[i][j].setVerticalTextPosition(SwingConstants.BOTTOM);
                    } catch (Exception e) {
                        System.err.println("Cannot load image: " + tiles[i][j].getImagePath(tiles[i][j].getState()));
                    }
                    tileNameIndex++;
                } else {
                    // Create water/sea tile for positions outside the map
                    mapButtons[i][j].setText(TileType.SUNKEN.name());
                    try {
                        ImageIcon icon = new ImageIcon("src/resources/Tiles/Sea.png");
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 10,
                                Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                    } catch (Exception e) {
                        System.err.println("Cannot load image: src/resources/Tiles/Sea.png");
                    }
                }

                // Add layered pane to main panel using grid bag constraints
                gbc.gridx = j;
                gbc.gridy = i;
                add(layeredPanes[i][j], gbc);
            }
        }

        // Set up adjacency relationships between tiles (up, down, left, right)
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Tile tile = tiles[i][j];
                if (tile != null) {
                    // Check all four directions
                    int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
                    for (int[] d : dirs) {
                        int ni = i + d[0], nj = j + d[1];
                        // Add adjacent tile if it exists and is within bounds
                        if (ni >= 0 && ni < MAP_SIZE && nj >= 0 && nj < MAP_SIZE && tiles[ni][nj] != null) {
                            tile.addAdjacentTile(tiles[ni][nj]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the button at the specified row and column.
     * 
     * @param row The row index
     * @param col The column index
     * @return The button at the specified position, or null if out of bounds
     */
    public JButton getButton(int row, int col) {
        if (row >= 0 && row < MAP_SIZE && col >= 0 && col < MAP_SIZE) {
            return mapButtons[row][col];
        }
        return null;
    }

    /**
     * Gets the total number of buttons in the map.
     *
     * @return Total number of buttons
     */
    public int getButtonCount() {
        return MAP_SIZE * MAP_SIZE;
    }

    /**
     * Gets a button by its linear index.
     *
     * @param index Button index (0 to MAP_SIZE*MAP_SIZE-1)
     * @return Corresponding button
     */
    public JButton getButton(int index) {
        int row = index / MAP_SIZE;
        int col = index % MAP_SIZE;
        return getButton(row, col);
    }

    /**
     * Sets the map type and reinitializes the UI accordingly.
     * Supports three map types: CLASSIC, ADVANCED, and EXPERT.
     * 
     * @param mapType The type of map to set ("CLASSIC", "ADVANCED", or "EXPERT")
     */
    public void setMapType(String mapType) {
        // Determine which map configuration to use
        switch (mapType) {
            case "CLASSIC":
                currentMapTiles = CLASSIC_MAP;
                break;
            case "ADVANCED":
                currentMapTiles = ADVANCED_MAP;
                break;
            case "EXPERT":
                currentMapTiles = EXPERT_MAP;
                break;
            default:
                currentMapTiles = CLASSIC_MAP;
        }
        
        // Remove all existing UI components
        removeAll();
        
        // Clear tile position information to prevent duplicate additions
        tilePosition.clear();
        
        // Completely clear the tile array to prevent memory leaks
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                tiles[i][j] = null;
            }
        }
        
        // Reset tile name index for new map
        tileNameIndex = 0;
        
        // Reinitialize the user interface
        initializeUI();
        
        // Refresh the layout and repaint
        revalidate();
        repaint();
    }

    /**
     * Custom layout method to ensure MapView maintains its preferred size.
     */
    @Override
    public void doLayout() {
        // Ensure MapView maintains its preferred size
        Dimension preferredSize = getPreferredSize();
        if (getSize().width != preferredSize.width || getSize().height != preferredSize.height) {
            setSize(preferredSize);
        }
        super.doLayout();
    }

    /**
     * Custom bounds setting method to maintain aspect ratio.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        // Calculate and maintain aspect ratio
        Dimension preferredSize = getPreferredSize();
        double aspectRatio = (double) preferredSize.width / preferredSize.height;

        if (width / aspectRatio <= height) {
            height = (int) (width / aspectRatio);
        } else {
            width = (int) (height * aspectRatio);
        }

        super.setBounds(x, y, width, height);
    }

    /**
     * Gets the tile position utility object.
     * 
     * @return The TilePosition object
     */
    public TilePosition getTilePosition() {
        return tilePosition;
    }

    /**
     * Gets the tile at the specified row and column.
     * 
     * @param row The row index
     * @param col The column index
     * @return The tile at the specified position, or null if out of bounds or no tile exists
     */
    public Tile getTile(int row, int col) {
        if (row >= 0 && row < MAP_SIZE && col >= 0 && col < MAP_SIZE) {
            return tiles[row][col];
        }
        return null;
    }

    /**
     * Gets all tiles currently on the map.
     * 
     * @return List of all non-null tiles
     */
    public List<Tile> getAllTiles() {
        List<Tile> all = new ArrayList<>();
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (tiles[i][j] != null) {
                    all.add(tiles[i][j]);
                }
            }
        }
        return all;
    }

    /**
     * Gets the number of players currently on the specified tile.
     * 
     * @param row Row index
     * @param col Column index
     * @return Number of players on the tile
     */
    public int getPlayerCountOnTile(int row, int col) {
        String tileKey = row + "," + col;
        return tilePlayers.getOrDefault(tileKey, new ArrayList<>()).size();
    }

    /**
     * Gets the fixed position for a player on a tile.
     * Each player has a designated corner position to avoid overlap.
     * 
     * @param playerIndex Player index (0-3)
     * @return Point representing the player's position on the tile
     */
    private Point getPlayerFixedPosition(int playerIndex) {
        return playerFixedPositions.computeIfAbsent(playerIndex, k -> {
            switch (k) {
                case 0: // First player, bottom left
                    return new Point(0, BUTTON_SIZE / 2);
                case 1: // Second player, top left
                    return new Point(0, 0);
                case 2: // Third player, top right
                    return new Point(BUTTON_SIZE / 2, 0);
                case 3: // Fourth player, bottom right
                    return new Point(BUTTON_SIZE / 2, BUTTON_SIZE / 2);
                default:
                    return new Point(0, 0);
            }
        });
    }

    /**
     * Displays a player's image on the specified tile.
     * 
     * @param row Row index of the tile
     * @param col Column index of the tile
     * @param playerImagePath Path to the player's image file
     * @param playerIndex Index of the player (0-3)
     */
    public void showPlayerImage(int row, int col, String playerImagePath, int playerIndex) {
        try {
            // Load and scale the player image
            ImageIcon originalIcon = new ImageIcon(playerImagePath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    BUTTON_SIZE, // Reduce image size to fit on tile
                    BUTTON_SIZE,
                    Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            // Get the player's designated position on the tile
            Point position = getPlayerFixedPosition(playerIndex);

            // Create and position the player label
            JLabel playerLabel = new JLabel(scaledIcon);
            playerLabel.setBounds(
                    position.x,
                    position.y,
                    BUTTON_SIZE / 2,
                    BUTTON_SIZE / 2
            );
            playerLabel.setVisible(true);

            // Update tracking data structures
            String tileKey = row + "," + col;
            List<Integer> players = tilePlayers.computeIfAbsent(tileKey, k -> new ArrayList<>());
            List<JLabel> labels = tilePlayerLabels.computeIfAbsent(tileKey, k -> new ArrayList<>());

            // Add new player to the tile
            players.add(playerIndex);
            labels.add(playerLabel);

            // Add label to the layered pane (above the tile)
            layeredPanes[row][col].add(playerLabel, JLayeredPane.PALETTE_LAYER);
            layeredPanes[row][col].revalidate();
            layeredPanes[row][col].repaint();
        } catch (Exception e) {
            System.err.println("Failed to load player image: " + e.getMessage());
        }
    }

    /**
     * Hides a player's image from the specified tile.
     * 
     * @param row Row index of the tile
     * @param col Column index of the tile
     * @param playerIndex Index of the player to hide
     */
    public void hidePlayerImage(int row, int col, int playerIndex) {
        String tileKey = row + "," + col;
        List<Integer> players = tilePlayers.get(tileKey);
        List<JLabel> labels = tilePlayerLabels.get(tileKey);

        if (players != null && labels != null) {
            // Find the player's index in the tracking lists
            int index = players.indexOf(playerIndex);
            if (index != -1) {
                // Remove the corresponding label from the UI
                JLabel label = labels.remove(index);
                layeredPanes[row][col].remove(label);
                players.remove(index);

                // Clean up empty data structures
                if (players.isEmpty()) {
                    tilePlayers.remove(tileKey);
                    tilePlayerLabels.remove(tileKey);
                }

                // Refresh the display
                layeredPanes[row][col].revalidate();
                layeredPanes[row][col].repaint();
            }
        }
    }

    /**
     * Sets the helicopter mode state.
     * In helicopter mode, all non-sunken tiles are highlighted to show possible landing spots.
     * 
     * @param enabled True to enable helicopter mode, false to disable
     */
    public void setHelicopterMode(boolean enabled) {
        System.out.println("\n========== MapView.setHelicopterMode Start ==========");
        System.out.println("Method called with enabled parameter: " + enabled);
        System.out.println("Current isHelicopterMode: " + isHelicopterMode);
        System.out.println("MapView instance: " + this);
        System.out.println("Number of tiles returned by getAllTiles(): " + getAllTiles().size());
        
        isHelicopterMode = enabled;
        if (enabled) {
            System.out.println("Start highlighting non-sunken tiles");
            int highlightedCount = 0;
            // Highlight all tiles that are not sunken (valid helicopter landing spots)
            for (Tile tile : getAllTiles()) {
                if (tile.getState() != TileState.SUNK) {
                    System.out.println("Highlighting tile: " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "]");
                    highlightTile(tile.getRow(), tile.getCol());
                    highlightedCount++;
                }
            }
            System.out.println("Total highlighted " + highlightedCount + " tiles");
        } else {
            System.out.println("Clear all highlights");
            // Clear all tile highlights
            clearHighlights();
        }
        System.out.println("========== MapView.setHelicopterMode End ==========\n");
        repaint();
    }

    /**
     * Highlights a tile at the specified position.
     * Used for indicating valid moves or special game states.
     * 
     * @param row Row index of the tile to highlight
     * @param col Column index of the tile to highlight
     */
    public void highlightTile(int row, int col) {
        if (row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length) {
            tiles[row][col].setHighlighted(true);
            repaint();
        }
    }

    /**
     * Clears highlights from all tiles on the map.
     * Used when exiting special game modes or resetting the display.
     */
    public void clearHighlights() {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j] != null) {
                    tiles[i][j].setHighlighted(false);
                }
            }
        }
        repaint();
    }
}