// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

import edu.seg2105.client.ui.ClientConsole;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  
  /**
   * The login ID for this client.
   */
  private String loginId;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param loginId The login ID of the client.
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  public ChatClient(String loginId, String host, int port, ChatIF clientUI) 
    throws IOException {
    super(host, port); // Call the superclass constructor
    this.clientUI = clientUI;
    this.loginId = loginId; // Store the login ID
    openConnection();
    
    // Send #login <loginId> to the server after connection is opened
    sendToServer("#login " + loginId);
  }
  
  /**
   * The main entry point for the ChatClient application.
   * Allows specifying the login ID, host, and port as command-line arguments.
   * If no login ID is provided, the client will terminate.
   *
   * @param args Command-line arguments where args[0] is the login ID,
   *             args[1] is the host (optional), and args[2] is the port (optional).
   */
  public static void main(String[] args) {
    // Default values for host and port
    String host = "localhost"; // Default host
    int port = 5555; // Default port
    
    // Check if the login ID is provided
    if (args.length < 1) {
      System.out.println("ERROR - No login ID specified. Connection aborted.");
      System.exit(1); // Exit if no login ID is provided
    }
    
    String loginId = args[0]; // First argument is the login ID

    // Check if host and port are provided
    if (args.length > 1) {
      host = args[1];
    }
    if (args.length > 2) {
      try {
        port = Integer.parseInt(args[2]);
      } catch (NumberFormatException e) {
        System.out.println("ERROR - Invalid port number. Using default port: " + port);
      }
    }

    // Create a client console instance to interface with the user
    ClientConsole chat = new ClientConsole(loginId, host, port); // Pass loginId
    chat.accept(); // Wait for console input
  }
      
  

  // Other instance methods (e.g., handleMessageFromServer, quit, etc.) follow here

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
    
    
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message) {
	    // Check if the message is a command (starts with '#')
	    if (message.startsWith("#")) {
	        processCommand(message); // Process command if it starts with '#'
	    } else {
	        try {
	            sendToServer(message); // Send regular messages to the server
	        } catch(IOException e) {
	            clientUI.display("Could not send message to server. Terminating client.");
	            quit(); // Quit if message cannot be sent to the server
	        }
	    }
	}

  
  /**
   * This method terminates the client.
   */
  
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
  
  
  /**
   * Exercise 1a
   * This method detects when the server connection is closed and terminates the client as a result 
   */
  
  @Override
  protected void connectionClosed() {
      clientUI.display("Server has shut down."); // Notifies the user that the server is no longer connected
      quit(); // Terminates the client
  }

  /**
   * Exercise 1a
   * Called when an exception occurs during server communication.
   * This method displays an error message specifying the exception that occurred...
   * ...Then it terminates the client as a result
   *
   * @param exception The exception that occurred.
   */
  @Override
  protected void connectionException(Exception exception) {
      clientUI.display("Server connection error: " + exception.getMessage()); // Displays a message specifying the exception that occurred 
      quit(); // Terminates the client
  }
  
  /**
   * This method processes client commands that start with '#'.
   * Supported commands include:
   * - #quit: Disconnect and exit the client
   * - #logoff: Disconnect from the server without exiting the client
   * - #sethost <host>: Set a new host (only allowed if disconnected)
   * - #setport <port>: Set a new port (only allowed if disconnected)
   * - #login: Reconnect to the server if currently disconnected
   * - #gethost: Display the current host
   * - #getport: Display the current port
   *
   * @param message The command to process.
   */
  private void processCommand(String message) {
      // Handle the #quit command to disconnect and exit the client
      if (message.equalsIgnoreCase("#quit")) {
          quit(); // Calls quit to close the connection and exit the client
      }
      // Handle the #logOff command to disconnect from the server without exiting
      else if (message.equalsIgnoreCase("#logoff")) {
          try {
              closeConnection(); // Disconnect from the server
              clientUI.display("Logged off from the server.");
          } catch (IOException e) {
              clientUI.display("Error disconnecting from server."); // Notify the user if disconnect fails
          }
      }
      // Handle the #setHost command to set a new host if disconnected
      else if (message.startsWith("#sethost ")) {
          if (!isConnected()) { // Check if disconnected
              setHost(message.substring(9)); // Update the host address to the specified value
              clientUI.display("Host set to: " + getHost());
          } else {
              clientUI.display("Error: Cannot change host while connected."); // Show error if client is connected
          }
      }
      // Handle the #setPort command to set a new port if disconnected
      else if (message.startsWith("#setport ")) {
          if (!isConnected()) { // Check if disconnected
              try {
                  setPort(Integer.parseInt(message.substring(9))); // Update the port number to the specified value
                  clientUI.display("Port set to: " + getPort());
              } catch (NumberFormatException e) {
                  clientUI.display("Invalid port number.");
              }
          } else {
              clientUI.display("Error: Cannot change port while connected."); // Show error if client is connected
          }
      }
      // Handle the #login command to reconnect to the server if disconnected
      else if (message.equalsIgnoreCase("#login")) {
          if (!isConnected()) {
              try {
                  openConnection(); // Reconnect to the server
                  clientUI.display("Logged in to the server.");
              } catch (IOException e) {
                  clientUI.display("Error connecting to server."); // Notify the user if connection fails
              }
          } else {
              clientUI.display("Error: Already connected."); // Show error if client is already connected
          }
      }
      // Handle the #getHost command to display the current host address
      else if (message.equalsIgnoreCase("#gethost")) {
          clientUI.display("Current host: " + getHost()); // Display the current host value
      }
      // Handle the #getPort command to display the current port number
      else if (message.equalsIgnoreCase("#getport")) {
          clientUI.display("Current port: " + getPort()); // Display the current port value
      }
      // Handle any unrecognized command
      else {
          clientUI.display("Unknown command."); // Show message for unknown commands
      }
  }
  
  

}
//End of ChatClient class
