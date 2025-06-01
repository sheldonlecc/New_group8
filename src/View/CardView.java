package View;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Enumeration.CardType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * CardView class represents a visual representation of a game card.
 * This class extends JButton to provide interactive card display with
 * custom graphics, hover effects, and responsive design based on player count.
 * Each card has different visual themes based on its type (flood, treasure, special cards).
 */
public class CardView extends JButton {
    // Core card data
    private Card card; // The underlying card model this view represents
    
    // Visual configuration constants
    private static final int CARD_WIDTH = 65; // Fixed width for all cards
    private int cardHeight; // Dynamic height based on player count
    private static final Font CARD_FONT = new Font("Arial", Font.BOLD, 10); // Font for card text
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 8); // Font for card title
    private static final String CARD_PATH = "/resources/Card/"; // Base path for card images
    
    // Interactive state variables
    private boolean isHovered = false; // Track mouse hover state
    private boolean isPressed = false; // Track mouse press state
    
    // Visual theme variables
    private Color baseColor; // Primary color for the card background
    private Color borderColor; // Color for the card border
    
    // Game configuration
    private int playerCount; // Number of players (affects card sizing and effects)

    /**
     * Constructor for CardView.
     * Creates a visual representation of the given card with responsive sizing.
     * 
     * @param card The card model to represent visually
     * @param playerCount The number of players in the game (affects card dimensions)
     */
    public CardView(Card card, int playerCount) {
        this.card = card;
        this.playerCount = playerCount;
        
        // Adjust card height based on player count for optimal screen usage
        this.cardHeight = (playerCount == 2) ? 130 : 110;  // Larger cards for 2-player games
        
        initializeUI();
    }

    /**
     * Initializes the user interface components and styling for the card.
     * Sets up dimensions, colors, mouse interactions, and loads card imagery.
     */
    private void initializeUI() {
        // Configure basic button properties
        setPreferredSize(new Dimension(CARD_WIDTH, cardHeight));
        setMargin(new Insets(0, 0, 0, 0)); // Remove default margins
        setFont(CARD_FONT);
        
        // Disable default button styling for custom rendering
        setContentAreaFilled(false);  // Allow custom background drawing
        setOpaque(false); // Transparent background
        setFocusPainted(false); // Remove focus border
        setBorderPainted(false); // Remove default border
        
        // Configure card visual theme based on card type
        setupCardColors();
        
        // Load and display card image
        loadCardImage();
        
        // Add interactive mouse event handlers
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint(); // Trigger visual update for hover effect
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint(); // Remove hover effect
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint(); // Show pressed state
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint(); // Return to normal state
            }
        });
        
        // Set informative tooltip with card details
        setToolTipText("<html><b>" + card.getName() + "</b><br>" + card.getDescription() + "</html>");
    }
    
    /**
     * Sets up the color theme for the card based on its type.
     * Different card types have distinct color schemes for easy identification.
     */
    private void setupCardColors() {
        if (card instanceof TreasureCard) {
            // Treasure cards use gray theme for consistency
            baseColor = new Color(156, 163, 175);  // Gray base
            borderColor = new Color(75, 85, 99);   // Darker gray border
        } else {
            // Set colors based on card type for visual categorization
            switch (card.getType()) {
                case FLOOD:
                    // Standard flood cards use neutral gray theme
                    baseColor = new Color(156, 163, 175);  // Gray
                    borderColor = new Color(75, 85, 99);
                    break;
                case HELICOPTER:
                    // Helicopter cards use blue theme (escape/movement)
                    baseColor = new Color(59, 130, 246);   // Blue
                    borderColor = new Color(29, 78, 216);
                    break;
                case SAND_BAG:
                    // Sandbag cards use yellow theme (protection/repair)
                    baseColor = new Color(251, 191, 36);   // Yellow
                    borderColor = new Color(217, 119, 6);
                    break;
                case WATER_RISE:
                    // Water rise cards use red theme (danger/threat)
                    baseColor = new Color(239, 68, 68);    // Red
                    borderColor = new Color(185, 28, 28);
                    break;
                default:
                    // Fallback to gray theme for unknown card types
                    baseColor = new Color(156, 163, 175);  // Gray
                    borderColor = new Color(75, 85, 99);
                    break;
            }
        }
    }
    
    /**
     * Loads and scales the card image from resources.
     * Falls back to text display if image loading fails.
     */
    private void loadCardImage() {
        // Construct image path based on card name
        String imagePath = CARD_PATH + card.getName() + ".png";
        
        try {
            // Load and scale image to fit card dimensions
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            
            // Scale image leaving space for borders and title
            Image image = icon.getImage().getScaledInstance(
                CARD_WIDTH - 12,  // Leave horizontal margin
                cardHeight - 35,  // Leave space for title at bottom
                Image.SCALE_SMOOTH // High-quality scaling
            );
            
            setIcon(new ImageIcon(image));
            setText(""); // Clear text when image is available
        } catch (Exception e) {
            // Fallback to text display if image loading fails
            System.err.println("Cannot load image: " + imagePath);
            setIcon(null);
            setText(card.getName()); // Display card name as text
        }
    }
    
    /**
     * Custom painting method for rendering card appearance.
     * Handles background gradients, borders, shadows, and interactive effects.
     * 
     * @param g The Graphics context for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Create high-quality graphics context
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Calculate scaling effects based on player count and interaction state
        // Reduced effects for 3-4 player games to prevent overlap
        float hoverScale;
        if (playerCount == 2) {
            hoverScale = 1.02f;  // Moderate hover effect for 2 players
        } else {
            hoverScale = 1.01f;  // Subtle hover effect for 3-4 players
        }
        
        // Apply scaling based on interaction state
        float scale = isPressed ? 0.95f : (isHovered ? hoverScale : 1.0f);
        int offsetX = (int) ((width * (1 - scale)) / 2);
        int offsetY = (int) ((height * (1 - scale)) / 2);
        
        // Apply transformation for scaling effect
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // Draw drop shadow effect (reduced for smaller cards)
        if (!isPressed) {
            // Adjust shadow intensity based on player count
            int shadowAlpha = (playerCount == 2) ? (isHovered ? 40 : 20) : (isHovered ? 25 : 15);
            g2d.setColor(new Color(0, 0, 0, shadowAlpha));
            g2d.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
        }
        
        // Create gradient background for card
        Color startColor = isPressed ? baseColor.darker() : baseColor;
        Color endColor = isPressed ? baseColor.darker().darker() : baseColor.darker();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, startColor,      // Top color
            0, height, endColor    // Bottom color
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, width - 2, height - 2, 8, 8);
        
        // Draw card border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(1, 1, width - 4, height - 4, 8, 8);
        
        // Draw inner highlight for depth effect
        int highlightAlpha = (playerCount == 2) ? (isHovered ? 60 : 30) : (isHovered ? 40 : 20);
        g2d.setColor(new Color(255, 255, 255, highlightAlpha));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRoundRect(2, 2, width - 6, height - 6, 6, 6);
        
        g2d.dispose();
        
        // Render the button's icon and default text
        super.paintComponent(g);
        
        // Draw card name at the bottom if icon is present
        if (getIcon() != null) {
            Graphics2D textG2d = (Graphics2D) g.create();
            textG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Configure font and measure text
            textG2d.setFont(TITLE_FONT);
            FontMetrics fm = textG2d.getFontMetrics();
            String name = card.getName();
            int textWidth = fm.stringWidth(name);
            
            // Center text horizontally, position at bottom
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() - 6;
            
            // Draw text shadow for better readability
            textG2d.setColor(new Color(0, 0, 0, 100));
            textG2d.drawString(name, x + 1, y + 1);
            
            // Draw main text in white
            textG2d.setColor(Color.WHITE);
            textG2d.drawString(name, x, y);
            
            textG2d.dispose();
        }
    }

    /**
     * Gets the underlying card model represented by this view.
     * 
     * @return The Card object associated with this view
     */
    public Card getCard() {
        return card;
    }
}
