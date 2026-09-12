package client;

import common.exception.ProtocolException;
import common.protocol.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates simple space-separated CLI input (quotes supported for values
 * containing spaces) into a structured Request.
 * Example: add_task 1 "Fix login bug" "Users can't log in" HIGH
 */
public final class CommandLineParser {
    private static final Map<String, List<String>> COMMAND_PARAMS = Map.ofEntries(
            Map.entry("register", List.of("username", "password")),
            Map.entry("login", List.of("username", "password")),
            Map.entry("logout", List.of()),
            Map.entry("create_board", List.of("name")),
            Map.entry("list_boards", List.of()),
            Map.entry("add_user_to_board", List.of("boardId", "userId")),
            Map.entry("view_board", List.of("boardId")),
            Map.entry("add_task", List.of("boardId", "title", "description", "priority")),
            Map.entry("list_tasks", List.of("boardId")),
            Map.entry("update_task_status", List.of("taskId", "status")),
            Map.entry("delete_task", List.of("taskId"))
    );

    private CommandLineParser() {}

    public static Request parse(String line) {
        List<String> tokens = tokenize(line);
        if (tokens.isEmpty()) {
            throw new ProtocolException("Empty command");
        }
        String command = tokens.get(0);
        List<String> paramNames = COMMAND_PARAMS.get(command);
        if (paramNames == null) {
            throw new ProtocolException("Unknown command: " + command);
        }
        List<String> args = tokens.subList(1, tokens.size());
        if (args.size() != paramNames.size()) {
            throw new ProtocolException(command + " expects " + paramNames.size() + " argument(s): " + paramNames);
        }
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < paramNames.size(); i++) {
            params.put(paramNames.get(i), args.get(i));
        }
        return new Request(command, params);
    }

    private static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
