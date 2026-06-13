package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int TCP_PORT = 8080;
    private static DatabaseManager dbManager;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        dbManager = new DatabaseManager();

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Server listening on TCP port " + TCP_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ClientHandler(clientSocket, dbManager));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}






//package server;
//
//import java.io.*;
//import java.net.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class Server {
//    private static final int TCP_PORT = 8080;
//    private static final int UDP_PORT = 12345;
//    private static DatabaseManager dbManager;
//
//    public static void main(String[] args) {
//        dbManager = new DatabaseManager();
//        new Thread(new TcpServer()).start();
//        new Thread(new UdpListener()).start();
//    }
//
//    static class TcpServer implements Runnable {
//        @Override
//        public void run() {
//            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
//                System.out.println("Server listening on port " + TCP_PORT);
//                while (true) {
//                    Socket clientSocket = serverSocket.accept();
//                    new Thread(new ClientHandler(clientSocket, dbManager)).start();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
