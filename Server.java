
package dodgeBall;

import java.io.*;
import java.net.*;

public class Server extends Thread{
	protected DatagramSocket socket = null;
	private byte[] ballLocs = new byte[500];
	private byte[] info = new byte[256];
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataOutputStream out;
	private DataInputStream in;
	private int port;
	private boolean running;

	public Server(int port) throws SocketException {
		this("Server", port);
	}
	private Server(String name, int port) throws SocketException {
		super(name);
		try {
			socket = new DatagramSocket(port);
		}
		catch(BindException e) {
			System.out.println("You started the server twice again you dingus");
		}
		this.port = port;
	}
	//udp
	public void run() {
		while(true) {
			//wait until running
			while(!running) {
				System.out.print("");//won't work without something to do oddly
			}
			while(running) {
				try {
					byte[] buf = new byte[256];
					//packet for receiving requests
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					CourtServer.connectionCheck();
					//fills packet with request info from socket
					socket.receive(packet);
					CourtServer.storeLocationInfo(packet.getData());
					//use info from packet to respond
					InetAddress address = packet.getAddress();
					int port = packet.getPort();
					buf = ballLocs;
					packet = new DatagramPacket(buf, buf.length, address, port);
					socket.send(packet);
					buf = info;
					packet = new DatagramPacket(buf, buf.length, address, port);
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//tcp
	private void start(int whatever) throws IOException  {
		try {
			serverSocket = new ServerSocket(this.port);
			clientSocket = serverSocket.accept();
			CourtServer.connected = true;
			out = new DataOutputStream(clientSocket.getOutputStream());
			in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			//use while ((input = in.read()) != -1) if communicating repeatedly
		}
		catch(BindException e) {
			CourtServer.connected = false;
		}
	}

	public void doStart() throws IOException {
		start(4446);
	}

	private boolean reconnectTry() throws IOException {
		try {
			serverSocket = new ServerSocket(this.port);
			clientSocket = serverSocket.accept();
			out = new DataOutputStream(clientSocket.getOutputStream());
			in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			CourtServer.connected = true;
			CourtServer.showConnected();
			return true;
		}
		catch(BindException e) {
			CourtServer.connected = false;
			return false;
		}
	}

	public void reconnect() throws IOException {
		while(!reconnectTry()) {};
	}

	public void close() throws IOException {
		serverSocket.close();
		clientSocket.close();
		out.close();
		in.close();
	}

	public void sendSettings(byte[] settings) throws IOException {
		try {
			out.write(settings);
			
		}
		catch(NullPointerException e) {
			System.out.println("ERROR: incorrect ip address probably");
		}

	}

	public void sendScores(byte[] scores) throws IOException {
		//sends weaveHighScore and score
		try {
			out.write(scores);
		}
		catch(SocketException e) {
			CourtServer.connected = false;
		}
	}

	public void setBallInfo(byte[] buf) {
		//balls locations
		ballLocs = buf;
	}

	public void setInfo(byte[] buf) {
		//sends you, width, hairWaves, shielded, shrink, speed, countDown, pause, menu, and score
		info = buf;
	}

	public void setRunning(boolean run) {
		running = run;
	}
}