package server.command;

import common.protocol.Request;
import common.protocol.Response;
import server.ClientSession;
import server.service.NotificationService;

public class LogoutCommand extends AbstractCommand {
    private final NotificationService notificationService;

    public LogoutCommand(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Response execute(Request request, ClientSession session) {
        // Preserves the original behavior of allowing logout even when not logged in
        // (it's a no-op reset to -1 either way); only deregister when there was a
        // real session, since an anonymous caller was never registered.
        if (session.isAuthenticated()) {
            notificationService.removeEndpoint(session.getUserId());
        }
        session.logout();
        return Response.success("Logged out");
    }
}
