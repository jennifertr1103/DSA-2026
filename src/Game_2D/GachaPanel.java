package Game_2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GachaPanel extends JPanel {
    private BufferedImage background;
    private final Main main;
    private JButton backButton;

    public GachaPanel(Main main) {
        this.main = main;
        this.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT + GamePanel.HUD_HEIGHT));
        this.setLayout(null);

        // Placeholder for Gacha background
        try {
            background = ImageIO.read(getClass().getResource("/res/UI/GachaPanel.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load GachaPanel.png");
        }

        backButton = createImageButton("/res/button/back.png");
        if (backButton != null) {
            int bw = backButton.getPreferredSize().width;
            int bh = backButton.getPreferredSize().height;
            backButton.setBounds(20, (GamePanel.HEIGHT + GamePanel.HUD_HEIGHT) - bh - 60, bw, bh);
            add(backButton);
            backButton.addActionListener(e -> main.showSelection());
        }
    }

    private JButton createImageButton(String path) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                if (img != null) {
                    int targetHeight = 80;
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
        g.setColor(new Color(30, 10, 50)); // Gacha theme color
        g.fillRect(0, 0, getWidth(), getHeight());

        if (background != null) {
            g.drawImage(background, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GACHA SYSTEM", GamePanel.WIDTH/2 - 150, 200);
            g.setFont(new Font("Arial", Font.ITALIC, 20));
            g.drawString("Coming Soon...", GamePanel.WIDTH/2 - 60, 250);
        }
    }
}
