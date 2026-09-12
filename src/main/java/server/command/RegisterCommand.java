package server.command;

import common.protocol.Request;
import common.protocol.Response;
import model.User;
import server.ClientSession;
import server.service.AuthService;

public class RegisterCommand extends AbstractCommand {
    private final AuthService authService;

    public RegisterCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        String username = request.requireParam("username");
        String password = request.requireParam("password");
        User user = authService.register(username, password);
        return user != null
                ? Response.success("User registered")
                : Response.error("Username already exists");
    }
}
