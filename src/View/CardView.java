package View;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Enumeration.CardType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CardView extends JButton {
    private Card card;
    private static final int CARD_WIDTH = 70;  // 减小宽度避免超出
    private int cardHeight;
    private static final Font CARD_FONT = new Font("Microsoft YaHei", Font.BOLD, 10);
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 8);
    private static final String CARD_PATH = "/resources/Card/";
    
    private boolean isHovered = false;
    private boolean isPressed = false;
    private Color baseColor;
    private Color borderColor;
    private int playerCount;

    public CardView(Card card, int playerCount) {
        this.card = card;
        this.playerCount = playerCount;
        this.cardHeight = (playerCount == 2) ? 130 : 110;  // 调整高度
        initializeUI();
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(CARD_WIDTH, cardHeight));
        setMargin(new Insets(0, 0, 0, 0));
        setFont(CARD_FONT);
        setContentAreaFilled(false);  // 自定义绘制
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        
        // 设置卡牌颜色主题
        setupCardColors();
        
        // 加载卡牌图片
        loadCardImage();
        
        // 添加鼠标事件监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
        
        // 设置工具提示
        setToolTipText("<html><b>" + card.getName() + "</b><br>" + card.getDescription() + "</html>");
    }
    
    private void setupCardColors() {
        if (card instanceof TreasureCard) {
            // 宝藏卡也使用灰色主题
            baseColor = new Color(156, 163, 175);  // 灰色
            borderColor = new Color(75, 85, 99);
        } else {
            switch (card.getType()) {
                case FLOOD:
                    // 普通洪水卡使用灰色边框
                    baseColor = new Color(156, 163, 175);  // 灰色
                    borderColor = new Color(75, 85, 99);
                    break;
                case HELICOPTER:
                    // Helicopter使用蓝色
                    baseColor = new Color(59, 130, 246);   // 蓝色
                    borderColor = new Color(29, 78, 216);
                    break;
                case SAND_BAG:
                    // Sandbag使用黄色
                    baseColor = new Color(251, 191, 36);   // 黄色
                    borderColor = new Color(217, 119, 6);
                    break;
                case WATER_RISE:
                    baseColor = new Color(239, 68, 68);    // 红色
                    borderColor = new Color(185, 28, 28);
                    break;
                default:
                    // 其他卡牌使用灰色边框
                    baseColor = new Color(156, 163, 175);  // 灰色
                    borderColor = new Color(75, 85, 99);
                    break;
            }
        }
    }
    
    private void loadCardImage() {
        String imagePath = CARD_PATH + card.getName() + ".png";
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image image = icon.getImage().getScaledInstance(CARD_WIDTH - 12, cardHeight - 35, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(image));
            setText("");
        } catch (Exception e) {
            System.err.println("无法加载图片: " + imagePath);
            setIcon(null);
            setText(card.getName());
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // 修改缩放逻辑：3-4人游戏时进一步减少悬停效果
        float hoverScale;
        if (playerCount == 2) {
            hoverScale = 1.02f;  // 2人时保持1.02倍
        } else {
            hoverScale = 1.01f;  // 3-4人时减少到1.01倍
        }
        
        float scale = isPressed ? 0.95f : (isHovered ? hoverScale : 1.0f);
        int offsetX = (int) ((width * (1 - scale)) / 2);
        int offsetY = (int) ((height * (1 - scale)) / 2);
        
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // 绘制阴影（3-4人时进一步减少阴影效果）
        if (!isPressed) {
            int shadowAlpha = (playerCount == 2) ? (isHovered ? 40 : 20) : (isHovered ? 25 : 15);
            g2d.setColor(new Color(0, 0, 0, shadowAlpha));
            g2d.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
        }
        
        // 绘制卡牌背景渐变
        Color startColor = isPressed ? baseColor.darker() : baseColor;
        Color endColor = isPressed ? baseColor.darker().darker() : baseColor.darker();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, startColor,
            0, height, endColor
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, width - 2, height - 2, 8, 8);
        
        // 绘制边框
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(1, 1, width - 4, height - 4, 8, 8);
        
        // 绘制内部高光（3-4人时减少高光效果）
        int highlightAlpha = (playerCount == 2) ? (isHovered ? 60 : 30) : (isHovered ? 40 : 20);
        g2d.setColor(new Color(255, 255, 255, highlightAlpha));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRoundRect(2, 2, width - 6, height - 6, 6, 6);
        
        g2d.dispose();
        
        // 绘制图标和文本
        super.paintComponent(g);
        
        // 绘制卡牌名称
        if (getIcon() != null) {
            Graphics2D textG2d = (Graphics2D) g.create();
            textG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            textG2d.setFont(TITLE_FONT);
            FontMetrics fm = textG2d.getFontMetrics();
            String name = card.getName();
            int textWidth = fm.stringWidth(name);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() - 6;
            
            // 绘制文本阴影
            textG2d.setColor(new Color(0, 0, 0, 100));
            textG2d.drawString(name, x + 1, y + 1);
            
            // 绘制文本
            textG2d.setColor(Color.WHITE);
            textG2d.drawString(name, x, y);
            
            textG2d.dispose();
        }
    }

    public Card getCard() {
        return card;
    }
}
