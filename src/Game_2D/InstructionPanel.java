package Game_2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class InstructionPanel extends JPanel {
    private BufferedImage background;
    private final Main main;
    private JButton backButton;

    public InstructionPanel(Main main) {
        this.main = main;
        this.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT + GamePanel.HUD_HEIGHT));
        this.setLayout(null);

        try {
            background = ImageIO.read(getClass().getResource("/res/UI/InstructionPanel.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load InstructionPanel.png");
        }

        backButton = createImageButton("/res/button/back.png");
        if (backButton != null) {
            int bw = backButton.getPreferredSize().width;
            int bh = backButton.getPreferredSize().height;
            backButton.setBounds(20, (GamePanel.HEIGHT + GamePanel.HUD_HEIGHT) - bh - 20, bw, bh);
            add(backButton);
            backButton.addActionListener(e -> main.showMenu());
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

        // Fill entire panel with a dark color
        g.setColor(new Color(20, 20, 25));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (background != null) {
            // Draw background only in the game area (1024x768)
            g.drawImage(background, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("HOW TO PLAY", 50, 100);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("- Use WASD or Arrows to move", 50, 150);
            g.drawString("- Press SPACE or X to place bombs", 50, 180);
            g.drawString("- Press R to restart", 50, 210);
        }
    }
}
