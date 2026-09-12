package server.command;

import common.exception.AuthRequiredException;
import server.ClientSession;

public abstract class AbstractCommand implements Command {
    protected long requireLogin(ClientSession session) {
        if (!session.isAuthenticated()) {
            throw new AuthRequiredException("You must be logged in");
        }
        return session.getUserId();
    }
}
