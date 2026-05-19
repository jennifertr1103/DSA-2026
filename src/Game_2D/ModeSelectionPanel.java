package Game_2D;

import tile.TileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ModeSelectionPanel extends JPanel {
    private final Main main;
    private final GamePanel gp;

    private boolean isMultiplayer = false;
    private TileManager.MapType selectedMap = TileManager.MapType.CLASSIC;

    private JButton p1Btn, p2Btn, map1Btn, map2Btn, playBtn;
    private BufferedImage background;

    public ModeSelectionPanel(Main main, GamePanel gp) {
        this.main = main;
        this.gp = gp;

        this.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT + GamePanel.HUD_HEIGHT));
        setLayout(null);

        try {
            background = ImageIO.read(getClass().getResource("/res/UI/ModeSelectionPanel.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load ModeSelectionPanel.png");
            setBackground(new Color(40, 45, 52));
        }

        int centerX = GamePanel.WIDTH / 2;
        int startXButtons = centerX - 20;

        // Note: Labels "PLAYER" and "MAP" are now part of the background image.
        // We only add the functional buttons.

        // Position for buttons next to the image labels
        p1Btn = createOptionButton("/res/button/1.png", startXButtons, 280);
        p2Btn = createOptionButton("/res/button/2.png", startXButtons + 120, 280);
        add(p1Btn);
        add(p2Btn);

        map1Btn = createOptionButton("/res/button/1.png", startXButtons, 460);
        map2Btn = createOptionButton("/res/button/2.png", startXButtons + 120, 460);
        add(map1Btn);
        add(map2Btn);

        // Play Button (Confirmation)
        playBtn = new JButton();
        try {
            java.net.URL playImgURL = getClass().getResource("/res/button/play.png");
            if (playImgURL != null) {
                BufferedImage playImg = ImageIO.read(playImgURL);
                int targetHeight = 150;
                double ratio = (double) playImg.getWidth() / playImg.getHeight();
                int targetWidth = (int) (targetHeight * ratio);
                Image scaledPlay = playImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                playBtn.setIcon(new ImageIcon(scaledPlay));
                playBtn.setBounds(centerX - targetWidth / 2, 600, targetWidth, targetHeight);
            } else {
                playBtn.setText("PLAY");
                playBtn.setBounds(centerX - 150, 500, 300, 90);
            }
        } catch (Exception e) {
            playBtn.setText("PLAY");
            playBtn.setBounds(centerX - 150, 500, 300, 90);
        }
        
        playBtn.setFocusPainted(false);
        playBtn.setContentAreaFilled(false);
        playBtn.setBorderPainted(false);
        playBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(playBtn);

        // Initial Selection State
        updateButtonSelection();

        // Listeners
        p1Btn.addActionListener(e -> { isMultiplayer = false; updateButtonSelection(); });
        p2Btn.addActionListener(e -> { isMultiplayer = true;  updateButtonSelection(); });
        map1Btn.addActionListener(e -> { selectedMap = TileManager.MapType.CLASSIC; updateButtonSelection(); });
        map2Btn.addActionListener(e -> { selectedMap = TileManager.MapType.IMAGE;   updateButtonSelection(); });

        playBtn.addActionListener(e -> {
            gp.setMultiplayer(isMultiplayer);
            gp.tileM.setMap(selectedMap);
            main.showCharacterSelection();
        });

        // Back button
        JButton backBtn = new JButton();
        try {
            java.net.URL backImgURL = getClass().getResource("/res/button/back.png");
            if (backImgURL != null) {
                BufferedImage backImg = ImageIO.read(backImgURL);
                int targetHeight = 60; // Slightly smaller than play button
                double ratio = (double) backImg.getWidth() / backImg.getHeight();
                int targetWidth = (int) (targetHeight * ratio);
                Image scaledBack = backImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                backBtn.setIcon(new ImageIcon(scaledBack));
                backBtn.setBounds(20, (GamePanel.HEIGHT + GamePanel.HUD_HEIGHT) - targetHeight - 60, targetWidth, targetHeight);
            } else {
                backBtn.setText("BACK");
                backBtn.setBounds(20, (GamePanel.HEIGHT + GamePanel.HUD_HEIGHT) - 110, 100, 50);
            }
        } catch (Exception e) {
            backBtn.setText("BACK");
            backBtn.setBounds(20, (GamePanel.HEIGHT + GamePanel.HUD_HEIGHT) - 110, 100, 50);
        }

        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> main.showSelection());
        add(backBtn);
    }

    private JButton createOptionButton(String path, int x, int y) {
        JButton btn = new JButton();
        btn.setBounds(x, y, 120, 120);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);

        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception e) {
            System.err.println("Could not load button image: " + path);
            btn.setText("?");
        }

        return btn;
    }

    private void updateButtonSelection() {
        highlightButton(p1Btn, !isMultiplayer);
        highlightButton(p2Btn, isMultiplayer);
        highlightButton(map1Btn, selectedMap == TileManager.MapType.CLASSIC);
        highlightButton(map2Btn, selectedMap == TileManager.MapType.IMAGE);
        
        repaint();
    }

    private void highlightButton(JButton btn, boolean selected) {
        if (selected) {
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            btn.setBorderPainted(true);
            btn.setOpaque(true);
            btn.setBackground(new Color(255, 140, 170, 120));
        } else {
            btn.setBorder(null);
            btn.setBorderPainted(false);
            btn.setOpaque(false);
            btn.setBackground(new Color(0, 0, 0, 0));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
