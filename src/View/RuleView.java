package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class RuleView extends JFrame implements KeyListener {
    private JPanel imagePanel;
    private JButton prevButton;
    private JButton nextButton;
    private JButton closeButton;
    private JLabel pageLabel;
    private int currentPagePair = 1;
    private final int totalPagePairs = 4; // 8页分为4对
    private Image[] ruleImages;
    private Image backgroundImage;
    private JPanel mainPanel;
    private JPanel controlPanel;
    
    public RuleView() {
        initializeFrame();
        loadImages();
        createComponents();
        setupLayout();
        setupEventListeners();
        
        // 确保窗口完全显示后再设置按钮位置
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            updateLayout();
            displayCurrentPagePair();
        });
    }
    
    private void initializeFrame() {
        setTitle("Game Rules");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        addKeyListener(this);
        setFocusable(true);
    }
    
    private void loadImages() {
        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("src/resources/background.png"));
        } catch (IOException e) {
            System.err.println("Cannot load background image: background.png");
            e.printStackTrace();
        }
        
        // Load rule images
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
    
    private void createComponents() {
        // Main panel with background
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setLayout(null); // 使用绝对布局
        
        // Image display panel
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintRulePages(g);
            }
        };
        imagePanel.setOpaque(false);
        
        // Control buttons
        prevButton = createStyledButton("Previous");
        nextButton = createStyledButton("Next");
        closeButton = createStyledButton("Close");
        
        // Page label
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
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(139, 69, 19, 200)); // 棕色
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedSoftBevelBorder(),
            BorderFactory.createEmptyBorder(8, 20, 8, 20) // 减少左右内边距从20改为12
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        // Hover effect - 悬停时的棕色效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(160, 82, 45, 220)); // 悬停时的浅棕色
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(139, 69, 19, 200)); // 恢复原棕色
            }
        });
        
        return button;
    }
    
    private void setupLayout() {
        setContentPane(mainPanel);
        
        // 添加组件到主面板
        mainPanel.add(imagePanel);
        mainPanel.add(controlPanel);
        
        // 确保控制面板在最上层
        mainPanel.setComponentZOrder(controlPanel, 0);
        mainPanel.setComponentZOrder(imagePanel, 1);
        
        // 响应窗口大小变化
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayout();
            }
            
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // 窗口显示时也更新布局
                SwingUtilities.invokeLater(() -> updateLayout());
            }
        });
    }
    
    private void updateLayout() {
        if (imagePanel != null && controlPanel != null && getWidth() > 0 && getHeight() > 0) {
            // 图片面板填充整个窗口
            imagePanel.setBounds(0, 0, getWidth(), getHeight());
            
            // 控制面板位于底部，稍微再靠下一点
            int controlHeight = 60;
            int controlY = getHeight() - controlHeight - 5; // 距离底部5像素（原来是20像素）
            controlPanel.setBounds(0, controlY, getWidth(), controlHeight);
            
            // 强制重绘
            imagePanel.repaint();
            controlPanel.repaint();
            mainPanel.repaint();
        }
    }
    
    private void paintRulePages(Graphics g) {
        if (ruleImages == null) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int panelWidth = imagePanel.getWidth();
        int panelHeight = imagePanel.getHeight();
        
        if (panelWidth <= 0 || panelHeight <= 0) return;
        
        // Calculate page indices
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
    
    private void setupEventListeners() {
        prevButton.addActionListener(e -> previousPagePair());
        nextButton.addActionListener(e -> nextPagePair());
        closeButton.addActionListener(e -> dispose());
    }
    
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
    
    private void previousPagePair() {
        if (currentPagePair > 1) {
            currentPagePair--;
            displayCurrentPagePair();
        }
    }
    
    private void nextPagePair() {
        if (currentPagePair < totalPagePairs) {
            currentPagePair++;
            displayCurrentPagePair();
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                previousPagePair();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
            case KeyEvent.VK_SPACE:
                nextPagePair();
                break;
            case KeyEvent.VK_ESCAPE:
                dispose();
                break;
            case KeyEvent.VK_HOME:
                currentPagePair = 1;
                displayCurrentPagePair();
                break;
            case KeyEvent.VK_END:
                currentPagePair = totalPagePairs;
                displayCurrentPagePair();
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}