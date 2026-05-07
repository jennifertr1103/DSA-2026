package Game_2D;

import entity.entity;
import tile_interactive.IT_BreakableWall;

public class collisionChecker {
    gamePanel gp;
    public collisionChecker(gamePanel gp){
        this.gp = gp;
    }
    public void checkTile(entity entity){
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX/gp.tileSize;
        int entityRightCol = entityRightWorldX/gp.tileSize;
        int entityTopRow = entityTopWorldY/gp.tileSize;
        int entityBottomRow = entityBottomWorldY/gp.tileSize;

        int tileNum1, tileNum2;

        switch (entity.direction) {
            case "up":
                entityTopRow= (entityTopWorldY-entity.speed)/gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "down":
                entityBottomRow= (entityBottomWorldY+entity.speed)/gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "left":
                entityLeftCol= (entityLeftWorldX-entity.speed)/gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "right":
                entityRightCol= (entityRightWorldX+entity.speed)/gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
        }
    }

    // Trong CollisionChecker.java
    public void checkBombExplosion(int col, int row) {
        for (int i = 0; i < gp.iTile.length; i++) {
            if (gp.iTile[i] != null && gp.iTile[i] instanceof IT_BreakableWall) {

                int tileCol = gp.iTile[i].worldX / gp.tileSize;
                int tileRow = gp.iTile[i].worldY / gp.tileSize;

                if (tileCol == col && tileRow == row) {
                    // Gọi phương thức chuẩn của Destructible
                    ((IT_BreakableWall) gp.iTile[i]).onDestroyedByFlame();

                    // Lưu ý: Việc xóa gp.iTile[i] = null nên để lớp IT_BreakableWall
                    // tự xử lý trong update() sau khi chạy xong hiệu ứng vỡ.
                }
            }
        }
    }

}
