package parkour;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ParkourGame game = new ParkourGame();
                JFrame frame = new JFrame("Parkour Legend: Final App");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                frame.add(game);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                game.startGame();
            }
        });
    }
}
