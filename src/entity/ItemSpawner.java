package entity;

import Game_2D.GamePanel;
import bomb.Bomb;
import sound.SoundManager;
import sound.SoundManager.SoundType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ItemSpawner {
    private GamePanel gp;
    private Random random = new Random();
    private SoundManager soundManager;

    // Thời gian spawn (tick)
    private int spawnTimer = 0;
    private int spawnInterval = 10 * 60; // 10 giây

    private List<Item> activeItems = new ArrayList<>();

    public ItemSpawner(GamePanel gp) {
        this.gp = gp;
        this.soundManager = gp.soundManager;
        resetSpawnTimer();
    }

    private void resetSpawnTimer() {
        // Random từ 7 đến 15 giây
        spawnInterval = 10 * 60 + random.nextInt(10 * 60);
        spawnTimer = spawnInterval;
    }

    public void update() {
        // Update existing items
        for (int i = activeItems.size() - 1; i >= 0; i--) {
            Item item = activeItems.get(i);
            item.update();
            if (item.isExpired()) {
                activeItems.remove(i);
            }
        }

        // Spawn new item
        spawnTimer--;
        if (spawnTimer <= 0) {
            spawnItem();
            resetSpawnTimer();
        }
    }

    private void spawnItem() {
        List<int[]> validPositions = new ArrayList<>();
        int maxRow = gp.tileM.getMaxRow();
        int maxCol = gp.tileM.getMaxCol();

        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
                if (gp.tileM.isSolid(col, row)) continue;

                // Check if there's already a bomb at this position
                boolean hasBomb = false;
                for (Bomb bomb : gp.bombAlgo.getActiveBombs()) {
                    if (bomb.getCol() == col && bomb.getRow() == row) {
                        hasBomb = true;
                        break;
                    }
                }
                if (hasBomb) continue;

                // Check if there's already an item at this position
                boolean hasItem = false;
                for (Item item : activeItems) {
                    if (item.getCol() == col && item.getRow() == row) {
                        hasItem = true;
                        break;
                    }
                }
                if (hasItem) continue;

                // Check if a player is at this position
                if (gp.player.getCol() == col && gp.player.getRow() == row) continue;
                if (gp.player2 != null && gp.player2.getCol() == col && gp.player2.getRow() == row) continue;

                // Check if a bot is at this position
                boolean onBot = false;
                for (Bot b : gp.bots) {
                    if (b.getCol() == col && b.getRow() == row) {
                        onBot = true;
                        break;
                    }
                }
                if (onBot) continue;

                validPositions.add(new int[]{col, row});
            }
        }

        if (!validPositions.isEmpty()) {
            int[] pos = validPositions.get(random.nextInt(validPositions.size()));
            ItemType type = ItemType.getRandom();
            Item item = new Item(gp, pos[0], pos[1], type);
            activeItems.add(item);
        }
    }

    public void checkPickup(Entity entity) {
        int col = entity.getCol();
        int row = entity.getRow();

        for (int i = activeItems.size() - 1; i >= 0; i--) {
            Item item = activeItems.get(i);
            if (item.getCol() == col && item.getRow() == row) {
                // Play pickup sound before applying effect
                playPickupSound();
                applyItemEffect(entity, item.getType());
                activeItems.remove(i);
            }
        }
    }

    /**
     * Play pickup sound effect
     */
    private void playPickupSound() {
        if (soundManager != null) {
            soundManager.play(SoundType.PICKUP);
        }
    }

    private void applyItemEffect(Entity entity, ItemType type) {
        if (entity instanceof Player) {
            Player p = (Player) entity;
            switch (type) {
                case SPEED_BOOST:
                    p.applySpeedBoost(2, 10 * 60);
                    break;
                case EXTRA_LIFE:
                    p.addLife();
                    break;
            }
        } else if (entity instanceof Bot) {
            Bot b = (Bot) entity;
            switch (type) {
                case SPEED_BOOST:
                    b.applySpeedBoost(2, 10 * 60);
                    break;
                case EXTRA_LIFE:
                    b.addLife();
                    break;
            }
        }
    }

    public void draw(Graphics2D g2) {
        int ts = gp.tileM.getTileSize();
        for (Item item : activeItems) {
            item.draw(g2, ts);
        }
    }

    public List<Item> getActiveItems() {
        return activeItems;
    }
}