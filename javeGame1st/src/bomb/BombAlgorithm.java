package bomb;

import Game_2D.gamePanel;
import entity.Destructible;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Central manager for the Bomberman explosion system.
 *
 * DATA STRUCTURES (per spec):
 *
 *  1. {@link #bombQueue}  — Queue<Bomb>, FIFO.
 *     Holds every bomb that has been placed but has not yet exploded.
 *     Each tick we walk the queue, decrement countdowns, and dequeue any
 *     bomb whose countdown has reached zero. FIFO is the natural fit:
 *     "the first bomb you placed is the first one whose timer expires".
 *
 *  2. {@link #chainReactionStack}  — Deque<Bomb> used as a Stack, LIFO.
 *     When a flame from bomb A touches bomb B, B is *not* allowed to
 *     explode mid-iteration (that would corrupt the queue walk and could
 *     recurse arbitrarily deep). Instead B is pushed onto this stack.
 *     After we finish handling A's explosion, we drain the stack — last
 *     pushed = first detonated, which feels right for cascading "boom,
 *     boom, boom" effects radiating outward.
 *     This is also a natural place to store other LIFO effects later
 *     (rewind/undo, layered visual effects, "last frame to repaint").
 *
 *  3. {@link #activeFlames}  — List<Flame>.
 *     Live flames currently rendered; iterated each tick to age them out
 *     and to call destroyTarget() against the registered Destructible
 *     list.
 *
 *  4. {@link #destructibles}  — List<Destructible>.
 *     The "things that flames can hurt" registry: player(s), monsters,
 *     items, other bombs. Registration is push-based so the bomb system
 *     never needs to know concrete entity types. (SOLID-D / OCP)
 *
 * SOLID notes:
 *  - SRP: this class only orchestrates. Spread math is in Bomb;
 *    destruction logic is in Flame; rendering is in *Appearance.
 *  - OCP: new entity types just register themselves as Destructible.
 *  - DIP: the bomb package depends on tile.TileManager only through the
 *    narrow isSolid/isBrick/destroyBrick methods.
 */
public class BombAlgorithm {

    private static final int DEFAULT_FLAME_DURATION_TICKS = Flame.DEFAULT_DURATION_TICKS;

    private final gamePanel gp;
    private final BombAppearance bombAppearance   = new BombAppearance();
    private final FlameAppearance flameAppearance = new FlameAppearance();

    /** Bombs placed but not yet exploded — FIFO. */
    private final Queue<Bomb> bombQueue = new LinkedList<>();

    /** Chain-reaction queue, used as a Stack (LIFO) — see class javadoc. */
    private final Deque<Bomb> chainReactionStack = new ArrayDeque<>();

    /** Flames currently burning. */
    private final List<Flame> activeFlames = new ArrayList<>();

    /** Anything in this list can be destroyed by a flame. */
    private final List<Destructible> destructibles = new ArrayList<>();

    public BombAlgorithm(gamePanel gp) {
        this.gp = gp;
    }

    // -------------------------------------------------------------- registration

    public void registerDestructible(Destructible d) {
        if (!destructibles.contains(d)) destructibles.add(d);
    }

    public void unregisterDestructible(Destructible d) {
        destructibles.remove(d);
    }


    public Bomb placeBomb(int col, int row, int countdownTicks, int explosionScale) {
        for (Bomb existing : bombQueue) {
            if (existing.getCol() == col && existing.getRow() == row) return null;
        }
        Bomb bomb = new Bomb(gp, this, col, row, countdownTicks, explosionScale, bombAppearance);
        bombQueue.offer(bomb);
        // Bombs are themselves Destructible — flames will trigger chain reactions.
        registerDestructible(bomb);
        return bomb;
    }

    /** Convenience: place a bomb with default parameters. */
    public Bomb placeBomb(int col, int row) {
        return placeBomb(col, row, Bomb.DEFAULT_COUNTDOWN_TICKS, Bomb.DEFAULT_EXPLOSION_SCALE);
    }

    // -------------------------------------------------------------- chain-reaction hook

    /** Called by Bomb.onDestroyedByFlame(): defer the cascade to the stack. */
    public void queueChainReaction(Bomb bomb) {
        chainReactionStack.push(bomb);
    }

    // -------------------------------------------------------------- main tick

    // Giả sử đây là một phần trong thuật toán nổ của bạn

    public void update() {
        // 1) FIFO bomb timers ------------------------------------------------
        Iterator<Bomb> bIt = bombQueue.iterator();
        List<Bomb> readyToDetonate = new ArrayList<>();
        while (bIt.hasNext()) {
            Bomb b = bIt.next();
            b.update();
            if (b.isReadyToExplode()) {
                bIt.remove();
                readyToDetonate.add(b);
            }
        }
        for (Bomb b : readyToDetonate) detonate(b);

        // 2) LIFO chain reactions -------------------------------------------
        while (!chainReactionStack.isEmpty()) {
            Bomb b = chainReactionStack.pop();
            bombQueue.remove(b); // also pull it out of the FIFO if it was still there
            if (!b.isDestroyed() || !b.isReadyToExplode()) {
                detonate(b);
            } else {
                detonate(b);
            }
        }

        // 3) Age flames + run destroyTarget --------------------------------
        for (Flame f : activeFlames) {
            f.destroyTarget(destructibles);
            f.update();
        }

        // 4) Sweep -----------------------------------------------------------
        activeFlames.removeIf(Flame::isExpired);
        destructibles.removeIf(Destructible::isDestroyed);
    }

    private void detonate(Bomb bomb) {
        List<Flame> spawned = bomb.explode(flameAppearance, DEFAULT_FLAME_DURATION_TICKS);
        activeFlames.addAll(spawned);
        // Run destroyTarget once immediately so anything standing on the
        // freshly-spawned flames (including other bombs) is hit on this
        // very tick — this is what makes chain reactions feel snappy.
        for (Flame f : spawned) f.destroyTarget(destructibles);
    }

    // -------------------------------------------------------------- rendering

    public void draw(Graphics2D g2) {
        for (Bomb b : bombQueue) b.draw(g2);
        for (Flame f : activeFlames) f.draw(g2);
    }

    // -------------------------------------------------------------- introspection (for debugging / tests)

    public int pendingBombCount()  { return bombQueue.size(); }
    public int activeFlameCount()  { return activeFlames.size(); }
    public int pendingChainCount() { return chainReactionStack.size(); }
}
