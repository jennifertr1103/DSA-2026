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

    // ── Map Types ──────────────────────────────────────────────────────────
    public enum MapType { CLASSIC, IMAGE }
    private MapType currentMapType = MapType.CLASSIC;
    private java.awt.image.BufferedImage fullMapImage;

    // ── DYNAMIC DIMENSIONS ───────────────────────────────────────────
    public int getTileSize() { return (currentMapType == MapType.IMAGE) ? 48 : 64; }
    public int getMaxCol()   { return (currentMapType == MapType.IMAGE) ? 21 : 16; }
    public int getMaxRow()   { return (currentMapType == MapType.IMAGE) ? 16 : 12; }

    // ── State ────────────────────────────────────────────────────────────────
    private final GamePanel gp;
    public Tile[] tile;
    public int[][] mapTileNum; // Sẽ được cấp phát lại khi đổi map

    // ── Construction ─────────────────────────────────────────────────────────
    public TileManager(GamePanel gp) {
        this.gp   = gp;
        this.tile = buildCatalog();
        
        loadMapAssets();
        setMap(MapType.CLASSIC); // Mặc định là map classic
    }

    private void loadMapAssets() {
        try {
            // Load ảnh full map mới (map2.png)
            fullMapImage = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/map/map2.png"));
        } catch (Exception e) {
            System.err.println("Could not load full map image: " + e.getMessage());
        }
    }

    public void setMap(MapType type) {
        this.currentMapType = type;
        
        // Cấp phát lại mảng map với kích thước mới
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
            // Map 2 (IMAGE): Tự động tạo lưới 48x48 (21x16 ô)
            Random rng = new Random();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) {
                        mapTileNum[c][r] = ID_HARD_WALL;
                    } else if (r % 2 == 0 && c % 2 == 0) {
                        mapTileNum[c][r] = ID_HARD_WALL;
                    } else {
                        // Bảo vệ spawn points
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
        
        // Luôn bảo vệ spawn points chính xác sau khi gán
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
        t[ID_PATH]      = new Tile(Tile.TileType.PATH,       C_PATH_FILL,  false, false);
        t[1]            = t[ID_PATH];
        t[ID_HARD_WALL] = new Tile(Tile.TileType.HARD_WALL,  C_HARD_FILL,  true,  false);
        t[ID_BRICK]     = new Tile(Tile.TileType.BRICK_WALL, C_HARD_FILL, true,  true);
        return t;
    }

    private boolean isBorderOrPillar(int col, int row, int cols, int rows) {
        return col == 0 || col == cols - 1 || row == 0 || row == rows - 1
                || (row % 2 == 0 && col % 2 == 0);
    }

    private boolean isSpawnProtected(int col, int row, int cols, int rows) {
        // Bảo vệ 4 góc cho 4 người (có thể chuyển thành mode 2 người chơi trong tương lai)
        boolean topLeft     = (col <= 2 && row <= 2);
        boolean topRight    = (col >= cols - 3 && row <= 2);
        boolean bottomLeft  = (col <= 2 && row >= rows - 3);
        boolean bottomRight = (col >= cols - 3 && row >= rows - 3);
        return topLeft || topRight || bottomLeft || bottomRight;
    }

    // ── Queries ────────────────────────────────────────────────────────────────
    public boolean isSolid(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.getMaxWorldCol() || row >= gp.getMaxWorldRow()) return true;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return true;
        return tile[id].collision;
    }

    public boolean isBrick(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.getMaxWorldCol() || row >= gp.getMaxWorldRow()) return false;
        int id = mapTileNum[col][row];
        if (id < 0 || id >= tile.length || tile[id] == null) return false;
        return tile[id].destructible;
    }

    public void destroyBrick(int col, int row) {
        if (isBrick(col, row)) mapTileNum[col][row] = ID_PATH;
    }

    // ── Rendering ────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        if (currentMapType == MapType.IMAGE && fullMapImage != null) {
            // Vẽ ảnh full map làm nền
            g2.drawImage(fullMapImage, 0, 0, gp.getWorldWidth(), gp.getWorldHeight(), null);
            
            // Vẽ vật cản đè lên ảnh nền
            int t = gp.tileSize;
            for (int row = 0; row < gp.getMaxWorldRow(); row++) {
                for (int col = 0; col < gp.getMaxWorldCol(); col++) {
                    int id = mapTileNum[col][row];
                    // Vẽ cả tường cứng (HARD_WALL) và gạch phá được (BRICK)
                    if (id == ID_HARD_WALL || id == ID_BRICK) {
                        drawTile(g2, col, row, col * t, row * t, t);
                    }
                }
            }
        } else {
            // Vẽ map classic (từng ô)
            int t = gp.tileSize;
            for (int row = 0; row < gp.getMaxWorldRow(); row++) {
                for (int col = 0; col < gp.getMaxWorldCol(); col++) {
                    drawTile(g2, col, row, col * t, row * t, t);
                }
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
                if (col == 0 || col == gp.getMaxWorldCol() - 1 || row == 0 || row == gp.getMaxWorldRow() - 1) {
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
                if (currentMapType == MapType.IMAGE) {
                    // Vẽ gạch kiểu tường đá thay vì hoa khi dùng Image Map
                    g2.setColor(new Color(120, 100, 80)); // Màu gạch nâu
                    g2.fillRect(x + 2, y + 2, t - 4, t - 4);
                    g2.setColor(new Color(150, 130, 110));
                    g2.drawRect(x + 2, y + 2, t - 4, t - 4);
                    // Chi tiết viên gạch
                    g2.drawLine(x + 2, y + t/2, x + t - 3, y + t/2);
                } else {
                    // Vẽ bông hoa (giữ nguyên cho map classic nếu muốn)
                    drawFlower(g2, x, y, t);
                }
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