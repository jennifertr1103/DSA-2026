package Game_2D;

import sound.SoundManager.SoundType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class StartPanel extends JPanel {
    private BufferedImage background;
    private final Main main;
    private JButton startButton;
    private JButton howToPlayButton;

    public StartPanel(Main main) {
        this.main = main;
        this.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT + GamePanel.HUD_HEIGHT));
        this.setLayout(null);

        try {
            background = ImageIO.read(getClass().getResource("/res/UI/StartPanel.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load StartPanel.png");
        }

        // Tạo nút bằng hình ảnh
        startButton = createImageButton("/res/button/start.png");
        howToPlayButton = createImageButton("/res/button/how to play.png");

        // Căn chỉnh vị trí
        int centerX = GamePanel.WIDTH / 2;
        int startY = 450;

        if (startButton != null) {
            int sw = startButton.getPreferredSize().width;
            int sh = startButton.getPreferredSize().height;
            startButton.setBounds(centerX - sw / 2, startY, sw, sh);
            add(startButton);
            startButton.addActionListener(e -> main.showSelection());
            
            // Nút How to play sẽ nằm dưới nút Start một khoảng hợp lý
            if (howToPlayButton != null) {
                int hw = howToPlayButton.getPreferredSize().width;
                int hh = howToPlayButton.getPreferredSize().height;
                howToPlayButton.setBounds(centerX - hw / 2, startY + sh + 20, hw, hh);
                add(howToPlayButton);
                howToPlayButton.addActionListener(e -> main.showInstructions());
            }
        }
    }

    private JButton createImageButton(String path) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                if (img != null) {
                    int targetHeight = 100;
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
        
        // Fill entire panel with black first (including HUD area)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (background != null) {
            // Draw background only in the game area (1024x768), not stretching to HUD
            g.drawImage(background, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            // Draw title if background is missing
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "BOM IT";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(title, (getWidth() - fm.stringWidth(title))/2, 200);
        }
    }
}
