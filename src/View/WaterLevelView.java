package View;

import javax.swing.*;
import java.awt.*;

/**
 * WaterLevelView class represents the water level display panel in the game.
 * This panel shows the current water level through both an image and text label.
 * The water level is a critical game mechanic that affects gameplay.
 */
public class WaterLevelView extends JPanel {
    // UI components for displaying water level
    private JLabel waterLevelImage; // Label to display water level image
    private JLabel waterLevelLabel; // Label to display water level text
    
    // Constants for image dimensions
    private static final int IMAGE_WIDTH = 180; // Width of water level image
    private static final int IMAGE_HEIGHT = 600; // Height of water level image

    /**
     * Constructor for WaterLevelView.
     * Initializes the water level display panel.
     */
    public WaterLevelView() {
        initializeUI();
    }

    /**
     * Initializes the user interface components for the water level panel.
     * Sets up the layout, border, and creates display components.
     */
    private void initializeUI() {
        // Set vertical box layout for components
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Add titled border to identify the panel
        setBorder(BorderFactory.createTitledBorder("Water Level"));

        // Add top spacing
        add(Box.createVerticalStrut(10));

        // Initialize water level image label
        waterLevelImage = new JLabel();
        waterLevelImage.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        waterLevelImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Initialize water level text label
        waterLevelLabel = new JLabel("current water level: 0");
        waterLevelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(waterLevelLabel);
        
        // Set initial water level to 1 and add image to panel
        updateWaterLevelImage(1);
        add(waterLevelImage);
    }

    /**
     * Updates the water level image based on the current level.
     * 
     * @param level The current water level (determines which image to load)
     */
    private void updateWaterLevelImage(int level) {
        // Construct image path based on water level
        String imagePath = "/resources/WaterLevel/" + level + ".png";
        
        // Debug information: attempting to load water level image
        System.out.println("Attempting to load water level image: " + imagePath);
        
        try {
            // Load the water level image
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            
            // Check if image loaded successfully
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                // Scale image to fit the label dimensions
                Image img = icon.getImage().getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                waterLevelImage.setIcon(new ImageIcon(img));
                
                // Debug information: successful image loading
                System.out.println("Successfully loaded water level image: " + level);
            } else {
                // Debug information: failed to load image
                System.err.println("Failed to load water level image: " + level);
            }
        } catch (Exception e) {
            // Debug information: error occurred during image loading
            System.err.println("Error occurred while loading water level image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the water level display with a new level value.
     * This method updates both the text label and the image.
     * 
     * @param level The new water level to display
     */
    public void updateWaterLevel(int level) {
        // Debug information: received water level update request
        System.out.println("Received water level update request: " + level);
        
        // Update the text label with current water level
        waterLevelLabel.setText("Current water level: " + level);
        
        // Update the water level image
        updateWaterLevelImage(level);
    }
}