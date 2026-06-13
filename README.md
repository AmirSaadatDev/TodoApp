# TodoApp

A multi-user Todo/Task Manager built with Java using a Client-Server architecture over TCP/UDP.

## Features

- User registration & login with secure BCrypt password hashing
- Create and manage boards (workspaces)
- Add tasks with title, description, priority, and status
- Invite other users to boards
- Real-time push notifications via UDP when tasks are added or updated
- Persistent JSON-based storage (no database required)

## Project Structure

src/main/java/
├── client/
│ ├── Client.java # Main client entry point
│ └── UdpListener.java # Listens for UDP push notifications
├── model/
│ ├── User.java
│ ├── Board.java
│ ├── Task.java
│ ├── Priority.java # Enum: LOW, MEDIUM, HIGH
│ └── Status.java # Enum: TODO, INPROGRESS, DONE
└── server/
├── Server.java # Main server entry point
├── ClientHandler.java # Handles each client in a separate thread
├── DatabaseManager.java # Business logic layer
└── Storage.java # JSON persistence layer


## Architecture

Client ──TCP (port 8080)──► Server
Server ──UDP (port 12345)──► Client (push notifications)


- Each client connection is handled in a separate thread (thread pool of 10)
- Data is persisted in `storage.json` using Gson

## Requirements

- Java 17+
- Maven 3.6+

