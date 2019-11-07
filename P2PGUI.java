import java.awt.*;
import java.awt.event.*;



public class P2PGUI extends Frame{
    final String TITLE = "File Transfer Client"
    
    public P2PGUI(){
        super(TITLE);
        setSize(400,600);
        setVisible(true);
        setLayout(new FlowLayout());
        
        //First section for connecting to server (takes their info) -------
        private Panel panel1 = new Panel(new FlowLayout()); 
        private Label serverDisplay = new Label("Server Hostname:");
        private TextField serverInput = new TextField();
        private Label portDisplay = new Label("Port:");
        private TextField portInput = new TextField();
        private Label userDisplay = new Label("Username:");
        private TextField userInput = new TextField();
        private Label hostDisplay = new Label("Hostname:");
        private TextField hostInput = new TextField();
        private Label speedDisplay = new Label("Speed:");
        private Choice speedInput = new Choice();
        speedInput.add("Ethernet"); //need more options?
        speedInput.add("Wifi");
        private Button connectButton = new Button("Connect");
        
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
        private Panel panel2 = new Panel(new FLowLayout());
        private TextField keywordInput = new TextField();
        private Button keywordButton = new Button("Search Keyword");
        //How to represent files?
        
        panel2.add(keywordInput);
        panel2.add(keywordButton);
        add(panel2);
        
        //third section: commands
        private Panel panel3 = new Panel(new Flowlayout());
        private Label commandDisplay = new Label("Enter command:");
        private TextField commandInput = new TextField();
        private Button commandButton = new Button("Go");
        private TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        
        panel3.add(commandDisplay);
        panel3.add(commandInput);
        panel3.add(commandButton);
        panel3.add(outputArea);
        add(panel3);
        
        
        
        pack();
        show();
    }
    public static void main (String agrs[]){
        new P2PGUI();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
