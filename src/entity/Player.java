package entity;

import Game_2D.GamePanel;
import Game_2D.KeyHandler;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Human-controlled character.
 */
public class Player extends Entity implements Destructible {

    private static final int SOLID_AREA_X = 8;
    private static final int SOLID_AREA_Y = 13;
    private static final int SOLID_AREA_W = 48;
    private static final int SOLID_AREA_H = 40;
    private static final int DEFAULT_SPEED = 4;

    private final GamePanel gp;
    private final KeyHandler keyH;

    /** Screen position = world position (no scrolling camera). */
    public final int screenX = 0;
    public final int screenY = 0;

    private int speedBoostTimer = 0;
    private int originalSpeed = DEFAULT_SPEED;

    // ── Construction ─────────────────────────────────────────────────────────

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp   = gp;
        this.keyH = keyH;

        solidArea.x      = SOLID_AREA_X;
        solidArea.y      = SOLID_AREA_Y;
        solidArea.width  = SOLID_AREA_W;
        solidArea.height = SOLID_AREA_H;

        setDefaultValues();
        
        // Thử load ảnh (Giả sử player 1 là SSR, folder tên "Model 1")
        // Nếu không có ảnh, nó sẽ tự động fallback về vẽ hình My Melody.
        loadSprites("Model 1", CharacterTier.SSR);
    }

    private void setDefaultValues() {
        int ts = gp.tileM.getTileSize();
        worldX    = ts * 1;
        worldY    = ts * 1;
        speed     = DEFAULT_SPEED;
        originalSpeed = DEFAULT_SPEED;
        direction = Direction.DOWN;
    }

    // ── Public methods for item effects ─────────────────────────────────────

    public void applySpeedBoost(int boostAmount, int duration) {
        if (speedBoostTimer <= 0) {
            originalSpeed = speed;
            speed = originalSpeed + boostAmount;
        }
        speedBoostTimer = duration;
    }

    public void addLife() {
        if (life < 3) {
            life++;
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getSpeedBoostTimer() {
        return speedBoostTimer;
    }

    public int getCurrentSpeed() {
        return speed;
    }

    public int getOriginalSpeed() {
        return originalSpeed;
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    public void update() {
        if (!alive) return;

        // GIẢM SPEED BOOST TIMER
        if (speedBoostTimer > 0) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) {
                speed = originalSpeed;
            }
        }

        // Edge-triggered bomb placement
        if (keyH.bombKeyPressed) {
            keyH.consumeBombKey();
            gp.bombAlgo.placeBomb(getCol(), getRow(), this);
        }

        boolean anyKey = keyH.upPress || keyH.downPress || keyH.leftPress || keyH.rightPress;
        if (anyKey) {
            if (keyH.upPress)    direction = Direction.UP;
            else if (keyH.downPress)  direction = Direction.DOWN;
            else if (keyH.leftPress)  direction = Direction.LEFT;
            else                      direction = Direction.RIGHT;

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

    // ── Destructible ─────────────────────────────────────────────────────────

    @Override
    public void onDestroyedByFlame() {
        takeDamage();
    }

    @Override
    public int getCol() {
        return (worldX + solidArea.x + solidArea.width / 2) / gp.tileM.getTileSize();
    }

    @Override
    public int getRow() {
        return (worldY + solidArea.y + solidArea.height / 2) / gp.tileM.getTileSize();
    }

    @Override
    public boolean isDestroyed() {
        return !alive;
    }

    // ── Rendering — My Melody ────────────────────────────────────────────────

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;

        // Nếu đã có ảnh thì vẽ ảnh và return luôn
        if (usingSprites) {
            super.draw(g2);
            return;
        }

        // Blink while invincible
        if (invincible && (System.currentTimeMillis() / 120) % 2 == 0) return;

        int x = worldX;
        int y = worldY;
        int s = gp.tileM.getTileSize() - 4;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(x + 8, y + s / 2 + 8, s - 10, s / 4);

        // ===== MŨ TRÙM (hood) =====
        g2.setColor(new Color(255, 180, 200));
        g2.fillRoundRect(x + s / 5, y + s / 8, 3 * s / 5, s / 2, 20, 20);

        // Tai thỏ bên trái
        g2.setColor(new Color(255, 200, 215));
        g2.fillOval(x + s / 6, y - s / 8, s / 4, s / 3);
        g2.setColor(new Color(255, 160, 180));
        g2.fillOval(x + s / 6 + 3, y - s / 12, s / 7, s / 5);

        // Tai thỏ bên phải
        g2.fillOval(x + s * 5 / 9, y - s / 8, s / 4, s / 3);
        g2.fillOval(x + s * 5 / 9 + 3, y - s / 12, s / 7, s / 5);

        // Nơ tai thỏ
        g2.setColor(new Color(255, 100, 130));
        g2.fillOval(x + s / 5, y + s / 7, s / 6, s / 8);
        g2.fillOval(x + s / 5 + 3, y + s / 7 - 2, s / 8, s / 7);

        // ===== MẶT =====
        g2.setColor(new Color(255, 240, 245));
        g2.fillOval(x + s / 4, y + s / 5, s / 2, s / 2);

        // Mắt
        g2.setColor(new Color(30, 30, 40));
        g2.fillOval(x + s * 5 / 16, y + s / 4, s / 8, s / 7);
        g2.fillOval(x + s * 9 / 16, y + s / 4, s / 8, s / 7);

        // Điểm sáng
        g2.setColor(Color.WHITE);
        g2.fillOval(x + s * 5 / 16 + 2, y + s / 4 + 2, s / 20, s / 20);
        g2.fillOval(x + s * 9 / 16 + 2, y + s / 4 + 2, s / 20, s / 20);

        // Mũi
        g2.setColor(new Color(255, 180, 150));
        g2.fillOval(x + s / 2 - 2, y + s / 3, s / 16, s / 16);

        // Miệng
        g2.setColor(new Color(200, 100, 120));
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(x + s / 2 - 6, y + s / 3 + 4, 12, 8, 0, -180);

        // Má hồng
        g2.setColor(new Color(255, 150, 170, 100));
        g2.fillOval(x + s * 5 / 16 - 4, y + s * 5 / 16, s / 10, s / 12);
        g2.fillOval(x + s * 9 / 16 + 2, y + s * 5 / 16, s / 10, s / 12);

        // ===== THÂN =====
        g2.setColor(new Color(255, 140, 170));
        g2.fillRoundRect(x + s / 5, y + s / 2, 3 * s / 5, s / 3, 15, 15);

        // Đai/viền váy
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillRoundRect(x + s / 5 + 2, y + s / 2 + 2, 3 * s / 5 - 4, s / 10, 5, 5);

        // Nơ nhỏ ở thân
        g2.setColor(new Color(255, 100, 130));
        g2.fillOval(x + s / 2 - 4, y + s / 2 + 12, 8, 6);
        g2.fillOval(x + s / 2 - 8, y + s / 2 + 10, 6, 8);
        g2.fillOval(x + s / 2 + 2, y + s / 2 + 10, 6, 8);

        // Tay
        g2.setColor(new Color(255, 220, 200));
        g2.fillOval(x + s / 6, y + s / 2 + 8, s / 7, s / 9);
        g2.fillOval(x + s * 4 / 5, y + s / 2 + 8, s / 7, s / 9);

        // Chân
        g2.setColor(new Color(255, 200, 180));
        g2.fillOval(x + s / 3, y + s * 7 / 8, s / 8, s / 12);
        g2.fillOval(x + s / 2, y + s * 7 / 8, s / 8, s / 12);

        // Râu thỏ
        g2.setColor(new Color(180, 180, 200, 150));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x + s / 4, y + s / 3, x + s / 6, y + s / 3 + 4);
        g2.drawLine(x + s / 4, y + s / 3 + 6, x + s / 6, y + s / 3 + 10);
        g2.drawLine(x + s * 3 / 4, y + s / 3, x + s * 5 / 6, y + s / 3 + 4);
        g2.drawLine(x + s * 3 / 4, y + s / 3 + 6, x + s * 5 / 6, y + s / 3 + 10);

        // Label "M"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("M", x + s / 2 - 3, y + s - 5);
    }

    @Override
    public int getScreenX() {
        return worldX;
    }

    @Override
    public int getScreenY() {
        return worldY;
    }

    @Override
    public int getDrawSize() {
        return gp.tileM.getTileSize();
    }
}