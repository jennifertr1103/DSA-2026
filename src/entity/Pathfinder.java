package entity;

import Game_2D.GamePanel;
import bomb.Bomb;

import java.util.*;

/**
 * Static BFS utilities for Bot navigation.
 *
 * CHANGES from original:
 *   • Added buildDangerSet(gp) — replaces Bot.computeDangerSet().
 *     The old version simply added the 4 cardinal neighbours up to
 *     radius 2 without checking for walls in between.  The new version
 *     traces each blast arm cell-by-cell and stops at hard walls or
 *     bricks (matching the actual explosion spread logic in Bomb.spread),
 *     giving the Bot an accurate picture of which tiles are threatened.
 *   • key(col, row) promoted to package-visible so Bot can use it.
 *   • shortestPath() and nearestSafeCell() kept exactly as the original.
 */
public final class Pathfinder {

    private Pathfinder() { /* static only */ }

    // ── Key helper ────────────────────────────────────────────────────────────

    public static String key(int col, int row) { return col + "," + row; }

    public static boolean isBombAt(GamePanel gp, int col, int row) {
        for (Bomb b : gp.bombAlgo.getActiveBombs()) {
            if (b.getCol() == col && b.getRow() == row) return true;
        }
        return false;
    }

    // ── Danger-set builder (NEW — replaces Bot.computeDangerSet) ─────────────

    /**
     * Constructs a Set<String> of "col,row" keys for every grid cell that
     * is currently threatened by an active bomb (either the bomb's own tile
     * or any tile its blast can reach).
     *
     * Each blast arm is traced one step at a time:
     *   – Hard wall → arm stops (tile NOT added).
     *   – Brick wall → tile added, arm stops (explosion destroys the brick
     *                   but doesn't propagate past it).
     *   – Open path → tile added, arm continues.
     *
     * This mirrors Bomb.spread() exactly, so the Bot's danger perception
     * matches what actually explodes.
     */
    public static Set<String> buildDangerSet(GamePanel gp) {
        Set<String> danger = new HashSet<>();

        for (Bomb bomb : gp.bombAlgo.getActiveBombs()) {
            int bc  = bomb.getCol();
            int br  = bomb.getRow();
            int rad = bomb.getExplosionScale();

            danger.add(key(bc, br));  // bomb centre is always dangerous

            int[][] arms = {{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }};
            for (int[] d : arms) {
                for (int step = 1; step <= rad; step++) {
                    int nc = bc + d[0] * step;
                    int nr = br + d[1] * step;

                    if (gp.tileM.isSolid(nc, nr) && !gp.tileM.isBrick(nc, nr)) {
                        break;  // hard wall — stops here, tile NOT threatened
                    }
                    danger.add(key(nc, nr));
                    if (gp.tileM.isBrick(nc, nr)) {
                        break;  // brick — threatened but arm stops here
                    }
                }
            }
        }
        return danger;
    }

    // ── Shortest path (BFS) — unchanged from original ────────────────────────

    /**
     * Finds the shortest walkable path from (sc, sr) to (gc, gr).
     *
     * @param extraBlocked optional set of "col,row" keys to treat as
     *                     impassable (used to avoid danger zones).
     * @return ordered list of [col, row] steps including start and goal,
     *         or null if no path exists.
     */
    public static List<int[]> shortestPath(GamePanel gp,
                                           int sc, int sr,
                                           int gc, int gr,
                                           Set<String> extraBlocked) {
        if (sc < 0 || gc >= gp.maxWorldCol || sr < 0 || gr >= gp.maxWorldRow) return null;
        if (sc == gc && sr == gr) return Collections.singletonList(new int[]{ sc, sr });

        Map<String, int[]> parent = new HashMap<>();
        Queue<int[]>       queue  = new LinkedList<>();

        String startKey = key(sc, sr);
        parent.put(startKey, null);
        queue.offer(new int[]{ sc, sr });

        int[][] dirs = {{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }};

        while (!queue.isEmpty()) {
            int[] cur  = queue.poll();
            int   curC = cur[0], curR = cur[1];

            for (int[] d : dirs) {
                int nc = curC + d[0];
                int nr = curR + d[1];

                if (gp.tileM.isSolid(nc, nr)) continue;
                if (isBombAt(gp, nc, nr)) continue;

                String k = key(nc, nr);
                if (parent.containsKey(k)) continue;
                if (extraBlocked != null && extraBlocked.contains(k)) continue;

                parent.put(k, new int[]{ curC, curR });

                if (nc == gc && nr == gr) {
                    return reconstruct(parent, sc, sr, gc, gr);
                }
                queue.offer(new int[]{ nc, nr });
            }
        }
        return null;  // no path found
    }

    // ── Nearest safe cell (BFS flood-fill) — unchanged from original ──────────

    /**
     * BFS outward from (sc, sr) until the first walkable cell that is NOT
     * in the danger set.
     *
     * @return [col, row] of the nearest safe cell, or null if none found.
     */
    public static int[] nearestSafeCell(GamePanel gp, int sc, int sr,
                                         Set<String> danger) {
        Queue<int[]> queue   = new LinkedList<>();
        Set<String>  visited = new HashSet<>();

        queue.offer(new int[]{ sc, sr });
        visited.add(key(sc, sr));

        int[][] dirs = {{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }};

        while (!queue.isEmpty()) {
            int[] cur  = queue.poll();
            int   curC = cur[0], curR = cur[1];

            if (danger == null || !danger.contains(key(curC, curR))) {
                return cur;  // first safe cell found
            }

            for (int[] d : dirs) {
                int nc = curC + d[0];
                int nr = curR + d[1];

                if (gp.tileM.isSolid(nc, nr)) continue;
                if (isBombAt(gp, nc, nr)) continue;

                String k = key(nc, nr);
                if (visited.add(k)) {
                    queue.offer(new int[]{ nc, nr });
                }
            }
        }
        return null;  // completely surrounded
    }

    /**
     * BFS outward to find the nearest grid cell that is adjacent to a brick.
     * Used by the Bot to clear paths when the player is blocked.
     */
    public static int[] nearestBrick(GamePanel gp, int sc, int sr) {
        Queue<int[]> queue   = new LinkedList<>();
        Set<String>  visited = new HashSet<>();

        queue.offer(new int[]{ sc, sr });
        visited.add(key(sc, sr));

        int[][] dirs = {{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }};

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cc = cur[0], cr = cur[1];

            // Check if any neighbor is a brick
            for (int[] d : dirs) {
                int nc = cc + d[0];
                int nr = cr + d[1];
                if (gp.tileM.isBrick(nc, nr)) return cur;
            }

            // Otherwise, keep searching through empty paths
            for (int[] d : dirs) {
                int nc = cc + d[0];
                int nr = cr + d[1];
                if (!gp.tileM.isSolid(nc, nr) && !isBombAt(gp, nc, nr)) {
                    String k = key(nc, nr);
                    if (visited.add(k)) {
                        queue.offer(new int[]{ nc, nr });
                    }
                }
            }
        }
        return null;
    }


    /**
     * BFS outward to find the nearest grid cell that contains an item.
     */
    public static int[] nearestItem(GamePanel gp, int sc, int sr) {
        if (gp.itemSpawner == null || gp.itemSpawner.getActiveItems().isEmpty()) return null;

        Queue<int[]> queue   = new LinkedList<>();
        Set<String>  visited = new HashSet<>();

        queue.offer(new int[]{ sc, sr });
        visited.add(key(sc, sr));

        int[][] dirs = {{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }};

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cc = cur[0], cr = cur[1];

            // Check if this cell has an item
            for (Item item : gp.itemSpawner.getActiveItems()) {
                if (item.getCol() == cc && item.getRow() == cr) return cur;
            }

            // Otherwise, keep searching through empty paths
            for (int[] d : dirs) {
                int nc = cc + d[0];
                int nr = cr + d[1];
                if (!gp.tileM.isSolid(nc, nr) && !isBombAt(gp, nc, nr)) {
                    String k = key(nc, nr);
                    if (visited.add(k)) {
                        queue.offer(new int[]{ nc, nr });
                    }
                }
            }
        }
        return null;
    }

    // ── Path reconstruction helper ────────────────────────────────────────────

    private static List<int[]> reconstruct(Map<String, int[]> parent,
                                           int sc, int sr, int gc, int gr) {
        List<int[]> path = new ArrayList<>();
        int c = gc, r = gr;
        while (true) {
            path.add(0, new int[]{ c, r });
            if (c == sc && r == sr) break;
            int[] p = parent.get(key(c, r));
            if (p == null) break;
            c = p[0]; r = p[1];
        }
        return path;
    }
}
