import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class SinglePlayerServer {
	public HashMap dictionary;
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
				this.dictionary.put(buffRead.readLine(),new Object());
	        }
		 } catch(FileNotFoundException e){
	         e.printStackTrace();
	     } catch (IOException e) {
	         e.printStackTrace();
	     }
	}
	public SinglePlayerServer(){
		super();
		this.dictionary = new HashMap();
	}
}

// Connection handler thread
class ListenerThread extends Thread {
	public SinglePlayerServer launcher;
	public ServerSocket server;
	
	
	public ListenerThread(SinglePlayerServer launcher) {
		this.launcher = launcher;
	}


	public void run(){
		launcher.populate();
		try {
			server = new ServerSocket(8128);
			while(true){
				Thread.sleep(50);
				new SinglePlayerGameInstance(launcher.dictionary,server.accept()).run();
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




