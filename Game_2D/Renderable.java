package Game_2D;

import java.awt.Graphics2D;

/**
 * Refactor note: companion to Updatable. The render loop talks to
 * Renderable; concrete types decide how to draw themselves.
 */
public interface Renderable {
    void draw(Graphics2D g2);

}
