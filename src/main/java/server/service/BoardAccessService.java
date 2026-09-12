package server.service;

import model.Board;

/**
 * Centralizes the access-control rule that used to be duplicated (and, in one
 * command, missing) across ClientHandler's switch statement.
 */
public class BoardAccessService {
    public boolean hasAccess(Board board, long userId) {
        return board != null && userId != -1
                && (board.getOwnerId() == userId || board.getMemberIds().contains(userId));
    }
}
