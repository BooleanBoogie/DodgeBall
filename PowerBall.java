package dodgeBall;
//1297 high score
import java.awt.*;
import java.awt.geom.Arc2D;

import javax.swing.*;

public class PowerBall extends Ball{
	public static final int BONUS = 1;
	public static final int SHIELD = 2;
	public static final int SLOMO = 3;
	public static final int SHRINK = 4;
	public static final int SPEED = 5;
	public static final int ASSASSIN = 6;
	private int numberOfPowers = 6;
	private int type;
	private int sneakCounter = 0;
	private int sneakMax = 100;
	private Point userLoc = new Point(0,0);

	public PowerBall(int x, int y) {
		super(x, y);
		type = (int)(numberOfPowers * Math.random() + 1);
		setDiameter(55);
		if(type == SHRINK)
			setDiameter(40);
		setBouncy(false);
	}

	public int getType() {
		return type;
	}

	public void move() {
		if(type != ASSASSIN)
			super.move();
		else {
			sneakCounter ++;
			int speed = 1;
			int sign = 1;
			if(userLoc.x < super.getX())
				sign = -1;
			super.setLocX(super.getX() + sign * speed);
			sign = 1;
			if(userLoc.y < super.getY())
				sign = -1;
			super.setLocY(super.getY() + sign * speed);
			super.phaseCheck();
		}
	}

	public void storeUserLoc(Point p) {
		userLoc = p;
	}

	public boolean ballCollision(Ball b) {
		if(sneakCounter > sneakMax) {
			int x = b.getX();
			int y = b.getY();
			int d = b.getDiameter();
			int disX = (x + d/2) - (getX() + getDiameter()/2);
			int disY = (y + d/2) - (getY() + getDiameter()/2);
			int radii = d/2 + getDiameter()/2;
			return ((disX * disX) + (disY * disY) < (radii * radii));
		}
		else
			return false;
	}

	public void drawBall(Graphics g) {
		if(type == SHIELD) {
			g.setColor(Color.black);
			g.fillOval(getX(), getY(), getDiameter(), getDiameter());
			g.setColor(Color.magenta);
			int diam2 = getDiameter() * 4 / 5;
			g.fillOval(getX() + (getDiameter() - diam2) / 2, getY() + (getDiameter() - diam2) / 2, diam2, diam2);
		}
		else if(type == BONUS) {
			g.setColor(Color.red);
			g.fillArc(getX(), getY(), getDiameter(), getDiameter(), 0, 60);
			g.setColor(Color.orange);
			g.fillArc(getX(), getY(), getDiameter(), getDiameter(), 60, 60);
			g.setColor(Color.YELLOW);
			g.fillArc(getX(), getY(), getDiameter(), getDiameter(), 120, 60);
			g.setColor(Color.GREEN);
			g.fillArc(getX(), getY(), getDiameter(), getDiameter(), 180, 60);
			g.setColor(Color.BLUE);
			g.fillArc(getX(), getY(), getDiameter(), getDiameter(), 240, 60);
			g.setColor(Color.magenta);
			g.fillArc(getX(), getY(), getDiameter(), getDiameter(), 300, 60);
		}
		else if(type == SLOMO) {
			g.setColor(Color.gray);
			g.fillOval(getX(), getY(), getDiameter(), getDiameter());
			int x = getX() + getDiameter() / 2;
			int y = getY() + getDiameter() / 2;
			g.setColor(Color.black);
			g.drawLine(x, y, x, y - getDiameter() * 6/7 / 2);
			g.drawLine(x, y, x - getDiameter() * 4/7 / 2, y);
		}
		else if(type == SHRINK) {
			g.setColor(Color.PINK);
			g.fillOval(getX(), getY(), getDiameter(), getDiameter());
		}
		else if(type == SPEED) {
			g.setColor(Color.yellow);
			g.fillOval(getX(), getY(), getDiameter(), getDiameter());
		}
		else if(type == ASSASSIN) {
			g.setColor(Color.gray);
			if(sneakCounter > sneakMax)
				g.setColor(Color.DARK_GRAY);
			g.fillOval(getX(), getY(), getDiameter(), getDiameter());
			g.setColor(new Color(198, 134, 66));
			g.fillOval(getX(), getY() + getDiameter() / 3, getDiameter(), getDiameter() / 5);
		}
	}
}