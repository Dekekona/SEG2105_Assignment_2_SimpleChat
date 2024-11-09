package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.IOException;

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5556;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }


  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  @Override
  protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
      String message = msg.toString();

      try {
          // Check if the message is a login command
          if (message.startsWith("#login ")) {
              // Ensure #login is the first command received
              if (client.getInfo("loginId") != null) {
                  client.sendToClient("Error: Already logged in.");
                  client.close();
                  return;
              }

              // Set login ID and notify successful login
              String loginId = message.substring(7).trim();
              client.setInfo("loginId", loginId);
              System.out.println("Client " + loginId + " has logged in.");
              client.sendToClient("Login successful as " + loginId);
          } else {
              // Ensure client has logged in before sending messages
              if (client.getInfo("loginId") == null) {
                  client.sendToClient("Error: Login required.");
                  client.close();
                  return;
              }

              // Retrieve login ID for message prefix
              String loginId = client.getInfo("loginId").toString();
              String prefixedMessage = loginId + ": " + message;

              System.out.println("Message received from " + loginId + ": " + message);
              sendToAllClients(prefixedMessage); // Broadcast prefixed message
          }
      } catch (IOException e) {
          System.out.println("Error: Could not send message to client.");
      }
  }

    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
  /**
   * Exercise 1c
   * 
   * This method is called when a client connects to the server.
   * Displays a message indicating the client's IP address.
   *
   * @param client The client that connected.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
	  System.out.println("Client connected:" + client.getInetAddress().getHostAddress()); // InetAddress class provides methods to get the IP address of any hostname
  }
  
  /**
   * Exercise 1c
   * 
   * This method is called when a client disconnects from the server.
   * Displays a message indicating the client's IP address.
   * Marking the method here as "synchronized" ensures thread-safety and avoids possible errors from simultaneous changes
   *
   * @param client The client that disconnected.
   */
  @Override
  protected void clientDisconnected(ConnectionToClient client) {
      String loginId = (String) client.getInfo("loginId");
      System.out.println("Client " + (loginId != null ? loginId : "unknown") + " has disconnected.");
  }


  

  /**
   * Gracefully shuts down the server.
   * Stops listening for new clients, disconnects all current clients,
   * and then exits the application.
   */
  public void quit() {
      try {
          // Stop listening for new clients and close the server socket
          if (isListening()) {
              stopListening(); // Stop accepting new connections
          }
          
          // Disconnect all clients connected to this server
          close(); // This will disconnect all clients and close the server

          System.out.println("Server is shutting down.");
      } catch (IOException e) {
          System.out.println("Error closing the server: " + e.getMessage());
      } finally {
          // Exit the application gracefully
          System.exit(0);
      }
  }
}
//End of EchoServer class
