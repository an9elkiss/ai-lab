<!--
SYNC IMPACT REPORT
Version: 0.0.0 -> 1.0.0
- Defined Principles: Core Architecture, RAG Architecture, Coding & API Standards.
- Replaced placeholders with concrete values based on user input.
- Validated alignment with project structure (Java, Spring Boot).
-->
# AI Lab Constitution

## Core Principles

### I. Core Architecture
The project is built on **Java 21** and **Spring Boot 3.5+**. **LangChain4j** is adopted as the core framework for AI integration. The **Model Context Protocol (MCP)** services MUST be implemented using **Spring AI**.

### II. RAG Architecture
The Retrieval-Augmented Generation (RAG) system MUST unify on **Elasticsearch** as the vector database. It MUST be configured with high-dimensional **Embedding models** (specifically **Qwen/Ali Qianwen**).

### III. Coding & API Standards
- **Lombok** MUST be used to simplify POJO development.
- APIs MUST follow **RESTful** specifications and integrate **Swagger** for documentation.
- **Hutool** MUST be the preferred library for utility classes to maintain code conciseness.

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5+
- **AI Integration**: LangChain4j, Spring AI
- **Data Store**: Elasticsearch
- **Tools**: Lombok, Hutool, Swagger

## Development Workflow

- **Branching**: Use feature branches (e.g., `feature/xyz`) merged via Pull Request.
- **Documentation**: API changes require corresponding Swagger updates.
- **Review**: Code must be reviewed for adherence to the Core Principles (especially usage of Hutool and REST compliance).

## Governance

This Constitution serves as the primary source of truth for architectural and development decisions.
- **Amendments**: Changes to these principles require a documented PR and team consensus.
- **Compliance**: All code reviews must verify alignment with these principles.

**Version**: 1.0.0 | **Ratified**: 2026-01-05 | **Last Amended**: 2026-01-05
