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
    private static final int BUTTON_SIZE = 105;
    private TilePosition tilePosition;
    private JLayeredPane[][] layeredPanes;
    private Map<String, List<JLabel>> tilePlayerLabels;
    private Map<String, List<Integer>> tilePlayers;
    private Map<Integer, Point> playerFixedPositions;
    private boolean isHelicopterMode = false;
    private boolean isSandbagMode = false;

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
        this.mapButtons = new JButton[MAP_SIZE][MAP_SIZE];
        this.tiles = new Tile[MAP_SIZE][MAP_SIZE];
        this.layeredPanes = new JLayeredPane[MAP_SIZE][MAP_SIZE];
        this.tilePlayerLabels = new HashMap<>();
        this.tilePlayers = new HashMap<>();
        this.playerFixedPositions = new HashMap<>();
        tilePosition = new TilePosition();

        // 设置首选大小和最小大小
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
            System.err.println("无法加载图片: " + tile.getImagePath(tile.getState()));
        }
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GAP_SIZE, GAP_SIZE, GAP_SIZE, GAP_SIZE);
        gbc.fill = GridBagConstraints.NONE; // 防止组件被拉伸

        // 随机分配地点名称
        List<TileName> availableTileNames = new ArrayList<>(Arrays.asList(TileName.values()));
        Collections.shuffle(availableTileNames);
        int tileNameIndex = 0;

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                // 创建层级面板
                layeredPanes[i][j] = new JLayeredPane();
                layeredPanes[i][j].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                layeredPanes[i][j].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                layeredPanes[i][j].setLayout(null);

                // 创建按钮
                mapButtons[i][j] = new JButton();
                mapButtons[i][j].setMargin(new Insets(0, 0, 0, 0));
                mapButtons[i][j].setBounds(0, 0, BUTTON_SIZE, BUTTON_SIZE);
                mapButtons[i][j].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
                mapButtons[i][j].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

                // 将按钮添加到层级面板
                layeredPanes[i][j].add(mapButtons[i][j], JLayeredPane.DEFAULT_LAYER);

                // 设置初始状态
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
                        System.err.println("无法加载图片: " + tiles[i][j].getImagePath(tiles[i][j].getState()));
                    }
                    tileNameIndex++;
                } else {
                    mapButtons[i][j].setEnabled(false);
                    mapButtons[i][j].setText(TileType.SUNKEN.name());
                    try {
                        ImageIcon icon = new ImageIcon("src/resources/Tiles/Sea.png");
                        Image image = icon.getImage().getScaledInstance(BUTTON_SIZE + 10, BUTTON_SIZE + 10,
                                Image.SCALE_SMOOTH);
                        mapButtons[i][j].setIcon(new ImageIcon(image));
                        mapButtons[i][j].setText("");
                    } catch (Exception e) {
                        System.err.println("无法加载图片: src/resources/Tiles/Sea.png");
                    }
                }

                // 使用GridBagConstraints添加层级面板
                gbc.gridx = j;
                gbc.gridy = i;
                add(layeredPanes[i][j], gbc);
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
        System.out.println("\n========== 切换地图类型 ==========");
        System.out.println("切换前MapView大小: " + getSize());
        System.out.println("切换前MapView首选大小: " + getPreferredSize());
        System.out.println("切换前MapView最小大小: " + getMinimumSize());

        // 打印每个板块的大小
        System.out.println("\n切换前板块大小:");
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                System.out.printf("板块[%d,%d] 大小: %s, 首选大小: %s\n",
                        i, j,
                        layeredPanes[i][j].getSize(),
                        layeredPanes[i][j].getPreferredSize());
            }
        }

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

        // 打印切换后的信息
        System.out.println("\n切换后MapView大小: " + getSize());
        System.out.println("切换后MapView首选大小: " + getPreferredSize());
        System.out.println("切换后MapView最小大小: " + getMinimumSize());

        System.out.println("\n切换后板块大小:");
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                System.out.printf("板块[%d,%d] 大小: %s, 首选大小: %s\n",
                        i, j,
                        layeredPanes[i][j].getSize(),
                        layeredPanes[i][j].getPreferredSize());
            }
        }

        // 获取父容器信息
        Container parent = getParent();
        if (parent != null) {
            System.out.println("\n父容器信息:");
            System.out.println("父容器类名: " + parent.getClass().getName());
            System.out.println("父容器大小: " + parent.getSize());
            System.out.println("父容器首选大小: " + parent.getPreferredSize());
            System.out.println("父容器最小大小: " + parent.getMinimumSize());
        }

        revalidate();
        repaint();
        System.out.println("========== 地图类型切换完成 ==========\n");
    }

    @Override
    public void doLayout() {
        // 确保MapView保持其首选大小
        Dimension preferredSize = getPreferredSize();
        if (getSize().width != preferredSize.width || getSize().height != preferredSize.height) {
            setSize(preferredSize);
        }
        super.doLayout();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        // 保持宽高比
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
     * 获取指定板块上的玩家数量
     * 
     * @param row 行
     * @param col 列
     * @return 玩家数量
     */
    public int getPlayerCountOnTile(int row, int col) {
        String tileKey = row + "," + col;
        return tilePlayers.getOrDefault(tileKey, new ArrayList<>()).size();
    }

    /**
     * 获取玩家的固定位置
     * 
     * @param playerIndex 玩家索引
     * @return 固定位置点
     */
    private Point getPlayerFixedPosition(int playerIndex) {
        return playerFixedPositions.computeIfAbsent(playerIndex, k -> {
            switch (k) {
                case 0: // 第一个玩家，左下角
                    return new Point(0, BUTTON_SIZE / 2);
                case 1: // 第二个玩家，左上角
                    return new Point(0, 0);
                case 2: // 第三个玩家，右上角
                    return new Point(BUTTON_SIZE / 2, 0);
                case 3: // 第四个玩家，右下角
                    return new Point(BUTTON_SIZE / 2, BUTTON_SIZE / 2);
                default:
                    return new Point(0, 0);
            }
        });
    }

    /**
     * 显示玩家图像
     * 
     * @param row             行
     * @param col             列
     * @param playerImagePath 玩家图像路径
     * @param playerIndex     玩家索引
     */
    public void showPlayerImage(int row, int col, String playerImagePath, int playerIndex) {
        try {
            ImageIcon originalIcon = new ImageIcon(playerImagePath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    BUTTON_SIZE, // 缩小图像尺寸
                    BUTTON_SIZE,
                    Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            // 获取玩家的固定位置
            Point position = getPlayerFixedPosition(playerIndex);

            // 创建新的标签并设置位置
            JLabel playerLabel = new JLabel(scaledIcon);
            playerLabel.setBounds(
                    position.x,
                    position.y,
                    BUTTON_SIZE / 2,
                    BUTTON_SIZE / 2);
            playerLabel.setVisible(true);

            // 记录玩家位置
            String tileKey = row + "," + col;
            List<Integer> players = tilePlayers.computeIfAbsent(tileKey, k -> new ArrayList<>());
            List<JLabel> labels = tilePlayerLabels.computeIfAbsent(tileKey, k -> new ArrayList<>());

            // 添加新玩家
            players.add(playerIndex);
            labels.add(playerLabel);

            // 将标签添加到层级面板
            layeredPanes[row][col].add(playerLabel, JLayeredPane.PALETTE_LAYER);
            layeredPanes[row][col].revalidate();
            layeredPanes[row][col].repaint();
        } catch (Exception e) {
            System.err.println("加载玩家图像失败: " + e.getMessage());
        }
    }

    /**
     * 隐藏玩家图像
     * 
     * @param row         行
     * @param col         列
     * @param playerIndex 玩家索引
     */
    public void hidePlayerImage(int row, int col, int playerIndex) {
        String tileKey = row + "," + col;
        List<Integer> players = tilePlayers.get(tileKey);
        List<JLabel> labels = tilePlayerLabels.get(tileKey);

        if (players != null && labels != null) {
            // 找到玩家在列表中的索引
            int index = players.indexOf(playerIndex);
            if (index != -1) {
                // 移除对应的标签
                JLabel label = labels.remove(index);
                layeredPanes[row][col].remove(label);
                players.remove(index);

                // 如果板块上没有玩家了，清理数据
                if (players.isEmpty()) {
                    tilePlayers.remove(tileKey);
                    tilePlayerLabels.remove(tileKey);
                }

                layeredPanes[row][col].revalidate();
                layeredPanes[row][col].repaint();
            }
        }
    }

    public void setHelicopterMode(boolean isHelicopterMode) {
        this.isHelicopterMode = isHelicopterMode;
    }

    public void setSandbagMode(boolean isSandbagMode) {
        this.isSandbagMode = isSandbagMode;
        if (isSandbagMode) {
            // 高亮所有被淹没的板块
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Tile tile = getTile(i, j);
                    if (tile != null && tile.getState() == TileState.FLOODED) {
                        JButton button = getButton(i, j);
                        if (button != null) {
                            button.setBackground(new Color(255, 255, 200));
                        }
                    }
                }
            }
            // 禁用不可加固的板块
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Tile tile = getTile(i, j);
                    JButton button = getButton(i, j);
                    if (button != null && (tile == null || tile.getState() != TileState.FLOODED)) {
                        button.setEnabled(false);
                    }
                }
            }
        } else {
            // 重置所有按钮状态
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    JButton button = getButton(i, j);
                    if (button != null) {
                        button.setEnabled(true);
                        button.setBackground(null);
                    }
                }
            }
        }
    }

    /**
     * 高亮显示指定位置的板块
     * 
     * @param row 行
     * @param col 列
     */
    public void highlightTile(int row, int col) {
        if (row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length) {
            tiles[row][col].setHighlighted(true);
            repaint();
        }
    }

    /**
     * 清除所有板块的高亮
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