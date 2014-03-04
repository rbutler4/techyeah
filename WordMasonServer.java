/**
*  WordMasonServer.java
*  
*  Debug print statements should be in the form (the '\n' is import since using print not println): 
	System.out.print((DEBUG)?"debug statment\n":"");
*
*  Method comments should be in the form:
	// name:   methodName
	// input:  inputOneType, inputTwoType, etc.
	// output: returnType
	// description:  description of method
	// comments:  optional comments about method
*  
*  Here's some code used as a template for multi-client paired threaded game server
*  Basically class TicTacToeServer = WordMasonServer.java and class Game = ServerInstance.java
*  http://cs.lmu.edu/~ray/notes/javanetexamples/#tictactoe
*/

// import statements alphabetically here
// try to import specifically what is needed and avoid using .*
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Stack;


/**
*  WordMasonServer
*  listens for connects and pairs them up into games
*/
public class WordMasonServer {
	// set DEBUG to false to turn off debug info for WordMasonServer
	private static final Boolean DEBUG = true;
	private static final int DEFAULT_PORT = 5000;
	private static int port;

	public static void main(String[] args) throws Exception {
		//parse input so a diffrent port can be used from cmd line
		if(args.length > 0){
			port = Integer.parseInt(args[0]);

		}else{
			port = DEFAULT_PORT;
		}
		ServerSocket listener = new ServerSocket(port);
		System.out.print((DEBUG)?"WordMasonServer listening on port: "+port+"\n":"");

		try {
			while(true){
				// create a new game, set players in it, and start it
				// Not sure if this will timeout
				Game game = new Game();

				Game.Player playerOne = game.new Player(listener.accept(), 'A');
				System.out.print((DEBUG)?"player one connected\n":"");

				Game.Player playerTwo = game.new Player(listener.accept(), 'B');
				System.out.print((DEBUG)?"player two connected\n":"");

				playerOne.setOpponent(playerTwo);
				playerTwo.setOpponent(playerOne);

				playerOne.start();
				playerTwo.start();
				game.run(playerOne, playerTwo);
			}
		} finally {
			listener.close();
		}
	}
}

/**
*  Game
*  handles a two player WordMason game
*/
class Game {
	// set DEBUG to false to turn off debug info for Game
	private static final Boolean DEBUG = true;
	private static boolean exit = false;	// false if game is being played
	private static final Integer MAX_WALL_HEIGHT = 15;
	private static HashSet dictionary;
	private static HashSet usedWords;
	private static final int NUMBER_OF_SWAPS = 20;  // used in David's swapLetters method
	private static int scoreA;
	private static int scoreB;
	private static Integer wallHeight = 0;
	private static List<Character> letterBankList = new ArrayList<Character>();
	private static List<Character> nextletterBankList = new ArrayList<Character>();
	private static long timeStamp1, timeStamp2;
	private static Stack wall = new Stack();  // stack of the current word wall
	private static String letterBankString;
	private static Random generator = new Random(System.currentTimeMillis());
	private static String file = "2of4brif.txt";

	// CONSTRUCTOR
	// name:   Game
	// input:  [none]
	// output: [none]
	// description:  initalizes game
	public Game(){
		super();
		// initalize dictionary, see Server.java
		dictionary = new HashSet();
		int totalCount;
		try{
	        FileReader reader = new FileReader(file);
	        BufferedReader buffRead = new BufferedReader (reader);
	        totalCount = (int) Integer.parseInt(buffRead.readLine());
	        for (int i =1; i< totalCount; i++) {
				dictionary.add(buffRead.readLine());
	        }

	        System.out.print((DEBUG)?"dictionary has been populated\n":"");
		 } catch(FileNotFoundException e){
	         e.printStackTrace();
	     } catch (IOException e) {
	         e.printStackTrace();
	     }
	}

	// name:   run
	// input:  Player, Player
	// output: [none]
	// description:  the main loop of the game, sends letterBank updates, checks if won/quit
	// comments:  partially David's code
	public synchronized void run(Player player, Player opponent){
		//send letter bank update (cur bank) for start of game
		letterBankString = getLetterBank();
		letterBankList = getLetterList(letterBankString);
		send("letterBankUpdate " + letterBankString, player, opponent);

		// while game is being played
		while (!exit){
			// reset timeStamps
			timeStamp1 = timeStamp2 = System.currentTimeMillis();

			// if start of game
			if(nextletterBankList.isEmpty()){
				// send another letterbank update (becomes nextbank)
				letterBankString = getLetterBank();
				nextletterBankList = getLetterList(letterBankString);
				send("letterBankUpdate " + letterBankString, player, opponent);
			} else {
				// copy nextletterbanklist to letterbanklist
				letterBankList = new ArrayList(nextletterBankList);
				// generate new bank, set as next bank, and send to clients
				letterBankString = getLetterBank();
				nextletterBankList = getLetterList(letterBankString);
				send("letterBankUpdate " + letterBankString, player, opponent);
			}

			// loop until a letterBank update or win or quit
			while (timeStamp2 - timeStamp1 < 20000 && !exit){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e){
					System.err.println("Sleep interrupted while waiting for input");
					e.printStackTrace();
				}

				// check if won
				if (wallHeight.compareTo(MAX_WALL_HEIGHT) > 0){
					send("endGame "+scoreA+" "+scoreB+"\n", player, opponent);
					exit = true;
				}

				timeStamp2 = System.currentTimeMillis();
			}
		}
	}

	// name:   getLetterBank
	// input:  [none]
	// output: String
	// description:  generates a new letter bank
	// comments:  David's code
	public synchronized String getLetterBank(){
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

	// name:   swapLetters
	// input:  String
	// output: String
	// description:  used in getLetterBank
	// comments:  David's code
	public synchronized String swapLetters (String original){
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

	// name:   getLetterList
	// input:  String
	// output: ArrayList<Character>
	// description:  Converts a string to an array list
	// comments:  David's code
	public synchronized ArrayList<Character> getLetterList (String bankString){
		ArrayList<Character> bankList = new ArrayList<Character>();
		char [] bankChars = bankString.toCharArray();
		for (char c : bankChars)
			bankList.add(c);
		return  bankList;
	}

	// name:   isValid
	// input:  String
	// output: boolean
	// description:  returns true if String is a valid word else false
	// comments:  synchronized so with threads it is "First come First serve" to maintain concurrency
	public synchronized boolean isValid(String word){
		System.out.print((DEBUG)?"isValid: "+word+"\n":"");

		//check if word is in dictionary
		word = word.toLowerCase();
		if (dictionary.contains(word)){
			System.out.print((DEBUG)?word+" is in dictionary\n":"");
			
			//check if word has been used
			if(!(usedWords.contains(word))){
				System.out.print((DEBUG)?word+" has not been used\n":"");

				//check if word uses letters from current letter bank
				char [] wordCharsArray = word.toCharArray();
				boolean isInLetterBank = true; 
				for (char c : wordCharsArray){
					isInLetterBank = letterBankList.remove((Character)c); //returns true if inBankList contains the specified char
					if (!isInLetterBank){
						isInLetterBank = false;
					}
				}

				if(isInLetterBank){
					System.out.print((DEBUG)?word+" is valid\n":"");
					return true;
				}
				System.out.print((DEBUG)?"invalid: "+word+" uses letters not in letter bank\n":"");
				return false;
			}
			System.out.print((DEBUG)?"invalid: "+word+" has been used\n":"");
			return false;
		}
		System.out.print((DEBUG)?"invalid: "+word+" is not in dictionary\n":"");
		return false;
	}

	// name:   parse
	// input:  String, Player, Player
	// output: [none]
	// description:  parse player input and if valid sends msg to clients
	// comments:  synchronized so with threads it is "First come First serve" to maintain concurrency
	public synchronized void parse(String msg, Player player, Player opponent){
		System.out.print((DEBUG)?"parse msg: "+msg+" player: "+player.player+" opponent: "+opponent.player+"\n":"");
		int flag;
		String msgWord;
		String str;
		
		// TODO: parse player input similar to client.parse()
		// see devUseCases for Client-to-Server "methods" it needs to handle
		// see devUseCases for Server-to-Client "methods" it needs to send
		String[] tokens = msg.split("[ ]+");

		if(0<tokens.length){
			str = tokens[0];
			System.out.print((DEBUG)?"first msg token: "+str+"\n":"");

			switch(str){
				// update
				case "update":
					if(2==tokens.length){
						try{
							flag = Integer.parseInt(tokens[1]);
						} catch(NumberFormatException err){
							System.err.println(err);
							break;
						}
						System.out.print((DEBUG)?"update(" + flag + ")\n":"");
						switch(flag){
							//wrecking ball
							case 1:
								// remove last two words from wall
								// be sure to remove from player's and opponent's own walls
								// send wordWallUpdate for wrecking ball
								System.out.print((DEBUG)?"wordWallUpdate(" + flag + " " + scoreA + " " + scoreB + " " + player.player + " " + opponent.player + "\n":"");
								send("wordWallUpdate "+ flag + " " + scoreA + " " + scoreB + "\n", player, opponent);

								break;
							//chisel
							case 2:
								// ??
								// send wordWallUpdate for chisel
								System.out.print((DEBUG)?"wordWallUpdate(" + flag + " " + scoreA + " " + scoreB + " " + player.player + " " + opponent.player + "\n":"");
								send("wordWallUpdate "+ flag + " " + scoreA + " " + scoreB + "\n", player, opponent);

								break;
							//thief
							case 3:
								// find opponent's last word
								// remove from opponent's wall
								// add to player's wall
								// remove word's points from opponent
								// give half points to player
								// send wordWallUpdate for theif
								System.out.print((DEBUG)?"wordWallUpdate(" + flag + " " + scoreA + " " + scoreB + " " + player.player + " " + opponent.player + "\n":"");
								send("wordWallUpdate "+ flag + " " + scoreA + " " + scoreB + "\n", player, opponent);

								break;
							//quit
							case 4:
								exit = true;
								// send endGame
								send("endGame "+scoreA+" "+scoreB+"\n", player, opponent);
								break;
						}
					} else {
						System.out.print((DEBUG)?"invalid update\n":"");
					}
					break;
				//word
				case "word":
					if(2==tokens.length){
						msgWord = tokens[1];
						System.out.print((DEBUG)?"word(" + msgWord + ")\n":"");
						//if word is valid
						if(isValid(msgWord)){
							// update wall
							// update player's own wall
							// update player score
							// send wordWallUpdate for word

							// if received powerup
								// send setPowerup to player
						}

					} else {
						System.out.print((DEBUG)?"invalid word sent from client\n":"");
					}
					break;
			}
		}
	}

	// name:   send
	// input:  String, Player
	// output: [none]
	// description:  sends String to one client
	// comments:  synchronized so with threads it is "First come First serve" to maintain concurrency
	public synchronized void send(String msg, Player player){
		player.output.println(msg);
		System.out.print((DEBUG)?"player "+player.player+" sent: "+msg+"\n":"");
	}

	// name:   send
	// input:  String, Player, Player
	// output: [none]
	// description:  sends String to two clients
	// comments:  synchronized so with threads it is "First come First serve" to maintain concurrency
	public synchronized void send(String msg, Player player, Player opponent){
		player.output.println(msg);
		opponent.output.println(msg);
		System.out.print((DEBUG)?"players: "+player.player+" "+opponent.player+" sent: "+msg+"\n":"");
	}

	// thread player
	class Player extends Thread {
		char player;  // A or B
		Player opponent;
		Stack wall = new Stack();  // stack of words owned by player
		Socket socket;
		BufferedReader input;
		PrintWriter output;

		// THREAD CONSTRUCTOR
		// name:   Player
		// input:  Socket, char
		// output: [none]
		// description:  initalizes this player
		public Player(Socket socket, char player){
			this.socket = socket;
			this.player = player;

			// initalize client comunications
			try {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				System.out.print((DEBUG)?"player "+this.player+" died\n":"");
				System.err.println(e);
				e.printStackTrace();
			}
		}

		// name:   setOpponent
		// input:  Player
		// output: [none]
		// description:  sets opponent of this player
		public void setOpponent(Player opponent){
			this.opponent = opponent;
		}

		// runs this thread
		// this thread started only if two players connected
		public void run(){
			try {
				// game going to start
				send("timeOut FALSE", this);

				String temp;
				// while game is going and player connected
				while(!exit && socket.isConnected() && !socket.isClosed()){
					// listen for input
					temp = input.readLine();

					// parse input
					if(temp != null){
						parse(temp, this, opponent);
						System.out.print((DEBUG)?"input detected\n":"");

					}
				}

				// close socket and streams
				input.close();
				output.close();
				socket.close();
			} catch (IOException | NullPointerException e){
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
}