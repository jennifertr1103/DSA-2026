package Game_2D;

import bomb.BombAlgorithm;
import entity.Bot;
import entity.Player;
import entity.ItemSpawner;  // THÊM IMPORT NÀY
import tile.TileManager;
import sound.SoundManager;
import sound.SoundManager.SoundType;

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
    public static final int TILE_SIZE          = ORIGINAL_TILE_SIZE * SCALE * EXTRA_SCALE; // 64 px

    public static final int MAX_COLUMN    = 16;
    public static final int MAX_ROW       = 12;
    public static final int WIDTH         = TILE_SIZE * MAX_COLUMN;   // 1024
    public static final int HEIGHT        = TILE_SIZE * MAX_ROW;      //  768

    public final int tileSize = TILE_SIZE; // Will be shadowed by tileM.getTileSize() in logic
    public final int width = WIDTH;
    public final int height = HEIGHT;

    public int getMaxWorldCol() { return tileM.getMaxCol(); }
    public int getMaxWorldRow() { return tileM.getMaxRow(); }
    public int getWorldWidth()  { return tileM.getMaxCol() * tileM.getTileSize(); }
    public int getWorldHeight() { return tileM.getMaxRow() * tileM.getTileSize(); }

    private static final int FPS        = 60;
    public static final int HUD_HEIGHT = 70; // Tăng lên 70 để chứa speed boost bar

    // ── Subsystems ──────────────────────────────────────────────────────────
    public final TileManager    tileM;
    public final KeyHandler     keyH;
    public final CollisionChecker cChecker;
    public Player player;
    public java.util.List<Bot> bots = new java.util.ArrayList<>();
    public BombAlgorithm bombAlgo;
    public ItemSpawner itemSpawner;

    public boolean multiplayer = false;  // true = 2 players, false = vs bots
    public entity.Player2 player2;              // Player 2 reference
    public Game_2D.KeyHandler2 keyH2;
    public SoundManager soundManager;
    private final Main main;

    // ── Game state ──────────────────────────────────────────────────────────
    public enum GameState { PLAY, GAME_OVER }
    private GameState gameState = GameState.PLAY;
    private String  winnerLabel = "";

    public void setMultiplayer(boolean multiplayer) {
        this.multiplayer = multiplayer;
    }

    public void setGameState(GameState state) {
        this.gameState = state;
    }

    // ── Thread ────────────────────────────────────────────────────
    private volatile boolean running = false;
    private Thread gameThread;

    // ── Construction ────────────────────────────────────────────────────────

    public GamePanel(Main main) {
        this.main = main;
        this.soundManager = main.getSoundManager();
        this.keyH2    = new KeyHandler2();
        this.keyH     = new KeyHandler();
        this.cChecker = new CollisionChecker(this);
        this.tileM    = new TileManager(this);

        resetGame();   // initialises player, bots, bombAlgo, itemSpawner

        setPreferredSize(new Dimension(WIDTH, HEIGHT + HUD_HEIGHT));
        setBackground(new Color(12, 12, 16));
        setDoubleBuffered(true);
        addKeyListener(keyH2);
        addKeyListener(keyH);
        setFocusable(true);
    }

    /**
     * Creates fresh player / bots / bombAlgo / itemSpawner instances.
     * Called at construction and on every restart.
     */
    public void resetGame() {
        bombAlgo = new BombAlgorithm(this);

        if (multiplayer) {
            keyH.setUseArrowKeys(false);
        } else {
            keyH.setUseArrowKeys(true);
        }

        player = new Player(this, keyH);
        bombAlgo.registerDestructible(player);

        if (multiplayer) {
            player2 = new entity.Player2(this, keyH2);
            bombAlgo.registerDestructible(player2);

            bots.clear();

            Bot bot1 = new Bot(this, 1, 10, 1);
            bot1.setStartDelay(0);
            bots.add(bot1);

            Bot bot2 = new Bot(this, 14, 1, 2);
            bot2.setStartDelay(1);
            bots.add(bot2);


            for (Bot b : bots) {
                bombAlgo.registerDestructible(b);
            }
        } else {
            player2 = null;
            bots.clear();

            Bot bot1 = new Bot(this, 14, 1, 1);
            bot1.setStartDelay(0);
            bots.add(bot1);

            Bot bot2 = new Bot(this, 1, 10, 2);
            bot2.setStartDelay(0);
            bots.add(bot2);

            Bot bot3 = new Bot(this, 13, 10, 3);
            bot3.setStartDelay(1);
            bots.add(bot3);

            for (Bot b : bots) {
                bombAlgo.registerDestructible(b);
            }
        }

        itemSpawner = new ItemSpawner(this);
        winnerLabel = "";
        gameState = GameState.PLAY;
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
        try {
            if (gameThread != null) gameThread.join(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        if (keyH.restartPressed) {
            keyH.consumeRestartKey();
            resetGame();
            gameState = GameState.PLAY;
            soundManager.resetWinLoseFlag();
            soundManager.stopMusic();
            soundManager.playMusic(SoundType.BGMUSIC);
            return;
        }

        if (gameState == GameState.GAME_OVER) {
            if (soundManager.isMusicPlaying()) {
                soundManager.stopMusic();  // Tắt hẳn bgmusic
            }
            // Play win/lose sound only once
            if (winnerLabel.equals("PLAYER 1") || winnerLabel.equals("PLAYER 2")) {
                soundManager.playWinOnce();
            } else if (winnerLabel.startsWith("BOT")) {
                soundManager.playLoseOnce();
            } else if (winnerLabel.equals("DRAW")) {
                soundManager.playLoseOnce();
            }

            if (keyH.enterPressed) {
                keyH.consumeEnterKey();
                main.showMenu();
            }
            return;
        }

        // Phần update game bình thường
        player.update();
        if (multiplayer && player2 != null) {
            player2.update();
        }
        for (int i = 0; i < bots.size(); i++) {
            bots.get(i).update();
        }
        bombAlgo.update();
        itemSpawner.update();
        itemSpawner.checkPickup(player);
        if (multiplayer && player2 != null) {
            itemSpawner.checkPickup(player2);
        }
        for (Bot b : bots) {
            itemSpawner.checkPickup(b);
        }
        checkGameOver();
    }

private void checkGameOver() {
    int aliveCount = 0;
    String lastSurvivor = "";

    if (player.alive) {
        aliveCount++;
        lastSurvivor = "PLAYER 1";
    }

    if (multiplayer && player2 != null && player2.alive) {
        aliveCount++;
        lastSurvivor = "PLAYER 2";
    }

    // Bot luôn được tính (cả 2 chế độ)
    for (int i = 0; i < bots.size(); i++) {
        if (bots.get(i).alive) {
            aliveCount++;
            lastSurvivor = "BOT " + (i + 1);
        }
    }

    if (aliveCount <= 1) {
        gameState = GameState.GAME_OVER;
        winnerLabel = aliveCount == 0 ? "DRAW" : lastSurvivor;
    }
}
    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        tileM.draw(g2);
        bombAlgo.draw(g2);
        player.draw(g2);

        // Vẽ player 2 NẾU có (multiplayer mode)
        if (multiplayer && player2 != null) {
            player2.draw(g2);
        }

        // Vẽ TẤT CẢ bot (luôn luôn)
        for (Bot b : bots) {
            b.draw(g2);
        }

        if (itemSpawner != null) {
            itemSpawner.draw(g2);
        }

        drawHUD(g2);

        if (gameState == GameState.GAME_OVER) drawGameOver(g2);
    }

    private void drawEntityStatus(Graphics2D g2, int x, int y, String label, int life, boolean alive, Color color) {
        if (!alive) g2.setColor(new Color(100, 100, 110));
        else g2.setColor(color);

        g2.drawString(label, x, y);
        for (int i = 0; i < 3; i++) {
            if (i < life && alive) g2.setColor(color);
            else g2.setColor(new Color(60, 50, 55));
            g2.fillOval(x + i * 15, y + 5, 10, 10);
        }
    }

    // ── HUD ──────────────────────────────────────────────────────────────────

private void drawHUD(Graphics2D g2) {
    int hudY = HEIGHT;

    g2.setColor(new Color(32, 36, 42));
    g2.fillRect(0, hudY, WIDTH, HUD_HEIGHT);
    g2.setColor(new Color(80, 90, 110));
    g2.drawLine(0, hudY, WIDTH, hudY);

    Font labelFont = new Font("SansSerif", Font.BOLD, 11);
    g2.setFont(labelFont);

    // Player 1 Status
    drawEntityStatus(g2, 20, hudY + 15, "P1", player.life, player.alive, new Color(255, 140, 170));

    if (multiplayer && player2 != null) {
        // Player 2 Status
        drawEntityStatus(g2, 150, hudY + 15, "P2", player2.life, player2.alive, new Color(100, 180, 255));

        // Bots Status (vị trí dịch sang phải)
        for (int i = 0; i < bots.size(); i++) {
            Bot b = bots.get(i);
            int bx = 280 + i * 150;
            drawEntityStatus(g2, bx, hudY + 15, "BOT " + (i + 1), b.life, b.alive, new Color(180, 70, 100));
        }

        String mode = "2P + 2 BOTS";
        g2.setColor(new Color(150, 150, 150));
        g2.drawString(mode, WIDTH - 120, hudY + 25);
    } else {
        // Bots Status
        for (int i = 0; i < bots.size(); i++) {
            Bot b = bots.get(i);
            int bx = 200 + i * 150;
            drawEntityStatus(g2, bx, hudY + 15, "BOT " + (i + 1), b.life, b.alive, new Color(180, 70, 100));
        }

        String mode = "1P + 3 BOTS";
        g2.setColor(new Color(150, 150, 150));
        g2.drawString(mode, WIDTH - 100, hudY + 25);
    }
}


// ── Game-over overlay ─────────────────────────────────────────────────────

    private void drawGameOver(Graphics2D g2) {
        // Dim the arena
        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Winner text
        String line = winnerLabel.equals("DRAW") ? "DRAW!" : winnerLabel + " WINS!";

        g2.setFont(new Font("SansSerif", Font.BOLD, 56));
        FontMetrics fm = g2.getFontMetrics();

        // Shadow
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(line, (WIDTH - fm.stringWidth(line)) / 2 + 3, HEIGHT / 2 + 3);

        // Coloured text
        Color textCol = winnerLabel.startsWith("PLAYER") ? new Color(255, 140, 170)
                : winnerLabel.startsWith("BOT")    ? new Color(180, 70, 100)
                :                                new Color(230, 205, 60);
        g2.setColor(textCol);
        g2.drawString(line, (WIDTH - fm.stringWidth(line)) / 2, HEIGHT / 2);

        // Sub-line
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        fm = g2.getFontMetrics();
        String sub = "Press  R  to play again | ENTER to Menu";
        g2.setColor(new Color(210, 210, 210));
        g2.drawString(sub, (WIDTH - fm.stringWidth(sub)) / 2, HEIGHT / 2 + 52);
    }
}