package server.service;

import model.Board;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardAccessServiceTest {
    private final BoardAccessService accessService = new BoardAccessService();

    @Test
    void ownerHasAccess() {
        Board board = new Board();
        board.setOwnerId(1L);
        assertTrue(accessService.hasAccess(board, 1L));
    }

    @Test
    void memberHasAccess() {
        Board board = new Board();
        board.setOwnerId(1L);
        board.getMemberIds().add(2L);
        assertTrue(accessService.hasAccess(board, 2L));
    }

    @Test
    void strangerHasNoAccess() {
        Board board = new Board();
        board.setOwnerId(1L);
        assertFalse(accessService.hasAccess(board, 99L));
    }

    @Test
    void loggedOutUserHasNoAccess() {
        Board board = new Board();
        board.setOwnerId(1L);
        assertFalse(accessService.hasAccess(board, -1L));
    }

    @Test
    void nullBoardHasNoAccess() {
        assertFalse(accessService.hasAccess(null, 1L));
    }
}
