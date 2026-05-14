package tile;

import java.awt.Color;

/**
 * Lightweight tile descriptor — no image assets required.
 *
 * CHANGES from original:
 *   • Replaced BufferedImage with a Color used by Graphics2D rendering,
 *     so the project compiles with zero external asset files.
 *   • Added TileType enum for clear intent in switch statements.
 *   • Kept collision / destructible flags exactly as they were.
 */
public class Tile {

    public enum TileType { PATH, HARD_WALL, BRICK_WALL }

    public final TileType type;
    public final Color    baseColor;
    public final boolean  collision;     // blocks movement and flame spread
    public final boolean  destructible;  // flame destroys this tile

    public Tile(TileType type, Color baseColor, boolean collision, boolean destructible) {
        this.type         = type;
        this.baseColor    = baseColor;
        this.collision    = collision;
        this.destructible = destructible;
    }
}
