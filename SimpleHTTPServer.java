import java.io.*;
import java.net.*;

class SimpleHTTPServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080,50,InetAddress.getByName("0.0.0.0"));
        System.out.println("HTTP Server running on port 8080...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while (!(line = in.readLine()).isEmpty()) {
                System.out.println("Received: " + line);
            }

            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" +
                    "<html><body><h1>Hello from Java HTTP Server!</h1></body></html>";
            out.write(response);
            out.flush();

            clientSocket.close();
            serverSocket.close();
        }
    }
}
