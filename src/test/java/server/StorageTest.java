package server;

import model.Board;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StorageTest {

    /**
     * Regression test for issue #14 / the Gson-collection subtlety documented in
     * Storage.load(): after reading a Board back from disk, its tasks/memberIds
     * fields must still be CopyOnWriteArrayList, not a plain ArrayList Gson handed
     * back while populating the JSON array.
     */
    @Test
    void reloadedBoardsUseThreadSafeCollections() throws Exception {
        Path tempFile = Files.createTempFile("taskboard-storage-test", ".json");
        Files.delete(tempFile); // let Storage's constructor create it fresh
        tempFile.toFile().deleteOnExit();

        Storage original = new Storage(tempFile);
        Board board = new Board();
        board.setId(1);
        board.setName("Test board");
        board.setOwnerId(42);
        board.getMemberIds().add(7L);
        original.boards.put("1", board);
        original.save();

        // Simulate a fresh process starting up and loading the same file.
        Storage reloaded = new Storage(tempFile);
        Board reloadedBoard = reloaded.boards.get("1");

        assertEquals("Test board", reloadedBoard.getName());
        assertEquals(1, reloadedBoard.getMemberIds().size());
        assertInstanceOf(CopyOnWriteArrayList.class, reloadedBoard.getTasks());
        assertInstanceOf(CopyOnWriteArrayList.class, reloadedBoard.getMemberIds());
    }
}
