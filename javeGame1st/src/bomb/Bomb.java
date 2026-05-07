package bomb;

import Game_2D.gamePanel;
import entity.Destructible;
import entity.Direction;
import entity.entity;
import tile.tileManager;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * One placed bomb.
 *
 * Responsibilities:
 *  - Tick its own countdown.
 *  - When the countdown hits zero, produce the list of Flame tiles that
 *    represent the explosion (4-direction spread, blocked by walls).
 *  - Disappear once it has exploded so the BombAlgorithm can drop it.
 *
 * SOLID notes:
 *  - SRP: Drawing is delegated to BombAppearance. Spread math lives
 *    here; collision handling against entities lives in Flame.
 *  - DIP: Bomb knows about TileManager.isSolid (an interface the manager
 *    publishes), not about how the map is stored.
 *  - Implements Destructible so a flame from one bomb can detonate this
 *    one early — chain reactions are routed through the BombAlgorithm's
 *    Stack (LIFO), see BombAlgorithm.queueChainReaction().
 */
public class Bomb extends entity implements Destructible {

    public static final int DEFAULT_COUNTDOWN_TICKS = 180; // ~3s @ 60fps
    public static final int DEFAULT_EXPLOSION_SCALE = 2;

    private final gamePanel gp;
    private final BombAlgorithm algorithm;
    private final BombAppearance appearance;

    private final int col;
    private final int row;
    private final int explosionScale;

    private int countdown;          // ticks remaining
    private boolean exploded = false;
    private boolean destroyed = false;

    public Bomb(gamePanel gp, BombAlgorithm algorithm, int col, int row,
                int countdownTicks, int explosionScale, BombAppearance appearance) {
        this.gp = gp;
        this.algorithm = algorithm;
        this.col = col;
        this.row = row;
        this.countdown = countdownTicks;
        this.explosionScale = Math.max(1, explosionScale);
        this.appearance = appearance;

        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.direction = "down"; // unused, but Entity requires one
    }

    @Override
    public void update() {
        if (exploded) return;
        if (countdown > 0) countdown--;
    }

    public boolean isReadyToExplode() {
        return !exploded && countdown <= 0;
    }

    /**
     * Produces the flames from this bomb. Called once by BombAlgorithm
     * when the bomb is ready to detonate (either via timer or via chain
     * reaction). Spread stops at walls; bricks consume one flame tile and
     * also stop the spread (classic Bomberman behaviour).
     */
    public List<Flame> explode(FlameAppearance flameAppearance, int flameDurationTicks) {
        if (exploded) return List.of();
        exploded = true;
        destroyed = true;

        tileManager tm = gp.tileM;
        List<Flame> flames = new ArrayList<>();

        // Center
        flames.add(new Flame(gp, col, row, FlameDirection.CENTER, flameDurationTicks, flameAppearance, algorithm));

        // Four arms — each spreads up to `explosionScale` tiles, stopping at walls.
        spread(tm, flames, FlameDirection.UP,    0, -1, flameAppearance, flameDurationTicks);
        spread(tm, flames, FlameDirection.DOWN,  0,  1, flameAppearance, flameDurationTicks);
        spread(tm, flames, FlameDirection.LEFT, -1,  0, flameAppearance, flameDurationTicks);
        spread(tm, flames, FlameDirection.RIGHT, 1,  0, flameAppearance, flameDurationTicks);

        return flames;
    }

    /**
     * Walks from (col,row) in (dx,dy) for up to explosionScale steps:
     *  - empty tile         -> spawn flame, continue
     *  - destructible brick -> spawn flame, destroy brick, STOP
     *  - solid wall         -> STOP (no flame on the wall tile)
     */
    private void spread(tileManager tm, List<Flame> out, FlameDirection dir,
                        int dx, int dy,
                        FlameAppearance appearance, int durationTicks) {
        for (int step = 1; step <= explosionScale; step++) {
            int c = col + dx * step;
            int r = row + dy * step;

            // 1. Kiểm tra vật cản cố định (Wall)
            if (tm.isSolid(c, r)) {
                return; // Dừng ngay lập tức, không vẽ lửa lên tường cứng
            }

            // 2. KIỂM TRA INTERACTIVE TILE (Tường vỡ của bạn)
            // Chúng ta duyệt mảng iTile để xem ô (c, r) có tường không
            for (int i = 0; i < gp.iTile.length; i++) {
                if (gp.iTile[i] != null &&
                        gp.iTile[i].worldX / gp.tileSize == c &&
                        gp.iTile[i].worldY / gp.tileSize == r) {

                    // Nếu chạm tường vỡ: Tạo lửa tại đó và DỪNG spread (giống Boom Online)
                    out.add(new Flame(gp, c, r, dir, durationTicks, appearance, algorithm));
                    return;
                }
            }

            // 3. Nếu là ô trống (Cỏ)
            out.add(new Flame(gp, c, r, dir, durationTicks, appearance, algorithm));
        }
    }

    /** Called by Flame when contact is made (chain reaction). */
    @Override
    public void onDestroyedByFlame() {
        if (exploded || destroyed) return;
        // Don't explode here — defer to the algorithm's chain-reaction stack
        // so the order of cascading explosions is well-defined and we don't
        // recurse arbitrarily deep on the call stack.
        algorithm.queueChainReaction(this);
    }

    /** Final cleanup hook required by the spec (mirrors Flame.disappear). */
    public void disappear() {
        destroyed = true;
    }

    @Override public boolean isDestroyed() { return destroyed; }
    @Override public int getCol() { return col; }
    @Override public int getRow() { return row; }

    public int getCountdown()          { return countdown; }
    public int getExplosionScale()     { return explosionScale; }

    public int getScreenX()  { return worldX - (gp.player.worldX - gp.player.screenX); }
    public int getScreenY()  { return worldY - (gp.player.worldY - gp.player.screenY); }
    public int getDrawSize() { return gp.tileSize; }

    @Override
    public void draw(Graphics2D g2) {
        if (exploded) return;
        appearance.draw(g2, worldX, worldY, gp.tileSize, countdown);
    }
}
