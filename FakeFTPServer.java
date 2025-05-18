import java.io.*;
import java.net.*;

public class FakeFTPServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(21);
        System.out.println("Fake FTP server running on port 21...");

        while (true) {
            Socket client = serverSocket.accept();
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println("220 FakeFTP 1.0 Server Ready.");
            client.close();
        }
    }
}
