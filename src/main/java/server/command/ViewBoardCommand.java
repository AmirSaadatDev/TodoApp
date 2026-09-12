package server.command;

import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import server.ClientSession;
import server.repository.BoardRepository;
import server.service.BoardAccessService;

import java.util.Optional;

public class ViewBoardCommand extends AbstractCommand {
    private final BoardRepository boardRepository;
    private final BoardAccessService accessService;

    public ViewBoardCommand(BoardRepository boardRepository, BoardAccessService accessService) {
        this.boardRepository = boardRepository;
        this.accessService = accessService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        long boardId = request.requireLongParam("boardId");

        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty() || !accessService.hasAccess(boardOpt.get(), userId)) {
            return Response.error("access is needed");
        }

        return Response.success("Board: " + boardOpt.get().getName());
    }
}
