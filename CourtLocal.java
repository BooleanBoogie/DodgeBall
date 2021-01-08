package dodgeBall;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;

public class CourtLocal extends JPanel implements KeyListener, ActionListener{
	private JFrame frame;
	private Container can;
	private JMenuBar menuBar;
	private JMenuItem mainMenu;
	private JMenuItem settings;
	private JMenuItem credits;
	private JPanel mainMenuPane;
	private JPanel settingsPane;
	private JPanel creditsPane;
	private JLabel label;
	private JLabel label2;
	private JButton button1;
	private JButton button2; 
	private JTextField difficulty;
	private JTextField numHairs1;
	private JTextField numHairs2;
	private JCheckBox weaveCheck;
	private JCheckBox bouncyCheck;
	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private UserBall userA;
	private UserBall userB;
	private Ball ball1;
	private Ball ball2;
	private Line2D line;
	private Timer timer;
	private Timer scoreTimer;
	private Dimension menuSize = new Dimension(250, 220);
	private int menuShow = 1;
	private int width = 600;
	private int height = width;
	private int initialTimerSpeed = 15;
	private int score;
	private int regHighScore;
	private int weaveHighScore;
	private int ballParentIndex = 0;
	private int initialBallsSize = 5;
	private int birthInterval = 50;
	private int birthCounter;
	private int shieldCounter = 0;
	private int slomoCounter = 0;
	private int shrinkCounter = 0;
	private int speedCounter = 0;
	private int powerMax = 7 * 10;
	private int hairs1 = 25;
	private int hairs2 = 25;
	private double powerChance = .3;
	private boolean upA;
	private boolean downA;
	private boolean leftA;
	private boolean rightA;
	private boolean upB;
	private boolean downB;
	private boolean leftB;
	private boolean rightB;
	private boolean singlePlayer;
	private boolean menuOn;
	private boolean started;
	private boolean done;
	private boolean pause;
	private boolean shielded;
	private boolean weave;
	private Object last;

	public CourtLocal() {
		frame = new JFrame("Menu");
		can = frame.getContentPane();
		makeMenuPanes();

		can.add(mainMenuPane);
		menuOn = true;

		menuBar = new JMenuBar();
		mainMenu = new JMenuItem("Main Menu");
		settings = new JMenuItem("Settings");
		credits = new JMenuItem("Credits");
		menuBar.add(mainMenu);
		menuBar.add(settings);
		menuBar.add(credits);
		frame.setJMenuBar(menuBar);
		last = mainMenu;

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(menuSize);
		frame.setVisible(true);

		frame.addKeyListener(this);
		button1.addActionListener(this);
		button2.addActionListener(this);
		mainMenu.addActionListener(this);
		settings.addActionListener(this);
		credits.addActionListener(this);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(Ball b: balls)
			b.drawBall(g);
		if(!started || shielded) {
			userA.drawShield(g);
			if(!singlePlayer)
				userB.drawShield(g);
		}
		userA.drawUser(g, shielded || !started);
		if(!singlePlayer)
			userB.drawUser(g, shielded || !started);
		if(weave) {
			drawWeaveLine((Graphics2D)g);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object ob = e.getSource();
		//start game when game mode selected
		if(ob == button1 || ob == button2) 
			startGame(e);
		else if(ob == timer) 
			timerHappens();
		else if(ob == scoreTimer) 
			scoreTimerHappens();
		else if(ob != last && (ob == mainMenu || ob == settings || ob == credits)) {
			frame.setSize((int)menuSize.getWidth(), (int)menuSize.getHeight() + menuShow);
			if(menuShow == 1)
				menuShow = 0;
			else
				menuShow = 1;
			can.removeAll();
			if(ob == mainMenu) 
				can.add(mainMenuPane);
			else if(ob == settings)
				can.add(settingsPane);
			else if(ob == credits)
				can.add(creditsPane);
			frame.setVisible(true);
			last = ob;
		}
	}

	public void timerHappens() {
		for(int i = 0; i < balls.size() && !done; i ++) {
			balls.get(i).move();
			int type = balls.get(i).getType();
			//collision
			if((started && !shielded || type != -1) && 
					(userA.ballCollision(balls.get(i)) || !singlePlayer && userB.ballCollision(balls.get(i)))) 
			{
				if(type == -1) {
					endGame();
				}
				//powerBalls
				else {
					if(type == PowerBall.BONUS) {
						if(weave)
							score += 5;
						else
							score += 200;
					}
					else if(type == PowerBall.SHIELD) {
						shielded = true;
						shieldCounter = 0;
					}
					else if(type == PowerBall.SLOMO) {
						timer.setDelay(initialTimerSpeed * 2);
						UserBall.speed = UserBall.FAST;
						slomoCounter = 1;
					}
					else if(type == PowerBall.SHRINK) {
						UserBall.diameter = UserBall.SMALL;
						shrinkCounter = 1;
					}
					else if(type == PowerBall.SPEED) {
						UserBall.speed = UserBall.FAST;
						speedCounter = 1;
					}

					balls.remove(i);
					i --;
					ballParentIndex --;
				}
			}	
		}
		//check if user weaved
		if(started && weave)
			if(userA.weavesThrough(line) || !singlePlayer && userB.weavesThrough(line)) {
				score ++;
				setWeaveBalls();
			}
		//move users
		if(upA)
			userA.moveUp();
		else if(downA)
			userA.moveDown();
		if(leftA)
			userA.moveLeft();
		else if(rightA) 
			userA.moveRight();
		if(!singlePlayer) {
			if(upB)
				userB.moveUp();
			else if(downB)
				userB.moveDown();
			if(leftB)
				userB.moveLeft();
			else if(rightB) 
				userB.moveRight();
		}
		repaint();
	}

	public void scoreTimerHappens(){
		birthCounter ++;
		if(!weave)
			score ++;
		if(birthCounter % birthInterval == 0) {
			Ball b = balls.get(ballParentIndex);
			if(Math.random() <= powerChance)
				balls.add(new PowerBall(b.getX(), b.getY()));
			else
				balls.add(new Ball(b.getX(), b.getY()));
			ballParentIndex ++;
		}
		if(shielded) {
			shieldCounter ++;
			if(shieldCounter == powerMax) {
				shielded = false;
				shieldCounter = 0;
			}
		}
		if(slomoCounter > 0) {
			slomoCounter ++;
			if(slomoCounter == powerMax) {
				slomoCounter = 0;
				timer.setDelay(initialTimerSpeed);
				UserBall.speed = UserBall.NORMAL_SPEED;
			} 
		}
		if(shrinkCounter > 0) {
			shrinkCounter ++;
			if(shrinkCounter == powerMax) {
				UserBall.diameter = UserBall.NORMAL_DIAMETER;
				shrinkCounter = 0;
			}
		}
		if(speedCounter > 0) {
			speedCounter ++;
			if(speedCounter == powerMax) {
				UserBall.speed = UserBall.NORMAL_SPEED;
				speedCounter = 0;
			}
		}
		frame.setTitle("Score: " + score);
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		//if moved
		if(key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT ||
				key == KeyEvent.VK_RIGHT || !singlePlayer && (key == KeyEvent.VK_W || 
				key == KeyEvent.VK_S || key == KeyEvent.VK_A || key == KeyEvent.VK_D)) {
			//when moved first time
			if(!started && !menuOn) {
				scoreTimer.start();
				started = true;
			}
			if(key == KeyEvent.VK_UP)
				upA = true;
			else if(key == KeyEvent.VK_DOWN)
				downA = true;
			else if(key == KeyEvent.VK_LEFT)
				leftA = true;
			else if(key == KeyEvent.VK_RIGHT)
				rightA = true;
			else if(!singlePlayer) {
				if(key == KeyEvent.VK_W)
					upB = true;
				else if(key == KeyEvent.VK_S)
					downB = true;
				else if(key == KeyEvent.VK_A)
					leftB = true;
				else if(key == KeyEvent.VK_D)
					rightB = true;
			}
		}
		else if(key == KeyEvent.VK_SPACE) {
			if(pause) {
				pause = false;
				timer.start();
				if(started)
					scoreTimer.start();
			}
			else {
				pause = true;
				timer.stop();
				scoreTimer.stop();
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP)
			upA = false;
		else if(e.getKeyCode() == KeyEvent.VK_DOWN)
			downA = false;
		else if(e.getKeyCode() == KeyEvent.VK_LEFT)
			leftA = false;
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			rightA = false;
		else if(!singlePlayer) {
			if(e.getKeyCode() == KeyEvent.VK_W)
				upB = false;
			else if(e.getKeyCode() == KeyEvent.VK_S)
				downB = false;
			else if(e.getKeyCode() == KeyEvent.VK_A)
				leftB = false;
			else if(e.getKeyCode() == KeyEvent.VK_D)
				rightB = false;
		}
	}

	public void startGame(ActionEvent e) {
		//takes focus off of buttons
		frame.setFocusable(true);
		//settings values
		if(weaveCheck.isSelected())
			weave = true;
		else
			weave = false;
		if(bouncyCheck.isSelected())
			Ball.isBouncyChecked(true);
		else
			Ball.isBouncyChecked(false);
		if(isNumeric(numHairs1.getText()))
			hairs1 = Integer.parseInt(numHairs1.getText()) + 1;
		if(isNumeric(numHairs2.getText()))
			hairs2 = Integer.parseInt(numHairs2.getText()) + 1;
		if(isNumeric(difficulty.getText()))
			initialBallsSize = Integer.parseInt(difficulty.getText());
		//number of players
		if(e.getSource() == button1) {
			singlePlayer = true;
			userA = new UserBall(width / 2, height / 2 - 22, new Color(241, 194, 125), this, hairs1);
		}
		else if(e.getSource() == button2) {
			singlePlayer = false;
			userA = new UserBall(width / 2 + 30, height / 2 - 22, new Color(241, 194, 125), this, hairs1);
			userB = new UserBall(width / 2 - 30, height / 2 - 22, new Color(198, 134, 66), this, hairs2);
		}
		can.removeAll();
		can.add(this);
		menuBar.setVisible(false);
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.white);
		frame.setTitle("Score: 0");
		frame.pack();

		balls.removeAll(balls);
		Ball.setPane(this);
		for(int j = 0; j < initialBallsSize; j ++) {
			balls.add(new Ball());
		}
		if(weave) {
			setWeaveBalls();
		}
		menuOn = false;
		frame.setVisible(true);
		timer = new Timer(initialTimerSpeed, this);
		timer.start();
		scoreTimer = new Timer(100, this);
	}

	public void endGame() {
		timer.stop();
		scoreTimer.stop();
		if(weave) {
			if(score > weaveHighScore)
				weaveHighScore = score;
			label.setText("High Score: " + regHighScore);
			label2.setText("Score: " + score + "  Weave High Score: " + weaveHighScore);
		}
		else {
			if(score > regHighScore)
				regHighScore = score;
			label.setText("Score: " + score + "  High Score: " + regHighScore);
			label2.setText("Weave High Score: " + weaveHighScore);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		started = false;
		menuOn = true;
		score = 0;
		ballParentIndex = 0;
		can.remove(this);
		can.add(mainMenuPane);
		menuBar.setVisible(true);
		frame.setTitle("Menu");
		frame.setSize(menuSize);
		frame.setVisible(true);
	}

	public void makeMenuPanes() {
		//Main Menu panel
		mainMenuPane = new JPanel();
		JPanel menuPane1 = new JPanel();
		JPanel menuPane2 = new JPanel();
		JPanel menuPane3 = new JPanel();
		label = new JLabel("Survival High Score: " + regHighScore);
		label2 = new JLabel("Weave High Score: " + weaveHighScore);
		button1 = new JButton("1 Player");
		button2 = new JButton("2 Player");
		menuOn = true;
		mainMenuPane.setLayout(new BorderLayout());
		menuPane1.add(label);
		menuPane2.add(label2);
		menuPane3.add(button1);
		menuPane3.add(button2);
		mainMenuPane.add(menuPane1,BorderLayout.NORTH);
		mainMenuPane.add(menuPane2, BorderLayout.CENTER);
		mainMenuPane.add(menuPane3,BorderLayout.SOUTH);
		//settings panel
		settingsPane = new JPanel();
		settingsPane.setLayout(new BorderLayout());
		//difficulty
		JPanel difficultyPane = new JPanel();
		JLabel q1 = new JLabel("Difficulty:");
		difficulty = new JTextField("5");
		difficultyPane.setLayout(new BorderLayout());
		difficultyPane.add(q1, BorderLayout.NORTH);
		difficultyPane.add(difficulty, BorderLayout.CENTER);
		//weave and bouncy
		JPanel weaveBouncyPane = new JPanel();
		weaveBouncyPane.setLayout(new BorderLayout());
		weaveCheck = new JCheckBox("Weave");
		bouncyCheck = new JCheckBox("Bouncy Balls");
		bouncyCheck.setSelected(true);
		weaveBouncyPane.add(weaveCheck, BorderLayout.NORTH);
		weaveBouncyPane.add(bouncyCheck, BorderLayout.SOUTH);
		//number of hairs
		JPanel numHairsPane = new JPanel();
		JPanel h2 = new JPanel();
		JLabel q21 = new JLabel("Player 1 Number of Hairs:");
		numHairs1 = new JTextField("25");
		JLabel q22 = new JLabel("Player 2 Number of Hairs:");
		numHairs2 = new JTextField("25");
		numHairsPane.setLayout(new BorderLayout());
		numHairsPane.add(q21, BorderLayout.NORTH);
		numHairsPane.add(numHairs1, BorderLayout.CENTER);
		h2.setLayout(new BorderLayout());
		h2.add(q22, BorderLayout.NORTH);
		h2.add(numHairs2, BorderLayout.CENTER);
		numHairsPane.add(h2, BorderLayout.SOUTH);

		settingsPane.add(difficultyPane, BorderLayout.NORTH);
		settingsPane.add(weaveBouncyPane, BorderLayout.CENTER);
		settingsPane.add(numHairsPane, BorderLayout.SOUTH);
		//credits panel
		creditsPane = new JPanel();
		creditsPane.add(new JLabel("Johnny"));
	}

	public void setWeaveBalls() {
		//set b1 and b2 to 2 random balls
		int i = 0;
		while(balls.get(i).getType() != -1)
			i = (int)((balls.size()) * Math.random());
		int j = i;
		while(i == j || balls.get(i).getType() != -1)
			i = (int)((balls.size()) * Math.random());
		ball1 = balls.get(j);
		ball2 = balls.get(i);
		System.out.println(ball2.getX());
	}

	public void drawWeaveLine(Graphics2D g2) {
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(Color.red);
		Point p1 = new Point(ball1.getX() + ball1.getDiameter() / 2, ball1.getY() + ball1.getDiameter() / 2);
		Point p2 = new Point(ball2.getX() + ball2.getDiameter() / 2, ball2.getY() + ball2.getDiameter() / 2);
		line = new Line2D.Double(p1, p2);
		g2.draw(line);
	}

	public static boolean isNumeric(String str) { 
		try {  
			Integer.parseInt(str);  
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}

	public void mouseDragged(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public static void main(String[] args) throws IOException {
		new CourtLocal();
	}
}
