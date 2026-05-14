package entity;

public enum ItemType {
    SPEED_BOOST,   // Tăng tốc +2 trong 10s
    EXTRA_LIFE,    // +1 tim
    BOMB_UP,       // Tăng số bomb tối đa (optional)
    FLAME_UP;      // Tăng kích thước lửa (optional)

    public static ItemType getRandom() {
        // Chỉ spawn 2 loại chính: speed và life
        return Math.random() < 0.5 ? SPEED_BOOST : EXTRA_LIFE;
    }
}