package Game_2D;

import bomb.BombAlgorithm;
import entity.Bot;
import entity.Player;
import entity.ItemSpawner;  // THÊM IMPORT NÀY
import tile.TileManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Main game panel — owns all subsystems, drives the update/render loop.
 */
public class GamePanel extends JPanel implements Runnable {

    // ── Tile / window sizing ────────────────────────────────────────────────
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE              = 2;
    public static final int EXTRA_SCALE        = 2;
    public final int tileSize = ORIGINAL_TILE_SIZE * SCALE * EXTRA_SCALE; // 64 px

    public final int maxColumn    = 16;
    public final int maxRow       = 12;
    public final int width        = tileSize * maxColumn;   // 1024
    public final int height       = tileSize * maxRow;      //  768

    public final int maxWorldCol  = 16;
    public final int maxWorldRow  = 12;
    public final int worldWidth   = tileSize * maxWorldCol;
    public final int worldHeight  = tileSize * maxWorldRow;

    private static final int FPS        = 60;
    private static final int HUD_HEIGHT = 70; // Tăng lên 70 để chứa speed boost bar

    // ── Subsystems ──────────────────────────────────────────────────────────
    public final TileManager    tileM;
    public final KeyHandler     keyH;
    public final CollisionChecker cChecker;
    public Player player;
    public Bot    bot;
    public BombAlgorithm bombAlgo;
    public ItemSpawner itemSpawner;  // THÊM DÒNG NÀY

    // ── Game state ──────────────────────────────────────────────────────────
    private boolean gameOver    = false;
    private String  winnerLabel = "";

    // ── Thread ──────────────────────────────────────────────────────────────
    private volatile boolean running = false;
    private Thread gameThread;

    // ── Construction ────────────────────────────────────────────────────────

    public GamePanel() {
        this.keyH     = new KeyHandler();
        this.cChecker = new CollisionChecker(this);
        this.tileM    = new TileManager(this);

        resetGame();   // initialises player, bot, bombAlgo, itemSpawner

        setPreferredSize(new Dimension(width, height + HUD_HEIGHT));
        setBackground(new Color(12, 12, 16));
        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);
    }

    /**
     * Creates fresh player / bot / bombAlgo / itemSpawner instances.
     * Called at construction and on every restart.
     */
    private void resetGame() {
        bombAlgo = new BombAlgorithm(this);

        // Spawn positions: player top-left (col 1, row 1);
        //                  bot bottom-right (col 14, row 10).
        player = new Player(this, keyH);
        bot    = new Bot(this, 14, 10);

        // Register AFTER construction so neither reference is null
        bombAlgo.registerDestructible(player);
        bombAlgo.registerDestructible(bot);

        // THÊM DÒNG NÀY - Khởi tạo item spawner
        itemSpawner = new ItemSpawner(this);

        gameOver    = false;
        winnerLabel = "";
    }

    // ── Thread management ────────────────────────────────────────────────────

    public void startGameThread() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
    }

    public void stopGameThread() {
        running = false;
    }

    // ── Game loop (fixed time-step, 60 fps) ──────────────────────────────────

    @Override
    public void run() {
        final double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (running) {
            update();
            repaint();

            try {
                double remainingNs = nextDrawTime - System.nanoTime();
                long   sleepMs     = (long) (remainingNs / 1_000_000.0);
                if (sleepMs > 0) Thread.sleep(sleepMs);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ── Update ───────────────────────────────────────────────────────────────

    public void update() {
        // Restart check (processed even when game is over)
        if (keyH.restartPressed) {
            keyH.consumeRestartKey();
            resetGame();
            return;
        }

        if (gameOver) return;

        player.update();
        bot.update();
        bombAlgo.update();

        // THÊM CÁC DÒNG NÀY - Update item system
        itemSpawner.update();
        itemSpawner.checkPickup(player); // Player nhặt item

        checkGameOver();
    }

    private void checkGameOver() {
        boolean playerDead = !player.alive;
        boolean botDead    = !bot.alive;

        if (playerDead && botDead)  { gameOver = true; winnerLabel = "DRAW";   }
        else if (playerDead)        { gameOver = true; winnerLabel = "BOT";    }
        else if (botDead)           { gameOver = true; winnerLabel = "PLAYER"; }
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);

        tileM.draw(g2);
        bombAlgo.draw(g2);
        player.draw(g2);
        bot.draw(g2);

        // THÊM DÒNG NÀY - Vẽ item
        if (itemSpawner != null) {
            itemSpawner.draw(g2);
        }

        drawHUD(g2);

        if (gameOver) drawGameOver(g2);
    }

    // ── HUD ──────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        int hudY = height;

        // Background bar
        g2.setColor(new Color(32, 36, 42));
        g2.fillRect(0, hudY, width, HUD_HEIGHT);
        g2.setColor(new Color(80, 90, 110));
        g2.drawLine(0, hudY, width, hudY);

        Font labelFont = new Font("SansSerif", Font.BOLD, 13);
        g2.setFont(labelFont);

        // ── Player lives ────────────────────────────────────────────────────
        g2.setColor(new Color(255, 140, 170)); // Màu hồng cho player
        g2.drawString("MY MELODY", 12, hudY + 18);
        for (int i = 0; i < 3; i++) {
            g2.setColor(i < player.life ? new Color(255, 100, 130) : new Color(60, 50, 55));
            g2.fillOval(12 + i * 19, hudY + 26, 14, 14);
            g2.setColor(new Color(0, 0, 0, 60));
            g2.drawOval(12 + i * 19, hudY + 26, 14, 14);
        }

        // ── Speed Boost Indicator (THÊM PHẦN NÀY) ─────────────────────────────
        if (player.getSpeedBoostTimer() > 0) {
            // Khung nền
            g2.setColor(new Color(100, 200, 255, 50));
            g2.fillRoundRect(12, hudY + 44, 80, 14, 7, 7);

            // Thanh thời gian
            float percent = player.getSpeedBoostTimer() / (10f * 60f);
            g2.setColor(new Color(100, 200, 255));
            g2.fillRoundRect(12, hudY + 44, (int)(80 * percent), 14, 7, 7);

            // Chữ
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            int boostAmount = player.getCurrentSpeed() - player.getOriginalSpeed();
            g2.drawString("SPEED +" + boostAmount, 16, hudY + 55);
        }

        // ── Bot lives ───────────────────────────────────────────────────────
        g2.setColor(new Color(180, 70, 100)); // Màu hồng đậm/đỏ cho Kuromi
        FontMetrics fm = g2.getFontMetrics();
        String botLabel = "KUROMI";
        int bx = width - fm.stringWidth(botLabel) - 14;
        g2.drawString(botLabel, bx, hudY + 18);
        for (int i = 0; i < 3; i++) {
            g2.setColor(i < bot.life ? new Color(200, 70, 100) : new Color(60, 50, 55));
            g2.fillOval(width - 70 + i * 19, hudY + 26, 14, 14);
            g2.setColor(new Color(0, 0, 0, 60));
            g2.drawOval(width - 70 + i * 19, hudY + 26, 14, 14);
        }

        // ── Controls hint ───────────────────────────────────────────────────
        g2.setColor(new Color(150, 150, 165));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String hint = "WASD / Arrows: Move    Space: Bomb    R: Restart";
        fm = g2.getFontMetrics();
        g2.drawString(hint, (width - fm.stringWidth(hint)) / 2, hudY + HUD_HEIGHT - 6);
    }

    // ── Game-over overlay ─────────────────────────────────────────────────────

    private void drawGameOver(Graphics2D g2) {
        // Dim the arena
        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRect(0, 0, width, height);

        // Winner text
        String line = winnerLabel.equals("DRAW") ? "DRAW!" : winnerLabel + " WINS!";

        g2.setFont(new Font("SansSerif", Font.BOLD, 56));
        FontMetrics fm = g2.getFontMetrics();

        // Shadow
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(line, (width - fm.stringWidth(line)) / 2 + 3, height / 2 + 3);

        // Coloured text
        Color textCol = winnerLabel.equals("PLAYER") ? new Color(255, 140, 170)  // Hồng cho My Melody
                : winnerLabel.equals("BOT")    ? new Color(180, 70, 100)   // Đỏ hồng cho Kuromi
                :                                new Color(230, 205, 60);
        g2.setColor(textCol);
        g2.drawString(line, (width - fm.stringWidth(line)) / 2, height / 2);

        // Sub-line
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        fm = g2.getFontMetrics();
        String sub = "Press  R  to play again";
        g2.setColor(new Color(210, 210, 210));
        g2.drawString(sub, (width - fm.stringWidth(sub)) / 2, height / 2 + 52);
    }
}