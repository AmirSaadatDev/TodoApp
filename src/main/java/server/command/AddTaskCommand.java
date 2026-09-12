package server.command;

import common.exception.ProtocolException;
import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import model.Priority;
import model.Task;
import server.ClientSession;
import server.repository.BoardRepository;
import server.service.BoardAccessService;
import server.service.NotificationService;

import java.util.Optional;

public class AddTaskCommand extends AbstractCommand {
    private final BoardRepository boardRepository;
    private final BoardAccessService accessService;
    private final NotificationService notificationService;

    public AddTaskCommand(BoardRepository boardRepository, BoardAccessService accessService,
                           NotificationService notificationService) {
        this.boardRepository = boardRepository;
        this.accessService = accessService;
        this.notificationService = notificationService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        long boardId = request.requireLongParam("boardId");
        String title = request.requireParam("title");
        String description = request.param("description") != null ? request.param("description") : "";
        Priority priority = parsePriority(request.requireParam("priority"));

        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            return Response.error("Board not found");
        }

        Board board = boardOpt.get();
        if (!accessService.hasAccess(board, userId)) {
            return Response.error("access is needed");
        }

        Task task = boardRepository.addTask(board, title, description, priority);
        notificationService.notifyBoardMembers(board, "Task added: " + task.getTitle());
        return Response.success("Task added successfully");
    }

    private Priority parsePriority(String value) {
        try {
            return Priority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProtocolException("Invalid priority: " + value);
        }
    }
}
