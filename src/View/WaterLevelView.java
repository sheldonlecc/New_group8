package View;

import javax.swing.*;
import java.awt.*;

public class WaterLevelView extends JPanel {
    private JProgressBar waterLevelBar;
    private JLabel waterLevelLabel;

    public WaterLevelView() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Water Level"));

        waterLevelLabel = new JLabel("Water Level: 0");
        waterLevelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(waterLevelLabel);

        add(Box.createVerticalStrut(10));

        waterLevelBar = new JProgressBar(JProgressBar.VERTICAL, 0, 10);
        waterLevelBar.setPreferredSize(new Dimension(30, 200));
        waterLevelBar.setStringPainted(true);
        add(waterLevelBar);
    }

    public void updateWaterLevel(int level) {
        waterLevelBar.setValue(level);
        waterLevelLabel.setText("Water Level: " + level);
    }
}