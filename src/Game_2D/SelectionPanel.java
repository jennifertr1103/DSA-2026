package Game_2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SelectionPanel extends JPanel {
    private BufferedImage background;
    private final Main main;
    private JButton playButton;
    private JButton gachaButton;
    private JButton backButton;
    private JButton mapButton;

    public SelectionPanel(Main main) {
        this.main = main;
        this.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT + GamePanel.HUD_HEIGHT));
        this.setLayout(null);

        try {
            background = ImageIO.read(getClass().getResource("/res/UI/StartPanel.png"));
        } catch (Exception e) {
            System.err.println("Could not load StartPanel.png");
        }

        // Create buttons
        playButton = createImageButton("/res/button/play.png", 150);
        gachaButton = createImageButton("/res/button/gacha.png", 150);
        backButton = createImageButton("/res/button/back.png", 60);

        // Nút đổi Map (dùng text tạm thời vì chưa có image)
        mapButton = new JButton("MAP: CLASSIC");
        mapButton.setFont(new Font("Arial", Font.BOLD, 20));
        mapButton.setBackground(new Color(70, 80, 100));
        mapButton.setForeground(Color.WHITE);
        mapButton.setFocusPainted(false);

        int centerX = Game_2D.GamePanel.WIDTH / 2;
        int screenHeight = Game_2D.GamePanel.HEIGHT + GamePanel.HUD_HEIGHT;

        int buttonHeight = 150;
        int gap = 15;
        int startY = (screenHeight - (buttonHeight * 2 + gap)) / 2 + 50;

        if (playButton != null) {
            int bw = playButton.getPreferredSize().width;
            int bh = playButton.getPreferredSize().height;
            playButton.setBounds(centerX - bw / 2, startY + 50, bw, bh);
            add(playButton);
            playButton.addActionListener(e -> main.showModeSelection());
        }

        if (gachaButton != null) {
            int bw = gachaButton.getPreferredSize().width;
            int bh = gachaButton.getPreferredSize().height;
            gachaButton.setBounds(centerX - bw / 2, startY + 200, bw, bh);
            add(gachaButton);
            gachaButton.addActionListener(e -> main.showGacha());
        }

        if (backButton != null) {
            int bw = backButton.getPreferredSize().width;
            int bh = backButton.getPreferredSize().height;
            // Place at bottom left, moved up to offset 60
            backButton.setBounds(20, screenHeight - bh - 60, bw, bh);
            add(backButton);
            backButton.addActionListener(e -> main.showMenu());
        }
    }

    private JButton createImageButton(String path, int targetHeight) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                if (img != null) {
                    double ratio = (double) img.getWidth() / img.getHeight();
                    int targetWidth = (int) (targetHeight * ratio);

                    Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaledImg);

                    JButton btn = new JButton(icon);
                    btn.setPreferredSize(new Dimension(targetWidth, targetHeight));
                    btn.setBorder(BorderFactory.createEmptyBorder());
                    btn.setContentAreaFilled(false);
                    btn.setFocusPainted(false);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    return btn;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load button: " + path);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (background != null) {
            g.drawImage(background, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("SELECTION SCREEN", GamePanel.WIDTH/2 - 200, 200);
        }
    }
}
