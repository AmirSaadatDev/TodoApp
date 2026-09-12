package server.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.protocol.Request;
import common.protocol.Response;
import model.Board;
import model.Task;
import server.ClientSession;
import server.repository.BoardRepository;
import server.service.BoardAccessService;

import java.util.Optional;

public class ListTasksCommand extends AbstractCommand {
    private final BoardRepository boardRepository;
    private final BoardAccessService accessService;

    public ListTasksCommand(BoardRepository boardRepository, BoardAccessService accessService) {
        this.boardRepository = boardRepository;
        this.accessService = accessService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        // NOTE: the original list_tasks never checked board access at all - any logged-in
        // user could list tasks on any board by guessing its id. Fixed here to match
        // view_board / add_task.
        long userId = requireLogin(session);
        long boardId = request.requireLongParam("boardId");

        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            return Response.error("Board not found");
        }

        Board board = boardOpt.get();
        if (!accessService.hasAccess(board, userId)) {
            return Response.error("access is needed");
        }

        JsonArray tasks = new JsonArray();
        for (Task task : board.getTasks()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", task.getId());
            json.addProperty("title", task.getTitle());
            json.addProperty("status", task.getStatus().name());
            json.addProperty("priority", task.getPriority().name());
            tasks.add(json);
        }
        return Response.success("Tasks retrieved", tasks);
    }
}
