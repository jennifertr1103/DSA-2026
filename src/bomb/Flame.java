package bomb;

import Game_2D.GamePanel;
import entity.Destructible;
import entity.Direction;
import entity.Entity;

import java.awt.Graphics2D;
import java.util.List;

/**
 * One flame tile produced by an exploding Bomb.
 *
 * Responsibilities:
 *  - Live for `durationTicks` ticks, then disappear.
 *  - Each tick, run destroyTarget() against the registered Destructible
 *    list and, if a target sits on this tile, call its
 *    onDestroyedByFlame() (which may push a chain reaction onto the
 *    algorithm's stack).
 *
 * SOLID notes:
 *  - SRP: collision is here, drawing is in FlameAppearance.
 *  - DIP: works against the Destructible interface, not concrete entity
 *    types — adding monsters, items, crates is zero-change for Flame.
 */
public class Flame extends Entity {

    public static final int DEFAULT_DURATION_TICKS = 30; // ~0.5s @ 60fps

    private final GamePanel gp;
    private final FlameDirection flameDirection;
    private final FlameAppearance appearance;
    private final BombAlgorithm algorithm;

    private final int col;
    private final int row;
    private final int totalDuration;
    private int duration;       // remaining ticks
    private boolean expired = false;

    public Flame(GamePanel gp, int col, int row, FlameDirection dir,
                 int durationTicks, FlameAppearance appearance, BombAlgorithm algorithm) {
        this.gp = gp;
        this.col = col;
        this.row = row;
        this.flameDirection = dir;
        this.totalDuration = durationTicks;
        this.duration = durationTicks;
        this.appearance = appearance;
        this.algorithm = algorithm;

        int ts = gp.tileM.getTileSize();
        this.worldX = col * ts;
        this.worldY = row * ts;
        this.direction = Direction.DOWN; // Entity requires one
    }

    /** Required by Entity/Updatable. Ticks the duration; calls disappear() when done. */
    @Override
    public void update() {
        if (expired) return;
        if (duration > 0) duration--;
        if (duration <= 0) disappear();
    }

    /**
     * Iterates the destructible list and destroys any whose tile-grid
     * position matches this flame's position. Called by BombAlgorithm
     * once per tick while the flame is alive.
     *
     * Bricks are NOT processed here — they are destroyed at the moment
     * of explosion in Bomb.spread() so that the spread itself stops at
     * the brick and the destruction is deterministic.
     */
    public void destroyTarget(java.util.List<entity.Destructible> targets) {
        if (expired) return;
        for (entity.Destructible d : targets) {
            if (d.isDestroyed()) continue;
            if (d.getCol() == col && d.getRow() == row) {
                d.onDestroyedByFlame();
            }
        }
    }

    public void disappear() {
        expired = true;
    }

    public boolean isExpired() { return expired; }
    public int getCol()        { return col; }
    public int getRow()        { return row; }
    public FlameDirection getFlameDirection() { return flameDirection; }

    @Override public int getScreenX()  { return worldX; }
    @Override public int getScreenY()  { return worldY; }
    @Override public int getDrawSize() { return gp.tileM.getTileSize(); }

    @Override
    public void draw(Graphics2D g2) {
        if (expired) return;
        appearance.draw(g2, getScreenX(), getScreenY(), gp.tileM.getTileSize(),
                flameDirection, duration, totalDuration);
    }
}
