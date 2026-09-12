package server;

import server.command.AddTaskCommand;
import server.command.AddUserToBoardCommand;
import server.command.CommandRegistry;
import server.command.CreateBoardCommand;
import server.command.DeleteTaskCommand;
import server.command.ListBoardsCommand;
import server.command.ListTasksCommand;
import server.command.LoginCommand;
import server.command.LogoutCommand;
import server.command.RegisterCommand;
import server.command.RegisterUdpCommand;
import server.command.UpdateTaskStatusCommand;
import server.command.ViewBoardCommand;
import server.repository.BoardRepository;
import server.repository.UserRepository;
import server.service.AuthService;
import server.service.BoardAccessService;
import server.service.NotificationService;

/** Composition root: wires repositories/services into commands and registers them by name. */
public final class CommandRegistryFactory {
    private CommandRegistryFactory() {}

    public static CommandRegistry create(UserRepository userRepository, BoardRepository boardRepository,
                                          NotificationService notificationService) {
        AuthService authService = new AuthService(userRepository);
        BoardAccessService accessService = new BoardAccessService();

        CommandRegistry registry = new CommandRegistry();
        registry.register("register", new RegisterCommand(authService));
        registry.register("login", new LoginCommand(authService));
        registry.register("logout", new LogoutCommand(notificationService));
        registry.register("register_udp", new RegisterUdpCommand(notificationService));
        registry.register("create_board", new CreateBoardCommand(boardRepository));
        registry.register("list_boards", new ListBoardsCommand(boardRepository));
        registry.register("add_user_to_board", new AddUserToBoardCommand(boardRepository, accessService));
        registry.register("view_board", new ViewBoardCommand(boardRepository, accessService));
        registry.register("add_task", new AddTaskCommand(boardRepository, accessService, notificationService));
        registry.register("list_tasks", new ListTasksCommand(boardRepository, accessService));
        registry.register("update_task_status",
                new UpdateTaskStatusCommand(boardRepository, accessService, notificationService));
        registry.register("delete_task", new DeleteTaskCommand(boardRepository, accessService));
        return registry;
    }
}
