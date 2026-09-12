package server.command;

import common.protocol.Request;
import common.protocol.Response;
import server.ClientSession;

public interface Command {
    Response execute(Request request, ClientSession session);
}
