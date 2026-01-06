# 🔍 Sherlock Holmes RAG Assistant

Spring Boot web app that builds a grounded, searchable knowledge base from the original Sherlock Holmes texts and answers questions with RAG (retrieval + guardrailed generation).

## Overview

Public LLM-based chatbots often struggle with detailed questions about classic literature. This application solves that problem by building a specialized knowledge base from the original Sherlock Holmes texts available on [Project Gutenberg](https://www.gutenberg.org/ebooks/author/69), then using RAG to provide accurate, grounded answers.

## Features

- **Document Ingestion**: Downloads and processes Sherlock Holmes stories from Project Gutenberg
- **Intelligent Chunking**: Splits texts into semantic chunks with configurable overlap
- **Vector Embeddings**: Uses OpenAI embeddings to create searchable vector representations
- **Semantic Search**: Retrieves the most relevant passages for any user query
- **Augmented Generation**: Combines retrieved context with LLM to generate accurate answers
- **Conversation History**: Maintains session-based chat history for contextual follow-up questions
- **Guardrails**: Input validation, output policy checking, and fact-checking to ensure response quality
- **Web Interface**: Clean, user-friendly chat interface for asking questions

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Spring Boot Application                      │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────┐  │
│  │ Ingestion   │    │ Retrieval   │    │ Generation              │  │
│  │ Module      │    │ Module      │    │ Module                  │  │
│  │             │    │             │    │                         │  │
│  │ • Download  │    │ • Query     │    │ • Augment Prompt        │  │
│  │ • Chunk     │    │   Embed     │    │ • Generate Response     │  │
│  │ • Embed     │    │ • Top-K     │    │ • Input/Output Guards   │  │
│  │ • Publish   │    │   Search    │    │ • Fact-Check            │  │
│  │             │    │             │    │ • Conversation History  │  │
│  └──────┬──────┘    └──────┬──────┘    └────────────┬────────────┘  │
│         │                  │                        │               │
│         └──────────────────┼────────────────────────┘               │
│                            │                                        │
│                    ┌───────▼───────┐                                │
│                    │  PostgreSQL   │                                │
│                    │  + pgvector   │                                │
│                    │ (Retrieval    │                                │
│                    │  owns DB)     │                                │
│                    └───────────────┘                                │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    Web UI Module                            │    │
│  │  • Chat Interface • Query Input • Response Display          │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │   OpenAI API          │
                    │   • Embeddings        │
                    │   • Chat Completions  │
                    └───────────────────────┘
```

## Tech Stack

- **Framework**: Spring Boot 3.5, Spring AI 1.1, Spring Modulith
- **Language**: Java 21
- **Build**: Gradle
- **AI Provider**: OpenAI (embeddings + chat)
- **Vector Store**: PostgreSQL with pgvector extension (owned exclusively by the retrieval module)
- **Testing**: JUnit 5, TestContainers (PostgreSQL), Playwright (E2E)
- **Web**: Thymeleaf + static HTML/CSS/JS frontend

## Data Source

All Sherlock Holmes stories from [Project Gutenberg](https://www.gutenberg.org/ebooks/author/69):
- The Adventures of Sherlock Holmes
- The Hound of the Baskervilles  
- A Study in Scarlet
- The Sign of the Four
- The Return of Sherlock Holmes
- The Memoirs of Sherlock Holmes
- The Valley of Fear
- His Last Bow
- The Case-Book of Sherlock Holmes

## Getting Started

### Prerequisites

- Java 21
- Docker (for PostgreSQL with pgvector)
- OpenAI API key (set as `OPENAI_API_KEY` environment variable)

### Start PostgreSQL with pgvector

Using Docker Compose (recommended):

```bash
docker compose up -d
```

Or manually with Docker (data persisted in `.docker/postgres-data`):

```bash
docker run -d --name sherlock-postgres \
  -e POSTGRES_DB=sherlock \
  -e POSTGRES_USER=sherlock \
  -e POSTGRES_PASSWORD=sherlock \
  -p 5432:5432 \
  -v "$(pwd)/.docker/postgres-data:/var/lib/postgresql/data" \
  pgvector/pgvector:pg16
```

### Running the Application

```bash
./gradlew bootRun
```

Then visit http://localhost:8081 to use the chat interface.

You can also use the helper script:

```bash
./app.sh start
./app.sh status
./app.sh stop
```

### Configuration

Configure in `src/main/resources/application.yaml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
  datasource:
    url: jdbc:postgresql://localhost:5432/sherlock
    username: sherlock
    password: sherlock
server:
  port: 8081
genai:
  ingestion:
    author-id: 69
    author-name: "Conan Doyle"
    chunk-size: 1000
    chunk-overlap: 100
    max-download-count: 2
    startup:
      enabled: true
    embedding:
      batch-size: 500
  generation:
    history-max-turns: 20
  retrieval:
    embedding-dimension: 1536
```

## Development

This project follows **TDD (Test-Driven Development)** with Spring Modulith architecture:

1. Write failing tests first
2. Implement minimal code to pass
3. Refactor while keeping tests green

See [AGENTS.md](AGENTS.md) for development guidelines.

## Testing

This project separates **unit tests** (fast, mocked) from **integration tests** (slower, real dependencies) using the Maven Surefire/Failsafe naming convention.

### Test Types

| Type | File Pattern | Speed | Dependencies | Command |
|------|--------------|-------|--------------|---------|
| Unit Tests | `*Test.java` | ~1 second | Mocked | `./gradlew test` |
| Integration Tests | `*IT.java` | ~30 seconds | Real (DB, network) | `./gradlew test --tests '*IT'` |

### Running Tests

```bash
# Unit tests only (fast, run frequently during development)
./gradlew test

# Integration tests only (requires Docker)
./gradlew test --tests '*IT'

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests 'de.bas.bodo.genai.ingestion.IngestionServiceTest'

# Run tests matching a pattern
./gradlew test --tests 'de.bas.bodo.genai.retrieval.*'
```

### E2E Tests with Playwright

The project includes end-to-end tests using Playwright that test the chat UI from a user's perspective.
Tests cover:

- **Legitimate questions** - Sherlock Holmes related queries
- **Input guardrail** - Blocks abusive/harmful input
- **Output guardrail** - Blocks inappropriate responses
- **Fact-checking guardrail** - Rejects hallucinated facts

Run E2E tests:

```bash
./gradlew test --tests 'de.bas.bodo.genai.e2e.*'
```

On first run, Playwright will automatically download the required browsers.

### Test Coverage

Generate test coverage reports using JaCoCo:

```bash
# Run tests and generate coverage report
./gradlew test jacocoTestReport

# Open HTML report
open build/reports/jacoco/test/html/index.html
```

**VS Code/Cursor Integration**: Install the **Coverage Gutters** extension to see coverage inline in the editor. After running tests, use `Cmd+Shift+P` → "Coverage Gutters: Display Coverage".

### Mutation Testing (Pitest)

Pitest runs can take several minutes depending on the number of mutations and available CPU/RAM.
By default, Pitest is configured to run **unit tests only** (`*Test`) and exclude `*IT` integration tests.

```bash
./gradlew pitest
```

## License

Public domain texts from Project Gutenberg. Application code under Apache 2.0.
