package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Keyboard input handler.
 *
 * CHANGES from original:
 *   • BUG FIX: VK_SPACE was mapped to `upPress = true`, making it act as
 *     an upward movement key.  Removed; SPACE is now the bomb key.
 *   • Bomb key changed from VK_X → VK_SPACE (more intuitive for
 *     Bomberman-style play; X is still supported as a secondary binding).
 *   • Added VK_R binding so GamePanel can detect a restart request.
 *   • Everything else (map-based dispatch, consumeBombKey()) unchanged.
 */
public class KeyHandler implements KeyListener {

    public boolean upPress, downPress, leftPress, rightPress;
    public boolean restartPressed, enterPressed;

    /**
     * Edge-triggered bomb flag: set on key-down, cleared by
     * Player after it has acted on it (prevents spam).
     */
    public boolean bombKeyPressed;

    private final Map<Integer, Consumer<Boolean>> bindings = new HashMap<>();

    public KeyHandler() {
        bindKeys();
    }

    private void bindKeys() {
        // Movement — W/S/A/D and Arrow keys
        bindings.put(KeyEvent.VK_W,     p -> upPress    = p);
        bindings.put(KeyEvent.VK_UP,    p -> upPress    = p);
        bindings.put(KeyEvent.VK_S,     p -> downPress  = p);
        bindings.put(KeyEvent.VK_DOWN,  p -> downPress  = p);
        bindings.put(KeyEvent.VK_A,     p -> leftPress  = p);
        bindings.put(KeyEvent.VK_LEFT,  p -> leftPress  = p);
        bindings.put(KeyEvent.VK_D,     p -> rightPress = p);
        bindings.put(KeyEvent.VK_RIGHT, p -> rightPress = p);

        // Bomb: SPACE (primary) or X (secondary) — edge-triggered on press only
        bindings.put(KeyEvent.VK_SPACE, p -> { if (p) bombKeyPressed = true; });
        bindings.put(KeyEvent.VK_X,     p -> { if (p) bombKeyPressed = true; });

        // Restart (edge-triggered)
        bindings.put(KeyEvent.VK_R, p -> { if (p) restartPressed = true; });

        // Enter (edge-triggered)
        bindings.put(KeyEvent.VK_ENTER, p -> { if (p) enterPressed = true; });
    }

    /** Player calls this after consuming the bomb press. */
    public void consumeBombKey() { bombKeyPressed = false; }

    /** GamePanel calls this after consuming the restart press. */
    public void consumeRestartKey() { restartPressed = false; }

    /** GamePanel calls this after consuming the enter press. */
    public void consumeEnterKey() { enterPressed = false; }

    private void dispatch(int code, boolean pressed) {
        Consumer<Boolean> b = bindings.get(code);
        if (b != null) b.accept(pressed);
    }

    @Override public void keyTyped(KeyEvent e)    { /* not used */ }
    @Override public void keyPressed(KeyEvent e)  { dispatch(e.getKeyCode(), true);  }
    @Override public void keyReleased(KeyEvent e) { dispatch(e.getKeyCode(), false); }
}
