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

public class MapView extends JPanel {
    private JButton[][] mapButtons;
    private Tile[][] tiles;
    private static final int MAP_SIZE = 6;
    private static final int GAP_SIZE = 1;
    private static final int BUTTON_SIZE = 100;
    private TilePosition tilePosition;
    private JLayeredPane[][] layeredPanes;
    private Map<String, List<JLabel>> tilePlayerLabels;
    private Map<String, List<Integer>> tilePlayers;
    private Map<Integer, Point> playerFixedPositions;
    private boolean isHelicopterMode = false;
    
    // Add member variable
    private int tileNameIndex = 0;
    
    private static final List<Point> CLASSIC_MAP = Arrays.asList(
            new Point(0, 2), new Point(0, 3),
            new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4),
            new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
            new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4), new Point(3, 5),
            new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4),
            new Point(5, 2), new Point(5, 3));

    private static final List<Point> ADVANCED_MAP = Arrays.asList(
            new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4), new Point(1, 5),
            new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
            new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4), new Point(3, 5),
            new Point(4, 0), new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4), new Point(4, 5));

    private static final List<Point> EXPERT_MAP = Arrays.asList(
            new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 4), new Point(0, 5),
            new Point(1, 0), new Point(1, 2), new Point(1, 3), new Point(1, 5),
            new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
            new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4),
            new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4));


    private List<Point> currentMapTiles = CLASSIC_MAP;

    public MapView() {
        this.mapButtons = new JButton[MAP_SIZE][MAP_SIZE];
        this.tiles = new Tile[MAP_SIZE][MAP_SIZE];
        this.layeredPanes = new JLayeredPane[MAP_SIZE][MAP_SIZE];
        this.tilePlayerLabels = new HashMap<>();
        this.tilePlayers = new HashMap<>();
        this.playerFixedPositions = new HashMap<>();
        tilePosition = new TilePosition();

        // Set preferred size and minimum size
        int preferredWidth = MAP_SIZE * (BUTTON_SIZE + GAP_SIZE * 2);
        int preferredHeight = MAP_SIZE * (BUTTON_SIZE + GAP_SIZE * 2);
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        setMinimumSize(new Dimension(preferredWidth, preferredHeight));

        initializeUI();
    }

    private void updateTileImage(Tile tile) {
        int row = tile.getRow();
        int col = tile.getCol();
        JButton button = mapButtons[row][col];
        try {
            ImageIcon icon = new ImageIcon(tile.getImagePath(tile.getState()));
            Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15,
                    Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(image));
            button.setText("");
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
        } catch (Exception e) {
            System.err.println("Unable to load image: " + tile.getImagePath(tile.getState()));
        }
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GAP_SIZE, GAP_SIZE, GAP_SIZE, GAP_SIZE);
        gbc.fill = GridBagConstraints.NONE;

        // Randomly assign location names
        List<TileName> availableTileNames = new ArrayList<>(Arrays.asList(TileName.values()));
        Collections.shuffle(availableTileNames);
        // Remove this line: int tileNameIndex = 0;
        
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                // Create layered panel
                layeredPanes[i][j] = new JLayeredPane();
                layeredPanes[i][j].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                layeredPanes[i][j].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                layeredPanes[i][j].setLayout(null);

                // Create button
                mapButtons[i][j] = new JButton();
                mapButtons[i][j].setMargin(new Insets(0, 0, 0, 0));
                mapButtons[i][j].setBounds(0, 0, BUTTON_SIZE, BUTTON_SIZE);
                mapButtons[i][j].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                mapButtons[i][j].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

                // Add button to layered panel
                layeredPanes[i][j].add(mapButtons[i][j], JLayeredPane.DEFAULT_LAYER);

                // Set initial state
                Point currentPoint = new Point(i, j);
                if (currentMapTiles.contains(currentPoint)) {
                    mapButtons[i][j].setEnabled(true);
                    tiles[i][j] = new Tile(availableTileNames.get(tileNameIndex), i, j);
                    tiles[i][j].addOnStateChangeListener(this::updateTileImage);
                    tilePosition.addTilePosition(tiles[i][j].getName().name(), i, j);
                    mapButtons[i][j].setText(tiles[i][j].getName().getDisplayName());

                    try {
                        ImageIcon icon = new ImageIcon(tiles[i][j].getImagePath(tiles[i][j].getState()));
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15,
                                Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                        mapButtons[i][j].setHorizontalTextPosition(SwingConstants.CENTER);
                        mapButtons[i][j].setVerticalTextPosition(SwingConstants.BOTTOM);
                    } catch (Exception e) {
                        System.err.println("Unable to load image: " + tiles[i][j].getImagePath(tiles[i][j].getState()));
                    }
                    tileNameIndex++;
                } else {
                    mapButtons[i][j].setText(TileType.SUNKEN.name());
                    try {
                        ImageIcon icon = new ImageIcon("src/resources/Tiles/Sea.png");
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 10,
                                Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                    } catch (Exception e) {
                        System.err.println("Unable to load image: src/resources/Tiles/Sea.png");
                    }
                }

                // Add layered panel using GridBagConstraints
                gbc.gridx = j;
                gbc.gridy = i;
                add(layeredPanes[i][j], gbc);
            }
        }

        // Set adjacent relationships for all tiles (up, down, left, right)
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Tile tile = tiles[i][j];
                if (tile != null) {
                    int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
                    for (int[] d : dirs) {
                        int ni = i + d[0], nj = j + d[1];
                        if (ni >= 0 && ni < MAP_SIZE && nj >= 0 && nj < MAP_SIZE && tiles[ni][nj] != null) {
                            tile.addAdjacentTile(tiles[ni][nj]);
                        }
                    }
                }
            }
        }
    }

    public JButton getButton(int row, int col) {
        if (row >= 0 && row < MAP_SIZE && col >= 0 && col < MAP_SIZE) {
            return mapButtons[row][col];
        }
        return null;
    }

    /**
     * Get total number of buttons
     *
     * @return Total number of buttons
     */
    public int getButtonCount() {
        return MAP_SIZE * MAP_SIZE;
    }

    /**
     * Get button by index
     *
     * @param index Button index
     * @return Corresponding button
     */
    public JButton getButton(int index) {
        int row = index / MAP_SIZE;
        int col = index % MAP_SIZE;
        return getButton(row, col);
    }

    public void setMapType(String mapType) {
        // Set map type
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
        
        // Remove all existing components
        removeAll();
        
        // Clear tile position information to prevent duplicate additions
        tilePosition.clear();
        
        // **Key fix: completely clear tile array**
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                tiles[i][j] = null;
            }
        }
        
        // Reset tile name index
        tileNameIndex = 0;
        
        // Reinitialize UI
        initializeUI();
        
        // Refresh layout
        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        // Ensure MapView maintains its preferred size
        Dimension preferredSize = getPreferredSize();
        if (getSize().width != preferredSize.width || getSize().height != preferredSize.height) {
            setSize(preferredSize);
        }
        super.doLayout();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        // Maintain aspect ratio
        Dimension preferredSize = getPreferredSize();
        double aspectRatio = (double) preferredSize.width / preferredSize.height;

        if (width / aspectRatio <= height) {
            height = (int) (width / aspectRatio);
        } else {
            width = (int) (height * aspectRatio);
        }

        super.setBounds(x, y, width, height);
    }

    public TilePosition getTilePosition() {
        return tilePosition;
    }

    public Tile getTile(int row, int col) {
        if (row >= 0 && row < MAP_SIZE && col >= 0 && col < MAP_SIZE) {
            return tiles[row][col];
        }
        return null;
    }

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
     * Get number of players on specified tile
     * @param row Row
     * @param col Column
     * @return Number of players
     */
    public int getPlayerCountOnTile(int row, int col) {
        String tileKey = row + "," + col;
        return tilePlayers.getOrDefault(tileKey, new ArrayList<>()).size();
    }

    /**
     * Get player's fixed position
     * @param playerIndex Player index
     * @return Fixed position point
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
     * Show player image
     * @param row Row
     * @param col Column
     * @param playerImagePath Player image path
     * @param playerIndex Player index
     */
    public void showPlayerImage(int row, int col, String playerImagePath, int playerIndex) {
        try {
            ImageIcon originalIcon = new ImageIcon(playerImagePath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    BUTTON_SIZE , // Reduce image size
                    BUTTON_SIZE,
                    Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            // Get player's fixed position
            Point position = getPlayerFixedPosition(playerIndex);

            // Create new label and set position
            JLabel playerLabel = new JLabel(scaledIcon);
            playerLabel.setBounds(
                    position.x,
                    position.y,
                    BUTTON_SIZE / 2,
                    BUTTON_SIZE / 2
            );
            playerLabel.setVisible(true);

            // Record player position
            String tileKey = row + "," + col;
            List<Integer> players = tilePlayers.computeIfAbsent(tileKey, k -> new ArrayList<>());
            List<JLabel> labels = tilePlayerLabels.computeIfAbsent(tileKey, k -> new ArrayList<>());

            // Add new player
            players.add(playerIndex);
            labels.add(playerLabel);

            // Add label to layered panel
            layeredPanes[row][col].add(playerLabel, JLayeredPane.PALETTE_LAYER);
            layeredPanes[row][col].revalidate();
            layeredPanes[row][col].repaint();
        } catch (Exception e) {
            System.err.println("Failed to load player image: " + e.getMessage());
        }
    }

    /**
     * Hide player image
     * @param row Row
     * @param col Column
     * @param playerIndex Player index
     */
    public void hidePlayerImage(int row, int col, int playerIndex) {
        String tileKey = row + "," + col;
        List<Integer> players = tilePlayers.get(tileKey);
        List<JLabel> labels = tilePlayerLabels.get(tileKey);

        if (players != null && labels != null) {
            // Find player's index in list
            int index = players.indexOf(playerIndex);
            if (index != -1) {
                // Remove corresponding label
                JLabel label = labels.remove(index);
                layeredPanes[row][col].remove(label);
                players.remove(index);

                // If no players on tile, clean up data
                if (players.isEmpty()) {
                    tilePlayers.remove(tileKey);
                    tilePlayerLabels.remove(tileKey);
                }

                layeredPanes[row][col].revalidate();
                layeredPanes[row][col].repaint();
            }
        }
    }

    public void setHelicopterMode(boolean enabled) {
        System.out.println("\n========== MapView.setHelicopterMode Start ==========");
        System.out.println("Method called, parameter enabled: " + enabled);
        System.out.println("Current isHelicopterMode: " + isHelicopterMode);
        System.out.println("MapView instance: " + this);
        System.out.println("Number of tiles returned by getAllTiles(): " + getAllTiles().size());
        
        isHelicopterMode = enabled;
        if (enabled) {
            System.out.println("Start highlighting unsunken tiles");
            int highlightedCount = 0;
            // Highlight all unsunken tiles
            for (Tile tile : getAllTiles()) {
                if (tile.getState() != TileState.SUNK) {
                    System.out.println("Highlight tile: " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "]");
                    highlightTile(tile.getRow(), tile.getCol());
                    highlightedCount++;
                }
            }
            System.out.println("Total highlighted tiles: " + highlightedCount);
        } else {
            System.out.println("Clear all highlights");
            // Clear all highlights
            clearHighlights();
        }
        System.out.println("========== MapView.setHelicopterMode End ==========\n");
        repaint();
    }

    /**
     * Highlight tile at specified position
     * @param row Row
     * @param col Column
     */
    public void highlightTile(int row, int col) {
        if (row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length) {
            tiles[row][col].setHighlighted(true);
            repaint();
        }
    }

    /**
     * Clear highlights from all tiles
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