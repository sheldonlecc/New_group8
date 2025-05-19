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
            new Point(5, 2), new Point(5, 3));

    private static final List<Point> ADVANCED_MAP = Arrays.asList(
            new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 4),
            new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3), new Point(1, 4),
            new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4),
            new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(3, 4),
            new Point(4, 0), new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4),
            new Point(5, 1), new Point(5, 2), new Point(5, 3), new Point(5, 4));

    private static final List<Point> EXPERT_MAP = Arrays.asList(
            new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(0, 4),
            new Point(1, 0), new Point(1, 1), new Point(1, 4), new Point(1, 5),
            new Point(2, 0), new Point(2, 1), new Point(2, 4), new Point(2, 5),
            new Point(3, 0), new Point(3, 1), new Point(3, 4), new Point(3, 5),
            new Point(4, 0), new Point(4, 1), new Point(4, 4), new Point(4, 5),
            new Point(5, 1), new Point(5, 2), new Point(5, 3), new Point(5, 4));

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
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15,
                                Image.SCALE_SMOOTH);
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
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 15,
                                Image.SCALE_SMOOTH);
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

        // 设置所有瓦片的相邻关系（上下左右）
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
     * 获取按钮总数
     * 
     * @return 按钮总数
     */
    public int getButtonCount() {
        return MAP_SIZE * MAP_SIZE;
    }

    /**
     * 根据索引获取按钮
     * 
     * @param index 按钮索引
     * @return 对应的按钮
     */
    public JButton getButton(int index) {
        int row = index / MAP_SIZE;
        int col = index % MAP_SIZE;
        return getButton(row, col);
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
}