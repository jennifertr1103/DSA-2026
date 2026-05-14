package bomb;

import Game_2D.GamePanel;
import entity.Destructible;
import entity.Entity;

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
 *
 * CHANGES from original:
 *   • placeBomb(col, row, owner) — new overload that records the Entity
 *     who placed the bomb.  On detonation the bomb is removed from its
 *     owner's personal Queue<Bomb> (per-entity FIFO requirement).
 *   • Old placeBomb(col, row) kept as a convenience no-op owner overload.
 *   • Everything else (FIFO bombQueue, LIFO chainReactionStack, flame
 *     aging, destructible sweeps) is identical to the original.
 */
public class BombAlgorithm {

    private static final int DEFAULT_FLAME_DURATION_TICKS = Flame.DEFAULT_DURATION_TICKS;

    private final GamePanel gp;
    private final BombAppearance  bombAppearance  = new BombAppearance();
    private final FlameAppearance flameAppearance = new FlameAppearance();

    /** Placed bombs awaiting detonation — FIFO order. */
    private final Queue<Bomb> bombQueue = new LinkedList<>();

    /**
     * Chain-reaction stack — LIFO.
     * When bomb A's flame hits bomb B, B is pushed here; it explodes on
     * the next tick so the cascade is deterministic and stack-safe.
     */
    private final Deque<Bomb> chainReactionStack = new ArrayDeque<>();

    /** Currently burning flame tiles. */
    private final List<Flame> activeFlames = new ArrayList<>();

    /** Everything that can be damaged by a flame (entities + other bombs). */
    private final List<Destructible> destructibles = new ArrayList<>();

    // ── Construction ─────────────────────────────────────────────────────────

    public BombAlgorithm(GamePanel gp) {
        this.gp = gp;
    }

    // ── Destructible registry ─────────────────────────────────────────────────

    public void registerDestructible(Destructible d) {
        if (d != null && !destructibles.contains(d)) destructibles.add(d);
    }

    public void unregisterDestructible(Destructible d) {
        destructibles.remove(d);
    }

    // ── Bomb placement ────────────────────────────────────────────────────────

    /**
     * Primary placement method — owner-aware.
     *
     * Enforces the per-entity bomb cap via owner.getBombQueue().size().
     * On success the bomb is added to BOTH:
     *   1. this.bombQueue     (global FIFO for timing)
     *   2. owner.getBombQueue() (personal FIFO for cap / UI)
     *
     * Returns the Bomb, or null if rejected (duplicate cell or cap reached).
     */
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

        bombQueue.offer(bomb);                     // global FIFO
        if (owner != null) owner.getBombQueue().offer(bomb);  // personal FIFO

        registerDestructible(bomb);                // flames can chain-detonate it
        return bomb;
    }

    /** Convenience overload (no owner — used by tests or map events). */
    public Bomb placeBomb(int col, int row) {
        return placeBomb(col, row, null);
    }

    // ── Chain-reaction hook ───────────────────────────────────────────────────

    /** Called by Bomb.onDestroyedByFlame(): defers the cascade to the LIFO stack. */
    public void queueChainReaction(Bomb bomb) {
        chainReactionStack.push(bomb);
    }

    // ── Per-frame update ──────────────────────────────────────────────────────

    /**
     * Order:
     *  1. FIFO bomb timers — detonate anything whose countdown reached 0.
     *  2. LIFO chain reactions — pop and detonate cascading bombs.
     *  3. Age flames; each calls destroyTarget() against the registry.
     *  4. Sweep expired flames and destroyed destructibles.
     */
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
        // Remove from owner's personal queue so they can place the next bomb
        if (bomb.getOwner() != null) {
            bomb.getOwner().getBombQueue().remove(bomb);
        }
        List<Flame> spawned = bomb.explode(flameAppearance, DEFAULT_FLAME_DURATION_TICKS);
        activeFlames.addAll(spawned);
        for (Flame f : spawned) f.destroyTarget(destructibles);
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
