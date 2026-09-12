package server.command;

import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import server.ClientSession;
import server.repository.BoardRepository;
import server.service.BoardAccessService;

import java.util.Optional;

public class DeleteTaskCommand extends AbstractCommand {
    private final BoardRepository boardRepository;
    private final BoardAccessService accessService;

    public DeleteTaskCommand(BoardRepository boardRepository, BoardAccessService accessService) {
        this.boardRepository = boardRepository;
        this.accessService = accessService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        long taskId = request.requireLongParam("taskId");

        Optional<Board> boardOpt = boardRepository.findBoardByTaskId(taskId);
        if (boardOpt.isEmpty()) {
            return Response.error("Task not found");
        }

        Board board = boardOpt.get();
        if (!accessService.hasAccess(board, userId)) {
            return Response.error("access is needed");
        }

        boolean deleted = boardRepository.deleteTask(board, taskId);
        return deleted ? Response.success("Task deleted successfully") : Response.error("Task not found");
    }
}
