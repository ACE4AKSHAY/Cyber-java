import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SunHTTPServer {

    public static void main(String[] args) throws IOException {
        // 1. Create a server listening on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
        // 2. Define the route and the handler (what to do when user accesses "/")
        server.createContext("/", new MyHandler());
        
        // 3. Start the server
        server.setExecutor(null); // default executor
        server.start();
        System.out.println("Server started on http://localhost:8000/");
    }

    // 4. Define what happens when a client accesses the server
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello from Java Server";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
