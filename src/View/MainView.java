package View;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private static MainView instance;
    private JPanel mainPanel;
    private SetupView setupView;
    private BoardView boardView;

    public static MainView getInstance() {
        return instance;
    }

    public MainView() {
        setTitle("Forbidden Island");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 获取屏幕尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        // Create title
        JLabel titleLabel = new JLabel("Forbidden Island");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // Create buttons
        JButton startButton = createButton("Start");
        JButton rulesButton = createButton("Rules");
        JButton exitButton = createButton("Exit");

        // Add button event listeners
        startButton.addActionListener(e -> showGameSetup());
        rulesButton.addActionListener(e -> showRules());
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to panel
        mainPanel.add(startButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(rulesButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(exitButton);

        setContentPane(mainPanel);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 24));
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
        if (boardView == null) {
            int playerCount = setupView.getPlayerCount();
            String mapType = setupView.getSelectedMap();
            boardView = new BoardView(playerCount, mapType);
        }
        setContentPane(boardView);
        revalidate();
        repaint();
    }

    private void showRules() {
        JDialog rulesDialog = new JDialog(this, "Game Rules", true);
        rulesDialog.setSize(600, 400);
        rulesDialog.setLocationRelativeTo(this);

        JTextArea rulesText = new JTextArea();
        rulesText.setText("Forbidden Island Game Rules:\n\n" +
                "1. Players work together to collect four sacred treasures\n" +
                "2. The island is sinking gradually\n" +
                "3. Each player has special abilities\n" +
                "4. Players must escape before the island sinks\n" +
                "5. Coordinate with your teammates to win!");
        rulesText.setEditable(false);
        rulesText.setFont(new Font("Arial", Font.PLAIN, 16));
        rulesText.setLineWrap(true);
        rulesText.setWrapStyleWord(true);
        rulesText.setMargin(new Insets(10, 10, 10, 10));

        rulesDialog.add(new JScrollPane(rulesText));
        rulesDialog.setVisible(true);
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

    public void showStartScreen() {
        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

}
