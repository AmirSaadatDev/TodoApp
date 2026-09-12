package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Listens on whatever ephemeral port the client's own DatagramSocket was bound to.
 * Using an ephemeral port (instead of the old hardcoded 12345) means multiple
 * clients can run on the same machine without a BindException.
 */
public class UdpListener implements Runnable {
    private final DatagramSocket socket;

    public UdpListener(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        System.out.println("UDP listener ready on port " + socket.getLocalPort());
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                if (packet.getLength() == buffer.length) {
                    // A packet that exactly fills the buffer may have been silently
                    // truncated by the OS - UDP gives no way to tell after the fact,
                    // so at least warn instead of showing a cut-off message with no clue why.
                    System.err.println("Warning: notification may be truncated (>= "
                            + buffer.length + " bytes)");
                }
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("\n[Notification] " + message);
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("UDP listener error: " + e.getMessage());
                }
            }
        }
    }
}
