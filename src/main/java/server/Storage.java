package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import common.GsonFactory;
import model.Board;
import model.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Persists users/boards as JSON. Two fixes versus the original version:
 *  1. save() writes to a temp file and atomically renames it, so a crash
 *     mid-write can never leave storage.json truncated/corrupted.
 *  2. load() re-wraps collections in thread-safe implementations, because
 *     Gson constructs objects via reflection and does not run field
 *     initializers - it would otherwise hand back plain ArrayLists.
 */
public class Storage {
    private final Path file;
    private final Gson gson = GsonFactory.forStorage();

    public final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Board> boards = new ConcurrentHashMap<>();

    public Storage() {
        this(Paths.get("storage.json"));
    }

    public Storage(Path file) {
        this.file = file;
        load();
    }

    public synchronized void load() {
        if (!Files.exists(file)) {
            save();
            return;
        }
        try (var reader = Files.newBufferedReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root == null) return;

            Type userMapType = new TypeToken<Map<String, User>>() {}.getType();
            Type boardMapType = new TypeToken<Map<String, Board>>() {}.getType();

            Map<String, User> loadedUsers = gson.fromJson(root.get("users"), userMapType);
            Map<String, Board> loadedBoards = gson.fromJson(root.get("boards"), boardMapType);

            if (loadedUsers != null) {
                users.putAll(loadedUsers);
            }
            if (loadedBoards != null) {
                loadedBoards.values().forEach(board -> {
                    board.setMemberIds(new CopyOnWriteArrayList<>(board.getMemberIds()));
                    board.setTasks(new CopyOnWriteArrayList<>(board.getTasks()));
                });
                boards.putAll(loadedBoards);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load storage from " + file, e);
        }
    }

    public synchronized void save() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Path tmp = Files.createTempFile(parent, "storage", ".tmp");
            try (var writer = Files.newBufferedWriter(tmp)) {
                JsonObject root = new JsonObject();
                root.add("users", gson.toJsonTree(users));
                root.add("boards", gson.toJsonTree(boards));
                gson.toJson(root, writer);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to persist storage to " + file, e);
        }
    }
}
