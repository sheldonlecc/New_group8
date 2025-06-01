package View;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import Controller.AudioManager;

/**
 * MainView class serves as the primary application window and navigation hub.
 * This class manages the main menu, game setup, and transitions between different views.
 * It implements a singleton pattern and handles the overall application lifecycle,
 * including background music, fullscreen display, and view management.
 */
public class MainView extends JFrame {
    // Singleton instance for global access
    private static MainView instance;
    
    // Core UI components
    private JPanel mainPanel; // Main container panel with background
    private SetupView setupView; // Game configuration view
    private BoardView boardView; // Main game board view
    
    // Visual assets
    private Image backgroundImage; // Background image for main menu
    private Image buttonImage; // Button background image

    /**
     * Gets the singleton instance of MainView.
     * 
     * @return The single MainView instance
     */
    public static MainView getInstance() {
        return instance;
    }

    /**
     * Constructor for MainView.
     * Initializes the main application window with fullscreen display,
     * background music, and main menu interface.
     */
    public MainView() {
        // Configure main window properties
        setTitle("Forbidden Island");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Configure fullscreen mode for immersive experience
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window
        setUndecorated(true); // Remove window decorations for clean fullscreen
        
        // Set window to full screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false); // Prevent resizing
        
        // Initialize background music for atmospheric experience
        AudioManager.getInstance().playBackgroundMusic();
        
        // Create main panel with custom background painting
        mainPanel = new JPanel() {
            /**
             * Custom paint method to draw background image.
             * Scales background image to fit the entire panel.
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    // Draw background image scaled to panel size
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // Load visual assets from resources
        try {
            backgroundImage = ImageIO.read(new File("src/resources/Background.png"));
            buttonImage = ImageIO.read(new File("src/resources/button.png"));
        } catch (IOException e) {
            // Log error if images cannot be loaded
            e.printStackTrace();
        }
        
        // Configure main panel layout and styling
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and configure author information panel
        JPanel authorPanel = new JPanel();
        authorPanel.setOpaque(false); // Transparent background
        authorPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Create author credit label
        JLabel authorLabel = new JLabel("Created by: Jiuzhou Zhu, Zhixiao Li, Haoyang You");
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        authorLabel.setForeground(Color.WHITE); // White text for visibility on background
        
        authorPanel.add(authorLabel);
        // Position author panel at the top of the screen
        mainPanel.add(authorPanel, BorderLayout.NORTH);

        // Create main menu button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Transparent background
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 0));

        // Create main menu buttons
        JButton startButton = createButton("START");
        JButton rulesButton = createButton("RULES");
        JButton exitButton = createButton("EXIT");

        // Configure button event handlers
        startButton.addActionListener(e -> showGameSetup()); // Navigate to game setup
        rulesButton.addActionListener(e -> showRules()); // Show game rules
        exitButton.addActionListener(e -> {
            // Clean shutdown: stop music and exit application
            AudioManager.getInstance().stopBackgroundMusic();
            System.exit(0);
        });

        // Add buttons to the button panel
        buttonPanel.add(startButton);
        buttonPanel.add(rulesButton);
        buttonPanel.add(exitButton);

        // Position button panel at the bottom of the screen
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set main panel as the window content
        setContentPane(mainPanel);
    }

    /**
     * Creates a styled button with custom appearance.
     * Uses button background image and configures text positioning.
     * 
     * @param text The text to display on the button
     * @return A configured JButton with custom styling
     */
    private JButton createButton(String text) {
        // Create button with scaled background image (300x60 pixels)
        JButton button = new JButton(text, new ImageIcon(buttonImage.getScaledInstance(300, 60, Image.SCALE_SMOOTH)));
        
        // Configure text positioning
        button.setHorizontalTextPosition(JButton.CENTER);  // Center text horizontally
        button.setVerticalTextPosition(JButton.CENTER);    // Center text vertically
        
        // Configure text appearance
        button.setFont(new Font("Arial", Font.BOLD, 30)); // Large, bold font
        button.setForeground(Color.WHITE); // White text for visibility
        
        // Remove default button styling for custom appearance
        button.setBorderPainted(false); // No border
        button.setContentAreaFilled(false); // No default background
        
        // Configure button alignment and size
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 60));
        
        return button;
    }

    /**
     * Displays the game setup view for configuring game parameters.
     * Creates a new SetupView instance if it doesn't exist.
     */
    private void showGameSetup() {
        if (setupView == null) {
            setupView = new SetupView(this);
        }
        
        // Switch to setup view
        setContentPane(setupView);
        revalidate(); // Refresh layout
        repaint(); // Refresh display
    }

    /**
     * Displays the main game board with configured parameters.
     * Always creates a new BoardView instance to ensure fresh game state.
     */
    private void showGameBoard() {
        // Get game configuration from setup view
        int playerCount = setupView.getPlayerCount();
        String mapType = setupView.getSelectedMap();
        int initialWaterLevel = setupView.getInitialWaterLevel();
        
        // Create new game board with specified configuration
        boardView = new BoardView(playerCount, mapType, initialWaterLevel);
        
        // Switch to game board view
        setContentPane(boardView);
        revalidate(); // Refresh layout
        repaint(); // Refresh display
    }
    
    /**
     * Returns to the main start screen and resets game state.
     * Called when exiting from an active game.
     */
    public void showStartScreen() {
        // Reset game state by clearing previous game instance
        boardView = null;
        
        // Return to main menu
        setContentPane(mainPanel);
        revalidate(); // Refresh layout
        repaint(); // Refresh display
    }

    /**
     * Displays the game rules in a separate window.
     * Creates and shows a new RuleView instance.
     */
    private void showRules() {
        SwingUtilities.invokeLater(() -> {
            RuleView ruleView = new RuleView();
            ruleView.setVisible(true);
        });
    }

    /**
     * Toggles background music on/off and provides user feedback.
     * Shows a dialog indicating the current music status.
     */
    private void toggleMusic() {
        AudioManager audioManager = AudioManager.getInstance();
        
        // Toggle music state
        audioManager.setMusicEnabled(!audioManager.isMusicEnabled());
        
        // Provide visual feedback to user
        String status = audioManager.isMusicEnabled() ? "ON" : "OFF";
        JOptionPane.showMessageDialog(this, "Music: " + status, "Music Control", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Confirms game setup and transitions to the game board.
     * Called by SetupView when configuration is complete.
     */
    public void confirmSetup() {
        showGameBoard();
    }

    /**
     * Application entry point.
     * Creates and displays the main application window.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // 设置系统语言环境为英文
        Locale.setDefault(new Locale("en", "US"));
        
        // 设置Swing组件的英文文本
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        UIManager.put("FileChooser.acceptAllFileFilterText", "All Files");
        UIManager.put("FileChooser.lookInLabelText", "Look in");
        UIManager.put("FileChooser.saveInLabelText", "Save in");
        UIManager.put("FileChooser.fileNameLabelText", "File name");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Files of type");
        UIManager.put("FileChooser.saveButtonText", "Save");
        UIManager.put("FileChooser.openButtonText", "Open");
        UIManager.put("FileChooser.cancelButtonText", "Cancel");
        
        SwingUtilities.invokeLater(() -> {
            // Create singleton instance and make it visible
            instance = new MainView();
            instance.setVisible(true);
        });
    }
}