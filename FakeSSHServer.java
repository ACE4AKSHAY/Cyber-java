import java.io.*;
import java.net.*;

public class FakeSSHServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(22);
        System.out.println("Fake SSH server running on port 22...");

        while (true) {
            Socket client = serverSocket.accept();
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println("SSH-2.0-OpenSSH_7.6p1 Ubuntu-4ubuntu0.3");
            client.close();
            serverSocket.close();
      }
    }
}
