import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;


public class SinglePlayerServer {
	public HashSet dictionary;
	public String file = "2of4brif.txt";
	
	public static void main (String [] args){
			ListenerThread clientReceiver = new ListenerThread(new SinglePlayerServer());
			clientReceiver.start();
	}
	
	public void populate(){
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
	public SinglePlayerServer(){
		super();
		this.dictionary = new HashSet();
	}
}

// Connection handler thread
class ListenerThread extends Thread {
	public SinglePlayerServer launcher;
	public ServerSocket server;
	public Socket newGameSocket;
	public SinglePlayerGameInstance newGame;
	
	
	public ListenerThread(SinglePlayerServer launcher) {
		this.launcher = launcher;
	}


	public void run(){
		launcher.populate();
		try {
			server = new ServerSocket(8128);
			while(true){
				Thread.sleep(50);
				newGameSocket = server.accept();
				newGame = new SinglePlayerGameInstance(launcher.dictionary,newGameSocket);
				System.out.println("Launched SinglePlayerGameInstance");
				newGame.run();
				System.out.println("Run SinglePlayerGameInstance");
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




