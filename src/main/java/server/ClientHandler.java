package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import common.GsonFactory;
import common.exception.AuthRequiredException;
import common.exception.ProtocolException;
import common.protocol.Request;
import common.protocol.Response;
import server.command.Command;
import server.command.CommandRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Every request now goes through a single try/catch that always produces a Response,
 * instead of letting ArrayIndexOutOfBoundsException / NumberFormatException /
 * IllegalArgumentException propagate out of run() and silently kill the thread
 * (which is what happened before on any malformed input).
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final Gson GSON = GsonFactory.forNetwork();

    // Without this, a client that opens a connection and never sends anything blocks its
    // handler thread on in.readLine() forever. With a fixed-size pool (10 threads), just
    // 10 such idle connections exhaust the server's entire capacity for real clients.
    private static final int IDLE_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    private final Socket clientSocket;
    private final CommandRegistry registry;

    public ClientHandler(Socket clientSocket, CommandRegistry registry) {
        this.clientSocket = clientSocket;
        this.registry = registry;
    }

    @Override
    public void run() {
        ClientSession session = new ClientSession(clientSocket.getInetAddress());
        try {
            clientSocket.setSoTimeout(IDLE_TIMEOUT_MS);
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, "Could not set idle timeout on client socket", e);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                out.println(GSON.toJson(process(line, session)));
            }
        } catch (SocketTimeoutException e) {
            LOGGER.log(Level.INFO, "Closing idle connection (no activity for {0} ms)", IDLE_TIMEOUT_MS);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Connection closed: {0}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
                // already closing; nothing actionable
            }
        }
    }

    private Response process(String line, ClientSession session) {
        try {
            Request request = GSON.fromJson(line, Request.class);
            if (request == null || request.getCommand() == null) {
                return Response.error("Invalid request");
            }
            Command command = registry.find(request.getCommand());
            if (command == null) {
                return Response.error("Unknown command");
            }
            return command.execute(request, session);
        } catch (JsonSyntaxException e) {
            return Response.error("Malformed request, expected JSON");
        } catch (AuthRequiredException | ProtocolException e) {
            return Response.error(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error handling request: " + line, e);
            return Response.error("Internal server error");
        }
    }
}
