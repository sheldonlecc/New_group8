package View;

import Model.Cards.Card;
import Model.Cards.TreasureCard;
import Model.Enumeration.CardType;

import javax.swing.*;
import java.awt.*;

public class CardView extends JButton {
    private Card card;
    private static final int CARD_WIDTH = 75;
    private int cardHeight;
    private static final Font CARD_FONT = new Font("Microsoft YaHei", Font.BOLD, 12);
    private static final String CARD_PATH = "/resources/Card/";

    public CardView(Card card, int playerCount) {
        this.card = card;
        this.cardHeight = (playerCount == 2) ? 120 : 100;
        initializeUI();
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(CARD_WIDTH, cardHeight));
        setMargin(new Insets(2, 2, 2, 2));
        setFont(CARD_FONT);
        setContentAreaFilled(true);
        setOpaque(true);
        setFocusPainted(false);
        
        // 加载卡牌图片
        String imagePath = CARD_PATH + card.getName() + ".png";
        
        if (!imagePath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
                Image image = icon.getImage().getScaledInstance(CARD_WIDTH, cardHeight, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(image));
                setText("");
            } catch (Exception e) {
                System.err.println("无法加载图片: " + imagePath);
                setText(card.getName());
            }
        } else {
            setText(card.getName());
        }
        
        // 添加鼠标事件监听器来实现自定义点击效果
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                setBackground(getBackground().darker());
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateCardStyle();
            }
        });
        
        // 设置工具提示
        setToolTipText(card.getDescription());

        // 设置卡牌布局
        setLayout(new BorderLayout(2, 2));
        
        updateCardStyle();
    }
    
    private void updateCardStyle() {
        // 只为非宝藏卡设置背景色
        if (!(card instanceof TreasureCard)) {
            switch (card.getType()) {
                case FLOOD:
                    setBackground(new Color(30, 144, 255)); // 蓝色
                    break;
                case HELICOPTER:
                case SAND_BAG:
                case WATER_RISE:
                    setBackground(new Color(147, 112, 219)); // 紫色
                    break;
                default:
                    setBackground(new Color(255, 255, 255)); // 白色
                    break;
            }
        }
        
        // 添加边框效果
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        setForeground(Color.BLACK);
    }

    public Card getCard() {
        return card;
    }
}
