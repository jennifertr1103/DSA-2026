package Game_2D;

import javax.swing.*;

public class main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);


        gamePanel gPanel = new gamePanel();
        window.add(gPanel);

        window.setSize(60,300);


        window.pack();

        window.setLocationRelativeTo(null);

        window.setTitle("My game");
        window.setVisible(true);

        gPanel.startGameThread();


    }
}


