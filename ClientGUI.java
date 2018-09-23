/**
 * Version 1.3
 * Author: MK2022
 * Date: 06/03/2018
 * 
 * GUI to handle displaying messages received by a Client object, and for receiving messages for a Client object to transmit to server.
 * Also displays to the user if clients connect or disconnect, or if the connection to the server has dropped, 
 * and provides a button for the user to disconnect from the server.
 */
import java.awt.BorderLayout; // Necessary imports
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ClientGUI extends JFrame 
{
	
	 	private JTextArea chat; // Displays chat messages
	    private JTextField message; // TextField for user to enter desired message
	    private JTextField data; 
	    private JTextField portField; // TextField for user to enter desired message
	    private JTextField addressField; // TextField for user to enter desired message
	    private JButton disconnect; // Button to disconnect
	    private JButton connect; // Button to connect
	    private JLabel info; // Label to inform user of failure to connect
	    private ClientConnection client; // The Client object that is connected to the server
	    private JButton login;
	    private String id;
	/**
	 * Constructor for ClientGUI object, creates Client object, starts Thread for listening for server, and sets up GUI
	 * @param port
	 * 		Port used for connecting to server
	 * @param address
	 * 		Address used for connecting to server
	 * @param id
	 * 		Username to be displayed in chat, is handled by Client object for object oriented principle reasons
	 */
	public ClientGUI(int port, String address)
	{
		super("Client Chat GUI"); // Calls superclass constructor
		
		chat = new JTextArea("", 80, 80); // Sets up elements in GUI
		JPanel chatPanel = new JPanel(new GridLayout(1,1));
		chatPanel.add(new JScrollPane(chat)); // Using a JScrollPane, messages will continue to be able to fit in the window
		chat.setEditable(false);
		add(chatPanel, BorderLayout.CENTER); 
		
		JPanel messagePanel = new JPanel();
		disconnect = new JButton("Disconnect"); // Adds disconnect and message elements to other section of window
		connect = new JButton("Connect"); // Adds disconnect, connect, info and message elements to other section of window
		message = new JTextField("", 15);
		portField = new JTextField("" + port, 5);
		addressField = new JTextField(address, 15);
		info = new JLabel("");
		login = new JButton("Set Username");
		data = new JTextField("", 15);
		
		messagePanel.add(data); // Adds elements to lower panel
		messagePanel.add(login);
		messagePanel.add(info);
		messagePanel.add(addressField);
		messagePanel.add(portField);
		messagePanel.add(connect);
		messagePanel.add(message);
		messagePanel.add(disconnect);
		
		add(messagePanel, BorderLayout.SOUTH);
		
		disconnect.setVisible(false); // Disconnect and Message should not be available until a successful connection has been made
		message.setVisible(false);
		connect.setVisible(false);
		addressField.setVisible(false);
		portField.setVisible(false);

        ButtonHandler bh = new ButtonHandler(); // Creates handler for the disconnect button
        disconnect.addActionListener(bh);
        connect.addActionListener(bh);
        login.addActionListener(bh);

        TextFieldHandler th = new TextFieldHandler(); // Creates handler for the message textfield
        message.addActionListener(th);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		
		client = new ClientConnection(this); // Creates Client object to establish connection
		
		write("Welcome to the Chat room:");
		write("Please enter your username:");
	}
		
	/**
	 * Writes a String to the chat field
	 * @param str
	 * 		Message to be written
	 */
	public void write(String str) 
	{
		chat.append(str + "\n"); // Writes incoming chat message
		chat.setCaretPosition(chat.getText().length() - 1); // Sets cursor position in chat field
	}
	
	/**
	 * Checks if a given string is a number
	 * @param str
	 * 			String to be checked
	 * @return
	 * 			Boolean denoting whether string is a digit
	 */
	public boolean isInt(String str) 
	{
		for (int i = 0; i < str.length(); i++)
		{
			if (!Character.isDigit(str.charAt(i))) // checks for a char that is not a digit
					return false;
		}
		return true; // If loop completes then only digits were in the string
	}
	
	/**
     * Method for Client to connect to Server
     */
    public void connect()
    {
    	if (!addressField.getText().equals("") || !portField.getText().equals(""))
    	{
    		String address = addressField.getText(); // gets address
    		if (isInt(portField.getText())) // Ensures that entry in portField is a number
    		{
    			int port = Integer.parseInt(portField.getText());
    		
    			if (!client.connect(address, port)) // If connect is unsuccessful, prompts user to try again
    			{
    				info.setText("Could not connect to server");
    				info.setForeground(Color.RED);
    			}
    			else // Procedure if connection is successful
    			{
    				new Thread(client).start(); // Starts thread to receive messages from server
    				
    				client.setID(id); // Client object informs server of user ID
    				
    				message.setVisible(true);
    				disconnect.setVisible(true); // Successful connection means message and disconnect should now be visible
    				message.setText("");
    				connect.setVisible(false); // Connect should now no longer be visible
    				info.setVisible(false); // If info was used, it should also no longer be visible
    				addressField.setVisible(false);
    				portField.setVisible(false);
    				login.setVisible(false);
    		}
    		}
    		else
    		{
    			info.setText("Incorrect format for port"); // Informs user of incorrect port format
				info.setForeground(Color.RED);
    		}
    	}
    	else
    	{
    		info.setText("Invalid entry, please try again"); // Informs user of invalid address and/or port entry
    		info.setForeground(Color.RED);
    	}
    }
    
    /**
     * Login method runs after user enters username
     */
    public void login()
    {
    	if (data.getText().equals("")) // ensures valid entry
    	{
    		info.setText("Invalid username, please try again"); // prompts user for correct entry
    		info.setForeground(Color.RED);
    	}
    	else
    	{
    		info.setText(""); // Resets field text
    		id = data.getText(); // Sets id value
    		connect.setVisible(true); // Sets elements for connecting to server to visible
    		addressField.setVisible(true); 
    		portField.setVisible(true);
    		login.setVisible(false); // Hides obsolete elements
    		data.setVisible(false);
    		write("Username set to: " + id); // Informs user of successful entry, in chat panel
    	}
    }
	
	/**
	 * Handles disconnect requests from user
	 *
	 */
	private class ButtonHandler implements ActionListener
    {
		/**
		 * Handles action upon button click depending on source
		 */
        public void actionPerformed(ActionEvent action)
        {
            if (action.getSource() == disconnect) // Different actions depending on source
            {
            	client.disconnect(); // Calls disconnect method in Client object
            	System.exit(0); // Exits system
            }
            else if (action.getSource() == connect)
            	{
            		connect(); // Calls connect method to create connection to server
            	}
            else // Last alternative is login
            {
            	login(); // Login sets the user's username and sets connects buttons and fields to visible
            }
        }
        
        
    }
	
	/**
	 * Handles textfield entries
	 */
	private class TextFieldHandler implements ActionListener
    {
		/**
		 * Calls send method in Client object to send contents of textfield to server
		 * Resets contents of textfield
		 */
        public void actionPerformed(ActionEvent action)
        {
        	client.send(message.getText()); 
        	message.setText(""); // Resets textfield value
        }
    }
	
}
