package View;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import Controller.AudioManager;

public class MainView extends JFrame {
    private static MainView instance;
    private JPanel mainPanel;
    private SetupView setupView;
    private BoardView boardView;
    private Image backgroundImage;
    private Image buttonImage;

    public static MainView getInstance() {
        return instance;
    }

    public MainView() {
        setTitle("Forbidden Island");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 设置为全屏模式
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true); // 移除窗口装饰（可选）
        
        // 获取屏幕尺寸并设置为全屏
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 启动背景音乐
        AudioManager.getInstance().playBackgroundMusic();
        
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        try {
            backgroundImage = ImageIO.read(new File("src/resources/Background.png"));
            buttonImage = ImageIO.read(new File("src/resources/button.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建作者信息面板，放置在顶部右侧
        JPanel authorPanel = new JPanel();
        authorPanel.setOpaque(false);
        authorPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel authorLabel = new JLabel("Created by: Jiuzhou Zhu, Zhixiao Li, Haoyang You");
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        authorLabel.setForeground(Color.WHITE);
        
        authorPanel.add(authorLabel);
        
        // 将作者信息添加到顶部
        mainPanel.add(authorPanel, BorderLayout.NORTH);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 0));

        // Create buttons
        JButton startButton = createButton("START");
        JButton rulesButton = createButton("RULES");
        JButton exitButton = createButton("EXIT");

        // Add button event listeners
        startButton.addActionListener(e -> showGameSetup());
        rulesButton.addActionListener(e -> showRules());
        exitButton.addActionListener(e -> {
            AudioManager.getInstance().stopBackgroundMusic();
            System.exit(0);
        });

        // Add buttons to button panel
        buttonPanel.add(startButton);
        buttonPanel.add(rulesButton);
        buttonPanel.add(exitButton);

        // Add button panel to main panel's south position
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createButton(String text) {
        // 增大按钮尺寸从200x50到280x70
        JButton button = new JButton(text, new ImageIcon(buttonImage.getScaledInstance(300, 60, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(JButton.CENTER);  // 水平居中
        button.setVerticalTextPosition(JButton.CENTER);    // 垂直居中 - 这里改为CENTER
        button.setFont(new Font("Arial", Font.BOLD, 30)); // 增大字体从24到28
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 60)); // 更新最大尺寸
        return button;
    }

    private void showGameSetup() {
        if (setupView == null) {
            setupView = new SetupView(this);
        }
        setContentPane(setupView);
        revalidate();
        repaint();
    }

    private void showGameBoard() {
        // 移除条件判断，每次都创建新的BoardView实例
        int playerCount = setupView.getPlayerCount();
        String mapType = setupView.getSelectedMap();
        int initialWaterLevel = setupView.getInitialWaterLevel(); // 获取初始水位
        boardView = new BoardView(playerCount, mapType, initialWaterLevel); // 传递初始水位
        setContentPane(boardView);
        revalidate();
        repaint();
    }
    
    public void showStartScreen() {
        // 重置游戏状态
        boardView = null; // 清除之前的游戏实例
        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private void showRules() {
        SwingUtilities.invokeLater(() -> {
            RuleView ruleView = new RuleView();
            ruleView.setVisible(true);
        });
    }

    private void toggleMusic() {
        AudioManager audioManager = AudioManager.getInstance();
        audioManager.setMusicEnabled(!audioManager.isMusicEnabled());
        
        // 可以添加视觉反馈
        String status = audioManager.isMusicEnabled() ? "ON" : "OFF";
        JOptionPane.showMessageDialog(this, "Music: " + status, "Music Control", JOptionPane.INFORMATION_MESSAGE);
    }

    public void confirmSetup() {
        showGameBoard();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            instance = new MainView();
            instance.setVisible(true);
        });
    }

}