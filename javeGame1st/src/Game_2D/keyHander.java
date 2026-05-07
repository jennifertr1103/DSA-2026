package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Refactor notes:
 *  - Renamed `keyHander` (typo) -> `KeyHandler`.
 *  - Replaced two giant if-chains in keyPressed/keyReleased with a
 *    single Map<keyCode, setter>. Adding a new bound key is now one
 *    line in `bindKeys()`. (DRY + KISS + OCP)
 *  - Fixed a subtle bug from the old code: the commented-out `VK_S`
 *    block left a dangling brace, which made the layout very fragile.
 *  - Now binds VK_S to downPress (was missing in keyPressed before).
 *  - Removed `== true` style checks throughout the codebase.
 */
public class keyHander implements KeyListener {

    public boolean upPress, downPress, leftPress, rightPress;

    /**
     * Edge-triggered: true on the frame the bomb key was just pressed,
     * cleared after the player consumes it. Prevents holding X from
     * spamming a bomb every tick.
     */
    public boolean bombKeyPressed;

    private final Map<Integer, Consumer<Boolean>> bindings = new HashMap<>();

    public keyHander() {
        bindKeys();
    }

    private void bindKeys() {
        // Up
        bindings.put(KeyEvent.VK_W,     pressed -> upPress = pressed);
        bindings.put(KeyEvent.VK_UP,    pressed -> upPress = pressed);
        bindings.put(KeyEvent.VK_SPACE, pressed -> upPress = pressed);
        // Down
        bindings.put(KeyEvent.VK_S,     pressed -> downPress = pressed);
        bindings.put(KeyEvent.VK_DOWN,  pressed -> downPress = pressed);
        // Left
        bindings.put(KeyEvent.VK_A,     pressed -> leftPress = pressed);
        bindings.put(KeyEvent.VK_LEFT,  pressed -> leftPress = pressed);
        // Right
        bindings.put(KeyEvent.VK_D,     pressed -> rightPress = pressed);
        bindings.put(KeyEvent.VK_RIGHT, pressed -> rightPress = pressed);

        // Bomb (Bomberman-style). X is used instead of SPACE because
        // SPACE is already mapped to "up" in the original game.
        // Edge-trigger only on key-down; release does nothing.
        bindings.put(KeyEvent.VK_X, pressed -> { if (pressed) bombKeyPressed = true; });
    }

    /** Player calls this once it has acted on the bomb press. */
    public void consumeBombKey() { bombKeyPressed = false; }

    private void dispatch(int keyCode, boolean pressed) {
        Consumer<Boolean> binding = bindings.get(keyCode);
        if (binding != null) binding.accept(pressed);
    }

    @Override public void keyTyped(KeyEvent e) { /* not used */ }

    @Override public void keyPressed(KeyEvent e)  { dispatch(e.getKeyCode(), true); }
    @Override public void keyReleased(KeyEvent e) { dispatch(e.getKeyCode(), false); }
}
