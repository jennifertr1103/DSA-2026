package bomb;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * Better looking flame renderer.
 * Softer glow + animated fire core.
 */
public class FlameAppearance {

    private static final Color OUTER = new Color(255, 80, 20);
    private static final Color MID   = new Color(255, 160, 40);
    private static final Color CORE  = new Color(255, 245, 180);

    private final Random random = new Random();

    public void draw(Graphics2D g2,
                     int screenX, int screenY, int tileSize,
                     FlameDirection dir,
                     int ticksRemaining, int totalTicks) {

        float life = totalTicks <= 0
                ? 1f
                : Math.max(0f, Math.min(1f,
                ticksRemaining / (float) totalTicks));

        float alpha = Math.min(1f, life * 1.4f);

        Composite old = g2.getComposite();
        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        alpha
                )
        );

        // Anti-alias = smoother flame
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int pad = tileSize / 8;

        // Slight flicker animation
        int flicker = random.nextInt(3);

        int x = screenX + pad;
        int y = screenY + pad;
        int w = tileSize - pad * 2;
        int h = tileSize - pad * 2;

        // =========================
        // OUTER GLOW
        // =========================
        g2.setColor(new Color(
                OUTER.getRed(),
                OUTER.getGreen(),
                OUTER.getBlue(),
                90
        ));

        drawDirectionalShape(g2, dir,
                screenX - 2,
                screenY - 2,
                tileSize + 4,
                tileSize + 4,
                true);

        // =========================
        // MIDDLE FIRE
        // =========================
        g2.setColor(MID);

        drawDirectionalShape(g2, dir,
                x - flicker,
                y - flicker,
                w + flicker * 2,
                h + flicker * 2,
                false);

        // =========================
        // HOT CORE
        // =========================
        g2.setColor(CORE);

        drawDirectionalShape(g2, dir,
                x + w / 5,
                y + h / 5,
                w - (w / 5) * 2,
                h - (h / 5) * 2,
                false);

        // restore
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
        g2.setComposite(old);
    }

    /**
     * Draw flame shape depending on direction.
     */
    private void drawDirectionalShape(Graphics2D g2,
                                      FlameDirection dir,
                                      int x, int y,
                                      int w, int h,
                                      boolean glow) {

        int arc = Math.max(8, w / 3);

        switch (dir) {

            case CENTER:
                g2.fill(new Ellipse2D.Float(x, y, w, h));
                break;

            case UP:
                g2.fill(new RoundRectangle2D.Float(
                        x + w / 3f,
                        y,
                        w / 3f,
                        h,
                        arc,
                        arc
                ));

                // flame tip
                g2.fillPolygon(
                        new int[]{
                                x + w / 2,
                                x + w / 3,
                                x + (2 * w / 3)
                        },
                        new int[]{
                                y - h / 4,
                                y + h / 6,
                                y + h / 6
                        },
                        3
                );
                break;

            case DOWN:
                g2.fill(new RoundRectangle2D.Float(
                        x + w / 3f,
                        y,
                        w / 3f,
                        h,
                        arc,
                        arc
                ));

                g2.fillPolygon(
                        new int[]{
                                x + w / 2,
                                x + w / 3,
                                x + (2 * w / 3)
                        },
                        new int[]{
                                y + h + h / 4,
                                y + h - h / 6,
                                y + h - h / 6
                        },
                        3
                );
                break;

            case LEFT:
                g2.fill(new RoundRectangle2D.Float(
                        x,
                        y + h / 3f,
                        w,
                        h / 3f,
                        arc,
                        arc
                ));

                g2.fillPolygon(
                        new int[]{
                                x - w / 4,
                                x + w / 6,
                                x + w / 6
                        },
                        new int[]{
                                y + h / 2,
                                y + h / 3,
                                y + (2 * h / 3)
                        },
                        3
                );
                break;

            case RIGHT:
                g2.fill(new RoundRectangle2D.Float(
                        x,
                        y + h / 3f,
                        w,
                        h / 3f,
                        arc,
                        arc
                ));

                g2.fillPolygon(
                        new int[]{
                                x + w + w / 4,
                                x + w - w / 6,
                                x + w - w / 6
                        },
                        new int[]{
                                y + h / 2,
                                y + h / 3,
                                y + (2 * h / 3)
                        },
                        3
                );
                break;
        }

        // extra center glow
        if (glow && dir == FlameDirection.CENTER) {
            g2.fillOval(
                    x + w / 4,
                    y + h / 4,
                    w / 2,
                    h / 2
            );
        }
    }
}