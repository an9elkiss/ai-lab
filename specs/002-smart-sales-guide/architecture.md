# System Architecture: Brand Clothing Intelligent Shopping Assistant Robot

**Feature**: Brand Clothing Intelligent Shopping Assistant Robot
**Branch**: `002-smart-sales-guide`
**Date**: 2026-01-06

本文档基于 `spec.md` 和 `plan.md` 绘制系统架构图，展示组件交互、数据流向及技术栈集成。

---

## 1. System Context Diagram (系统上下文)

展示智能导购机器人与用户及外部系统的宏观交互。

```mermaid
C4Context
    title System Context Diagram for Smart Sales Assistant

    Person(customer, "Customer", "品牌服装消费者")
    
    System(sales_bot, "Smart Sales Assistant", "智能导购机器人系统\n提供自然语言导购、搭配推荐、尺码建议等服务")
    
    System_Ext(ecommerce_platform, "E-commerce Platform", "现有电商平台核心系统\n提供商品、库存、促销、订单服务")
    System_Ext(clip_service, "CLIP Service", "视觉向量服务\n提供图片向量化和视觉相似性计算")
    System_Ext(llm_service, "LLM Service", "大语言模型服务\n(e.g., OpenAI, Qwen) 提供推理能力")

    Rel(customer, sales_bot, "进行对话交互", "HTTPS/WebSocket")
    Rel(sales_bot, ecommerce_platform, "查询商品/库存/搭配", "REST API")
    Rel(sales_bot, clip_service, "生成图片向量", "REST API")
    Rel(sales_bot, llm_service, "发送Prompt/获取推理结果", "HTTPS")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

---

## 2. Container Architecture Diagram (容器架构)

展示系统内部核心容器、数据存储及详细技术选型。

```mermaid
C4Container
    title Container Diagram - Smart Sales Assistant System

    Person(customer, "Customer", "Web/Mobile User")

    Container_Boundary(frontend_boundary, "Frontend Layer") {
        Container(web_app, "Chat Interface", "React/Vue", "用户对话窗口组件")
    }

    Container_Boundary(backend_boundary, "Backend Layer (Spring Boot)") {
        Container(api_gateway, "API Layer", "Spring MVC", "ChatController\n处理会话请求，鉴权")
        
        Component(agent_core, "LangChain4j Agent", "Java", "核心对话逻辑、状态机管理、Prompt工程")
        
        Component(tool_layer, "Tool Layer", "Java Beans", "封装外部服务调用\nProductTool, LookbookTool, etc.")
        
        Component(rag_service, "RAG Service", "Spring AI", "销售政策知识库检索服务")
    }

    Container_Boundary(storage_boundary, "Data Storage Layer") {
        ContainerDb(redis, "Session Cache", "Redis 7.x", "存储会话上下文 (Short-term Memory)\nTTL: 30min")
        ContainerDb(mysql, "User Store", "PostgreSQL/MySQL", "Agent Memory 持久化后端 (Long-term Memory)\n用户画像、偏好标签")
        ContainerDb(es, "Vector DB", "Elasticsearch 8.x", "销售政策知识库 (Sales Policy)\n存储话术向量、推荐策略")
    }

    System_Ext(ext_api, "External APIs", "Product, Inventory, Lookbook APIs")
    System_Ext(clip, "CLIP Service", "Image Vectorization")

    Rel(customer, web_app, "发送消息")
    Rel(web_app, api_gateway, "POST /chat/messages", "JSON/HTTPS")
    
    Rel(api_gateway, agent_core, "转发消息")
    
    Rel(agent_core, redis, "读写会话状态", "Jedis")
    Rel(agent_core, mysql, "加载/更新用户画像", "JDBC/JPA")
    
    Rel(agent_core, tool_layer, "调用工具")
    Rel(tool_layer, ext_api, "查询业务数据", "HTTP Client")
    Rel(tool_layer, clip, "图片向量化", "HTTP Client")
    
    Rel(agent_core, rag_service, "检索销售话术")
    Rel(rag_service, es, "向量检索 (k-NN)", "ES Client")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

---

## 3. Agent Internal Logic Flow (Agent 内部逻辑)

展示 LangChain4j Agent 处理一条用户消息的内部流程。

```mermaid
flowchart TD
    UserMsg[用户消息输入] --> ContextLoad[加载上下文]
    
    subgraph Memory_Management [Memory Management]
        ContextLoad -->|Session ID| LoadRedis[加载 Redis 短期记忆]
        ContextLoad -->|User ID| LoadDB[加载 MySQL 长期画像]
        LoadRedis & LoadDB --> MergeContext[合并上下文]
    end
    
    MergeContext --> IntentRec[意图识别 & 状态机]
    
    subgraph Decision_Process [Decision Process]
        IntentRec -->|缺失信息| Clarify[生成澄清问题]
        IntentRec -->|信息完整| ToolSelect[选择工具]
        
        ToolSelect -->|需要商品| CallProduct[ProductSearchTool]
        ToolSelect -->|需要搭配| CallLookbook[LookbookSearchTool]
        ToolSelect -->|需要库存| CallInventory[InventoryCheckTool]
        ToolSelect -->|需要政策| CallRAG[Elasticsearch RAG]
        
        CallProduct & CallLookbook & CallInventory & CallRAG --> Observation[获取工具结果]
    end
    
    subgraph Response_Generation [Response Generation]
        Observation -->|基于结果| GenerateReason[生成推荐理由]
        GenerateReason -->|应用话术| PolishResponse[润色回复 RAG辅助]
    end
    
    PolishResponse --> UpdateMem[更新记忆]
    
    subgraph Memory_Update [Memory Update]
        UpdateMem --> SaveRedis[更新 Redis 会话状态]
        UpdateMem -->|有新偏好?| SaveDB[持久化画像到 MySQL]
    end
    
    SaveDB --> FinalResponse[返回给用户]
```

---

## 4. Data Flow & Storage Strategy (数据流与存储)

| 数据类型 | 存储组件 | 持久化策略 | 访问方式 |
|---------|---------|-----------|---------|
| **会话上下文** (Context) | Redis | **短期** (TTL 30min) | 会话 ID 索引，Agent 自动加载 |
| **用户画像** (User Profile) | MySQL/PG | **长期** (持久化) | 用户 ID 索引，LangChain4j Memory Backend |
| **销售政策** (Sales Policy) | Elasticsearch | **长期** (知识库) | 向量检索 (k-NN)，Agent RAG 调用 |
| **商品数据** (Product) | 外部系统 | **不存储** (实时查询) | ProductSearchTool 封装 API 调用 |
| **搭配数据** (Lookbook) | 外部系统 | **不存储** (实时查询) | LookbookSearchTool 封装 API 调用 |
| **图片向量** (Image Vec) | 内存/临时 | **不存储** (实时计算) | CLIP Service 计算，仅用于单次检索 |

