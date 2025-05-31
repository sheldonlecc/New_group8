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
    private static final int CARD_WIDTH = 65;  // Reduce width to avoid overflow
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
        this.cardHeight = (playerCount == 2) ? 130 : 110;  // Adjust height
        initializeUI();
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(CARD_WIDTH, cardHeight));
        setMargin(new Insets(0, 0, 0, 0));
        setFont(CARD_FONT);
        setContentAreaFilled(false);  // Custom drawing
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        
        // Set card color theme
        setupCardColors();
        
        // Load card image
        loadCardImage();
        
        // Add mouse event listeners
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
        
        // Set tooltip
        setToolTipText("<html><b>" + card.getName() + "</b><br>" + card.getDescription() + "</html>");
    }
    
    private void setupCardColors() {
        if (card instanceof TreasureCard) {
            // Treasure cards also use gray theme
            baseColor = new Color(156, 163, 175);  // Gray
            borderColor = new Color(75, 85, 99);
        } else {
            switch (card.getType()) {
                case FLOOD:
                    // Regular flood cards use gray border
                    baseColor = new Color(156, 163, 175);  // Gray
                    borderColor = new Color(75, 85, 99);
                    break;
                case HELICOPTER:
                    // Helicopter uses blue
                    baseColor = new Color(59, 130, 246);   // Blue
                    borderColor = new Color(29, 78, 216);
                    break;
                case SAND_BAG:
                    // Sandbag uses yellow
                    baseColor = new Color(251, 191, 36);   // Yellow
                    borderColor = new Color(217, 119, 6);
                    break;
                case WATER_RISE:
                    baseColor = new Color(239, 68, 68);    // Red
                    borderColor = new Color(185, 28, 28);
                    break;
                default:
                    // Other cards use gray border
                    baseColor = new Color(156, 163, 175);  // Gray
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
            System.err.println("Unable to load image: " + imagePath);
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
        
        // Modify scaling logic: further reduce hover effect for 3-4 player games
        float hoverScale;
        if (playerCount == 2) {
            hoverScale = 1.02f;  // Keep 1.02x for 2 players
        } else {
            hoverScale = 1.01f;  // Reduce to 1.01x for 3-4 players
        }
        
        float scale = isPressed ? 0.95f : (isHovered ? hoverScale : 1.0f);
        int offsetX = (int) ((width * (1 - scale)) / 2);
        int offsetY = (int) ((height * (1 - scale)) / 2);
        
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // Draw shadow (further reduce shadow effect for 3-4 players)
        if (!isPressed) {
            int shadowAlpha = (playerCount == 2) ? (isHovered ? 40 : 20) : (isHovered ? 25 : 15);
            g2d.setColor(new Color(0, 0, 0, shadowAlpha));
            g2d.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
        }
        
        // Draw card background gradient
        Color startColor = isPressed ? baseColor.darker() : baseColor;
        Color endColor = isPressed ? baseColor.darker().darker() : baseColor.darker();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, startColor,
            0, height, endColor
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, width - 2, height - 2, 8, 8);
        
        // Draw border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(1, 1, width - 4, height - 4, 8, 8);
        
        // Draw inner highlight (reduce highlight effect for 3-4 players)
        int highlightAlpha = (playerCount == 2) ? (isHovered ? 60 : 30) : (isHovered ? 40 : 20);
        g2d.setColor(new Color(255, 255, 255, highlightAlpha));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRoundRect(2, 2, width - 6, height - 6, 6, 6);
        
        g2d.dispose();
        
        // Draw icon and text
        super.paintComponent(g);
        
        // Draw card name
        if (getIcon() != null) {
            Graphics2D textG2d = (Graphics2D) g.create();
            textG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            textG2d.setFont(TITLE_FONT);
            FontMetrics fm = textG2d.getFontMetrics();
            String name = card.getName();
            int textWidth = fm.stringWidth(name);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() - 6;
            
            // Draw text shadow
            textG2d.setColor(new Color(0, 0, 0, 100));
            textG2d.drawString(name, x + 1, y + 1);
            
            // Draw text
            textG2d.setColor(Color.WHITE);
            textG2d.drawString(name, x, y);
            
            textG2d.dispose();
        }
    }

    public Card getCard() {
        return card;
    }
}
