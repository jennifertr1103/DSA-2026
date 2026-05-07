package Game_2D;

import bomb.Bomb;
import entity.player;
import tile.tileManager;
import tile_interactive.IT_BreakableWall;
import tile_interactive.InteractiveTile;
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

    public final int gameWidth = tileSize * 16;
    public final int gameHeight = tileSize * 12;

    public final int sidebarWidth = tileSize * 4;


    public final int padding = 24;

    public final int screenWidth = gameWidth + sidebarWidth + (padding * 3);
    public final int screenHeight = gameHeight + (padding * 2);

    public java.util.ArrayList<Bomb> bombList = new java.util.ArrayList<>();


    int FPS = 60;
    //number of wall
    public InteractiveTile iTile[] = new InteractiveTile[50];

    public tileManager tileM = new tileManager(this);

    keyHander keyH = new keyHander() ;

    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public  player player = new player(this, keyH);

    public gamePanel (){
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(new Color(34, 47, 62));
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
        for (int i = 0; i < bombList.size(); i++) {
            Bomb b = bombList.get(i);
            b.update();

            if (b.isReadyToExplode()) {
                // Tạo lửa và thêm vào danh sách hiển thị
                // flameList.addAll(b.explode(flameAppearance, 30));
                bombList.remove(i);
                i--;
            }
        }
        for (int i = 0; i < iTile.length; i++) {
            if (iTile[i] != null) {
                iTile[i].update();

                // Nếu tường đã hoàn thành chu kỳ "chết", giải phóng mảng
                if (iTile[i] instanceof IT_BreakableWall && ((IT_BreakableWall)iTile[i]).isDestroyed()) {
                    iTile[i] = null;
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;



        // --- VẼ KHUNG SIDEBAR (Bên trái hoặc phải tùy bạn) ---
        drawUI(g2);

        // --- VẼ VÙNG GAME ---
        // Dịch chuyển "bút vẽ" đến vị trí sau khi đã tính Padding và Sidebar
        int gameStartX = padding + sidebarWidth + padding;
        int gameStartY = padding;

        g2.translate(gameStartX, gameStartY); // Mọi thứ vẽ sau lệnh này sẽ bắt đầu từ (gameStartX, gameStartY)

        // Vẽ nền đen hoặc khung cho vùng game
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gameWidth, gameHeight);



        for(int i = 0; i < iTile.length; i++) {
            if(iTile[i] != null) {
                iTile[i].draw(g2);
            }
        }

        // Vẽ các đối tượng game
        tileM.draw(g2);
        player.draw(g2);

        for (Bomb b : bombList) {
            b.draw(g2);
        }

        g2.translate(-gameStartX, -gameStartY); // Trả lại tọa độ cũ để vẽ các thứ khác nếu cần

        g2.dispose();
    }

    public void drawUI(Graphics2D g2) {
        // Vẽ nền cho Sidebar
        g2.setColor(new Color(200, 200, 200, 200)); // Màu xám nhạt có độ trong suốt
        g2.fillRoundRect(padding, padding, sidebarWidth, gameHeight, 15, 15);

        // Vẽ viền cho Sidebar
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(padding, padding, sidebarWidth, gameHeight, 15, 15);

        // Ghi thông tin
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("PLAYER INFO", padding + 20, padding + 50);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Speed: " + player.speed, padding + 20, padding + 100);
        // Bạn có thể vẽ thêm avatar nhân vật ở đây
    }

}
