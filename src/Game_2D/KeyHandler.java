package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Keyboard input handler for PLAYER 1.
 * - Single player mode: uses WASD + Arrow keys (both)
 * - Multiplayer mode: uses ONLY WASD (Arrow keys reserved for Player 2)
 */
public class KeyHandler implements KeyListener {

    public boolean upPress, downPress, leftPress, rightPress;
    public boolean restartPressed, enterPressed;
    public boolean bombKeyPressed;
    public boolean modeTogglePressed;

    // Flag to disable arrow keys in multiplayer mode
    private boolean useArrowKeys = true;  // Default true for single player

    private final Map<Integer, Consumer<Boolean>> bindings = new HashMap<>();

    public KeyHandler() {
        bindKeys();
    }

    /**
     * Call this when switching to multiplayer mode to disable arrow keys.
     */
    public void setUseArrowKeys(boolean enabled) {
        this.useArrowKeys = enabled;
    }

    private void bindKeys() {
        // Movement — WASD (always active)
        bindings.put(KeyEvent.VK_W, p -> upPress = p);
        bindings.put(KeyEvent.VK_S, p -> downPress = p);
        bindings.put(KeyEvent.VK_A, p -> leftPress = p);
        bindings.put(KeyEvent.VK_D, p -> rightPress = p);

        // Arrow keys — conditionally active (will be checked in dispatch)
        bindings.put(KeyEvent.VK_UP,    p -> { if (useArrowKeys) upPress = p; });
        bindings.put(KeyEvent.VK_DOWN,  p -> { if (useArrowKeys) downPress = p; });
        bindings.put(KeyEvent.VK_LEFT,  p -> { if (useArrowKeys) leftPress = p; });
        bindings.put(KeyEvent.VK_RIGHT, p -> { if (useArrowKeys) rightPress = p; });

        // Bomb: SPACE or X
        bindings.put(KeyEvent.VK_SPACE, p -> { if (p) bombKeyPressed = true; });
        bindings.put(KeyEvent.VK_X,     p -> { if (p) bombKeyPressed = true; });

        // Restart
        bindings.put(KeyEvent.VK_R, p -> { if (p) restartPressed = true; });

        // Enter (for menu)
        bindings.put(KeyEvent.VK_ENTER, p -> { if (p) enterPressed = true; });

        // Mode toggle: M
        bindings.put(KeyEvent.VK_M, p -> { if (p) modeTogglePressed = true; });
    }

    public void consumeBombKey() { bombKeyPressed = false; }
    public void consumeRestartKey() { restartPressed = false; }
    public void consumeEnterKey() { enterPressed = false; }
    public void consumeModeToggleKey() { modeTogglePressed = false; }

    private void dispatch(int code, boolean pressed) {
        Consumer<Boolean> b = bindings.get(code);
        if (b != null) b.accept(pressed);
    }

    @Override public void keyTyped(KeyEvent e) { }
    @Override public void keyPressed(KeyEvent e) { dispatch(e.getKeyCode(), true); }
    @Override public void keyReleased(KeyEvent e) { dispatch(e.getKeyCode(), false); }
}