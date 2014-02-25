<<<<<<< HEAD
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;



public class GameServer {
	public Vector <PlayerPair> clientList = new Vector <PlayerPair>();
	public int byteNum1;
	public int byteNum2;
	public String newMessage = "";
	public ServerSocket server;
	@SuppressWarnings("rawtypes")
	public HashMap dictionary = new HashMap <String,Object>();
	public String file = "2of4brif.txt";
	
	public GameServer(){
		System.out.println((int)'\n');
		populate();
		try {
			server = new ServerSocket(8128);
			System.out.println(server.getInetAddress());
			ListenThread clientReceiver = new ListenThread(this);
			clientReceiver.start();
		}
			
		catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (clientList.size()==0 || clientList.size()==1){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while (true){
			for (int i=0; i<clientList.size()-1; i++){
				try {
					InputStream input1 = clientList.get(i).playerOne.getInputStream();
					byteNum1 = input1.available();
					byte[] receivedByte1 = new byte[byteNum1];
					while (byteNum1>0){
						input1.read(receivedByte1);
						newMessage = (new String (receivedByte1));
						System.out.println("Here. "+(int)(newMessage.charAt(newMessage.length()-1)));
						if(newMessage.charAt(newMessage.length()-1)=='\n')
							newMessage = newMessage.substring(0,newMessage.length()-1);
						System.out.println(newMessage);
						// Validate client message is a word.
						if (dictionary.containsKey(newMessage))
							sendMessage(newMessage + " ONE VALID\n",i);
						else
							sendMessage(newMessage + " ONE INVALID\n", i);
						byteNum1 = 0;
					}
					InputStream input2 = clientList.get(i).playerTwo.getInputStream();
					byteNum2 = input2.available();

					byte[] receivedByte2 = new byte[byteNum2];
					while (byteNum2>0){
						input2.read(receivedByte2);
						newMessage = (new String (receivedByte2));
						System.out.println("Here too. "+(int)(newMessage.charAt(newMessage.length()-1)));
						if(newMessage.charAt(newMessage.length()-1)=='\n')
							newMessage = newMessage.substring(0,newMessage.length()-1);
						System.out.println(newMessage);
						if (dictionary.containsKey(newMessage))
							sendMessage(newMessage + " TWO VALID\n",i);
						else
							sendMessage(newMessage + " TWO INVALID\n", i);
						byteNum2 = 0;
					}
				} 
				catch (IOException e) {
					e.printStackTrace();
					}

			}
		}

	}
	@SuppressWarnings("unchecked")
	public void populate(){
		int totalCount;
		try{
	        FileReader reader = new FileReader(file);
	        BufferedReader buffRead = new BufferedReader (reader);
	        totalCount = (int) Integer.parseInt(buffRead.readLine());
	        for (int i =1; i< totalCount; i++) {
				dictionary.put(buffRead.readLine(),new Object());
	        }
		 } catch(FileNotFoundException e){
	         e.printStackTrace();
	     } catch (IOException e) {
	         e.printStackTrace();
	     }
	}
	
	public void sendMessage(String message, int playerPair){
		OutputStream output;
		System.out.println(message);
			try {
				output = clientList.get(playerPair).playerOne.getOutputStream();
				output.write(message.getBytes());
				output = clientList.get(playerPair).playerTwo.getOutputStream();
				output.write(message.getBytes());
			} catch (IOException e) {
				//TODO handle quits better
				String saveMessage = new String (message);
				clientList.remove(playerPair);
				System.out.println(newMessage);
			}

	}


	public static void main (String [] args){
		new GameServer();
	}

}
class ListenThread extends Thread{
	public GameServer cServer;
	public ListenThread (GameServer server){
		super();
		cServer = server;
	}
	
	public void run(){
		
		while(true){
			try {
				PlayerPair newPair = new PlayerPair();
				cServer.clientList.add(newPair);
				newPair.addOne(cServer.server.accept());
				newPair.addTwo(cServer.server.accept());
				String userInets = cServer.clientList.get(cServer.clientList.size()-1).playerOne.getInetAddress().toString();
				userInets = userInets+cServer.clientList.get(cServer.clientList.size()-1).playerTwo.getInetAddress().toString();
				System.out.println("New Game started: "+userInets);
				}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

class PlayerPair {
	public Socket playerOne;
	public Socket playerTwo;
	
	public PlayerPair () {
	}
	public void addOne(Socket onePlayer){
		this.playerOne = onePlayer; 
	}

	public void addTwo(Socket onePlayer){
		this.playerTwo = onePlayer; 
	}
	
	public PlayerPair (Socket onePlayer, Socket twoPlayer){
		this.playerOne = onePlayer;
		this.playerTwo = twoPlayer;
	}
}
=======
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;



public class GameServer {
	public Vector <PlayerPair> clientList = new Vector <PlayerPair>();
	public int byteNum1;
	public int byteNum2;
	public String newMessage = "";
	public ServerSocket server;
	@SuppressWarnings("rawtypes")
	public HashMap dictionary = new HashMap <String,Object>();
	public String file = "2of4brif.txt";
	
	public GameServer(){
		System.out.println((int)'\n');
		populate();
		try {
			server = new ServerSocket(8128);
			System.out.println(server.getInetAddress());
			ListenThread clientReceiver = new ListenThread(this);
			clientReceiver.start();
		}
			
		catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (clientList.size()==0 || clientList.size()==1){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while (true){
			for (int i=0; i<clientList.size()-1; i++){
				try {
					InputStream input1 = clientList.get(i).playerOne.getInputStream();
					byteNum1 = input1.available();
					byte[] receivedByte1 = new byte[byteNum1];
					while (byteNum1>0){
						input1.read(receivedByte1);
						newMessage = (new String (receivedByte1));
						System.out.println("Here. "+(int)(newMessage.charAt(newMessage.length()-1)));
						if(newMessage.charAt(newMessage.length()-1)=='\n')
							newMessage = newMessage.substring(0,newMessage.length()-1);
						System.out.println(newMessage);
						// Validate client message is a word.
						if (dictionary.containsKey(newMessage))
							sendMessage(newMessage + " ONE VALID",i);
						else
							sendMessage(newMessage + " ONE INVALID", i);
						byteNum1 = 0;
					}
					InputStream input2 = clientList.get(i).playerTwo.getInputStream();
					byteNum2 = input2.available();

					byte[] receivedByte2 = new byte[byteNum2];
					while (byteNum2>0){
						input2.read(receivedByte2);
						newMessage = (new String (receivedByte2));
						System.out.println("Here too. "+(int)(newMessage.charAt(newMessage.length()-1)));
						if(newMessage.charAt(newMessage.length()-1)=='\n')
							newMessage = newMessage.substring(0,newMessage.length()-1);
						System.out.println(newMessage);
						if (dictionary.containsKey(newMessage))
							sendMessage(newMessage + " TWO VALID",i);
						else
							sendMessage(newMessage + " TWO INVALID", i);
						byteNum2 = 0;
					}
				} 
				catch (IOException e) {
					e.printStackTrace();
					}

			}
		}

	}
	@SuppressWarnings("unchecked")
	public void populate(){
		int totalCount;
		try{
	        FileReader reader = new FileReader(file);
	        BufferedReader buffRead = new BufferedReader (reader);
	        totalCount = (int) Integer.parseInt(buffRead.readLine());
	        for (int i =1; i< totalCount; i++) {
				dictionary.put(buffRead.readLine(),new Object());
	        }
		 } catch(FileNotFoundException e){
	         e.printStackTrace();
	     } catch (IOException e) {
	         e.printStackTrace();
	     }
	}
	
	public void sendMessage(String message, int playerPair){
		OutputStream output;
		System.out.println(message);
			try {
				output = clientList.get(playerPair).playerOne.getOutputStream();
				output.write(message.getBytes());
				output = clientList.get(playerPair).playerTwo.getOutputStream();
				output.write(message.getBytes());
			} catch (IOException e) {
				//TODO handle quits better
				String saveMessage = new String (message);
				clientList.remove(playerPair);
				System.out.println(newMessage);
			}

	}


	public static void main (String [] args){
		new GameServer();
	}

}
class ListenThread extends Thread{
	public GameServer cServer;
	public ListenThread (GameServer server){
		super();
		cServer = server;
	}
	
	public void run(){
		
		while(true){
			try {
				PlayerPair newPair = new PlayerPair();
				cServer.clientList.add(newPair);
				newPair.addOne(cServer.server.accept());
				newPair.addTwo(cServer.server.accept());
				String userInets = cServer.clientList.get(cServer.clientList.size()-1).playerOne.getInetAddress().toString();
				userInets = userInets+cServer.clientList.get(cServer.clientList.size()-1).playerTwo.getInetAddress().toString();
				System.out.println("New Game started: "+userInets);
				}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

class PlayerPair {
	public Socket playerOne;
	public Socket playerTwo;
	
	public PlayerPair () {
	}
	public void addOne(Socket onePlayer){
		this.playerOne = onePlayer; 
	}

	public void addTwo(Socket onePlayer){
		this.playerTwo = onePlayer; 
	}
	
	public PlayerPair (Socket onePlayer, Socket twoPlayer){
		this.playerOne = onePlayer;
		this.playerTwo = twoPlayer;
	}
}
>>>>>>> 7d4d01acb63549918c7de7268faa80104dfb3c59
