package entity;

/**
 * Anything that flames can destroy implements this interface:
 * the player, monsters, items, crates, and other Bombs (so that
 * flames can trigger chain-reaction explosions).
 *
 * SOLID notes:
 *  - Interface Segregation: a tiny, single-purpose contract.
 *  - Open/Closed: BombAlgorithm/Flame work against this abstraction,
 *    so adding new destructible entity types (e.g. Monster, Crate)
 *    does not require editing the bomb system.
 */

public interface Destructible {
    void onDestroyedByFlame();
    boolean isDestroyed();
    int getCol();
    int getRow();
}