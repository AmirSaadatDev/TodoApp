package client;

import common.exception.ProtocolException;
import common.protocol.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineParserTest {

    @Test
    void parsesSimpleCommand() {
        Request request = CommandLineParser.parse("login alice secret");
        assertEquals("login", request.getCommand());
        assertEquals("alice", request.param("username"));
        assertEquals("secret", request.param("password"));
    }

    @Test
    void respectsQuotedArgumentsWithSpaces() {
        Request request = CommandLineParser.parse("add_task 1 \"Fix login bug\" \"users can't log in\" HIGH");
        assertEquals("1", request.param("boardId"));
        assertEquals("Fix login bug", request.param("title"));
        assertEquals("users can't log in", request.param("description"));
        assertEquals("HIGH", request.param("priority"));
    }

    @Test
    void rejectsUnknownCommand() {
        assertThrows(ProtocolException.class, () -> CommandLineParser.parse("delete_everything 1"));
    }

    @Test
    void rejectsWrongArgumentCount() {
        assertThrows(ProtocolException.class, () -> CommandLineParser.parse("login alice"));
    }
}
