import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Base64;

public class AuthHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new AuthHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("HTTP server with Basic Auth started on http://localhost:8000/");
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            String username = "admin";
            String password = "password123";

            if (auth == null || !isAuthorized(auth, username, password)) {
                String unauthorized = "401 Unauthorized - Login Required";
                byte[] unauthorizedBytes = unauthorized.getBytes("UTF-8");

                exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"MyServer\"");
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(401, unauthorizedBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(unauthorizedBytes);
                }
                return;
            }

            String response = "✅ Access Granted to protected server";
            byte[] responseBytes = response.getBytes("UTF-8");

            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private boolean isAuthorized(String authHeader, String user, String pass) {
            if (!authHeader.startsWith("Basic ")) return false;
            String base64Credentials = authHeader.substring("Basic ".length());
            String decoded = new String(Base64.getDecoder().decode(base64Credentials));
            return decoded.equals(user + ":" + pass);
        }
    }
}
