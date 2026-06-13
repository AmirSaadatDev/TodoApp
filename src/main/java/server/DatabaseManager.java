//package server;
//
//import model.*;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class DatabaseManager {
//    private Map<Long, User> users = new HashMap<>();
//    public Map<Long, Board> boards = new HashMap<>();
//    private long nextUserId = 1;
//    private long nextBoardId = 1;
//    private long nextTaskId = 1;
//
//
//    private String hashPassword(String password) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hash) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) hexString.append('0');
//                hexString.append(hex);
//            }
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("Hashing algorithm not found", e);
//        }
//    }
//
//    public User register(String username, String password) {
//        if (users.containsValue(username)) return null;
//        User user = new User();
//        user.setId(nextUserId++);
//        user.setUsername(username);
//        user.setPasswordHash(hashPassword(password));
//        users.put(user.getId(), user);
//        return user;
//    }
//
//    public User login(String username, String password) {
//        for (User user : users.values()) {
//            if (user.getUsername().equals(username) && user.getPasswordHash().equals(hashPassword(password))) {
//                return user;
//            }
//        }
//        return null;
//    }
//
//    public Board createBoard(String boardName, long ownerId) {
//        Board board = new Board();
//        board.setId(nextBoardId++);
//        board.setName(boardName);
//        board.setOwnerId(ownerId);
//        boards.put(board.getId(), board);
//        return board;
//    }
//
//    public List<Board> listBoards(long userId) {
//        List<Board> userBoards = new ArrayList<>();
//        for (Board board : boards.values()) {
//            if (board.getOwnerId() == userId || board.getMemberIds().contains(userId)) {
//                userBoards.add(board);
//            }
//        }
//        return userBoards;
//    }
//
//    public boolean addUserToBoard(long boardId, long userId) {
//        Board board = boards.get(boardId);
//        if (board != null && !board.getMemberIds().contains(userId)) {
//            board.getMemberIds().add(userId);
//            return true;
//        }
//        return false;
//    }
//
//    public Board viewBoard(long boardId) {
//        return boards.get(boardId);
//    }
//
//    public Task addTask(long boardId, String title, String description, Priority priority) {
//        Board board = boards.get(boardId);
//        if (board != null) {
//            Task task = new Task();
//            task.setId(nextTaskId++);
//            task.setBoardId(boardId);
//            task.setTitle(title);
//            task.setDescription(description);
//            task.setPriority(priority);
//            task.setStatus(Status.TODO);
//            board.getTasks().add(task);
//            return task;
//        }
//        return null;
//    }
//
//    public List<Task> listTasks(long boardId) {
//        Board board = boards.get(boardId);
//        return board != null ? board.getTasks() : new ArrayList<>();
//    }
//
//    public boolean updateTaskStatus(long taskId, Status newStatus) {
//        for (Board board : boards.values()) {
//            for (Task task : board.getTasks()) {
//                if (task.getId() == taskId) {
//                    task.setStatus(newStatus);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    public boolean deleteTask(long taskId) {
//        for (Board board : boards.values()) {
//            boolean removed = board.getTasks().removeIf(task -> task.getId() == taskId);
//            if (removed) return true;
//        }
//        return false;
//    }
//}
package server;

import model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private Storage storage;
    private long nextUserId = 1;
    private long nextBoardId = 1;
    private long nextTaskId = 1;

    public DatabaseManager() {
        storage = new Storage();
        updateNextIds();
    }

    private void updateNextIds() {
        for (Map.Entry<String, User> entry : storage.users.entrySet()) {
            try {
                long id = Long.parseLong(entry.getKey());
                nextUserId = Math.max(nextUserId, id + 1);
            } catch (NumberFormatException e) {}
        }
        for (Map.Entry<String, Board> entry : storage.boards.entrySet()) {
            try {
                long id = Long.parseLong(entry.getKey());
                nextBoardId = Math.max(nextBoardId, id + 1);
            } catch (NumberFormatException e) {}

            for (Task task : entry.getValue().getTasks()) {
                nextTaskId = Math.max(nextTaskId, task.getId() + 1);
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    public User register(String username, String password) {
        for (User user : storage.users.values()) {
            if (user.getUsername().equals(username)) {
                return null;
            }
        }
        User user = new User();
        user.setId(nextUserId++);
        user.setUsername(username);
        user.setPasswordHash(hashPassword(password));
        storage.users.put(String.valueOf(user.getId()), user);
        storage.save();
        return user;
    }


    public User login(String username, String password) {
        for (User user : storage.users.values()) {
            if (user.getUsername().equals(username) && user.getPasswordHash().equals(hashPassword(password))) {
                return user;
            }
        }
        return null;
    }

    public Board createBoard(String boardName, long ownerId) {
        Board board = new Board();
        board.setId(nextBoardId++);
        board.setName(boardName);
        board.setOwnerId(ownerId);
        storage.boards.put(String.valueOf(board.getId()), board);
        storage.save();
        return board;
    }

    public List<Board> listBoards(long userId) {
        List<Board> userBoards = new ArrayList<>();
        for (Board board : storage.boards.values()) {
            if (board.getOwnerId() == userId || board.getMemberIds().contains(userId)) {
                userBoards.add(board);
            }
        }
        return userBoards;
    }

    public boolean addUserToBoard(long boardId, long userId) {
        Board board = storage.boards.get(String.valueOf(boardId));
        if (board != null && !board.getMemberIds().contains(userId)) {
            board.getMemberIds().add(userId);
            storage.save();
            return true;
        }
        return false;
    }

    public Board viewBoard(long boardId) {
        return storage.boards.get(String.valueOf(boardId));
    }

    public Task addTask(long boardId, String title, String description, Priority priority) {
        Board board = storage.boards.get(String.valueOf(boardId));
        if (board != null) {
            Task task = new Task();
            task.setId(nextTaskId++);
            task.setBoardId(boardId);
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(priority);
            task.setStatus(Status.TODO);
            board.getTasks().add(task);
            storage.save();
            return task;
        }
        return null;
    }

    public List<Task> listTasks(long boardId) {
        Board board = storage.boards.get(String.valueOf(boardId));
        return board != null ? board.getTasks() : new ArrayList<>();
    }

    public boolean updateTaskStatus(long taskId, Status newStatus) {
        for (Board board : storage.boards.values()) {
            for (Task task : board.getTasks()) {
                if (task.getId() == taskId) {
                    task.setStatus(newStatus);
                    storage.save();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteTask(long taskId) {
        for (Board board : storage.boards.values()) {
            boolean removed = board.getTasks().removeIf(task -> task.getId() == taskId);
            if (removed) {
                storage.save();
                return true;
            }
        }
        return false;
    }
}