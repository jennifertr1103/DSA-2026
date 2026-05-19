package Game_2D;

import entity.Entity.CharacterTier;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectionPanel extends JPanel {
    private final Main main;
    private final GamePanel gp;
    private BufferedImage background;
    
    private int currentPlayerSelecting = 1;
    private String p1Model, p2Model;
    private CharacterTier p1Tier, p2Tier;
    
    private JLabel playerTitle;
    private JPanel cardPanel;
    private ImageIcon p1TitleIcon, p2TitleIcon;
    
    private static class CharacterInfo {
        String name;
        String cardPath;
        String modelPath;
        CharacterTier tier;

        CharacterInfo(String name, String cardPath, String modelPath, CharacterTier tier) {
            this.name = name;
            this.cardPath = cardPath;
            this.modelPath = modelPath;
            this.tier = tier;
        }
    }
    
    private final List<CharacterInfo> characters = new ArrayList<>();

    public CharacterSelectionPanel(Main main, GamePanel gp) {
        this.main = main;
        this.gp = gp;
        this.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT + GamePanel.HUD_HEIGHT));
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(30, 30, 40));

        try {
            background = ImageIO.read(getClass().getResource("/res/UI/BG1.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load BG2.png");
        }

        initCharacters();
        initComponents();
    }

    private void initCharacters() {
        characters.add(new CharacterInfo("Model 1", "/res/player/Player Card/1.png", "Model 1", CharacterTier.SSR));
        characters.add(new CharacterInfo("Model 2", "/res/player/Player Card/2.png", "Model 2", CharacterTier.SSR));
        characters.add(new CharacterInfo("Model 3", "/res/player/Player Card/3.png", "Model 3", CharacterTier.SSR));
        characters.add(new CharacterInfo("Model 4", "/res/player/Player Card/4.png", "Model 4", CharacterTier.SSR));
        characters.add(new CharacterInfo("Model 5", "/res/player/Player Card/5.png", "Model 5", CharacterTier.SSR));
        characters.add(new CharacterInfo("SSR 1", "/res/player/Player Card/srr1.png", "SSR 1", CharacterTier.SSR));
        characters.add(new CharacterInfo("SSR 2", "/res/player/Player Card/srr2.png", "SSR 2", CharacterTier.SSR));
        characters.add(new CharacterInfo("SSR 3", "/res/player/Player Card/srr3.png", "SSR 3", CharacterTier.SSR));
    }

    private void initComponents() {
        // Load title icons
        p1TitleIcon = loadTitleIcon("/res/button/c1.png");
        p2TitleIcon = loadTitleIcon("/res/button/c2.png");

        playerTitle = new JLabel(p1TitleIcon);
        playerTitle.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 0));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(playerTitle);
        add(topPanel, BorderLayout.NORTH);

        cardPanel = new JPanel(new GridLayout(2, 4, 20, 20));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 50));

        for (CharacterInfo info : characters) {
            JButton cardBtn = createCardButton(info);
            cardPanel.add(cardBtn);
        }

        add(cardPanel, BorderLayout.CENTER);
        
        // Back button
        JButton backBtn = new JButton("BACK");
        backBtn.setFont(new Font("Arial", Font.BOLD, 20));
        backBtn.addActionListener(e -> {
            if (currentPlayerSelecting == 2) {
                currentPlayerSelecting = 1;
                updateUIForPlayer();
            } else {
                main.showModeSelection();
            }
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0)); // Move up by 40px
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createCardButton(CharacterInfo info) {
        JButton btn = new JButton();
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            java.net.URL imgURL = getClass().getResource(info.cardPath);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                // Scale to fit grid cell nicely
                int targetHeight = 250;
                double ratio = (double) img.getWidth() / img.getHeight();
                int targetWidth = (int) (targetHeight * ratio);
                Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception e) {
            btn.setText(info.name);
            btn.setForeground(Color.WHITE);
        }

        btn.addActionListener(e -> selectCharacter(info));
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBorderPainted(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBorderPainted(false);
            }
        });

        return btn;
    }

    private void selectCharacter(CharacterInfo info) {
        if (currentPlayerSelecting == 1) {
            p1Model = info.modelPath;
            p1Tier = info.tier;
            if (gp.multiplayer) {
                currentPlayerSelecting = 2;
                updateUIForPlayer();
            } else {
                finishSelection();
            }
        } else {
            p2Model = info.modelPath;
            p2Tier = info.tier;
            finishSelection();
        }
    }

    private void updateUIForPlayer() {
        if (currentPlayerSelecting == 1) {
            playerTitle.setIcon(p1TitleIcon);
        } else {
            playerTitle.setIcon(p2TitleIcon);
        }
        repaint();
    }

    private ImageIcon loadTitleIcon(String path) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                // Scale title image to a reasonable size
                int targetHeight = 60;
                double ratio = (double) img.getWidth() / img.getHeight();
                int targetWidth = (int) (targetHeight * ratio);
                Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            System.err.println("Could not load title icon: " + path);
        }
        return null;
    }

    private void finishSelection() {
        gp.setP1Character(p1Model, p1Tier);
        if (gp.multiplayer) {
            gp.setP2Character(p2Model, p2Tier);
        }
        main.showGame();
        
        // Reset for next time
        currentPlayerSelecting = 1;
        updateUIForPlayer();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
