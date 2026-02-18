<div align="center">

# TabMates Server

**The backend service for TabMates – a collaborative expense splitting application**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

🚧 **Work in Progress** – This project is actively under development and will be released at a later date.

</div>

---

## 📖 About

TabMates Server is the backend API powering the TabMates application. TabMates helps friends, roommates, and travel companions effortlessly split shared expenses. Whether you're on vacation, living in a shared flat, or organizing any group activity with shared costs, TabMates keeps track of who paid what and calculates fair settlements.

## ✨ Features

- **👥 User Management** – Register with email or anonymously, secure authentication with JWT tokens
- **📊 Expense Groups** – Create and manage groups with multiple participants
- **💸 Flexible Expense Splitting** – Split costs equally, by exact amounts, percentages, or custom shares
- **⚡ Real-time Updates** – WebSocket support for instant synchronization across devices
- **🔔 Notifications** – Push notifications to notify you about new updates in a group
- **🔐 Security** – JWT-based authentication, rate limiting, and secure password handling

## 🏗️ Architecture

The project follows a **modular architecture** with clear separation of concerns:

```
TabMatesServer/
├── app/               # Main Spring Boot application entry point
├── common/            # Shared utilities, DTOs, and domain types
├── user/              # User authentication and account management
├── tabgroup/          # Group and expense management logic
├── notification/      # Email and push notification services
└── build-logic/       # Gradle convention plugins
```

Each module follows a layered architecture:
- **API** – REST controllers, DTOs, and WebSocket handlers
- **Service** – Business logic and orchestration
- **Domain** – Core entities and domain events
- **Infrastructure** – Database repositories and external integrations

## 🛠️ Tech Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Kotlin |
| **Framework** | Spring Boot |
| **Database** | PostgreSQL, Redis (caching & sessions) |
| **Messaging** | RabbitMQ |
| **Authentication** | JWT (jjwt) |
| **Notifications** | Firebase Admin SDK, Spring Mail |
| **Real-time** | WebSocket |
| **Build** | Gradle with Kotlin DSL, Version Catalog |
| **Code Quality** | ktlint |

## 🚀 Getting Started

### Prerequisites

- JDK 21+
- PostgreSQL
- Redis
- RabbitMQ
- Firebase project (for push notifications)

### Configuration

The application requires the following environment variables:

```bash
# Database
POSTGRES_URL=jdbc:postgresql://localhost:5432/tabmates
POSTGRES_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# JWT
TABMATES_JWT_SECRET=your_secret_key

# Email (SMTP)
SMPT_API_KEY=your_smtp_api_key
```

### Running Locally

```bash
# Clone the repository
git clone https://github.com/your-username/TabMatesServer.git
cd TabMatesServer

# Build the project
./gradlew build

# Run with dev profile
./gradlew :app:bootRun --args='--spring.profiles.active=dev'
```

### Running Tests

```bash
./gradlew test
```

## 📄 License

This project is licensed under the GNU General Public License v3.0 – see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome once the project reaches a stable release. Stay tuned for contribution guidelines.

---

<div align="center">

**Made with ❤️ for hassle-free expense sharing**

</div>

