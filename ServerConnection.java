/**
 * Version 3.2
 * Author: MK2022
 * Date: 06/03/2018
 * 
 * The MutliClientServer class handles connections to clients, using a thread to listen for messages  which are broadcast to all connected clients
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class ServerConnection implements Runnable
{

	private Socket s; // Socket used in given connection
	private static ArrayList<PrintWriter> connections = new ArrayList<PrintWriter>(); // ArrayList of PrintWriters. Is static so all instances have access to all PrintWriters
	private boolean thread; // Boolean value used in while loop in thread
	private PrintWriter out; // PrintWriter for given connection
	private String id; // User ID
	private ServerGUI sg;
	private boolean gui;
	//private ChatServer cs;
	
	/**
	 * Constructor for MultiClientServer object when a GUI is in use
	 * @param s
	 * 		Socket used for connection
	 */
	public ServerConnection(Socket s, ServerGUI sg)
	{
		this.sg = sg;
		this.s = s;
		this.gui = true;
		thread = true;
	}
	
	/**
	 * Constructor for MultiClientServer object when GUI is not used
	 * @param s
	 * 		Socket used for connection
	 */
	public ServerConnection(Socket s)
	{
		this.s = s;
		this.gui = false;
		thread = true;
	}
	
	/**
	 * Method to stop thread by setting variable to false
	 */
	public synchronized void stopThread()
	{
		thread = false;
	}
	
	/**
	 * Adds an element to ArrayList of PrinWriters
	 */
	private synchronized void addConnection()
	{
		connections.add(out);
	}
	
	/**
	 * Method to return a PrintWriter at a given index in ArrayList
	 * @param connection
	 * 			Index of PrintWriter desired
	 * @return
	 * 			PrintWriter object
	 */
	private static synchronized PrintWriter getConnection(int connection)
	{
		return connections.get(connection);
	}
	
	/**
	 * Prints a message to server output, either console or GUI
	 * @param msg
	 * 			Message to write to GUI
	 */
	private synchronized void output(String msg)
	{
		if (gui)
			sg.write(msg);
		else
			System.out.println(msg);
	}
	
	/**
	 * Method to print a given string to all clients
	 * @param message
	 * 			Message to be broadcast
	 */
	private void printToAll(String message)
	{
		for (int i = 0; i < connections.size(); i++)
		{
			
			getConnection(i).println(message); // Gets a PrintWriter object and prints to it
		}
		output(message);
	}
	
	/**
	 * Method to disconnect the client handled by this instance of a MultiClientServer
	 */
	private synchronized void disconnectClient() 
	{
		for (int i = 0; i < connections.size(); i++)
		{
			if(getConnection(i) == out) // Finds correct index
			{
				try {
					out.close(); // Closes PrintWriter and Socket
					s.close();
				} catch (IOException e) 
				{	
					e.printStackTrace();
				}
				connections.remove(i); // Removes PrintWriter from ArrayList
				break;
			}
		}
		printToAll("User " + id + " has disconnected"); // Informs other users that a user has disconnected
	}
	
	/**
	 * run method in MultiClientServer object, runs a thread to listen for messages from user to be broadcast
	 */
	public void run ()
	{
		try 
		{
			InputStreamReader r = new InputStreamReader(s.getInputStream()); // Creates necessary input and output streams
			BufferedReader clientIn = new BufferedReader(r);
			out = new PrintWriter(s.getOutputStream(), true);
			
			addConnection(); // Adds PrintWriter to ArrayList
			
			id = clientIn.readLine(); // Gets user ID

			
			printToAll("User: " + id + " has connected"); // Informs other users that a new user has connected
			
			while (thread)
			{
				String userInput = clientIn.readLine(); // Reads user input
				
				if (userInput != null &&!userInput.equalsIgnoreCase("Disconnect me")) // Checks for disconnect message. Cannot be copied by user since all user messages have ID added to front of String
				{
					printToAll(id + ": " + userInput); // Prints message to all clients, led by user ID
				}
				else
				{
					thread = false; // Ends loop upon next evaluation of condition
				}
				Thread.sleep(250);
			}
			
		} catch (SocketException e) 
		{
			// Not much to do here, finally block will handle it
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally
		{
			disconnectClient(); // Disconnects client
		}
	}
}
