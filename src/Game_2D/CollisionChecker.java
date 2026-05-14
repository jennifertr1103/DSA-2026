package Game_2D;

import entity.Direction;
import entity.Entity;
import java.awt.*;

public class CollisionChecker {

    private final GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {
        // 1. Tọa độ hiện tại của Hitbox (World-space)
        int leftX = entity.worldX + entity.solidArea.x;
        int rightX = leftX + entity.solidArea.width;
        int topY = entity.worldY + entity.solidArea.y;
        int bottomY = topY + entity.solidArea.height;

        // 2. Dự đoán vị trí TIẾP THEO dựa trên tốc độ
        Direction dir = entity.direction;
        int nextLeftX = leftX + dir.dx * entity.speed;
        int nextRightX = rightX + dir.dx * entity.speed;
        int nextTopY = topY + dir.dy * entity.speed;
        int nextBottomY = bottomY + dir.dy * entity.speed;

        // --- BƯỚC MỚI: KIỂM TRA BIÊN BẢN ĐỒ (WORLD BOUNDARY CHECK) ---
        // Chặn không cho các cạnh của Hitbox vượt quá giới hạn pixel của Map
        if (nextLeftX < 0 ||
                nextRightX > gp.maxWorldCol * gp.tileSize ||
                nextTopY < 0 ||
                nextBottomY > gp.maxWorldRow * gp.tileSize) {

            entity.collisionOn = true;
            return; // Dừng lại luôn, không cần check Tile bên dưới nữa
        }

        // 3. Chuyển đổi tọa độ pixel dự đoán sang tọa độ ô lưới (Tile grid)
        int tileSize = gp.tileSize;
        int probeColA, probeRowA, probeColB, probeRowB;

        switch (dir) {
            case UP:
                probeColA = nextLeftX / tileSize;
                probeRowA = nextTopY / tileSize;
                probeColB = nextRightX / tileSize;
                probeRowB = nextTopY / tileSize;
                break;
            case DOWN:
                probeColA = nextLeftX / tileSize;
                probeRowA = nextBottomY / tileSize;
                probeColB = nextRightX / tileSize;
                probeRowB = nextBottomY / tileSize;
                break;
            case LEFT:
                probeColA = nextLeftX / tileSize;
                probeRowA = nextTopY / tileSize;
                probeColB = nextLeftX / tileSize;
                probeRowB = nextBottomY / tileSize;
                break;
            case RIGHT:
                probeColA = nextRightX / tileSize;
                probeRowA = nextTopY / tileSize;
                probeColB = nextRightX / tileSize;
                probeRowB = nextBottomY / tileSize;
                break;
            default:
                return;
        }

        // Kiểm tra xem 2 điểm thăm dò có chạm vào vật thể rắn (Wall/Brick) không
        if (gp.tileM.isSolid(probeColA, probeRowA) || gp.tileM.isSolid(probeColB, probeRowB)) {
            entity.collisionOn = true;
        }
    }

    public void checkDamage(Entity entity, java.util.List<bomb.Flame> flames) {
        // Tối ưu: Dùng Rectangle tạm thời để kiểm tra va chạm lửa
        Rectangle entityHitbox = new Rectangle(
                entity.worldX + entity.solidArea.x,
                entity.worldY + entity.solidArea.y,
                entity.solidArea.width,
                entity.solidArea.height
        );

        for (bomb.Flame flame : flames) {
            Rectangle flameHitbox = new Rectangle(
                    flame.worldX + flame.solidArea.x,
                    flame.worldY + flame.solidArea.y,
                    flame.solidArea.width,
                    flame.solidArea.height
            );

            if (entityHitbox.intersects(flameHitbox)) {
                entity.takeDamage();
                break;
            }
        }
    }
}