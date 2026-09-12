package server;

import common.protocol.Request;
import common.protocol.Response;
import org.junit.jupiter.api.Test;
import server.command.CommandRegistry;
import server.repository.BoardRepository;
import server.repository.UserRepository;
import server.service.NotificationService;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the full command pipeline end-to-end (register -> login -> create_board ->
 * add_task -> list_tasks -> update_task_status -> delete_task) directly against the
 * CommandRegistry, without opening real sockets. Regression test for issue #13
 * (no test previously covered this whole path).
 */
class CommandFlowIntegrationTest {

    @Test
    void fullTaskLifecycle() throws Exception {
        Path tempFile = Files.createTempFile("taskboard-flow-test", ".json");
        Files.delete(tempFile);
        tempFile.toFile().deleteOnExit();

        Storage storage = new Storage(tempFile);
        UserRepository userRepository = new UserRepository(storage);
        BoardRepository boardRepository = new BoardRepository(storage);
        NotificationService notificationService = new NotificationService();
        CommandRegistry registry = CommandRegistryFactory.create(userRepository, boardRepository, notificationService);

        ClientSession session = new ClientSession(InetAddress.getLoopbackAddress());

        assertSuccess(registry, session, "register", Map.of("username", "alice", "password", "secret123"));
        assertSuccess(registry, session, "login", Map.of("username", "alice", "password", "secret123"));
        assertSuccess(registry, session, "create_board", Map.of("name", "My Board"));

        Response listBoards = execute(registry, session, "list_boards", Map.of());
        assertTrue(listBoards.isSuccess());
        long boardId = listBoards.getData().getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();

        assertSuccess(registry, session, "add_task", Map.of(
                "boardId", String.valueOf(boardId),
                "title", "Fix bug",
                "description", "users can't log in",
                "priority", "HIGH"));

        Response listTasks = execute(registry, session, "list_tasks", Map.of("boardId", String.valueOf(boardId)));
        assertTrue(listTasks.isSuccess());
        assertEquals(1, listTasks.getData().getAsJsonArray().size());
        long taskId = listTasks.getData().getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();

        assertSuccess(registry, session, "update_task_status",
                Map.of("taskId", String.valueOf(taskId), "status", "DONE"));

        assertSuccess(registry, session, "delete_task", Map.of("taskId", String.valueOf(taskId)));

        Response afterDelete = execute(registry, session, "list_tasks", Map.of("boardId", String.valueOf(boardId)));
        assertTrue(afterDelete.isSuccess());
        assertEquals(0, afterDelete.getData().getAsJsonArray().size());
    }

    @Test
    void strangerCannotAccessSomeoneElsesBoard() throws Exception {
        Path tempFile = Files.createTempFile("taskboard-flow-access-test", ".json");
        Files.delete(tempFile);
        tempFile.toFile().deleteOnExit();

        Storage storage = new Storage(tempFile);
        UserRepository userRepository = new UserRepository(storage);
        BoardRepository boardRepository = new BoardRepository(storage);
        NotificationService notificationService = new NotificationService();
        CommandRegistry registry = CommandRegistryFactory.create(userRepository, boardRepository, notificationService);

        ClientSession owner = new ClientSession(InetAddress.getLoopbackAddress());
        assertSuccess(registry, owner, "register", Map.of("username", "owner", "password", "secret123"));
        assertSuccess(registry, owner, "login", Map.of("username", "owner", "password", "secret123"));
        assertSuccess(registry, owner, "create_board", Map.of("name", "Private Board"));
        long boardId = execute(registry, owner, "list_boards", Map.of())
                .getData().getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();

        ClientSession stranger = new ClientSession(InetAddress.getLoopbackAddress());
        assertSuccess(registry, stranger, "register", Map.of("username", "stranger", "password", "secret123"));
        assertSuccess(registry, stranger, "login", Map.of("username", "stranger", "password", "secret123"));

        Response result = execute(registry, stranger, "list_tasks", Map.of("boardId", String.valueOf(boardId)));
        assertTrue(!result.isSuccess() && "access is needed".equals(result.getMessage()));
    }

    private Response execute(CommandRegistry registry, ClientSession session, String command, Map<String, String> params) {
        return registry.find(command).execute(new Request(command, params), session);
    }

    private void assertSuccess(CommandRegistry registry, ClientSession session, String command, Map<String, String> params) {
        Response response = execute(registry, session, command, params);
        assertTrue(response.isSuccess(), command + " failed: " + response.getMessage());
    }
}
