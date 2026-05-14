package tile;

import Game_2D.GamePanel;
import java.awt.geom.AffineTransform;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class TileManager {

    // ── Tile ID constants ──────────────────────────────────────────────
    public static final int ID_PATH      = 0;
    public static final int ID_HARD_WALL = 2;
    public static final int ID_BRICK     = 3;

    // ── MÀU SẮC MỚI - HÀI HÒA HƠN ────────────────────────────────────
    // Nền đường (màu cỏ nhẹ - xanh lá pastel)
    private static final Color C_PATH_FILL  = new Color(168, 204, 120);  // Xanh lá pastel
    private static final Color C_PATH_GRID  = new Color(148, 184, 100);  // Xanh đậm hơn 1 chút

    // Gạch viền (tường đá cổ điển)
    private static final Color C_HARD_FILL  = new Color(88, 88, 98);      // Xám đá
    private static final Color C_HARD_HI    = new Color(128, 128, 138);   // Sáng
    private static final Color C_HARD_SH    = new Color(48, 48, 58);      // Tối
    private static final Color C_HARD_DETAIL = new Color(108, 108, 118);  // Chi tiết

    // Màu cho viền trang trí (sẽ dùng thêm)
    private static final Color C_BORDER_GOLD = new Color(218, 165, 32);    // Vàng đồng
    private static final Color C_BORDER_SHADOW = new Color(50, 50, 60);

    // ── State ────────────────────────────────────────────────────────────────
    private final GamePanel gp;
    public Tile[] tile;
    public int[][] mapTileNum;

    // ── Construction ─────────────────────────────────────────────────────────
    public TileManager(GamePanel gp) {
        this.gp         = gp;
        this.tile       = buildCatalog();
        this.mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        generateMap(new Random(0xDEADBEEFL));
    }

    private Tile[] buildCatalog() {
        Tile[] t = new Tile[4];
        t[ID_PATH]      = new Tile(Tile.TileType.PATH,       C_PATH_FILL,  false, false);
        t[1]            = t[ID_PATH];
        t[ID_HARD_WALL] = new Tile(Tile.TileType.HARD_WALL,  C_HARD_FILL,  true,  false);
        t[ID_BRICK]     = new Tile(Tile.TileType.BRICK_WALL, C_HARD_FILL, true,  true);
        return t;
    }

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
                    mapTileNum[col][row] = (rng.nextFloat() < 0.45f) ? ID_BRICK : ID_PATH;
                }
            }
        }
    }

    private boolean isBorderOrPillar(int col, int row, int cols, int rows) {
        return col == 0 || col == cols - 1 || row == 0 || row == rows - 1
                || (row % 2 == 0 && col % 2 == 0);
    }

    private boolean isSpawnProtected(int col, int row, int cols, int rows) {
        // Bảo vệ 4 góc cho 4 người chơi
        boolean topLeft     = (col <= 2 && row <= 2);
        boolean topRight    = (col >= cols - 3 && row <= 2);
        boolean bottomLeft  = (col <= 2 && row >= rows - 3);
        boolean bottomRight = (col >= cols - 3 && row >= rows - 3);
        return topLeft || topRight || bottomLeft || bottomRight;
    }

    // ── Queries ────────────────────────────────────────────────────────────────
    public boolean isSolid(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.maxWorldCol || row >= gp.maxWorldRow) return true;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return true;
        return tile[id].collision;
    }

    public boolean isBrick(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.maxWorldCol || row >= gp.maxWorldRow) return false;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return false;
        return tile[id].destructible;
    }

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
                // Nền cỏ xanh
                g2.setColor(C_PATH_FILL);
                g2.fillRect(x, y, t, t);
                // Hoa văn cỏ nhỏ (chấm bi)
                g2.setColor(C_PATH_GRID);
                g2.drawRect(x, y, t, t);
                for (int i = 0; i < 3; i++) {
                    g2.fillOval(x + t/4 + i*15, y + t/2, 2, 2);
                }
                break;

            case HARD_WALL:
                // Tường đá xám trang trí
                g2.setColor(C_HARD_FILL);
                g2.fillRect(x, y, t, t);

                // Hiệu ứng đá (vân đá)
                g2.setColor(C_HARD_HI);
                g2.drawLine(x + 2, y + 2, x + t - 3, y + 2);
                g2.drawLine(x + 2, y + 2, x + 2, y + t - 3);
                g2.setColor(C_HARD_SH);
                g2.drawLine(x + t - 3, y + 2, x + t - 3, y + t - 3);
                g2.drawLine(x + 2, y + t - 3, x + t - 3, y + t - 3);

                // Gạch viền trang trí (đường kẻ vàng)
                if (col == 0 || col == gp.maxWorldCol - 1 || row == 0 || row == gp.maxWorldRow - 1) {
                    g2.setColor(C_BORDER_GOLD);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(x + 3, y + 3, t - 7, t - 7);
                    g2.setColor(C_BORDER_SHADOW);
                    g2.drawRect(x + 4, y + 4, t - 9, t - 9);
                }

                // Hoa văn viên gạch
                g2.setColor(C_HARD_DETAIL);
                g2.fillOval(x + t/2 - 3, y + t/2 - 3, 6, 6);
                break;

            case BRICK_WALL:
                // Vẽ bông hoa
                drawFlower(g2, x, y, t);
                break;
        }
    }

    /**
     * Vẽ bông hoa với màu sắc tươi sáng hơn
     */
    private void drawFlower(Graphics2D g2, int x, int y, int tileSize) {
        // Nền là cỏ xanh
        g2.setColor(C_PATH_FILL);
        g2.fillRect(x, y, tileSize, tileSize);

        // Thêm cỏ nhỏ
        g2.setColor(C_PATH_GRID);
        for (int i = 0; i < 5; i++) {
            g2.fillOval(x + 5 + i * 12, y + tileSize - 8, 2, 3);
        }

        int centerX = x + tileSize / 2;
        int centerY = y + tileSize / 2;
        int flowerSize = tileSize * 2 / 3;

        // Bóng đổ nhẹ
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(centerX - flowerSize / 2 + 3, centerY - flowerSize / 2 + 4, flowerSize, flowerSize);

        // 5 cánh hoa - màu hồng đào
        for (int i = 0; i < 5; i++) {
            double angle = i * Math.PI * 2 / 5;
            int px = centerX + (int) (Math.cos(angle) * flowerSize * 0.45);
            int py = centerY + (int) (Math.sin(angle) * flowerSize * 0.45);

            java.awt.geom.AffineTransform old = g2.getTransform();
            g2.rotate(angle, px, py);

            // Màu cánh hoa gradient từ đậm sang nhạt
            if (i % 2 == 0) {
                g2.setColor(Color.decode("#FBEDFF")); // Hồng đào
            } else {
                g2.setColor(Color.decode("#FFD3FA")); // Hồng đậm
            }

            g2.fillOval(px - flowerSize / 4, py - flowerSize / 5,
                    flowerSize / 2, (int) (flowerSize / 2.5));
            g2.setColor(new Color(255, 200, 220, 150));
            g2.drawOval(px - flowerSize / 4, py - flowerSize / 5,
                    flowerSize / 2, (int) (flowerSize / 2.5));

            g2.setTransform(old);
        }

        // Nhụy hoa màu vàng cam
        g2.setColor(new Color(255, 210, 70));
        g2.fillOval(centerX - flowerSize / 4, centerY - flowerSize / 4, flowerSize / 2, flowerSize / 2);
        g2.setColor(new Color(255, 160, 50));
        g2.fillOval(centerX - flowerSize / 6, centerY - flowerSize / 6, flowerSize / 3, flowerSize / 3);

        // Hạt nhụy
        g2.setColor(new Color(220, 100, 30));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            int dotX = centerX + (int) (Math.cos(angle) * flowerSize / 5);
            int dotY = centerY + (int) (Math.sin(angle) * flowerSize / 5);
            g2.fillOval(dotX - 1, dotY - 1, 2, 2);
        }

        // Hiệu ứng lung linh
        g2.setColor(new Color(255, 255, 200, 200));
        g2.fillOval(centerX - 2, centerY - 2, 4, 4);

        // Lá xanh
        g2.setColor(new Color(100, 180, 80));
        g2.fillOval(centerX - flowerSize / 2 - 3, centerY + flowerSize / 5, flowerSize / 4, flowerSize / 5);
        g2.fillOval(centerX + flowerSize / 3, centerY + flowerSize / 5, flowerSize / 4, flowerSize / 5);

    }
}