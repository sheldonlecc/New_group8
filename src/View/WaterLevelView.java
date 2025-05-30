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

        waterLevelLabel = new JLabel("current water level: 0");
        waterLevelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(waterLevelLabel);
        
        updateWaterLevelImage(1);
        add(waterLevelImage);
    }

    private void updateWaterLevelImage(int level) {
        String imagePath = "/resources/WaterLevel/" + level + ".png";
        System.out.println("正在尝试加载水位图像: " + imagePath); // 调试信息D
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image img = icon.getImage().getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                waterLevelImage.setIcon(new ImageIcon(img));
                System.out.println("成功加载水位图像: " + level); // 调试信息
            } else {
                System.err.println("无法加载水位图像: " + level); // 调试信息
            }
        } catch (Exception e) {
            System.err.println("加载水位图像时发生错误: " + e.getMessage()); // 调试信息
            e.printStackTrace();
        }
    }

    public void updateWaterLevel(int level) {
        System.out.println("收到水位更新请求: " + level); // 调试信息
        waterLevelLabel.setText("当前水位: " + level);
        updateWaterLevelImage(level);
    }
}