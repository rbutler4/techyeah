import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

// Individual games are handled as threads by the server.
public class SinglePlayerGameInstance {
	private static final Integer MAX_WALL_HEIGHT = 9;
	private static final int NUMBER_OF_SWAPS = 20;
	public boolean exit = false;
	public final HashSet dictionary;
	public HashSet usedWords;
	public Socket player;
	public InputStream fromPlayer;
	public OutputStream toPlayer;
	public int byteNum;
	public long timeStamp1, timeStamp2;
	public String letterBankString;
	public List<Character> letterBankList = new ArrayList<Character>();
	public char [] playerWordChars;
	public String gameMessage;
	public Integer playerScore = 0;
	public Integer wallHeight = 0;
	public static Random generator = new Random(System.currentTimeMillis());
	
	
	public SinglePlayerGameInstance(HashSet dictionary, Socket player){
		this.dictionary = dictionary;
		this.player = player;
	}

	public void run(){
		//sendMessage("Connected to server\n");
		
		while (!exit){
			timeStamp1 = timeStamp2 = System.currentTimeMillis();
			letterBankString = getLetterBank();
			letterBankList = getLetterList(letterBankString);
			sendMessage("letterBankUpdate " + letterBankString);
			while (timeStamp2 - timeStamp1 < 20000 && !exit){
				try{
					Thread.sleep(10);
					InputStream fromPlayer = player.getInputStream();
					byteNum = fromPlayer.available();
					byte[] receivedByte = new byte[byteNum];
					while(byteNum >0){
						fromPlayer.read(receivedByte);
						gameMessage = updateStatus(new String(receivedByte));
						sendMessage(gameMessage);
						byteNum = 0;
					}
					if (wallHeight >=SinglePlayerGameInstance.MAX_WALL_HEIGHT){
						sendMessage("endGame "+playerScore.toString()+" 0\n");
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
				//System.out.println(timeStamp2-timeStamp1);
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
				// Check if repeated word
				if (!usedWords.contains(playerWord)){
					// Check if in letterbank
					if (CheckLetterList(playerWord, (ArrayList<Character>) letterBankList)){
						// Remove letters from bank
						playerWordChars = playerWord.toCharArray();
						for (char c : playerWordChars)
							letterBankList.remove((Character)c);
						// Add word to usedWords
						usedWords.add(playerWord);
					}
					else{
					}
				}else{
					
				}
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
		if (dictionary.contains(playerWord))
			return true;
		return false;
	}
	
	public Integer score(String playerWord){
		int letterCount = playerWord.length();
		return (Integer) (letterCount*letterCount);
	}
	
	public static String getLetterBank(){
		String newBank = "";
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		char newLetter;
		int randomlySelected = 0;
		ArrayList<Character> vowels = new ArrayList<Character>();
		ArrayList<Character> consonants = new ArrayList<Character>();
		
		vowels.add('a');
		vowels.add('e');
		vowels.add('i');
		vowels.add('o');
		vowels.add('u');
		
		for (int i =0; i <3; i++){
			randomlySelected = generator.nextInt(5);
			newLetter = vowels.get(randomlySelected);
			newBank += newLetter;
		}

		consonants.add('b');
		consonants.add('c');
		consonants.add('d');
		consonants.add('f');
		consonants.add('g');
		consonants.add('h');
		consonants.add('j');
		consonants.add('k');
		consonants.add('l');
		consonants.add('m');
		consonants.add('n');
		consonants.add('p');
		consonants.add('q');
		consonants.add('r');
		consonants.add('s');
		consonants.add('t');
		consonants.add('v');
		consonants.add('w');
		consonants.add('x');
		consonants.add('y');
		consonants.add('z');
		
		for (int i =21; i >16; i--){
			randomlySelected = generator.nextInt(i);
			newLetter = consonants.get(randomlySelected);
			consonants.remove(randomlySelected);
			newBank += newLetter;
		}
		for (int i = 0; i < 7; i++){
			randomlySelected = generator.nextInt(26);
			newLetter = alphabet.charAt(randomlySelected);
			newBank += newLetter;
		}
		for (int i =5; i >2; i--){
			randomlySelected = generator.nextInt(i);
			newLetter = vowels.get(randomlySelected);
			vowels.remove(randomlySelected);
			newBank += newLetter;
		}
		
		return swapLetters(newBank) +"\n";
	}
	
	public static String swapLetters (String original){
		char [] result = original.toCharArray();
		int sLength = original.length();
		char toBeReplaced;
		int firstPlaceReplaced = 0;
		int secondPlaceReplaced = 0;
		for (int i = 0; i <NUMBER_OF_SWAPS; i++){
			firstPlaceReplaced = generator.nextInt(sLength);
			toBeReplaced = result[firstPlaceReplaced];
			secondPlaceReplaced = generator.nextInt(sLength);
			result[firstPlaceReplaced] = result[secondPlaceReplaced];
			result[secondPlaceReplaced] = toBeReplaced;
			
		}
		return new String(result);
	}
	
	public static ArrayList<Character> getLetterList (String bankString){
		ArrayList<Character> bankList = new ArrayList<Character>();
		char [] bankChars = bankString.toCharArray();
		for (char c : bankChars)
			bankList.add(c);
		return  bankList;
	}
	
	public static boolean CheckLetterList (String checkWord, ArrayList<Character> letterBank){
		char [] checkChars = checkWord.toCharArray();
		boolean inBank = true;
		for (char c : checkChars){
			inBank = letterBank.remove((Character)c);
			if (!inBank)
				return false;
		}
		return true;
	}
	
	

	public void sendMessage (String message){
		try {
			toPlayer = player.getOutputStream();
			toPlayer.write(message.getBytes());
		}
		catch (IOException e){
			System.err.println(message);
			System.err.println("Something happened. Maybe client d/c'ed?");
			e.printStackTrace();
		}
	}
	
	public static void main (String [] args){
		for (int i = 0; i<10; i++)
			System.out.println(getLetterBank());
	}
}
