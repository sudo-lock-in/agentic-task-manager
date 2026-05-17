# Task Manager API with AI Suggestions

A Spring Boot REST API for personal task management with AI-powered task suggestions using Ollama Mistral. Built with Java 17, Spring Boot 3.3.0, H2 database, and a responsive web UI.

## Features

- **CRUD Operations**: Create, read, update, delete tasks
- **Task Filtering**: Filter by status, priority, date range, or search by title
- **AI Suggestions**: Generate task suggestions from natural language using Ollama Mistral
- **Web UI**: Responsive single-page application for task management
- **Comprehensive Tests**: 26 unit and integration tests
- **No External Dependencies**: H2 in-memory database, local Ollama (no API keys needed)

## Prerequisites

### Required
- Java 17+
- Gradle 7.0+ (gradlew wrapper included)

### Optional
- Ollama Mistral (for AI suggestions) - [Download Ollama](https://ollama.ai)

## Quick Start

### 1. Install Ollama and Download Mistral Model

```bash
# Download and install Ollama from https://ollama.ai
# Then download the Mistral model:
ollama pull mistral

# Start Ollama (if not running as a service)
ollama serve
```

Ollama will listen on `http://localhost:11434` by default.

### 2. Run the Application

```bash
# From the task-manager-api directory
./gradlew bootRun
```

The API will start on **http://localhost:8080**

### 3. Access the Web UI

Open your browser to: **http://localhost:8080**

## API Endpoints

### Task Management

#### Create Task
```bash
POST /tasks
Content-Type: application/json

{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "dueDate": "2024-01-15",
  "priority": "HIGH",
  "status": "TODO"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "dueDate": "2024-01-15",
  "priority": "HIGH",
  "status": "TODO",
  "createdAt": "2024-01-10"
}
```

#### Get All Tasks
```bash
GET /tasks
```

**Response:** `200 OK` - Array of all tasks

#### Get Task by ID
```bash
GET /tasks/{id}
```

**Response:** `200 OK` or `404 Not Found`

#### Update Task
```bash
PUT /tasks/{id}
Content-Type: application/json

{
  "title": "Buy groceries",
  "status": "DONE",
  "priority": "MEDIUM"
}
```

**Response:** `200 OK` - Updated task or `404 Not Found`

#### Delete Task
```bash
DELETE /tasks/{id}
```

**Response:** `204 No Content` or `404 Not Found`

### AI-Powered Task Suggestions

#### Generate Task from Description
```bash
POST /tasks/suggest
Content-Type: application/json

{
  "description": "I need to finish the project report by next Friday with charts and analysis"
}
```

**Response:** `200 OK`
```json
{
  "title": "Finish project report",
  "description": "Create charts and analysis for project report",
  "dueDate": "2024-01-19",
  "priority": "HIGH",
  "status": "TODO"
}
```

**Note:** The AI generates a task object but does NOT automatically save it. You can review it and then create it with the POST /tasks endpoint.

## Task Model

### Task Fields
- **id** (Long): Unique identifier (auto-generated)
- **title** (String): Task name (required, max 100 chars)
- **description** (String): Detailed description (optional)
- **dueDate** (LocalDate): Due date in YYYY-MM-DD format (optional)
- **priority** (Enum): `LOW`, `MEDIUM`, `HIGH` (default: MEDIUM)
- **status** (Enum): `TODO`, `IN_PROGRESS`, `DONE` (default: TODO)
- **createdAt** (LocalDate): Creation date (auto-set)

## Testing

Run the comprehensive test suite (26 tests):

```bash
./gradlew test
```

Test coverage includes:
- **TaskServiceTest** (10 tests): Service layer business logic
- **TaskControllerIntegrationTest** (7 tests): REST endpoint integration
- **OllamaServiceTest** (9 tests): AI service with mocked responses

## Configuration

### application.properties

Key configuration options:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:taskdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Ollama AI
ollama.base-url=http://localhost:11434
ollama.model=mistral

# Logging
logging.level.root=INFO
logging.level.com.taskmanager=DEBUG
```

### Custom Ollama Configuration

To use a different Ollama instance or model, modify `application.properties`:

```properties
# Use remote Ollama server
ollama.base-url=http://192.168.1.100:11434

# Use different model (e.g., llama2 instead of mistral)
ollama.model=llama2
```

## Project Structure

```
task-manager-api/
├── src/main/java/com/taskmanager/
│   ├── TaskManagerApplication.java       # Main entry point
│   ├── config/
│   │   └── OllamaConfig.java            # Ollama configuration
│   ├── controller/
│   │   ├── TaskController.java          # REST endpoints
│   │   ├── TaskControllerAdvice.java    # Exception handling
│   │   ├── TaskRequest.java             # Task DTO
│   │   └── SuggestTaskRequest.java      # AI suggestion DTO
│   ├── model/
│   │   └── Task.java                    # Task entity with enums
│   ├── repository/
│   │   └── TaskRepository.java          # JPA repository
│   └── service/
│       ├── TaskService.java             # Business logic
│       └── OllamaService.java           # AI integration
├── src/main/resources/
│   ├── application.properties           # Configuration
│   └── static/
│       └── index.html                   # Web UI
├── src/test/java/com/taskmanager/
│   ├── controller/
│   │   └── TaskControllerIntegrationTest.java
│   └── service/
│       ├── TaskServiceTest.java
│       └── OllamaServiceTest.java
├── build.gradle                         # Gradle configuration
├── gradlew                              # Gradle wrapper (Unix)
├── gradlew.bat                          # Gradle wrapper (Windows)
└── README.md                            # This file
```

## Development

### Build Project
```bash
./gradlew clean build
```

### Run Tests
```bash
./gradlew test
```

### Build Docker Image (if needed)
```bash
./gradlew bootBuildImage
```

### Check Dependencies
```bash
./gradlew dependencies
```

## Troubleshooting

### Application fails to start
- Ensure Java 17 is installed: `java -version`
- Check if port 8080 is available: `lsof -i :8080`
- Start application with verbose logging:
  ```bash
  ./gradlew bootRun --info
  ```

### Ollama connection errors
- Verify Ollama is running: `ollama serve` (or check if running as service)
- Verify Mistral model is downloaded: `ollama list`
- Test Ollama directly: `curl http://localhost:11434/api/tags`
- If Ollama unavailable, AI suggestions will fall back to default task

### Tests fail
- Ensure H2 database driver is included: `./gradlew dependencies | grep h2`
- Run tests with verbose output: `./gradlew test --info`
- Clean and rebuild: `./gradlew clean build`

## Technologies

- **Java 17** - Language
- **Spring Boot 3.3.0** - Framework
- **Spring Data JPA** - Database access
- **H2 Database** - In-memory database
- **Gradle 7+** - Build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking library
- **Lombok** - Boilerplate reduction
- **Ollama Mistral** - AI integration
- **Vanilla JavaScript** - Frontend

