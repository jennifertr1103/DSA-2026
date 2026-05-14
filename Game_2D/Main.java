package Game_2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        JFrame window = new JFrame("My game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gPanel = new GamePanel();
        window.add(gPanel);
        window.pack();
        window.setLocationRelativeTo(null);

        window.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                gPanel.stopGameThread();
            }
        });

        window.setVisible(true);
        gPanel.requestFocusInWindow();
        gPanel.startGameThread();
    }
}
