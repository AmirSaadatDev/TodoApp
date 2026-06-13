package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private long id;
    private String name;
    private long ownerId;
    private List<Long> memberIds = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private Timestamp createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getOwnerId() { return ownerId; }
    public void setOwnerId(long ownerId) { this.ownerId = ownerId; }
    public List<Long> getMemberIds() { return memberIds; }
    public List<Task> getTasks() { return tasks; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}