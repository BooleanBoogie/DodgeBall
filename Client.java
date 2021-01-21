
package dodgeBallClient;

import java.io.*;
import java.net.*;

public class Client {
	private static byte[] locationInfo;
	private static byte[] menuInfo = new byte[256];
	private static int port = 4446;
	private static String ip;
	private static Socket clientSocket;
	private static DataInputStream in;
	private static boolean playing = true;
	public static boolean firstTime = true;
	private static boolean connected = false;

	//Label:starts tcp connection, then runs udp
	public static void startConnection(String tryip) throws IOException, InterruptedException {
		//default ip (school)
		if(tryip == null || tryip.length() == 0)
			tryip = "10.21.18.38";
		ip = tryip;
		try {
			clientSocket = new Socket(ip, port);
			in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			ClientMain.showConnected(true);
			connected = true;
			run();
		} catch(ConnectException | NoRouteToHostException | UnknownHostException e){
			ClientMain.showConnected(false);
		}
	}

	//Label:gets menu info from server, stores in clientMain
	public static void getMenuInfo() throws IOException {
		//scores and settings
		in.read(menuInfo);
		ClientMain.storeMenuInfo(menuInfo);
		if(!firstTime) {
			in.read(menuInfo);
			ClientMain.storeMenuInfo(menuInfo);
		}
	}

	//Label: stores location info from clientmain
	public static void setLocationInfo(byte[] buf) {
		locationInfo = buf;
	}

	//Label: sets playing to not notMenuOn
	public static void setPlaying(boolean notMenuOn) {
		playing = notMenuOn;
	}

	//Label:runs udp, if done then tcp and rerun
	public static void run() throws IOException, InterruptedException {
		firstTime = true;
		getMenuInfo();
		DatagramSocket socket;
		playing = true;
		//while game running
		while(connected) {
			socket = new DatagramSocket();
			socket.setSoTimeout(200);
			//while game playing
			while(playing) {
				byte[] buf = ClientMain.sendInfo();
				InetAddress address = InetAddress.getByName(ip);
				//send request
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
				socket.send(packet);
				//receive response
				buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				//reset connectionCheckCounter
				if(playing) {
					try {
						socket.receive(packet);
						ClientMain.storeInfo(packet.getData());
					}
					catch(SocketTimeoutException e) {
						//XXX delete exception? timeout is ok to happen
						connected = false;
						playing = false;
						ClientMain.disconnectToMenu();
					}
				}
			}
			if(connected) {
				//clears all old messages
				socket.close();
				//in menu
				firstTime = false;
				getMenuInfo();
				playing = true;
			}
		}
	}
}
