package tile_interactive;

import Game_2D.gamePanel;
import entity.entity;
import java.awt.Graphics2D;

public abstract class InteractiveTile extends entity {
    protected gamePanel gp;
    public boolean destructible = false;

    public InteractiveTile(gamePanel gp, int col, int row) {
        this.gp = gp;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
    }

    // Hàm update để xử lý logic theo thời gian (nếu cần)
    public abstract void update();

    public void draw(Graphics2D g2) {
        // Vẽ hình ảnh tại vị trí worldX, worldY
        // Lưu ý: Nếu camera đứng yên, vẽ trực tiếp worldX, worldY.
        // Nếu camera di chuyển, phải trừ đi gp.player.worldX như TileManager.
        g2.drawImage(down1, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}