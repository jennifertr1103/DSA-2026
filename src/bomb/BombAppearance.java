package bomb;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Pure rendering class for a Bomb sprite.
 *
 * SOLID-S (Single Responsibility): Bomb itself only owns timing and
 * explosion logic. All visual concerns (animation, palette, easing)
 * live here, so swapping in real sprite-sheet artwork later only
 * requires changing this one class.
 *
 * The default implementation draws programmatically (no asset files
 * needed) so the system runs out-of-the-box. To use real sprites,
 * subclass and override draw().
 */
public class BombAppearance {

    private static final Color BODY        = new Color(20, 20, 20);
    private static final Color BODY_PULSE  = new Color(60, 30, 30);
    private static final Color HIGHLIGHT   = new Color(120, 120, 120);
    private static final Color FUSE        = new Color(180, 120, 50);
    private static final Color SPARK       = new Color(255, 220, 80);

    /**
     * @param ticksUntilExplode  remaining countdown ticks (smaller = pulse faster)
     */
    public void draw(Graphics2D g2, int screenX, int screenY, int tileSize, int ticksUntilExplode) {
        // Pulse rate: faster as the bomb gets close to detonating.
        int pulsePeriod = Math.max(6, ticksUntilExplode / 6 + 6);
        boolean swollen = ((ticksUntilExplode / pulsePeriod) & 1) == 0;

        int pad = swollen ? tileSize / 10 : tileSize / 6;
        int x = screenX + pad;
        int y = screenY + pad;
        int size = tileSize - pad * 2;

        // Body
        g2.setColor(swollen ? BODY_PULSE : BODY);
        g2.fillOval(x, y, size, size);

        // Top highlight (gives it volume)
        g2.setColor(HIGHLIGHT);
        g2.fillOval(x + size / 5, y + size / 6, size / 4, size / 5);

        // Fuse
        int fuseX = x + size / 2;
        int fuseY = y - tileSize / 10;
        g2.setColor(FUSE);
        g2.drawLine(fuseX, y, fuseX + tileSize / 12, fuseY);

        // Spark on the fuse tip — flashes once per pulse
        if (swollen) {
            g2.setColor(SPARK);
            g2.fillOval(fuseX + tileSize / 12 - 3, fuseY - 3, 6, 6);
        }
    }
}
