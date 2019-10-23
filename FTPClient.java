import java.io.*; 
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

class FTPClient { 
	
public static void main(String argv[]) throws Exception
    { 
    String sentence; 
    String modifiedSentence; 
    String statusCode;
    String fileName;
    StringTokenizer tokens;
    
   	int controlPort = 12000;
	int command_port = 0;
	
	boolean isOpen = true;
	boolean connectionEstablished = false;
	boolean notEnd = true;
	boolean fileExists = false;
	
	DataOutputStream outToServer = null;
	DataInputStream inFromServer = null;
	DataInputStream inData = null;
	
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
         else if(sentence.equals("quit")) {
        	 outToServer.writeUTF(command);
        	 outToServer.writeInt(command_port);
        	 isOpen = false;
        	 System.out.println("Have a nice day!");
         }
	else if (connectionEstablished)
		{
		fileName = null;
		outToServer.writeUTF(command);
		outToServer.writeInt(command_port);
		if (!command.equals("list")) {
			fileName = tokens.nextToken();
			outToServer.writeUTF(fileName);
		}
		//establish data connection
		ServerSocket welcomeFile = new ServerSocket(command_port);
    		dataSocket = welcomeFile.accept();
		
		/*
		if(command.equals("list")) {
		try {
	 	inData = new DataInputStream(dataSocket.getInputStream());
		}
		catch (Exception e) {
		System.out.println(e);
		}

	    System.out.println("\nListing files in host directory...");
		while (true) {
		    try {
		    modifiedSentence = inData.readUTF();
	            if (modifiedSentence.equals("EOF"))
			break;

		    System.out.println(modifiedSentence);
		    }
		    catch (EOFException e) {}
		    }
 
		System.out.println("\nAll files displayed.");
		inData.close();
	        }
         else */ 
	 if(command.equals("retr:")) {

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
	 /*
         else if(command.equals("stor:")) {
        	//fileName = tokens.nextToken();
		
		fileExists = true;
		FileInputStream fileIn = null;
		File currentDirectory = new File("./"+fileName);

		//System.out.println(fileName);

		try {
		    	fileIn = new FileInputStream(currentDirectory);
		}
		catch (FileNotFoundException e) {
		    	System.out.println("\nRequested file not found or is a directory.\n");
		    	fileExists = false;
		    	}
         	if (fileExists) {

			//for reading file
			BufferedReader fileStream = new BufferedReader(
				new FileReader(currentDirectory));

        	    //for sending file
		    	BufferedWriter dataOut = new BufferedWriter(
				new OutputStreamWriter(dataSocket.getOutputStream()));
			
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
		    	System.out.println("\nSent file "+fileName+" successfully.\n");
	 	
			fileIn.close();
			fileStream.close();
			dataOut.close();
         	}
         }
	 */
	 else if (command.equals("request: ") {
		
		String keyword = fileName; //will come from GUI, converted to FTP command
		//send keyword
		outToServer.writeUTF(keyword);
		
		//until "done"
			//read/store speed, hostname, filename
		
		//records are displayed by GUI
	 }
         else {
        	 System.out.println("\nInvalid command; use one of the listed commands\n");
         }
		welcomeFile.close();
		}
	
	}
    if (ControlSocket != null) {
    	outToServer.close();
    	inFromServer.close();
    	ControlSocket.close();
    	}
}
}
