// KeyHandler2.java - NEW FILE for Player 2 controls
package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Keyboard input handler for Player 2.
 * Controls: Arrow keys + Enter (bomb)
 */
public class KeyHandler2 implements KeyListener {

    public boolean upPress, downPress, leftPress, rightPress;
    public boolean bombKeyPressed;

    private final Map<Integer, Consumer<Boolean>> bindings = new HashMap<>();

    public KeyHandler2() {
        bindKeys();
    }

    private void bindKeys() {
        // Movement — Arrow keys
        bindings.put(KeyEvent.VK_UP,    p -> upPress    = p);
        bindings.put(KeyEvent.VK_DOWN,  p -> downPress  = p);
        bindings.put(KeyEvent.VK_LEFT,  p -> leftPress  = p);
        bindings.put(KeyEvent.VK_RIGHT, p -> rightPress = p);

        // Bomb: Enter (primary) — edge-triggered on press only
        bindings.put(KeyEvent.VK_ENTER, p -> { if (p) bombKeyPressed = true; });
    }

    public void consumeBombKey() { bombKeyPressed = false; }

    private void dispatch(int code, boolean pressed) {
        Consumer<Boolean> b = bindings.get(code);
        if (b != null) b.accept(pressed);
    }

    @Override public void keyTyped(KeyEvent e)    { }
    @Override public void keyPressed(KeyEvent e)  { dispatch(e.getKeyCode(), true);  }
    @Override public void keyReleased(KeyEvent e) { dispatch(e.getKeyCode(), false); }
}