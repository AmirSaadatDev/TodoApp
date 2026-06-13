//package client;
//
//import java.io.*;
//import java.net.*;
//
//public class TcpListener implements Runnable {
//    @Override
//    public void run() {
//        try (ServerSocket serverSocket = new ServerSocket(8080)) {
//            System.out.println("TCP Listener started on port 8080");
//            while (true) {
//                try {
//                    Socket clientSocket = serverSocket.accept();
//                    new Thread(() -> {
//                        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
//                            String line;
//                            while ((line = in.readLine()) != null) {
//                                System.out.println("TCP Received: " + line);
//                                out.println("TCP Response:" + line);
//                            }
//                        } catch (IOException e) {
//                            System.err.println("Error handling TCP : " + e.getMessage());
//                        }
//                    }).start();
//                } catch (IOException e) {
//                    System.err.println("Error accepting TCP connection: " + e.getMessage());
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Failed to initialize TCP socket: " + e.getMessage());
//        }
//    }
//}
package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TcpListener {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected to server on port 8080");

            while (true) {
                System.out.print("> ");
                String command = sc.nextLine();
                if (command.equalsIgnoreCase("exit")) break;

                out.println(command);
                String response = in.readLine();
                System.out.println("Server: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
