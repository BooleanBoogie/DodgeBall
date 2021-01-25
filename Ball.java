package dodgeBall;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Ball {
	private static JPanel pane;
	private int locX;
	private int locY;
	private int diameter;
	private int dx;
	private int dy;
	private int type = -1;
	private Color color;
	private boolean bouncy = true;
	private static boolean bouncyChecked;
	private boolean solidYet = false;

	public Ball() {
		this(randomX(), randomY());
		dx = (int)(7 * Math.random() - 3);
		dy = (int)(7 * Math.random() - 3);
		//make initial dx and dy not 0
		if(dx == 0)
			dx = Math.random() < .5 ? 1 : -1;
		if(dy == 0)
			dy = Math.random() < .5 ? 1 : -1;
		solidYet = true;
	}

	public Ball(int x, int y) {
		locX = x;
		locY = y;
		diameter = 60;
		color = Color.red;
		if(!bouncyChecked)
			bouncy = false;
		//set for when new balls are added. initial balls dx/dy are overridden in respective constructors
		dx = (int)(7 * Math.random() - 3);
		dy = (int)(7 * Math.random() - 3);
		//make initial dx and dy not 0
		if(dx == 0)
			dx = Math.random() < .5 ? 1 : -1;
		if(dy == 0)
			dy = Math.random() < .5 ? 1 : -1;
	}
	//constructor for wave
	public Ball(int dy, int y, boolean starterBall) {
		this(randomX(), y);
		this.dy = dy;
		dx = 0;
		//make initial dy not 0
		if(dy == 0)
			dy = Math.random() < .5 ? 1 : -1;
		if(starterBall)
			solidYet = true;
	}

	public static void setPane(JPanel p) {
		pane = p;
	}

	public void move() {
		locX += dx;
		locY += dy;
		if(bouncy)
			bounceCheck();
		else
			phaseCheck();
	}

	private void bounceCheck() {
		if(locX < 0) {
			dx = -dx;
			locX = 0;
		}
		else if(locX > pane.getWidth() - diameter) {
			dx = -dx;
			locX = pane.getWidth() - diameter;
		}
		if(locY < 0) {
			dy = -dy;
			locY = 0;
		}
		else if(locY > pane.getHeight() - diameter) {
			dy = -dy;
			locY = pane.getHeight() - diameter;
		}
	}

	public void phaseCheck() {
		if(locX <= -diameter)
			locX = pane.getWidth();
		else if(locX >= pane.getWidth())
			locX = -diameter;
		if(locY <= -diameter)
			locY = pane.getHeight();
		else if(locY >= pane.getHeight())
			locY = -diameter;
	}

	//index: index of ball being checked
	public int[] checkSpecialBounce(int index, ArrayList<Ball> balls, int bounceType) {
		//bounces balls off each others
		final int wackyInt = 0;
		final int solidInt = 1;
		boolean stillInParent = false;
		for(index ++; index < balls.size(); index ++) {
			int x2 = balls.get(index).getX();
			int y2 = balls.get(index).getY();
			int d2 = balls.get(index).getDiameter();
			int disX = (x2 + d2/2) - (locX + diameter/2);
			int disY = (y2 + d2/2) - (locY + diameter/2);
			int radii = d2/2 + diameter/2;
			//if collision
			if ((disX * disX) + (disY * disY) < (radii * radii)) {
				if(type == -1 && balls.get(index).getType() == -1) {
					stillInParent = true;
					if(bounceType == wackyInt) {
						dx = (int)(7 * Math.random() - 3);
						dy = (int)(7 * Math.random() - 3);
						break;
					}
					//solid bounce: changes dx and dy of this ball, 
					//and sends back index of other ball for clientserver to change to oldDx and oldDy
					else if(bounceType == solidInt && solidYet && balls.get(index).isSolidYet()) {
						//XXX not working
						int oldDx = dx;
						int oldDy = dy;
						dx = balls.get(index).dx;
						dy = balls.get(index).dy;
						int[] ballBouncedInfo = {index, oldDx, oldDy};
						return ballBouncedInfo;
					}
				}
			}
		}
		//makes solid after ball has left parent Ball
		if(!stillInParent) {
			solidYet = true;
		}
		return null;
	}
	public void setLocX(int x) {
		locX = x;
	}
	public void setLocY(int y) {
		locY = y;
	}
	public void setDx(int dx) {
		this.dx = dx;
	}
	public void setDy(int dy) {
		this.dy = dy;
	}
	public int getDx() {
		return dx;
	}
	public int getDy() {
		return dy;
	}
	public void setDiameter(int d) {
		diameter = d;
	}
	public void drawBall(Graphics g) {
		g.setColor(color);
		g.fillOval(locX, locY, diameter, diameter);
	}
	public int getX() {
		return locX;
	}
	public int getY() {
		return locY;
	}
	public int getDiameter() {
		return diameter;
	}
	public static int randomX() {
		return (int)((pane.getWidth() + 1) * Math.random());
	}
	public static int randomY() {
		return (int)((pane.getHeight() + 1) * Math.random());
	}
	//shows not PowerBall
	public int getType() {
		return type;
	}
	public void setType(int t) {
		type = t;
	}
	public void setBouncy(boolean b) {
		bouncy = b;
	}
	public static void isBouncyChecked(boolean b) {
		bouncyChecked = b;
	}
	public boolean isSolidYet() {
		return solidYet;
	}
	public void setSolidYet() {
		solidYet = true;
	}
}

