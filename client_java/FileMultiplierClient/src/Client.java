/*
 * File Multiplier Client
 * Ian Stewart
 * 
 * 
 */


import java.io.*; 
import java.net.*;

public class Client { 

	private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

   
     
    public Client(String ip, int port) throws IOException {
    	 // Make connection and initialize streams
        socket = new Socket(ip, port);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }


    public void run() {
//    	PrintWriter outfile;
//		try {
//			outfile = new PrintWriter(new BufferedWriter(new FileWriter("test.text")));
//			outfile.println("This");
//	    	outfile.println("is");
//	    	outfile.println("a");
//	    	outfile.println("file");
//	    	outfile.close();
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
    	
    	
    	
    	
    	 BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
         for (;;) {
 	        System.out.println("Enter a message:");
 	        String lineFromUser = null;
 			try {
 				lineFromUser = inFromUser.readLine();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 			if (lineFromUser.equals("logout")) {
 				break;
 			} else if (lineFromUser.equals("INIT")) {
 				out.println(lineFromUser);
 	 	        System.out.println("Enter a file name:");
 	 	        String fileName = "";
 				try {
 	 				fileName = inFromUser.readLine();
 	 			} catch (IOException e1) {
 	 				e1.printStackTrace();
 	 			}
 				
            	try {
					BufferedReader file = new BufferedReader(new FileReader(fileName));
					out.println(fileName);
					
                	System.out.println("Sending contents of file: " + fileName + " to server");
					
					String fileLine = file.readLine();
					while (fileLine != null) {
						out.println(fileLine);
						fileLine = file.readLine();
					}
					file.close();
					
					System.out.println("File: " + fileName + " sent to server");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
    			out.println("END");
    			
    			String response;
     	        try {
     	        	response = in.readLine();
     	        	if (response == null || response.equals("")) {
     	                System.exit(0);
     	            }
     	        	
     	        	 if (response.equals("REC")) {
          	        	System.out.println("Server created file: " + fileName + " successfully");
          	        }
     	        } catch (IOException e) {
     	        	e.printStackTrace();
     	        }
     	        
     	       
    			
 			} else if (lineFromUser.equals("GET")) {
 				out.println(lineFromUser);
 				 System.out.println("Enter a file name:");
 				 String fileName = "";
  				try {
  	 				fileName = inFromUser.readLine();
  	 			} catch (IOException e1) {
  	 				e1.printStackTrace();
  	 			}
  				
  				out.println(fileName);
  				
  				System.out.println("Getting file: " + fileName + " from server");
  				
  				String response;
  				 try {
  	 	        	response = in.readLine();
  	 	        	if (response == null || response.equals("")) {
  	 	                System.exit(0);
  	 	            }
  	 	        	
					PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
  	 	        	
  	 	        	while (!response.equals("END")) {
  	 	        		file.println(response);
  	 	        		response = in.readLine();
  	 	        	}
  	 	        	
  	 	        	file.close();
  	 	        	
  	 	        	System.out.println("File: " + fileName + " copied to local machine from server");
  	 	        	
  	 	        	out.println("REC");
  	 	        } catch (IOException e) {
  	 	        	e.printStackTrace();
  	 	        }
 			}
 			 			
 			
 	        
         }
         try {
 			socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
    }
    
    public static void main(String[] args) throws Exception {
    	if (args.length != 2) {
    		System.out.println("Usage: Server <Server IP> <Server Port>");
    		System.exit(1);
    	}
    	
        Client client = new Client(args[0], Integer.parseInt(args[1]));
        client.run();
    }
} 