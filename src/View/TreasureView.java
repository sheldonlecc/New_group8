package View;

import javax.swing.*;
import java.awt.*;

/**
 * TreasureView class represents the treasure display panel in the game.
 * This panel shows the four treasures (Earth, Fire, Wind, Water) and their collection status.
 * Each treasure is displayed as a button with different images based on whether it has been found.
 */
public class TreasureView extends JPanel {
    // Array to hold treasure buttons
    private JButton[] treasureButtons;
    
    // Constants for treasure configuration
    private static final int TREASURE_COUNT = 4; // Total number of treasures in the game
    private static final int BUTTON_SIZE = 120; // Size of each treasure button in pixels
    private static final String[] TREASURE_NAMES = {"Earth", "Fire", "Wind", "Water"}; // Names of the four treasures

    /**
     * Constructor for TreasureView.
     * Initializes the treasure display panel.
     */
    public TreasureView() {
        initializeUI();
    }

    /**
     * Initializes the user interface components for the treasure panel.
     * Sets up the layout, border, and creates treasure buttons.
     */
    private void initializeUI() {
        // Set vertical box layout for treasure buttons
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Add titled border to identify the panel
        setBorder(BorderFactory.createTitledBorder("Treasures"));
        
        // Set preferred size based on button count and size
        setPreferredSize(new Dimension(150, BUTTON_SIZE * TREASURE_COUNT + 50));

        // Initialize the treasure buttons array
        treasureButtons = new JButton[TREASURE_COUNT];

        // Create and add each treasure button
        for (int i = 0; i < TREASURE_COUNT; i++) {
            // Create a panel to center each treasure button
            JPanel treasurePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            
            // Create treasure button (initially not found)
            treasureButtons[i] = createTreasureButton(TREASURE_NAMES[i], false);
            
            // Add button to panel and panel to main view
            treasurePanel.add(treasureButtons[i]);
            add(treasurePanel);
            
            // Add vertical spacing between buttons
            add(Box.createVerticalStrut(10));
        }
    }

    /**
     * Creates a treasure button with the specified name and found status.
     * 
     * @param treasureName The name of the treasure
     * @param found Whether the treasure has been found
     * @return A configured JButton for the treasure
     */
    private JButton createTreasureButton(String treasureName, boolean found) {
        JButton button = new JButton();
        
        // Set button size
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        
        // Remove default button styling for custom appearance
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        
        // Update button with appropriate image
        updateButtonImage(button, treasureName, found);
        
        return button;
    }

    /**
     * Updates the button image based on treasure name and found status.
     * 
     * @param button The button to update
     * @param treasureName The name of the treasure
     * @param found Whether the treasure has been found (affects image selection)
     */
    private void updateButtonImage(JButton button, String treasureName, boolean found) {
        // Construct image path based on treasure name and found status
        // Found treasures use "_1.png", unfound treasures use "_0.png"
        String imagePath = "/resources/Treasures/" + treasureName + "_" + (found ? "1" : "0") + ".png";
        
        try {
            // Load and scale the treasure image
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image img = icon.getImage().getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            // Fallback to text if image loading fails
            button.setText(treasureName);
            e.printStackTrace();
        }
    }

    /**
     * Updates the status of a specific treasure (found or not found).
     * This method is called when a treasure is collected or lost.
     * 
     * @param index The index of the treasure to update (0-3)
     * @param found Whether the treasure has been found
     */
    public void updateTreasureStatus(int index, boolean found) {
        // Validate index to prevent array out of bounds
        if (index >= 0 && index < TREASURE_COUNT) {
            // Update the button image to reflect new status
            updateButtonImage(treasureButtons[index], TREASURE_NAMES[index], found);
        }
    }
}