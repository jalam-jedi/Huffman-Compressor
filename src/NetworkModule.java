import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;

public class NetworkModule {

    // 1. SENDER LOGIC (Client)
    public static void sendFile(File file, String ipAddress, int port, JTextArea logArea) {
        new Thread(() -> {
            try {
                logArea.append("📡 Connecting to " + ipAddress + ":" + port + "...\n");
                try (Socket socket = new Socket(ipAddress, port);
                     FileInputStream fis = new FileInputStream(file);
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

                    // Send Filename and Size first
                    dos.writeUTF(file.getName());
                    dos.writeLong(file.length());

                    byte[] buffer = new byte[4096];
                    int read;
                    long totalSent = 0;
                    long fileSize = file.length();

                    logArea.append("🚀 Sending data...\n");
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                        totalSent += read;
                    }
                    logArea.append("✅ Transfer Complete!\n");
                }
            } catch (Exception e) {
                logArea.append("❌ Network Error: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }).start();
    }

    // 2. RECEIVER LOGIC (Server)
    public static void startServer(int port, File saveDir, JTextArea logArea) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                logArea.append("🎧 Waiting for files on port " + port + "...\n");
                
                while (true) {
                    Socket socket = serverSocket.accept();
                    logArea.append("🔗 Connection from " + socket.getInetAddress() + "\n");
                    
                    try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                        String fileName = dis.readUTF();
                        long fileSize = dis.readLong();
                        
                        File saveFile = new File(saveDir, fileName);
                        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                            byte[] buffer = new byte[4096];
                            int read;
                            long remaining = fileSize;
                            
                            while (remaining > 0 && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {
                                fos.write(buffer, 0, read);
                                remaining -= read;
                            }
                        }
                        logArea.append("💾 Received: " + fileName + "\n");
                        JOptionPane.showMessageDialog(null, "File Received: " + fileName);
                    }
                }
            } catch (Exception e) {
                logArea.append("❌ Server Error: " + e.getMessage() + "\n");
            }
        }).start();
    }
}