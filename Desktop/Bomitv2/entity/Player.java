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
 *
 * CHANGES from original:
 *   • Removed sprite-sheet loading (no image assets in simplified build).
 *     draw() now uses Graphics2D shapes (satisfies "draw basic graphics"
 *     requirement) — swap in real sprites later by overriding draw() only.
 *   • onDestroyedByFlame() was setting dead=true immediately (instant
 *     kill regardless of remaining lives).  Fixed: now calls takeDamage()
 *     which respects the lives counter and invincibility window.
 *   • Bomb placement already delegated to gp.bombAlgo.placeBomb(owner) —
 *     no per-bomb logic needed here since BombAlgorithm handles the queue.
 *   • screenX / screenY are now fixed to (0,0) because the map fits the
 *     window exactly — no scrolling camera needed.
 */
public class Player extends Entity implements Destructible {

    private static final int SOLID_AREA_X = 8;
    private static final int SOLID_AREA_Y = 8;
    private static final int SOLID_AREA_W = 48;
    private static final int SOLID_AREA_H = 48;
    private static final int DEFAULT_SPEED = 4;

    private final GamePanel gp;
    private final KeyHandler keyH;

    /** Screen position = world position (no scrolling camera). */
    public final int screenX = 0;
    public final int screenY = 0;

    // ── Construction ─────────────────────────────────────────────────────────

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp   = gp;
        this.keyH = keyH;

        solidArea.x      = SOLID_AREA_X;
        solidArea.y      = SOLID_AREA_Y;
        solidArea.width  = SOLID_AREA_W;
        solidArea.height = SOLID_AREA_H;

        setDefaultValues();
    }

    private void setDefaultValues() {
        worldX    = gp.tileSize * 1;   // spawn col 1
        worldY    = gp.tileSize * 1;   // spawn row 1
        speed     = DEFAULT_SPEED;
        direction = Direction.DOWN;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void update() {
        if (!alive) return;

        // Edge-triggered bomb placement (one bomb per key press)
        if (keyH.bombKeyPressed) {
            keyH.consumeBombKey();
            gp.bombAlgo.placeBomb(getCol(), getRow(), this);
        }

        boolean anyKey = keyH.upPress || keyH.downPress || keyH.leftPress || keyH.rightPress;
        if (anyKey) {
            if      (keyH.upPress)    direction = Direction.UP;
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

    /**
     * FIX from original: was `dead = true` (instant kill).
     * Now delegates to takeDamage() which honours remaining lives and
     * grants a brief invincibility window.
     */
    @Override
    public void onDestroyedByFlame() {
        takeDamage();
    }

    @Override
    public int     getCol()       { return (worldX + solidArea.x + solidArea.width  / 2) / gp.tileSize; }
    @Override
    public int     getRow()       { return (worldY + solidArea.y + solidArea.height / 2) / gp.tileSize; }
    @Override
    public boolean isDestroyed()  { return !alive; }

    // ── Rendering — Graphics2D shapes (no sprites) ────────────────────────────

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;

        // Blink while invincible
        if (invincible && (System.currentTimeMillis() / 120) % 2 == 0) return;

        int x = worldX + 2;
        int y = worldY + 2;
        int s = gp.tileSize - 4;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(x + 5, y + s / 2 + 6, s - 6, s / 3);

        // Body
        g2.setColor(new Color(30, 105, 225));
        g2.fillRoundRect(x + s / 6, y + s / 3, 2 * s / 3, 2 * s / 3, 10, 10);

        // Head
        g2.setColor(new Color(255, 213, 168));
        g2.fillOval(x + s / 5, y, 3 * s / 5, 3 * s / 5);

        // Hair
        g2.setColor(new Color(55, 32, 10));
        g2.fillArc(x + s / 5, y, 3 * s / 5, 3 * s / 5, 0, 180);

        // Eyes
        g2.setColor(Color.WHITE);
        g2.fillOval(x + s * 5 / 16, y + s / 7, s / 7, s / 6);
        g2.fillOval(x + s * 9 / 16, y + s / 7, s / 7, s / 6);
        g2.setColor(new Color(30, 30, 150));
        g2.fillOval(x + s * 5 / 16 + 1, y + s / 7 + 1, s / 12, s / 12);
        g2.fillOval(x + s * 9 / 16 + 1, y + s / 7 + 1, s / 12, s / 12);

        // "P" label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("P", x + s / 2 - 3, y + s - 2);
    }

    @Override public int getScreenX()  { return worldX;        }
    @Override public int getScreenY()  { return worldY;        }
    @Override public int getDrawSize() { return gp.tileSize;   }
}
