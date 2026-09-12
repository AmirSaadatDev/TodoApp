package server.command;

import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import server.ClientSession;
import server.repository.BoardRepository;
import server.service.BoardAccessService;

import java.util.Optional;

public class AddUserToBoardCommand extends AbstractCommand {
    private final BoardRepository boardRepository;
    private final BoardAccessService accessService;

    public AddUserToBoardCommand(BoardRepository boardRepository, BoardAccessService accessService) {
        this.boardRepository = boardRepository;
        this.accessService = accessService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        long boardId = request.requireLongParam("boardId");
        long userIdToAdd = request.requireLongParam("userId");

        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            return Response.error("Board not found");
        }

        Board board = boardOpt.get();
        if (!accessService.hasAccess(board, userId)) {
            return Response.error("access is needed");
        }

        boolean added = boardRepository.addMember(board, userIdToAdd);
        return added
                ? Response.success("User added to board successfully")
                : Response.error("User already added or invalid request");
    }
}
