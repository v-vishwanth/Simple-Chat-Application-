import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {

  private static final int PORT = 12345;
  private static final Map<String, PrintWriter> clientMap = new ConcurrentHashMap<>();
  private static final List<String> chatHistory = new ArrayList<>();

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("Chat server is running...");

      // Ask for the number of users
      BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Enter the number of users: ");
      int numUsers = Integer.parseInt(consoleIn.readLine());

      // Ask for the names of the users
      for (int i = 0; i < numUsers; i++) {
        System.out.print("Enter the name of user " + (i + 1) + ": ");
        String username = consoleIn.readLine();
        clientMap.put(username, null);
      }

      while (true) {
        new ClientHandler(serverSocket.accept()).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
      this.clientSocket = socket;
    }

    public void run() {
      try {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Send the username to the client
        out.println(username);

        // Send chat history to the new client
        for (String message : chatHistory) {
          out.println(message);
        }

        String message;
        while ((message = in.readLine()) != null) {
          if (message.equals("exit")) {
            // Handle the user exit here
            System.out.println(username + " has left the chat.");
            broadcast(username + " has left the chat.");
            break; // Exit the loop
          } else {
            chatHistory.add(username + ": " + message);
            broadcast(username + ": " + message);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          clientSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        clientMap.remove(username);
        System.out.println(username + " has left the chat.");
        broadcast(username + " has left the chat.");
      }
    }

    private void broadcast(String message) {
      for (PrintWriter writer : clientMap.values()) {
        if (writer != null) {
          writer.println(message);
        }
      }
    }
  }
}