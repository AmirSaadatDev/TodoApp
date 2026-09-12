package common.exception;

/** Thrown by commands that require an authenticated session. */
public class AuthRequiredException extends RuntimeException {
    public AuthRequiredException(String message) {
        super(message);
    }
}
