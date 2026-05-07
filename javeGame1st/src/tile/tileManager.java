package tile;

import Game_2D.gamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class tileManager {
    gamePanel gp;
    public tile[] tile;
    public int mapTileNum[][];

    public tileManager(Game_2D.gamePanel gp) {
        this.gp = gp;

        tile = new tile[4];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/map/map.txt");

    }

    public void getTileImage() {
        try {
            tile[0] = new tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tile/grass.png"));

            tile[1] = new tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tile/water.png"));


            tile[2] = new tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/tile/stone.png"));
            tile[2].collision = true ;

            tile[3] = new tile();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/tile/wood.png"));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int row = 0;

            while (row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;

                String number[] = line.trim().split("\\s+");
                for (int col = 0; col < gp.maxWorldCol; col++) {
                    int num = Integer.parseInt(number[col]);
                    mapTileNum[col][row] = num;
                }
                row++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {

        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

            int tileNum = mapTileNum[worldCol][worldRow];

            // Tọa độ thực của Tile trên bản đồ
            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;

        /*
           CAMERA ĐỨNG YÊN:
           Chúng ta vẽ trực tiếp bằng worldX và worldY.
           Nếu bạn muốn camera đứng yên ở góc (0,0), hãy dùng worldX, worldY.
           Nếu muốn đứng yên ở một vị trí cụ thể, hãy trừ đi một hằng số cố định.
        */

            // Chỉ vẽ những Tile nằm trong phạm vi chiều rộng và chiều cao của màn hình Panel
            if (worldX < gp.width && worldY < gp.height) {
                g2.drawImage(tile[tileNum].image, worldX, worldY, gp.tileSize, gp.tileSize, null);
            }

            worldCol++;

            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }

    }

    // Kiểm tra xem ô đó có phải tường cứng/vật cản không
    public boolean isSolid(int col, int row) {
        if(col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) return true;
        int tileNum = mapTileNum[col][row];
        return tile[tileNum].collision;
    }

    // Kiểm tra xem có phải gạch (kiểu cũ) không - Hiện tại cứ để false để dùng InteractiveTile
    public boolean isBrick(int col, int row) {
        return false;
    }

    // Hàm hủy gạch kiểu cũ
    public void destroyBrick(int col, int row) {
        // Để trống vì bạn dùng IT_BreakableWall rồi
    }
}