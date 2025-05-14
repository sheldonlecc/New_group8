package View;

import javax.swing.*;
import java.awt.*;

public class TreasureView extends JPanel {
    private JButton[] treasureButtons;
    private static final int TREASURE_COUNT = 4;
    private static final int BUTTON_SIZE = 120;
    private static final String[] TREASURE_NAMES = {"Earth", "Fire", "Wind", "Water"};

    public TreasureView() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Treasures"));
        setPreferredSize(new Dimension(150, BUTTON_SIZE * TREASURE_COUNT + 50));

        treasureButtons = new JButton[TREASURE_COUNT];

        for (int i = 0; i < TREASURE_COUNT; i++) {
            JPanel treasurePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            treasureButtons[i] = createTreasureButton(TREASURE_NAMES[i], false);
            treasurePanel.add(treasureButtons[i]);
            add(treasurePanel);
            add(Box.createVerticalStrut(10));
        }
    }

    private JButton createTreasureButton(String treasureName, boolean found) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        updateButtonImage(button, treasureName, found);
        return button;
    }

    private void updateButtonImage(JButton button, String treasureName, boolean found) {
        String imagePath = "/resources/Treasures/" + treasureName + "_" + (found ? "1" : "0") + ".png";
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image img = icon.getImage().getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            button.setText(treasureName);
            e.printStackTrace();
        }
    }

    public void updateTreasureStatus(int index, boolean found) {
        if (index >= 0 && index < TREASURE_COUNT) {
            updateButtonImage(treasureButtons[index], TREASURE_NAMES[index], found);
        }
    }
}