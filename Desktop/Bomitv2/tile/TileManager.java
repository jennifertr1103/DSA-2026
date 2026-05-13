package tile;

import Game_2D.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Manages the 2-D tile grid.
 *
 * CHANGES from original:
 *   • Removed all image / file-system loading (BufferedImage, ImageIO,
 *     map.txt).  The map is now generated procedurally so the project
 *     compiles without any asset files.
 *   • Tile catalog switched from BufferedImage → Color (see Tile.java).
 *   • draw() uses Graphics2D shapes instead of g2.drawImage().
 *   • isSolid() / isBrick() / destroyBrick() signatures unchanged —
 *     CollisionChecker and BombAlgorithm call them exactly as before.
 *
 * Tile IDs (unchanged):
 *   0 = PATH       – walkable, indestructible
 *   2 = HARD_WALL  – solid, indestructible  (checkerboard pillars + border)
 *   3 = BRICK_WALL – solid, destructible by flame
 */
public class TileManager {

    // ── Tile ID constants (matches original map.txt convention) ──────────────
    public static final int ID_PATH      = 0;
    public static final int ID_HARD_WALL = 2;
    public static final int ID_BRICK     = 3;

    // ── Colours for Graphics2D rendering ────────────────────────────────────
    private static final Color C_PATH_FILL  = new Color(200, 182, 140);
    private static final Color C_PATH_GRID  = new Color(180, 162, 120);
    private static final Color C_HARD_FILL  = new Color(65, 65, 78);
    private static final Color C_HARD_HI    = new Color(105, 105, 118);
    private static final Color C_HARD_SH    = new Color(35, 35, 42);
    private static final Color C_BRICK_FILL = new Color(185, 78, 35);
    private static final Color C_BRICK_HI   = new Color(230, 118, 68);
    private static final Color C_BRICK_MRT  = new Color(130, 48, 15);

    // ── State ────────────────────────────────────────────────────────────────

    private final GamePanel gp;

    /** Tile catalog indexed by tile ID. */
    public Tile[] tile;

    /**
     * 2-D grid of tile IDs.
     * Indexed [col][row] to mirror the original TileManager convention.
     */
    public int[][] mapTileNum;

    // ── Construction ─────────────────────────────────────────────────────────

    public TileManager(GamePanel gp) {
        this.gp         = gp;
        this.tile       = buildCatalog();
        this.mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        generateMap(new Random(0xDEADBEEFL));
    }

    /** Three-entry catalog: PATH (0), HARD_WALL (2), BRICK (3). */
    private Tile[] buildCatalog() {
        Tile[] t = new Tile[4];
        t[ID_PATH]      = new Tile(Tile.TileType.PATH,       C_PATH_FILL,  false, false);
        t[1]            = t[ID_PATH];  // slot 1 unused – alias to PATH to avoid NPE
        t[ID_HARD_WALL] = new Tile(Tile.TileType.HARD_WALL,  C_HARD_FILL,  true,  false);
        t[ID_BRICK]     = new Tile(Tile.TileType.BRICK_WALL, C_BRICK_FILL, true,  true);
        return t;
    }

    /**
     * Generates the classic Bomberman checkerboard layout:
     *   – Border ring   → HARD_WALL
     *   – Even-row, even-col pillars → HARD_WALL
     *   – 3×3 corners around each spawn → PATH (clear zone)
     *   – Everything else → random PATH / BRICK
     */
    private void generateMap(Random rng) {
        int cols = gp.maxWorldCol;
        int rows = gp.maxWorldRow;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (isBorderOrPillar(col, row, cols, rows)) {
                    mapTileNum[col][row] = ID_HARD_WALL;
                } else if (isSpawnProtected(col, row, cols, rows)) {
                    mapTileNum[col][row] = ID_PATH;
                } else {
                    mapTileNum[col][row] = (rng.nextFloat() < 0.44f) ? ID_BRICK : ID_PATH;
                }
            }
        }
    }

    private boolean isBorderOrPillar(int col, int row, int cols, int rows) {
        return col == 0 || col == cols - 1 || row == 0 || row == rows - 1
                || (row % 2 == 0 && col % 2 == 0);
    }

    /** Clear the 3×3 areas around the two spawn corners. */
    private boolean isSpawnProtected(int col, int row, int cols, int rows) {
        boolean playerCorner = (col <= 2 && row <= 2);
        boolean botCorner    = (col >= cols - 3 && row >= rows - 3);
        return playerCorner || botCorner;
    }

    // ── Queries (API unchanged from original) ────────────────────────────────

    /** Bounds-safe solidity check used by CollisionChecker. */
    public boolean isSolid(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.maxWorldCol || row >= gp.maxWorldRow) return true;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return true;
        return tile[id].collision;
    }

    /** True if the tile at (col,row) is a destructible brick. */
    public boolean isBrick(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.maxWorldCol || row >= gp.maxWorldRow) return false;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return false;
        return tile[id].destructible;
    }

    /** Replace the brick at (col,row) with PATH. No-op for non-bricks. */
    public void destroyBrick(int col, int row) {
        if (isBrick(col, row)) mapTileNum[col][row] = ID_PATH;
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        int t = gp.tileSize;
        for (int row = 0; row < gp.maxWorldRow; row++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                drawTile(g2, col, row, col * t, row * t, t);
            }
        }
    }

    private void drawTile(Graphics2D g2, int col, int row, int x, int y, int t) {
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return;

        switch (tile[id].type) {

            case PATH:
                g2.setColor(C_PATH_FILL);
                g2.fillRect(x, y, t, t);
                g2.setColor(C_PATH_GRID);
                g2.drawRect(x, y, t, t);
                break;

            case HARD_WALL:
                g2.setColor(C_HARD_FILL);
                g2.fillRect(x, y, t, t);
                // Bevel highlight (top-left)
                g2.setColor(C_HARD_HI);
                g2.drawLine(x + 1, y + 1, x + t - 2, y + 1);
                g2.drawLine(x + 1, y + 1, x + 1,     y + t - 2);
                // Bevel shadow (bottom-right)
                g2.setColor(C_HARD_SH);
                g2.drawLine(x + t - 1, y + 1,     x + t - 1, y + t - 1);
                g2.drawLine(x + 1,     y + t - 1, x + t - 1, y + t - 1);
                break;

            case BRICK_WALL:
                g2.setColor(C_BRICK_FILL);
                g2.fillRect(x, y, t, t);
                g2.setColor(C_BRICK_HI);
                g2.drawRect(x + 1, y + 1, t - 3, t - 3);
                // Mortar lines — 3-row brick pattern
                g2.setColor(C_BRICK_MRT);
                int h = t / 3;
                g2.drawLine(x,         y + h,     x + t,     y + h);
                g2.drawLine(x,         y + 2 * h, x + t,     y + 2 * h);
                g2.drawLine(x + t / 4, y,         x + t / 4, y + h);
                g2.drawLine(x + 3*t/4, y,         x + 3*t/4, y + h);
                g2.drawLine(x + t / 2, y + h,     x + t / 2, y + 2 * h);
                g2.drawLine(x + t / 4, y + 2 * h, x + t / 4, y + t);
                g2.drawLine(x + 3*t/4, y + 2 * h, x + 3*t/4, y + t);
                break;
        }
    }
}
