package common.protocol;

import common.exception.ProtocolException;

import java.util.HashMap;
import java.util.Map;

/**
 * Replaces the old "command|arg1|arg2" pipe-delimited protocol.
 * A single JSON object per line: no delimiter collisions, no broken
 * multi-line descriptions (Gson escapes newlines inside strings).
 */
public class Request {
    private String command;
    private Map<String, String> params = new HashMap<>();

    public Request() {
        // required by Gson
    }

    public Request(String command, Map<String, String> params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public String param(String key) {
        // Gson runs this class's no-arg constructor on deserialization (so a missing
        // "params" key in the JSON leaves the field initializer's empty map in place),
        // but a client that sends an explicit "params": null overwrites it with null.
        // Guarding here turns that into a normal "missing parameter" error instead of
        // an NPE that would otherwise surface as an opaque "Internal server error".
        return params == null ? null : params.get(key);
    }

    public String requireParam(String key) {
        String value = param(key);
        if (value == null || value.isBlank()) {
            throw new ProtocolException("Missing required parameter: " + key);
        }
        return value;
    }

    public long requireLongParam(String key) {
        try {
            return Long.parseLong(requireParam(key));
        } catch (NumberFormatException e) {
            throw new ProtocolException("Parameter '" + key + "' must be a number");
        }
    }
}
