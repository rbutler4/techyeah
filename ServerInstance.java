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
public class ServerInstance {
	private static final Integer MAX_WALL_HEIGHT = 15;
	private static final int NUMBER_OF_SWAPS = 20;
	public boolean exit = false;
	public final HashSet dictionary;
	public HashSet usedWords = new HashSet();
	public Socket player;
	public InputStream fromPlayer;
	public OutputStream toPlayer;
	public int byteNum;
	public long timeStamp1, timeStamp2;
	public String letterBankString;
	public List<Character> letterBankList = new ArrayList<Character>();
	public List<Character> nextletterBankList = new ArrayList<Character>();
	public char [] playerWordChars;
	public String gameMessage;
	public Integer playerScore = 0;
	public Integer wallHeight = 0;
	public static Random generator = new Random(System.currentTimeMillis());
	
	
	public ServerInstance(HashSet dictionary, Socket player){
		this.dictionary = dictionary;
		this.player = player;
	}

	public void run(){
		// set player
		sendMessage("setPlayer "+'A'+"\n");

		//send letter bank update (cur bank) for start of game
		letterBankString = getLetterBank();
		letterBankList = getLetterList(letterBankString);
		sendMessage("letterBankUpdate " + letterBankString);

		while (!exit){
			timeStamp1 = timeStamp2 = System.currentTimeMillis();

			// if start of game
			if(nextletterBankList.isEmpty()){
				// send another letterbank update (becomes nextbank)
				letterBankString = getLetterBank();
				nextletterBankList = getLetterList(letterBankString);
				sendMessage("letterBankUpdate " + letterBankString);
			} else {
				// copy nextletterbanklist to letterbanklist
				letterBankList = new ArrayList(nextletterBankList);
				// generate new bank, set as next bank, and send to clients
				letterBankString = getLetterBank();
				nextletterBankList = getLetterList(letterBankString);
				sendMessage("letterBankUpdate " + letterBankString);
			}
			while (timeStamp2 - timeStamp1 < 20000 && !exit){
				try{
					Thread.sleep(10);
					InputStream fromPlayer = player.getInputStream();
					byteNum = fromPlayer.available();
					byte[] receivedByte = new byte[byteNum];
					while(byteNum >0){
						fromPlayer.read(receivedByte);
						gameMessage = updateStatus(new String(receivedByte));
						// checks if valid msg to send
						if(gameMessage != null){
							sendMessage(gameMessage);
						}
						byteNum = 0;
					}
					if (wallHeight.compareTo(MAX_WALL_HEIGHT) > 0){
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
	
	/**
	*	Updates game status after a player submits a word. Checks to see if
	*	word is valid by calling the isValid method. If word is valid, checks to 
	* 	see if word is a repeated word and if letters are in the letter bank. If word
	* 	is valid, not repeated, and uses only letters from the letter bank, letters
	*	from the bank are removed, and word is added to UsedWords hash set. Score is
	*	also updated and wall height is increased by one.
	*
	*	@param playerInput	a player's inputed word
	*	@return	a string with a Wall Update (int, int, int, string): Wall flag, scoreA, scoreB, word
	*/

	public String updateStatus (String playerInput){
		// validate input
		if(playerInput == null || playerInput.equals("")){
			return null;
		}

		StringTokenizer st = new StringTokenizer(playerInput);
		String firstWord = st.nextToken();
		// Player has input a word
		//equalsIgnoreCase determines whether two string objects contain
		//the same data, ignoring the case of letters in the String
		if (firstWord.equalsIgnoreCase("word")){
			// checks if user sent a word to check
			if(!st.hasMoreTokens()){
				return null;
			}
			String playerWord = st.nextToken();
			System.out.println("Player submitted: " + playerWord);
			if (isValid(playerWord)){
				System.out.println("Word is valid");
				// Check if repeated word
				System.out.println(usedWords);
				if (!usedWords.contains(playerWord)){
					// Check if in letterbank
					if (CheckLetterList(playerWord, (ArrayList<Character>) letterBankList)){
						System.out.println("Word uses letters from letter bank");
						// Remove letters from bank
						System.out.println("Removing letters from letter bank");
						playerWordChars = playerWord.toCharArray();
						for (char c : playerWordChars)
							letterBankList.remove((Character)c);
						// Add word to usedWords
						usedWords.add(playerWord);
						playerScore+=score(playerWord);
						System.out.println("Player score: " + playerScore);
						wallHeight++;
						System.out.println("Wall height: " + wallHeight);
						//WallUpdate (int, int, int, string): Wall flag, scoreA, scoreB, word
						//Wall flags: 0 -> no powerup, 1 -> wreching  ball, 2 -> chisel, 3 -> thief
						System.out.println("updateStatus method returning: " + "wordWallUpdate 0 "+ playerScore.toString() + " 0 " + playerWord +"\n");
						return "wordWallUpdate 0 "+ playerScore.toString() + " 0 " + playerWord +"\n";
					}
				}
			}
		}
		// Player has update
		//equalsIgnoreCase determines whether two string objects contain
		//the same data, ignoring the case of letters in the String
		else if (firstWord.equalsIgnoreCase("update")){
			if (Integer.parseInt(st.nextToken()) == (Integer) 4){
				exit = true;
				return "endGame "+playerScore.toString()+" 0\n";
			}
		}

		// else invalid input
		return null;
	}
	

	/**
	*	Determines whether a word is valid by checking it against the dictionary
	*
	*	@param playerWord	a player's inputed word
	*	@return boolean true if word is in dictionary, false if word is not in dictionary
	*/
	public boolean isValid(String playerWord) {
		playerWord = playerWord.toLowerCase();
		if (dictionary.contains(playerWord))
			return true;
		return false;
	}
	
	/**
	*	Determines score for a valid, submitted word. Score is determined
	*	by word length and is quadratic. For example, if a word is three 
	*	letters long, score will be 9 (= 3 * 3).
	*
	*	@param playerWord	a player's inputed word
	*	@return Integer	player score for submitted word
	*/
	public Integer score(String playerWord){
		int letterCount = playerWord.length();
		return (Integer) (letterCount*letterCount);
	}
	
	/**
	*	Generates a new letter bank.
	*
	*	@param none.
	*	@return swapLetters(newBank) a newly generated letter bank
	*/
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
		System.out.println("New letter bank: " + swapLetters(newBank));
		return swapLetters(newBank) +"\n";
	}
	
	/**
	*	Swaps letters in word bank to display characters in more appealing fashion.
	*
	*	@param original a string that represents a newly generated letter bank
	*	@return String(result) a string that represents a newly generated letter 
	*		bank with switched ordering of vowels and consonants
	*/
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
	
	/**
	*	Converts a string to an array list.
	*
	*	@param bankString a string that represents a letter bank
	*	@return bankList an array list that contains characters from the string bankString
	*/
	public static ArrayList<Character> getLetterList (String bankString){
		ArrayList<Character> bankList = new ArrayList<Character>();
		char [] bankChars = bankString.toCharArray();
		for (char c : bankChars)
			bankList.add(c);
		return  bankList;
	}
	
	/**
	*	Checks to see if word uses letters from letter bank by removing characters present in 
	*	the word to be checked from the letter bank array list.
	*
	*	@param  checkWord a string that represents word to be checked
	*	@param	letterBank an array list representing a letter bank
	*	@return boolean	returns true if word is in the letterbank, 
	*					false if word is not in letter bank
	*/
	public static boolean CheckLetterList (String checkWord, ArrayList<Character> letterBank){
		char [] checkChars = checkWord.toCharArray();
		boolean inBank = true; 
		for (char c : checkChars){
			inBank = letterBank.remove((Character)c); //Returns true if inBank list contains the specified char
			if (!inBank)
				return false;
		}
		return true;
	}
	
	
	/**
	*	Sends message to player. 
	*
	*	@param  message a string that represents a message to be sent to player/client
	*	@return none
	*/
	public void sendMessage (String message){
		try {
			System.out.println("Message to be sent to player: " + message);
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
