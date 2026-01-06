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
项目基于 **Java 21** 和 **Spring Boot 3.5+** 构建。采用 **LangChain4j** 作为 AI 集成的核心框架。**Model Context Protocol (MCP)** 服务必须使用 **Spring AI** 实现。

### II. RAG Architecture
检索增强生成（RAG）系统必须统一使用 **Elasticsearch** 作为向量数据库。必须配置高维度的 **Embedding 模型**（具体为 **Qwen/Ali Qianwen**）。

### III. Coding & API Standards
- 必须使用 **Lombok** 简化 POJO 开发。
- API 必须遵循 **RESTful** 规范，并集成 **Swagger** 进行文档化。
- **Hutool** 必须作为工具类的首选库，以保持代码简洁性。

## Technology Stack

- **编程语言**: Java 21
- **框架**: Spring Boot 3.5+
- **AI 集成**: LangChain4j, Spring AI
- **数据存储**: Elasticsearch
- **工具库**: Lombok, Hutool, Swagger

## Development Workflow

- **分支管理**: 使用功能分支（例如 `feature/xyz`），通过 Pull Request 合并。
- **文档**: API 变更需要相应的 Swagger 更新。
- **代码审查**: 代码必须经过审查，确保符合核心原则（特别是 Hutool 的使用和 REST 合规性）。

## Governance

本规范是架构和开发决策的主要参考依据。
- **修订**: 对这些原则的变更需要文档化的 PR 和团队共识。
- **合规性**: 所有代码审查必须验证是否符合这些原则。

**版本**: 1.0.0 | **批准日期**: 2026-01-05 | **最后修订**: 2026-01-05
