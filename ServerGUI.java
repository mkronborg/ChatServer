/**
 * Version 1.1
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


public class ServerGUI extends JFrame 
{
	
	private JTextArea chat; // Displays chat messages
     private JTextField portField; // TextField for user to enter desired message
    private JButton disconnect; // Button to disconnect
    private JButton connect; // Button to connect
    private JLabel info; // Label to inform user of failure to connect
    private ChatServer cs;
    private int port;
	    
	/**
	 * Constructor for ServerGUI object, sets up GUI
	 * @param port
	 * 		Port used for connecting to server
	 * @param cs
	 * 			ChatServer object being used, 
	 */
	public ServerGUI(int port, ChatServer cs)
	{
		super("Server"); // Calls superclass constructor
		
		this.cs = cs;
		this.port = port;
		
		chat = new JTextArea("Welcome to the Chat room\n", 80, 80); // Sets up elements in GUI
		JPanel chatPanel = new JPanel(new GridLayout(1,1));
		chatPanel.add(new JScrollPane(chat)); // Using a JScrollPane, messages will continue to be able to fit in the window
		chat.setEditable(false);
		add(chatPanel, BorderLayout.CENTER); 
		
		JPanel messagePanel = new JPanel();
		disconnect = new JButton("Exit"); // Adds disconnect and message elements to other section of window
		connect = new JButton("Connect"); // Adds disconnect, connect, info and message elements to other section of window
		portField = new JTextField("" + port, 5); // Sets port entry prior to user entry
		info = new JLabel("");
		
		messagePanel.add(info);
		messagePanel.add(portField);
		messagePanel.add(connect);
		messagePanel.add(disconnect);
		add(messagePanel, BorderLayout.SOUTH);
		
		disconnect.setVisible(false); // Disconnect and Message should not be available until a successful connection has been made

        ButtonHandler bh = new ButtonHandler(); // Creates handler for the disconnect button
        disconnect.addActionListener(bh);
        connect.addActionListener(bh);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		
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
	 * Handles disconnect requests from user
	 *
	 */
	private class ButtonHandler implements ActionListener
    {
		/**
		 * Stops thread in Client object, which informs server of disconnect, making server shut down connection, then shuts down client side of connection.
		 * Then closes system.
		 */
        public void actionPerformed(ActionEvent action)
        {
            if (action.getSource() == disconnect) // Different actions depending on source
            {
            	cs.disconnect(); // Shuts down server if requested to do so
            }
            else 
            	{
            		if (!portField.getText().equals("") && isInt(portField.getText())) // Ensures valid entry, in terms of content and format
            		{
            			port = Integer.parseInt(portField.getText()); // Gets number in port field
            			if (cs.startServer(port)) // If starting server with the given port was successful, user is informed GUI elements are updated
                		{
                			write("ServerSocket object created on port: " + portField.getText()); // 
                			disconnect.setVisible(true);
                			portField.setVisible(false);
                			connect.setVisible(false);
                		}
                		else
                		{
                			info.setText("Failure to create ServerSocket object"); // Informs user of failure
                			info.setForeground(Color.RED);
                			write("ServerSocket object could not be created, attempt with different port:");
                		}
            		}
            		else
            		{
            			info.setText("Invalid port entry"); // Informs user of invalid entry
            			info.setForeground(Color.RED);
            		}
            		
            	}
        }
    }
}
