package server.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import server.ClientSession;
import server.repository.BoardRepository;

public class ListBoardsCommand extends AbstractCommand {
    private final BoardRepository boardRepository;

    public ListBoardsCommand(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        JsonArray boards = new JsonArray();
        for (Board board : boardRepository.findAccessibleTo(userId)) {
            JsonObject json = new JsonObject();
            json.addProperty("id", board.getId());
            json.addProperty("name", board.getName());
            boards.add(json);
        }
        return Response.success("Boards retrieved", boards);
    }
}
