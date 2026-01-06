# Implementation Plan: Brand Clothing Intelligent Shopping Assistant Robot

**Branch**: `002-smart-sales-guide` | **Date**: 2026-01-06 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-smart-sales-guide/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

本项目为某服装品牌构建在线智能导购系统，模拟金牌店员交互逻辑，通过深层对话精准捕捉用户需求，利用实时数据驱动转化，最终引导用户完成商品加购。

核心能力包括：
- 智能需求感知（自然语言/图片识别）
- 交互式需求澄清（混合模式 AI 推断 + 工具校验）
- 精选商品推举（根据用户意图进行单品或Lookbook推荐）
- 互动反馈优化（视觉相似性检索 + 动态候选集刷新）
- 转化引导与闭环（尺码推荐、库存提醒、加购引导）

技术架构基于 LangChain4j 构建对话式 AI Agent，使用 Elasticsearch 作为向量数据库搭建销售政策知识库，商品和搭配信息通过 Tool 封装外部 API 提供，用户画像通过 LangChain4j Agent Memory 机制管理。

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.5+, LangChain4j (AI集成), Spring AI (MCP服务), Lombok, Hutool, Swagger  
**Storage**: 
- LangChain4j Agent Memory (用户画像、会话上下文、反馈记录)
  - 持久化后端：关系型数据库 PostgreSQL/MySQL（存储用户画像和偏好标签）
  - 会话上下文：仅在内存中维护，会话结束后清空
- Elasticsearch 8.x (销售政策知识库 - 向量数据库)
- Redis 7.x (会话上下文缓存)
**Testing**: JUnit 5, Spring Boot Test, REST Assured, WireMock  
**Target Platform**: Linux server (容器化部署)  
**Project Type**: Web (RESTful API 后端 + 前端集成点)  
**Performance Goals**: 
- 响应延迟 < 2秒（P95）
- 支持并发会话：初期 500 并发会话，峰值设计 2000 并发
- Embedding 检索耗时 < 500ms  
**Constraints**: 
- 意图识别准确率 > 90%
- 平均会话轮数 5-10 轮
- 推荐接受率 > 20%  
**Scale/Scope**: 
- 商品库规模：初期 10,000 SKU，扩展设计支持 100,000 SKU（通过外部 API 提供）
- Lookbook 库规模：初期 500-1000 套搭配方案（通过外部 API 提供）
- 用户并发量：初期支持 500 并发会话，峰值设计 2000 并发
- 对话历史保留策略：短期记忆（Session 内，Redis 30分钟）+ 长期记忆（用户画像，PostgreSQL/MySQL）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Initial Check (Before Phase 0)

| 原则 | 要求 | 本项目状态 | 符合性 |
|------|------|-----------|--------|
| **Core Architecture** | Java 21 + Spring Boot 3.5+ | ✅ Java 21 + Spring Boot 3.5+ | **PASS** |
| **AI Integration** | LangChain4j (核心) + Spring AI (MCP) | ✅ LangChain4j 用于对话式 Agent, Spring AI 用于 MCP 服务 | **PASS** |
| **RAG Architecture** | Elasticsearch + Qwen Embedding | ✅ Elasticsearch 用于销售政策知识库（向量数据库） | **PASS** |
| **Coding Standards** | Lombok + Hutool | ✅ 使用 Lombok 简化 POJO，Hutool 作为工具类首选 | **PASS** |
| **API Standards** | RESTful + Swagger | ✅ RESTful API 设计，Swagger 文档化 | **PASS** |

**结论**: 所有核心原则均满足，无违规项。✅ 准予进入 Phase 0。

### Re-check (After Phase 1 Design)

| 原则 | Phase 1 设计决策 | 符合性 |
|------|-----------------|--------|
| **Core Architecture** | 确认使用 Java 21 + Spring Boot 3.5+ 标准分层架构 | ✅ **PASS** |
| **AI Integration** | LangChain4j Agent + 工具（Tool-based）+ Spring AI 预留MCP扩展 | ✅ **PASS** |
| **RAG Architecture** | Elasticsearch 8.x + Qwen Embedding V2 (768维) + CLIP图片向量服务 | ✅ **PASS** |
| **Data Storage** | LangChain4j Agent Memory (用户画像持久化到 PostgreSQL/MySQL, 会话上下文内存) + Redis (临时上下文) + Elasticsearch (销售政策知识库) | ✅ **符合多层存储架构** |
| **Coding Standards** | 数据模型使用 Lombok `@Data`, 工具类预设 Hutool 优先 | ✅ **PASS** |
| **API Standards** | OpenAPI 3.0.3 规范 + Swagger UI + RESTful 路径设计 | ✅ **PASS** |

**新增技术（未在原 Constitution 中明确，需记录）**:
- **CLIP 模型**: 用于视觉相似性检索（处理元数据缺失场景），作为独立服务（API）
- **Redis**: 会话上下文缓存（符合性能优化原则）
- **LangChain4j Agent Memory**: Agent 记忆机制，用户画像和偏好标签通过关系型数据库（PostgreSQL/MySQL）持久化，会话上下文仅在内存中维护
- **PostgreSQL/MySQL**: 作为 Agent Memory 的持久化后端，存储用户画像和偏好标签（不直接存储会话数据）

**结论**: 
- ✅ Phase 1 设计完全符合 Constitution 要求
- ✅ 新增技术均为合理的架构补充，不违反核心原则
- ✅ **准予进入 Phase 2 (任务拆解)**

## Project Structure

### Documentation (this feature)

```text
specs/002-smart-sales-guide/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/ailab/smartsales/
│   ├── agent/              # LangChain4j Agent 定义
│   │   ├── SalesAssistantAgent.java
│   │   ├── tools/          # Agent 可调用的工具
│   │   │   ├── ProductSearchTool.java
│   │   │   ├── LookbookSearchTool.java
│   │   │   ├── SizeRecommendationTool.java
│   │   │   ├── InventoryCheckTool.java
│   │   │   └── PromotionTool.java
│   │   └── memory/         # Agent Memory 配置（LangChain4j）
│   │       └── AgentMemoryConfig.java
│   ├── model/              # 实体模型
│   │   ├── domain/         # 领域模型
│   │   │   ├── Product.java
│   │   │   ├── Lookbook.java
│   │   │   ├── UserProfile.java
│   │   │   ├── DialogueSession.java
│   │   │   └── SalesPolicy.java
│   │   └── dto/            # API 传输对象
│   ├── service/            # 业务服务
│   │   ├── IntentRecognitionService.java
│   │   ├── ProductRetrievalService.java
│   │   ├── RecommendationService.java
│   │   ├── FeedbackProcessingService.java
│   │   └── SalesPolicyService.java
│   ├── repository/         # 数据访问层
│   │   ├── ProductRepository.java (Elasticsearch - 销售政策知识库)
│   │   └── SalesPolicyRepository.java (Elasticsearch)
│   │   # 注意：UserProfile 通过 LangChain4j Agent Memory 管理，不在此层
│   ├── api/                # RESTful API 控制器
│   │   └── ChatController.java
│   │   # 注意：所有商品相关操作通过 Agent 和 Tool 完成，不需要 ProductController
│   └── config/             # Spring 配置
│       ├── LangChain4jConfig.java
│       ├── AgentMemoryConfig.java (Agent Memory 持久化后端配置)
│       ├── ElasticsearchConfig.java
│       ├── DatabaseConfig.java (PostgreSQL/MySQL 配置，用于 Agent Memory 持久化)
│       └── SwaggerConfig.java
└── src/test/java/
    ├── contract/           # API Contract Tests
    ├── integration/        # 集成测试
    └── unit/               # 单元测试

frontend/ (可选，如有独立前端)
├── src/
│   ├── components/
│   │   ├── ChatWindow.tsx
│   │   └── ProductCard.tsx
│   ├── pages/
│   │   └── ShoppingAssistant.tsx
│   └── services/
│       └── chatApi.ts
└── tests/
```

**Structure Decision**: 选择 Web 应用结构（后端 + 可选前端集成）。后端采用 Spring Boot 标准分层架构，将 LangChain4j Agent 作为独立模块（`agent/`），工具封装在 `agent/tools/` 下。Elasticsearch 访问通过 Repository 层统一管理（仅用于销售政策知识库）。用户画像和偏好标签通过 LangChain4j Agent Memory 机制管理，使用关系型数据库（PostgreSQL/MySQL）作为持久化后端，不通过传统 Repository 层访问。前端部分根据实际需求可能作为独立项目或嵌入现有电商平台。

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**状态**: ✅ 无违规项，本节无需填写。

所有设计决策均符合 AI Lab Constitution，未引入超出必要范围的复杂性。
