package entity;

public interface Destructible {
    /** Tile-grid column of this entity. */
    int getCol();

    /** Tile-grid row of this entity. */
    int getRow();

    /**
     * Called by a Flame whose tile coincides with this entity.
     * Implementations decide what "destroyed" means (despawn, set a
     * dead flag, push self onto chain-reaction stack, drop loot, ...).
     */
    void onDestroyedByFlame();

    /**
     * Lets the flame skip already-dead targets.
     */
    default boolean isDestroyed() {
        return false;
    }
}
