package View;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;

public class SetupView extends JPanel {
    private boolean confirmed = false;
    private JComboBox<String> playerCountSelector;
    private JComboBox<String> mapSelector;
    private JComboBox<String> difficultySelector; // 新增难度选择器
    private MainView mainView;
    private Image backgroundImage;  // 添加背景图片变量

    public SetupView(MainView mainView) {
        this.mainView = mainView;
        // 加载背景图片
        try {
            backgroundImage = ImageIO.read(new File("src/resources/Background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        initializeUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(800, 600));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, gbc);

        // Player count selection
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Number of Players:"), gbc);

        gbc.gridx = 1;
        String[] playerCounts = {"2 Players", "3 Players", "4 Players"};
        playerCountSelector = new JComboBox<>(playerCounts);
        add(playerCountSelector, gbc);

        // Map selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Select Map:"), gbc);

        gbc.gridx = 1;
        String[] mapOptions = {"CLASSIC", "ADVANCED", "EXPERT"};
        mapSelector = new JComboBox<>(mapOptions);
        add(mapSelector, gbc);

        // Difficulty selection (修改难度选项)
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Select Difficulty:"), gbc);

        gbc.gridx = 1;
        String[] difficultyOptions = {
            "NOVICE", 
            "NORMAL", 
            "ELITE", 
            "LEGENDAIRE"
        };
        difficultySelector = new JComboBox<>(difficultyOptions);
        add(difficultySelector, gbc);

        // Button panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(e -> {
            confirmed = true;
            mainView.confirmSetup();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            mainView.showStartScreen();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);

        // 设置所有标签为白色文字，以便在深色背景上更容易看见
        titleLabel.setForeground(Color.WHITE);
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(Color.WHITE);
            }
        }

        // 设置按钮面板为透明
        buttonPanel.setOpaque(false);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getPlayerCount() {
        String selected = (String) playerCountSelector.getSelectedItem();
        return Character.getNumericValue(selected.charAt(0));
    }

    public String getSelectedMap() {
        return (String) mapSelector.getSelectedItem();
    }

    // 获取难度对应的初始水位方法
    public int getInitialWaterLevel() {
        int selectedIndex = difficultySelector.getSelectedIndex();
        return selectedIndex + 1; // NOVICE=1, NORMAL=2, ELITE=3, LEGENDAIRE=4
    }

    // 删除原来的isRandomGeneration方法，因为不再需要
}