package View;

import javax.swing.*;
import java.awt.*;
import Controller.GameController;
import Model.WaterLevel;

public class BoardView extends JPanel {
    private MapView mapView;
    private TreasureView treasureView;
    private WaterLevelView waterLevelView;
    private PlayerInfoView[] playerInfoViews;
    private int playerCount;
    private GameController gameController;

    public BoardView(int playerCount, String mapType) {
        this.playerCount = playerCount;
        this.gameController = new GameController(playerCount);
        // 从GameController获取PlayerInfoView实例
        this.playerInfoViews = new PlayerInfoView[playerCount];
        for (int i = 0; i < playerCount; i++) {
            this.playerInfoViews[i] = gameController.getPlayerInfoViews().get(i);
        }
        initializeUI(mapType);
    }

    private void initializeUI(String mapType) {
        setLayout(null);
        
        // 创建一个新的面板来容纳其他组件，使用GridBagLayout
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBounds(10, 10, screenSize.width - 20, screenSize.height - 40);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.BOTH;

        // 上方玩家区域（Player1和Player2）
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
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
        gbc.weighty = 0.2;
        contentPanel.add(topPanel, gbc);

        // 中间区域（宝藏状态、地图、水位计）
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.weighty = 0.6;

        // 左侧宝藏状态
        treasureView = new TreasureView();
        gbc.gridx = 0;
        contentPanel.add(treasureView, gbc);

        // 中间地图
        mapView = new MapView();
        mapView.setMapType(mapType);
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        contentPanel.add(mapView, gbc);

        // 右侧水位计
        waterLevelView = new WaterLevelView();
        waterLevelView.updateWaterLevel(WaterLevel.getCurrentLevel()); // 初始化时设置当前水位
        WaterLevel.setWaterLevelView(waterLevelView); // 设置WaterLevelView实例
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        contentPanel.add(waterLevelView, gbc);

        // 下方玩家区域（Player3和Player4）
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
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
        gbc.weighty = 0.2;
        contentPanel.add(bottomPanel, gbc);

        // 添加contentPanel到主面板
        add(contentPanel);

        // 设置边距
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
