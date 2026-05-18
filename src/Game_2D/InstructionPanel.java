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

        backButton = new JButton("BACK");
        backButton.setBounds(20, 20, 100, 40);
        styleButton(backButton);
        add(backButton);

        backButton.addActionListener(e -> main.showMenu());
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
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
