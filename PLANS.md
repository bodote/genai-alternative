# Project Plan: Sherlock Holmes RAG Web App

Goal: Build a Spring Boot RAG web app that ingests the Project Gutenberg Sherlock Holmes corpus, retrieves top‑K chunks per query, and answers with grounded, guardrailed responses.

Principles: TDD first, Spring Modulith package boundaries, JSpecify `@NullMarked` per package, Gradle build.

## Phase 0: Foundations
1) Align dependencies & configuration
   - Decide embedding/chat provider (OpenAI initially) and models.
   - Add/verify Spring AI, pgvector, and DB config in `application.yaml`.
   - Add `package-info.java` with `@NullMarked` for each new package.
   - Tests first for any new behavior.

2) Define domain model
   - Identify core records: `Book`, `Chunk`, `Embedding`, `RetrievalResult`, `Answer`.
   - Decide metadata fields (book id, title, chapter, source URL, chunk index).

## Phase 1: Ingestion Pipeline
1) Gutenberg source discovery
   - Write tests for fetching the list of works for author 69.
   - Implement a downloader that resolves canonical text URLs and saves raw text.

2) Cleaning & normalization
   - Tests for stripping boilerplate headers/footers.
   - Normalize whitespace and fix obvious encoding issues.

3) Chunking
   - Tests for chunk size/overlap behavior and stable chunk boundaries.
   - Implement recursive text split (paragraph/sentence/word fallback).

4) Embedding & storage
   - Tests using mocked embedding client.
   - Persist chunks + embeddings into pgvector-backed store.

## Phase 2: Retrieval
1) Query embedding
   - Tests: embedding call + vector size match.
2) Top‑K search
   - Tests: deterministic retrieval ordering and metadata mapping.
3) Retrieval API
   - Service that returns `RetrievalResult` (chunks + metadata). 

## Phase 3: Guardrailed Generation
1) Prompt assembly
   - Tests: prompt includes top‑K context and user question.
2) Input guardrail
   - Tests: rejects policy‑violating inputs.
3) Output guardrail
   - Tests: blocks unsafe model responses.
4) Fact‑checking guardrail
   - Tests: answers not grounded in context are rejected.
5) Final answer orchestration
   - Tests: full RAG flow with mocked model and guardrails.

## Phase 4: Web App
1) HTTP endpoints
   - Tests for POST `/ask` (or similar) returning structured response.
2) UI
   - Minimal landing page with question input and answer panel.
3) Error handling
   - Clear user feedback for ingestion not run / no results / guardrail block.

## Phase 5: Observability & Ops
1) Logging & metrics
   - Log ingestion progress, retrieval hits, guardrail outcomes.
2) Admin tasks
   - Manual ingestion trigger endpoint or CLI task.
3) Documentation
   - Update README for setup, ingestion, and run instructions.

## Validation Checklist
- Unit tests for each new class with behavior.
- Integration tests for DB + vector search (Testcontainers).
- `./gradlew test` and `./gradlew jacocoTestReport` green.

