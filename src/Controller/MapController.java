package Controller;

import Model.Tile;
import Model.TilePosition;
import Model.Player;
import Model.Enumeration.TileState;
import Model.Role.Role;
import View.MapView;
import Model.Cards.Card;
import Model.Cards.SandbagCard;
import Model.Cards.HelicopterCard;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import java.awt.Dimension;

public class MapController implements ActionListener {
    private final GameController gameController;
    private final MapView mapView;
    private boolean isMoveMode = false;
    private boolean isInShoreUpMode = false;
    private boolean isNavigatorMoveMode = false;
    private int currentPlayerIndex = -1;
    private int targetPlayerIndex = -1; // Target player index in navigator move mode
    private boolean isSandbagMode = false;
    private int sandbagPlayerIndex = -1;
    private boolean isHelicopterMoveMode = false;
    private List<Player> selectedPlayers = null;
    private HelicopterCard helicopterCard = null;
    private boolean isHelicopterMode = false;
    private int helicopterPlayerIndex = -1;
    private boolean isInEmergencyMoveMode = false;
    private int emergencyMovePlayerIndex = -1;
    private List<Tile> emergencyMoveAvailableTiles = null;
    private int navigatorIndex = -1;

    public MapController(GameController gameController, MapView mapView) {
        this.gameController = gameController;
        this.mapView = mapView;
        initializeListeners();
    }

    private void initializeListeners() {
        // Add listeners for all buttons on the map
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                mapView.getButton(i, j).addActionListener(this);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isMoveMode && !isInShoreUpMode && !isNavigatorMoveMode && !isSandbagMode && !isHelicopterMode
                && !isInEmergencyMoveMode) {
            return;
        }

        // Get the position of the clicked button
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (e.getSource() == mapView.getButton(i, j)) {
                    if (isSandbagMode) {
                        // Sandbag reinforcement logic
                        gameController.sandbagShoreUpTile(sandbagPlayerIndex, i, j);
                        exitSandbagMode();
                        return;
                    } else if (isHelicopterMode) {
                        // Helicopter movement logic
                        System.out.println("\n========== Helicopter Mode Tile Click ==========");
                        System.out.println("Click position: [" + i + "," + j + "]");
                        handleHelicopterMove(i, j);
                        return;
                    } else if (isInEmergencyMoveMode) {
                        // Emergency movement logic
                        System.out.println("\n========== Emergency Move Mode Tile Click ==========");
                        System.out.println("Click position: [" + i + "," + j + "]");
                        Tile clickedTile = mapView.getTile(i, j);
                        if (clickedTile != null && emergencyMoveAvailableTiles.contains(clickedTile)) {
                            if (gameController.performEmergencyMove(emergencyMovePlayerIndex, clickedTile)) {
                                exitEmergencyMoveMode();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Please select an available tile!");
                        }
                        return;
                    }
                    handleTileClick(i, j);
                    return;
                }
            }
        }
    }

    public void handleTileClick(int row, int col) {
        if (isInEmergencyMoveMode) {
            Tile clickedTile = mapView.getTile(row, col);
            if (clickedTile != null && emergencyMoveAvailableTiles.contains(clickedTile)) {
                if (gameController.performEmergencyMove(emergencyMovePlayerIndex, clickedTile)) {
                    exitEmergencyMoveMode();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select an available tile!");
            }
            return;
        }

        System.out.println("\n========== Processing Tile Click ==========");
        System.out.printf("Click position: [%d, %d]\n", row, col);

        if (isNavigatorMoveMode) {
            handleNavigatorMoveModeClick(row, col);
            return;
        }

        // Get current player
        Player currentPlayer = gameController.getCurrentPlayer();
        if (currentPlayer == null) {
            System.out.println("Error: Cannot get current player");
            exitMoveMode();
            return;
        }

        // Get target tile
        Tile targetTile = mapView.getTile(row, col);
        if (targetTile == null) {
            System.out.println("Error: Target tile does not exist");
            JOptionPane.showMessageDialog(mapView,
                    "You clicked on the ocean area, please select a valid land tile to move to",
                    "Invalid Move",
                    JOptionPane.WARNING_MESSAGE);
            exitMoveMode();
            return;
        }

        // Execute corresponding operation based on current mode
        if (isMoveMode) {
            // Check if move is legal
            if (!isValidMove(currentPlayer, targetTile)) {
                System.out.println("Illegal move: Target tile is not reachable");
                JOptionPane.showMessageDialog(mapView, "Illegal move: Target tile is not reachable", "Move Error",
                        JOptionPane.ERROR_MESSAGE);
                exitMoveMode();
                return;
            }
            // Execute move
            gameController.movePlayer(currentPlayerIndex, row, col);
            exitMoveMode();
        } else if (isInShoreUpMode) {
            // Check if reinforcement is legal
            if (!gameController.canShoreUpTile(currentPlayerIndex, targetTile)) {
                System.out.println("Illegal reinforcement: Target tile cannot be reinforced");
                JOptionPane.showMessageDialog(mapView, "Illegal reinforcement: Target tile cannot be reinforced",
                        "Reinforcement Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Execute reinforcement
            gameController.shoreUpTile(currentPlayerIndex, row, col);
        }

        System.out.println("========== Tile Click Processing Complete ==========\n");
    }

    private boolean isValidMove(Player player, Tile targetTile) {
        // Check if target tile is sunk
        if (targetTile.getState() == TileState.SUNK) {
            System.out.println("Target tile is sunk, cannot move");
            return false;
        }

        // Get current tile
        Tile currentTile = player.getCurrentTile();
        if (currentTile == null) {
            System.out.println("Cannot get current tile");
            return false;
        }

        // Get role
        Role role = player.getRole();
        if (role == null) {
            System.out.println("Player has no role, cannot move");
            return false;
        }

        // Special handling for Diver
        if (role.getClass().getSimpleName().equals("Diver")) {
            return isDiverReachable(currentTile, targetTile);
        }

        // Check if role can move to this tile
        if (!role.canMoveTo(targetTile)) {
            System.out.println("Current role cannot move to this tile");
            return false;
        }

        // Check if adjacent (considering Explorer's diagonal movement)
        int currentRow = currentTile.getRow();
        int currentCol = currentTile.getCol();
        int targetRow = targetTile.getRow();
        int targetCol = targetTile.getCol();

        // Calculate Manhattan distance
        int distance = Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol);

        // If Explorer, allow diagonal movement (distance of 2)
        if (role instanceof Model.Role.Explorer) {
            return distance <= 2;
        }

        // If Pilot and ability available, can move to any position
        if (role instanceof Model.Role.Pilot && role.canUseAbility()) {
            return true;
        }

        // Other roles can only move to adjacent positions
        return distance == 1;
    }

    // Diver special movement ability: BFS traverse all connected FLOODED/SUNK
    // areas, finally stop at NORMAL or FLOODED
    public boolean isDiverReachable(Tile start, Tile target) {
        if (start == null || target == null)
            return false;
        if (target.getState() == TileState.SUNK)
            return false;
        java.util.Queue<Tile> queue = new java.util.LinkedList<>();
        java.util.Set<Tile> visited = new java.util.HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Tile curr = queue.poll();
            if (curr == target && (curr.getState() == TileState.NORMAL || curr.getState() == TileState.FLOODED)) {
                return true;
            }
            for (Tile adj : curr.getAdjacentTiles()) {
                if (!visited.contains(adj)) {
                    // Can traverse FLOODED and SUNK, can only stop at NORMAL or FLOODED
                    if (adj.getState() == TileState.FLOODED || adj.getState() == TileState.SUNK) {
                        queue.add(adj);
                        visited.add(adj);
                    } else if (adj.getState() == TileState.NORMAL) {
                        // Also add NORMAL to visited to prevent cycles
                        visited.add(adj);
                        if (adj == target)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public void enterMoveMode(int playerIndex) {
        System.out.println("\n========== Entering Move Mode ==========");
        System.out.println("Current player: " + (playerIndex + 1));

        // Get current player and output their position information
        Player currentPlayer = gameController.getCurrentPlayer();
        if (currentPlayer != null) {
            Tile currentTile = currentPlayer.getCurrentTile();
            if (currentTile != null) {
                System.out.printf("Current position: %s [%d, %d]\n",
                        currentTile.getName(),
                        currentTile.getRow(),
                        currentTile.getCol());
            }
        }

        isMoveMode = true;
        currentPlayerIndex = playerIndex;
        System.out.println("========== Move Mode Entered ==========\n");
    }

    private void exitMoveMode() {
        System.out.println("\n========== Exiting Move Mode ==========");
        isMoveMode = false;
        currentPlayerIndex = -1;
        System.out.println("========== Move Mode Exited ==========\n");
    }

    public MapView getMapView() {
        return mapView;
    }

    /**
     * Enter reinforcement mode
     * 
     * @param playerIndex Current player index
     */
    public void enterShoreUpMode(int playerIndex) {
        Player player = gameController.getPlayers().get(playerIndex);
        Tile currentTile = player.getCurrentTile();

        // Check if Explorer
        boolean isExplorer = player.getRole().getClass().getSimpleName().equals("Explorer");

        // Get list of reinforceable tiles
        List<Tile> shoreableTiles = new ArrayList<>();

        // Add current tile
        shoreableTiles.add(currentTile);

        // Add adjacent tiles
        shoreableTiles.addAll(currentTile.getAdjacentTiles());

        // If Explorer, add diagonal tiles
        if (isExplorer) {
            int currentRow = currentTile.getRow();
            int currentCol = currentTile.getCol();

            // Top-left
            if (currentRow > 0 && currentCol > 0) {
                Tile upLeftTile = mapView.getTile(currentRow - 1, currentCol - 1);
                if (upLeftTile != null) {
                    shoreableTiles.add(upLeftTile);
                }
            }
            // Top-right
            if (currentRow > 0 && currentCol < 5) {
                Tile upRightTile = mapView.getTile(currentRow - 1, currentCol + 1);
                if (upRightTile != null) {
                    shoreableTiles.add(upRightTile);
                }
            }
            // Bottom-left
            if (currentRow < 5 && currentCol > 0) {
                Tile downLeftTile = mapView.getTile(currentRow + 1, currentCol - 1);
                if (downLeftTile != null) {
                    shoreableTiles.add(downLeftTile);
                }
            }
            // Bottom-right
            if (currentRow < 5 && currentCol < 5) {
                Tile downRightTile = mapView.getTile(currentRow + 1, currentCol + 1);
                if (downRightTile != null) {
                    shoreableTiles.add(downRightTile);
                }
            }
        }

        // Check if there are reinforceable tiles
        boolean hasShoreable = false;
        for (Tile tile : shoreableTiles) {
            if (tile != null && tile.getState() == TileState.FLOODED) {
                hasShoreable = true;
                break;
            }
        }

        if (!hasShoreable) {
            JOptionPane.showMessageDialog(mapView, "No reinforceable tiles nearby!", "Notice",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        isInShoreUpMode = true;
        isMoveMode = false;
        currentPlayerIndex = playerIndex;
        System.out.println(
                "[Log] Entering reinforcement mode, player " + (playerIndex + 1) + " can select tiles to reinforce");
        highlightShoreableTiles(playerIndex);
    }

    /**
     * Highlight reinforceable tiles
     * 
     * @param playerIndex Current player index
     */
    private void highlightShoreableTiles(int playerIndex) {
        System.out.println("\n========== Starting to Highlight Reinforceable Tiles ==========");
        Player player = gameController.getPlayers().get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        System.out.println("Current player: " + (playerIndex + 1));
        System.out.println("Player role: " + player.getRole().getClass().getSimpleName());
        System.out.println(
                "Current position: " + currentTile.getName() + " [" + currentTile.getRow() + "," + currentTile.getCol()
                        + "]");

        // Check if has sandbag card
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }
        System.out.println("Has sandbag card: " + hasSandbag);

        // Reset all button states
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true); // Keep all buttons visible
                button.setBackground(null); // Reset background color
            }
        }

        if (hasSandbag) {
            // If has sandbag card, highlight all flooded tiles
            System.out.println("Using sandbag mode, highlighting all flooded tiles");
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Tile tile = mapView.getTile(i, j);
                    if (tile != null && tile.getState() == TileState.FLOODED) {
                        JButton button = mapView.getButton(i, j);
                        if (button != null) {
                            button.setBackground(new Color(255, 255, 200)); // Light yellow highlight
                            System.out
                                    .println("Highlighting flooded tile: " + tile.getName() + " [" + i + "," + j + "]");
                        }
                    }
                }
            }
        } else {
            // If no sandbag card, highlight reinforceable tiles based on role type
            List<Tile> adjacentTiles = new ArrayList<>();

            // Get adjacent tiles of current tile
            int currentRow = currentTile.getRow();
            int currentCol = currentTile.getCol();

            // Check if Explorer
            boolean isExplorer = player.getRole().getClass().getSimpleName().equals("Explorer");
            System.out.println("Is Explorer: " + isExplorer);

            // Add tiles in four directions
            if (currentRow > 0) {
                Tile upTile = mapView.getTile(currentRow - 1, currentCol);
                if (upTile != null) {
                    adjacentTiles.add(upTile);
                    System.out.println("Adding upper tile: " + upTile.getName() + " [" + (currentRow - 1) + ","
                            + currentCol + "]");
                }
            }
            if (currentRow < 5) {
                Tile downTile = mapView.getTile(currentRow + 1, currentCol);
                if (downTile != null) {
                    adjacentTiles.add(downTile);
                    System.out.println("Adding lower tile: " + downTile.getName() + " [" + (currentRow + 1) + ","
                            + currentCol + "]");
                }
            }
            if (currentCol > 0) {
                Tile leftTile = mapView.getTile(currentRow, currentCol - 1);
                if (leftTile != null) {
                    adjacentTiles.add(leftTile);
                    System.out.println("Adding left tile: " + leftTile.getName() + " [" + currentRow + ","
                            + (currentCol - 1) + "]");
                }
            }
            if (currentCol < 5) {
                Tile rightTile = mapView.getTile(currentRow, currentCol + 1);
                if (rightTile != null) {
                    adjacentTiles.add(rightTile);
                    System.out.println("Adding right tile: " + rightTile.getName() + " [" + currentRow + ","
                            + (currentCol + 1) + "]");
                }
            }

            // If Explorer, add diagonal tiles
            if (isExplorer) {
                System.out.println("Explorer mode, adding diagonal tiles");
                // Top-left
                if (currentRow > 0 && currentCol > 0) {
                    Tile upLeftTile = mapView.getTile(currentRow - 1, currentCol - 1);
                    if (upLeftTile != null) {
                        adjacentTiles.add(upLeftTile);
                        System.out
                                .println("Adding top-left tile: " + upLeftTile.getName() + " [" + (currentRow - 1) + ","
                                        + (currentCol - 1) + "]");
                    }
                }
                // Top-right
                if (currentRow > 0 && currentCol < 5) {
                    Tile upRightTile = mapView.getTile(currentRow - 1, currentCol + 1);
                    if (upRightTile != null) {
                        adjacentTiles.add(upRightTile);
                        System.out.println(
                                "Adding top-right tile: " + upRightTile.getName() + " [" + (currentRow - 1) + ","
                                        + (currentCol + 1) + "]");
                    }
                }
                // Bottom-left
                if (currentRow < 5 && currentCol > 0) {
                    Tile downLeftTile = mapView.getTile(currentRow + 1, currentCol - 1);
                    if (downLeftTile != null) {
                        adjacentTiles.add(downLeftTile);
                        System.out.println(
                                "Adding bottom-left tile: " + downLeftTile.getName() + " [" + (currentRow + 1) + ","
                                        + (currentCol - 1) + "]");
                    }
                }
                // Bottom-right
                if (currentRow < 5 && currentCol < 5) {
                    Tile downRightTile = mapView.getTile(currentRow + 1, currentCol + 1);
                    if (downRightTile != null) {
                        adjacentTiles.add(downRightTile);
                        System.out.println(
                                "Adding bottom-right tile: " + downRightTile.getName() + " [" + (currentRow + 1) + ","
                                        + (currentCol + 1) + "]");
                    }
                }
            }

            // Add current tile
            adjacentTiles.add(currentTile);
            System.out.println(
                    "Adding current tile: " + currentTile.getName() + " [" + currentRow + "," + currentCol + "]");

            // Highlight reinforceable tiles
            System.out.println("\nStarting to highlight reinforceable tiles:");
            boolean hasShoreableTile = false;
            for (Tile tile : adjacentTiles) {
                if (tile != null && tile.getState() == TileState.FLOODED) {
                    JButton button = mapView.getButton(tile.getRow(), tile.getCol());
                    if (button != null) {
                        button.setBackground(new Color(255, 255, 200)); // Light yellow highlight
                        hasShoreableTile = true;
                        System.out.println("Highlighting reinforceable tile: " + tile.getName() + " [" + tile.getRow()
                                + "," + tile.getCol()
                                + "] State: " + tile.getState());
                    }
                } else {
                    System.out.println("Tile not reinforceable: " + (tile != null ? tile.getName() : "null") +
                            " [" + (tile != null ? tile.getRow() : "N/A") + "," + (tile != null ? tile.getCol() : "N/A")
                            + "]" +
                            " State: " + (tile != null ? tile.getState() : "null"));
                }
            }

            if (!hasShoreableTile) {
                System.out.println("No reinforceable tiles");
                JOptionPane.showMessageDialog(mapView, "No reinforceable tiles nearby!", "Notice",
                        JOptionPane.INFORMATION_MESSAGE);
                exitShoreUpMode();
                return;
            }
        }

        // Disable non-reinforceable tiles
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
        System.out.println("========== Finished Highlighting Reinforceable Tiles ==========\n");
    }

    /**
     * Exit reinforcement mode
     */
    public void exitShoreUpMode() {
        isInShoreUpMode = false;
        currentPlayerIndex = -1;

        // Reset all button states
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            button.setEnabled(true);
            button.setBackground(null); // Restore default background color
        }
    }

    /**
     * Enter navigator move mode
     * 
     * @param navigatorIndex    Navigator player index
     * @param targetPlayerIndex Target player index
     */
    public void enterNavigatorMoveMode(int navigatorIndex, int targetPlayerIndex) {
        this.navigatorIndex = navigatorIndex;
        this.targetPlayerIndex = targetPlayerIndex;
        this.isNavigatorMoveMode = true;
        System.out.println("[Log] Entering navigator move mode, Navigator: " + (navigatorIndex + 1)
                + ", Target player: " + (targetPlayerIndex + 1));
        // Don't do BFS highlighting, just reset all button states
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }
    }

    /**
     * Exit navigator move mode
     */
    public void exitNavigatorMoveMode() {
        this.isNavigatorMoveMode = false;
        this.navigatorIndex = -1;
        this.targetPlayerIndex = -1;
        System.out.println("[Log] Exiting navigator move mode");
    }

    /**
     * Handle click events in navigator move mode
     * 
     * @param row Clicked row
     * @param col Clicked column
     */
    private void handleNavigatorMoveModeClick(int row, int col) {
        if (!isNavigatorMoveMode || navigatorIndex == -1 || targetPlayerIndex == -1) {
            return;
        }

        // Get target player
        Player targetPlayer = gameController.getPlayers().get(targetPlayerIndex);
        if (targetPlayer == null) {
            return;
        }

        // Check if target player is Pilot
        boolean isPilot = targetPlayer.getRole() instanceof Model.Role.Pilot;
        if (isPilot) {
            gameController.moveOtherPlayer(navigatorIndex, targetPlayerIndex, row, col);
            return;
        }

        // Check if target player is Diver
        boolean isDiver = targetPlayer.getRole().getClass().getSimpleName().equals("Diver");
        Tile currentTile = targetPlayer.getCurrentTile();
        if (currentTile == null) {
            return;
        }
        Tile targetTile = mapView.getTile(row, col);
        if (isDiver) {
            // Allow clicking all BFS reachable tiles
            if (isDiverReachable(currentTile, targetTile)) {
                gameController.moveOtherPlayer(navigatorIndex, targetPlayerIndex, row, col);
            } else {
                JOptionPane.showMessageDialog(null, "Diver can only move to connected reachable tiles!", "Move Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // Check if target player is Explorer
        boolean isExplorer = targetPlayer.getRole() instanceof Model.Role.Explorer;
        int rowDistance = Math.abs(currentTile.getRow() - row);
        int colDistance = Math.abs(currentTile.getCol() - col);
        if (isExplorer) {
            if (rowDistance <= 1 && colDistance <= 1) {
                gameController.moveOtherPlayer(navigatorIndex, targetPlayerIndex, row, col);
            } else {
                System.out.println("[Log] Illegal move: Explorer can only move to eight adjacent tiles");
                JOptionPane.showMessageDialog(null, "Illegal move: Explorer can only move to eight adjacent tiles",
                        "Move Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // Other players can only move to adjacent tiles
        if ((rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1)) {
            gameController.moveOtherPlayer(navigatorIndex, targetPlayerIndex, row, col);
        } else {
            System.out.println("[Log] Illegal move: Can only move to adjacent tiles");
            JOptionPane.showMessageDialog(null, "Illegal move: Can only move to adjacent tiles", "Move Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void enterSandbagMode(int playerIndex) {
        isSandbagMode = true;
        sandbagPlayerIndex = playerIndex;
        isMoveMode = false;
        isInShoreUpMode = false;
        currentPlayerIndex = playerIndex;
        System.out.println("[Log] Entering sandbag reinforcement mode, player " + (playerIndex + 1)
                + " can select any flooded tile");
        // Highlight all flooded tiles
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Tile tile = mapView.getTile(i, j);
                if (tile != null && tile.getState() == TileState.FLOODED) {
                    JButton button = mapView.getButton(i, j);
                    if (button != null) {
                        button.setBackground(new Color(255, 255, 200));
                    }
                }
            }
        }
        // Disable non-reinforceable tiles
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
    }

    public void exitSandbagMode() {
        isSandbagMode = false;
        sandbagPlayerIndex = -1;
        // Reset all button states
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }
        // Continue discard process
        if (gameController.getCardController().isInDiscardMode()) {
            gameController.getCardController().continueDiscardMode();
        }
    }

    /**
     * Enter helicopter move mode
     * 
     * @param currentPlayerIndex Current player index
     * @param selectedPlayers    Selected players list
     * @param card               Helicopter card
     */
    public void enterHelicopterMoveMode(int currentPlayerIndex, List<Player> selectedPlayers, HelicopterCard card) {
        System.out.println("\n========== Entering Helicopter Move Mode ==========");
        System.out.println("Current player: " + (currentPlayerIndex + 1));
        System.out.println("Number of selected players: " + selectedPlayers.size());

        isHelicopterMoveMode = true;
        isMoveMode = false;
        isInShoreUpMode = false;
        isNavigatorMoveMode = false;
        isSandbagMode = false;
        this.currentPlayerIndex = currentPlayerIndex;
        this.selectedPlayers = selectedPlayers;
        this.helicopterCard = card;

        // Highlight movable areas (all non-sunk tiles)
        highlightHelicopterMovableTiles();
        System.out.println("========== Helicopter Move Mode Entered ==========\n");
    }

    /**
     * Highlight helicopter movable areas
     */
    private void highlightHelicopterMovableTiles() {
        // Reset all button states
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }

        // Highlight all non-sunk tiles
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Tile tile = mapView.getTile(i, j);
                if (tile != null && tile.getState() != TileState.SUNK) {
                    JButton button = mapView.getButton(i, j);
                    if (button != null) {
                        button.setBackground(new Color(200, 255, 200)); // Light green highlight
                    }
                }
            }
        }

        // Disable non-movable areas
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
    }

    /**
     * Exit helicopter move mode
     */
    private void exitHelicopterMoveMode() {
        System.out.println("\n========== Exiting Helicopter Move Mode ==========");
        isHelicopterMoveMode = false;
        currentPlayerIndex = -1;
        selectedPlayers = null;
        helicopterCard = null;
        System.out.println("========== Helicopter Move Mode Exited ==========\n");
    }

    /**
     * Handle helicopter movement
     * 
     * @param row Target row
     * @param col Target column
     */
    private void handleHelicopterMove(int row, int col) {
        // New: If all players are at Fool's Landing and treasures are collected, using
        // helicopter card means immediate victory
        List<Player> allPlayers = gameController.getPlayers();
        boolean allAtFoolsLanding = allPlayers.stream().allMatch(
                p -> p.getCurrentTile() != null && p.getCurrentTile().getName().name().equals("FOOLS_LANDING"));
        if (gameController.getTreasureDeck().allTreasuresCollected() && allAtFoolsLanding) {
            int currentPlayerIndex = helicopterPlayerIndex;
            gameController.getCardController().useHelicopterCardForWin(currentPlayerIndex);
            exitHelicopterMode();
            return;
        }
        System.out.println("\n========== Processing Helicopter Movement ==========");
        System.out.println("Target position: [" + row + "," + col + "]");
        System.out.println("Successfully received tile click event");

        Tile targetTile = mapView.getTile(row, col);
        if (targetTile == null || targetTile.getState() == TileState.SUNK) {
            System.out.println("Target tile invalid or sunk");
            JOptionPane.showMessageDialog(null, "Cannot move to a sunk tile!");
            return;
        }

        // Get current player
        Player currentPlayer = gameController.getPlayers().get(helicopterPlayerIndex);
        if (currentPlayer == null) {
            System.out.println("Cannot get current player");
            return;
        }

        // Find player's helicopter card
        HelicopterCard helicopterCard = null;
        for (Card card : currentPlayer.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                helicopterCard = (HelicopterCard) card;
                break;
            }
        }

        if (helicopterCard == null) {
            System.out.println("Player does not have helicopter card");
            JOptionPane.showMessageDialog(null, "You don't have a helicopter card!");
            return;
        }

        // Check if there are other players on current tile
        List<Player> playersOnCurrentTile = new ArrayList<>();
        Tile currentTile = currentPlayer.getCurrentTile();
        for (Player player : gameController.getPlayers()) {
            if (player != currentPlayer && player.getCurrentTile() == currentTile) {
                playersOnCurrentTile.add(player);
            }
        }

        // If there are other players on current tile, let player choose who to move
        // with
        if (!playersOnCurrentTile.isEmpty()) {
            // Create options array
            String[] options = new String[playersOnCurrentTile.size()];
            for (int i = 0; i < playersOnCurrentTile.size(); i++) {
                Player player = playersOnCurrentTile.get(i);
                options[i] = player.getRole().getClass().getSimpleName();
            }

            // Create multi-select list
            JList<String> list = new JList<>(options);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            // Create scroll pane
            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setPreferredSize(new Dimension(200, 100));

            // Show dialog
            int result = JOptionPane.showConfirmDialog(null,
                    scrollPane,
                    "Select players to move with",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Get selected players
                List<Player> selectedPlayers = new ArrayList<>();
                for (int index : list.getSelectedIndices()) {
                    selectedPlayers.add(playersOnCurrentTile.get(index));
                }

                // Move selected players to target position
                for (Player player : selectedPlayers) {
                    // Remove player image from original position
                    mapView.hidePlayerImage(currentTile.getRow(), currentTile.getCol(),
                            gameController.getPlayers().indexOf(player));

                    // Update player position
                    player.setCurrentTile(targetTile);

                    // Show player at new position
                    String roleName = player.getRole().getClass().getSimpleName().toLowerCase();
                    String playerImagePath = "src/resources/Player/" + roleName + "2.png";
                    mapView.showPlayerImage(targetTile.getRow(), targetTile.getCol(),
                            playerImagePath, gameController.getPlayers().indexOf(player));
                }
            }
        }

        System.out.println("Starting to move player...");
        // Get player's current position
        if (currentTile != null) {
            System.out.println("Player's current position: " + currentTile.getName() + " [" + currentTile.getRow() + ","
                    + currentTile.getCol() + "]");
            // Remove player from current position
            mapView.hidePlayerImage(currentTile.getRow(), currentTile.getCol(), helicopterPlayerIndex);
            System.out.println("Removed player image from original position");
        }

        // Update player position
        currentPlayer.setCurrentTile(targetTile);
        System.out.println(
                "Player's new position: " + targetTile.getName() + " [" + targetTile.getRow() + ","
                        + targetTile.getCol() + "]");

        // Show player at new position
        String roleName = currentPlayer.getRole().getClass().getSimpleName().toLowerCase();
        String playerImagePath = "src/resources/Player/" + roleName + "2.png";
        mapView.showPlayerImage(targetTile.getRow(), targetTile.getCol(), playerImagePath, helicopterPlayerIndex);
        System.out.println("Showed player image at new position");

        // Use helicopter card
        System.out.println("Starting to consume helicopter card...");
        currentPlayer.getHandCard().removeCard(helicopterCard);
        gameController.getPlayerInfoView(helicopterPlayerIndex).removeCard(helicopterCard);
        gameController.getTreasureDeck().discard(helicopterCard);
        System.out.println("Helicopter card consumed");

        // Exit helicopter mode
        exitHelicopterMode();

        // Reset all button states
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }

        // Show movement success message
        JOptionPane.showMessageDialog(null,
                "Helicopter movement successful!",
                "Movement Complete",
                JOptionPane.INFORMATION_MESSAGE);

        System.out.println("Helicopter movement complete");
        System.out.println("========== Helicopter Movement Processing Complete ==========\n");
    }

    public void enterHelicopterMode(int playerIndex) {
        System.out.println("\n========== Entering Helicopter Mode ==========");
        System.out.println("Player index: " + playerIndex);
        System.out.println("MapView status: " + (mapView != null ? "Initialized" : "Not initialized"));

        // Check if player has helicopter card
        Player player = gameController.getPlayers().get(playerIndex);
        boolean hasHelicopterCard = false;
        HelicopterCard helicopterCard = null;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                hasHelicopterCard = true;
                helicopterCard = (HelicopterCard) card;
                break;
            }
        }

        if (hasHelicopterCard) {
            System.out.println("Player has helicopter card, successfully entered helicopter mode");
            isHelicopterMode = true;
            helicopterPlayerIndex = playerIndex;
            mapView.setHelicopterMode(true);

            // Show helicopter mode entry message
            JOptionPane.showMessageDialog(null,
                    "Entered helicopter mode, please select target tile.\nIf there are other players on the target tile, you can choose whether to move with them.",
                    "Helicopter Mode",
                    JOptionPane.INFORMATION_MESSAGE);

            System.out.println("Waiting for player to select target tile...");
        } else {
            System.out.println("Player does not have helicopter card, cannot enter helicopter mode");
            JOptionPane.showMessageDialog(null, "You don't have a helicopter card!");
            return;
        }

        System.out.println("Helicopter mode status: " + isHelicopterMode);
        System.out.println("Helicopter player index: " + helicopterPlayerIndex);
        System.out.println("========== Helicopter Mode Entry Complete ==========\n");
    }

    public void exitHelicopterMode() {
        isHelicopterMode = false;
        helicopterPlayerIndex = -1;
        mapView.setHelicopterMode(false);
        // Continue discard process
        if (gameController.getCardController().isInDiscardMode()) {
            gameController.getCardController().continueDiscardMode();
        }
    }

    /**
     * Enter emergency move mode
     * 
     * @param playerIndex    Player index that needs to move
     * @param availableTiles List of available target tiles
     */
    public void enterEmergencyMoveMode(int playerIndex, List<Tile> availableTiles) {
        isInEmergencyMoveMode = true;
        emergencyMovePlayerIndex = playerIndex;
        emergencyMoveAvailableTiles = availableTiles;

        // Highlight available target tiles
        for (Tile tile : availableTiles) {
            mapView.highlightTile(tile.getRow(), tile.getCol());
        }
    }

    /**
     * Exit emergency move mode
     */
    public void exitEmergencyMoveMode() {
        isInEmergencyMoveMode = false;
        emergencyMovePlayerIndex = -1;
        emergencyMoveAvailableTiles = null;
        // Clear all highlights
        mapView.clearHighlights();
        // Notify GameController to continue processing next emergency move
        gameController.processNextEmergencyMove();
    }
}