import java.io.*;
import java.net.*;

public class RATServer {
    public static void main(String[] args) {
        int port = 4444;
        System.out.println("RATServer: Listening on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("RATServer: Connection from " + clientSocket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Connected to server.");
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Client says: " + line);
            }

            clientSocket.close();
            System.out.println("RATServer: Connection closed.");
        } catch (IOException e) {
            System.err.println("RATServer Error: " + e.getMessage());
        }
    }
}
