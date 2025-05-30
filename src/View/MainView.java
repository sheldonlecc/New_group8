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
        
        // Set to fullscreen mode
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true); // Remove window decorations (optional)
        
        // Get screen size and set to fullscreen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Start background music
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

        // Create author info panel, placed at top right
        JPanel authorPanel = new JPanel();
        authorPanel.setOpaque(false);
        authorPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel authorLabel = new JLabel("Created by: Jiuzhou Zhu, Zhixiao Li, Haoyang You");
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        authorLabel.setForeground(Color.WHITE);
        
        authorPanel.add(authorLabel);

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
        // Increase button size from 200x50 to 280x70
        JButton button = new JButton(text, new ImageIcon(buttonImage.getScaledInstance(300, 60, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(JButton.CENTER);  // Horizontal center
        button.setVerticalTextPosition(JButton.CENTER);    // Vertical center - changed to CENTER here
        button.setFont(new Font("Arial", Font.BOLD, 30)); // Increase font from 24 to 28
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 60)); // Update maximum size
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
        // Remove conditional check, always create new BoardView instance
        int playerCount = setupView.getPlayerCount();
        String mapType = setupView.getSelectedMap();
        int initialWaterLevel = setupView.getInitialWaterLevel(); // Get initial water level
        boardView = new BoardView(playerCount, mapType, initialWaterLevel); // Pass initial water level
        setContentPane(boardView);
        revalidate();
        repaint();
    }
    
    public void showStartScreen() {
        // Reset game state
        boardView = null; // Clear previous game instance
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
        
        // Can add visual feedback
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