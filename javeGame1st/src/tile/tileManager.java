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

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            if (worldX +gp.tileSize > gp.player.worldX - gp.player.screenX &&
                    worldX -gp.tileSize < gp.player.worldX + gp.player.screenX  &&
                    worldY +gp.tileSize> gp.player.worldY - gp.player.screenY &&
                    worldY-gp.tileSize < gp.player.worldY + gp.player.screenY)
            {
                g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);

            }

            worldCol++;

            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;

            }
        }
    }
}