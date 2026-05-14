package entity;

public enum Direction {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

    // dx, dy giúp Bot di chuyển pixel và giúp Bom tính ô gạch để phá
    public final int dx;
    public final int dy;

    // dr, dc (row/col) dùng cho thuật toán BFS của Pathfinder
    public final int dr;
    public final int dc;

    Direction(int x, int y) {
        this.dx = x;
        this.dy = y;
        this.dr = y; // Trong mảng 2 chiều, row tương ứng với trục y
        this.dc = x; // col tương ứng với trục x
    }
}