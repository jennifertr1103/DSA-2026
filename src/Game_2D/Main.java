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
    private SelectionPanel selectionPanel;
    private ModeSelectionPanel modeSelectionPanel;
    private GachaPanel gachaPanel;
    private SoundManager soundManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().launch());
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public GamePanel getGamePanel() {
        return gPanel;
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
        selectionPanel = new SelectionPanel(this);
        modeSelectionPanel = new ModeSelectionPanel(this, gPanel);
        gachaPanel = new GachaPanel(this);

        mainPanel.add(sPanel, "MENU");
        mainPanel.add(iPanel, "INSTRUCTIONS");
        mainPanel.add(selectionPanel, "SELECTION");
        mainPanel.add(modeSelectionPanel, "MODE_SELECTION");
        mainPanel.add(gachaPanel, "GACHA");
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

    public void showSelection() {
        cardLayout.show(mainPanel, "SELECTION");
        selectionPanel.requestFocusInWindow();
    }

    public void showModeSelection() {
        cardLayout.show(mainPanel, "MODE_SELECTION");
        modeSelectionPanel.requestFocusInWindow();
    }

    public void showGacha() {
        cardLayout.show(mainPanel, "GACHA");
        gachaPanel.requestFocusInWindow();
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
