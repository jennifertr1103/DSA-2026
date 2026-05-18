package Game_2D;

import sound.SoundManager;
import sound.SoundManager.SoundType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private JFrame window;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GamePanel gPanel;
    private StartPanel sPanel;
    private InstructionPanel iPanel;
    private SoundManager soundManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().launch());
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    private void launch() {
        window = new JFrame("My game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        soundManager = new SoundManager();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize panels
        gPanel = new GamePanel(this);
        sPanel = new StartPanel(this);
        iPanel = new InstructionPanel(this);

        mainPanel.add(sPanel, "MENU");
        mainPanel.add(iPanel, "INSTRUCTIONS");
        mainPanel.add(gPanel, "GAME");

        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);

        window.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                gPanel.stopGameThread();
            }
        });

        window.setVisible(true);
        showMenu();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "MENU");
        sPanel.requestFocusInWindow();
        
        // Stop game thread if returning from game
        if (gPanel != null) {
            gPanel.stopGameThread();
        }
        
        if (soundManager != null) {
            soundManager.stopMusic();
            soundManager.playMusic(SoundType.OPENING);
        }
    }

    public void showInstructions() {
        cardLayout.show(mainPanel, "INSTRUCTIONS");
        iPanel.requestFocusInWindow();
    }

    public void showGame() {
        cardLayout.show(mainPanel, "GAME");
        gPanel.requestFocusInWindow();
        
        // Reset and start game
        gPanel.resetGame();
        gPanel.startGameThread();
        
        if (soundManager != null) {
            soundManager.stopMusic();
            soundManager.playMusic(SoundType.BGMUSIC);
        }
    }
}
