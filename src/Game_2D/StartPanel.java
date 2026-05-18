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

        int btnWidth = 240;
        int btnHeight = 60;
        int centerX = (GamePanel.WIDTH - btnWidth) / 2;
        int startY = 520; // Middle bottom area

        startButton = new JButton("START");
        startButton.setBounds(centerX, startY, btnWidth, btnHeight);
        styleButton(startButton);

        howToPlayButton = new JButton("HOW TO PLAY");
        howToPlayButton.setBounds(centerX, startY + btnHeight + 20, btnWidth, btnHeight);
        styleButton(howToPlayButton);

        add(startButton);
        add(howToPlayButton);

        startButton.addActionListener(e -> main.showGame());
        howToPlayButton.addActionListener(e -> main.showInstructions());
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 140, 0)); // Dark Orange
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(255, 165, 0)); // Orange
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(255, 140, 0));
            }
        });
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
