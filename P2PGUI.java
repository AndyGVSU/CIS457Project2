import java.awt.*;
import java.awt.event.*;

import javax.swing.BoxLayout;

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
    
    public P2PGUI() {

        super(TITLE);
        setSize(400,600);
        setVisible(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        //First section for connecting to server (takes their info) -------
        Panel panel1 = new Panel(new FlowLayout());
        Label serverDisplay = new Label("Server Hostname:");
        TextField serverInput = new TextField("",20);
        Label portDisplay = new Label("Port:");
        TextField portInput = new TextField("",20);
        Label userDisplay = new Label("Username:");
        TextField userInput = new TextField("",20);
        Label hostDisplay = new Label("Hostname:");
        TextField hostInput = new TextField("",20);
        Label speedDisplay = new Label("Speed:");
        Choice speedInput = new Choice();
        speedInput.add("Ethernet"); //need more options?
        speedInput.add("Wifi");
        Button connectButton = new Button("Connect");
        
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
         Panel panel2 = new Panel(new FlowLayout());
         TextField keywordInput = new TextField("",20);
         Button keywordButton = new Button("Search Keyword");
         TextArea recordArea = new TextArea();
        
        panel2.add(keywordInput);
        panel2.add(keywordButton);
        panel2.add(recordArea);
        add(panel2);
        
        //third section: commands
         Panel panel3 = new Panel(new FlowLayout());
         Label commandDisplay = new Label("Enter command:");
         TextField commandInput = new TextField("",20);
         Button commandButton = new Button("Go");
         TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        
        panel3.add(commandDisplay);
        panel3.add(commandInput);
        panel3.add(commandButton);
        panel3.add(outputArea);
        add(panel3);
        
        pack();
        //show();
        setVisible(true);
    }
    public static void main (String args[]){
        P2PGUI gui = new P2PGUI();
    }
}