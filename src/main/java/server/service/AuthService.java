package server.service;

import model.User;
import org.mindrot.jbcrypt.BCrypt;
import server.repository.UserRepository;

import java.util.Optional;

/**
 * Replaces raw, unsalted SHA-256 hashing with BCrypt, which generates a random
 * salt per password and is deliberately slow (defeats brute-force/rainbow tables).
 */
public class AuthService {
    // Computed once at class-load time and used whenever the username lookup misses,
    // so an unknown-username login always pays the same BCrypt cost as a real one.
    // Without this, checkpw() only runs when the user exists, and its deliberate slowness
    // (tens-hundreds of ms) becomes a timing side-channel an attacker can use to enumerate
    // valid usernames even though the error message itself is identical either way.
    private static final String DUMMY_HASH = BCrypt.hashpw("no-such-user-timing-guard", BCrypt.gensalt());

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return userRepository.createIfAbsent(username, hash);
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        String hashToCheck = userOpt.map(User::getPasswordHash).orElse(DUMMY_HASH);
        boolean passwordMatches = BCrypt.checkpw(password, hashToCheck);
        return (userOpt.isPresent() && passwordMatches) ? userOpt.get() : null;
    }
}
