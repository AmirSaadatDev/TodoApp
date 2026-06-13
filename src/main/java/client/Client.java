
package client;
import java.io.*;
import java.net.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Client {
    public static void main(String[] args) {
        new Thread(new UdpListener()).start();

        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to TCP server. Enter commands:");
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server (TCP): " + response);
            }
        } catch (IOException e) {
            System.err.println("TCP connection error: " + e.getMessage());
        }
    }
    private static class UdpListener implements Runnable {
        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(12345)) {
                System.out.println("UDP Listener started on port 12345");
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("UDP Notification: " + message);
                }
            } catch (IOException e) {
                System.err.println("UDP listener error: " + e.getMessage());
            }
        }
    }
}
