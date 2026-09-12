package model;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Board {
    private long id;
    private String name;
    private long ownerId;
    private List<Long> memberIds = new CopyOnWriteArrayList<>();
    private List<Task> tasks = new CopyOnWriteArrayList<>();
    private Instant createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getOwnerId() { return ownerId; }
    public void setOwnerId(long ownerId) { this.ownerId = ownerId; }

    public List<Long> getMemberIds() { return memberIds; }
    // Exposed so Storage can rewrap the list after Gson deserialization
    // (Gson bypasses field initializers and gives back a plain ArrayList).
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
