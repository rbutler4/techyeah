import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class SinglePlayerServer {
	public HashMap dictionary;
	public String file;
	
	public void main (String [] args){
		if (args.length == 1){
			file = args[0];
			populate();
			ListenerThread clientReceiver = new ListenerThread(this);
			clientReceiver.start();
		}
			
		else {
			System.out.println("Requires dictionary filename");
		}
	}
	
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
}

// Connection handler thread
class ListenerThread extends Thread {
	public SinglePlayerServer launcher;
	public ServerSocket server;
	
	
	public ListenerThread(SinglePlayerServer launcher) {
		this.launcher = launcher;
	}


	public void run(){
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




