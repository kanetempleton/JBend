# JBend

**Java Backend Framework built from scratch**

JBend is a lightweight, from-scratch network application framework written entirely in Java. It provides non-blocking TCP servers with native support for raw TCP, HTTP, and WebSocket protocols, plus integrated MySQL database management and a custom ORM layer.

Live example: [https://kanetempleton.com](https://kanetempleton.com)

---

## Overview

Unlike frameworks that sit on top of Netty, Undertow, or Spring, JBend implements the core networking stack itself using Java NIO (`ServerSocketChannel` + `Selector`). Protocol handlers for HTTP and WebSocket are written on top of the raw TCP layer, giving full control over connection lifecycle, framing, and concurrency.

The framework is designed to run multiple protocol servers in parallel (TCP + HTTP + WebSocket) that share a single asynchronous MySQL connection manager.

### Core Capabilities

| Feature | Description |
|---------|-------------|
| **Non-blocking TCP Server** | Java NIO selector-based server with connection pooling and recycled IDs |
| **HTTP Server** | Full request handling (GET/POST/PUT/PATCH/DELETE), cookie support, static file serving, config-driven routing |
| **WebSocket Server** | Complete protocol implementation (handshake, frame encode/decode per RFC 6455, ping/pong) |
| **MySQL Integration** | Threaded database manager with query queue, connection locking, and startup schema checks |
| **Custom ORM** | Auto-mapping of Java objects to database tables with schema repair capabilities |
| **Security** | IP banning, login handling, basic cryptography, injection-aware query patterns |
| **Configuration** | External config files for database credentials, routes, and application settings |
| **Multi-threaded Launcher** | Coordinated startup of servers, database manager, console, and application logic |

---

## Architecture

```
Launcher
 ├── Console (interactive)
 ├── DatabaseManager (async query queue + JDBC)
 ├── TCP Server
 ├── HTTP Server
 └── WebSocket Server
```

All servers share the same `DatabaseManager` instance. The launcher starts components in stages and manages thread lifecycle.

Key packages:

- `com.server` – Core non-blocking server and connection management
- `com.server.protocol` – Protocol handlers (TCP, HTTP, WebSocket)
- `com.server.web` – HTTP utilities, cookies, page routing
- `com.db` – Database manager, query queue, locking
- `com.db.crud` – Custom ORM / CRUD object system
- `com.server.login` – Authentication and ban handling
- `com.util.crypt` – Cryptography helpers

---

## Getting Started

### Prerequisites

- Java 8+
- MySQL 5.7+ (or compatible)
- Basic familiarity with running Java applications from the command line

### Configuration

Edit `universe/env.cfg`:

```
db_addr:=localhost=;
db_name:=jbend=;
db_user:=root=;
db_pass:=your_password=;
ws_addr:=ws://127.0.0.1:42069/ws=;
```

Edit `universe/routes.cfg` to define HTTP routes:

```
/login -> /pages/login/login.html
/login.js -> /pages/login/login.js
```

### Build & Run

Make sure to first run the initialization script before running the project:
```bash
# From the repository root
./init.sh
```

```bash
# From the repository root
./build.sh          # or use the provided makejar / init scripts
./run.sh
```

The launcher will start the configured servers and database manager. An interactive console is available for runtime commands.

---

## Key Design Decisions

- **From-scratch networking**: No Netty / Jetty / Undertow. Everything is built on Java NIO for maximum control and educational value.
- **Protocol layering**: HTTP and WebSocket are implemented as protocol handlers over the same non-blocking TCP server core.
- **Shared database layer**: All protocol servers talk to one asynchronous `DatabaseManager` with a query queue and locking to avoid concurrent access issues.
- **Custom ORM**: Objects can embed CRUD behavior. The system can inspect class structure and repair/create corresponding database tables.
- **Config-driven**: Routes, database credentials, and application settings live outside the code.

---

## Project Status & Notes

This project was developed iteratively as a learning and production experiment. It powers a live personal site and was used as the backend for multiplayer experiments.

It is **not** intended as a drop-in replacement for Spring Boot or production-grade frameworks. Its value is in demonstrating low-level systems programming, protocol implementation, concurrency, and database integration in pure Java.

Known limitations:
- Some older code paths contain incomplete features or debug comments
- Documentation was historically minimal (this README aims to fix that)
- Not hardened for high-scale production traffic without additional work

---

## License

This project is provided as-is for educational and portfolio purposes.

---

**Author:** Kane Templeton  
GitHub: [kanetempleton/JBend](https://github.com/kanetempleton/JBend)
