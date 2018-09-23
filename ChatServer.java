/**
 * Version 1.1
 * Author: MK2022
 * Date: 06/03/2018
 * 
 * The ChatServer is the starting point for the Server side of the server chat program
 * It runs a thread to accept connections from clients
 */

import java.io.BufferedReader; // Necessary imports
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class ChatServer implements Runnable
{

	private static ServerSocket in; // ServerSocket object needed for Socket creation
	private ServerConnection mcs; // ServerConnection which handles connections
	private static ArrayList<ServerConnection> connections; // List of ServerConnection objects, used for disconnecting
	private static boolean thread; // Variable to run loop in thread
	private ServerGUI sg; // GUI object to write to
	int port; // port to be used
	private boolean gui;
	
	/**
	 * Constructor for ChatServer
	 * @param port
	 * 			Port to listen to
	 * @param gui
	 * 			Whether a GUI is used
	 */
	public ChatServer(int port, boolean gui)
	{
		this.port = port;
		this.gui = gui;
		if (gui)
			sg = new ServerGUI(port, this); // Creates GUI object
		else
			startServer(port); // Starts server directly
	}
	
	/**
	 * Thread method, waits for connection requests and spawns ServerConnection threads, which handle the connections
	 */
	public void run()
	{
		thread = true;
		connections = new ArrayList<ServerConnection>(); // Initialises ArrayList
		try
		{
			while (thread)
			{
				Socket s = in.accept(); // Waits to accept connection
				
				System.out.println("Server accepted connection on " + in.getLocalPort() + " ; " + s.getPort()); // Informs of successful connections in command line
				if (gui)
					mcs = new ServerConnection(s, sg); //Creates new ServerConnection object
				else
					mcs = new ServerConnection(s);
				addConnection(mcs); // ServerConnection object to arraylist so threads can be handled
				
				new Thread(mcs).start(); // Starts ServerConnection thread to deal with connection
				Thread.sleep(250);
			}
		}catch (SocketException e) 
		{
			// Happens when closing the connections, as loop will be waiting for connect request when ServerSocket object is closed
		} 
		catch (IOException e) 
			{
				e.printStackTrace();
			} catch (InterruptedException e) 
			{
			e.printStackTrace();
		}
		finally
		{
			closeConnections(); // Calls method to close connections to clients
		}
	}
	
	/**
	 * Closes connections to clients
	 * 
	 * Loops through ServerConnection objects and calls methods to change method loop variables, ending the thread and disconnecting from the client
	 * 
	 */
	private synchronized void closeConnections()
	{
		for(int i = 0; i < connections.size(); i++)
		{
			getConnection(i).stopThread();
		}
		try {
			in.close(); // Closes ServerSocket object
			System.exit(0); // Exits system
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a ServerConnection object to the connections ArrayList
	 * @param mcs
	 * 		ServerConnectionobject to be added to the ArrayList
	 */
	private synchronized void addConnection(ServerConnection mcs)
	{
		connections.add(mcs);
	}

	/**
	 * Returns the (i-1)th element in the ArrayList
	 * @param i
	 * 		The index to be accessed
	 * @return
	 * 		(i-1)th ServerConnection object in the ArrayList
	 */
	public synchronized ServerConnection getConnection(int i)
	{
		return connections.get(i);
	}
	
	/**
	 * Instantiates ServerSocket object, and starts thread to listen for connections
	 * @param port
	 * 			Port to connect to
	 * @return
	 * 			Whether action was successful
	 */
	public synchronized boolean startServer(int port)
	{
		try {
			in = new ServerSocket(port); // Creates ServerSocket object
			new Thread(this).start();
			return true;
		} catch (IOException e) 
		{
			return false;
		}
	}
	
	/**
	 * Method to stop server
	 */
	public void disconnect() // Stops system
	{
		closeConnections();
		thread = false;
	}
	
	/**
	 * Main method, checks for changes to the port used to connect through, launches the ChatServer thread to accept connections, and waits for the exit command in the terminal
	 * @param args
	 */
	public static void main(String[]args)
	{
		int port = 14001;
		if (args.length >= 2 && args[0].equalsIgnoreCase("-csp")) // ensures arguments are not null
			port = Integer.parseInt(args[1]); // Assigns port value
		try 
		{
			
			BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in)); // Reads from command line
			
			System.out.println("Would you like to use a GUI"); // Prompts user for GUI use or not
			String input;
			do
			{
				System.out.println("Please enter 'Y' or 'N':");
				input = userIn.readLine();
			}while (input != null && !input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")); // Loops for answer until correct format is received
			
			boolean gui; // Whether the user wants to use a GUI
			
			if (input.equalsIgnoreCase("y"))
				gui = true;
			else
				gui = false;
			ChatServer cs = new ChatServer(port, gui);  // Creates ChatServer object
			
			connections = new ArrayList<ServerConnection>(); // instantiates ArrayList
			
			do
			{
				input = userIn.readLine();
			}while (input != null && !input.equalsIgnoreCase("Exit")); // Listens until user enters exit in terminal to end connection
				cs.disconnect();
			
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
}




