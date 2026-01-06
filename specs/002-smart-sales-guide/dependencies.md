# Dependencies Specification

**Feature**: Brand Clothing Intelligent Shopping Assistant Robot  
**Branch**: `002-smart-sales-guide`  
**Date**: 2026-01-06  
**Framework**: **Spring Boot Only** (不使用 Spring Cloud)

## Maven Dependencies (pom.xml)

### Spring Boot Parent

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.0</version>
    <relativePath/>
</parent>

<properties>
    <java.version>21</java.version>
    <langchain4j.version>0.35.0</langchain4j.version>
    <springai.version>1.0.0</springai.version>
</properties>
```

---

## Core Dependencies

### 1. Spring Boot Starters

```xml
<!-- Web API -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Data JPA (MySQL) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Elasticsearch (Data) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Actuator (Health Check & Metrics) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

### 2. Database Drivers

```xml
<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- HikariCP (included in spring-boot-starter-jdbc) -->
<!-- No additional dependency needed -->
```

---

### 3. AI & LLM Integration

```xml
<!-- LangChain4j Core -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>${langchain4j.version}</version>
</dependency>

<!-- LangChain4j Spring Boot Starter -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>${langchain4j.version}</version>
</dependency>

<!-- LangChain4j OpenAI (or Qwen) -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>${langchain4j.version}</version>
</dependency>

<!-- Spring AI (for MCP support) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-core</artifactId>
    <version>${springai.version}</version>
</dependency>
```

---

### 4. Utilities

```xml
<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Hutool (工具类库) -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.25</version>
</dependency>

<!-- Swagger (OpenAPI 3) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

---

### 5. JSON Processing

```xml
<!-- Jackson (included in spring-boot-starter-web) -->
<!-- For additional features: -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

---

### 6. Database Migration

```xml
<!-- Flyway (or Liquibase) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

---

## Testing Dependencies

### 1. Core Testing

```xml
<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <!-- Exclude JUnit 4 if needed -->
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

### 2. REST API Testing

```xml
<!-- REST Assured -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST Assured Spring Mock MVC -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>spring-mock-mvc</artifactId>
    <scope>test</scope>
</dependency>
```

---

### 3. Mock & Stub

```xml
<!-- WireMock (外部依赖模拟) -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.3.1</version>
    <scope>test</scope>
</dependency>

<!-- Mockito (included in spring-boot-starter-test) -->
```

---

### 4. Database Testing

```xml
<!-- H2 Database (in-memory for tests) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers (optional, for integration tests) -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

---

### 5. Contract Testing (Optional)

```xml
<!-- Pact (for frontend-backend contract testing) -->
<dependency>
    <groupId>au.com.dius.pact.consumer</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.3</version>
    <scope>test</scope>
</dependency>
```

---

### 6. Code Coverage

```xml
<!-- JaCoCo Plugin -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Development Dependencies

```xml
<!-- Spring Boot DevTools (Hot Reload) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

<!-- Spring Boot Configuration Processor -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

---

## Build Plugins

```xml
<build>
    <plugins>
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>

        <!-- Maven Compiler Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>21</source>
                <target>21</target>
            </configuration>
        </plugin>

        <!-- Flyway Plugin -->
        <plugin>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-maven-plugin</artifactId>
            <version>10.4.1</version>
        </plugin>
    </plugins>
</build>
```

---

## Excluded Dependencies

**明确不使用的 Spring Cloud 组件**:

❌ **spring-cloud-starter-config** - 不使用配置中心  
❌ **spring-cloud-starter-netflix-eureka** - 不使用服务注册发现  
❌ **spring-cloud-starter-gateway** - 不使用网关  
❌ **spring-cloud-starter-openfeign** - 不使用声明式 HTTP 客户端  
❌ **spring-cloud-contract** - 不使用 CDC 契约测试  
❌ **spring-cloud-starter-circuitbreaker** - 不使用熔断器（使用 LangChain4j 自带重试）

**替代方案**:
- 配置管理 → Spring Boot `application.yml` + 环境变量
- HTTP 客户端 → `RestTemplate` 或 `WebClient` (Spring WebFlux)
- 契约测试 → REST Assured + WireMock

---

## Dependency Management Best Practices

1. **版本统一**: 使用 Spring Boot Parent 管理依赖版本
2. **Scope 控制**: 
   - 测试依赖使用 `<scope>test</scope>`
   - 运行时依赖使用 `<scope>runtime</scope>`
   - Lombok 使用 `<optional>true</optional>`
3. **排除冲突**: 使用 `<exclusions>` 排除传递依赖冲突
4. **安全更新**: 定期运行 `mvn versions:display-dependency-updates` 检查更新

---

## Verification Commands

```bash
# 查看依赖树
mvn dependency:tree

# 检查依赖冲突
mvn dependency:analyze

# 检查可更新的依赖
mvn versions:display-dependency-updates

# 检查插件更新
mvn versions:display-plugin-updates
```

---

## Summary

| 类别 | 依赖数量 | 说明 |
|------|---------|------|
| Spring Boot Starters | 6 个 | Web, JPA, Redis, Elasticsearch, Validation, Actuator |
| AI 集成 | 4 个 | LangChain4j, Spring AI |
| 数据库 | 2 个 | MySQL Driver, Flyway |
| 工具类 | 3 个 | Lombok, Hutool, Swagger |
| 测试框架 | 5 个 | Spring Boot Test, REST Assured, WireMock, Testcontainers, JaCoCo |
| **总计** | **20+** | **纯 Spring Boot 生态，无 Spring Cloud** |

---

**更新日期**: 2026-01-06  
**维护说明**: 本文档随项目演进更新，添加新依赖需在此记录
