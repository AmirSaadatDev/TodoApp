package server.repository;

import model.User;
import server.Storage;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class UserRepository {
    private final Storage storage;
    private final AtomicLong nextId;

    public UserRepository(Storage storage) {
        this.storage = storage;
        long max = storage.users.keySet().stream()
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0);
        this.nextId = new AtomicLong(max + 1);
    }

    public Optional<User> findByUsername(String username) {
        return storage.users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Checks availability and inserts the user as one atomic step. Previously this was
     * split across AuthService (check) and this class (insert) with nothing serializing
     * the two, so two concurrent registrations for the same username could both pass the
     * check before either was persisted, creating duplicate accounts. Locking the whole
     * check-then-act sequence on this instance closes that race. Registration is not a
     * hot path, so a coarse lock here has no meaningful performance cost.
     */
    public synchronized User createIfAbsent(String username, String passwordHash) {
        if (findByUsername(username).isPresent()) {
            return null;
        }
        User user = new User();
        user.setId(nextId.getAndIncrement());
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(Instant.now());
        storage.users.put(String.valueOf(user.getId()), user);
        storage.save();
        return user;
    }
}
