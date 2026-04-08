package Game_2D;

import entity.player;
import tile.tileManager;

import javax.swing.*;
import java.awt.*;

public class gamePanel extends JPanel implements Runnable {
    final int orgsSize = 16;
    final int scale = 2;
    public final int tileSize = orgsSize * scale *2; // = 64
//set size màn hình mặt định
    public int maxColumn = 16;
    public int maxRow = 12;

    public final int width = tileSize * maxColumn;   // 64 * 16 = 1024
    public final int height = tileSize * maxRow;

    public final int maxWorldCol=30;
    public final int maxWorldRow=30;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize* maxWorldRow ;


    int FPS = 60;

    tileManager tileM = new tileManager(this);

    keyHander keyH = new keyHander() ;

    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public  player player = new player(this, keyH);

    public gamePanel (){
        this.setPreferredSize(new Dimension(width,height));
        this.setBackground(Color.white);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

    }
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();

    }

    public void run(){

        double drawInterval = 1000000000/FPS ;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while (gameThread != null){
//            System.out.println("game running");
            update();
            repaint();

            try {
                double remaniningTime = nextDrawTime - System.nanoTime();

                if (remaniningTime < 0) {
                    remaniningTime = 0;
                }
                Thread.sleep((long) remaniningTime / 1000000);
                nextDrawTime+=drawInterval;
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            }

        }

    public void update(){
        player.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        tileM.draw(g2);
        player.draw(g2);
        g2.dispose();

    }

}
