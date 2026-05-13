package bomb;

import Game_2D.GamePanel;
import entity.Destructible;
import entity.Direction;
import entity.Entity;
import tile.TileManager;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * One placed bomb.
 *
 * CHANGES from original:
 *   • Added `Entity owner` field + getOwner() so BombAlgorithm can
 *     remove this bomb from the owner's personal Queue<Bomb> on
 *     detonation — fulfilling the per-entity FIFO queue requirement.
 *   • DEFAULT_COUNTDOWN_TICKS changed to 120 (2 s at 60 fps) per spec.
 *   • Everything else (countdown, spread, chain reactions) is identical.
 */
public class Bomb extends Entity implements Destructible {

    public static final int DEFAULT_COUNTDOWN_TICKS = 120; // 2 s @ 60 fps
    public static final int DEFAULT_EXPLOSION_SCALE = 2;

    private final GamePanel      gp;
    private final BombAlgorithm  algorithm;
    private final BombAppearance appearance;
    private final Entity         owner;       // ← NEW: who placed this bomb

    private final int col;
    private final int row;
    private final int explosionScale;

    private int     countdown;
    private boolean exploded  = false;
    private boolean destroyed = false;

    // ── Constructor (owner-aware) ─────────────────────────────────────────────

    public Bomb(GamePanel gp, BombAlgorithm algorithm, int col, int row,
                int countdownTicks, int explosionScale,
                BombAppearance appearance, Entity owner) {
        this.gp             = gp;
        this.algorithm      = algorithm;
        this.col            = col;
        this.row            = row;
        this.countdown      = countdownTicks;
        this.explosionScale = Math.max(1, explosionScale);
        this.appearance     = appearance;
        this.owner          = owner;

        this.worldX    = col * gp.tileSize;
        this.worldY    = row * gp.tileSize;
        this.direction = Direction.DOWN;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void update() {
        if (exploded) return;
        if (countdown > 0) countdown--;
    }

    public boolean isReadyToExplode() {
        return !exploded && countdown <= 0;
    }

    // ── Explosion ─────────────────────────────────────────────────────────────

    /**
     * Produces Flames in 4 directions.
     * Arms stop at hard walls; bricks are consumed and stop the arm.
     */
    public List<Flame> explode(FlameAppearance flameAppearance, int flameDurationTicks) {
        if (exploded) return List.of();
        exploded  = true;
        destroyed = true;

        TileManager tm     = gp.tileM;
        List<Flame> flames = new ArrayList<>();

        flames.add(new Flame(gp, col, row, FlameDirection.CENTER,
                             flameDurationTicks, flameAppearance, algorithm));

        spread(tm, flames, FlameDirection.UP,     0, -1, flameAppearance, flameDurationTicks);
        spread(tm, flames, FlameDirection.DOWN,   0,  1, flameAppearance, flameDurationTicks);
        spread(tm, flames, FlameDirection.LEFT,  -1,  0, flameAppearance, flameDurationTicks);
        spread(tm, flames, FlameDirection.RIGHT,  1,  0, flameAppearance, flameDurationTicks);

        return flames;
    }

    private void spread(TileManager tm, List<Flame> out, FlameDirection dir,
                        int dx, int dy, FlameAppearance ap, int dur) {
        for (int step = 1; step <= explosionScale; step++) {
            int c = col + dx * step;
            int r = row + dy * step;

            if (tm.isBrick(c, r)) {
                out.add(new Flame(gp, c, r, dir, dur, ap, algorithm));
                tm.destroyBrick(c, r);
                return;
            }
            if (tm.isSolid(c, r)) return;
            out.add(new Flame(gp, c, r, dir, dur, ap, algorithm));
        }
    }

    // ── Destructible (chain reactions) ────────────────────────────────────────

    @Override
    public void onDestroyedByFlame() {
        if (exploded || destroyed) return;
        algorithm.queueChainReaction(this);
    }

    public void disappear() { destroyed = true; }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @Override public boolean isDestroyed() { return destroyed;      }
    @Override public int     getCol()      { return col;            }
    @Override public int     getRow()      { return row;            }

    public int    getCountdown()       { return countdown;       }
    public int    getExplosionScale()  { return explosionScale;  }
    public Entity getOwner()           { return owner;           } // ← NEW

    @Override public int getScreenX()  { return worldX;          }
    @Override public int getScreenY()  { return worldY;          }
    @Override public int getDrawSize() { return gp.tileSize;     }

    @Override
    public void draw(Graphics2D g2) {
        if (exploded) return;
        appearance.draw(g2, getScreenX(), getScreenY(), gp.tileSize, countdown);
    }
}
