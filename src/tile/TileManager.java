package tile;

import Game_2D.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class TileManager {

    // ── Tile ID constants ──────────────────────────────────────────────
    public static final int ID_PATH      = 0;
    public static final int ID_HARD_WALL = 2;
    public static final int ID_BRICK     = 3;

    // ── MAP STYLES ─────────────────────────────────────────────────────
    public enum MapStyle {
        WOOD_CHERRY,   // Sàn gỗ + hoa anh đào
        PIXEL_GRASS    // Cỏ pixel tươi sáng + hoa pixel
    }

    private MapStyle currentStyle = MapStyle.WOOD_CHERRY;

    // Style 1: Màu sàn gỗ và anh đào
    private static final Color C_WOOD_FILL   = new Color(185, 125, 85);
    private static final Color C_WOOD_LINES  = new Color(145, 90, 55);
    private static final Color C_WOOD_LIGHT  = new Color(205, 145, 105);
    private static final Color C_PATH_PETAL  = new Color(255, 190, 210);
    private static final Color C_STONE_BASE  = new Color(135, 140, 145);
    private static final Color C_STONE_HI    = new Color(185, 190, 195);
    private static final Color C_STONE_SH    = new Color(85, 90, 95);
    private static final Color C_STONE_CORE  = new Color(105, 110, 115);
    private static final Color C_FLOWER_PETAL_1 = new Color(255, 130, 175);
    private static final Color C_FLOWER_PETAL_2 = new Color(255, 185, 215);
    private static final Color C_FLOWER_CENTER  = new Color(255, 225, 90);
    private static final Color C_FLOWER_CORE    = new Color(240, 100, 40);

    // Style 2: Màu cỏ pixel tươi sáng
    private static final Color C_PATH_FILL   = new Color(110, 215, 95);
    private static final Color C_PATH_GRID   = new Color(85, 185, 70);
    private static final Color C_PATH_DOT    = new Color(140, 240, 120);
    private static final Color C_HARD_FILL   = new Color(115, 115, 125);
    private static final Color C_HARD_HI     = new Color(170, 170, 180);
    private static final Color C_HARD_SH     = new Color(60, 60, 70);
    private static final Color C_HARD_DETAIL = new Color(85, 85, 95);
    private static final Color C_BRK_FILL    = new Color(185, 85, 55);
    private static final Color C_BRK_HI      = new Color(220, 125, 95);
    private static final Color C_BRK_SH      = new Color(120, 45, 25);
    private static final Color C_BORDER_GOLD = new Color(240, 180, 40);
    private static final Color C_BORDER_SHADOW = new Color(140, 95, 10);

    // ── Map Data ────────────────────────────────────────────────────────
    public enum MapType { CLASSIC, IMAGE }
    private MapType currentMapType = MapType.CLASSIC;
    private java.awt.image.BufferedImage fullMapImage;

    public int getTileSize() { return 64; }
    public int getMaxCol()   { return 16; }
    public int getMaxRow()   { return 12; }

    private final GamePanel gp;
    public Tile[] tile;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.tile = buildCatalog();
        loadMapAssets();
        setMap(MapType.CLASSIC);
    }

    public void setMapStyle(MapStyle style) {
        this.currentStyle = style;
    }

    public MapStyle getMapStyle() {
        return currentStyle;
    }

    private void loadMapAssets() {
        try {
            fullMapImage = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/map/map chơi 1.png"));
        } catch (Exception e) {
            System.err.println("Could not load full map image: " + e.getMessage());
        }
    }

    public void setMap(MapType type) {
        this.currentMapType = type;
        int rows = getMaxRow();
        int cols = getMaxCol();
        mapTileNum = new int[cols][rows];

        if (type == MapType.CLASSIC) {
            int[][] classicMap = {
                    {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2},
                    {2,0,3,3,3,3,0,0,0,0,0,0,3,0,0,2},
                    {2,0,2,3,2,0,2,3,2,0,2,0,2,3,2,2},
                    {2,3,0,0,3,3,0,0,0,0,0,0,3,3,3,2},
                    {2,0,2,0,2,0,2,0,2,0,2,0,2,0,2,2},
                    {2,0,0,0,3,0,3,0,0,0,3,0,0,0,3,2},
                    {2,3,2,3,2,0,2,0,2,0,2,3,2,3,2,2},
                    {2,0,3,3,0,3,0,0,0,3,3,0,0,3,3,2},
                    {2,3,2,3,2,3,2,0,2,3,2,3,2,0,2,2},
                    {2,3,0,3,0,0,3,3,3,0,3,0,3,3,0,2},
                    {2,0,2,3,2,0,2,3,2,0,2,3,2,3,0,2},
                    {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2}
            };
            for (int r = 0; r < 12; r++) {
                for (int c = 0; c < 16; c++) {
                    mapTileNum[c][r] = classicMap[r][c];
                }
            }
        } else {
            Random rng = new Random();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) {
                        mapTileNum[c][r] = ID_HARD_WALL;
                    } else if (r % 2 == 0 && c % 2 == 0) {
                        mapTileNum[c][r] = ID_HARD_WALL;
                    } else {
                        boolean isNearSpawn = (r <= 2 && c <= 2) || (r <= 2 && c >= cols - 3) ||
                                (r >= rows - 3 && c <= 2) || (r >= rows - 3 && c >= cols - 3);
                        if (!isNearSpawn && rng.nextInt(100) < 60) {
                            mapTileNum[c][r] = ID_BRICK;
                        } else {
                            mapTileNum[c][r] = ID_PATH;
                        }
                    }
                }
            }
        }

        int mc = getMaxCol();
        int mr = getMaxRow();
        mapTileNum[1][1] = ID_PATH;
        mapTileNum[mc-2][1] = ID_PATH;
        mapTileNum[1][mr-2] = ID_PATH;
        mapTileNum[mc-2][mr-2] = ID_PATH;
    }

    public void reset() {
        setMap(currentMapType);
    }

    public MapType getCurrentMapType() {
        return currentMapType;
    }

    private Tile[] buildCatalog() {
        Tile[] t = new Tile[4];
        t[ID_PATH]      = new Tile(Tile.TileType.PATH, C_WOOD_FILL, false, false);
        t[1]            = t[ID_PATH];
        t[ID_HARD_WALL] = new Tile(Tile.TileType.HARD_WALL, C_STONE_BASE, true, false);
        t[ID_BRICK]     = new Tile(Tile.TileType.BRICK_WALL, C_FLOWER_PETAL_1, true, true);
        return t;
    }

    public boolean isSolid(int col, int row) {
        if (col < 0 || row < 0 || col >= getMaxCol() || row >= getMaxRow()) return true;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return true;
        return tile[id].collision;
    }

    public boolean isBrick(int col, int row) {
        if (col < 0 || row < 0 || col >= getMaxCol() || row >= getMaxRow()) return false;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return false;
        return tile[id].destructible;
    }

    public void destroyBrick(int col, int row) {
        if (isBrick(col, row)) mapTileNum[col][row] = ID_PATH;
    }

    public void draw(Graphics2D g2) {
        int t = getTileSize();
        if (currentMapType == MapType.IMAGE && fullMapImage != null) {
            g2.drawImage(fullMapImage, 0, 0, getMaxCol() * t, getMaxRow() * t, null);
            for (int row = 0; row < getMaxRow(); row++) {
                for (int col = 0; col < getMaxCol(); col++) {
                    int id = mapTileNum[col][row];
                    if (id == ID_HARD_WALL || id == ID_BRICK) {
                        drawTile(g2, col, row, col * t, row * t, t);
                    }
                }
            }
        } else {
            for (int row = 0; row < getMaxRow(); row++) {
                for (int col = 0; col < getMaxCol(); col++) {
                    drawTile(g2, col, row, col * t, row * t, t);
                }
            }
        }
    }

    private void drawTile(Graphics2D g2, int col, int row, int x, int y, int t) {
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return;
        int p = Math.max(1, t / 16);

        switch (tile[id].type) {
            case PATH:
                if (currentStyle == MapStyle.WOOD_CHERRY) {
                    drawWoodenFloor(g2, x, y, t, p);
                    // Cánh hoa đào rơi
                    g2.setColor(C_PATH_PETAL);
                    if ((col + row) % 3 == 0) {
                        g2.fillRect(x + p*4, y + p*5, p*2, p);
                        g2.fillRect(x + p*5, y + p*4, p, p*2);
                    }
                } else {
                    drawPixelGrass(g2, x, y, t, p);
                }
                break;

            case HARD_WALL:
                if (currentStyle == MapStyle.WOOD_CHERRY) {
                    drawStonePillar(g2, x, y, t, p);
                } else {
                    drawPixelStone(g2, x, y, t, p, col, row);
                }
                break;

            case BRICK_WALL:
                if (currentMapType == MapType.IMAGE) {
                    drawPixelBrick(g2, x, y, t, p);
                } else {
                    if (currentStyle == MapStyle.WOOD_CHERRY) {
                        drawCherryFlower(g2, x, y, t, p);
                    } else {
                        drawPixelFlower(g2, x, y, t, p);
                    }
                }
                break;
        }
    }

    // ==================== STYLE 1: WOOD & CHERRY ====================
    private void drawWoodenFloor(Graphics2D g2, int x, int y, int t, int p) {
        g2.setColor(C_WOOD_FILL);
        g2.fillRect(x, y, t, t);
        g2.setColor(C_WOOD_LINES);
        g2.fillRect(x, y, p, t);
        g2.fillRect(x + t/2, y, p, t);
        g2.setColor(C_WOOD_LIGHT);
        g2.fillRect(x + p*2, y + p*3, p*4, p);
        g2.fillRect(x + t/2 + p*3, y + p*8, p*3, p);
    }

    private void drawStonePillar(Graphics2D g2, int x, int y, int t, int p) {
        g2.setColor(C_STONE_SH);
        g2.fillRect(x, y, t, t);
        g2.setColor(C_STONE_BASE);
        g2.fillRect(x + p, y + p, t - p*2, t - p*2);
        g2.setColor(C_STONE_HI);
        g2.fillRect(x + p, y + p, t - p*2, p*2);
        g2.fillRect(x + p, y + p, p*2, t - p*2);
        g2.setColor(C_STONE_CORE);
        g2.fillRect(x + p*4, y + p*4, t - p*8, t - p*8);
    }

    private void drawCherryFlower(Graphics2D g2, int x, int y, int t, int p) {
        drawWoodenFloor(g2, x, y, t, p);
        int cx = x + t / 2;
        int cy = y + t / 2;
        int r = p * 4;

        g2.setColor(new Color(110, 65, 40));
        g2.fillRect(cx - r - p, cy - r + p, r * 2 + p * 2, r * 2 + p * 2);
        g2.setColor(C_FLOWER_PETAL_1);
        g2.fillRect(cx - p*3, cy - r - p*2, p*6, p*4);
        g2.fillRect(cx - p*3, cy + r - p*2, p*6, p*4);
        g2.fillRect(cx - r - p*2, cy - p*3, p*4, p*6);
        g2.fillRect(cx + r - p*2, cy - p*3, p*4, p*6);
        g2.setColor(C_FLOWER_PETAL_2);
        g2.fillRect(cx - r + p, cy - r + p, p*2, p*2);
        g2.fillRect(cx + r - p*3, cy - r + p, p*2, p*2);
        g2.fillRect(cx - r + p, cy + r - p*3, p*2, p*2);
        g2.fillRect(cx + r - p*3, cy + r - p*3, p*2, p*2);
        g2.setColor(C_FLOWER_CORE);
        g2.fillRect(cx - p*2, cy - p*2, p*4, p*4);
        g2.setColor(C_FLOWER_CENTER);
        g2.fillRect(cx - p, cy - p, p*2, p*2);
        g2.setColor(Color.WHITE);
        g2.fillRect(cx - p, cy - p, p, p);
    }

    // ==================== STYLE 2: PIXEL GRASS ====================
    private void drawPixelGrass(Graphics2D g2, int x, int y, int t, int p) {
        g2.setColor(C_PATH_FILL);
        g2.fillRect(x, y, t, t);
        g2.setColor(C_PATH_GRID);
        g2.fillRect(x + 2*p, y + 3*p, p, p);
        g2.fillRect(x + 3*p, y + 4*p, p, p);
        g2.fillRect(x + 10*p, y + 11*p, p, p);
        g2.setColor(C_PATH_DOT);
        g2.fillRect(x + 12*p, y + 3*p, p, p);
        g2.fillRect(x + 4*p, y + 12*p, p, p);
    }

    private void drawPixelStone(Graphics2D g2, int x, int y, int t, int p, int col, int row) {
        g2.setColor(C_HARD_FILL);
        g2.fillRect(x, y, t, t);
        g2.setColor(C_HARD_HI);
        g2.fillRect(x, y, t, p * 2);
        g2.fillRect(x, y, p * 2, t);
        g2.setColor(C_HARD_SH);
        g2.fillRect(x, y + t - p * 2, t, p * 2);
        g2.fillRect(x + t - p * 2, y, p * 2, t);
        g2.setColor(C_HARD_DETAIL);
        g2.fillRect(x + p * 4, y + p * 4, t - p * 8, t - p * 8);

        if (col == 0 || col == gp.getMaxWorldCol() - 1 || row == 0 || row == gp.getMaxWorldRow() - 1) {
            g2.setColor(C_BORDER_GOLD);
            g2.fillRect(x + p * 2, y + p * 2, t - p * 4, p);
            g2.fillRect(x + p * 2, y + p * 2, p, t - p * 4);
        }
    }

    private void drawPixelBrick(Graphics2D g2, int x, int y, int t, int p) {
        g2.setColor(C_BRK_SH);
        g2.fillRect(x, y, t, t);
        g2.setColor(C_BRK_FILL);
        g2.fillRect(x + p, y + p, t - p*2, t/2 - p*2);
        g2.setColor(C_BRK_HI);
        g2.fillRect(x + p, y + p, t - p*2, p);
        g2.setColor(C_BRK_FILL);
        g2.fillRect(x + p, y + t/2, t/2 - p*2, t/2 - p);
        g2.fillRect(x + t/2, y + t/2, t/2 - p, t/2 - p);
        g2.setColor(C_BRK_HI);
        g2.fillRect(x + p, y + t/2, t/2 - p*2, p);
        g2.fillRect(x + t/2, y + t/2, t/2 - p, p);
    }

    private void drawPixelFlower(Graphics2D g2, int x, int y, int t, int p) {
        g2.setColor(C_PATH_FILL);
        g2.fillRect(x, y, t, t);
        int cx = x + t / 2;
        int cy = y + t / 2;
        int r = p * 3;

        g2.setColor(new Color(60, 140, 50));
        g2.fillRect(cx - r - p, cy - r + p, r * 2 + p * 2, r * 2 + p * 2);
        g2.setColor(Color.decode("#FFD3FA"));
        g2.fillRect(cx - p*2, cy - r - p*2, p*4, p*3);
        g2.fillRect(cx - p*2, cy + r - p, p*4, p*3);
        g2.fillRect(cx - r - p*2, cy - p*2, p*3, p*4);
        g2.fillRect(cx + r - p, cy - p*2, p*3, p*4);
        g2.setColor(Color.decode("#FBEDFF"));
        g2.fillRect(cx - r, cy - r, p*2, p*2);
        g2.fillRect(cx + r - p*2, cy - r, p*2, p*2);
        g2.fillRect(cx - r, cy + r - p*2, p*2, p*2);
        g2.fillRect(cx + r - p*2, cy + r - p*2, p*2, p*2);
        g2.setColor(new Color(255, 160, 40));
        g2.fillRect(cx - p*2, cy - p*2, p*4, p*4);
        g2.setColor(new Color(255, 220, 60));
        g2.fillRect(cx - p, cy - p, p*2, p*2);
        g2.setColor(Color.WHITE);
        g2.fillRect(cx - p, cy - p, p, p);
    }
}