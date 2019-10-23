import java.io.*; 
import java.net.*;
import java.util.*;


public class FTPServer{
 
public static void main(String[] args) throws IOException {

    final int controlPort = 12000;
    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;

        boolean isOpen = false;

      try {
        welcomeSocket = new ServerSocket(controlPort);
        isOpen = true;
    System.out.println("Server set up on port "+controlPort);
      }catch(IOException ioEx){
        System.out.println("\nUnable to set up port \n");
        System.exit(1);
      }
        
	while(isOpen) {
        connectionSocket = welcomeSocket.accept();

		try {
		ClientCommand request = new ClientCommand(connectionSocket);
		Thread requestThread = new Thread(request); 
		requestThread.start();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	welcomeSocket.close();
}  

private static class ClientCommand implements Runnable {
	Socket controlSocket;
	Socket dataSocket;
        String fromClient;
        String clientCommand = "";
	String nextConnection;
    int port;
    static int clientDataPort = 12002;
	DataOutputStream outToClient;
	DataInputStream inFromClient;

	public ClientCommand(Socket ctrlSocket) throws Exception {
		controlSocket = ctrlSocket;

		outToClient = new DataOutputStream(controlSocket.getOutputStream());
        inFromClient = new DataInputStream(controlSocket.getInputStream());

		System.out.println("Client thread started.");
	}

	public void run() {
		try {
		processRequest();
		}
		catch (Exception e) {
		System.out.println(e);
		}
	}

	private void processRequest() throws Exception {
		
		boolean isOpen = true;
		
		while(isOpen) {
			//if there's data to read
		while (inFromClient.available() > 0) {
		
		//read command
                fromClient = inFromClient.readUTF();
                StringTokenizer tokens = new StringTokenizer(fromClient);
                clientCommand = tokens.nextToken();
                nextConnection = controlSocket.getInetAddress().getHostAddress();
		
		//read port (if not connecting)
                if (!clientCommand.equals("connect")){
                	port = inFromClient.readInt();
                	System.out.println("Command "+clientCommand+" received from "+nextConnection+":"+port);
                }
	    
		switch(clientCommand) {
			case "connect":
				outToClient.writeInt(clientDataPort);				
				System.out.println("Received connection from: "+nextConnection+", allocated to port: "+clientDataPort);	
				clientDataPort += 2;
				break;
			case "list":

				//establish data connection
				dataSocket = new Socket(nextConnection, port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

				File currDir = new File(".");
                	File[] fileList = currDir.listFiles();
                	for(File f : fileList) {
                    	if(f.isFile()) {	
                			dataOutToClient.writeUTF(f.getName());
                    		}
                	}
				dataOutToClient.writeUTF("EOF");

				dataOutToClient.close();
        		dataSocket.close();
				break;

			case "stor:":	

					String fileName = inFromClient.readUTF();
		   
					//establish data connection
					dataSocket = new Socket(nextConnection, port);
					FileOutputStream fileOut = new FileOutputStream(fileName);
					BufferedReader dataIn = new BufferedReader(
							new InputStreamReader(dataSocket.getInputStream()));
					
					String nextLine;
					byte[] newLine = new String("\n").getBytes();
					while (true) {
						try{
						nextLine = dataIn.readLine();
						if (nextLine.equals("EOF"))
							break;
						fileOut.write(nextLine.getBytes());
						fileOut.write(newLine);
						}
						catch(Exception e){
							System.out.println(e);
						}
					}
					dataIn.close();
					fileOut.close();
					dataSocket.close();			
				break;
			case "retr:":
				fileName = inFromClient.readUTF();

				currDir = new File(".");
				File newFile = new File(fileName);
				byte[] data = new byte[(int)newFile.length()];
				fileList = currDir.listFiles();
				boolean fileExists = true;
				DataInputStream inStream = null;
				
				try {
				inStream = new DataInputStream(
					new BufferedInputStream(new FileInputStream(newFile)));
				}
				catch (FileNotFoundException e) {
		    		fileExists = false;
		    	}
				
				try{
					if (fileExists) {
						//System.out.println("File found");
						outToClient.writeInt(200);
						//establish data connection
						dataSocket = new Socket(nextConnection, port);
						DataOutputStream dataOut = 
								new DataOutputStream(dataSocket.getOutputStream());
						
						inStream.readFully(data, 0, data.length);
						dataOut.write(data);
						dataOut.write("EOF".getBytes());
						
						inStream.close();
						dataOut.close();
						dataSocket.close();
						outToClient.flush();
						//System.out.println("File finished sending to Client.");
					}
					else {
						outToClient.writeInt(550);
					}
					}
				catch(Exception e){
					System.out.println(e);
				}
					
					break;
		case "quit":
			System.out.println("Client thread terminated.");
			isOpen = false;
			break;
			}
		}
		}
		controlSocket.close();
		outToClient.close();
		inFromClient.close();
	}
}
}
