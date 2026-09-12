package client;

import common.protocol.Request;
import common.protocol.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.util.Map;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        try (ServerConnection connection = new ServerConnection(HOST, PORT);
             DatagramSocket udpSocket = new DatagramSocket(); // port 0 -> OS assigns a free ephemeral port
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            Thread udpThread = new Thread(new UdpListener(udpSocket));
            udpThread.setDaemon(true);
            udpThread.start();

            System.out.println("Connected to server. Type commands (e.g. 'login alice secret'), or 'exit' to quit.");

            String line;
            while ((line = stdIn.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.equalsIgnoreCase("exit")) break;

                try {
                    Request request = CommandLineParser.parse(line);
                    Response response = connection.send(request);
                    System.out.println(response.getStatus() + ": " + response.getMessage()
                            + (response.getData() != null ? " " + response.getData() : ""));

                    if ("login".equals(request.getCommand()) && response.isSuccess()) {
                        registerUdpEndpoint(connection, udpSocket);
                    }
                } catch (Exception e) {
                    System.out.println("error: " + e.getMessage());
                }
            }
        }
    }

    private static void registerUdpEndpoint(ServerConnection connection, DatagramSocket udpSocket) throws IOException {
        connection.send(new Request("register_udp", Map.of("port", String.valueOf(udpSocket.getLocalPort()))));
    }
}
