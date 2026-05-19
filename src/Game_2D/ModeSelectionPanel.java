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
            background = ImageIO.read(getClass().getResource("/res/UI/BG2.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load BG2.png");
            setBackground(new Color(40, 45, 52));
        }

        Font labelFont = new Font("SansSerif", Font.BOLD, 40);
        Color textColor = new Color(240, 240, 240); // Light color for dark backgrounds

        int centerX = GamePanel.WIDTH / 2;
        int startXLabel = centerX - 250;
        int startXButtons = centerX + 20;

        // Row 1: PLAYER
        JLabel playerLabel = new JLabel("PLAYER");
        playerLabel.setFont(labelFont);
        playerLabel.setForeground(textColor);
        playerLabel.setBounds(startXLabel, 200, 200, 60);
        add(playerLabel);

        p1Btn = createOptionButton("/res/button/1.png", startXButtons, 200);
        p2Btn = createOptionButton("/res/button/2.png", startXButtons + 120, 200);
        add(p1Btn);
        add(p2Btn);

        // Row 2: MAP
        JLabel mapLabel = new JLabel("MAP");
        mapLabel.setFont(labelFont);
        mapLabel.setForeground(textColor);
        mapLabel.setBounds(startXLabel, 330, 200, 60);
        add(mapLabel);

        map1Btn = createOptionButton("/res/button/1.png", startXButtons, 330);
        map2Btn = createOptionButton("/res/button/2.png", startXButtons + 120, 330);
        add(map1Btn);
        add(map2Btn);

        // Play Button (Confirmation)
        playBtn = new JButton();
        try {
            java.net.URL playImgURL = getClass().getResource("/res/button/play.png");
            if (playImgURL != null) {
                BufferedImage playImg = ImageIO.read(playImgURL);
                int targetHeight = 100;
                double ratio = (double) playImg.getWidth() / playImg.getHeight();
                int targetWidth = (int) (targetHeight * ratio);
                Image scaledPlay = playImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                playBtn.setIcon(new ImageIcon(scaledPlay));
                playBtn.setBounds(centerX - targetWidth / 2, 500, targetWidth, targetHeight);
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
            main.showGame();
        });

        // Back button to return to selection
        JButton backBtn = new JButton("BACK");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        backBtn.setBounds(20, (GamePanel.HEIGHT + GamePanel.HUD_HEIGHT) - 70, 100, 50);
        backBtn.setBackground(new Color(80, 80, 80));
        backBtn.setForeground(Color.WHITE);
        backBtn.addActionListener(e -> main.showSelection());
        add(backBtn);
    }

    private JButton createOptionButton(String path, int x, int y) {
        JButton btn = new JButton();
        btn.setBounds(x, y, 80, 80);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);

        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                Image scaledImg = img.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
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
