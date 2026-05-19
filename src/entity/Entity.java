package entity;

import Game_2D.Renderable;
import Game_2D.Updatable;
import bomb.Bomb;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public abstract class Entity implements Updatable, Renderable {

    public enum CharacterTier {
        SSR, R, C
    }

    public int worldX, worldY;
    public int speed;

    public Direction direction = Direction.DOWN;

    public int     maxLife = 3;
    public int     life    = 3;
    public boolean alive   = true;
    public boolean dying   = false;

    // ── Invincibility ─────────────────────────────────────────────────────────
    public boolean invincible        = false;
    public int     invincibleCounter = 0;
    private static final int INVINCIBLE_TICKS = 90; // ~1.5 s at 60 fps

    // ── Sprite & Animation ────────────────────────────────────────────────────
    protected final Map<Direction, BufferedImage[]> sprites = new EnumMap<>(Direction.class);
    public int spriteCounter = 0;
    public int spriteNum     = 1;
    protected boolean usingSprites = false; // Flag to check if images were successfully loaded
    public CharacterTier tier = CharacterTier.SSR; // Default tier

    // ── Collision ─────────────────────────────────────────────────────────────
    public Rectangle solidArea           = new Rectangle(0, 0, 48, 48);
    public int       solidAreaDefaultX, solidAreaDefaultY;
    public boolean   collisionOn         = false;

    // ── Per-entity bomb queue (NEW) ───────────────────────────────────────────
    private final Queue<Bomb> bombQueue = new LinkedList<>();
    protected int maxBombs = 3;

    // ── Damage ────────────────────────────────────────────────────────────────

    @Override
    public void takeDamage() {
        if (!invincible) {
            life--;
            if (life <= 0) {
                life  = 0;
                alive = false;
            } else {
                invincible        = true;
                invincibleCounter = 0;
            }
        }
    }

    // ── Common per-frame logic (call from subclass update()) ──────────────────

    protected void updateCommonLogic() {
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter >= INVINCIBLE_TICKS) {
                invincible        = false;
                invincibleCounter = 0;
            }
        }
        bombQueue.removeIf(Bomb::isDestroyed);
    }

    // ── Bomb queue accessors ──────────────────────────────────────────────────

    public Queue<Bomb> getBombQueue() { return bombQueue; }
    public int getMaxBombs() { return maxBombs; }

    public Rectangle getWorldHitbox() {
        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y,
                             solidArea.width, solidArea.height);
    }

    // ── Sprite Loading (NEW TIERED SYSTEM) ────────────────────────────────────

    /**
     * Loads character sprites based on their tier.
     * folderName: The folder inside /res/player/ containing the images (e.g., "Model 1").
     */
    public void loadSprites(String folderName, CharacterTier tier) {
        this.tier = tier;
        String basePath = "/res/player/" + folderName + "/";
        
        try {
            if (tier == CharacterTier.SSR || tier == CharacterTier.R) {
                // Tier SSR & R: 3 frames per direction in sub-folders
                setSprites(Direction.UP,    loadImage(basePath + "back/1.png"),  loadImage(basePath + "back/2.png"),  loadImage(basePath + "back/3.png"));
                setSprites(Direction.DOWN,  loadImage(basePath + "front/1.png"), loadImage(basePath + "front/2.png"), loadImage(basePath + "front/3.png"));
                setSprites(Direction.LEFT,  loadImage(basePath + "left/1.png"),  loadImage(basePath + "left/2.png"),  loadImage(basePath + "left/3.png"));
                setSprites(Direction.RIGHT, loadImage(basePath + "right/1.png"), loadImage(basePath + "right/2.png"), loadImage(basePath + "right/3.png"));
            } else if (tier == CharacterTier.C) {
                // Tier C: 1 frame per direction in sub-folders
                setSprites(Direction.UP,    loadImage(basePath + "back/1.png"));
                setSprites(Direction.DOWN,  loadImage(basePath + "front/1.png"));
                setSprites(Direction.LEFT,  loadImage(basePath + "left/1.png"));
                setSprites(Direction.RIGHT, loadImage(basePath + "right/1.png"));
            }
            usingSprites = true;
            System.out.println("Successfully loaded sprites for: " + folderName);
        } catch (Exception e) {
            System.err.println("FAILED to load sprites for [" + folderName + "] at path [" + basePath + "]");
            System.err.println("Error: " + e.getMessage());
            usingSprites = false;
        }
    }

    private BufferedImage loadImage(String path) throws IOException {
        java.net.URL url = getClass().getResource(path);
        if (url == null) throw new IOException("File not found: " + path);
        return ImageIO.read(url);
    }

    protected void setSprites(Direction dir, BufferedImage... frames) {
        sprites.put(dir, frames);
    }

    protected BufferedImage currentFrame() {
        BufferedImage[] frames = sprites.get(direction);
        if (frames == null || frames.length == 0) return null;
        int index = Math.max(0, Math.min(frames.length - 1, spriteNum - 1));
        return frames[index];
    }

    protected void advanceWalkAnimation() {
        if (tier == CharacterTier.C) return;

        spriteCounter++;
        if (spriteCounter > 10) {
            spriteNum++;
            if (spriteNum > 3) {
                spriteNum = 1;
            }
            spriteCounter = 0;
        }
    }

    // ── Default draw (blink while invincible) ─────────────────────────────────

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;
        
        if (invincible && (System.currentTimeMillis() / 120) % 2 == 0) return;

        if (usingSprites) {
            BufferedImage frame = currentFrame();
            if (frame != null) {
                int tileSize = getDrawSize();
                
                // Tỉ lệ gốc của ảnh
                double imgRatio = (double) frame.getWidth() / frame.getHeight();
                
                // Tăng kích thước cơ bản lên 1.5 lần ô gạch
                double scaleMult = 1.5;
                
                // Bù đắp thị giác: Khi đi ngang (thân mỏng), ta phóng to thêm 15% để nhân vật trông đỡ nhỏ
                if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                    scaleMult *= 1.15;
                }

                int drawHeight = (int) (tileSize * scaleMult); 
                int drawWidth = (int) (drawHeight * imgRatio);
                
                // Căn giữa ngang và đặt chân nhân vật chạm đáy ô gạch
                int x = getScreenX() + (tileSize - drawWidth) / 2;
                int y = getScreenY() + tileSize - drawHeight;

                g2.drawImage(frame, x, y, drawWidth, drawHeight, null);
                return;
            }
        }
        
        // Cần override ở class con nếu muốn fallback
    }

    public abstract int getScreenX();
    public abstract int getScreenY();
    public abstract int getDrawSize();
    public abstract int getCol();
    public abstract int getRow();
}
