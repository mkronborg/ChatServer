import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Version 1.3
 * Author: MK2022
 * Date: 06/03/2018
 * 
 * GUI to handle displaying messages received by a Client object, and for receiving messages for a Client object to transmit to server.
 * Also displays to the user if clients connect or disconnect, or if the connection to the server has dropped, 
 * and provides a button for the user to disconnect from the server.
 */


public class ChatClient implements Runnable
{
	private ClientConnection connection;
	
	 private boolean thread;
	 private int port;
	 private String address;
	 private BufferedReader userIn;
	/**
	 * Constructor for ClientGUI object, creates Client object, starts Thread for listening for server, and sets up GUI
	 * @param port
	 * 		Port used for connecting to server
	 * @param address
	 * 		Address used for connecting to server
	 * @param id
	 * 		Username to be displayed in chat, is handled by Client object for object oriented principle reasons
	 */
	public ChatClient(int port, String address, boolean gui)
	{
		if (gui)
			new ClientGUI(port, address);
		else // If the user has requested a GUI
		{
			this.port = port;
			this.address = address;
			connection = new ClientConnection(this);
			thread = true;
			new Thread(this).start(); // Thread to listen for user input
		}
	}
	
	/**
	 * Prerequisites for run method, sets up BuffereReader, sets username and calls connect method
	 */
	public void setup()
	{
		try 
		{
			userIn = new BufferedReader(new InputStreamReader(System.in)); // Reads from command line

			String input;
			do
			{
				write("Please enter your username:"); // Prompts user for usename
				input = userIn.readLine();
				
			}while(input.equals(""));
			String id = input; // Saves username until connection is made
			write("Would you like to connect?");
			boolean failure;
			do // Double do-while loop, one to ensure successful connection, and the inner to get a Y/N response from user
			{
				do
				{
					write("Please enter 'Y' or 'N':");
					input = userIn.readLine();
				}while ((input != null && !input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"))); // Listens until user enters exit in terminal to end connection
				
				if (input.equalsIgnoreCase("n")) // If user does not want to connect then system is closed
					System.exit(0);
				failure = !connection.connect(address, port); // successful connection returns true
				
				if (failure)
					write("Failure to connect to server. Try again?"); // Prompts user to reattempt
			}while (failure);
			
			connection.setID(id); // Sends username to server
			
			new Thread(connection).start(); // Starts a thread listening to server

			write("To disconnect, please enter \"Disconnect Me\" and then confirm when prompted:");
			write("Welcome to the chatroom:");
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Prompts the user to confirm a request to disconnect
	 * @return
	 * 			Whether the user wants to disconnect
	 */
	public boolean confirmDisconnect()
	{
		try
		{
			String input;
			write("Are you sure you want to disconnect?");
			do
			{
				write("Please enter 'Y' or 'N':");
				input = userIn.readLine();
			
			}while ((input != null && !input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"))); // Listens until user enters a correct format of answer
			
		if (input.equalsIgnoreCase("y"))
			return true; // Returns appropriate value
		else 
			return false;
		
		}catch (IOException e) 
		{
				e.printStackTrace();
				return true; // return statement required
		}
	}
	
	/**
	 * Run method of ChatClient. Only runs if GUI is not in use. Listens for input from the console
	 */
	public void run()
	{
		setup(); // Prerequisites for run method
		try 
		{
			while (thread)
			{
				String input = userIn.readLine();
				if (input.equals("Disconnect Me"))
				{
					if (confirmDisconnect())
					{
						connection.disconnect();
						thread = false;
					}
				}
				if (input != null && !input.equals("") && thread) // if the thread variable is stil true, then transmission is sent.
					connection.send(input); // Sends message to server
				Thread.sleep(250);
			}
			System.exit(0);
		} catch (IOException e) 
		{
			e.printStackTrace();
		} catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to write to terminal, used by a ClientConnection object, and the ChatClient object itself
	 * @param msg
	 * 			Message to be printed
	 */
	public void write(String msg)
	{
		System.out.println(msg);
	}
	
	/**
	 * Checks if a given string is a number
	 * @param str
	 * 			String to be checked
	 * @return
	 * 			Boolean denoting whether string is a digit
	 */
	public static boolean isInt(String str) 
	{
		for (int i = 0; i < str.length(); i++)
		{
			if (!Character.isDigit(str.charAt(i))) // checks for a char that is not a digit
					return false;
		}
		return true; // If loop completes then only digits were in the string
	}

	
	/**
	 * Main method, starting point of sever chat program
	 * @param args
	 * 		In the case of an alternative selection of address and port other than the default, these are changed
	 */
	public static void main(String[]args) 
	{
		String address = "localhost"; // Sets default values
		int port = 14001;
		
		if (args.length >= 2) // ensures arguments are not null
		{
			if (args[0].equals("-cca")) // Checks for address entry
			{
				address = args[1]; // sets new address
				if (args.length >= 4) // Checks for accompanying port change
				{
					if (args[2].equals("-ccp"))
					{	
						if(isInt(args[3]))
						port = Integer.parseInt(args[3]); // sets port value
						else
							System.out.println("You failed to enter a valid port value");
					}
				}
			}
			else if (args[0].equals("-ccp")) // Checks for port entry
				port = Integer.parseInt(args[1]);// sets port value
		}
		try 
		{
			BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in)); // Reads from command line
		
			System.out.println("Would you like to use a GUI");
			String input;
			do
			{
				System.out.println("Please enter 'Y' or 'N':");
				input = userIn.readLine();
			}while (input != null && !input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")); // Listens until user enters exit in terminal to end connection
		
			boolean gui;
		
			if (input.equalsIgnoreCase("y"))
				gui = true;
			else
				gui = false;
			
			new ChatClient(port, address, gui); // Creates ChatClient object
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
}
