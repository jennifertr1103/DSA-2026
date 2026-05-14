package bomb;

/**
 * Directional kind of a Flame tile, used by the renderer to pick the
 * right glyph (center / arm / endpoint).
 *
 * Kept separate from `entity.Direction` because flames have a CENTER
 * concept that movement directions don't, and because conflating the
 * two would couple the bomb package to the entity package's movement
 * semantics. (SOLID-I)
 */
public enum FlameDirection {
    CENTER, UP, DOWN, LEFT, RIGHT
}
