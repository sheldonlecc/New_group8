package View;

import javax.swing.*;
import java.awt.*;

public class WaterLevelView extends JPanel {
    private JLabel waterLevelImage;
    private JLabel waterLevelLabel;
    private static final int IMAGE_WIDTH = 180;
    private static final int IMAGE_HEIGHT = 600;

    public WaterLevelView() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Water Level"));

        add(Box.createVerticalStrut(10));

        waterLevelImage = new JLabel();
        waterLevelImage.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        waterLevelImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateWaterLevelImage(0);
        add(waterLevelImage);
    }

    private void updateWaterLevelImage(int level) {
        String imagePath = "/resources/WaterLevel/" + level + ".png";
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image img = icon.getImage().getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
            waterLevelImage.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWaterLevel(int level) {
        updateWaterLevelImage(level);
    }
}