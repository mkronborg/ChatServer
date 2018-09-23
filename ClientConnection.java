/**
 * Version 3.2
 * Author: MK2022
 * Date: 06/03/2018
 * 
 * The Client class handles connections to MultiClientServer objects, and passes received messages to its usingGUI object, and sends messages from the usingGUI to the server
 */
import java.io.BufferedReader; // Necessary imports
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class ClientConnection implements Runnable
{
	
	private Socket server;
	private ChatClient userConsole;
	private ClientGUI userGUI;
	private BufferedReader serverIn;
	private PrintWriter out;
	private boolean thread;
	private boolean usingGUI;
	
	/**
	 * Constructor for Client object where GUI is not in use
	 * @param cg
	 * 			ChatGUI object
	 */
	public ClientConnection(ClientGUI cg)
	{
		userGUI = cg;  // ChatClient object that created this Client object
		thread = true;
		usingGUI = true;
	}
	
	/**
	 * Constructor for Client object when a GUI is not used
	 * @param cc
	 * 			ChatClient object
	 */
	public ClientConnection(ChatClient cc)
	{
		userConsole = cc;  // ChatClient object that created this Client object
		thread = true;
		usingGUI = false;
	}
	
	/**
	 * Sends user ID to server for informing other connected clients that user has connected
	 * @param id
	 * 			String to set user ID to
	 */
	public void setID(String id)
	{
		out.println(id); // Prints the user ID to the server, which is written in front of all messages from the user
	}

	
	/**
	 * Attempts to connect to server
	 * @return
	 * 		True if successful connection, otherwise false
	 */
	public synchronized boolean connect(String address, int port)
	{
		try {
			server = new Socket(address, port); // Connects to server
			serverIn = new BufferedReader(new InputStreamReader(server.getInputStream())); // Creates input and output streams
			out = new PrintWriter(server.getOutputStream(), true);
			
			return true;
		}catch (ConnectException e) 
		{
			return false; // Acceptable failure, informs usingGUI
		} 
		catch (UnknownHostException e) 
		{
			return false;
		} catch (IOException e) 
		{	
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Method to set variable in thread to false, ending the loop, and then the connection
	 */
	public synchronized void disconnect()
	{
		thread = false;
	}
	
	/**
	 * Sends a message to the user
	 * @param message
	 * 			Message to be displayed
	 */
	public synchronized void write(String message)
	{
		if (usingGUI) // Depending on whether a GUI is used, different methods are called
		{
			userGUI.write(message);
		}
		else
		{
			userConsole.write(message);
		}
	}
	
	/**
	 * Closes the various input and output streams as well as the socket
	 */
	public synchronized void close()
	{
		try {
			out.close();
			server.close();
			serverIn.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a string to the server
	 * @param message
	 * 			Message to be sent
	 */
	public synchronized void send(String message)
	{
		out.println(message);
	}
	
	/**
	 * run method of Client object, listens for transmissions from server and sends them to usingGUI
	 */
	public void run()
	{
		try {
			String serverRes;
			
			while (thread) // When variable is false, user has requested a disconnect
			{
				serverRes = serverIn.readLine(); // Reads messages from server and sends to GUI
				if (serverRes == null)
					thread = false;
				write(serverRes);
				Thread.sleep(250);
			}
			send("Disconnect me"); // Sends message with passcode so server can shut down correctly rather than catch an exception, though that also works
		} catch (SocketException e) 
		{
			// Not much to do here, finally block will handle it
		}catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally
		{
			close(); // Closes the various connections
			write("Connection has been lost"); // Informs user
		}
	}
	
}
