package entity;

import Game_2D.Renderable;
import Game_2D.Updatable;
import bomb.Bomb;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Abstract base for every character (Player, Bot).
 *
 * CHANGES from original:
 *   • Added per-entity Queue<Bomb> bombQueue (FIFO) and int maxBombs.
 *     Each entity owns its own queue.  BombAlgorithm.placeBomb(col, row, owner)
 *     enqueues the same Bomb into BOTH this queue and the global timing
 *     queue, preserving FIFO order in both.
 *   • getBombQueue() / getMaxBombs() accessors added.
 *   • updateCommonLogic() now calls bombQueue.removeIf(Bomb::isDestroyed)
 *     so the per-entity cap is reclaimed automatically after detonation.
 *   • takeDamage() fixed: no longer sets alive=false on the first hit;
 *     invincibility window added so rapid multi-hits are ignored.
 *   • Sprite / animation helpers kept exactly as the original.
 */

public abstract class Entity implements Updatable, Renderable {

    public int worldX, worldY;
    public int speed;

    public Direction direction = Direction.DOWN;

    public int     maxLife = 3;
    public int     life    = 3;
    public boolean alive   = true;
    public boolean dying   = false;

    // ── Invincibility ─────────────────────────────────────────────────────────
    public boolean invincible        = false;
    public int     invincibleCounter = 0;
    private static final int INVINCIBLE_TICKS = 90; // ~1.5 s at 60 fps

    // ── Sprite & Animation ────────────────────────────────────────────────────
    protected final Map<Direction, BufferedImage[]> sprites = new EnumMap<>(Direction.class);
    public int spriteCounter = 0;
    public int spriteNum     = 1;

    // ── Collision ─────────────────────────────────────────────────────────────
    public Rectangle solidArea           = new Rectangle(0, 0, 48, 48);
    public int       solidAreaDefaultX, solidAreaDefaultY;
    public boolean   collisionOn         = false;

    // ── Per-entity bomb queue (NEW) ───────────────────────────────────────────
    /**
     * Personal FIFO bomb queue.
     *
     * Bombs are offered to the tail by BombAlgorithm.placeBomb(owner)
     * and removed from the head when they detonate.  The queue size is
     * checked against maxBombs before allowing a new placement so each
     * character cannot spam more than their allotted simultaneous bombs.
     */
    private final Queue<Bomb> bombQueue = new LinkedList<>();

    /** Maximum live bombs this entity may have on the board at once. */
    protected int maxBombs = 3;

    // ── Damage ────────────────────────────────────────────────────────────────

    @Override
    public void takeDamage() {
        if (!invincible) {
            life--;
            if (life <= 0) {
                life  = 0;
                alive = false;
            } else {
                invincible        = true;
                invincibleCounter = 0;
            }
        }
    }

    // ── Common per-frame logic (call from subclass update()) ──────────────────

    protected void updateCommonLogic() {
        // Tick invincibility window down
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter >= INVINCIBLE_TICKS) {
                invincible        = false;
                invincibleCounter = 0;
            }
        }
        // Reclaim capacity for any bombs that have already detonated
        bombQueue.removeIf(Bomb::isDestroyed);
    }

    // ── Bomb queue accessors ──────────────────────────────────────────────────

    /** Returns this entity's personal bomb queue (FIFO). */
    public Queue<Bomb> getBombQueue() { return bombQueue; }

    /** Hard cap on simultaneous live bombs. */
    public int getMaxBombs() { return maxBombs; }

    // ── Hitbox ────────────────────────────────────────────────────────────────

    public Rectangle getWorldHitbox() {
        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y,
                             solidArea.width, solidArea.height);
    }

    // ── Sprite helpers (unchanged) ────────────────────────────────────────────

    protected void setSprites(Direction dir, BufferedImage f1, BufferedImage f2) {
        sprites.put(dir, new BufferedImage[]{ f1, f2 });
    }

    protected BufferedImage currentFrame() {
        BufferedImage[] frames = sprites.get(direction);
        if (frames == null || frames.length == 0) return null;
        return frames[Math.max(0, Math.min(frames.length - 1, spriteNum - 1))];
    }

    protected void advanceWalkAnimation() {
        spriteCounter++;
        if (spriteCounter > 10) {
            spriteNum     = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }
    }

    // ── Default draw (blink while invincible) ─────────────────────────────────

    @Override
    public void draw(Graphics2D g2) {
        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, 0.35f));
        }
        BufferedImage frame = currentFrame();
        if (frame != null) {
            g2.drawImage(frame, getScreenX(), getScreenY(), getDrawSize(), getDrawSize(), null);
        }
        g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 1f));
    }

    public abstract int getScreenX();
    public abstract int getScreenY();
    public abstract int getDrawSize();
    public abstract int getCol();
    public abstract int getRow();
}
