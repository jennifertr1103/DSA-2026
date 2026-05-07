package tile_interactive;

import Game_2D.gamePanel;
import entity.Destructible;
import javax.imageio.ImageIO;
import java.io.IOException;

public class IT_BreakableWall extends InteractiveTile implements Destructible {

    // THÊM BIẾN NÀY ĐỂ HẾT LỖI "Cannot resolve symbol health"
    public int health = 1;

    private boolean dying = false;
    private int dyingCounter = 0;
    private boolean alive = true;

    public IT_BreakableWall(gamePanel gp, int col, int row) {
        super(gp, col, row);
        try {
            // Đảm bảo đường dẫn ảnh chính xác
            this.down1 = ImageIO.read(getClass().getResourceAsStream("/tile/wood.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (dying) {
            dyingCounter++;
            // Hiệu ứng nhấp nháy khi đang vỡ (tùy chọn)
            if (dyingCounter % 5 == 0) {
                // Bạn có thể thêm logic thay đổi độ trong suốt ở đây
            }

            if (dyingCounter > 20) {
                alive = false;
            }
        }
    }

    @Override
    public void onDestroyedByFlame() {
        // Giảm máu khi trúng bom
        health--;

        if(health <= 0) {
            dying = true; // Bắt đầu đếm ngược để biến mất
        }
    }

    @Override
    public boolean isDestroyed() {
        // Trả về true để BombAlgorithm xóa khỏi danh sách quản lý bom
        return !alive;
    }

    // Các hàm bắt buộc từ Destructible để định vị gạch trên lưới (grid)
    @Override
    public int getCol() {
        return worldX / gp.tileSize;
    }

    @Override
    public int getRow() {
        return worldY / gp.tileSize;
    }
}