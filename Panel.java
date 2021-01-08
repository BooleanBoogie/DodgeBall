package DodgeBallClient;

import java.awt.Graphics;
import javax.swing.JPanel;

public class Panel extends JPanel{

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		ClientMain.paintStuff(g);
	}
}
