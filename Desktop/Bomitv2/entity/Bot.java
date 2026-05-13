package entity;

import Game_2D.GamePanel;
import bomb.Bomb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;

/**
 * AI-controlled opponent.
 *
 * CHANGES / FIXES from original:
 *   • BUG FIX: was using gp.bombM (a separate, disconnected instance).
 *     Now uses gp.bombAlgo for everything — same queue the player uses.
 *   • BUG FIX: onDestroyedByFlame() called life-- directly, bypassing
 *     the invincibility window.  Fixed: now calls takeDamage().
 *   • ADDED: HUNT / EVADE state machine.
 *       HUNT  – BFS toward the player, avoiding danger zones.
 *               When within striking range, place a bomb then EVADE.
 *       EVADE – BFS to the nearest safe cell using
 *               Pathfinder.nearestSafeCell(), computed with a wall-aware
 *               danger set (Pathfinder.buildDangerSet()).
 *   • ADDED: bomb placement via gp.bombAlgo.placeBomb(col, row, this),
 *     which correctly enqueues into both the global and personal queues.
 *   • draw() rewritten with Graphics2D shapes (no sprite assets).
 *   • computeDangerSet() replaced by Pathfinder.buildDangerSet() which
 *     properly respects hard-wall occlusion along each blast arm.
 */
public class Bot extends Entity implements Destructible {

    // ── State machine ────────────────────────────────────────────────────────

    private enum BotState { HUNT, EVADE }

    private BotState aiState = BotState.HUNT;

    // ── Pathfinding / timing ─────────────────────────────────────────────────

    private static final int DEFAULT_SPEED    = 2;
    private static final int BOMB_COOLDOWN    = 80;  // ticks between placements
    private static final int REPLAN_INTERVAL  = 20;  // ticks between BFS recalcs

    private int bombCooldownTimer  = 0;
    private int replanTimer        = 0;

    /** Next grid cell to step toward. [-1,-1] = no target. */
    private int targetCol = -1, targetRow = -1;

    private final GamePanel gp;

    // ── Construction ─────────────────────────────────────────────────────────

    public Bot(GamePanel gp, int startCol, int startRow) {
        this.gp   = gp;
        worldX    = startCol * gp.tileSize;
        worldY    = startRow * gp.tileSize;
        speed     = DEFAULT_SPEED;
        life      = 3;
        direction = Direction.DOWN;
        solidArea.setBounds(8, 8, gp.tileSize - 16, gp.tileSize - 16);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void update() {
        if (!alive) return;

        updateCommonLogic();
        bombCooldownTimer = Math.max(0, bombCooldownTimer - 1);
        replanTimer       = Math.max(0, replanTimer - 1);

        int curCol = getCol();
        int curRow = getRow();

        // ── State decision ───────────────────────────────────────────────────
        // Build the danger set once per tick (cheap on a 16×12 grid).
        Set<String> danger = Pathfinder.buildDangerSet(gp);

        boolean inDanger = danger.contains(Pathfinder.key(curCol, curRow));
        aiState = inDanger ? BotState.EVADE : BotState.HUNT;

        // ── Plan (rate-limited, or immediately on state change) ──────────────
        if (replanTimer == 0) {
            if (aiState == BotState.EVADE) {
                planEvade(curCol, curRow, danger);
            } else {
                planHunt(curCol, curRow, danger);
            }
            replanTimer = REPLAN_INTERVAL;
        }

        // ── Move one step toward target cell ─────────────────────────────────
        stepTowardTarget();
    }

    // ── EVADE: move to the nearest safe cell ─────────────────────────────────

    private void planEvade(int curCol, int curRow, Set<String> danger) {
        int[] safe = Pathfinder.nearestSafeCell(gp, curCol, curRow, danger);
        if (safe == null) return;  // completely surrounded — stay put

        List<int[]> path = Pathfinder.shortestPath(
                gp, curCol, curRow, safe[0], safe[1], null); // allow any path when fleeing
        if (path != null && path.size() > 1) {
            targetCol = path.get(1)[0];
            targetRow = path.get(1)[1];
        }
    }

    // ── HUNT: chase the player; place a bomb when close ───────────────────────

    private void planHunt(int curCol, int curRow, Set<String> danger) {
        int playerCol = gp.player.getCol();
        int playerRow = gp.player.getRow();

        int dist = Math.abs(playerCol - curCol) + Math.abs(playerRow - curRow);

        if (dist <= 2 && bombCooldownTimer == 0) {
            // Within striking range — drop a bomb then immediately evade
            gp.bombAlgo.placeBomb(curCol, curRow, this);
            bombCooldownTimer = BOMB_COOLDOWN;

            // Rebuild danger set with the freshly placed bomb included
            Set<String> newDanger = Pathfinder.buildDangerSet(gp);
            planEvade(curCol, curRow, newDanger);
        } else {
            // Chase player, avoiding known danger zones
            List<int[]> path = Pathfinder.shortestPath(
                    gp, curCol, curRow, playerCol, playerRow, danger);
            if (path != null && path.size() > 1) {
                targetCol = path.get(1)[0];
                targetRow = path.get(1)[1];
            }
        }
    }

    // ── Pixel movement toward the planned grid cell ───────────────────────────

    /**
     * Moves the bot's pixel position toward (targetCol, targetRow) at
     * `speed` pixels per tick, using CollisionChecker so it respects walls
     * exactly like the Player does.
     */
    private void stepTowardTarget() {
        if (targetCol < 0) return;

        int tx = targetCol * gp.tileSize;
        int ty = targetRow * gp.tileSize;
        int dx = Integer.compare(tx, worldX);
        int dy = Integer.compare(ty, worldY);

        if (dx != 0) {
            direction    = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            collisionOn  = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) worldX += direction.dx * speed;
        } else if (dy != 0) {
            direction    = (dy > 0) ? Direction.DOWN : Direction.UP;
            collisionOn  = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) worldY += direction.dy * speed;
        }

        // Snap to target when close enough to avoid overshooting
        if (Math.abs(worldX - tx) <= speed && Math.abs(worldY - ty) <= speed) {
            worldX    = tx;
            worldY    = ty;
            targetCol = -1;
            targetRow = -1;
        }

        advanceWalkAnimation();
    }

    // ── Destructible ─────────────────────────────────────────────────────────

    /**
     * FIX from original: was `this.life--` (no invincibility window).
     * Now delegates to takeDamage() which guards against rapid multi-hit.
     */
    @Override
    public void onDestroyedByFlame() {
        takeDamage();
    }

    @Override
    public int     getCol()      { return (worldX + solidArea.x + solidArea.width  / 2) / gp.tileSize; }
    @Override
    public int     getRow()      { return (worldY + solidArea.y + solidArea.height / 2) / gp.tileSize; }
    @Override
    public boolean isDestroyed() { return !alive; }

    // ── Rendering — Graphics2D shapes ────────────────────────────────────────

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

        // Body (red armour)
        g2.setColor(new Color(190, 35, 35));
        g2.fillRoundRect(x + s / 6, y + s / 3, 2 * s / 3, 2 * s / 3, 10, 10);
        g2.setColor(new Color(100, 15, 15));
        g2.drawRoundRect(x + s / 6, y + s / 3, 2 * s / 3, 2 * s / 3, 10, 10);

        // Helmet
        g2.setColor(new Color(175, 180, 192));
        g2.fillOval(x + s / 7, y, 5 * s / 7, 3 * s / 5);

        // Visor
        g2.setColor(new Color(70, 185, 255, 185));
        g2.fillArc(x + s / 4, y + s / 8, s / 2, s / 3, 0, -180);

        // "B" label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("B", x + s / 2 - 3, y + s - 2);

        // State indicator dot: green = HUNT, yellow = EVADE
        g2.setColor(aiState == BotState.EVADE
                    ? new Color(255, 215, 0) : new Color(55, 210, 55));
        g2.fillOval(x + s - 9, y + 1, 8, 8);
    }

    @Override public int getScreenX()  { return worldX;       }
    @Override public int getScreenY()  { return worldY;       }
    @Override public int getDrawSize() { return gp.tileSize;  }
}
