package View;

import javax.swing.*;
import java.awt.*;

public class SetupView extends JPanel {
    private boolean confirmed = false;
    private JComboBox<String> playerCountSelector;
    private JComboBox<String> mapSelector;
    private JCheckBox randomGenerationCheckbox;

    private MainView mainView;

    public SetupView(MainView mainView) {
        this.mainView = mainView;
        initializeUI();
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(800, 600));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, gbc);

        // Player count selection
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Number of Players:"), gbc);

        gbc.gridx = 1;
        String[] playerCounts = {"2 Players", "3 Players", "4 Players"};
        playerCountSelector = new JComboBox<>(playerCounts);
        add(playerCountSelector, gbc);

        // Map selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Select Map:"), gbc);

        gbc.gridx = 1;
        String[] mapOptions = {"CLASSIC", "ADVANCED", "EXPERT"};
        mapSelector = new JComboBox<>(mapOptions);
        add(mapSelector, gbc);

        // Random generation option
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        randomGenerationCheckbox = new JCheckBox("Generate Random Map");
        add(randomGenerationCheckbox, gbc);

        // Button panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(e -> {
            confirmed = true;
            mainView.confirmSetup();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            mainView.showStartScreen();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);
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

    public boolean isRandomGeneration() {
        return randomGenerationCheckbox.isSelected();
    }
}