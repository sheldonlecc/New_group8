package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * RuleView class represents a full-screen window for displaying game rules.
 * It shows rule pages in pairs (left and right) with navigation controls.
 * Users can navigate through pages using buttons or keyboard shortcuts.
 */
public class RuleView extends JFrame implements KeyListener {
    // UI components for rule display
    private JPanel imagePanel;
    private JButton prevButton;
    private JButton nextButton;
    private JButton closeButton;
    private JLabel pageLabel;
    
    // Page navigation variables
    private int currentPagePair = 1;
    private final int totalPagePairs = 4;
    
    // Image resources
    private Image[] ruleImages;
    private Image backgroundImage;
    
    // Layout panels
    private JPanel mainPanel;
    private JPanel controlPanel;
    
    /**
     * Constructor for RuleView
     * Initializes the rule viewer window with all necessary components
     */
    public RuleView() {
        initializeFrame();
        loadImages();
        createComponents();
        setupLayout();
        setupEventListeners();

        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            updateLayout();
            displayCurrentPagePair();
        });
    }
    
    /**
     * Initialize the main frame properties
     * Sets up full-screen, undecorated window with key listener
     */
    private void initializeFrame() {
        setTitle("Game Rules");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        addKeyListener(this);
        setFocusable(true);
    }
    
    /**
     * Load all required images including background and rule pages
     */
    private void loadImages() {
        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("src/resources/background.png"));
        } catch (IOException e) {
            System.err.println("Cannot load background image: background.png");
            e.printStackTrace();
        }
        
        // Load rule images (8 pages total)
        ruleImages = new Image[8];
        for (int i = 1; i <= 8; i++) {
            try {
                String imagePath = "src/resources/Rules/RULES_" + i + ".png";
                ruleImages[i-1] = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                System.err.println("Cannot load rule image: RULES_" + i + ".png");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Create all UI components including panels, buttons, and labels
     */
    private void createComponents() {
        // Main panel with background painting
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setLayout(null); // Use absolute layout
        
        // Image display panel for rule pages
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintRulePages(g);
            }
        };
        imagePanel.setOpaque(false);
        
        // Create navigation and control buttons
        prevButton = createStyledButton("Previous");
        nextButton = createStyledButton("Next");
        closeButton = createStyledButton("Close");
        
        // Page indicator label
        pageLabel = new JLabel();
        pageLabel.setForeground(Color.WHITE);
        pageLabel.setFont(new Font("Arial", Font.BOLD, 12));
        pageLabel.setHorizontalAlignment(JLabel.CENTER);
        pageLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        pageLabel.setOpaque(true);
        pageLabel.setBackground(new Color(0, 0, 0, 180));
        
        // Control panel (floating at bottom)
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);
        controlPanel.add(prevButton);
        controlPanel.add(pageLabel);
        controlPanel.add(nextButton);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(closeButton);
    }
    
    /**
     * Create a styled button with consistent appearance and hover effects
     * @param text The text to display on the button
     * @return A styled JButton with brown theme and hover effects
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(139, 69, 19, 200)); 
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedSoftBevelBorder(),
            BorderFactory.createEmptyBorder(8, 20, 8, 20) 
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        // Hover effect - brown color effect on hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(160, 82, 45, 220)); 
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(139, 69, 19, 200)); 
            }
        });
        
        return button;
    }
    
    /**
     * Setup the layout of components within the main panel
     */
    private void setupLayout() {
        setContentPane(mainPanel);
        
        // Add components to main panel
        mainPanel.add(imagePanel);
        mainPanel.add(controlPanel);
        
        // Ensure control panel is on top layer
        mainPanel.setComponentZOrder(controlPanel, 0);
        mainPanel.setComponentZOrder(imagePanel, 1);
        
        // Respond to window size changes
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayout();
            }
            
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Update layout when window is shown
                SwingUtilities.invokeLater(() -> updateLayout());
            }
        });
    }
    
    /**
     * Update the layout and positioning of components based on current window size
     */
    private void updateLayout() {
        if (imagePanel != null && controlPanel != null && getWidth() > 0 && getHeight() > 0) {
            // Image panel fills the entire window
            imagePanel.setBounds(0, 0, getWidth(), getHeight());
            
            // Control panel at bottom, positioned slightly lower
            int controlHeight = 60;
            int controlY = getHeight() - controlHeight - 5;
            controlPanel.setBounds(0, controlY, getWidth(), controlHeight);
            
            // Force repaint
            imagePanel.repaint();
            controlPanel.repaint();
            mainPanel.repaint();
        }
    }
    
    /**
     * Paint the current pair of rule pages (left and right)
     * @param g Graphics context for painting
     */
    private void paintRulePages(Graphics g) {
        if (ruleImages == null) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int panelWidth = imagePanel.getWidth();
        int panelHeight = imagePanel.getHeight();
        
        // Calculate page indices for current page pair
        int leftPageIndex = (currentPagePair - 1) * 2;
        int rightPageIndex = leftPageIndex + 1;
        
        // Each page takes half the width
        int pageWidth = panelWidth / 2;
        int pageHeight = panelHeight;
        
        // Draw left page
        if (leftPageIndex < ruleImages.length && ruleImages[leftPageIndex] != null) {
            Image leftImage = ruleImages[leftPageIndex];
            g2d.drawImage(leftImage, 0, 0, pageWidth, pageHeight, null);
        }
        
        // Draw right page
        if (rightPageIndex < ruleImages.length && ruleImages[rightPageIndex] != null) {
            Image rightImage = ruleImages[rightPageIndex];
            g2d.drawImage(rightImage, pageWidth, 0, pageWidth, pageHeight, null);
        }
        
        // Draw center divider line
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(pageWidth, 0, pageWidth, pageHeight);
        
        g2d.dispose();
    }
    
    /**
     * Setup event listeners for all interactive components
     */
    private void setupEventListeners() {
        prevButton.addActionListener(e -> previousPagePair());
        nextButton.addActionListener(e -> nextPagePair());
        closeButton.addActionListener(e -> dispose());
    }
    
    /**
     * Update the display to show the current page pair
     * Updates page label and button states
     */
    private void displayCurrentPagePair() {
        // Update page label and button states
        int startPage = (currentPagePair - 1) * 2 + 1;
        int endPage = Math.min(startPage + 1, 8);
        pageLabel.setText(String.format("Pages %d-%d / 8", startPage, endPage));
        
        prevButton.setEnabled(currentPagePair > 1);
        nextButton.setEnabled(currentPagePair < totalPagePairs);
        
        // Repaint to show new pages
        imagePanel.repaint();
    }
    
    /**
     * Navigate to the previous page pair
     */
    private void previousPagePair() {
        if (currentPagePair > 1) {
            currentPagePair--;
            displayCurrentPagePair();
        }
    }
    
    /**
     * Navigate to the next page pair
     */
    private void nextPagePair() {
        if (currentPagePair < totalPagePairs) {
            currentPagePair++;
            displayCurrentPagePair();
        }
    }
    
    /**
     * Handle key press events for keyboard navigation
     * @param e KeyEvent containing the pressed key information
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:   // Left arrow key
            case KeyEvent.VK_A:      // A key
                previousPagePair();
                break;
            case KeyEvent.VK_RIGHT:  // Right arrow key
            case KeyEvent.VK_D:      // D key
            case KeyEvent.VK_SPACE:  // Space bar
                nextPagePair();
                break;
            case KeyEvent.VK_ESCAPE: // Escape key to close
                dispose();
                break;
            case KeyEvent.VK_HOME:   // Home key to go to first page
                currentPagePair = 1;
                displayCurrentPagePair();
                break;
            case KeyEvent.VK_END:    // End key to go to last page
                currentPagePair = totalPagePairs;
                displayCurrentPagePair();
                break;
        }
    }
    
    /**
     * Handle key typed events (not used in this implementation)
     * @param e KeyEvent containing the typed key information
     */
    @Override
    public void keyTyped(KeyEvent e) {}
    
    /**
     * Handle key released events (not used in this implementation)
     * @param e KeyEvent containing the released key information
     */
    @Override
    public void keyReleased(KeyEvent e) {}
}