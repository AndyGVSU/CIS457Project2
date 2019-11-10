import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.BoxLayout;
import java.util.*;

public class P2PGUI extends Frame {

    final static String TITLE = "File Transfer Client";
    private Panel panel1;
    private Label serverDisplay;
    private TextField serverInput;
    private Label portDisplay;
    private TextField portInput;
    private Label userDisplay;
    private TextField userInput;
    private Label hostDisplay;
    private TextField hostInput;;
    private Label speedDisplay;
    private Choice speedInput;
    private Button connectButton;

    private Panel panel2;
    private TextField keywordInput;
    private Button keywordButton;
    private TextArea recordArea;

    private Panel panel3;
    private Label commandDisplay;
    private TextField commandInput;
    private Button commandButton;
    private TextArea outputArea;

    private FTPHandler handler;
    private FTPClient client;
    ArrayList<String> receivedRecords = new ArrayList<String>();
    
    public P2PGUI(FTPHandler h) {

        super(TITLE);
        setSize(400,600);
        setVisible(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.handler = h;
        this.client = handler.getClient();
        //First section for connecting to server (takes their info) -------
         panel1 = new Panel(new FlowLayout());
         serverDisplay = new Label("Server Hostname:");
         serverInput = new TextField("localhost",20);
         portDisplay = new Label("Port:");
         portInput = new TextField("11900",20);
         userDisplay = new Label("Username:");
         userInput = new TextField("andy",20);
         hostDisplay = new Label("Hostname:");
         hostInput = new TextField("dc15",20);
         speedDisplay = new Label("Speed:");
         speedInput = new Choice();
        speedInput.add("Ethernet"); //need more options?
        speedInput.add("Wifi");
        speedInput.add("Modem");
         connectButton = new Button("Connect");
         connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                String serverName = serverInput.getText();
                String port = portInput.getText();
                String username = userInput.getText();
                String hostname = hostInput.getText();
                String speed = speedInput.getItem(speedInput.getSelectedIndex());

                if (!username.isEmpty() && !hostname.isEmpty() &&
                    !port.isEmpty() && !speed.isEmpty()) {
                        //System.out.println("cmd sent");
                        client.setCommand("connectp2p "+serverName + " "+
                            port+" "+username+" "+hostname+" "+speed);  
                        serverInput.setText("");
                        portInput.setText("");
                        userInput.setText("");
                        hostInput.setText(""); //clear boxes
                        
                    }
                }});


        //add them in the proper order...
        panel1.add(serverDisplay);
        panel1.add(serverInput);
        panel1.add(portDisplay);
        panel1.add(portInput);
        panel1.add(userDisplay);
        panel1.add(userInput);
        panel1.add(hostDisplay);
        panel1.add(hostInput);
        panel1.add(speedDisplay);
        panel1.add(speedInput);
        panel1.add(connectButton);
        add(panel1);
        
        //Second section for file search -------
          panel2 = new Panel(new FlowLayout());
          keywordInput = new TextField("",20);
          keywordButton = new Button("Search Keyword");
          keywordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String keyword = keywordInput.getText();
                if (!keyword.isEmpty()) {
                        client.setRecordsAvailable(false);
                        client.setCommand("request: "+keyword);
                        setReceivedRecords();
                        keywordInput.setText("");//clear text box
                    }
                }});
          
        recordArea = new TextArea();
          
        panel2.add(keywordInput);
        panel2.add(keywordButton);
        panel2.add(recordArea);
        add(panel2);
        
        //third section: commands
          panel3 = new Panel(new FlowLayout());
          commandDisplay = new Label("Enter command:");
          commandInput = new TextField("",20);
          commandButton = new Button("Go");
          commandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String cmd = commandInput.getText();
                if (!cmd.isEmpty()) {
                        client.setCommand(cmd);
                        commandInput.setText(""); //clear text box
                    }
                }});
                
          outputArea = new TextArea();
        outputArea.setEditable(false);
        
        panel3.add(commandDisplay);
        panel3.add(commandInput);
        panel3.add(commandButton);
        panel3.add(outputArea);
        add(panel3);
        
        pack();
        setVisible(true);
    }
    public static void main (String args[]){
        FTPHandler handler = new FTPHandler();
        P2PGUI gui = new P2PGUI(handler);
    }

    public void setReceivedRecords() {
        receivedRecords = client.getReceivedRecords();
        recordArea.setText("");
        String nextRecord;
        for (int i = 0; i < receivedRecords.size(); i+=3) {
            nextRecord = "\n" + receivedRecords.get(i) + " " + receivedRecords.get(i+1) + 
                " " + receivedRecords.get(i+2);
            recordArea.setText(recordArea.getText() + nextRecord);
        }
    }
}
