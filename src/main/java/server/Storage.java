package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private final Path file = Paths.get("storage.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // in-memory storage
    public final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Board> boards = new ConcurrentHashMap<>();

    public Storage() {
        load();
    }

//    public synchronized void load() {
//        if (!Files.exists(file)) {
//            save(); // create empty
//            return;
//        }
//        try (Reader r = Files.newBufferedReader(file)) {
//            JsonObject obj = gson.fromJson(r, JsonObject.class);
//            if (obj == null) return;
//            Type userMapType = new TypeToken<Map<String, User>>(){}.getType();
//            Type boardMapType = new TypeToken<Map<String, Board>>(){}.getType();
//            Map<String, User> u = gson.fromJson(obj.get("users"), userMapType);
//            Map<String, Board> b = gson.fromJson(obj.get("boards"), boardMapType);
//            if (u != null) users.putAll(u);
//            if (b != null) boards.putAll(b);
//            // Load tasks into boards
//            for (Board board : boards.values()) {
//                Type taskListType = new TypeToken<List<Task>>(){}.getType();
//                List<Task> tasks = gson.fromJson(obj.getAsJsonObject("tasks").get(board.getId() + ""), taskListType);
//                if (tasks != null) board.getTasks().addAll(tasks);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
public synchronized void load() {
    if (!Files.exists(file)) {
        save();
        return;
    }
    try (Reader r = Files.newBufferedReader(file)) {
        JsonObject obj = gson.fromJson(r, JsonObject.class);
        if (obj == null) return;

        Type userMapType = new TypeToken<Map<String, User>>(){}.getType();
        Type boardMapType = new TypeToken<Map<String, Board>>(){}.getType();

        Map<String, User> u = gson.fromJson(obj.get("users"), userMapType);
        Map<String, Board> b = gson.fromJson(obj.get("boards"), boardMapType);

        if (u != null) users.putAll(u);
        if (b != null) boards.putAll(b);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public synchronized void save() {
        try (Writer w = Files.newBufferedWriter(file)) {
            JsonObject obj = new JsonObject();
            obj.add("users", gson.toJsonTree(users));
            obj.add("boards", gson.toJsonTree(boards));
            gson.toJson(obj, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}