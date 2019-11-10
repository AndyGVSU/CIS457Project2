import java.io.*; 
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

//now must be an object (cannot run main(); GUI runs main )
public class FTPClient extends Thread {
	
private ArrayList<String> receivedRecords = new ArrayList<String>();
private static String receivedCommand = null;
private static boolean newCommand = false;
private boolean recordsAvailable = false;

public void run() {
	try {
    String sentence; 
    String modifiedSentence; 
    String statusCode;
	StringTokenizer tokens;
	String userHostname;
	String username;
	String connectionSpeed;
	String fileName = null;
    
   	int controlPort = 12000;
	int command_port = 0;
	int p2pCommandPort = 0;
	
	boolean isOpen = true;
	boolean connectionEstablished = false;
	boolean P2PconnectionEstablished = false;
	boolean notEnd = true;
	boolean fileExists = false;
	
	DataOutputStream outToServer = null;
	DataInputStream inFromServer = null;
	DataInputStream inData = null;
	DataOutputStream outToP2P = null;
	DataInputStream inFromP2P = null;

	Socket P2PSocket = null;
	ServerSocket p2pRecordSocket = null;
	ServerSocket welcomeData = null;
	Socket dataSocket = null;
	Socket ControlSocket = null;

	while(isOpen) {

	//System.out.println("\n|| FTP Client Project 1 ~ CIS 457 ||");

	//replace with a hang if possible?
	while(!newCommand);
	System.out.println("Received");
	sentence = receivedCommand;
	newCommand = false;
    
	String command;
	tokens = null;
	if (!sentence.isEmpty())
		{
		tokens = new StringTokenizer(sentence);
		command = tokens.nextToken();
		}
	else
		command = "";
        
	if (command.equals("connect")) {
		//will need to be modified to distinguish P2PServer connection from host connection
		//connect to a FTP Host

		String serverName = tokens.nextToken();
		controlPort = Integer.parseInt(tokens.nextToken());

    		try {
    			System.out.println("Connecting to " + serverName + ":" + controlPort);
				
    			if (ControlSocket != null) {
    				outToServer.close();
    				inFromServer.close();
    				ControlSocket.close();
    			}
    			
    			ControlSocket = new Socket(serverName, controlPort);
    			System.out.println("You are connected to " + serverName + ":" + controlPort);
				connectionEstablished = true;

        		outToServer = new DataOutputStream(ControlSocket.getOutputStream());
        		inFromServer = new DataInputStream(ControlSocket.getInputStream());
        	
				outToServer.writeUTF(command);
				//permanent port for data connection
				command_port = inFromServer.readInt();
    		}
    		catch (Exception e) {
    			System.out.println("Failed to set up socket.");
    			connectionEstablished = false;
		}

	}
	else if(command.equals("connectp2p")) {
		//connect with P2P server
		String serverName = tokens.nextToken();
		controlPort = Integer.parseInt(tokens.nextToken());
		username = tokens.nextToken();
		userHostname = tokens.nextToken();
		connectionSpeed = tokens.nextToken();

		try {
			System.out.println("Connecting to " + serverName + ":" + controlPort);
			
			if (P2PSocket != null) {
				outToP2P.close();
				inFromP2P.close();
				P2PSocket.close();
			}
			
			P2PSocket = new Socket(serverName, controlPort);
			System.out.println("You are connected to P2P Server " + serverName + ":" + controlPort);
			P2PconnectionEstablished = true;

			outToP2P = new DataOutputStream(P2PSocket.getOutputStream());
			inFromP2P = new DataInputStream(P2PSocket.getInputStream());
			
			outToP2P.writeUTF(command);
			p2pCommandPort = inFromP2P.readInt();

			outToP2P.writeUTF(username);
			outToP2P.writeUTF(userHostname);
			outToP2P.writeUTF(connectionSpeed);

			fileExists = true;
			FileInputStream fileIn = null;
			File currentDirectory = new File("./"+"filelist.txt");

			//System.out.println(fileName);

			try {
				fileIn = new FileInputStream(currentDirectory);
			}
			catch (FileNotFoundException e) {
				System.out.println("\n filelist.txt not found or is a directory.\n");
				fileExists = false;
			}
			if (fileExists) {
			
			ServerSocket sendFile = new ServerSocket(p2pCommandPort);
			outToP2P.writeInt(1);
			outToP2P.writeInt(p2pCommandPort);
			//for reading file
			BufferedReader fileStream = new BufferedReader(
				new FileReader(currentDirectory));

				//for sending file
				BufferedWriter dataOut = new BufferedWriter(
				new OutputStreamWriter(sendFile.accept().getOutputStream()));
			
			String nextLine;
				while (true) {
				try {
				nextLine = fileStream.readLine();
				
				if (nextLine == null)
					break;
				
					dataOut.write(nextLine, 0, nextLine.length());
					dataOut.newLine();
				}
				catch (Exception e) {
					System.out.println("\nError writing file.\n");
				}	
			}
				dataOut.write("EOF",0,3);
				System.out.println("\nSent filelist.txt successfully.\n");
			
			sendFile.close();
			fileIn.close();
			fileStream.close();
			dataOut.close();
			}
			else {
				outToP2P.writeInt(0);
			}
			}
		catch (Exception e) {
			System.out.println("Failed to set up P2P socket.");
			P2PconnectionEstablished = false;
		}
	}	
	//remove?
         else if(sentence.equals("quit")) {
        	 outToServer.writeUTF(command);
        	 outToServer.writeInt(command_port);
        	 isOpen = false;
        	 System.out.println("Have a nice day!");
         }
	else if (connectionEstablished && command.equals("retr:"))
		{
		fileName = null;
		outToServer.writeUTF(command);
		outToServer.writeInt(command_port);
		
		//get extra arguments
		fileName = tokens.nextToken();
		outToServer.writeUTF(fileName);
		

		//establish data connection
		ServerSocket welcomeFile = new ServerSocket(command_port);
    		dataSocket = welcomeFile.accept();
		
		int fileStatus = 0;
		//listen on the control connection for the file's status
		while (true) {
			if (inFromServer.available() != 0) {
				fileStatus = inFromServer.readInt();
				break;
			}
		}

		if (fileStatus == 200) {
			System.out.println("\n 200 OK; Retrieving file");

		    	BufferedReader dataIn = new BufferedReader(
				new InputStreamReader(dataSocket.getInputStream()));
		    
		    	fileExists = true;
		    	FileOutputStream fileOut = null;
		    	try {
		    		fileOut = new FileOutputStream(fileName);
		    	}
		    	catch (FileNotFoundException e) {
		    		System.out.println("Requested file is already a directory.");
		    		fileExists = false;
		    	}
		    			    
			String nextLine;
			byte[] newLine = "\n".getBytes();
		    	if (fileExists) {
		    		boolean fileWritten = true;
		    		while (true) {
					try {
					nextLine = dataIn.readLine();

					if (nextLine.equals("EOF"))
						break;

		    			fileOut.write(nextLine.getBytes());
		    			fileOut.write(newLine);
					}
					catch (Exception e) {
						System.out.println("\nError writing file.");
						fileWritten = false;
						break;
					}
				}
		    	if (fileWritten)
		    	System.out.println("Retrieved file "+fileName+" successfully.");
		    	}
			fileOut.close();
			dataIn.close();
			//dataSocket.close();
			welcomeFile.close();
		}
		else {
			System.out.println("\n 550 File Not Found\n");
		}
	}
	 else if (P2PconnectionEstablished && command.equals("request:")) {

		outToP2P.writeUTF(sentence);
		outToP2P.writeInt(command_port);

		String nextSpeed;
		String nextHostName;
		String nextFileName;
		receivedRecords.clear();

		boolean isDone = false;
		while (!isDone) {
			nextSpeed = inFromP2P.readUTF();
			
			if (nextSpeed.equals("DONE")) {
				isDone = true;
			}
			else {
				nextHostName = inFromP2P.readUTF();
				nextFileName = inFromP2P.readUTF();

				receivedRecords.add(nextSpeed);
				receivedRecords.add(nextHostName);
				receivedRecords.add(nextFileName);
			}
			}
		System.out.println("Request successful.");
		setRecordsAvailable(true);
		//records are displayed by GUI
		//dataIn.close();
		//dataSocket.close();
		//p2pRecordSocket.close();
		}
    else if (command.equals("help")){
        System.out.println("connect [server] [port]: connects to a server (not another peer)");
        System.out.println("connectp2p [server] [port] [user] [target user] [connection speed]: connect to another peer to exchange files");
        System.out.println("request: [file]: retrieve a file");
	}
    else {
        System.out.println("\nInvalid command; use one of the listed commands\n");
    }
		//welcomeFile.close();
	}
	

    if (ControlSocket != null) {
    	outToServer.close();
    	inFromServer.close();
    	ControlSocket.close();
    	}
    if (P2PSocket != null) {
		outToP2P.close();
		inFromP2P.close();
		P2PSocket.close();
    	}
	}
	catch (Exception e) {
		System.out.println(e);
	}
}

public ArrayList<String> getReceivedRecords() {
	while (!recordsAvailable) {
		System.out.print("");
	}
	return receivedRecords;
}

public void setRecordsAvailable(boolean rec) {
	recordsAvailable = rec;
}

public static void setCommand(String s) {
	//System.out.println("method call");
	receivedCommand = s;
	newCommand = true;
	//System.out.println(newCommand);
}
}
