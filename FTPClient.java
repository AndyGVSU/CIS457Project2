import java.io.*; 
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

//now must be an object (cannot run main(); GUI runs main )
public class FTPClient implements Runnable {
	
private ArrayList<String> receivedRecords = new ArrayList<String>();

//public static void main(String argv[]) throws Exception
//public void receiveCommand(String command) throws Exception
public void run()
	{
	try {
    String sentence; 
    String modifiedSentence; 
    String statusCode;
    String fileName = null;
    StringTokenizer tokens;
    
   	int controlPort = 12000;
	int command_port = 0;
	
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
	
	ServerSocket welcomeData = null;
	Socket dataSocket = null;
	Socket ControlSocket = null;
	
	System.out.println("\n|| FTP Client Project 1 ~ CIS 457 ||");

	while(isOpen) {

        System.out.println("\nInput next command:\nconnect <host> <port> | quit | list | stor: <file> | retr: <file>");  

	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	sentence = inFromUser.readLine();
    
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
	else if(sentence.equals("connect2p2")) {
		//connect with P2P server
		
		String serverName = tokens.nextToken();
		controlPort = Integer.parseInt(tokens.nextToken());

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
    		}
    		catch (Exception e) {
    			System.out.println("Failed to set up P2P socket.");
    			P2PconnectionEstablished = false;
		}
	}	
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
		if (!command.equals("list")) {
			fileName = tokens.nextToken();
			outToServer.writeUTF(fileName);
		}

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
	
		String keyword = fileName;
		//send keyword (from command argument - passed automatically from GUI)
		outToP2P.writeUTF(keyword);
		
		String record;
		ArrayList<String> newRecords = getReceivedRecords();
		newRecords.clear();
		
		record = inFromP2P.readUTF();
		boolean isDone = false;
		while (!isDone) {

			record = inFromP2P.readUTF();
			if (record.equals("DONE")) {
				isDone = true;	
			}
			else {
				newRecords.add(record);
			}
		}
		
		//setReceivedRecords(newRecords);	
		//records are displayed by GUI
	 	}
         else {
        	 System.out.println("\nInvalid command; use one of the listed commands\n");
         }
		//welcomeFile.close();
	//}
	
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
	return receivedRecords;
}
}
