package client;

import com.google.gson.Gson;
import common.GsonFactory;
import common.protocol.Request;
import common.protocol.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Thin wrapper around the TCP socket. This is the class a future GUI frontend
 * should call directly (send(Request) -> Response) instead of going through
 * the CLI text parser, which only exists for the terminal test client.
 */
public class ServerConnection implements AutoCloseable {
    private static final Gson GSON = GsonFactory.forNetwork();

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public ServerConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public synchronized Response send(Request request) throws IOException {
        out.println(GSON.toJson(request));
        String line = in.readLine();
        if (line == null) {
            throw new IOException("Server closed the connection");
        }
        return GSON.fromJson(line, Response.class);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
