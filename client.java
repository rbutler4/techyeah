/*
*	Java threaded Client
*	iteration 2.0
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

	// send update(int flag) to server
	public static void update(int flag){
		// check if connected to server and can read & write
		if(sock != null && output != null && input != null){
			// send message
			output.println("update "+flag);
			System.out.print((DEBUG)?"update "+flag+"\n":"");
		} else {
			System.out.print((DEBUG)?"update: not connected\n":"");
		}
	}

	// send word(String word) to server
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

	// parses a string then calls apporpriate GUI method
	public static void parse(String msg){
		System.out.print((DEBUG)?"parse msg: "+msg+"\n":"");
		int flag;
		int scoreA;
		int scoreB;
		char player;
		Boolean t;
		String str;

		// tokenize msg by spaces
		String[] tokens = msg.split("[ ]+");

		if(0<tokens.length){
			str = tokens[0];

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
						//GUI.wordWallUpdate(flag,scoreA,scoreB,str);
					} else {
						System.out.print((DEBUG)?"invalid wordWallUpdate\n":"");
					}
					break;

				// letterBankUpdate (string bank)
				case "letterBankUpdate":
					if(2==tokens.length){
						str = tokens[1];
						System.out.print((DEBUG)?"letterBankUpdate("+str+")\n":"");
						//GUI.letterBankUpdate(str);
					} else {
						System.out.print((DEBUG)?"invalid letterBankUpdate\n":"");
					}
					break;

				// setPlayer (char player)
				case "setPlayer":
					if(2==tokens.length){
						player = tokens[1].charAt(0);
						System.out.print((DEBUG)?"setPlayer("+player+")\n":"");
						//GUI.setPlayer(player);
					} else {
						System.out.print((DEBUG)?"invalid setPlayer\n":"");
					}
					break;

				// timeOut (boolean t)
				case "timeOut":
					if(2==tokens.length){
						t = Boolean.valueOf(tokens[1]);
						System.out.print((DEBUG)?"timeOut("+t+")\n":"");
						//GUI.timeOut(t);
					} else {
						System.out.print((DEBUG)?"invalid timeOut\n":"");
					}
					break;

				// setPowerup (char player, int flag)
				case "setPowerup":
					if(3==tokens.length){
						player = tokens[1].charAt(0);
						try{
							flag = Integer.parseInt(tokens[2]);
						} catch(NumberFormatException err){
							System.err.println(err);
							break;
						}
						System.out.print((DEBUG)?"setPowerup("+player+", "+flag+")\n":"");
						//GUI.setPowerup(player,flag);
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
						//GUI.endGame(scoreA,scoreB);
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

	public static void main(String [] args){
		// check argument port or use default port
		client.host = DEFAULT_HOST;
		client.port = DEFAULT_PORT;
		if(0<args.length){
			String temp = null;
			Pattern IPv4 = Pattern.compile("\\d*\\.\\d*\\.\\d*\\.\\d*");	// *.*.*.*
			//Pattern IPv6 = Pattern.compile("");	// *:*:*:*:*:*:*:*
			Pattern p = Pattern.compile("\\d*");	// any number of digits
			Matcher m;
			for(int i=0; i<args.length; i++){
				System.out.print((DEBUG)?"args["+i+"]: "+args[i]+"\n":"");
				// temp = args[i];
				m = IPv4.matcher(args[i]);
				if(m.matches()){
					System.out.print((DEBUG)?"match host\n":"");
					temp = "host";
				} else {
					m = p.matcher(args[i]);
					if(m.matches()){
						System.out.print((DEBUG)?"match port\n":"");
						temp = "port";
					}
				}

				switch(temp){
					case "host":
						client.host = args[i];
						break;
					case "port":
						try{
							int tempInt = Integer.parseInt(args[i]);
							if(tempInt > 1024 && tempInt < 65535){
								client.port = tempInt;
							} else {
								client.port = DEFAULT_PORT;
							}
						} catch(NumberFormatException err){
							System.err.println(err);
							client.port = DEFAULT_PORT;
						}
						break;
					default:
						break;
				}
			}
		}

		System.out.print((DEBUG)?"host: "+client.host+"\n":"");
		System.out.print((DEBUG)?"port: "+client.port+"\n":"");

		// test parse
		// if(DEBUG){
		// 	parse("wordWallUpdate 0 1 2 word");
		// 	parse("wordWallUpdate 0 e 2 word");	// err
		// 	parse("wordWallUpdate 0 1 2");	// err
		// 	parse("letterBankUpdate abcdefghijklmnopqrstuvwxyz");
		// 	parse("letterBankUpdate ");	// err
		// 	parse("letterBankUpdate");	// err
		// 	parse("setPlayer A");
		// 	parse("setPlayer 3");
		// 	parse("setPlayer error");	// player set to 'e', the first char of the token
		// 	parse("setPlayer");	// err
		// 	parse("timeOut true");	// true
		// 	parse("timeOut TRUE");	// true
		// 	parse("timeOut TruE");	// true
		// 	parse("timeOut truth");	// false
		// 	parse("timeOut false");	// false
		// 	parse("timeOut FALSE");	// false
		// 	parse("setPowerup A 1");
		// 	parse("setPowerup 1 A");	// err
		// 	parse("setPowerup A");	// err
		// 	parse("endGame 46 28");
		// 	parse("endGame win loss");	// err
		// 	parse("endGame 46");	// err
		// 	parse("endGame");	// err
		// 	parse(" ");	// err
		// 	parse("");	// err
		// }

		// set up socket, output stream, input stream
		System.out.print((DEBUG)?"connecting...":"");
		try{
			sock = new Socket(client.host, client.port);
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

			// while thread is alive (Game is being played)
			while(lt.isAlive()){
				// wait for client-to-server messages
			}
		}

		// close streams and socket
		try{
			input.close();
			output.close();
			sock.close();
		} catch(IOException err){
			System.err.println(err);
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
			} catch(IOException err){
				System.err.println(err);
			}
		}
	}
}