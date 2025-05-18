from http.server import BaseHTTPRequestHandler, HTTPServer

class SimpleHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(b"<h1>Hello from Simple HTTP Server</h1>")

server = HTTPServer(("0.0.0.0", 8080), SimpleHandler)
print("Server started at http://0.0.0.0:8080")
server.serve_forever()
