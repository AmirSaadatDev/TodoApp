package server.service;

import model.Board;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fixes the original UDP push design, which always sent to a hardcoded
 * "localhost:12345" - broken for real (non-loopback) clients, and unusable
 * with more than one client on the same machine since they'd all fight over
 * the same UDP port.
 *
 * Each client now registers its own (address, ephemeral port) after login,
 * and only board owner/members are notified.
 */
public class NotificationService {
    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final Map<Long, InetSocketAddress> endpoints = new ConcurrentHashMap<>();
    private final DatagramSocket socket;

    public NotificationService() throws SocketException {
        this.socket = new DatagramSocket();
    }

    public void registerEndpoint(long userId, InetAddress address, int port) {
        endpoints.put(userId, new InetSocketAddress(address, port));
    }

    /** Called on logout so a user stops receiving board pushes until they log back in. */
    public void removeEndpoint(long userId) {
        endpoints.remove(userId);
    }

    public void notifyBoardMembers(Board board, String message) {
        Set<Long> recipients = new LinkedHashSet<>(board.getMemberIds());
        recipients.add(board.getOwnerId());

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        for (Long userId : recipients) {
            InetSocketAddress address = endpoints.get(userId);
            if (address == null) {
                continue; // client hasn't registered a UDP endpoint (yet)
            }
            try {
                socket.send(new DatagramPacket(data, data.length, address));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not push notification to user " + userId, e);
            }
        }
    }
}
