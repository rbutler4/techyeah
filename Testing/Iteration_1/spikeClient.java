import java.io.*;
import java.net.*;
import java.util.concurrent.*; 

public class spikeClient {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Socket firstSocket = new Socket(hostName, portNumber);
        PrintWriter out = new PrintWriter(firstSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(firstSocket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
       
        while ((userInput = stdIn.readLine()) != null && userInput.equals("exit") != true) 
        {
        	 double total_milliseconds=0.0;
        	for(int i=0; i<10000; i++){
        		//TESTING PURPOSES--start the timer-- in nanoseconds
 				//may want to use System.currentTimeMillis() --appears it might work a little faster
 				long startTime = System.nanoTime();
 				//////////////////////////////////
 			
 			
            	out.println(userInput);
            	System.out.println("received: " + in.readLine());
            
          		//TESTING PURPOSES--stop the timer-- print duration	
             	long endTime = System.nanoTime();
         		long duration = endTime - startTime;
         		//System.out.println("nanoseconds: "+duration);
         		//convert to milliseconds 
 				double duration_in_milliseconds= (double)duration/1000000.0;
 				total_milliseconds=total_milliseconds+duration_in_milliseconds;
 				System.out.println("milliseconds: "+duration_in_milliseconds);
         		//////////////////////////////////
         	}
         	System.out.println("TOTAL  milliseconds for 10,000 pings:"+total_milliseconds);
        	System.out.println("avg time for ping (milliseconds):"+total_milliseconds/10000.0);
        }
        in.close();
        stdIn.close();
        firstSocket.close();

    }
}