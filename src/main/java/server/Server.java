package server;

import server.command.CommandRegistry;
import server.repository.BoardRepository;
import server.repository.UserRepository;
import server.service.NotificationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final int TCP_PORT = 8080;
    private static final int SHUTDOWN_AWAIT_SECONDS = 10;

    public static void main(String[] args) throws IOException {
        Storage storage = new Storage();
        UserRepository userRepository = new UserRepository(storage);
        BoardRepository boardRepository = new BoardRepository(storage);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        NotificationService notificationService = new NotificationService();
        CommandRegistry registry = CommandRegistryFactory.create(userRepository, boardRepository, notificationService);

        ServerSocket serverSocket = new ServerSocket(TCP_PORT);

        // The previous version called executor.shutdown() in a finally block that, in
        // practice, was only ever reached on a startup IOException - stopping the process
        // any other way (Ctrl+C, IDE stop button) killed in-flight requests mid-handling
        // with no chance to finish. This hook gives them up to SHUTDOWN_AWAIT_SECONDS.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(serverSocket, executor)));

        LOGGER.info("Server listening on TCP port " + TCP_PORT);
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ClientHandler(clientSocket, registry));
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                LOGGER.log(Level.SEVERE, "Server stopped unexpectedly", e);
            }
            // else: serverSocket.close() was called by the shutdown hook - expected.
        }
    }

    private static void shutdown(ServerSocket serverSocket, ExecutorService executor) {
        LOGGER.info("Shutting down: waiting for in-flight requests to finish...");
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // already shutting down, nothing actionable
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
