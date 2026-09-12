package server.command;

import common.protocol.Request;
import common.protocol.Response;
import server.ClientSession;
import server.repository.BoardRepository;

public class CreateBoardCommand extends AbstractCommand {
    private final BoardRepository boardRepository;

    public CreateBoardCommand(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        String name = request.requireParam("name");
        boardRepository.create(name, userId);
        return Response.success("Board created successfully");
    }
}
