package dodgeBall;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.JPanel;

public class UserBall{
	private JPanel pane;
	private int x;
	private int y;
	public static int diameter;
	public final static int SMALL = 30;
	public final static int NORMAL_DIAMETER = 40;
	public static int speed;
	public final static int FAST = 8;
	public final static int NORMAL_SPEED = 4;
	private int shieldWidth = 4;
	private Color c;
	//ball bigger than actual game space
	private int extraRoom = 1;
	int hairs;
	private boolean hairWave;

	public UserBall(int x, int y, Color col, JPanel p) {
		this(x, y, col, p, 25);
	}
	public UserBall(int x, int y, Color col, JPanel p, int hair) {
		pane = p;
		diameter = 40;
		c = col;
		speed = NORMAL_SPEED;
		this.x = x;
		this.y = y;
		hairs = hair;
	}

	public void drawUser(Graphics g, boolean shielded) {
		g.setColor(c);
		g.fillOval(x, y, diameter + extraRoom, diameter + extraRoom);
		//only do ears when not shielded, otherwise they look wierd
		if(!shielded) {
			g.fillArc(x - diameter * 7/16, y + diameter / 3, diameter, diameter, 90, 30);
			g.fillArc(x + diameter * 7/16, y + diameter / 3, diameter, diameter, 90, -30);
		}
		//nose
		g.setColor(c.darker());
		g.fillArc(x + diameter / 3, y + diameter / 6, diameter / 3, diameter * 3/4, 220, 100);
		//eyes
		g.setColor(Color.black);
		g.fillOval(x + diameter / 4, y + diameter * 4/9, diameter / 6, diameter / 7);
		g.fillOval(x + diameter * 9/16, y + diameter * 4/9, diameter / 6, diameter / 7);
		//hair
		g.setColor(new Color(141, 85, 36));
		if(!hairWave)
			for(int k = 1; k < hairs; k ++)
				g.drawArc(x + diameter * k / hairs, y - diameter * 3 / 4, diameter * 2, diameter * 2, 190, -40);
		else{
			for(int k = 1; k < hairs; k ++)
				g.drawArc(x + diameter * k / hairs, y - diameter * 1 / 4, diameter * 2, diameter, 190, -40);
		}
		hairWave = false;
	}

	public void drawShield(Graphics g) {
		g.setColor(Color.black);
		g.fillOval(x - shieldWidth, y - shieldWidth, diameter + shieldWidth * 2 + extraRoom, diameter + shieldWidth * 2 + extraRoom);
	}

	public void moveDown() {
		if(y + diameter <= pane.getHeight() - speed)
			y += speed;
	}

	public void moveUp() {
		if(y >= speed)
			y -= speed;
	}

	public void moveLeft() {
		if(x >= speed)
			x -= speed;
	}

	public void moveRight() {
		if(x + diameter <= pane.getWidth() - speed)
			x += speed;
	}
	
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean ballCollision(Ball b) {
		int x = b.getX();
		int y = b.getY();
		int d = b.getDiameter();
		int disX = (x + d/2) - (this.x + diameter/2);
		int disY = (y + d/2) - (this.y + diameter/2);
		int radii = d/2 + diameter/2;
		//hair
		if(y + d > this.y - diameter / 2 && y + d < this.y + diameter / 2 &&
				x + d/2  + d * 2/5> this.x && x + d/2 - d * 2/5< this.x + diameter) 
			hairWave = true;
		return ((disX * disX) + (disY * disY) < (radii * radii));
	}

	public boolean weavesThrough(Line2D line) {
		return line.ptSegDist(x + diameter / 2, y + diameter / 2) < diameter / 2;
	}

	public Point getLocation() {
		return new Point(x, y);
	}
	
	public int ishairWave() {
		if(hairWave)
			return 1;
		else
			return 0;
	}
	
	public void setHairWave(boolean b){
		hairWave = b;
	}
}
