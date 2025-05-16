package View;

import Model.Enumeration.TileType;
import Model.Enumeration.TileName;
import Model.Tile;
import Model.TilePosition;
import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapView extends JPanel {
    private JButton[][] mapButtons;
    private Tile[][] tiles;
    private static final int MAP_SIZE = 6;
    private static final int GAP_SIZE = 1;
    private static final int BUTTON_SIZE = 100;
    private TilePosition tilePosition;
    
    private static final List<Point> CLASSIC_MAP = Arrays.asList(
        new Point(0, 2), new Point(0, 3),
        new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4),
        new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5),
        new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4), new Point(3, 5),
        new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4),
        new Point(5, 2), new Point(5, 3)
    );

    private static final List<Point> ADVANCED_MAP = Arrays.asList(
        new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 4),
        new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4),
        new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4),
        new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4),
        new Point(4, 0), new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4),
        new Point(5, 1), new Point(5, 2), new Point(5, 3), new Point(5, 4)
    );

    private static final List<Point> EXPERT_MAP = Arrays.asList(
        new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 4),
        new Point(1, 0), new Point(1, 1), new Point(1, 4), new Point(1, 5),
        new Point(2, 0), new Point(2, 1), new Point(2, 4), new Point(2, 5),
        new Point(3, 0), new Point(3, 1), new Point(3, 4), new Point(3, 5),
        new Point(4, 0), new Point(4, 1), new Point(4, 4), new Point(4, 5),
        new Point(5, 1), new Point(5, 2), new Point(5, 3), new Point(5, 4)
    );

    private List<Point> currentMapTiles = CLASSIC_MAP;

    public MapView() {
        tilePosition = new TilePosition();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        mapButtons = new JButton[MAP_SIZE][MAP_SIZE];
        tiles = new Tile[MAP_SIZE][MAP_SIZE];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GAP_SIZE, GAP_SIZE, GAP_SIZE, GAP_SIZE);

        // 随机分配地点名称
        List<TileName> availableTileNames = new ArrayList<>(Arrays.asList(TileName.values()));
        Collections.shuffle(availableTileNames);
        int tileNameIndex = 0;

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                mapButtons[i][j] = new JButton();
                mapButtons[i][j].setMargin(new Insets(0, 0, 0, 0));
                
                // 强制设置为正方形
                Dimension squareSize = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
                mapButtons[i][j].setPreferredSize(squareSize);
                mapButtons[i][j].setMinimumSize(squareSize);
                mapButtons[i][j].setMaximumSize(squareSize);
                
                // 设置初始状态
                Point currentPoint = new Point(i, j);
                if (currentMapTiles.contains(currentPoint)) {
                    mapButtons[i][j].setEnabled(true);
                    // 创建Tile对象并分配随机名称
                    tiles[i][j] = new Tile(availableTileNames.get(tileNameIndex), i, j);
                    
                    // 将板块位置信息添加到TilePosition对象中
                    tilePosition.addTilePosition(tiles[i][j].getName().name(), i, j);
                    
                    // 设置按钮文本为地点名称
                    mapButtons[i][j].setText(tiles[i][j].getName().getDisplayName());
                    
                    // 尝试加载并设置图片
                    try {
                        ImageIcon icon = new ImageIcon(tiles[i][j].getImagePath());
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15, Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                        mapButtons[i][j].setHorizontalTextPosition(SwingConstants.CENTER);
                        mapButtons[i][j].setVerticalTextPosition(SwingConstants.BOTTOM);
                    } catch (Exception e) {
                        System.err.println("无法加载图片: " + tiles[i][j].getImagePath());
                    }
                    tileNameIndex++;
                } else {
                    mapButtons[i][j].setEnabled(false);
                    mapButtons[i][j].setText(TileType.SUNKEN.name());
                    // 加载Sea.png图片
                    try {
                        ImageIcon icon = new ImageIcon("src/resources/Tiles/Sea.png");
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15, Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                    } catch (Exception e) {
                        System.err.println("无法加载图片: src/resources/Tiles/Sea.png");
                    }
                }
                
                // 使用GridBagConstraints添加按钮
                gbc.gridx = j;
                gbc.gridy = i;
                add(mapButtons[i][j], gbc);
            }
        }
    }

    public JButton getButton(int row, int col) {
        return mapButtons[row][col];
    }

    public void setMapType(String mapType) {
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
        
        // 只更新按钮状态，不重新创建布局
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Point currentPoint = new Point(i, j);
                if (currentMapTiles.contains(currentPoint)) {
                    mapButtons[i][j].setEnabled(true);
                    mapButtons[i][j].setText(TileType.DRY.name());
                } else {
                    mapButtons[i][j].setEnabled(true);
                    mapButtons[i][j].setText(TileType.SUNKEN.name());
                }
            }
        }
        revalidate();
        repaint();
    }

    public TilePosition getTilePosition() {
        return tilePosition;
    }
}