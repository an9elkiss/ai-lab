# 微商城系统 (WEIS)

基于Spring Boot 3.5.6的企业级电商微服务系统。

## 技术栈

- **JDK**: 21
- **Spring Boot**: 3.5.6
- **Maven**: 单模块项目
- **API文档**: SpringDoc OpenAPI 3 (Swagger)
- **工具类**: Hutool 5.8.34

## 项目结构

```
ai-lab/
├── pom.xml                          # Maven配置文件
├── README.md                        # 项目说明文档
├── .gitignore                       # Git忽略配置
└── src/
    └── main/
        ├── java/
        │   └── com/weis/demo/
        │       ├── DemoApplication.java          # 启动类
        │       ├── config/
        │       │   └── SwaggerConfig.java       # Swagger配置
        │       └── controller/
        │           └── HelloWorldController.java # 示例控制器
        └── resources/
            └── application.yml                   # 应用配置
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+

### 启动应用

1. **克隆项目**
```bash
git clone <repository-url>
cd ai-lab
```

2. **编译项目**
```bash
mvn clean install
```

3. **启动应用**
```bash
mvn spring-boot:run
```

或者直接运行：
```bash
java -jar target/ai-lab-1.0.0-SNAPSHOT.jar
```

### 访问应用

- **应用地址**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API文档**: http://localhost:8080/v3/api-docs

## API接口

### HelloWorld接口

#### 1. Hello World
```
GET /api/hello/world
```

返回示例：
```json
{
  "message": "Hello World!",
  "timestamp": "2025-12-16T10:00:00.000+00:00",
  "status": "success"
}
```

#### 2. 个性化问候
```
GET /api/hello/greet/{name}
```

参数：
- `name`: 用户名称

返回示例：
```json
{
  "message": "Hello, 张三!",
  "timestamp": "2025-12-16T10:00:00.000+00:00",
  "status": "success"
}
```

#### 3. 回显消息
```
POST /api/hello/echo
Content-Type: application/json

{
  "message": "测试消息",
  "data": "任意数据"
}
```

返回示例：
```json
{
  "echo": {
    "message": "测试消息",
    "data": "任意数据"
  },
  "timestamp": "2025-12-16T10:00:00.000+00:00",
  "status": "success"
}
```

#### 4. 系统信息
```
GET /api/hello/info
```

返回示例：
```json
{
  "application": "ai-lab",
  "version": "1.0.0-SNAPSHOT",
  "springBootVersion": "3.5.6",
  "javaVersion": "21",
  "timestamp": "2025-12-16T10:00:00.000+00:00",
  "status": "success"
}
```

## 开发规范

### 代码规范

- 日期字段使用 `java.util.Date`
- 使用 `cn.hutool.json.JSONUtil` 进行JSON转换
- Service实现类方法需要标注 `@Override` 并在接口中声明
- 普通日志使用 `warn` 级别，异常日志使用 `error` 级别

### 依赖管理规范

- 项目继承 `spring-boot-starter-parent`，直接使用Spring Boot的依赖管理
- 自定义依赖版本统一在 `pom.xml` 的 `<properties>` 中声明

## 许可证

Apache 2.0

## 联系方式

- Email: support@weis.com
- 文档: https://docs.weis.com

