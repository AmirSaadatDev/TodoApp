package server;

import model.Priority;
import model.Status;
import model.Board;
import model.Task;
import model.User;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DatabaseManager dbManager;
    private long userId = -1;

    public ClientHandler(Socket socket, DatabaseManager db) {
        this.clientSocket = socket;
        this.dbManager = db;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                String response = handleRequest(line);
                out.println(response);
                if (line.contains("update_task_status") || line.contains("add_task")) {
                    sendUdpPush("Update received!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleRequest(String request) {
        String[] parts = request.split("\\|");
        String command = parts[0];
        switch (command) {
            case "register":
                String username = parts[1];
                String password = parts[2];
                User user = dbManager.register(username, password);
                return user != null ? "success|User registered" : "error|Username already exists";

            case "login":
                username = parts[1];
                password = parts[2];
                user = dbManager.login(username, password);
                if (user != null) {
                    userId = user.getId();
                    return "success|Logged in successfully";
                }
                return "error|Invalid username or password";

            case "logout":
                userId = -1;
                return "success|Logged out";
            case "create_board":
                if (userId == -1) return "error|You must be logged in";
                String boardName = parts[1];
                Board board = dbManager.createBoard(boardName, userId);
                return board != null ? "success|Board created successfully" : "error|Invalid request";

                case "list_boards":
                if (userId == -1) return "error|You must be logged in";
                StringBuilder boardsList = new StringBuilder();
                for (Board b : dbManager.listBoards(userId)) {
                    boardsList.append(b.getId()).append(",").append(b.getName()).append(";");
                }
                return "success|" + (boardsList.length() > 0 ? boardsList.substring(0, boardsList.length() - 1) : "");

            case "add_user_to_board":
                long boardId = Long.parseLong(parts[1]);
                long userIdToAdd = Long.parseLong(parts[2]);
                Board board1 = dbManager.viewBoard(boardId);
                if (board1 == null) {
                    return "error|Board not found";
                }
                if (checkBoardAccess(board1)) {
                    boolean added = dbManager.addUserToBoard(boardId, userIdToAdd);
                    if (added) {
                        return "success|User added to board successfully";
                    } else {
                        return "error|User already added or invalid request";
                    }
                }
                return "error|access is needed";
            case "view_board":
                boardId = Long.parseLong(parts[1]);
                board = dbManager.viewBoard(boardId);
                if (board != null && checkBoardAccess(board)) {
                    return "success|Board: " + board.getName();
                }
                return "error|access is needed";
            case "add_task":
                if (userId == -1) return "error|You must be logged in";
                boardId = Long.parseLong(parts[1]);
                String title = parts[2];
                String description = parts[3];
                Priority priority = Priority.valueOf(parts[4]);
                board = dbManager.viewBoard(boardId);
                if (board == null) {
                    return "error|Board not found";
                }
                if (checkBoardAccess(board)) {
                    Task task = dbManager.addTask(boardId, title, description, priority);
                    if (task != null) {
                        return "success|Task added successfully";
                    } else {
                        return "error|Task not found";
                    }
                }
                return "error|access is needed";
            case "list_tasks":
                if (userId == -1) return "error|You must be logged in";
                boardId = Long.parseLong(parts[1]);
                StringBuilder tasksList = new StringBuilder();
                for (Task t : dbManager.listTasks(boardId)) {
                    tasksList.append(t.getId()).append(",").append(t.getTitle()).append(";");
                }
                return "success|" + (tasksList.length() > 0 ? tasksList.substring(0, tasksList.length() - 1) : "");
            case "update_task_status":
                long taskId = Long.parseLong(parts[1]);
                Status newStatus = Status.valueOf(parts[2]);
                board = findBoardByTask(taskId);
                if (board == null) {
                    return "error|Board not found";
                }
                if (checkBoardAccess(board)) {
                    boolean updated = dbManager.updateTaskStatus(taskId, newStatus);
                    if (updated) {
                        return "success|Task status updated successfully";
                    } else {
                        return "error|Task not found";
                    }
                }
                return "error|access is needed";
            case "delete_task":
                taskId = Long.parseLong(parts[1]);
                board = findBoardByTask(taskId);
                if (board == null) {
                    return "error|Task not found";
                }
                if (checkBoardAccess(board)) {
                    boolean deleted = dbManager.deleteTask(taskId);
                    if (deleted) {
                        return "success|Task deleted successfully";
                    } else {
                        return "error|Task not found";
                    }
                }
                return "error|access is needed";
            default:
                return "error|Unknown command";
        }
    }

    private boolean checkBoardAccess(Board board) {
        return userId != -1 && (board.getOwnerId() == userId || board.getMemberIds().contains(userId));
    }

    private Board findBoardByTask(long taskId) {
        for (Board b : dbManager.listBoards(userId)) {
            for (Task t : b.getTasks()) {
                if (t.getId() == taskId) {
                    return b;
                }
            }
        }
        return null;
    }

    private void sendUdpPush(String message) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), 12345);
            udpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}