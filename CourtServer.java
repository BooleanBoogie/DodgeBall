package dodgeBall;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;

public class CourtServer extends JPanel implements KeyListener, ActionListener{
	private static Server server;
	private static JFrame frame;
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
	private static JLabel label3;
	private static JButton startButton;
	private JTextField difficulty;
	private JTextField numHairs1;
	private JTextField numHairs2;
	private JCheckBox waveCheck;
	private JCheckBox weaveCheck;
	private JCheckBox bouncyCheck;
	private JCheckBox wackyCheck;
	private JCheckBox solidCheck;
	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private UserBall userA;
	private static UserBall userB;
	private int ball1Index;
	private int ball2Index;
	private Line2D line;
	private Timer timer;
	private Timer scoreTimer;
	private Dimension menuSize = new Dimension(260, 290);
	private int menuShow = 1;
	private int width = 600;
	private int height = width;
	private int initialTimerSpeed = 15;
	private int score;
	private int regHighScore;
	private int weaveHighScore;
	private int parentBallIndex = 0;
	private int initialBallsSize = 5;
	private int waveDy;
	private int birthInterval = 50;
	private int birthCounter;
	private int countDown = 5;
	private int shieldCounter = 0;
	private int slomoCounter = 0;
	private int shrinkCounter = 0;
	private int speedCounter = 0;
	private int powerMax = 7 * 10;
	private int hairs1 = 25;
	private int hairs2 = 25;
	private static int connectionCheckCounter;
	private double powerChance = .3;
	private boolean up;
	private boolean down;
	private boolean left;
	private boolean right;
	private boolean menuOn;
	private static boolean started;
	public static boolean connected;
	private static boolean ballsRecievedByClient;
	private boolean done;
	private boolean pause;
	private boolean shielded;
	private boolean wave;
	private boolean weave;
	private boolean wackyBounce;
	private boolean solidBounce;
	private Object last;

	//XXX make the game check for disconnect in menu
	//XXX make 4 player (or infinite)
	//XXX add 1 player option

	public CourtServer() {
		frame = new JFrame("Menu");
		can = frame.getContentPane();
		makeMenuPanes();
		can.add(mainMenuPane);

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
		startButton.addActionListener(this);
		mainMenu.addActionListener(this);
		settings.addActionListener(this);
		credits.addActionListener(this);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(wave) {
			drawWaveLine(g);
			g.setFont(new Font("countDown Font", Font.BOLD, 30));
			g.drawString("" +countDown, frame.getWidth() / 2, frame.getHeight() / 2 - 22 - 30);
		}
		for(Ball b: balls)
			b.drawBall(g);
		if(!started || shielded) {
			userA.drawShield(g);
			userB.drawShield(g);
		}
		userA.drawUser(g, shielded || !started);
		userB.drawUser(g, shielded || !started);
		if(weave) {
			drawWeaveLine((Graphics2D)g);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object ob = e.getSource();
		//start game when game mode selected
		if(ob == startButton) {
			if(connected)
				try {
					startGame(e);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
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
		else if(ob == wackyCheck) {
			solidCheck.setEnabled(!wackyCheck.isSelected());
		}
		else if(ob == solidCheck) {
			wackyCheck.setEnabled(!solidCheck.isSelected());
		}
	}

	private void timerHappens() {
		connectionCheckCounter ++;
		if(connectionCheckCounter > 15)
			connected = false;
		if(!connected)
			endGame();
		if(!pause && ballsRecievedByClient) {
			for(int i = 0; i < balls.size() && !done; i ++) {
				balls.get(i).move();
				//bounce off other balls
				final int wackyInt = 0;
				final int solidInt = 1;
				if(wackyBounce)
					balls.get(i).checkSpecialBounce(i, balls, wackyInt);
				else if(solidBounce) {
					int[] ballBouncedInfo = balls.get(i).checkSpecialBounce(i, balls, solidInt);
					if(ballBouncedInfo != null) {
						balls.get(ballBouncedInfo[0]).setDx(ballBouncedInfo[1]);
						balls.get(ballBouncedInfo[0]).setDy(ballBouncedInfo[2]);
					}
				}
				int type = balls.get(i).getType();
				//collision
				if((started && !shielded || type != -1) && 
						(userA.ballCollision(balls.get(i)) || userB.ballCollision(balls.get(i)))) 
				{
					if(type == -1) {
						endGame();
					}
					//powerBalls
					else {
						if(type == PowerBall.BONUS) {
							if(weave)
								score += 10;
							else
								score += 500;
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
						parentBallIndex --;
					}
				}	
			}
			//check if user weaved
			if(started && weave)
				if(userA.weavesThrough(line) || userB.weavesThrough(line)) {
					score ++;
					setWeaveBalls();
				}
			//stops last info send plus extra work
			if(!menuOn) {
				//move users
				if(up)
					userA.moveUp();
				else if(down)
					userA.moveDown();
				if(left)
					userA.moveLeft();
				else if(right) 
					userA.moveRight();
				sendBalls();
				sendInfo();
				repaint();
			}
		}
	}

	private void scoreTimerHappens(){
		if(!pause) {
			birthCounter ++;
			if(wave) 
				countDown = (int) (5 - birthCounter % birthInterval / 10);
			if(!weave)
				score ++;
			if(birthCounter % birthInterval == 0) {
				if(wave) {
					waveDy = (int)(9 * Math.random() - 4);
					for(Ball b: balls) {
						if(b.getType() == -1)
							b.setDy(waveDy);
					}
					setWeaveBalls();
				}
				Ball b = balls.get(parentBallIndex);
				if(Math.random() <= powerChance)
					balls.add(new PowerBall(b.getX(), b.getY()));
				else {
					if(!wave)
						balls.add(new Ball(b.getX(), b.getY()));
					else
						balls.add(new Ball(waveDy, frame.getHeight() / 2 - 22, true));
				}
				parentBallIndex ++;
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
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		//if moved
		if(key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT ||
				key == KeyEvent.VK_RIGHT) {
			//when moved first time
			if(!started && !menuOn) {
				scoreTimer.start();
				started = true;
			}
			if(key == KeyEvent.VK_UP)
				up = true;
			else if(key == KeyEvent.VK_DOWN)
				down = true;
			else if(key == KeyEvent.VK_LEFT)
				left = true;
			else if(key == KeyEvent.VK_RIGHT)
				right = true;
		}
		else if(key == KeyEvent.VK_SPACE) {
			if(pause)
				pause = false;
			else 
				pause = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP)
			up = false;
		else if(e.getKeyCode() == KeyEvent.VK_DOWN)
			down = false;
		else if(e.getKeyCode() == KeyEvent.VK_LEFT)
			left = false;
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			right = false;
	}

	private void startGame(ActionEvent e) throws IOException {
		//takes focus off of buttons
		frame.setFocusable(true);
		//settings values
		wave = waveCheck.isSelected();
		weave = weaveCheck.isSelected();
		if(bouncyCheck.isSelected())
			Ball.isBouncyChecked(true);
		else
			Ball.isBouncyChecked(false);
		solidBounce = solidCheck.isSelected();
		wackyBounce = wackyCheck.isSelected();
		if(isNumeric(numHairs1.getText()))
			hairs1 = Integer.parseInt(numHairs1.getText()) + 1;
		if(isNumeric(numHairs2.getText()))
			hairs2 = Integer.parseInt(numHairs2.getText()) + 1;
		//max 38 before they all blend. 37 is fun
		if(hairs1 > 40)
			hairs1 = 40;
		if(hairs2 > 40)
			hairs2 = 40;
		if(isNumeric(difficulty.getText()))
			initialBallsSize = Integer.parseInt(difficulty.getText());
		//XXX adjust to add number of players
		userA = new UserBall(width / 2 + 30, height / 2 - 22, new Color(241, 194, 125), this, hairs1);
		userB = new UserBall(width / 2 - 30, height / 2 - 22, new Color(198, 134, 66), this, hairs2);
		can.removeAll();
		can.add(this);
		menuBar.setVisible(false);
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.white);
		frame.setTitle("Score: 0");
		frame.pack();

		balls.removeAll(balls);
		Ball.setPane(this);
		//for if wave
		waveDy = (int)(7 * Math.random() - 3);
		for(int j = 0; j < initialBallsSize; j ++) {
			if(!wave) 
				balls.add(new Ball());
			else {
				balls.add(new Ball(waveDy, height / 2, true));
			}
		}
		if(weave) {
			setWeaveBalls();
		}
		menuOn = false;
		frame.setVisible(true);
		timer = new Timer(initialTimerSpeed, this);
		sendBalls();
		sendSettings();
		timer.start();
		sendInfo();
		server.setRunning(true);
		scoreTimer = new Timer(100, this);
	}

	private void endGame() {
		timer.stop();
		scoreTimer.stop();
		started = false;
		menuOn = true;
		ballsRecievedByClient = false;
		connectionCheckCounter = 0;
		birthCounter = 0;
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
		if(connected) {
			sendInfo();
			sendScore();
		}
		else
			label3.setText("Waiting for Client to connect...");
		//wait before showing menu
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		score = 0;
		parentBallIndex = 0;
		can.remove(this);
		can.add(mainMenuPane);
		menuBar.setVisible(true);
		frame.setTitle("Menu");
		frame.setSize(menuSize);
		//open another server for new game
		if(!connected) {
			new Thread() {
				public void run() {
					try {
						server.close();
						server.reconnect();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}.start();
		}
		//only reached when connected
		frame.setVisible(true);
	}

	public static void showConnected() {
		label3.setText("Connected");
		frame.setVisible(true);
	}

	private void makeMenuPanes() {
		//Main Menu panel
		mainMenuPane = new JPanel();
		JPanel menuPane1 = new JPanel();
		JPanel menuPane2 = new JPanel();
		JPanel menuPane3 = new JPanel();
		JPanel menuPane31 = new JPanel();
		JPanel menuPane32 = new JPanel();
		label = new JLabel("Survival High Score: " + regHighScore);
		label2 = new JLabel("Weave High Score: " + weaveHighScore);
		label3 = new JLabel("Waiting for Client to connect...");
		startButton = new JButton("Start");
		startButton.setBackground(Color.BLUE);
		startButton.setForeground(Color.gray);
		menuOn = true;
		mainMenuPane.setLayout(new BorderLayout());
		menuPane3.setLayout(new BorderLayout());
		menuPane1.add(label);
		menuPane2.add(label2);
		menuPane31.add(label3);
		menuPane32.add(startButton);
		menuPane3.add(menuPane31, BorderLayout.NORTH);
		menuPane3.add(menuPane32, BorderLayout.CENTER);
		mainMenuPane.add(menuPane1, BorderLayout.NORTH);
		mainMenuPane.add(menuPane2, BorderLayout.CENTER);
		mainMenuPane.add(menuPane3, BorderLayout.SOUTH);
		//settings panel
		settingsPane = new JPanel();
		settingsPane.setLayout(new BorderLayout());
		//difficulty
		JPanel difficultyPane = new JPanel();
		JLabel q1 = new JLabel("Difficulty:");
		difficulty = new JTextField("5");
		waveCheck = new JCheckBox("Waves");
		difficultyPane.setLayout(new BorderLayout());
		difficultyPane.add(q1, BorderLayout.NORTH);
		difficultyPane.add(difficulty, BorderLayout.CENTER);
		difficultyPane.add(waveCheck, BorderLayout.SOUTH);
		//weave, bouncy, and wacky
		JPanel weaveBouncySolidWackyPane = new JPanel();
		JPanel solidWackyOpenPane = new JPanel();
		weaveBouncySolidWackyPane.setLayout(new BorderLayout());
		solidWackyOpenPane.setLayout(new BorderLayout());
		weaveCheck = new JCheckBox("Weave");
		bouncyCheck = new JCheckBox("Solid Walls");
		solidCheck = new JCheckBox("Solid Balls");
		wackyCheck = new JCheckBox("Wacky Balls");
		bouncyCheck.setSelected(true);
		weaveBouncySolidWackyPane.add(weaveCheck, BorderLayout.NORTH);
		weaveBouncySolidWackyPane.add(bouncyCheck, BorderLayout.CENTER);
		weaveBouncySolidWackyPane.add(solidWackyOpenPane, BorderLayout.SOUTH);
		solidWackyOpenPane.add(solidCheck, BorderLayout.NORTH);
		solidWackyOpenPane.add(wackyCheck, BorderLayout.CENTER);
		solidWackyOpenPane.setBorder(BorderFactory.createEtchedBorder());
		wackyCheck.addActionListener(this);
		solidCheck.addActionListener(this);
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
		settingsPane.add(weaveBouncySolidWackyPane, BorderLayout.CENTER);
		settingsPane.add(numHairsPane, BorderLayout.SOUTH);
		//credits panel
		creditsPane = new JPanel();
		creditsPane.add(new JLabel("Johnny"));
	}

	private void setWeaveBalls() {
		//set b1 and b2 to 2 random balls
		int i = 0;
		while(balls.get(i).getType() != -1)
			i = (int)((balls.size()) * Math.random());
		int j = i;
		while(i == j || balls.get(i).getType() != -1)
			i = (int)((balls.size()) * Math.random());
		ball1Index = j;
		ball2Index = i;
	}

	private void drawWeaveLine(Graphics2D g2) {
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(Color.red);
		Point p1 = new Point(balls.get(ball1Index).getX() + balls.get(ball1Index).getDiameter() / 2, 
				balls.get(ball1Index).getY() + balls.get(ball1Index).getDiameter() / 2);
		Point p2 = new Point(balls.get(ball2Index).getX() + balls.get(ball2Index).getDiameter() / 2, 
				balls.get(ball2Index).getY() + balls.get(ball2Index).getDiameter() / 2);
		line = new Line2D.Double(p1, p2);
		g2.draw(line);
	}

	private void drawWaveLine(Graphics g) {
		g.setColor(Color.red);
		if(countDown == 1)
			g.fillRect(0, frame.getHeight() / 2 - 22, frame.getWidth(), 60);
		else
			g.drawRect(0, frame.getHeight() / 2 - 22, frame.getWidth(), 60);
	}

	private static boolean isNumeric(String str) { 
		try {  
			Integer.parseInt(str);  
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}

	private void sendSettings() throws IOException {
		//sends weave, bouncy, hairsA, hairsB
		byte[] buf = new byte[5];
		//message type declaration 
		final int SETTINGS = 0;
		buf[0] = SETTINGS;
		buf[1] = (byte) (weave ? 1 : 0);
		buf[2] = (byte) (wave ? 1 : 0);
		buf[3] = (byte) hairs1;
		buf[4] = (byte) hairs2;
		server.sendSettings(buf);
	}

	private void sendScore() {
		//sends score
		byte[] buf = new byte[256];
		//message type declaration
		final int SCORES = 1;
		buf[0] = SCORES;
		int x = score;
		int index = 1;
		while(x != 0) {
			buf[index] = (byte) (x < 255 ? x : 255);
			x -= 255;
			if(x <= 0)
				x = 0;
			index ++;
		}
		try {
			server.sendScores(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendBalls() {
		//sends ball locations
		//In buf: message  type, ball1, ball2(index), then (6)(x, y), and int type for all balls.
		//max # of balls: 20?
		byte[] buf = new byte[500];
		final int BALLS = 1;
		buf[0] = BALLS;
		buf[1] = (byte) ball1Index;
		buf[2] = (byte) ball2Index;
		int ballIndex = 0;
		for(int i = 3; i < balls.size() * 13 - 1; i += 13) {
			int x = balls.get(ballIndex).getX() + 55;
			int y = balls.get(ballIndex).getY() + 55;
			for(int j = i; j < i + 13; j ++) {
				if(j < i + 6) {
					buf[j] = (byte) (x < 255 ? x : 255);
					x -= 255;
					if(x < 0)
						x = 0;
				}
				else if(j < i + 12) {
					buf[j] = (byte) (y < 255 ? y : 255);
					y -= 255;
					if(y < 0)
						y = 0;
				}
				else
					buf[j] = (byte)(balls.get(ballIndex).getType());
			}
			ballIndex ++;
		}
		server.setBallInfo(buf);
	}

	private void sendInfo() {
		//first 20 in buf are message type, (3)( (user) x/4, y/4, (frame) width, height ), 
		//userA hairWave, userB hairWave, shielded, shrink, speed, pause, menu.
		//XXX send location of all players
		byte[] buf = new byte[256];
		int spotsBeforeScore = 32;
		final int INFO = 2;
		buf[0] = INFO;
		//things requiring 6 spots
		int bigThings = 4;
		for(int k = 1; k < bigThings + 1; k ++) {
			int x = 0;
			if(k == 1)
				x = (int) userA.getLocation().getX();
			else if(k == 2)
				x = (int) userA.getLocation().getY();
			else if(k == 3)
				x = frame.getWidth();
			else if(k == 4)
				x = frame.getHeight();
			for(int j = (k - 1) * 6 + 1; j < (k - 1) * 6 + 1 + 6; j ++) {
				buf[j] = (byte) (x < 255 ? x: 255);
				x -= 255;
				if(x < 0)
					x = 0;
			}
		}
		//boolean hair waves
		buf[bigThings * 6 + 1] = (byte) userA.ishairWave();
		buf[bigThings * 6 + 2] = (byte) userB.ishairWave();
		//boolean shielded, shrink, speed
		buf[bigThings * 6 + 3] = (byte) (shielded ? 1 : 0);
		buf[bigThings * 6 + 4] = (byte) (shrinkCounter > 0 ? 1 : 0);
		buf[bigThings * 6 + 5] = (byte) (speedCounter > 1 ? 1 : 0);
		buf[bigThings * 6 + 6] = (byte) (countDown);
		//pause, menu
		buf[bigThings * 6 + 7] = (byte) (pause ? 1 : 0);
		//0 and 1 so that a forced 0 doesn't end menu for client (closing server)
		buf[bigThings * 6 + 8] = (byte) (menuOn ? 0 : 1);
		//score
		int x = score;
		int index = spotsBeforeScore + 1;
		while(x != 0) {
			buf[index] = (byte) (x < 255 ? x : 255);
			x -= 255;
			if(x <= 0) {
				x = 0;
			}
			index ++;
		}
		server.setInfo(buf);
	}

	public static void storeLocationInfo(byte[] buf) {
		ballsRecievedByClient = true;
		//stops sending when client replies from menuOn
		if(buf[0] == 1) {
			server.setRunning(false);
		}
		//sets location of userB
		int x = 0;
		int y;
		int l = 0;
		//length: 7
		for(int i = 1; i < buf.length; i ++) {
			l += buf[i] < 0 ? 256 + buf[i]: buf[i];
			if(i == 2) {
				x = l;
				l = 0;
			}
		}
		y = l;
		userB.setLocation(x, y);
	}

	public static void connectionCheck() {
		connectionCheckCounter = 0;
	}

	public void mouseDragged(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public static void main(String[] args) throws IOException {
		new CourtServer();
		int port = 4446;
		server = new Server(port);
		try {
			server.doStart();
		} catch (IOException e) {
			e.printStackTrace();
		}
		label3.setText("Connected");
		startButton.setForeground(Color.black);
		server.run();
	}
}
