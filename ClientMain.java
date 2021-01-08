package DodgeBallClient;

import java.awt.event.*;
import java.awt.geom.*;
import java.io.IOException;
import java.awt.*;
import javax.swing.*;

public class ClientMain implements KeyListener, ActionListener{
	private static Panel pane;
	private static JFrame frame;
	private static JPanel menuPane;
	private static JPanel connectPane;
	private static JLabel score;
	private static JLabel regHighScore;
	private static JLabel weaveHighScore;
	private static JLabel label;
	private static JTextField ipField;
	private static JButton button;
	private static int maxBalls = 71 * 3;
	private static int[] ballInfo = new int[maxBalls];
	private static Timer t;
	private static ClientUserBall userA;
	private static ClientUserBall userYou;
	private static Line2D line;
	private static int width = 300;
	private static int highScore = 0;
	private static int wHighScore;
	private static int ball1Index;
	private static int ball2Index;
	private static int hairs1;
	private static int hairs2;
	private static int countDown = 5;
	public static final int NORMAL = -1;
	public static final int BONUS = 1;
	public static final int SHIELD = 2;
	public static final int SLOMO = 3;
	public static final int SHRINK = 4;
	public static final int SPEED = 5;
	private static boolean pause;
	private static boolean menuOn = true;
	private static boolean weave;
	private static boolean wave;
	private static boolean hairWaveA;
	private static boolean hairWaveYou;
	private static boolean speed;
	private static boolean shielded;
	private static boolean shrink;
	private boolean up;
	private boolean down;
	private boolean left;
	private boolean right;

	public static byte[] sendInfo() {
		//sends boolean received menuOn, userYou location to server
		byte[] buf = new byte[7];
		//sends notification that menuOn is received
		if(menuOn) {
			buf[0] = 1;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int x = (int) userYou.getLocation().getX();
		int y = (int) userYou.getLocation().getY();
		int l = x;
		//length: 7
		for(int j = 1; j < buf.length; j ++) {
			if(j == 4)
				l = y;
			buf[j] = (byte) (l < 255 ? l : 255);
			l -= 255;
			if(l < 0)
				l = 0;
		}
		return buf;
	}

	public static void storeMenuInfo(byte[] buf) {
		final int SETTINGS = 0;
		final int SCORES = 1;
		if(buf[0] == SETTINGS) {
			weave = buf[1] == 1;
			wave = buf[2] == 1;
			hairs1 = buf[3];
			hairs2 = buf[4];
			startGame();
		}
		else if(buf[0] == SCORES) {
			storeScore(buf);
		}
	}

	private static void storeScore(byte[] buf) {
		int index = 1;
		while(buf[index] != 0) {
			index ++;
		}
		int thisScore = bytesToInt(buf, 1, index);
		score.setText("Score: " + thisScore);
		if(!weave) {
			if(thisScore > highScore)
				highScore = thisScore;
		}
		else {
			if(thisScore > wHighScore)
				wHighScore = thisScore;
		}
		regHighScore.setText("Survival High Score: " + highScore);
		weaveHighScore.setText("Weave High Score: " + wHighScore);
		//go to menu after scores received
		frame.setTitle("Menu");
		goToMenu();
	}

	public static void storeInfo(byte[] buf) {
		final int BALLS = 1;
		final int INFO = 2;
		if(buf[0] == BALLS) {
			ball1Index = buf[1];
			ball2Index = buf[2];
			int ballIndex = 0;
			//refresh balls
			ballInfo = new int[maxBalls];
			//store x, y, type
			for(int i = 3; i < buf.length - 13 && buf[i + 12] != 0; i += 13) {
				ballInfo[ballIndex] = bytesToInt(buf, i) - 55;
				ballInfo[ballIndex + 1] = bytesToInt(buf, i + 6) - 55;
				ballInfo[ballIndex + 2] = buf[i + 12];
				ballIndex += 3;
			}
		}
		else if(buf[0] == INFO) {
			storeExtraInfo(buf);
		}
	}

	//indexes
	private final static int xInd = 1;
	private final static int yInd = 7;
	private final static int wInd = 13;
	private final static int hInd = 19;
	private final static int waveAInd = 25;
	private final static int waveYouInd = 26;
	private final static int shieldInd = 27;
	private final static int shrinkInd = 28;
	private final static int speedInd = 29;
	private final static int countDownInd = 30;
	private final static int pauseInd = 31;
	private final static int menuInd = 32;
	private final static int scoreInd = 33;

	private static void storeExtraInfo(byte[] buf) {
		int x = bytesToInt(buf, xInd);
		int y = bytesToInt(buf, yInd);
		userA.setLocation(x, y);
		int width = bytesToInt(buf, wInd);
		int height = bytesToInt(buf, hInd);
		frame.setSize(width, height);
		hairWaveA = buf[waveAInd] == 1;
		hairWaveYou = buf[waveYouInd] == 1;
		shielded = buf[shieldInd] == 1;
		shrink = buf[shrinkInd] == 1;
		speed = buf[speedInd] == 1;
		countDown = buf[countDownInd];
		pause = buf[pauseInd] == 1;
		//menuOn true when 0
		menuOn = buf[menuInd] == 0;
		if(menuOn) {
			Client.setPlaying(false);
			t.stop();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		//scores
		int index = scoreInd;
		while(buf[index] != 0) {
			index ++;
		}
		frame.setTitle("Score: " + bytesToInt(buf, scoreInd, index));
	}

	public ClientMain() {
		pane = new Panel();
		frame = new JFrame("Menu");
		Container can = frame.getContentPane();
		menuPane = new JPanel();
		can.add(pane);
		pane.add(menuPane);
		makeMenu();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.addKeyListener(this);
		userA = new ClientUserBall(width / 2 + 30, width / 2 - 22, new Color(241, 194, 125), pane, hairs1);
		userYou = new ClientUserBall(width / 2 - 30, width / 2 - 22, new Color(198, 134, 66), pane, hairs2);
		t = new Timer(15, this);
	}
	//for Panel class to access ClientMain methods without starting
	public ClientMain(int doesntMatter){}

	public static void paintStuff(Graphics g) {
		if(!menuOn) {
			if(wave) {
				drawWaveLine(g);
				g.setFont(new Font("countDown Font", Font.BOLD, 30));
				g.drawString("" +countDown, frame.getWidth() / 2, frame.getHeight() / 2 - 22 - 30);
			}
			drawBalls(g);
			if(shielded) {
				userA.drawShield(g);
				userYou.drawShield(g);
			}
			userA.drawUser(g, shielded);
			userYou.drawUser(g, shielded);
			if(weave) {
				drawWeaveLine((Graphics2D)g);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button) {
			new Thread() {
				public void run() {
					try {
						Client.startConnection(ipField.getText());
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
			connectPane.removeAll();
			connectPane.add(new JLabel("Connecting..."), BorderLayout.NORTH);
			frame.setVisible(true);
		}
		else {
			if(!pause) {
				if(up)
					userYou.moveUp();
				else if(down)
					userYou.moveDown();
				if(left)
					userYou.moveLeft();
				else if(right) 
					userYou.moveRight();
			}
			if(shrink)
				ClientUserBall.diameter = ClientUserBall.SMALL;
			else
				ClientUserBall.diameter = ClientUserBall.NORMAL_DIAMETER;
			if(speed)
				ClientUserBall.speed = ClientUserBall.FAST;
			else
				ClientUserBall.speed = ClientUserBall.NORMAL_SPEED;
			userA.setHairWave(hairWaveA);
			userYou.setHairWave(hairWaveYou);
			pane.repaint();
		}
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		//if moved
		if(key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT ||
				key == KeyEvent.VK_RIGHT) {
			//when moved first time
			if(key == KeyEvent.VK_UP)
				up = true;
			else if(key == KeyEvent.VK_DOWN)
				down = true;
			else if(key == KeyEvent.VK_LEFT)
				left = true;
			else if(key == KeyEvent.VK_RIGHT)
				right = true;
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

	private static void drawWeaveLine(Graphics2D g2) {
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(Color.red);
		Point p1 = new Point(ballInfo[ball1Index * 3] + 30, ballInfo[ball1Index * 3 + 1] + 30);
		Point p2 = new Point(ballInfo[ball2Index * 3] + 30, ballInfo[ball2Index * 3 + 1] + 30);
		line = new Line2D.Double(p1, p2);
		g2.draw(line);
	}

	private static void drawWaveLine(Graphics g) {
		g.setColor(Color.red);
		if(countDown == 1)
			g.fillRect(0, frame.getHeight() / 2 - 22, frame.getWidth(), 60);
		else
			g.drawRect(0, frame.getHeight() / 2 - 22, frame.getWidth(), 60);
	}

	private static int bytesToInt(byte[] buf, int startIndex) {
		return bytesToInt(buf, startIndex, startIndex + 6);
	}

	private static int bytesToInt(byte[] buf, int startIndex, int endIndex) {
		int x = 0;
		for(int i = startIndex; i < endIndex; i ++) {
			if(buf[i] < 0)
				x += 256 + buf[i];
			else
				x += buf[i];
		}
		return x;
	}

	private static void drawBalls(Graphics g) {
		int diam = 55;
		for(int i = 0; i < ballInfo.length - 2; i += 3) {
			int x = ballInfo[i];
			int y = ballInfo[i + 1];
			int type = ballInfo[i + 2];
			if(type == NORMAL) {
				g.setColor(Color.red);
				g.fillOval(x, y, diam, diam);
			}
			else {
				if(type == SHIELD) {
					g.setColor(Color.black);
					g.fillOval(x, y, diam, diam);
					g.setColor(Color.magenta);
					int diam2 = diam * 4 / 5;
					g.fillOval(x + (diam - diam2) / 2, y + (diam - diam2) / 2, diam2, diam2);
				}
				else if(type == BONUS) {
					g.setColor(Color.red);
					g.fillArc(x, y, diam, diam, 0, 60);
					g.setColor(Color.orange);
					g.fillArc(x, y, diam, diam, 60, 60);
					g.setColor(Color.YELLOW);
					g.fillArc(x, y, diam, diam, 120, 60);
					g.setColor(Color.GREEN);
					g.fillArc(x, y, diam, diam, 180, 60);
					g.setColor(Color.BLUE);
					g.fillArc(x, y, diam, diam, 240, 60);
					g.setColor(Color.magenta);
					g.fillArc(x, y, diam, diam, 300, 60);
				}
				else if(type == SLOMO) {
					g.setColor(Color.gray);
					g.fillOval(x, y, diam, diam);
					int x1 = x + diam / 2;
					int y1 = y + diam / 2;
					g.setColor(Color.black);
					g.drawLine(x1, y1, x1, y1 - diam * 6/7 / 2);
					g.drawLine(x1, y1, x1 - diam * 4/7 / 2, y1);
				}
				else if(type == SHRINK) {
					diam = 40;
					g.setColor(Color.PINK);
					g.fillOval(x, y, diam, diam);
				}
				else if(type == SPEED) {
					g.setColor(Color.yellow);
					g.fillOval(x, y, diam, diam);
				}
			}	
		}
	}

	private void makeMenu() {
		menuPane.setLayout(new BorderLayout());
		JPanel scoresPane = new JPanel();
		connectPane = new JPanel();
		score = new JLabel("Score: 0");
		regHighScore = new JLabel("Survival High Score: 0");
		weaveHighScore = new JLabel("WeaveHighScore: 0");
		label = new JLabel("Enter ip address of host:");
		ipField = new JTextField();
		button = new JButton("Connect");
		button.addActionListener(this);
		scoresPane.setLayout(new BorderLayout());
		connectPane.setLayout(new BorderLayout());

		scoresPane.add(score, BorderLayout.NORTH);
		scoresPane.add(regHighScore, BorderLayout.CENTER);
		scoresPane.add(weaveHighScore, BorderLayout.SOUTH);

		connectPane.add(label, BorderLayout.NORTH);
		connectPane.add(ipField, BorderLayout.CENTER);
		connectPane.add(button, BorderLayout.SOUTH);

		menuPane.add(scoresPane, BorderLayout.NORTH);
		//to make a space
		menuPane.add(new JTextArea(6, 0) {{this.setOpaque(false); this.setEnabled(false);}}, BorderLayout.CENTER);
		menuPane.add(connectPane, BorderLayout.SOUTH);

		menuPane.setBackground(null);
		frame.setSize(width, width);
		frame.setVisible(true);
	}

	private static void goToMenu() {
		menuPane.setVisible(true);
		frame.setSize(width, width);
		frame.setVisible(true);
		pane.setBackground(null);
	}

	private static void startGame() {
		userA.setHairs(hairs1);
		userYou.setHairs(hairs2);
		menuPane.setVisible(false);
		pane.setBackground(Color.white);
		menuOn = false;
		t.start();
	}

	public static void showConnected(boolean connected) {
		connectPane.removeAll();
		if(connected) {
			connectPane.add(new JLabel("Connected"), BorderLayout.NORTH);
			connectPane.add(new JLabel("Waiting for Host To start game..."), BorderLayout.CENTER);
		}
		else {
			connectPane.add(new JLabel("Incorrect ip"), BorderLayout.NORTH);
			connectPane.add(ipField, BorderLayout.CENTER);
			connectPane.add(button, BorderLayout.SOUTH);
		}
		frame.setVisible(true);
	}

	public static void disconnectToMenu() {
		t.stop();
		goToMenu();
		connectPane.removeAll();
		connectPane.add(new JLabel("Host Disconnected. Enter new ip address:"), BorderLayout.NORTH);
		connectPane.add(ipField, BorderLayout.CENTER);
		connectPane.add(button, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	public void mouseDragged(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public static void main(String[] peepeepoopoo) {
		new ClientMain();
		//start path: button press-> run-> storeMenuInfo-> start
		//every storeMenuInfo tells clientMain to start again
	}
}