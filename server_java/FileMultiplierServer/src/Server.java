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

        public Syncer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client # " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by repeatedly reading strings
         * and sending back the modified version of the file if necessary.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Get messages from the client, line by line;
                while (true) {

                    String lineFromClient = in.readLine();
//                    if (lineFromClient == null) {
//                        break;
//                    }
                    
                    System.out.println("received line from client # " + this.clientNumber + ": " + lineFromClient);
                    
                    if (lineFromClient.equals("INIT")) {
                    	// get file from client and write to server
                    	
                    	String fileName = in.readLine();	// file name
                    	
                    	PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
                    	
                    	String fileLine = in.readLine();
                    	
                    	// write to file
                    	System.out.println("Writing to file: " + fileName);
                    	while (!fileLine.equals("END")) {
                    		file.println(fileLine);
                    		fileLine = in.readLine();
                    	}
                    	                    	
                    	file.close();	
                    	System.out.println("file created: " + fileName);
                    	
                    	out.println("REC");		// tell the client that the file was created successfully
                    	
                    } else if (lineFromClient.equals("GET")) {
                    	String fileName = in.readLine();	// file name
                    	
                    	BufferedReader file = new BufferedReader(new FileReader(fileName));
                    	
                    	System.out.println("Sending contents of file: " + fileName + " to client # " + this.clientNumber);
                    	
                    	String fileLine = file.readLine();
                    	while (fileLine != null) {
                    		out.println(fileLine);
                    		fileLine = file.readLine();
                    	}
                    	
                    	file.close();
                    	
                    	System.out.println("Done sending file: " + fileName + " to client # " + this.clientNumber);
                    	
                    	out.println("END");
                    	
                    	String response;
                    	
                    	try {
             	        	response = in.readLine();
             	        	if (response == null || response.equals("")) {
             	                System.exit(0);
             	            }
             	        	
             	        	 if (response.equals("REC")) {
                  	        	System.out.println("Client # " + this.clientNumber + " created file: " + fileName + " successfully");
                  	        }
             	        } catch (IOException e) {
             	        	e.printStackTrace();
             	        }
                    	
                    	
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