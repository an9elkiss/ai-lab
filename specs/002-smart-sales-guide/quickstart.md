# Quick Start Guide: Brand Clothing Intelligent Shopping Assistant Robot

**Feature**: Brand Clothing Intelligent Shopping Assistant Robot  
**Branch**: `002-smart-sales-guide`  
**Date**: 2026-01-06

## Overview

本文档提供智能导购机器人系统的快速开始指南，包括环境准备、依赖安装、配置说明和基本使用示例。

---

## Prerequisites

### Required Software

- **Java 21+**: [OpenJDK 21](https://openjdk.org/) 或 Oracle JDK 21
- **Maven 3.8+**: [Apache Maven](https://maven.apache.org/)
- **Docker & Docker Compose**: [Docker Desktop](https://www.docker.com/products/docker-desktop/)（用于本地开发环境）

### Required Services

- **PostgreSQL 8.0+** 或 **MySQL 8.0+**: Agent Memory 持久化后端
- **Elasticsearch 8.x**: 销售政策知识库（向量数据库）
- **Redis 7.x**: 会话上下文缓存

### External Services (Mock/Stub)

- **商品服务 API**: 提供商品信息（ProductSearchTool）
- **搭配服务 API**: 提供搭配信息（LookbookSearchTool）
- **库存服务 API**: 提供库存查询（InventoryCheckTool）
- **促销服务 API**: 提供促销信息（PromotionTool）
- **CLIP 服务 API**: 提供图片向量生成（CLIP模型服务）

---

## Local Development Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd ai-lab
git checkout 002-smart-sales-guide
```

### 2. Start Infrastructure Services

使用 Docker Compose 启动基础服务：

```bash
cd backend
docker-compose up -d
```

`docker-compose.yml` 应包含：
- PostgreSQL/MySQL（端口 5432/3306）
- Elasticsearch（端口 9200）
- Redis（端口 6379）

### 3. Configure Application

创建 `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartsales
    username: postgres
    password: postgres
  redis:
    host: localhost
    port: 6379
  elasticsearch:
    uris: http://localhost:9200

langchain4j:
  chat-model:
    provider: openai # 或 qwen, 根据实际LLM选择
    api-key: ${LLM_API_KEY}
  embedding-model:
    provider: qwen
    api-key: ${QWEN_API_KEY}
  memory:
    type: database
    database:
      url: jdbc:postgresql://localhost:5432/smartsales
      username: postgres
      password: postgres

external-services:
  product-api:
    base-url: http://localhost:8081/api/products
  lookbook-api:
    base-url: http://localhost:8081/api/lookbooks
  inventory-api:
    base-url: http://localhost:8082/api/inventory
  promotion-api:
    base-url: http://localhost:8082/api/promotions
  clip-service:
    base-url: http://localhost:8083/api/clip
```

### 4. Initialize Database Schema

Agent Memory 表结构由 LangChain4j 自动创建，无需手动初始化。

Elasticsearch 索引创建脚本（`backend/scripts/init-elasticsearch.sh`）:

```bash
curl -X PUT "localhost:9200/sales-policy" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "policyId": {"type": "keyword"},
      "type": {"type": "keyword"},
      "content": {"type": "text", "analyzer": "ik_max_word"},
      "embedding": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,
        "similarity": "cosine"
      },
      "tags": {"type": "keyword"},
      "priority": {"type": "integer"},
      "effectiveDate": {"type": "date"},
      "expiryDate": {"type": "date"}
    }
  }
}
'
```

### 5. Seed Sales Policy Knowledge Base

导入销售政策知识库数据（示例）：

```bash
curl -X POST "localhost:9200/sales-policy/_doc" -H 'Content-Type: application/json' -d'
{
  "policyId": "policy_001",
  "type": "SALES_PHRASE",
  "content": "这款正红色非常衬肤色，剪裁适合晚宴场合",
  "embedding": [0.1, 0.2, ...],  # 768维向量（需通过Qwen Embedding生成）
  "tags": ["红色", "晚宴", "正式"],
  "priority": 8,
  "effectiveDate": "2026-01-01"
}
'
```

### 6. Build and Run

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

应用将在 `http://localhost:8080` 启动。

---

## API Usage Examples

### 1. Create Session

```bash
curl -X POST http://localhost:8080/v1/chat/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_12345"
  }'
```

Response:
```json
{
  "sessionId": "session_abc123",
  "userId": "user_12345",
  "createdAt": "2026-01-06T10:00:00Z"
}
```

### 2. Send Message

```bash
curl -X POST http://localhost:8080/v1/chat/sessions/session_abc123/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "我想买一件红色连衣裙"
  }'
```

Response:
```json
{
  "messageId": "msg_12345",
  "content": "好的，我为您推荐几款红色连衣裙...",
  "recommendations": [
    {
      "type": "PRODUCT",
      "id": "product_001",
      "name": "正红色A字连衣裙",
      "reason": "这款正红色非常衬肤色，剪裁适合晚宴场合",
      "price": 599.00,
      "imageUrl": "https://example.com/product.jpg"
    }
  ],
  "state": "RECOMMENDATION",
  "requiresClarification": false,
  "timestamp": "2026-01-06T10:00:05Z"
}
```

### 3. Send Message with Image

```bash
curl -X POST http://localhost:8080/v1/chat/sessions/session_abc123/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "这件衣服怎么搭？",
    "imageUrl": "https://example.com/user-image.jpg"
  }'
```

### 4. Get Session Info

```bash
curl http://localhost:8080/v1/chat/sessions/session_abc123
```

---

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify -P integration-test
```

### Contract Tests

```bash
mvn test -Dtest=ContractTest
```

---

## Development Workflow

### 1. Add New Tool

1. 创建 Tool 类（实现 LangChain4j `Tool` 接口）:
```java
@Tool("Search products by attributes")
public List<Product> searchProducts(
    @ToolParam("category") String category,
    @ToolParam("color") String color
) {
    return productRetrievalService.search(category, color);
}
```

2. 注册到 Agent:
```java
@Bean
public SalesAssistantAgent salesAssistantAgent(
    ChatLanguageModel chatModel,
    ProductSearchTool productSearchTool
) {
    return SalesAssistantAgent.builder()
        .chatLanguageModel(chatModel)
        .tools(productSearchTool)
        .build();
}
```

### 2. Update Sales Policy Knowledge Base

1. 准备知识内容（销售话术或推荐策略）
2. 使用 Qwen Embedding 生成向量
3. 导入到 Elasticsearch

### 3. Monitor Agent Memory

查看用户画像数据：
```sql
SELECT * FROM agent_memory WHERE user_id = 'user_12345';
```

---

## Troubleshooting

### Common Issues

1. **Agent Memory 表未创建**
   - 检查数据库连接配置
   - 确认 LangChain4j 版本支持数据库持久化

2. **Elasticsearch 连接失败**
   - 检查 Elasticsearch 是否启动: `curl http://localhost:9200`
   - 确认索引已创建: `curl http://localhost:9200/_cat/indices`

3. **外部 API 调用失败**
   - 使用 WireMock 模拟外部服务进行本地开发
   - 检查 API 配置和网络连接

4. **CLIP 服务不可用**
   - 本地开发可使用 Mock CLIP 服务
   - 或使用预计算的图片向量

---

## Next Steps

1. ✅ 完成本地环境搭建
2. ✅ 验证 API 接口
3. 🔄 集成外部服务（商品、搭配、库存、促销）
4. 🔄 部署 CLIP 模型服务
5. 🔄 完善销售政策知识库内容
6. 🔄 前端集成（可选）

---

**文档完成日期**: 2026-01-06  
**最后更新**: 2026-01-06
