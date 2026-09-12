package server.service;

import org.junit.jupiter.api.Test;
import server.Storage;
import server.repository.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void registerThenLoginSucceeds() throws Exception {
        AuthService authService = newAuthService();
        assertNotNull(authService.register("alice", "secret123"));
        assertNotNull(authService.login("alice", "secret123"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        AuthService authService = newAuthService();
        authService.register("bob", "correct-password");
        assertNull(authService.login("bob", "wrong-password"));
    }

    @Test
    void loginFailsForUnknownUsername() throws Exception {
        AuthService authService = newAuthService();
        assertNull(authService.login("ghost", "anything"));
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        AuthService authService = newAuthService();
        authService.register("carol", "pw1");
        assertNull(authService.register("carol", "pw2"));
    }

    /**
     * Regression test for the check-then-act race in the original register():
     * findByUsername() and the insert were two separate, unsynchronized steps, so
     * concurrent registrations for the same username could both pass the check.
     * Before the UserRepository.createIfAbsent() fix, this test was flaky and would
     * often report more than one success.
     */
    @Test
    void concurrentRegistrationOfSameUsernameOnlySucceedsOnce() throws Exception {
        AuthService authService = newAuthService();
        int attempts = 8;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            pool.submit(() -> {
                try {
                    startSignal.await();
                    if (authService.register("dave", "password") != null) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        startSignal.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, successCount.get());
    }

    private AuthService newAuthService() throws Exception {
        Path tempFile = Files.createTempFile("taskboard-test-storage", ".json");
        Files.delete(tempFile); // Storage's constructor creates it fresh
        tempFile.toFile().deleteOnExit();
        Storage storage = new Storage(tempFile);
        return new AuthService(new UserRepository(storage));
    }
}
