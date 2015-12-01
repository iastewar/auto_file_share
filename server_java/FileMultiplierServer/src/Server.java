/*
 * File Multiplier Server
 * Ian Stewart
 * 
 * 
 */

import java.io.*;
import java.net.*;


public class Server {
	
	
	/**
     * Application method to run the server runs in an infinite loop
     * listening on port args[0].  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.
     */
    public static void main(String[] args) throws Exception {
    	if (args.length != 1) {
    		System.out.println("Usage: TCPServer <Listening Port>");
    		System.exit(1);
    	}
    	
        System.out.println("The server is running.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(Integer.parseInt(args[0]));
        try {
            while (true) {
                new Syncer(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A private thread to handle sync requests on a particular
     * socket.
     */
    private static class Syncer extends Thread {
        private Socket socket;
        private int clientNumber;
        private BufferedReader in;
        private PrintWriter out;

        public Syncer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client # " + clientNumber + " at " + socket);
            try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        /**
         * Responds to a CREATE request by writing the file specified by the client to the current directory.
         * 
         * ACKs with "REC".
         * 
         * @throws IOException
         */
        public void respondToCreateRequest() throws IOException {
        	// get file from client and write to server
        	
        	String fileName = in.readLine();
        	
        	PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        	
        	String fileLine = in.readLine();
        	
        	// write to file
        	log("Writing to file: " + fileName);
        	while (!fileLine.equals("END")) {
        		file.println(fileLine);
        		fileLine = in.readLine();
        	}
        	                    	
        	file.close();	
        	log("file created: " + fileName);
        	
        	out.println("REC");		// tell the client that the file was created successfully
        }
        
        /**
         * Responds to a GET request by getting the file specified by the client from the current directory and sending it to the client.
         * 
         * Sends "END" to the client when the file has been sent
         * 
         * @throws IOException
         */
        public void respondToGetRequest() throws IOException {
        	String fileName = in.readLine();
        	
        	BufferedReader file = new BufferedReader(new FileReader(fileName));
        	
        	log("Sending contents of file: " + fileName + " to client # " + this.clientNumber);
        	
        	String fileLine = file.readLine();
        	while (fileLine != null) {
        		out.println(fileLine);
        		fileLine = file.readLine();
        	}
        	
        	file.close();
        	
        	log("Done sending file: " + fileName + " to client # " + this.clientNumber);
        	
        	out.println("END");
        	
        	String response;
        	
        	try {
 	        	response = in.readLine();
 	        	if (response == null || response.equals("")) {
 	                System.exit(0);
 	            }
 	        	
 	        	 if (response.equals("REC")) {
      	        	log("Client # " + this.clientNumber + " created file: " + fileName + " successfully");
      	        }
 	        } catch (IOException e) {
 	        	e.printStackTrace();
 	        }
        }
        
        /**
         * Services this thread's client by repeatedly reading strings
         * and sending back the modified version of the file if necessary.
         */
        public void run() {
            try {

               

                // Get messages from the client, line by line;
                while (true) {

                    String lineFromClient = in.readLine();
                    if (lineFromClient == null) {
                        break;
                    }
                    
                    log("received line from client # " + this.clientNumber + ": " + lineFromClient);
                    
                    if (lineFromClient.equals("CREATE")) {
                    	this.respondToCreateRequest();
                    	
                    } else if (lineFromClient.equals("GET")) {
                    	this.respondToGetRequest();
                    	
                    	
                    }
                                        
                    
                }
            } catch (IOException e) {
                log("Error handling client # " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client # " + clientNumber + " closed");
            }
        }

        /**
         * Logs a simple message to stout.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }
}







/*
Questions:

- How does the client get the updated files from the server without conflicts? For example, lets say the user is editing the file on the client side. If the server sends an
  update to the file, it will overwrite any changes the user makes. The only solution I can think of is making sure users use an editor that saves extremely frequently.
  
- If two or more clients make changes to the same file at around same time, the server might be writing to the file at the same time via two or more different threads.
  This is a concurrency issue that needs to be avoided. I can think of two solutions:
  	1. Making clients send a timestamp along with the file so that we can keep a linear history of file changes. We will also have to keep multiple versions of the same file
  	   since we don't want multiple threads writing at the same time. The server will then loop through the history and apply the file changes one by one.
  	2. Making clients send a token requesting access to the file. The server keeps a queue of tokens and allows access one by one. The problem with this approach is network
  	   latency however (linear file history might not be preserved).
  
  The problem with both of these approaches is that we want users to be able to edit in real time. If many users are editing the file at once it might take a very long time
  before the user sees the most recent file.


*/