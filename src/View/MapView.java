package View;

import javax.swing.*;
import java.awt.*;

public class MapView extends JPanel {
    private JButton[][] mapButtons;
    private static final int MAP_SIZE = 10;

    public MapView() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridLayout(MAP_SIZE, MAP_SIZE, 2, 2));
        mapButtons = new JButton[MAP_SIZE][MAP_SIZE];

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                mapButtons[i][j] = new JButton();
                mapButtons[i][j].setPreferredSize(new Dimension(60, 60));
                add(mapButtons[i][j]);
            }
        }
    }

    public JButton getButton(int row, int col) {
        return mapButtons[row][col];
    }
}