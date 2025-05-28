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

    public PlayerInfoView(GameController gameController) {
        this.gameController = gameController;
        initializeUI();
        setPlayerName("Player");
    }

    private JButton[] actionButtons;

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(""));

        // 左侧区域：玩家信息
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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
        sandbagButton = new JButton("使用沙袋卡");
        sandbagButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sandbagButton.setBackground(new Color(255, 204, 102));
        sandbagButton.setFont(new Font("Arial", Font.BOLD, 12));
        sandbagButton.setFocusPainted(false);
        sandbagButton.setEnabled(true); // 默认可用，后续由控制器控制

        // 添加"使用直升机卡"按钮
        helicopterButton = new JButton("直升机");
        helicopterButton.setPreferredSize(new Dimension(80, 30));
        helicopterButton.setBackground(new Color(102, 204, 255));
        helicopterButton.setFont(new Font("Arial", Font.BOLD, 12));
        helicopterButton.setFocusPainted(false);
        helicopterButton.setEnabled(true); // 默认可用，后续由控制器控制
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
        JPanel actionButtonsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtonsPanel.setMaximumSize(new Dimension(200, 120));

        // 创建并添加动作按钮
        String[] actionNames = { "Move", "Shore up", "Give Cards", "Special Skill", "Get Treasure", "Skip" };
        actionButtons = new JButton[actionNames.length];
        for (int i = 0; i < actionNames.length; i++) {
            actionButtons[i] = createActionButton(actionNames[i]);
            actionButtonsPanel.add(actionButtons[i]);
        }

        leftPanel.add(actionButtonsPanel);
        add(leftPanel, BorderLayout.WEST);

        // 右侧区域：卡牌
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        cardsPanel.setBorder(BorderFactory.createTitledBorder("Cards"));
        cardsPanel.setBackground(new Color(240, 240, 240));
        add(cardsPanel, BorderLayout.CENTER);

        // 设置首选大小和最小大小
        setPreferredSize(new Dimension(700, 200));
        setMinimumSize(new Dimension(700, 200));

        // 调整卡牌区域的大小，确保能显示足够多的卡牌
        cardsPanel.setPreferredSize(new Dimension(700, 120));
    }

    public void setPlayerName(String name) {
        playerNameLabel.setText(name);
        updateTitle();
    }

    public void setRole(String role) {
        roleLabel.setText(role);
        updateTitle();
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
        button.setFocusPainted(false);
        button.setBackground(new Color(51, 122, 183));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 109, 164), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
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
