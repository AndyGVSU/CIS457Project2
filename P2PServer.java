import java.io.*; 
import java.net.*;
import java.util.*;


public class P2PServer{
 
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
		HostThread request = new HostThread(connectionSocket);
		Thread requestThread = new Thread(request); 
		requestThread.start();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	welcomeSocket.close();
}  

private static class HostThread implements Runnable {
	Socket controlSocket;
	Socket dataSocket;
        String fromClient;
        String clientCommand = "";
	String nextConnection;
    	int port;
    	static int clientDataPort = 12002;
	DataOutputStream outToClient;
	DataInputStream inFromClient;

	public HostThread(Socket ctrlSocket) throws Exception {
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

				//read username
				String hostUsername = inFromClient.readUTF();
				String hostName = inFromClient.readUTF();
				String hostSpeed = inFromClient.readUTF();
				Int userIP = controlSocket.getInetAddress();

				Database db = DatabaseBuilder.open(new File("gvnapster.mdb"));
				Table users = db.getTable("Users");
				Table files = db.getTable("SharedFiles");
				Cursor userCursor = CursorBuilder.createCursor(users);
				Cursor defCursor = CursorBuilder.createCursor(files);
				
				//register user in database...
				
				//if user doesn't exist...
				int i = 0;
				for(Row row : userCursor.newEntryIterable(hostName)){
					//update database
					i++;
				}
				if(i==0)
					users.addRow(user.userID, first, last, userName, hostName, userIP, clientDataPort);
				//upload shared file descriptions

				//establish data connection
				dataSocket = new Socket(nextConnection, port);
                		DataInputStream dataInFromClient = new DataInputStream(dataSocket.getInputStream());
				//receive host file descriptions (summary text file)...
				fileName = dataInFromClient.readUTF();
				fileDesc = dataInFromClient.readUTF();
				//add file description to database...
				files.addRow(files.fileID, users.userID, fileName, files.type, fileDesc);
				//client is now connected!
				break;
			case "request":
				
				//DON'T establish data connection; no file is transferred here
				
				//receive keyword request
				String fileName = inFromClient.readUTF();
				
				//assume that speed, filename, hostname, file description are all primary keys
				//so that duplicates don't need to be sorted through?

				//search data for keyword
				//for each matching description 
					//get filename, speed, hostname
					//send data to client
				for(Row row : defCursor.newEntryIterable(fileDesc)){
					db.name = outToClient.writeUTF();
					hostSpeed= outToClient.writeUTF();
					hostName = outToClient.writeUTF();
				}
				//returned all matching records
				outToClient.writeUTF("done");
				
				break;
			/* - will be replaced with request upon connection
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
			*/
			/* - host cannot store files on other hosts
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
			*/
			/* -P2P server does not store files, only file descriptions
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
					*/
		case "quit":
			System.out.println("Client thread terminated.");
			isOpen = false;

			//set host's availability to false:
			//all files stored in that host will be irretrievable
			//Remove user from the db
			break;
			}
		}
		}
		//Close the db to avoid db corruption
		db.close();
		controlSocket.close();
		outToClient.close();
		inFromClient.close();
	}
}
}
