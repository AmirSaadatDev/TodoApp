package server;

import java.net.InetAddress;

/** Per-connection state. One instance per client, replacing raw fields on ClientHandler. */
public class ClientSession {
    private final InetAddress clientAddress;
    private long userId = -1;

    public ClientSession(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public boolean isAuthenticated() {
        return userId != -1;
    }

    public long getUserId() {
        return userId;
    }

    public void login(long userId) {
        this.userId = userId;
    }

    public void logout() {
        this.userId = -1;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }
}
