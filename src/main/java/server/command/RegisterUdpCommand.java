package server.command;

import common.protocol.Request;
import common.protocol.Response;
import server.ClientSession;
import server.service.NotificationService;

public class RegisterUdpCommand extends AbstractCommand {
    private final NotificationService notificationService;

    public RegisterUdpCommand(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        long userId = requireLogin(session);
        int port = (int) request.requireLongParam("port");
        notificationService.registerEndpoint(userId, session.getClientAddress(), port);
        return Response.success("UDP endpoint registered");
    }
}
