//package client;
//public class UdpListener implements Runnable {
//    @Override
//    public void run() {
//        try (DatagramSocket socket = new DatagramSocket(12345)) {
//            byte[] buffer = new byte[1024];
//            while (true) {
//                try {
//                  DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                    socket.receive(packet);
//                    String message = new String(packet.getData(), 0, packet.getLength());
//                    System.out.println("UDP Notification: " + message);
//                } catch (IOException e) {
//                    System.err.println("Error receiving UDP packet: " + e.getMessage());
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Failed to initialize UDP socket: " + e.getMessage());
//        }
//    }
//}

package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpListener implements Runnable {
    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(12345)) {
            System.out.println("UDP Listener started on port 12345");
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("\n[Notification] " + message);
            }
        } catch (IOException e) {
            System.err.println("UDP listener error: " + e.getMessage());
        }
    }
}