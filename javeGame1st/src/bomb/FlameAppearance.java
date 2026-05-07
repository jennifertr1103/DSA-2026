package bomb;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * Pure rendering class for a Flame tile.
 *
 * Same SOLID-S motivation as BombAppearance: keep all visual concerns
 * out of Flame so the logic stays unit-testable and so swapping in
 * sprite-sheet artwork is a one-class change.
 *
 * Renders programmatically: a hot inner core plus directional arms.
 * Fades out as the duration runs down.
 */
public class FlameAppearance {

    private static final Color CORE  = new Color(255, 240, 120);
    private static final Color OUTER = new Color(255, 110, 30);

    public void draw(Graphics2D g2,
                     int screenX, int screenY, int tileSize,
                     FlameDirection dir,
                     int ticksRemaining, int totalTicks) {

        // Fade out over the last third of the lifetime.
        float life = totalTicks <= 0 ? 1f : Math.max(0f, Math.min(1f, ticksRemaining / (float) totalTicks));
        float alpha = (float) Math.min(1.0, life * 1.4); // hold full intensity, then fade

        Composite original = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int pad = tileSize / 8;
        int x = screenX + pad;
        int y = screenY + pad;
        int size = tileSize - pad * 2;

        // Outer glow
        g2.setColor(OUTER);
        switch (dir) {
            case CENTER:
                g2.fillOval(x, y, size, size);
                break;
            case UP:
            case DOWN:
                g2.fillRect(x + size / 4, screenY, size / 2, tileSize);
                break;
            case LEFT:
            case RIGHT:
                g2.fillRect(screenX, y + size / 4, tileSize, size / 2);
                break;
        }

        // Hot core
        g2.setColor(CORE);
        int coreInset = size / 4;
        switch (dir) {
            case CENTER:
                g2.fillOval(x + coreInset / 2, y + coreInset / 2, size - coreInset, size - coreInset);
                break;
            case UP:
            case DOWN:
                g2.fillRect(x + size / 3, screenY + tileSize / 8, size / 3, tileSize - tileSize / 4);
                break;
            case LEFT:
            case RIGHT:
                g2.fillRect(screenX + tileSize / 8, y + size / 3, tileSize - tileSize / 4, size / 3);
                break;
        }

        g2.setComposite(original);
    }
}
