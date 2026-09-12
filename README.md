# 📝 TodoApp — Multi-User Networked Task Manager

A high-performance **Client-Server Todo application** built in Java, featuring thread-safe architecture, dynamic UDP push notifications, BCrypt authentication, and crash-resilient storage. Refactored around **Clean Architecture**, **Command Pattern**, and **Network Reliability**.
---

## 🚀 Features

- 🔐 **BCrypt Security & Side-Channel Defense:** Uses salted BCrypt hashing with a dummy-hash mechanism to prevent timing side-channel attacks during authentication.
- ⚡ **Dynamic UDP Push Notifications:** Clients bind to ephemeral ports automatically, allowing multiple clients to run on the same host without port conflicts.
- 🛡️ **Atomic File Persistence:** Data is written atomically via temporary files (`StandardCopyOption.ATOMIC_MOVE`) to prevent JSON corruption during system crashes.
- 🧩 **Clean Architecture & Design Patterns:** Decoupled into `Command`, `Repository`, `Service`, and `Model` layers with a `CommandRegistryFactory` composition root.
- ⚡ **O(1) Memory Indexing:** `BoardRepository` uses secondary indexing (`ConcurrentHashMap`) for O(1) task lookups across boards.
- 🔄 **Thread Safety & Resource Limits:** Utilizes `ConcurrentHashMap`, `CopyOnWriteArrayList`, dynamic socket idle timeouts (5 mins), and a graceful shutdown hook.
- 💬 **Robust JSON Protocol:** Replaced brittle pipe-delimited commands with a structured, line-delimited JSON protocol using custom `Instant` type adapters.
---

## 🏗️ Architecture

```
Client ──── TCP ────► Server
Server ──── UDP ───► Client  (push notifications)
```

- **Persistence Layer:** Dual `ConcurrentHashMap` caches backed by atomic JSON storage.
- **Network Layer:** Socket timeout handling (5 min limit), centralized error responses, and clean exception mapping.
---

## 📁 Project Structure

```
src/main/java/
├── client/                       # CLI client & network listeners
│   ├── Client.java
│   ├── CommandLineParser.java
│   ├── ServerConnection.java
│   └── UdpListener.java
├── common/                       # Shared protocol, exceptions & serializers
│   ├── exception/
│   ├── protocol/                 # Request / Response
│   ├── GsonFactory.java
│   └── InstantTypeAdapter.java
├── model/                        # Domain entities & enums
│   ├── Board.java
│   ├── Task.java
│   ├── User.java
│   ├── Priority.java
│   └── Status.java
└── server/                       # Multi-threaded backend core
    ├── command/                  # Command Pattern implementations
    ├── repository/                # BoardRepository, UserRepository
    ├── service/                   # AuthService, BoardAccessService, NotificationService
    ├── ClientHandler.java
    ├── ClientSession.java
    ├── CommandRegistryFactory.java
    ├── Server.java
    └── Storage.java
```

---

## ⚙️ Requirements

| Tool  | Version |
|-------|---------|
| Java  | 17+     |
| Maven | 3.6+    |

---

## 🔧 Build

```bash
mvn clean package
```

---

## ▶️ Run

**1. Start the server:**
```bash
java -cp target/TodoApp-1.0-SNAPSHOT.jar server.Server
```

**2. Start the client** (in a new terminal):
```bash
java -cp target/TodoApp-1.0-SNAPSHOT.jar client.Client
```

> ⚠️ Always start the **server before the client**.You can start multiple clients — each binds its own ephemeral UDP port, so they won't conflict.

---


## 🧪 Quick Test Scenario

```
register alice secret123
login alice secret123
create_board "Backend Refactoring"
list_boards
add_task 1 "Fix Auth Leak" "Mitigate timing side-channels" HIGH
list_tasks 1
update_task_status 1 INPROGRESS
logout
```

---

## 🛠️ Tech Stack

- **Java 17** — Core language
- **Maven** — Build tool & dependency management
- **Gson** — JSON serialization/deserialization
- **TCP Sockets** — Client-server communication
- **UDP Sockets** — Push notifications

---

📌 Known Limitations
Documented trade-offs, kept deliberately out of scope for the current version:
- `Storage.save()` rewrites the entire JSON file on every change — fine at this scale, would need incremental/batched writes at production scale.
- No rate limiting on UDP push notifications — a very large board could, in principle, be used to flood its members with packets.
- Board membership management (`add_user_to_board`) is currently allowed for any board member, not just the owner.
---


## 📄 License

This project is for educational purposes.
