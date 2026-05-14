package Game_2D;

/**
 * Refactor note: small interface added to follow the
 * Interface Segregation + Dependency Inversion principles.
 * GamePanel.update() can now iterate over any Updatable
 * (player, future enemies, NPCs, projectiles) without knowing
 * their concrete types.
 */
public interface Updatable {
    void update();

    void takeDamage();
}
