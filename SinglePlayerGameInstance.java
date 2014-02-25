import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

// Individual games are handled as threads by the server.
public class SinglePlayerGameInstance {
	private static final Integer MAX_WALL_HEIGHT = 9;
	public boolean exit = false;
	public final HashMap dictionary;
	public Socket player;
	public InputStream fromPlayer;
	public OutputStream toPlayer;
	public int byteNum;
	public long timeStamp1, timeStamp2;
	public String gameMessage;
	public Integer playerScore = 0;
	public Integer wallHeight = 0;
	
	
	public SinglePlayerGameInstance(HashMap dictionary, Socket player){
		this.dictionary = dictionary;
		this.player = player;
	}

	public void run(){
		timeStamp1 = timeStamp2 = System.currentTimeMillis();
		sendMessage("Connected to server");
		
		while (!exit){
			while (timeStamp2 - timeStamp1 < 20000){
				try{
					Thread.sleep(10);
					InputStream fromPlayer = player.getInputStream();
					byteNum = fromPlayer.available();
					byte[] receivedByte = new byte[byteNum];
					while(byteNum >0){
						fromPlayer.read(receivedByte);
						gameMessage = updateStatus(new String(receivedByte));
						sendMessage(gameMessage);
					}
					if (wallHeight >=SinglePlayerGameInstance.MAX_WALL_HEIGHT){
						sendMessage("endGame "+playerScore.toString()+" 0");
						exit = true;
					}
					
				}
				catch (IOException e){
					System.err.println("Something happened, probably client closing connection.");
					e.printStackTrace();
					exit = true;
				} catch (InterruptedException e) {
					System.err.println("Sleep interrupted while waiting for input");
					e.printStackTrace();
				}
				timeStamp2 = System.currentTimeMillis();
			}
		}
	}
	
	public String updateStatus (String playerInput){
		StringTokenizer st = new StringTokenizer(playerInput);
		String firstWord = st.nextToken();
		// Player has input a word
		if (firstWord.equalsIgnoreCase("word")){
			String playerWord = st.nextToken();
			if (isValid(playerWord)){
				playerScore+=score(playerWord);
				wallHeight ++;
				return "wordWallUpdate 0 "+ playerScore.toString() + " 0 " + playerWord +"\n";
			}
				
		}
		// Player has updated
		if (firstWord.equalsIgnoreCase("update"))
			if (Integer.parseInt(st.nextToken()) == (Integer) 4)
			return "quit";
		return null;
	}
	
	public boolean isValid(String playerWord) {
		playerWord = playerWord.toLowerCase();
		if (dictionary.containsKey(playerWord))
			return true;
		return false;
	}
	
	public Integer score(String playerWord){
		int letterCount = playerWord.length();
		return (Integer) (letterCount*letterCount);
	}

	public void sendMessage (String message){
		try {
			toPlayer = player.getOutputStream();
			toPlayer.write(message.getBytes());
		}
		catch (IOException e){
			System.err.println("message");
			System.err.println("Something happened. Maybe client d/c'ed?");
			e.printStackTrace();
		}
	}
}
