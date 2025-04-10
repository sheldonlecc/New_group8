package View;

import javax.swing.*;
import java.awt.*;

public class BoardView extends JPanel {
    private MapView mapView;
    private TreasureView treasureView;
    private WaterLevelView waterLevelView;
    private PlayerInfoView[] playerInfoViews;
    private static final int PLAYER_COUNT = 4;

    public BoardView() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // 中心地图区域
        mapView = new MapView();
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.add(mapView);
        add(centerPanel, BorderLayout.CENTER);

        // 左侧宝藏状态区域
        treasureView = new TreasureView();
        add(treasureView, BorderLayout.WEST);

        // 右侧水位计区域
        waterLevelView = new WaterLevelView();
        add(waterLevelView, BorderLayout.EAST);

        // 玩家信息区域（四个角落）
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel southPanel = new JPanel(new BorderLayout());
        playerInfoViews = new PlayerInfoView[PLAYER_COUNT];

        for (int i = 0; i < PLAYER_COUNT; i++) {
            playerInfoViews[i] = new PlayerInfoView();
            playerInfoViews[i].setPreferredSize(new Dimension(200, 150));
        }

        northPanel.add(playerInfoViews[0], BorderLayout.WEST);
        northPanel.add(playerInfoViews[1], BorderLayout.EAST);
        southPanel.add(playerInfoViews[2], BorderLayout.WEST);
        southPanel.add(playerInfoViews[3], BorderLayout.EAST);

        add(northPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);

        // 设置边距
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        if (index >= 0 && index < PLAYER_COUNT) {
            return playerInfoViews[index];
        }
        return null;
    }
}
