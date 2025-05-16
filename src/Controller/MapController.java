package Controller;

import Model.Tile;
import Model.TilePosition;
import Model.Player;
import Model.Enumeration.TileState;
import View.MapView;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

public class MapController implements ActionListener {
    private final GameController gameController;
    private final MapView mapView;
    private boolean isMoveMode = false;
    private int currentPlayerIndex = -1;

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
        if (!isMoveMode) {
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
        gameController.movePlayer(currentPlayerIndex, row, col);
        
        // 移动完成后退出移动模式
        exitMoveMode();
        System.out.println("========== 板块点击处理完成 ==========\n");
    }

    private boolean isValidMove(Player player, Tile targetTile) {
        // 检查目标板块是否被淹没
        if (targetTile.getState() == TileState.SUNK) {
            return false;
        }

        // 获取当前板块
        Tile currentTile = player.getCurrentTile();
        if (currentTile == null) {
            return false;
        }

        // 检查是否相邻
        int currentRow = currentTile.getRow();
        int currentCol = currentTile.getCol();
        int targetRow = targetTile.getRow();
        int targetCol = targetTile.getCol();

        // 计算曼哈顿距离
        int distance = Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol);
        
        // 如果距离为1，说明是相邻的
        return distance == 1;
    }

    public void enterMoveMode(int playerIndex) {
        System.out.println("\n========== 进入移动模式 ==========");
        System.out.println("当前玩家: " + (playerIndex + 1));
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
} 