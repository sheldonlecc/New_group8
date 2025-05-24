package Controller;

import Model.Tile;
import Model.TilePosition;
import Model.Player;
import Model.Enumeration.TileState;
import Model.Role.Role;
import View.MapView;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.Color;
import java.util.List;

public class MapController implements ActionListener {
    private final GameController gameController;
    private final MapView mapView;
    private boolean isMoveMode = false;
    private boolean isInShoreUpMode = false;
    private boolean isNavigatorMoveMode = false;
    private int currentPlayerIndex = -1;
    private int targetPlayerIndex = -1; // 领航员移动模式下的目标玩家索引

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
        if (!isMoveMode && !isInShoreUpMode && !isNavigatorMoveMode) {
            return;
        }

        // 获取被点击的按钮位置
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (e.getSource() == mapView.getButton(i, j)) {
                    handleTileClick(i, j);
                    return;
                }
            }
        }
    }

    private void handleTileClick(int row, int col) {
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

        // 检查移动是否合法
        if (!isValidMove(currentPlayer, targetTile)) {
            System.out.println("非法移动：目标板块不可到达");
            JOptionPane.showMessageDialog(mapView, "非法移动：目标板块不可到达", "移动错误", JOptionPane.ERROR_MESSAGE);
            exitMoveMode();
            return;
        }

        // 执行合法移动
        if (isMoveMode) {
            gameController.movePlayer(currentPlayerIndex, row, col);
            exitMoveMode();
        } else if (isInShoreUpMode) {
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
        List<Tile> shoreableTiles = currentTile.getAdjacentTiles();
        shoreableTiles.add(currentTile);
        boolean hasShoreable = false;
        for (Tile tile : shoreableTiles) {
            if (tile.isShoreable()) {
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
        Player player = gameController.getPlayers().get(playerIndex);
        Tile currentTile = player.getCurrentTile();

        // 重置所有按钮状态
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null) {
                button.setEnabled(true); // 保持所有按钮可见
                button.setBackground(null); // 重置背景色
            }
        }

        // 高亮当前瓦片和相邻瓦片
        List<Tile> adjacentTiles = currentTile.getAdjacentTiles();
        adjacentTiles.add(currentTile); // 添加当前瓦片

        for (Tile tile : adjacentTiles) {
            if (tile.isShoreable()) { // 只高亮可加固的瓦片
                JButton button = mapView.getButton(tile.getRow(), tile.getCol());
                if (button != null) {
                    button.setBackground(new Color(255, 255, 200)); // 浅黄色高亮
                }
            }
        }

        // 禁用不可加固的瓦片
        for (int i = 0; i < mapView.getButtonCount(); i++) {
            JButton button = mapView.getButton(i);
            if (button != null && button.getBackground() == null) {
                button.setEnabled(false);
            }
        }
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
}