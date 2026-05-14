package entity;

import Game_2D.GamePanel;
import bomb.Bomb;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ItemSpawner {
    private GamePanel gp;
    private Random random = new Random();

    // Thời gian spawn (tick)
    private int spawnTimer = 0;
    private int spawnInterval = 15 * 60; // 15 giây (có thể random 15-30s)

    private List<Item> activeItems = new ArrayList<>();

    public ItemSpawner(GamePanel gp) {
        this.gp = gp;
        resetSpawnTimer();
    }

    private void resetSpawnTimer() {
        // Random từ 15 đến 30 giây
        spawnInterval =  60+ random.nextInt(15 * 60);
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

    /**
     * Thuật toán spawn item:
     * 1. Tìm tất cả các ô PATH trống (không có tường, không có item, không có player/bot)
     * 2. Nếu có ô trống -> chọn random
     * 3. Nếu không có -> đợi lần sau
     */
    private void spawnItem() {
        List<int[]> validPositions = new ArrayList<>();

        for (int row = 0; row < gp.maxWorldRow; row++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                // Chỉ spawn trên PATH
                if (gp.tileM.isSolid(col, row)) continue;

                // Không spawn lên bomb
                boolean hasBomb = false;
                for (Bomb bomb : gp.bombAlgo.getActiveBombs()) {
                    if (bomb.getCol() == col && bomb.getRow() == row) {
                        hasBomb = true;
                        break;
                    }
                }
                if (hasBomb) continue;

                // Không spawn lên item khác
                boolean hasItem = false;
                for (Item item : activeItems) {
                    if (item.getCol() == col && item.getRow() == row) {
                        hasItem = true;
                        break;
                    }
                }
                if (hasItem) continue;

                // Không spawn lên player hoặc bot
                if (gp.player.getCol() == col && gp.player.getRow() == row) continue;
                if (gp.bot.getCol() == col && gp.bot.getRow() == row) continue;

                // Hợp lệ
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

    /**
     * Kiểm tra player có nhặt item không
     * Gọi mỗi frame trong Player.update()
     */
    public void checkPickup(Player player) {
        int playerCol = player.getCol();
        int playerRow = player.getRow();

        for (int i = activeItems.size() - 1; i >= 0; i--) {
            Item item = activeItems.get(i);
            if (item.getCol() == playerCol && item.getRow() == playerRow) {
                applyItemEffect(player, item.getType());
                activeItems.remove(i);
            }
        }
    }

    private void applyItemEffect(Player player, ItemType type) {
        switch (type) {
            case SPEED_BOOST:
                // Tăng tốc +2 trong 10s
                player.applySpeedBoost(2, 10 * 60); // +2 speed, 600 ticks
                break;
            case EXTRA_LIFE:
                // Thêm 1 tim
                player.addLife();
                break;
        }
    }

    public void draw(Graphics2D g2) {
        for (Item item : activeItems) {
            item.draw(g2, gp.tileSize);
        }
    }

    public List<Item> getActiveItems() {
        return activeItems;
    }
}