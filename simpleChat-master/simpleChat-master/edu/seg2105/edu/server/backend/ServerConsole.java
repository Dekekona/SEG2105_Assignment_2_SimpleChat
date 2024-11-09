package edu.seg2105.edu.server.backend;

import edu.seg2105.client.common.ChatIF;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


/**
 * This class implements a console interface for the server.
 * It allows the server to accept commands from the console, process them,
 * and broadcast messages to all clients when appropriate.
 */
public class ServerConsole implements ChatIF {
    private EchoServer server;
    private BufferedReader fromConsole;

    /**
     * Constructor for ServerConsole. Sets up the console reader and links to the server instance.
     *
     * @param server The EchoServer instance that will process commands and broadcast messages.
     */
    public ServerConsole(EchoServer server) {
        this.server = server;
        fromConsole = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public static void main(String[] args) {
        int port = 5556; // Default port

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]); // Parse port from arguments
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port " + port);
            }
        }

        EchoServer server = new EchoServer(port);
        ServerConsole serverConsole = new ServerConsole(server);
        
        try {
            server.listen();
            serverConsole.accept(); // Start reading console input for commands
        } catch (IOException e) {
            System.out.println("Error: Could not listen for clients on port " + port);
        }
    }

    /**
     * Starts the console input loop. This method continuously reads input from the console,
     * processes commands if the input starts with '#', or broadcasts messages otherwise.
     */
    public void accept() {
        try {
            String message;
            while (true) {
                message = fromConsole.readLine(); // Read input from the server console
                
                // Check if the message is a command
                if (message.startsWith("#")) {
                    processCommand(message); // Process as command if it starts with '#'
                } else {
                    // Prefix the message with "SERVER MSG>"
                    String serverMessage = "SERVER MSG> " + message;
                    
                    // Display the message on the server console
                    display(serverMessage);
                    
                    // Send the message to all connected clients
                    server.sendToAllClients(serverMessage);
                }
            }
        } catch (IOException e) {
            System.out.println("Unexpected error while reading from console!");
        }
    }


    /**
     * Processes server commands starting with '#'.
     */
    private void processCommand(String command) {
        if (command.equalsIgnoreCase("#quit")) {
            server.quit();
        } else if (command.equalsIgnoreCase("#stop")) {
            server.stopListening();
            display("Server has stopped listening for clients.");
        } else if (command.equalsIgnoreCase("#close")) {
            try {
                server.close();
                display("Server has closed all connections.");
            } catch (IOException e) {
                display("Error: Unable to close server connections.");
            }
        } else if (command.startsWith("#setport ")) {
            if (!server.isListening() && server.getClientConnections().length == 0) {
                int port = Integer.parseInt(command.substring(9));
                server.setPort(port);
                display("Port set to " + port);
            } else {
                display("Error: Cannot change port while server is open.");
            }
        } else if (command.equalsIgnoreCase("#start")) {
            if (!server.isListening()) {
                try {
                    server.listen();
                    display("Server is now listening for clients.");
                } catch (IOException e) {
                    display("Error: Could not start server.");
                }
            } else {
                display("Error: Server is already listening.");
            }
        } else if (command.equalsIgnoreCase("#getport")) {
            display("Current port: " + server.getPort());
        } else {
            display("Unknown command.");
        }
    }

    /**
     * Displays a message on the server console.
     */
    @Override
    public void display(String message) {
        System.out.println(message);
    }
}
