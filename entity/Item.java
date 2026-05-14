package entity;

import Game_2D.GamePanel;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class Item {
    private int col, row;
    private ItemType type;
    private int lifespan; // số tick còn lại (20s = 20 * 60 = 1200 ticks)
    private boolean expired = false;
    private int animationTick = 0;

    public Item(GamePanel gp, int col, int row, ItemType type) {
        this.col = col;
        this.row = row;
        this.type = type;
        this.lifespan = 20 * 60; // 20 giây với 60 FPS = 1200 ticks
    }

    public void update() {
        lifespan--;
        animationTick++;
        if (lifespan <= 0) {
            expired = true;
        }
    }

    public boolean isExpired() {
        return expired;
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public ItemType getType() { return type; }

    public void draw(Graphics2D g2, int tileSize) {
        if (expired) return;

        int x = col * tileSize;
        int y = row * tileSize;
        int centerX = x + tileSize / 2;
        int centerY = y + tileSize / 2;
        int size = tileSize / 2;

        // Hiệu ứng nhấp nháy (lung linh)
        float alpha = 0.7f + (float)Math.sin(animationTick * 0.1) * 0.3f;
        Composite original = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(centerX - size/2 + 2, centerY - size/2 + 3, size, size);

        switch (type) {
            case SPEED_BOOST:
                drawSpeedBoost(g2, centerX, centerY, size);
                break;
            case EXTRA_LIFE:
                drawHeart(g2, centerX, centerY, size);
                break;
        }

        g2.setComposite(original);
    }

    private void drawSpeedBoost(Graphics2D g2, int cx, int cy, int size) {
        // Vẽ đôi giày thể thao
        g2.setColor(new Color(100, 200, 255)); // Xanh dương sáng
        g2.fillRoundRect(cx - size/3, cy - size/4, size/2, size/2, 5, 5);
        g2.setColor(new Color(50, 150, 255));
        g2.fillRect(cx - size/4, cy - size/4, size/8, size/2);
        g2.fillRect(cx + size/8, cy - size/4, size/8, size/2);

        // Tia sáng
        g2.setColor(new Color(255, 255, 200));
        for (int i = 0; i < 3; i++) {
            double angle = (animationTick * 0.1 + i) * Math.PI * 2 / 3;
            int px = cx + (int)(Math.cos(angle) * size/2);
            int py = cy + (int)(Math.sin(angle) * size/2);
            g2.fillOval(px - 2, py - 2, 4, 4);
        }
    }

    private void drawHeart(Graphics2D g2, int cx, int cy, int size) {
        // Vẽ trái tim
        g2.setColor(new Color(255, 80, 120));
        // Nửa trái tim
        g2.fillOval(cx - size/3, cy - size/4, size/3, size/3);
        g2.fillOval(cx, cy - size/4, size/3, size/3);
        g2.fillPolygon(
                new int[]{cx - size/3, cx + size/3, cx},
                new int[]{cy - size/8, cy - size/8, cy + size/4},
                3
        );

        // Hiệu ứng tim đập
        if (animationTick % 15 < 8) {
            g2.setColor(new Color(255, 150, 180, 150));
            g2.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }
}