package View;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

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
            buttonImage = ImageIO.read(new File("src/resources/button.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        // Create buttons
        JButton startButton = createButton("START");
        JButton rulesButton = createButton("RULES");
        JButton exitButton = createButton("EXIT");

        // Add button event listeners
        startButton.addActionListener(e -> showGameSetup());
        rulesButton.addActionListener(e -> showRules());
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to button panel
        buttonPanel.add(startButton);
        buttonPanel.add(rulesButton);
        buttonPanel.add(exitButton);

        // Add button panel to main panel's south position
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text, new ImageIcon(buttonImage.getScaledInstance(200, 50, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setVerticalTextPosition(JButton.TOP);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 50));
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
        boardView = new BoardView(playerCount, mapType);
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
        try {
            File pdfFile = new File("src/resources/RULES.pdf");
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this, "Desktop is not supported on this platform.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "RULES.pdf not found at " + pdfFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening PDF file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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