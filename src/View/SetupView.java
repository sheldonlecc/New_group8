package View;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Controller.AudioManager;

public class SetupView extends JPanel {
    private boolean confirmed = false;
    private JComboBox<String> playerCountSelector;
    private JComboBox<String> mapSelector;
    private JComboBox<String> difficultySelector;
    private JCheckBox musicCheckBox;
    private MainView mainView;
    private Image backgroundImage;
    private JPanel mapPreviewPanel;
    private JLabel[] mapImageLabels;
    private Image[] mapImages;

    public SetupView(MainView mainView) {
        this.mainView = mainView;
        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("src/resources/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadMapImages();
        initializeUI();
    }

    private void loadMapImages() {
        String[] mapNames = {"CLASSIC", "ADVANCED", "EXPERT"};
        mapImages = new Image[3];
        
        for (int i = 0; i < mapNames.length; i++) {
            try {
                mapImages[i] = ImageIO.read(new File("src/resources/Map/" + mapNames[i] + ".png"));
            } catch (IOException e) {
                System.err.println("Failed to load map image: " + mapNames[i] + ".png");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());

        // Add larger top spacing to move selection area down more
        JPanel topSpacer = new JPanel();
        topSpacer.setOpaque(false);
        topSpacer.setPreferredSize(new Dimension(0, 400)); // Increase from 50 to 100 pixels
        add(topSpacer, BorderLayout.NORTH);

        // Create central control panel
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Create map preview panel
        createMapPreviewPanel();
        add(mapPreviewPanel, BorderLayout.SOUTH);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        centerPanel.add(titleLabel, gbc);

        // Player count selection
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel playerLabel = new JLabel("Number of Players:");
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(playerLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        String[] playerCounts = {"2 Players", "3 Players", "4 Players"};
        playerCountSelector = new JComboBox<>(playerCounts);
        playerCountSelector.setPreferredSize(new Dimension(150, 30));
        centerPanel.add(playerCountSelector, gbc);

        // Map selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel mapLabel = new JLabel("Select Map:");
        mapLabel.setForeground(Color.WHITE);
        mapLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(mapLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        String[] mapOptions = {"CLASSIC", "ADVANCED", "EXPERT"};
        mapSelector = new JComboBox<>(mapOptions);
        mapSelector.setPreferredSize(new Dimension(150, 30));
        mapSelector.addActionListener(e -> updateMapPreview());
        centerPanel.add(mapSelector, gbc);

        // Difficulty selection
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel difficultyLabel = new JLabel("Select Difficulty:");
        difficultyLabel.setForeground(Color.WHITE);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(difficultyLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        String[] difficultyOptions = {
            "NOVICE", 
            "NORMAL", 
            "ELITE", 
            "LEGENDAIRE"
        };
        difficultySelector = new JComboBox<>(difficultyOptions);
        difficultySelector.setPreferredSize(new Dimension(150, 30));
        centerPanel.add(difficultySelector, gbc);

        // Music toggle
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel musicLabel = new JLabel("Background Music:");
        musicLabel.setForeground(Color.WHITE);
        musicLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(musicLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        musicCheckBox = new JCheckBox("Enable Music");
        musicCheckBox.setSelected(AudioManager.getInstance().isMusicEnabled());
        musicCheckBox.setOpaque(false);
        musicCheckBox.setForeground(Color.WHITE);
        musicCheckBox.setFont(new Font("Arial", Font.BOLD, 14));
        musicCheckBox.addActionListener(e -> {
            AudioManager.getInstance().setMusicEnabled(musicCheckBox.isSelected());
        });
        centerPanel.add(musicCheckBox, gbc);

        // Button panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");
        
        confirmButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));

        confirmButton.addActionListener(e -> {
            confirmed = true;
            mainView.confirmSetup();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            mainView.showStartScreen();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);
        centerPanel.add(buttonPanel, gbc);
        
        return centerPanel;
    }

    private void createMapPreviewPanel() {
        mapPreviewPanel = new JPanel();
        // Increase distance between maps from 30 to 50
        mapPreviewPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 150, 20));
        mapPreviewPanel.setOpaque(false);
        mapPreviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        String[] mapNames = {"CLASSIC", "ADVANCED", "EXPERT"};
        mapImageLabels = new JLabel[3];
        
        for (int i = 0; i < 3; i++) {
            JPanel mapContainer = new JPanel();
            mapContainer.setLayout(new BorderLayout());
            mapContainer.setOpaque(false);
            
            // Create map name label
            JLabel nameLabel = new JLabel(mapNames[i]);
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Create map image label
            mapImageLabels[i] = new JLabel();
            if (mapImages[i] != null) {
                // Scale image to appropriate size
                Image scaledImage = mapImages[i].getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                mapImageLabels[i].setIcon(new ImageIcon(scaledImage));
            }
            mapImageLabels[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            mapImageLabels[i].setPreferredSize(new Dimension(180, 180));
            
            mapContainer.add(nameLabel, BorderLayout.NORTH);
            mapContainer.add(mapImageLabels[i], BorderLayout.CENTER);
            
            mapPreviewPanel.add(mapContainer);
        }
        
        // Highlight first map initially
        updateMapPreview();
    }
    
    private void updateMapPreview() {
        int selectedIndex = mapSelector.getSelectedIndex();
        
        for (int i = 0; i < mapImageLabels.length; i++) {
            if (i == selectedIndex) {
                // Highlight selected map
                mapImageLabels[i].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
            } else {
                // Normal border
                mapImageLabels[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getPlayerCount() {
        String selected = (String) playerCountSelector.getSelectedItem();
        return Character.getNumericValue(selected.charAt(0));
    }

    public String getSelectedMap() {
        return (String) mapSelector.getSelectedItem();
    }

    public int getInitialWaterLevel() {
        int selectedIndex = difficultySelector.getSelectedIndex();
        return selectedIndex + 1; // NOVICE=1, NORMAL=2, ELITE=3, LEGENDAIRE=4
    }
}