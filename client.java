/*
*	Java threaded Client
*	iteration 2.3
*	Tech Yeah!
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.Socket;
import java.net.UnknownHostException;

public class client{
	// set DEBUG to false to turn off debug info
	private static final Boolean DEBUG = true;
	private static final String DEFAULT_HOST = "127.0.0.1";
	private static final int DEFAULT_PORT = 5000;
	private static String host = null;
	private static int port = -1;
	private static Socket sock = null;
	private static BufferedReader input = null;
	private static PrintWriter output = null;
	private static WordMasonGUI GUI = null;
	private static char player = 'x';
	private static String nextBank = null;
	private static int playerScoreA;
	private static int playerScoreB;
	private static boolean wreckUsed;

	// CONSTRUCTOR
	// name:   client
	// input:  [none]
	// output: [none]
	// description:  sets host and port to defaults
	public client(){
		host = DEFAULT_HOST;
		port = DEFAULT_PORT;

		System.out.print((DEBUG)?"host: "+host+"\n":"");
		System.out.print((DEBUG)?"port: "+port+"\n":"");
	}

	// CONSTRUCTOR
	// name:   client
	// input:  String
	// output: [none]
	// description:  sets port to default and host to input if valid, else default
	public client(String hos){
		host = DEFAULT_HOST;
		port = DEFAULT_PORT;

		Pattern IPv4 = Pattern.compile("\\d*\\.\\d*\\.\\d*\\.\\d*");	// *.*.*.*
		Matcher m;
		m = IPv4.matcher(hos);
		if(m.matches()){
			host = hos;
		}

		System.out.print((DEBUG)?"host: "+host+"\n":"");
		System.out.print((DEBUG)?"port: "+port+"\n":"");
	}

	// CONSTRUCTOR
	// name:   client
	// input:  int
	// output: [none]
	// description:  sets host to default and port to input if valid, else default
	public client(int por){
		host = DEFAULT_HOST;
		port = DEFAULT_PORT;

		Pattern p = Pattern.compile("\\d*");	// any number of digits
		Matcher m;
		m = p.matcher(Integer.toString(por));
		if(m.matches() && por > 1024 && por < 65535){
			port = por;
		}

		System.out.print((DEBUG)?"host: "+host+"\n":"");
		System.out.print((DEBUG)?"port: "+port+"\n":"");
	}

	// CONSTRUCTOR
	// name:   client
	// input:  String, int
	// output: [none]
	// description:  sets host and port to input if valid, else defaults
	public client(String hos, int por){
		host = DEFAULT_HOST;
		port = DEFAULT_PORT;

		Pattern IPv4 = Pattern.compile("\\d*\\.\\d*\\.\\d*\\.\\d*");	// *.*.*.*
		Pattern p = Pattern.compile("\\d*");	// any number of digits
		Matcher m;
		m = IPv4.matcher(hos);
		if(m.matches()){
			host = hos;
		}
		m = p.matcher(Integer.toString(por));
		if(m.matches() && por > 1024 && por < 65535){
			port = por;
		}

		System.out.print((DEBUG)?"host: "+host+"\n":"");
		System.out.print((DEBUG)?"port: "+port+"\n":"");
	}

	// name:   setGUI
	// input:  WordMasonGUI
	// output: [none]
	// description:  sets the WordMasonGUI to calling instance so we can send messages to it
	public static void setGUI(WordMasonGUI inst){
		GUI = inst;
		System.out.print((DEBUG)?"WordMasonGUI set\n":"");
	}

	// name:   update
	// input:  int
	// output: [none]
	// description:  send update(int flag) to server
	public static void update(int flag){
		// check if connected to server and can read & write
		if(sock != null && output != null && input != null){
			// send message
			output.println("update "+flag);
			if (flag == 1) wreckUsed = true;
			System.out.print((DEBUG)?"update "+flag+" wreckUsed: "+wreckUsed+"\n":"");
		} else {
			System.out.print((DEBUG)?"update: not connected\n":"");
		}
	}

	// name:   word
	// input:  String
	// output: [none]
	// description:  send word(String word) to server
	public static void word(String word){
		// check if connected to server and can read & write
		if(sock != null && output != null && input != null){
			// send message
			output.println("word "+word);
			System.out.print((DEBUG)?"word "+word+"\n":"");
		} else {
			System.out.print((DEBUG)?"word: not connected\n":"");
		}
	}

	// name:   parse
	// input:  String
	// output: [none]
	// description:  parses a string then calls apporpriate GUI method(s)
	public static void parse(String msg){
		System.out.print((DEBUG)?"parse msg: "+msg+"\n":"");
		int flag;
		int scoreA;
		int scoreB;
		char play;
		Boolean t;
		String str;

		// tokenize msg by spaces
		String[] tokens = msg.split("[ ]+");

		if(0<tokens.length){
			str = tokens[0];
			System.out.println((DEBUG)?"Number of tokens: "+tokens.length+"\n":"");
			switch(str){
				// wordWallUpdate (int wallFlag, int scoreA, int scoreB, string word)
				case "wordWallUpdate":
					if(5==tokens.length){
						try{
							flag = Integer.parseInt(tokens[1]);
							scoreA = Integer.parseInt(tokens[2]);
							scoreB = Integer.parseInt(tokens[3]);
							str = tokens[4];
						} catch(NumberFormatException err){
							System.err.println(err);
							break;
						}
						System.out.print((DEBUG)?"wordWallUpdate("+flag+", "+scoreA+", "+scoreB+", "+str+")\n":"");
						switch(flag){
							// add word
							case 0:
								//player A added a word to the wall
								if(playerScoreA < scoreA) {
									if(player == 'A') {
										GUI.setOwner(0);
									} else {
										GUI.setOwner(1);
									}
									playerScoreA = scoreA;
								//player B added a word to the wall
								} else if (playerScoreB < scoreB) {
									if(player == 'B') {
										GUI.setOwner(0);
									} else {
										GUI.setOwner(1);
									}
									playerScoreB = scoreB;
								}
								GUI.addWord(str);
								if(player == 'A'){
									GUI.setPlayerOneScore(scoreA);
									GUI.setPlayerTwoScore(scoreB);
								} else {  // player B
									GUI.setPlayerOneScore(scoreB);
									GUI.setPlayerTwoScore(scoreA);
								}
								break;
							// use powerup
							default:
								int user;
								//player A's score has decreased, player is A: opponent used powerup		
								if (playerScoreA > scoreA && player == 'A') {
									user = 1;
								//player B's score has decreased, player is B: opponent used powerup	
								} else if (playerScoreB > scoreB && player == 'B') {
									user = 1;
								//special case: thief or chisel used when opponent has no words
								//(no effect)		
								} else if (playerScoreA == scoreA && playerScoreB == scoreB
									&& flag != 1) {
									break;
								} else {
									user = 0;
								}	
								
								//can't use score to tell who used wrecking ball, so 
								//check if wreckUsed has recently been set to true
								if (flag == 1 && wreckUsed == false) {
									user = 1;
								}
								
								GUI.powerupUsed(flag, user);
								wreckUsed = false;
								System.out.println((DEBUG)?"Powerup used: " + user + ", " + flag:"");
								if (player == 'A') {
									GUI.setPlayerOneScore(scoreA);
									GUI.setPlayerTwoScore(scoreB);
								} else {
									GUI.setPlayerOneScore(scoreB);
									GUI.setPlayerTwoScore(scoreA);
								}
								break;
						}
						//update tracking of player scores
						playerScoreA = scoreA;
						playerScoreB = scoreB;
					} else {
						System.out.print((DEBUG)?"invalid wordWallUpdate\n":"");
					}
					break;

				// letterBankUpdate (string bank)
				case "letterBankUpdate":
					if(2==tokens.length){
						str = tokens[1];
						System.out.print((DEBUG)?"letterBankUpdate("+str+")\n":"");
						if(nextBank != null){
							GUI.setBank(nextBank);
							GUI.setNextBank(str);
							nextBank = str;
						} else {  // first bank update, assumes another bank update is comming soon
							nextBank = str;
						}
					} else {
						System.out.print((DEBUG)?"invalid letterBankUpdate\n":"");
					}
					break;

				// setPlayer (char player)
				case "setPlayer":
					if(2==tokens.length){
						play = tokens[1].charAt(0);
						System.out.print((DEBUG)?"setPlayer("+play+")\n":"");
						player = play;
					} else {
						System.out.print((DEBUG)?"invalid setPlayer\n":"");
					}
					break;

				// timeOut (boolean t)
				case "timeOut":
					if(2==tokens.length){
						t = Boolean.valueOf(tokens[1]);
						System.out.print((DEBUG)?"timeOut("+t+")\n":"");
						if(t){
							GUI.timeOutDialog();
						}
					} else {
						System.out.print((DEBUG)?"invalid timeOut\n":"");
					}
					break;

				// setPowerup (char player, int flag)
				case "setPowerup":
					if(3==tokens.length){
						play = tokens[1].charAt(0);
						try{
							flag = Integer.parseInt(tokens[2]);
						} catch(NumberFormatException err){
							System.err.println(err);
							break;
						}
						System.out.print((DEBUG)?"setPowerup("+play+", "+flag+")\n":"");
						if(play == player){
							 GUI.setPlayerOnePowerup(flag);
						} else {	// play B && player B
							 GUI.setPlayerTwoPowerup(flag);
						}
					} else {
						System.out.print((DEBUG)?"invalid setPowerup\n":"");
					}
					break;

				// endGame (int scoreA, int scoreB)
				case "endGame":
					if(3==tokens.length){
						try{
							scoreA = Integer.parseInt(tokens[1]);
							scoreB = Integer.parseInt(tokens[2]);
						} catch(NumberFormatException err){
							System.err.println(err);
							break;
						}
						System.out.print((DEBUG)?"endGame("+scoreA+", "+scoreB+")\n":"");
						if(player == 'A'){
							GUI.setPlayerOneScore(scoreA);
							GUI.setPlayerTwoScore(scoreB);
							GUI.gameOverDialog();
						} else {  // player B
							GUI.setPlayerOneScore(scoreB);
							GUI.setPlayerTwoScore(scoreA);
							GUI.gameOverDialog();
						}
					} else {
						System.out.print((DEBUG)?"invalid endGame\n":"");
					}
					break;

				// invalid token
				default:
					System.out.print((DEBUG)?"parse token invalid: "+str+"\n":"");
					break;
			}
		} else {
			System.out.print((DEBUG)?"parse input string invalid\n":"");
		}
	}

	// name:   connect
	// input:  [none]
	// output: [none]
	// description:  connects to server, plays game, closes sockets and streams
	public static void connect(){
		// check that we have a host and port or use defaults
		if(host==null){
			host = DEFAULT_HOST;
		}
		if(port==-1){
			port=DEFAULT_PORT;
		}

		// set up socket, output stream, input stream
		System.out.print((DEBUG)?"connecting...":"");
		try{
			sock = new Socket(host, port);
			output = new PrintWriter(sock.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.print((DEBUG)?"connected\n":"");
		} catch(UnknownHostException err){
			System.out.print((DEBUG)?"failed\n":"");
			System.err.println(err);
		} catch(IOException err){
			System.out.print((DEBUG)?"failed\n":"");
			System.err.println(err);
		}

		// connected to server, now listen for input
		if(sock != null && output != null && input != null){
			// create and run listening/parsing thread
			listenThread lt = new listenThread(sock, output, input);
			lt.start();
		}
	}
}

// thread that listens for input from server
class listenThread extends Thread{
	Socket sock = null;
	BufferedReader input = null;
	PrintWriter output = null;

	// constructor
	listenThread(Socket sock, PrintWriter output, BufferedReader input){
		this.sock = sock;
		this.output = output;
		this.input = input;
	}

	// what the thread does when run
	public void run(){
		// check if connected and can read & write
		if(sock != null && output != null && input != null){
			try{
				String temp;
				// game is over when server disconnects us
				while((sock.isConnected() && !sock.isClosed())){
					// listen for input
					temp = input.readLine();
					
					// parse input
					if(temp != null){
						client.parse(temp);
					}
				}

				// close socket and streams
				input.close();
				output.close();
				sock.close();
				
			} catch(IOException | NullPointerException err){
				System.err.println(err);
			}
		}
	}
}
