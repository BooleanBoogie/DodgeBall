package dodgeBallClient;

import java.awt.Graphics;
import javax.swing.JPanel;

public class Panel extends JPanel{

	//Label: draws client game, I forget exactly why, but something to do with threads
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		ClientMain.paintStuff(g);
	}
}
