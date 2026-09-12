package server.repository;

import model.Board;
import model.Priority;
import model.Status;
import model.Task;
import server.Storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BoardRepository {
    private final Storage storage;
    private final AtomicLong nextBoardId;
    private final AtomicLong nextTaskId;

    // taskId -> boardId. The original findBoardByTask() scanned every board and every
    // task on every call; this index makes that lookup O(1) and is kept in sync on
    // every add/delete.
    private final Map<Long, Long> taskIndex = new ConcurrentHashMap<>();

    public BoardRepository(Storage storage) {
        this.storage = storage;
        long maxBoardId = 0;
        long maxTaskId = 0;
        for (Board board : storage.boards.values()) {
            maxBoardId = Math.max(maxBoardId, board.getId());
            for (Task task : board.getTasks()) {
                maxTaskId = Math.max(maxTaskId, task.getId());
                taskIndex.put(task.getId(), board.getId());
            }
        }
        this.nextBoardId = new AtomicLong(maxBoardId + 1);
        this.nextTaskId = new AtomicLong(maxTaskId + 1);
    }

    public Board create(String name, long ownerId) {
        Board board = new Board();
        board.setId(nextBoardId.getAndIncrement());
        board.setName(name);
        board.setOwnerId(ownerId);
        board.setCreatedAt(Instant.now());
        storage.boards.put(String.valueOf(board.getId()), board);
        storage.save();
        return board;
    }

    public Optional<Board> findById(long boardId) {
        return Optional.ofNullable(storage.boards.get(String.valueOf(boardId)));
    }

    public List<Board> findAccessibleTo(long userId) {
        List<Board> result = new ArrayList<>();
        for (Board board : storage.boards.values()) {
            if (board.getOwnerId() == userId || board.getMemberIds().contains(userId)) {
                result.add(board);
            }
        }
        return result;
    }

    public boolean addMember(Board board, long userId) {
        if (board.getMemberIds().contains(userId)) {
            return false;
        }
        board.getMemberIds().add(userId);
        storage.save();
        return true;
    }

    public Optional<Board> findBoardByTaskId(long taskId) {
        Long boardId = taskIndex.get(taskId);
        return boardId != null ? findById(boardId) : Optional.empty();
    }

    public Task addTask(Board board, String title, String description, Priority priority) {
        Task task = new Task();
        task.setId(nextTaskId.getAndIncrement());
        task.setBoardId(board.getId());
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(Status.TODO);
        task.setCreatedAt(Instant.now());
        board.getTasks().add(task);
        taskIndex.put(task.getId(), board.getId());
        storage.save();
        return task;
    }

    /**
     * Takes the board the caller already resolved via findBoardByTaskId() for its access
     * check, instead of looking it up again internally - the two callers (commands) used
     * to trigger that lookup twice per request.
     */
    public boolean updateTaskStatus(Board board, long taskId, Status newStatus) {
        for (Task task : board.getTasks()) {
            if (task.getId() == taskId) {
                task.setStatus(newStatus);
                storage.save();
                return true;
            }
        }
        return false;
    }

    public boolean deleteTask(Board board, long taskId) {
        boolean removed = board.getTasks().removeIf(t -> t.getId() == taskId);
        if (removed) {
            taskIndex.remove(taskId);
            storage.save();
        }
        return removed;
    }
}
