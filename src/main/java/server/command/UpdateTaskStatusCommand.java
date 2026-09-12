package server.command;

import common.exception.ProtocolException;
import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import model.Status;
import server.ClientSession;
import server.repository.BoardRepository;
import server.service.BoardAccessService;
import server.service.NotificationService;

import java.util.Optional;

public class UpdateTaskStatusCommand extends AbstractCommand {
    private final BoardRepository boardRepository;
    private final BoardAccessService accessService;
    private final NotificationService notificationService;

    public UpdateTaskStatusCommand(BoardRepository boardRepository, BoardAccessService accessService,
                                    NotificationService notificationService) {
        this.boardRepository = boardRepository;
        this.accessService = accessService;
        this.notificationService = notificationService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        // NOTE: the original command never checked userId == -1 up front; it happened
        // to "work" only because findBoardByTask(taskId) searched listBoards(userId),
        // which returns nothing for an anonymous session. Made explicit here.
        long userId = requireLogin(session);
        long taskId = request.requireLongParam("taskId");
        Status newStatus = parseStatus(request.requireParam("status"));

        Optional<Board> boardOpt = boardRepository.findBoardByTaskId(taskId);
        if (boardOpt.isEmpty()) {
            return Response.error("Board not found");
        }

        Board board = boardOpt.get();
        if (!accessService.hasAccess(board, userId)) {
            return Response.error("access is needed");
        }

        boolean updated = boardRepository.updateTaskStatus(board, taskId, newStatus);
        if (!updated) {
            return Response.error("Task not found");
        }
        notificationService.notifyBoardMembers(board, "Task " + taskId + " status changed to " + newStatus);
        return Response.success("Task status updated successfully");
    }

    private Status parseStatus(String value) {
        try {
            return Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProtocolException("Invalid status: " + value);
        }
    }
}
