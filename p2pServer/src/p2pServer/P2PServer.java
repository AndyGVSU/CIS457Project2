package p2pServer;

import java.io.*; 
import java.net.*;
import java.util.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.util.IterableBuilder;

public class P2PServer {

final static int controlPort = 11900;
	
public static void main(String[] args) throws IOException {

    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;

    boolean isOpen = false;

    try {
        welcomeSocket = new ServerSocket(controlPort);
        isOpen = true;
    System.out.println("P2P Server set up on port "+controlPort);
      }catch(IOException ioEx){
        System.out.println("\nUnable to set up port \n");
        System.exit(1);
      }
        
	while(isOpen) {
        connectionSocket = welcomeSocket.accept();

		try {
		HostThread request = new HostThread(connectionSocket);
		request.start();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	welcomeSocket.close();
}  

private static class HostThread extends Thread {
    String fromClient;
    String clientCommand = "";
	String nextConnection;

    int port;
	private int clientDataPort = 11901;
	
	Socket controlSocket;
	Socket dataSocket;

	DataOutputStream outToClient;
	DataInputStream inFromClient;

	Database db;
	Table users;
	Table files;

	private String hostName;
	private String hostUsername;
	private String hostSpeed;
	
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
                if (!clientCommand.equals("connectp2p")){
                	port = inFromClient.readInt();
                	System.out.println("Command "+clientCommand+" received from "+nextConnection+":"+port);
                }
	    
		switch(clientCommand) {
			case "connectp2p":
				outToClient.writeInt(clientDataPort);
				System.out.println("Received connection from: "+nextConnection+", allocated to port: "+clientDataPort);
				clientDataPort += 2;

				//read username
				hostUsername = inFromClient.readUTF();
				hostName = inFromClient.readUTF();
				hostSpeed = inFromClient.readUTF();
				//outToClient.writeInt(clientDataPort);
				//UserIP is nextConnection

				db = DatabaseBuilder.open(new File("gvnapster.mdb"));
				users = db.getTable("Users");
				files = db.getTable("SharedFiles");
				//register user in database...
				//if user doesn't exist...
				//IndexCursor uCursor = CursorBuilder.createCursor(users.getIndex("first"));
				//for(Row row : uCursor){
				
				Cursor userCursor = CursorBuilder.createCursor(users);
				boolean dupUser = userCursor.findFirstRow(Collections.singletonMap("hostName", hostName));
				boolean dupUser2 = userCursor.findFirstRow(Collections.singletonMap("userName", hostUsername));
				
				if (!dupUser && !dupUser2) {
					users.addRow(Column.AUTO_NUMBER, hostUsername, hostName
							,nextConnection, clientDataPort, hostSpeed);
				}
				
				int fileFound = inFromClient.readInt();
				
				if (fileFound == 1) {
				
				//establish data connection
				dataSocket = new Socket(nextConnection, inFromClient.readInt());
                		DataInputStream dataInFromClient = new DataInputStream(dataSocket.getInputStream());
				//receive host file descriptions (summary text file)...
				BufferedReader dataIn = new BufferedReader(
					new InputStreamReader(dataSocket.getInputStream()));
				
					String nextLine;
					while (true) {
						try {
						nextLine = dataIn.readLine();
	
						if (nextLine.equals("EOF"))
							break;
	
						String fileName = nextLine;
						String fileDesc = dataIn.readLine(); //assume filedesc follows next line after filename
						//add file description to database...
						//Hostname and connection speed are retrieved from the entry in the USERS table
						Cursor uCursor = CursorBuilder.createCursor(users);
						uCursor.findFirstRow(Collections.singletonMap("hostName", hostName));
						int nextHostName = (int) uCursor.getCurrentRowValue(users.getColumn("userID"));
						
						Cursor fCursor = CursorBuilder.createCursor(files);
						boolean fileDup = fCursor.findFirstRow(Collections.singletonMap("fileName", fileName));
						boolean fileDup2 = fCursor.findFirstRow(Collections.singletonMap("userID", nextHostName));
						if (!(fileDup && fileDup2))
							files.addRow(Column.AUTO_NUMBER, fileName, fileDesc, nextHostName);
						}
						catch (Exception e) {
							//System.out.println(e);
							System.out.println("\nError reading filelist.");
							break;
						}
					}
					System.out.println("Received filelist from "+hostUsername);
					dataIn.close();
					dataSocket.close();
				}
				else {
					System.out.println("Invalid client / could not upload filelist.txt");
					isOpen = false;
				}
				//client is now connected!
				break;
			case "request:":
				
				//DON'T establish data connection; no file is transferred here
				
				//receive keyword request
				String fName = tokens.nextToken();
				
				//assume that speed, filename, hostname, file description are all primary keys
				//so that duplicates don't need to be sorted through?

				//search data for keyword
				//for each matching description 
					//get filename, speed, hostname
					//send data to client
				Cursor fCursor = CursorBuilder.createCursor(files);
				IterableBuilder iter = fCursor.newIterable();
				
				int nextHostIndex;
				for (Row r : iter) {
					//hostSpeed = outToClient.writeUTF(r.getString("hostSpeed"));
					nextHostIndex = (int) r.get("userID");

					Cursor uCursor = CursorBuilder.createCursor(users);
					uCursor.findFirstRow(Collections.singletonMap("userID", nextHostIndex));
					String nextHost = (String) uCursor.getCurrentRowValue(users.getColumn("hostName"));
					String nextSpeed = (String) uCursor.getCurrentRowValue(users.getColumn("speed"));
					String nextDescription = (String) fCursor.getCurrentRowValue(files.getColumn("fileDescription"));
					//return this host's speed
					//users.
					//hostName = outToClient.writeUTF();
					if (nextDescription.contains(fName)) {
						outToClient.writeUTF(nextSpeed);
						outToClient.writeUTF(nextHost);
						outToClient.writeUTF((String) r.get("fileName"));
					}
					
					//portNum = outToClient.writeInt(r.getString("port"));
					//currFile = outToClient.writeUTF(r.getString("name"));
				}
				//returned all matching records
				outToClient.writeUTF("DONE");
				
				break;
		case "quitp2p":
			System.out.println("Client thread terminated; user removed from P2P Database");
			isOpen = false;
			
			//set host's availability to false:
			//all files stored in that host will be irretrievable
			//Remove user from the db
			userCursor = CursorBuilder.createCursor(users);
			userCursor.findFirstRow(Collections.singletonMap("hostName", hostName));
			users.deleteRow(userCursor.getCurrentRow());
			}
			break;
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

