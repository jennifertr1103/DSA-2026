package bomb;

import Game_2D.GamePanel;
import entity.Destructible;
import entity.Entity;
import sound.SoundManager;
import sound.SoundManager.SoundType;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Manages all active bombs and flames on the map.
 */
public class BombAlgorithm {

    private static final int DEFAULT_FLAME_DURATION_TICKS = Flame.DEFAULT_DURATION_TICKS;

    private final GamePanel gp;
    private final BombAppearance  bombAppearance  = new BombAppearance();
    private final FlameAppearance flameAppearance = new FlameAppearance();

    // Sound manager reference
    private SoundManager soundManager;

    // Cooldown để tránh spam sound bomb quá nhiều cùng lúc
    private long lastBombSoundTime = 0;
    private static final long BOMB_SOUND_COOLDOWN_MS = 100; // 100ms giữa các lần play

    /** Placed bombs awaiting detonation — FIFO order. */
    private final Queue<Bomb> bombQueue = new LinkedList<>();

    /** Chain-reaction stack — LIFO. */
    private final Deque<Bomb> chainReactionStack = new ArrayDeque<>();

    /** Currently burning flame tiles. */
    private final List<Flame> activeFlames = new ArrayList<>();

    /** Everything that can be damaged by a flame. */
    private final List<Destructible> destructibles = new ArrayList<>();

    // ── Construction ─────────────────────────────────────────────────────────

    public BombAlgorithm(GamePanel gp) {
        this.gp = gp;
        this.soundManager = gp.soundManager;
    }

    // ── Destructible registry ─────────────────────────────────────────────────

    public void registerDestructible(Destructible d) {
        if (d != null && !destructibles.contains(d)) destructibles.add(d);
    }

    public void unregisterDestructible(Destructible d) {
        destructibles.remove(d);
    }

    // ── Bomb placement ────────────────────────────────────────────────────────

    public Bomb placeBomb(int col, int row, Entity owner) {
        // Reject duplicate cell
        for (Bomb existing : bombQueue) {
            if (existing.getCol() == col && existing.getRow() == row) return null;
        }
        // Enforce per-entity cap
        if (owner != null && owner.getBombQueue().size() >= owner.getMaxBombs()) return null;

        Bomb bomb = new Bomb(gp, this, col, row,
                Bomb.DEFAULT_COUNTDOWN_TICKS,
                Bomb.DEFAULT_EXPLOSION_SCALE,
                bombAppearance, owner);

        bombQueue.offer(bomb);
        if (owner != null) owner.getBombQueue().offer(bomb);

        registerDestructible(bomb);
        return bomb;
    }

    public Bomb placeBomb(int col, int row) {
        return placeBomb(col, row, null);
    }

    public void queueChainReaction(Bomb bomb) {
        chainReactionStack.push(bomb);
    }

    // ── Per-frame update ──────────────────────────────────────────────────────

    public void update() {
        // 1) FIFO ──────────────────────────────────────────────────────────────
        List<Bomb> ready = new ArrayList<>();
        for (Bomb b : bombQueue) {
            b.update();
            if (b.isReadyToExplode()) ready.add(b);
        }
        for (Bomb b : ready) {
            bombQueue.remove(b);
            detonate(b);
        }

        // 2) LIFO chain reactions ───────────────────────────────────────────────
        while (!chainReactionStack.isEmpty()) {
            Bomb b = chainReactionStack.pop();
            bombQueue.remove(b);
            detonate(b);
        }

        // 3) Age flames ─────────────────────────────────────────────────────────
        for (Flame f : activeFlames) {
            f.destroyTarget(destructibles);
            f.update();
        }

        // 4) Sweep ──────────────────────────────────────────────────────────────
        activeFlames.removeIf(Flame::isExpired);
        destructibles.removeIf(Destructible::isDestroyed);
    }

    private void detonate(Bomb bomb) {
        // Play bomb explosion sound (with cooldown to avoid spam)
        playBombSound();

        // Remove from owner's personal queue
        if (bomb.getOwner() != null) {
            bomb.getOwner().getBombQueue().remove(bomb);
        }
        List<Flame> spawned = bomb.explode(flameAppearance, DEFAULT_FLAME_DURATION_TICKS);
        activeFlames.addAll(spawned);
        for (Flame f : spawned) f.destroyTarget(destructibles);
    }

    /**
     * Play bomb sound with cooldown to avoid overlapping too many explosions
     */
    private void playBombSound() {
        if (soundManager == null) return;

        long now = System.currentTimeMillis();
        if (now - lastBombSoundTime >= BOMB_SOUND_COOLDOWN_MS) {
            lastBombSoundTime = now;
            soundManager.play(SoundType.BOMB);
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        for (Bomb  b : bombQueue)    b.draw(g2);
        for (Flame f : activeFlames) f.draw(g2);
    }

    // ── Introspection ─────────────────────────────────────────────────────────

    public Queue<Bomb>  getActiveBombs()   { return bombQueue;          }
    public List<Flame>  getFlames()        { return activeFlames;       }
    public int          pendingBombCount() { return bombQueue.size();   }
    public int          activeFlameCount() { return activeFlames.size();}
    public int          pendingChainCount(){ return chainReactionStack.size(); }
}