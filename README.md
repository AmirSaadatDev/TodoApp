# 📝 TodoApp — Multi-User Task Manager

A **Client-Server Todo application** built in Java, featuring real-time push notifications, secure authentication, and persistent storage. Designed with clean architecture and multi-threaded server handling.

---

## 🚀 Features

- ✅ User registration & login with **SHA-256** password hashing
- 📋 Create and manage **boards** (workspaces)
- ➕ Add tasks with **title**, **description**, **priority**, and **status**
- 👥 Invite other users to boards
- 🔔 Real-time **UDP push notifications** on task updates
- 💾 Persistent **JSON-based storage** — no database setup required
- ⚡ Multi-threaded server with thread pool

---

## 🏗️ Architecture

```
Client ──── TCP (port 8080) ────► Server
Server ──── UDP (port 12345) ───► Client  (push notifications)
```

- Each client is handled in a **separate thread** (thread pool of 10)
- Business logic is separated into `DatabaseManager`
- Storage is handled by a dedicated `Storage` class using **Gson**

---

## 📁 Project Structure

```
src/main/java/
├── client/
│   ├── Client.java              # Main client entry point (CLI)
│   └── UdpListener.java         # Listens for UDP push notifications
├── model/
│   ├── User.java
│   ├── Board.java
│   ├── Task.java
│   ├── Priority.java            # Enum: LOW, MEDIUM, HIGH
│   └── Status.java              # Enum: TODO, INPROGRESS, DONE
└── server/
    ├── Server.java              # Main server entry point
    ├── ClientHandler.java       # Per-client TCP handler (threaded)
    ├── DatabaseManager.java     # Business logic layer
    └── Storage.java             # JSON persistence layer
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
java -cp target/TodoApp1-1.0-SNAPSHOT.jar server.Server
```

**2. Start the client** (in a new terminal):
```bash
java -cp target/TodoApp1-1.0-SNAPSHOT.jar client.Client
```

> ⚠️ Always start the **server before the client**.

---

## 💻 Available Commands

| Command | Description |
|---------|-------------|
| `register\|username\|password` | Register a new user |
| `login\|username\|password` | Login to your account |
| `logout` | Logout from current session |
| `create_board\|name` | Create a new board |
| `list_boards` | List all accessible boards |
| `view_board\|boardId` | View board details |
| `add_user_to_board\|boardId\|userId` | Add a user to a board |
| `add_task\|boardId\|title\|description\|priority` | Add a task (`LOW` / `MEDIUM` / `HIGH`) |
| `list_tasks\|boardId` | List all tasks in a board |
| `update_task_status\|taskId\|status` | Update status (`TODO` / `INPROGRESS` / `DONE`) |
| `delete_task\|taskId` | Delete a task |

---

## 🧪 Example Session

```
register|alice|secret123
login|alice|secret123
create_board|MyProject
list_boards
add_task|1|Fix login bug|Critical issue in auth flow|HIGH
list_tasks|1
update_task_status|1|INPROGRESS
delete_task|1
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

## 📄 License

This project is for educational purposes.
