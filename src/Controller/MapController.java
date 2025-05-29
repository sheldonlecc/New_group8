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
    private int targetPlayerIndex = -1; // 领航员移动模式下的目标玩家索引
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

    public MapController(GameController gameController, MapView mapView) {
        this.gameController = gameController;
        this.mapView = mapView;
        initializeListeners();
    }

    private void initializeListeners() {
        // 为地图上的所有按钮添加监听器
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                mapView.getButton(i, j).addActionListener(this);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isMoveMode && !isInShoreUpMode && !isNavigatorMoveMode && !isSandbagMode && !isHelicopterMode && !isInEmergencyMoveMode) {
            return;
        }

        // 获取被点击的按钮位置
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (e.getSource() == mapView.getButton(i, j)) {
                    if (isSandbagMode) {
                        // 沙袋卡加固逻辑
                        gameController.sandbagShoreUpTile(sandbagPlayerIndex, i, j);
                        exitSandbagMode();
                        return;
                    } else if (isHelicopterMode) {
                        // 直升机移动逻辑
                        System.out.println("\n========== 直升机模式板块点击 ==========");
                        System.out.println("点击位置: [" + i + "," + j + "]");
                        handleHelicopterMove(i, j);
                        return;
                    } else if (isInEmergencyMoveMode) {
                        // 紧急移动逻辑
                        System.out.println("\n========== 紧急移动模式板块点击 ==========");
                        System.out.println("点击位置: [" + i + "," + j + "]");
                        Tile clickedTile = mapView.getTile(i, j);
                        if (clickedTile != null && emergencyMoveAvailableTiles.contains(clickedTile)) {
                            if (gameController.performEmergencyMove(emergencyMovePlayerIndex, clickedTile)) {
                                exitEmergencyMoveMode();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "请选择一个可用的板块！");
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
                JOptionPane.showMessageDialog(null, "请选择一个可用的板块！");
            }
            return;
        }

        System.out.println("\n========== 处理板块点击 ==========");
        System.out.printf("点击位置: [%d, %d]\n", row, col);

        if (isNavigatorMoveMode) {
            // 处理领航员移动其他玩家
            gameController.moveOtherPlayer(currentPlayerIndex, targetPlayerIndex, row, col);
            return;
        }

        // 获取当前玩家
        Player currentPlayer = gameController.getCurrentPlayer();
        if (currentPlayer == null) {
            System.out.println("错误：无法获取当前玩家");
            exitMoveMode();
            return;
        }

        // 获取目标板块
        Tile targetTile = mapView.getTile(row, col);
        if (targetTile == null) {
            System.out.println("错误：目标板块不存在");
            JOptionPane.showMessageDialog(mapView,
                    "您点击了海洋区域，请选择有效的陆地板块进行移动",
                    "无效移动",
                    JOptionPane.WARNING_MESSAGE);
            exitMoveMode();
            return;
        }

        // 根据当前模式执行相应的操作
        if (isMoveMode) {
            // 检查移动是否合法
            if (!isValidMove(currentPlayer, targetTile)) {
                System.out.println("非法移动：目标板块不可到达");
                JOptionPane.showMessageDialog(mapView, "非法移动：目标板块不可到达", "移动错误", JOptionPane.ERROR_MESSAGE);
                exitMoveMode();
                return;
            }
            // 执行移动
            gameController.movePlayer(currentPlayerIndex, row, col);
            exitMoveMode();
        } else if (isInShoreUpMode) {
            // 检查加固是否合法
            if (!gameController.canShoreUpTile(currentPlayerIndex, targetTile)) {
                System.out.println("非法加固：目标板块不可加固");
                JOptionPane.showMessageDialog(mapView, "非法加固：目标板块不可加固", "加固错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 执行加固
            gameController.shoreUpTile(currentPlayerIndex, row, col);
        }

        System.out.println("========== 板块点击处理完成 ==========\n");
    }

    private boolean isValidMove(Player player, Tile targetTile) {
        // 检查目标板块是否被沉没
        if (targetTile.getState() == TileState.SUNK) {
            System.out.println("目标板块已沉没，无法移动");
            return false;
        }

        // 获取当前板块
        Tile currentTile = player.getCurrentTile();
        if (currentTile == null) {
            System.out.println("无法获取当前板块");
            return false;
        }

        // 获取角色
        Role role = player.getRole();
        if (role == null) {
            System.out.println("玩家没有角色，无法移动");
            return false;
        }

        // Diver特殊处理
        if (role.getClass().getSimpleName().equals("Diver")) {
            return isDiverReachable(currentTile, targetTile);
        }

        // 检查角色是否可以移动到该瓦片
        if (!role.canMoveTo(targetTile)) {
            System.out.println("当前角色无法移动到该板块");
            return false;
        }

        // 检查是否相邻（考虑探险家的斜向移动）
        int currentRow = currentTile.getRow();
        int currentCol = currentTile.getCol();
        int targetRow = targetTile.getRow();
        int targetCol = targetTile.getCol();

        // 计算曼哈顿距离
        int distance = Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol);

        // 如果是探险家，允许斜向移动（距离为2）
        if (role instanceof Model.Role.Explorer) {
            return distance <= 2;
        }

        // 如果是飞行员且能力可用，可以移动到任何位置
        if (role instanceof Model.Role.Pilot && role.canUseAbility()) {
            return true;
        }

        // 其他角色只能移动到相邻位置
        return distance == 1;
    }

    // Diver特殊移动能力：BFS遍历所有连通的FLOODED/SUNK区域，最终停在NORMAL或FLOODED
    private boolean isDiverReachable(Tile start, Tile target) {
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
                    // 可以穿越FLOODED和SUNK，最终只能停在NORMAL或FLOODED
                    if (adj.getState() == TileState.FLOODED || adj.getState() == TileState.SUNK) {
                        queue.add(adj);
                        visited.add(adj);
                    } else if (adj.getState() == TileState.NORMAL) {
                        // 也要把NORMAL加入visited，防止环路
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
        System.out.println("\n========== 进入移动模式 ==========");
        System.out.println("当前玩家: " + (playerIndex + 1));

        // 获取当前玩家并输出其位置信息
        Player currentPlayer = gameController.getCurrentPlayer();
        if (currentPlayer != null) {
            Tile currentTile = currentPlayer.getCurrentTile();
            if (currentTile != null) {
                System.out.printf("当前位置: %s [%d, %d]\n",
                        currentTile.getName(),
                        currentTile.getRow(),
                        currentTile.getCol());
            }
        }

        isMoveMode = true;
        currentPlayerIndex = playerIndex;
        System.out.println("========== 移动模式已进入 ==========\n");
    }

    private void exitMoveMode() {
        System.out.println("\n========== 退出移动模式 ==========");
        isMoveMode = false;
        currentPlayerIndex = -1;
        System.out.println("========== 移动模式已退出 ==========\n");
    }

    public MapView getMapView() {
        return mapView;
    }

    /**
     * 进入加固模式
     * 
     * @param playerIndex 当前玩家索引
     */
    public void enterShoreUpMode(int playerIndex) {
        Player player = gameController.getPlayers().get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        
        // 检查是否是探险家
        boolean isExplorer = player.getRole().getClass().getSimpleName().equals("Explorer");
        
        // 获取可加固的板块列表
        List<Tile> shoreableTiles = new ArrayList<>();
        
        // 添加当前板块
        shoreableTiles.add(currentTile);
        
        // 添加相邻板块
        shoreableTiles.addAll(currentTile.getAdjacentTiles());
        
        // 如果是探险家，添加斜向板块
        if (isExplorer) {
            int currentRow = currentTile.getRow();
            int currentCol = currentTile.getCol();
            
            // 左上
            if (currentRow > 0 && currentCol > 0) {
                Tile upLeftTile = mapView.getTile(currentRow - 1, currentCol - 1);
                if (upLeftTile != null) {
                    shoreableTiles.add(upLeftTile);
                }
            }
            // 右上
            if (currentRow > 0 && currentCol < 5) {
                Tile upRightTile = mapView.getTile(currentRow - 1, currentCol + 1);
                if (upRightTile != null) {
                    shoreableTiles.add(upRightTile);
                }
            }
            // 左下
            if (currentRow < 5 && currentCol > 0) {
                Tile downLeftTile = mapView.getTile(currentRow + 1, currentCol - 1);
                if (downLeftTile != null) {
                    shoreableTiles.add(downLeftTile);
                }
            }
            // 右下
            if (currentRow < 5 && currentCol < 5) {
                Tile downRightTile = mapView.getTile(currentRow + 1, currentCol + 1);
                if (downRightTile != null) {
                    shoreableTiles.add(downRightTile);
                }
            }
        }
        
        // 检查是否有可加固的板块
        boolean hasShoreable = false;
        for (Tile tile : shoreableTiles) {
            if (tile != null && tile.getState() == TileState.FLOODED) {
                hasShoreable = true;
                break;
            }
        }
        
        if (!hasShoreable) {
            JOptionPane.showMessageDialog(mapView, "周围没有可加固的瓦片！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        isInShoreUpMode = true;
        isMoveMode = false;
        currentPlayerIndex = playerIndex;
        System.out.println("[日志] 进入加固模式，玩家" + (playerIndex + 1) + "可以选择要加固的瓦片");
        highlightShoreableTiles(playerIndex);
    }

    /**
     * 高亮显示可加固的瓦片
     * 
     * @param playerIndex 当前玩家索引
     */
    private void highlightShoreableTiles(int playerIndex) {
        System.out.println("\n========== 开始高亮可加固板块 ==========");
        Player player = gameController.getPlayers().get(playerIndex);
        Tile currentTile = player.getCurrentTile();
        System.out.println("当前玩家: " + (playerIndex + 1));
        System.out.println("玩家角色: " + player.getRole().getClass().getSimpleName());
        System.out.println("当前位置: " + currentTile.getName() + " [" + currentTile.getRow() + "," + currentTile.getCol() + "]");

        // 检查是否有沙袋卡
        boolean hasSandbag = false;
        for (Card card : player.getHandCard().getCards()) {
            if (card instanceof SandbagCard) {
                hasSandbag = true;
                break;
            }
        }
        System.out.println("是否有沙袋卡: " + hasSandbag);

        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true); // 保持所有按钮可见
                button.setBackground(null); // 重置背景色
            }
        }

        if (hasSandbag) {
            // 如果有沙袋卡，高亮所有被淹没的板块
            System.out.println("使用沙袋卡模式，高亮所有被淹没的板块");
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Tile tile = mapView.getTile(i, j);
                    if (tile != null && tile.getState() == TileState.FLOODED) {
                        JButton button = mapView.getButton(i, j);
                        if (button != null) {
                            button.setBackground(new Color(255, 255, 200)); // 浅黄色高亮
                            System.out.println("高亮被淹没板块: " + tile.getName() + " [" + i + "," + j + "]");
                        }
                    }
                }
            }
        } else {
            // 如果没有沙袋卡，根据角色类型高亮可加固的瓦片
            List<Tile> adjacentTiles = new ArrayList<>();
            
            // 获取当前瓦片的相邻瓦片
            int currentRow = currentTile.getRow();
            int currentCol = currentTile.getCol();
            
            // 检查是否是探险家
            boolean isExplorer = player.getRole().getClass().getSimpleName().equals("Explorer");
            System.out.println("是否是探险家: " + isExplorer);
            
            // 添加上下左右四个方向的瓦片
            if (currentRow > 0) {
                Tile upTile = mapView.getTile(currentRow - 1, currentCol);
                if (upTile != null) {
                    adjacentTiles.add(upTile);
                    System.out.println("添加上方板块: " + upTile.getName() + " [" + (currentRow - 1) + "," + currentCol + "]");
                }
            }
            if (currentRow < 5) {
                Tile downTile = mapView.getTile(currentRow + 1, currentCol);
                if (downTile != null) {
                    adjacentTiles.add(downTile);
                    System.out.println("添加下方板块: " + downTile.getName() + " [" + (currentRow + 1) + "," + currentCol + "]");
                }
            }
            if (currentCol > 0) {
                Tile leftTile = mapView.getTile(currentRow, currentCol - 1);
                if (leftTile != null) {
                    adjacentTiles.add(leftTile);
                    System.out.println("添加左方板块: " + leftTile.getName() + " [" + currentRow + "," + (currentCol - 1) + "]");
                }
            }
            if (currentCol < 5) {
                Tile rightTile = mapView.getTile(currentRow, currentCol + 1);
                if (rightTile != null) {
                    adjacentTiles.add(rightTile);
                    System.out.println("添加右方板块: " + rightTile.getName() + " [" + currentRow + "," + (currentCol + 1) + "]");
                }
            }
            
            // 如果是探险家，添加斜向的瓦片
            if (isExplorer) {
                System.out.println("探险家模式，添加斜向板块");
                // 左上
                if (currentRow > 0 && currentCol > 0) {
                    Tile upLeftTile = mapView.getTile(currentRow - 1, currentCol - 1);
                    if (upLeftTile != null) {
                        adjacentTiles.add(upLeftTile);
                        System.out.println("添加左上板块: " + upLeftTile.getName() + " [" + (currentRow - 1) + "," + (currentCol - 1) + "]");
                    }
                }
                // 右上
                if (currentRow > 0 && currentCol < 5) {
                    Tile upRightTile = mapView.getTile(currentRow - 1, currentCol + 1);
                    if (upRightTile != null) {
                        adjacentTiles.add(upRightTile);
                        System.out.println("添加右上板块: " + upRightTile.getName() + " [" + (currentRow - 1) + "," + (currentCol + 1) + "]");
                    }
                }
                // 左下
                if (currentRow < 5 && currentCol > 0) {
                    Tile downLeftTile = mapView.getTile(currentRow + 1, currentCol - 1);
                    if (downLeftTile != null) {
                        adjacentTiles.add(downLeftTile);
                        System.out.println("添加左下板块: " + downLeftTile.getName() + " [" + (currentRow + 1) + "," + (currentCol - 1) + "]");
                    }
                }
                // 右下
                if (currentRow < 5 && currentCol < 5) {
                    Tile downRightTile = mapView.getTile(currentRow + 1, currentCol + 1);
                    if (downRightTile != null) {
                        adjacentTiles.add(downRightTile);
                        System.out.println("添加右下板块: " + downRightTile.getName() + " [" + (currentRow + 1) + "," + (currentCol + 1) + "]");
                    }
                }
            }
            
            // 添加当前瓦片
            adjacentTiles.add(currentTile);
            System.out.println("添加当前板块: " + currentTile.getName() + " [" + currentRow + "," + currentCol + "]");

            // 高亮可加固的瓦片
            System.out.println("\n开始高亮可加固板块:");
            boolean hasShoreableTile = false;
            for (Tile tile : adjacentTiles) {
                if (tile != null && tile.getState() == TileState.FLOODED) {
                    JButton button = mapView.getButton(tile.getRow(), tile.getCol());
                    if (button != null) {
                        button.setBackground(new Color(255, 255, 200)); // 浅黄色高亮
                        hasShoreableTile = true;
                        System.out.println("高亮可加固板块: " + tile.getName() + " [" + tile.getRow() + "," + tile.getCol() + "] 状态: " + tile.getState());
                    }
                } else {
                    System.out.println("板块不可加固: " + (tile != null ? tile.getName() : "null") + 
                        " [" + (tile != null ? tile.getRow() : "N/A") + "," + (tile != null ? tile.getCol() : "N/A") + "]" +
                        " 状态: " + (tile != null ? tile.getState() : "null"));
                }
            }

            if (!hasShoreableTile) {
                System.out.println("没有可加固的板块");
                JOptionPane.showMessageDialog(mapView, "周围没有可加固的瓦片！", "提示", JOptionPane.INFORMATION_MESSAGE);
                exitShoreUpMode();
                return;
            }
        }

        // 禁用不可加固的瓦片
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
        System.out.println("========== 高亮可加固板块完成 ==========\n");
    }

    /**
     * 退出加固模式
     */
    public void exitShoreUpMode() {
        isInShoreUpMode = false;
        currentPlayerIndex = -1;

        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            button.setEnabled(true);
            button.setBackground(null); // 恢复默认背景色
        }
    }

    /**
     * 进入领航员移动模式
     * 
     * @param navigatorIndex    领航员玩家索引
     * @param targetPlayerIndex 目标玩家索引
     */
    public void enterNavigatorMoveMode(int navigatorIndex, int targetPlayerIndex) {
        System.out.println("\n========== 进入领航员移动模式 ==========");
        System.out.println("领航员玩家: " + (navigatorIndex + 1));
        System.out.println("目标玩家: " + (targetPlayerIndex + 1));

        isNavigatorMoveMode = true;
        isMoveMode = false;
        isInShoreUpMode = false;
        currentPlayerIndex = navigatorIndex;
        this.targetPlayerIndex = targetPlayerIndex;

        // 高亮显示可移动的区域
        highlightNavigatorMovableTiles(targetPlayerIndex);
        System.out.println("========== 领航员移动模式已进入 ==========\n");
    }

    /**
     * 高亮显示领航员可移动的区域
     * 
     * @param targetPlayerIndex 目标玩家索引
     */
    private void highlightNavigatorMovableTiles(int targetPlayerIndex) {
        Player targetPlayer = gameController.getPlayers().get(targetPlayerIndex);
        Tile currentTile = targetPlayer.getCurrentTile();

        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }

        // 高亮显示可移动的区域（距离为2以内的所有非沉没板块）
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Tile tile = mapView.getTile(i, j);
                if (tile != null && tile.getState() != TileState.SUNK) {
                    int distance = Math.abs(currentTile.getRow() - i) + Math.abs(currentTile.getCol() - j);
                    if (distance <= 2) {
                        JButton button = mapView.getButton(i, j);
                        if (button != null) {
                            button.setBackground(new Color(200, 255, 200)); // 浅绿色高亮
                        }
                    }
                }
            }
        }

        // 禁用不可移动的区域
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
    }

    /**
     * 退出领航员移动模式
     */
    public void exitNavigatorMoveMode() {
        System.out.println("\n========== 退出领航员移动模式 ==========");
        isNavigatorMoveMode = false;
        currentPlayerIndex = -1;
        targetPlayerIndex = -1;

        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }
        System.out.println("========== 领航员移动模式已退出 ==========\n");
    }

    public void enterSandbagMode(int playerIndex) {
        isSandbagMode = true;
        sandbagPlayerIndex = playerIndex;
        isMoveMode = false;
        isInShoreUpMode = false;
        currentPlayerIndex = playerIndex;
        System.out.println("[日志] 进入沙袋卡加固模式，玩家" + (playerIndex + 1) + "可以选择任意被淹没的板块");
        // 高亮所有被淹没的板块
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
        // 禁用不可加固的板块
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
        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }
        // 继续弃牌流程
        if (gameController.getCardController().isInDiscardMode()) {
            gameController.getCardController().continueDiscardMode();
        }
    }

    /**
     * 进入直升机移动模式
     * 
     * @param currentPlayerIndex 当前玩家索引
     * @param selectedPlayers 选中的玩家列表
     * @param card 直升机卡
     */
    public void enterHelicopterMoveMode(int currentPlayerIndex, List<Player> selectedPlayers, HelicopterCard card) {
        System.out.println("\n========== 进入直升机移动模式 ==========");
        System.out.println("当前玩家: " + (currentPlayerIndex + 1));
        System.out.println("选中的玩家数量: " + selectedPlayers.size());

        isHelicopterMoveMode = true;
        isMoveMode = false;
        isInShoreUpMode = false;
        isNavigatorMoveMode = false;
        isSandbagMode = false;
        this.currentPlayerIndex = currentPlayerIndex;
        this.selectedPlayers = selectedPlayers;
        this.helicopterCard = card;

        // 高亮显示可移动的区域（所有未被沉没的板块）
        highlightHelicopterMovableTiles();
        System.out.println("========== 直升机移动模式已进入 ==========\n");
    }

    /**
     * 高亮显示直升机可移动的区域
     */
    private void highlightHelicopterMovableTiles() {
        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }

        // 高亮显示所有未被沉没的板块
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Tile tile = mapView.getTile(i, j);
                if (tile != null && tile.getState() != TileState.SUNK) {
                    JButton button = mapView.getButton(i, j);
                    if (button != null) {
                        button.setBackground(new Color(200, 255, 200)); // 浅绿色高亮
                    }
                }
            }
        }

        // 禁用不可移动的区域
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
    }

    /**
     * 退出直升机移动模式
     */
    private void exitHelicopterMoveMode() {
        System.out.println("\n========== 退出直升机移动模式 ==========");
        isHelicopterMoveMode = false;
        currentPlayerIndex = -1;
        selectedPlayers = null;
        helicopterCard = null;
        System.out.println("========== 直升机移动模式已退出 ==========\n");
    }

    /**
     * 处理直升机移动
     * 
     * @param row 目标行
     * @param col 目标列
     */
    private void handleHelicopterMove(int row, int col) {
        System.out.println("\n========== 处理直升机移动 ==========");
        System.out.println("目标位置: [" + row + "," + col + "]");
        System.out.println("成功接收到板块点击事件");
        
        Tile targetTile = mapView.getTile(row, col);
        if (targetTile == null || targetTile.getState() == TileState.SUNK) {
            System.out.println("目标板块无效或已沉没");
            JOptionPane.showMessageDialog(null, "无法移动到已沉没的板块！");
            return;
        }

        // 获取当前玩家
        Player currentPlayer = gameController.getPlayers().get(helicopterPlayerIndex);
        if (currentPlayer == null) {
            System.out.println("无法获取当前玩家");
            return;
        }

        // 查找玩家的直升机卡
        HelicopterCard helicopterCard = null;
        for (Card card : currentPlayer.getHandCard().getCards()) {
            if (card instanceof HelicopterCard) {
                helicopterCard = (HelicopterCard) card;
                break;
            }
        }

        if (helicopterCard == null) {
            System.out.println("玩家没有直升机卡");
            JOptionPane.showMessageDialog(null, "您没有直升机卡！");
            return;
        }

        // 检查当前板块上是否有其他玩家
        List<Player> playersOnCurrentTile = new ArrayList<>();
        Tile currentTile = currentPlayer.getCurrentTile();
        for (Player player : gameController.getPlayers()) {
            if (player != currentPlayer && player.getCurrentTile() == currentTile) {
                playersOnCurrentTile.add(player);
            }
        }

        // 如果当前板块上有其他玩家，让玩家选择要带哪些人一起移动
        if (!playersOnCurrentTile.isEmpty()) {
            // 创建选项数组
            String[] options = new String[playersOnCurrentTile.size()];
            for (int i = 0; i < playersOnCurrentTile.size(); i++) {
                Player player = playersOnCurrentTile.get(i);
                options[i] = player.getRole().getClass().getSimpleName();
            }
            
            // 创建多选列表
            JList<String> list = new JList<>(options);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            
            // 创建滚动面板
            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setPreferredSize(new Dimension(200, 100));
            
            // 显示对话框
            int result = JOptionPane.showConfirmDialog(null,
                scrollPane,
                "选择要一起移动的玩家",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                // 获取选中的玩家
                List<Player> selectedPlayers = new ArrayList<>();
                for (int index : list.getSelectedIndices()) {
                    selectedPlayers.add(playersOnCurrentTile.get(index));
                }
                
                // 将选中的玩家移动到目标位置
                for (Player player : selectedPlayers) {
                    // 从原位置移除玩家图像
                    mapView.hidePlayerImage(currentTile.getRow(), currentTile.getCol(), 
                        gameController.getPlayers().indexOf(player));
                    
                    // 更新玩家位置
                    player.setCurrentTile(targetTile);
                    
                    // 在新位置显示玩家
                    String roleName = player.getRole().getClass().getSimpleName().toLowerCase();
                    String playerImagePath = "src/resources/Player/" + roleName + "2.png";
                    mapView.showPlayerImage(targetTile.getRow(), targetTile.getCol(), 
                        playerImagePath, gameController.getPlayers().indexOf(player));
                }
            }
        }

        System.out.println("开始移动玩家...");
        // 获取玩家当前位置
        if (currentTile != null) {
            System.out.println("玩家当前位置: " + currentTile.getName() + " [" + currentTile.getRow() + "," + currentTile.getCol() + "]");
            // 从当前位置移除玩家
            mapView.hidePlayerImage(currentTile.getRow(), currentTile.getCol(), helicopterPlayerIndex);
            System.out.println("已移除玩家在原位置的图像");
        }
        
        // 更新玩家位置
        currentPlayer.setCurrentTile(targetTile);
        System.out.println("玩家新位置: " + targetTile.getName() + " [" + targetTile.getRow() + "," + targetTile.getCol() + "]");
        
        // 在新位置显示玩家
        String roleName = currentPlayer.getRole().getClass().getSimpleName().toLowerCase();
        String playerImagePath = "src/resources/Player/" + roleName + "2.png";
        mapView.showPlayerImage(targetTile.getRow(), targetTile.getCol(), playerImagePath, helicopterPlayerIndex);
        System.out.println("已在新位置显示玩家图像");

        // 使用直升机卡
        System.out.println("开始消耗直升机卡...");
        currentPlayer.getHandCard().removeCard(helicopterCard);
        gameController.getPlayerInfoView(helicopterPlayerIndex).removeCard(helicopterCard);
        gameController.getTreasureDeck().discard(helicopterCard);
        System.out.println("直升机卡已消耗");

        // 退出直升机模式
        exitHelicopterMode();

        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true);
                button.setBackground(null);
            }
        }

        // 显示移动成功的提示
        JOptionPane.showMessageDialog(null, 
            "直升机移动成功！", 
            "移动完成", 
            JOptionPane.INFORMATION_MESSAGE);

        System.out.println("直升机移动完成");
        System.out.println("========== 直升机移动处理结束 ==========\n");
    }

    public void enterHelicopterMode(int playerIndex) {
        System.out.println("\n========== 进入直升机模式 ==========");
        System.out.println("玩家索引: " + playerIndex);
        System.out.println("MapView状态: " + (mapView != null ? "已初始化" : "未初始化"));
        
        // 检查玩家是否有直升机卡
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
            System.out.println("玩家拥有直升机卡，成功进入直升机模式");
            isHelicopterMode = true;
            helicopterPlayerIndex = playerIndex;
            mapView.setHelicopterMode(true);
            
            // 显示进入直升机模式的提示
            JOptionPane.showMessageDialog(null, 
                "已进入直升机模式，请选择目标板块。\n如果目标板块上有其他玩家，您可以选择是否带他们一起移动。", 
                "直升机模式", 
                JOptionPane.INFORMATION_MESSAGE);
            
            System.out.println("等待玩家选择目标板块...");
        } else {
            System.out.println("玩家没有直升机卡，无法进入直升机模式");
            JOptionPane.showMessageDialog(null, "您没有直升机卡！");
            return;
        }
        
        System.out.println("直升机模式状态: " + isHelicopterMode);
        System.out.println("直升机玩家索引: " + helicopterPlayerIndex);
        System.out.println("========== 直升机模式进入完成 ==========\n");
    }

    public void exitHelicopterMode() {
        isHelicopterMode = false;
        helicopterPlayerIndex = -1;
        mapView.setHelicopterMode(false);
        // 继续弃牌流程
        if (gameController.getCardController().isInDiscardMode()) {
            gameController.getCardController().continueDiscardMode();
        }
    }

    /**
     * 进入紧急移动模式
     * @param playerIndex 需要移动的玩家索引
     * @param availableTiles 可用的目标板块列表
     */
    public void enterEmergencyMoveMode(int playerIndex, List<Tile> availableTiles) {
        isInEmergencyMoveMode = true;
        emergencyMovePlayerIndex = playerIndex;
        emergencyMoveAvailableTiles = availableTiles;
        
        // 高亮显示可用的目标板块
        for (Tile tile : availableTiles) {
            mapView.highlightTile(tile.getRow(), tile.getCol());
        }
    }

    /**
     * 退出紧急移动模式
     */
    public void exitEmergencyMoveMode() {
        isInEmergencyMoveMode = false;
        emergencyMovePlayerIndex = -1;
        emergencyMoveAvailableTiles = null;
        
        // 清除所有高亮
        mapView.clearHighlights();
    }
}