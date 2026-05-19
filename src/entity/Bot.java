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

    private enum BotState { HUNT, EVADE, CLEAR, COLLECT }

    private BotState aiState = BotState.HUNT;

    // ── Pathfinding / timing ─────────────────────────────────────────────────

    private static final int DEFAULT_SPEED = 2;
    private static final int BOMB_COOLDOWN = 80;  // ticks between placements
    private static final int REPLAN_INTERVAL = 10; // Reduced for better responsiveness

    private int startDelayTimer = 0;
    private boolean isActive = true;
    private int bombCooldownTimer = 0;
    private int replanTimer = 0;
    private int lastActiveBombCount = 0;
    private int lastActiveItemCount = 0;

    private int targetCol = -1, targetRow = -1;

    private final GamePanel gp;
    private int botId;

    public Bot(GamePanel gp, int startCol, int startRow, int botId) {
        this.gp   = gp;
        this.botId = botId;
        int ts = gp.tileM.getTileSize();
        worldX    = startCol * ts;
        worldY    = startRow * ts;
        speed     = DEFAULT_SPEED;
        life      = 3;
        direction = Direction.DOWN;
        
        // Dynamic collision size
        setCollisionSize((int)(ts * 0.75), (int)(ts * 0.65));

        // Randomly select a model from Model 1 to Model 5
        java.util.Random rand = new java.util.Random();
        int modelNum = rand.nextInt(5) + 1; // 1 to 5
        String modelName = "Model " + modelNum;
        loadSprites(modelName, CharacterTier.SSR);
    }

    // ── Public methods for item effects ─────────────────────────────────────

    public void applySpeedBoost(int boostAmount, int duration) {
        this.speed = DEFAULT_SPEED + boostAmount;
        speedBoostTimer = duration;
    }

    private int speedBoostTimer = 0;

    public void addLife() {
        if (life < 3) life++;
    }

    public void setStartDelay(int seconds) {
        if (seconds > 0) {
            this.startDelayTimer = seconds * 60;  // 60 FPS
            this.isActive = false;
        } else {
            this.startDelayTimer = 0;
            this.isActive = true;
        }
    }

    @Override
    public void update() {
        if (!alive) return;
        if (!isActive) {
            if (startDelayTimer > 0) {
                startDelayTimer--;
                if (startDelayTimer <= 0) {
                    isActive = true;
                }
            }
            return;
        }

        updateCommonLogic();
        bombCooldownTimer = Math.max(0, bombCooldownTimer - 1);
        replanTimer       = Math.max(0, replanTimer - 1);

        if (speedBoostTimer > 0) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) speed = DEFAULT_SPEED;
        }

        // Trigger immediate replan if bomb count changed or item count changed
        int currentBombCount = gp.bombAlgo.getActiveBombs().size();
        int currentItemCount = (gp.itemSpawner != null) ? gp.itemSpawner.getActiveItems().size() : 0;

        if (currentBombCount != lastActiveBombCount || currentItemCount != lastActiveItemCount) {
            replanTimer = 0;
            lastActiveBombCount = currentBombCount;
            lastActiveItemCount = currentItemCount;
        }

        // ── Plan (rate-limited) ────────────────────────────────────────────────
        if (replanTimer == 0) {
            int curCol = getCol();
            int curRow = getRow();
            java.util.Set<String> danger = Pathfinder.buildDangerSet(gp);
            boolean inDanger = danger.contains(Pathfinder.key(curCol, curRow));

            if (inDanger) {
                aiState = BotState.EVADE;
                planEvade(curCol, curRow, danger);
            } else {
                if (planHunt(curCol, curRow, danger)) {
                } else if (planClear(curCol, curRow, danger)) {
                } else if (planCollect(curCol, curRow, danger)) {
                } else {
                    aiState = BotState.HUNT;
                    targetCol = -1;
                    targetRow = -1;
                }
            }
            replanTimer = REPLAN_INTERVAL;
        }

        stepTowardTarget();
    }

    private boolean isNarrowPassage(int col, int row) {
        int walkableCount = 0;
        if (!gp.tileM.isSolid(col, row - 1)) walkableCount++;
        if (!gp.tileM.isSolid(col, row + 1)) walkableCount++;
        if (!gp.tileM.isSolid(col - 1, row)) walkableCount++;
        if (!gp.tileM.isSolid(col + 1, row)) walkableCount++;
        return walkableCount <= 2;
    }

    private boolean planCollect(int curCol, int curRow, java.util.Set<String> danger) {
        int[] itemCell = Pathfinder.nearestItem(gp, curCol, curRow);
        if (itemCell == null) return false;

        java.util.List<int[]> path = Pathfinder.shortestPath(gp, curCol, curRow, itemCell[0], itemCell[1], danger);      
        if (path != null && path.size() > 1) {
            aiState = BotState.COLLECT;
            targetCol = path.get(1)[0];
            targetRow = path.get(1)[1];
            return true;
        }
        return false;
    }

    private void planEvade(int curCol, int curRow, java.util.Set<String> danger) {
        int[] safe = Pathfinder.nearestSafeCell(gp, curCol, curRow, danger);
        if (safe == null) return;

        java.util.List<int[]> path = Pathfinder.shortestPath(gp, curCol, curRow, safe[0], safe[1], null);
        if (path != null && path.size() > 1) {
            targetCol = path.get(1)[0];
            targetRow = path.get(1)[1];
        }
    }

    private boolean planClear(int curCol, int curRow, java.util.Set<String> danger) {
        int[] brickCell = Pathfinder.nearestBrick(gp, curCol, curRow);
        if (brickCell == null) return false;

        aiState = BotState.CLEAR;
        if (curCol == brickCell[0] && curRow == brickCell[1]) {
            if (bombCooldownTimer == 0) {
                gp.bombAlgo.placeBomb(curCol, curRow, this);
                bombCooldownTimer = BOMB_COOLDOWN;
                aiState = BotState.EVADE;
                planEvade(curCol, curRow, Pathfinder.buildDangerSet(gp));
            }
            return true;
        }

        java.util.List<int[]> path = Pathfinder.shortestPath(gp, curCol, curRow, brickCell[0], brickCell[1], danger);    
        if (path != null && path.size() > 1) {
            targetCol = path.get(1)[0];
            targetRow = path.get(1)[1];
            return true;
        }
        return false;
    }

    private boolean planHunt(int curCol, int curRow, java.util.Set<String> danger) {
        int playerCol = gp.player.getCol();
        int playerRow = gp.player.getRow();
        int dist = Math.abs(playerCol - curCol) + Math.abs(playerRow - curRow);

        java.util.List<int[]> path = Pathfinder.shortestPath(gp, curCol, curRow, playerCol, playerRow, danger);
        if (path != null && path.size() > 1) {
            aiState = BotState.HUNT;
            boolean shouldBlock = (dist <= 3 && isNarrowPassage(curCol, curRow));
            boolean shouldAttack = (dist <= 2);

            if ((shouldBlock || shouldAttack) && bombCooldownTimer == 0) {
                gp.bombAlgo.placeBomb(curCol, curRow, this);
                bombCooldownTimer = BOMB_COOLDOWN;
                aiState = BotState.EVADE;
                planEvade(curCol, curRow, Pathfinder.buildDangerSet(gp));
                return true;
            }

            targetCol = path.get(1)[0];
            targetRow = path.get(1)[1];
            return true;
        }
        return false;
    }

    private void stepTowardTarget() {
        if (targetCol < 0) return;

        int ts = gp.tileM.getTileSize();
        int tx = targetCol * ts;
        int ty = targetRow * ts;

        int dx = Integer.compare(tx, worldX);
        int dy = Integer.compare(ty, worldY);

        if (dx != 0) {
            // Moving horizontally - nudge Y to center of current row
            int idealY = (worldY + ts / 2) / ts * ts;
            if (worldY < idealY)      worldY = Math.min(idealY, worldY + speed);
            else if (worldY > idealY) worldY = Math.max(idealY, worldY - speed);

            direction    = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            collisionOn  = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) worldX += direction.dx * speed;
            else replanTimer = 0; // If blocked, replan immediately
        } else if (dy != 0) {
            // Moving vertically - nudge X to center of current column
            int idealX = (worldX + ts / 2) / ts * ts;
            if (worldX < idealX)      worldX = Math.min(idealX, worldX + speed);
            else if (worldX > idealX) worldX = Math.max(idealX, worldX - speed);

            direction    = (dy > 0) ? Direction.DOWN : Direction.UP;
            collisionOn  = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) worldY += direction.dy * speed;
            else replanTimer = 0;
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
    public int     getCol()      { return (worldX + solidArea.x + solidArea.width  / 2) / gp.tileM.getTileSize(); }
    @Override
    public int     getRow()      { return (worldY + solidArea.y + solidArea.height / 2) / gp.tileM.getTileSize(); }
    @Override
    public boolean isDestroyed() { return !alive; }

    // ── Rendering — Graphics2D shapes ────────────────────────────────────────

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

        // Bot specific colors based on ID
        Color primaryColor = new Color(45, 40, 50); // Default Kuromi Black
        Color accentColor = new Color(200, 70, 100); // Default Pink
        Color dressColor = Color.decode("#9752B1"); // Default Purple

        if (botId == 1) { // Green Bot
            primaryColor = new Color(40, 60, 45);
            accentColor = new Color(100, 200, 120);
            dressColor = Color.decode("#52B16B");
        } else if (botId == 2) { // Orange/Fire Bot
            primaryColor = new Color(60, 45, 40);
            accentColor = new Color(255, 140, 50);
            dressColor = Color.decode("#B16B52");
        } else if (botId == 3) { // Blue/Ice Bot
            primaryColor = new Color(40, 45, 60);
            accentColor = new Color(100, 150, 255);
            dressColor = Color.decode("#526BB1");
        }

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(x + 8, y + s / 2 + 8, s - 10, s / 4);

        // ===== MŨ KUROMI (đầu lâu đen) =====
        g2.setColor(primaryColor);
        g2.fillRoundRect(x + s / 5, y + s / 8, 3 * s / 5, s / 2, 15, 15);

        // Tai thỏ đen bên trái
        g2.setColor(primaryColor.brighter());
        g2.fillOval(x + s / 6, y - s / 8, s / 4, s / 3);
        g2.setColor(accentColor.darker());
        g2.fillOval(x + s / 6 + 3, y - s / 12, s / 7, s / 5);

        // Tai thỏ đen bên phải
        g2.fillOval(x + s * 5 / 9, y - s / 8, s / 4, s / 3);
        g2.fillOval(x + s * 5 / 9 + 3, y - s / 12, s / 7, s / 5);

        // Đầu lâu trắng trên mũ
        g2.setColor(new Color(220, 220, 240));
        g2.fillOval(x + s / 2 - 8, y + s / 6, 16, 14);

        // Mắt đầu lâu (lỗ đen)
        g2.setColor(new Color(30, 30, 40));
        g2.fillOval(x + s / 2 - 6, y + s / 6 + 2, 4, 4);
        g2.fillOval(x + s / 2 + 2, y + s / 6 + 2, 4, 4);

        // Miệng đầu lâu (hình răng cưa)
        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(x + s / 2 - 5, y + s / 6 + 7, 3, 3);
        g2.fillRect(x + s / 2 + 2, y + s / 6 + 7, 3, 3);

        // Nơ hồng sẫm bên trái tai
        g2.setColor(accentColor);
        g2.fillOval(x + s / 5 - 2, y + s / 7, s / 6, s / 8);
        g2.fillOval(x + s / 5 + 1, y + s / 7 - 2, s / 8, s / 7);

        // ===== MẶT KUROMI =====
        g2.setColor(new Color(255, 235, 240));
        g2.fillOval(x + s / 4, y + s / 5, s / 2, s / 2);

        // Mắt to đen
        g2.setColor(new Color(40, 35, 45));
        g2.fillOval(x + s * 5 / 16, y + s / 4, s / 8, s / 7);
        g2.fillOval(x + s * 9 / 16, y + s / 4, s / 8, s / 7);

        // Điểm sáng trong mắt
        g2.setColor(accentColor.brighter());
        g2.fillOval(x + s * 5 / 16 + 2, y + s / 4 + 2, s / 20, s / 20);
        g2.fillOval(x + s * 9 / 16 + 2, y + s / 4 + 2, s / 20, s / 20);

        // Mũi nhỏ
        g2.setColor(new Color(40, 35, 45));
        g2.fillOval(x + s / 2 - 2, y + s / 3, s / 16, s / 16);

        // Miệng đểu
        g2.setColor(accentColor.darker());
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawArc(x + s / 2 - 4, y + s / 3 + 4, 8, 6, 0, -160);

        // Răng nanh nhỏ
        g2.setColor(new Color(255, 250, 250));
        g2.fillPolygon(
                new int[]{x + s / 2 - 1, x + s / 2 + 1, x + s / 2},
                new int[]{y + s / 3 + 7, y + s / 3 + 7, y + s / 3 + 10},
                3
        );

        // Má hồng
        g2.setColor(new Color(255, 130, 150, 100));
        g2.fillOval(x + s * 5 / 16 - 4, y + s * 5 / 16, s / 10, s / 12);
        g2.fillOval(x + s * 9 / 16 + 2, y + s * 5 / 16, s / 10, s / 12);

        // ===== THÂN =====
        g2.setColor(dressColor);
        g2.fillRoundRect(x + s / 5, y + s / 2, 3 * s / 5, s / 3, 15, 15);

        // Đai/viền váy
        g2.setColor(dressColor.brighter());
        g2.fillRoundRect(x + s / 5 + 2, y + s / 2 + 2, 3 * s / 5 - 4, s / 10, 5, 5);

        // Nơ đen ở thân
        g2.setColor(dressColor.darker());
        g2.fillOval(x + s / 2 - 4, y + s / 2 + 12, 8, 6);
        g2.fillOval(x + s / 2 - 8, y + s / 2 + 10, 6, 8);
        g2.fillOval(x + s / 2 + 2, y + s / 2 + 10, 6, 8);

        // Đuôi quỷ
        g2.setColor(primaryColor);
        g2.fillOval(x + s - 12, y + s / 2 + 8, 8, 6);
        g2.fillPolygon(
                new int[]{x + s - 8, x + s - 4, x + s - 12},
                new int[]{y + s / 2 + 11, y + s / 2 + 15, y + s / 2 + 13},
                3
        );

        // Tay nhỏ
        g2.setColor(new Color(255, 220, 210));
        g2.fillOval(x + s / 6, y + s / 2 + 8, s / 7, s / 9);
        g2.fillOval(x + s * 4 / 5, y + s / 2 + 8, s / 7, s / 9);

        // Chân
        g2.setColor(primaryColor.darker());
        g2.fillOval(x + s / 3, y + s * 7 / 8, s / 8, s / 12);
        g2.fillOval(x + s / 2, y + s * 7 / 8, s / 8, s / 12);

        // Râu thỏ
        g2.setColor(new Color(150, 140, 160, 120));
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.drawLine(x + s / 4, y + s / 3, x + s / 6, y + s / 3 + 4);
        g2.drawLine(x + s / 4, y + s / 3 + 6, x + s / 6, y + s / 3 + 10);
        g2.drawLine(x + s * 3 / 4, y + s / 3, x + s * 5 / 6, y + s / 3 + 4);
        g2.drawLine(x + s * 3 / 4, y + s / 3 + 6, x + s * 5 / 6, y + s / 3 + 10);

        // State indicator dot
        g2.setColor(aiState == BotState.EVADE
                ? new Color(255, 180, 50)  // Cam cho EVADE
                : accentColor); // Accent for HUNT
        g2.fillOval(x + s - 12, y + 4, 8, 8);
    }
    @Override public int getScreenX()  { return worldX;       }
    @Override public int getScreenY()  { return worldY;       }
    @Override public int getDrawSize() { return gp.tileM.getTileSize();  }
}
