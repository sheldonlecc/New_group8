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

    public BoardView(int playerCount, String mapType) {
        this.playerCount = playerCount;
        // 创建直升机场瓦片（使用FOOLS_LANDING作为直升机场）
        Tile helicopterTile = new Tile(TileName.FOOLS_LANDING, 2, 2);
        
        // 先创建WaterLevelView实例
        this.waterLevelView = new WaterLevelView();
        
        // 先创建和初始化MapView
        this.mapView = new MapView();
        this.mapView.setMapType(mapType);
        
        // 创建GameController时传入waterLevelView
        this.gameController = new GameController(playerCount, helicopterTile, waterLevelView);
        
        // 将MapView设置到GameController
        this.gameController.setMapView(mapView);
        
        // 将BoardView设置到GameController
        this.gameController.setBoardView(this);
        
        // 从GameController获取PlayerInfoView实例
        this.playerInfoViews = new PlayerInfoView[playerCount];
        for (int i = 0; i < playerCount; i++) {
            this.playerInfoViews[i] = gameController.getPlayerInfoViews().get(i);
        }
        initializeUI(mapType);
    }

    private void initializeUI(String mapType) {
        setLayout(new BorderLayout());
        
        // 创建一个新的面板来容纳其他组件，使用GridBagLayout
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 10, 3, 10);
        gbc.fill = GridBagConstraints.BOTH;
    
        // 添加退出按钮到顶部
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
                // 返回主菜单
                MainView.getInstance().showStartScreen();
            }
        });
        topControlPanel.add(exitButton);
        
        // 将退出按钮面板添加到顶部
        add(topControlPanel, BorderLayout.NORTH);
    
        // 上方玩家区域（Player1和Player2）
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
        gbc.weighty = 0.15; // 从0.2减少到0.15
        contentPanel.add(topPanel, gbc);

        // 中间区域（宝藏状态、地图、水位计）- 增加地图权重
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.7; // 从0.6增加到0.7

        // 左侧宝藏状态 - 减小宽度权重
        treasureView = new TreasureView();
        gbc.gridx = 0;
        gbc.weightx = 0.15; // 从0.2减少到0.15
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(treasureView, gbc);

        // 中间地图 - 增加宽度权重
        gbc.gridx = 1;
        gbc.weightx = 0.7; // 从0.6增加到0.7
        JPanel mapCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20)); // 减小垂直间距
        mapCenterPanel.add(mapView);
        contentPanel.add(mapCenterPanel, gbc);

        // 右侧水位计 - 减小宽度权重
        waterLevelView.updateWaterLevel(WaterLevel.getInstance().getCurrentLevel());
        WaterLevel.setWaterLevelView(waterLevelView);
        gbc.gridx = 2;
        gbc.weightx = 0.15; // 从0.2减少到0.15
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(waterLevelView, gbc);

        // 下方玩家区域（Player3和Player4）- 减小高度权重
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
        gbc.weighty = 0.15; // 从0.2减少到0.15
        contentPanel.add(bottomPanel, gbc);

        // 添加contentPanel到主面板
        add(contentPanel, BorderLayout.CENTER);

        // 减小边距
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
