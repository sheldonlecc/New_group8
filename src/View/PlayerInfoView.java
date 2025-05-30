package View;

import Model.Cards.Card;
import Controller.GameController;

import javax.swing.*;
import java.awt.*;

public class PlayerInfoView extends JPanel {
    private JLabel playerNameLabel;
    private JLabel roleLabel;
    private JLabel actionPointsLabel;
    private JPanel cardsPanel;
    private GameController gameController;
    private static final int MAX_CARDS = 7;
    private JButton sandbagButton;
    private JButton helicopterButton;
    private JLabel roleIconLabel;
    private int playerCount;
    private JButton[] actionButtons;

    public PlayerInfoView(GameController gameController) {
        this.gameController = gameController;
        this.playerCount = gameController.getPlayerCount();
        initializeUI();
        setPlayerName("Player");
    }

    private void initializeUI() {
        setLayout(new BorderLayout(3, 3));
        setBorder(BorderFactory.createTitledBorder(""));
    

        int panelWidth, panelHeight;
        if (playerCount <= 2) {
            panelWidth = 750;
            panelHeight = 170;
        } else {
            panelWidth = 750;
            panelHeight = 165;
        }
        
        setMaximumSize(new Dimension(panelWidth, panelHeight));
        setPreferredSize(new Dimension(panelWidth - 20, panelHeight - 20));

        // 左侧区域：玩家信息
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        // 隐藏roleLabel和playerNameLabel，只在标题中显示
        roleLabel = new JLabel("Role");
        roleLabel.setVisible(false);

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setVisible(false);

        // 只显示行动点数
        actionPointsLabel = new JLabel("Actions: 3");
        actionPointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(actionPointsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 添加"使用沙袋卡"按钮
        sandbagButton = new JButton("Sandbag");
        sandbagButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sandbagButton.setPreferredSize(new Dimension(90, 25)); // 统一尺寸
        sandbagButton.setBackground(new Color(255, 204, 102));
        sandbagButton.setFont(new Font("Arial", Font.BOLD, 11)); // 减小字体
        sandbagButton.setFocusPainted(false);
        sandbagButton.setEnabled(true);

        // 添加"使用直升机卡"按钮
        helicopterButton = new JButton("Helicopter");
        helicopterButton.setPreferredSize(new Dimension(90, 25)); // 保持统一尺寸
        helicopterButton.setBackground(new Color(102, 204, 255));
        helicopterButton.setFont(new Font("Arial", Font.BOLD, 11)); // 减小字体
        helicopterButton.setFocusPainted(false);
        helicopterButton.setEnabled(true);
        helicopterButton.addActionListener(e -> {
            System.out.println("\n========== 直升机按钮被点击 ==========");
            System.out.println("当前玩家索引: " + gameController.getPlayerInfoViews().indexOf(this));
            System.out.println("直升机按钮状态: " + (helicopterButton.isEnabled() ? "启用" : "禁用"));
            System.out.println("正在检查直升机卡...");
            gameController.handleHelicopterCard(gameController.getPlayerInfoViews().indexOf(this));
            System.out.println("========== 直升机按钮点击事件结束 ==========\n");
        });

        // 创建一个水平布局的面板来放置这两个按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(sandbagButton);
        buttonPanel.add(helicopterButton);
        leftPanel.add(buttonPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 添加动作按钮面板
        JPanel actionButtonsPanel = new JPanel(new GridLayout(3, 2, 2, 2)); // 减小间距
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtonsPanel.setMaximumSize(new Dimension(200, 100)); // 增加宽度

        // 创建并添加动作按钮
        String[] actionNames = { "Move", "Shore up", "Give Cards", "Special", "Treasure", "Skip" }; // 缩短文本
        actionButtons = new JButton[actionNames.length];
        for (int i = 0; i < actionNames.length; i++) {
            actionButtons[i] = createActionButton(actionNames[i]);
            actionButtonsPanel.add(actionButtons[i]);
        }

        leftPanel.add(actionButtonsPanel);
        add(leftPanel, BorderLayout.WEST);

        // 中间区域：卡牌 - 根据玩家数量调整卡牌区域大小
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        cardsPanel.setBorder(BorderFactory.createTitledBorder("Cards"));
        cardsPanel.setBackground(new Color(240, 240, 240));
        
        int cardsPanelWidth = (playerCount <= 2) ? 120 : 100; // 3-4人时减小卡牌区域
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 100));
        add(cardsPanel, BorderLayout.CENTER);
        
        // 右侧区域：角色图标 - 根据玩家数量调整图标区域大小
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Role"));
        
        int iconPanelWidth = (playerCount <= 2) ? 90 : 80; // 3-4人时减小图标区域
        rightPanel.setPreferredSize(new Dimension(iconPanelWidth, 100));
        
        roleIconLabel = new JLabel();
        roleIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        int iconSize = (playerCount <= 2) ? 60 : 50; // 3-4人时减小图标尺寸
        roleIconLabel.setPreferredSize(new Dimension(iconSize, 80));
        
        rightPanel.add(roleIconLabel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // 设置最终尺寸
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(new Dimension(panelWidth, panelHeight));

        // 调整卡牌区域的大小
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(226, 232, 240)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        cardsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // 增加卡牌面板宽度以适应更大的卡牌
        cardsPanelWidth = (playerCount <= 2) ? 160 : 140;  // 从140/120增加到160/140
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 135));  // 高度也稍微增加
        add(cardsPanel, BorderLayout.CENTER);
        
        // 右侧区域：角色图标 - 根据玩家数量调整图标区域大小
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Role"));
        
        iconPanelWidth = (playerCount <= 2) ? 90 : 80; // 3-4人时减小图标区域
        rightPanel.setPreferredSize(new Dimension(iconPanelWidth, 100));
        
        roleIconLabel = new JLabel();
        roleIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        iconSize = (playerCount <= 2) ? 60 : 50; // 3-4人时减小图标尺寸
        roleIconLabel.setPreferredSize(new Dimension(iconSize, 80));
        
        rightPanel.add(roleIconLabel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // 设置最终尺寸
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(new Dimension(panelWidth, panelHeight));

        // 调整卡牌区域的大小
        cardsPanel.setPreferredSize(new Dimension(cardsPanelWidth, 120));
    }
    
    public void setPlayerName(String name) {
        playerNameLabel.setText(name);
        updateTitle();
    }
    
    public void setRole(String role) {
        roleLabel.setText(role);
        updateTitle();
        updateRoleIcon(role); // 更新角色图标
    }
    
    // 新增方法：更新角色图标
    private void updateRoleIcon(String role) {
        if (role != null && !role.equals("Role") && !role.equals("Not Assigned")) {
            try {
                String iconPath = "src/resources/Player/" + role + "_Icon.png";
                ImageIcon icon = new ImageIcon(iconPath);
                // 缩放图标到合适大小
                Image scaledImage = icon.getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH);
                roleIconLabel.setIcon(new ImageIcon(scaledImage));
                roleIconLabel.setText("");
            } catch (Exception e) {
                System.err.println("无法加载角色图标: " + role);
                roleIconLabel.setIcon(null);
                roleIconLabel.setText(role);
            }
        } else {
            roleIconLabel.setIcon(null);
            roleIconLabel.setText("No Role");
        }
    }

    public void setActionPoints(int points) {
        actionPointsLabel.setText("Actions: " + points);
    }

    public JPanel getCardsPanel() {
        return cardsPanel;
    }

    public void addCard(Card card) {
        gameController.getCardController().addCard(this, card);
    }

    public void removeCard(Card card) {
        gameController.getCardController().removeCard(this, card);
    }

    public void clearCards() {
        gameController.getCardController().clearCards(this);
    }

    public JLabel getActionPointsLabel() {
        return actionPointsLabel;
    }

    private void updateTitle() {
        String playerName = playerNameLabel.getText();
        String role = roleLabel.getText();
        if (!role.equals("Role") && !role.equals("Not Assigned")) {
            setBorder(BorderFactory.createTitledBorder(playerName + ": " + role));
        } else {
            setBorder(BorderFactory.createTitledBorder(playerName));
        }
    }

    private JButton createActionButton(String actionName) {
        JButton button = new JButton(actionName);
        button.setPreferredSize(new Dimension(90, 30)); // 统一尺寸
        button.setFocusPainted(false);
        button.setBackground(new Color(51, 122, 183));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12)); // 减小字体以适应按钮
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 109, 164), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4))); // 减小内边距
        button.addActionListener(e -> performAction(actionName));
        return button;
    }

    private void performAction(String actionName) {
        // 获取当前玩家的索引
        int currentPlayerIndex = gameController.getPlayerInfoViews().indexOf(this);
        // 调用GameController的performAction方法
        gameController.performAction(currentPlayerIndex, actionName);
    }

    public void setButtonsEnabled(boolean enabled) {
        if (actionButtons != null) {
            for (JButton button : actionButtons) {
                button.setEnabled(enabled);
                if (enabled) {
                    button.setBackground(new Color(51, 122, 183));
                } else {
                    button.setBackground(new Color(180, 180, 180));
                }
            }
        }
    }

    public JButton getSandbagButton() {
        return sandbagButton;
    }

    public JButton getHelicopterButton() {
        return helicopterButton;
    }
}
