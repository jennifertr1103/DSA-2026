package entity;

import Game_2D.GamePanel;
import Game_2D.KeyHandler2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Second human-controlled character (Player 2).
 * Controls: Arrow keys + Enter (bomb)
 */
public class Player2 extends Entity implements Destructible {

    private static final int SOLID_AREA_X = 8;
    private static final int SOLID_AREA_Y = 13;
    private static final int SOLID_AREA_W = 48;
    private static final int SOLID_AREA_H = 40;
    private static final int DEFAULT_SPEED = 4;

    private final GamePanel gp;
    private final KeyHandler2 keyH2;

    private int speedBoostTimer = 0;
    private int originalSpeed = DEFAULT_SPEED;

    public Player2(GamePanel gp, KeyHandler2 keyH2) {
        this.gp = gp;
        this.keyH2 = keyH2;

        solidArea.x = SOLID_AREA_X;
        solidArea.y = SOLID_AREA_Y;
        solidArea.width = SOLID_AREA_W;
        solidArea.height = SOLID_AREA_H;

        setDefaultValues();
    }

    private void setDefaultValues() {
        worldX = gp.tileSize * 14;  // Bottom-right corner (col 14)
        worldY = gp.tileSize * 10;  // row 10
        speed = DEFAULT_SPEED;
        originalSpeed = DEFAULT_SPEED;
        direction = Direction.UP;
        life = 3;
        alive = true;
    }

    public void applySpeedBoost(int boostAmount, int duration) {
        if (speedBoostTimer <= 0) {
            originalSpeed = speed;
            speed = originalSpeed + boostAmount;
        }
        speedBoostTimer = duration;
    }

    public void addLife() {
        if (life < 3) life++;
    }

    public int getSpeedBoostTimer() {
        return speedBoostTimer;
    }

    public int getCurrentSpeed() {
        return speed;
    }

    @Override
    public void update() {
        if (!alive) return;

        if (speedBoostTimer > 0) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) speed = originalSpeed;
        }

        // Bomb placement with Enter key
        if (keyH2.bombKeyPressed) {
            keyH2.consumeBombKey();
            gp.bombAlgo.placeBomb(getCol(), getRow(), this);
        }

        boolean anyKey = keyH2.upPress || keyH2.downPress || keyH2.leftPress || keyH2.rightPress;
        if (anyKey) {
            if (keyH2.upPress) direction = Direction.UP;
            else if (keyH2.downPress) direction = Direction.DOWN;
            else if (keyH2.leftPress) direction = Direction.LEFT;
            else if (keyH2.rightPress) direction = Direction.RIGHT;

            collisionOn = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) {
                worldX += direction.dx * speed;
                worldY += direction.dy * speed;
            }
            advanceWalkAnimation();
        }

        updateCommonLogic();
    }

    @Override
    public void onDestroyedByFlame() {
        takeDamage();
    }

    @Override
    public int getCol() {
        return (worldX + solidArea.x + solidArea.width / 2) / gp.tileSize;
    }

    @Override
    public int getRow() {
        return (worldY + solidArea.y + solidArea.height / 2) / gp.tileSize;
    }

    @Override
    public boolean isDestroyed() {
        return !alive;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;

        if (invincible && (System.currentTimeMillis() / 120) % 2 == 0) return;

        int x = worldX;
        int y = worldY;
        int s = gp.tileSize - 4;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(x + 8, y + s / 2 + 8, s - 10, s / 4);

        // ===== HAT =====
        g2.setColor(new Color(100, 180, 255));
        g2.fillRoundRect(x + s / 5, y + s / 8, 3 * s / 5, s / 2, 20, 20);

        // Cat ears
        g2.setColor(new Color(80, 150, 230));
        g2.fillPolygon(
                new int[]{x + s / 5 + 10, x + s / 5 + 25, x + s / 5 + 18},
                new int[]{y + s / 8, y + s / 8 + 15, y + s / 8 + 8},
                3
        );
        g2.fillPolygon(
                new int[]{x + s * 4 / 5 - 10, x + s * 4 / 5 - 25, x + s * 4 / 5 - 18},
                new int[]{y + s / 8, y + s / 8 + 15, y + s / 8 + 8},
                3
        );

        // ===== FACE =====
        g2.setColor(new Color(255, 240, 220));
        g2.fillOval(x + s / 4, y + s / 5, s / 2, s / 2);

        // Eyes
        g2.setColor(new Color(40, 60, 100));
        g2.fillOval(x + s * 5 / 16, y + s / 4, s / 8, s / 7);
        g2.fillOval(x + s * 9 / 16, y + s / 4, s / 8, s / 7);

        // Eye sparkles
        g2.setColor(Color.WHITE);
        g2.fillOval(x + s * 5 / 16 + 2, y + s / 4 + 2, s / 20, s / 20);
        g2.fillOval(x + s * 9 / 16 + 2, y + s / 4 + 2, s / 20, s / 20);

        // Nose
        g2.setColor(new Color(255, 150, 130));
        g2.fillOval(x + s / 2 - 2, y + s / 3, s / 16, s / 16);

        // Smile
        g2.setColor(new Color(80, 60, 100));
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(x + s / 2 - 8, y + s / 3 + 2, 16, 10, 0, -180);

        // Blush
        g2.setColor(new Color(255, 150, 170, 100));
        g2.fillOval(x + s * 5 / 16 - 4, y + s * 5 / 16, s / 10, s / 12);
        g2.fillOval(x + s * 9 / 16 + 2, y + s * 5 / 16, s / 10, s / 12);

        // ===== BODY =====
        g2.setColor(new Color(80, 160, 255));
        g2.fillRoundRect(x + s / 5, y + s / 2, 3 * s / 5, s / 3, 15, 15);

        // Scarf
        g2.setColor(new Color(255, 100, 100));
        g2.fillRoundRect(x + s / 5 + 2, y + s / 2 + 2, 3 * s / 5 - 4, s / 10, 5, 5);
        g2.fillOval(x + s / 2 - 6, y + s / 2 + 10, 12, 8);

        // Arms
        g2.setColor(new Color(255, 230, 200));
        g2.fillOval(x + s / 6, y + s / 2 + 8, s / 7, s / 9);
        g2.fillOval(x + s * 4 / 5, y + s / 2 + 8, s / 7, s / 9);

        // Legs
        g2.setColor(new Color(80, 100, 150));
        g2.fillOval(x + s / 3, y + s * 7 / 8, s / 8, s / 12);
        g2.fillOval(x + s / 2, y + s * 7 / 8, s / 8, s / 12);

        // Label "P2"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("P2", x + s / 2 - 4, y + s - 5);
    }

    @Override
    public int getScreenX() { return worldX; }
    @Override
    public int getScreenY() { return worldY; }
    @Override
    public int getDrawSize() { return gp.tileSize; }
}