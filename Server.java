import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;


public class Server {
	public HashSet dictionary;
	public String file = "2of4brif.txt";
	
	public static void main (String [] args){
			System.out.println("Creating client listener thread...");
			ListenerThread clientReceiver = new ListenerThread(new Server());
			clientReceiver.start();
			System.out.println("Server listening for clients...");
	}
	
	/**
	* 	Reads lines from dictionary text file and populates hash set
	*	@param none
	*	@param none
	*/
	public void populate(){
		System.out.println("populate method called...loading .txt words to dictionary");
		int totalCount;
		try{
	        FileReader reader = new FileReader(file);
	        BufferedReader buffRead = new BufferedReader (reader);
	        totalCount = (int) Integer.parseInt(buffRead.readLine());
	        for (int i =1; i< totalCount; i++) {
				this.dictionary.add(buffRead.readLine());
	        }
		 } catch(FileNotFoundException e){
	         e.printStackTrace();
	     } catch (IOException e) {
	         e.printStackTrace();
	     }
	}

	//Constructor
	public Server(){
		super();
		this.dictionary = new HashSet();
	}
}

/** 
* Connection handler thread. Sets up new game on server side.
* 
*/
class ListenerThread extends Thread {
	public Server launcher;
	public ServerSocket server;
	public Socket newGameSocket;
	public ServerInstance newGame;
	
	
	public ListenerThread(Server launcher) {
		this.launcher = launcher;
	}


	public void run(){
		launcher.populate();
		try {
			System.out.println("Running thread...creating game");
			server = new ServerSocket(5000);
			System.out.println("Server listening on port 5000");
			while(true){
				Thread.sleep(50);
				newGameSocket = server.accept();
				newGame = new ServerInstance(launcher.dictionary,newGameSocket);
				System.out.println("Launched ServerInstance");
				newGame.run();
				System.out.println("Run ServerInstance (new game)");
			}
		} catch (IOException e) {
			System.err.println("Something went wrong. Maybe the port is blocked? (8128)");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Socket Listener Sleep interrupted");
			e.printStackTrace();
		}
		
	}
}
