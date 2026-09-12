package common.exception;

/**
 * Thrown when an incoming request is malformed or missing required data.
 * Caught centrally by ClientHandler and turned into an error Response,
 * instead of the old behavior where a bad request silently killed the
 * client's thread.
 */
public class ProtocolException extends RuntimeException {
    public ProtocolException(String message) {
        super(message);
    }
}
