package server.command;

import common.protocol.Request;
import common.protocol.Response;
import model.User;
import server.ClientSession;
import server.service.AuthService;

public class LoginCommand extends AbstractCommand {
    private final AuthService authService;

    public LoginCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        String username = request.requireParam("username");
        String password = request.requireParam("password");
        User user = authService.login(username, password);
        if (user == null) {
            return Response.error("Invalid username or password");
        }
        session.login(user.getId());
        return Response.success("Logged in successfully");
    }
}
