# Redis Sentinel 配置指南

## 概述

本项目已配置为使用Redis Sentinel模式，提供Redis的高可用性解决方案。Sentinel能够监控Redis主从复制，并在主节点故障时自动进行故障转移。

## 配置说明

### 1. application.yml配置

```yaml
spring:
  data:
    redis:
      # Redis数据库索引（默认为0）
      database: 0
      # Redis服务器连接密码（默认为空）
      password: 
      # 连接超时时间
      timeout: 5000ms
      # Sentinel配置
      sentinel:
        # Sentinel主服务器名称
        master: mymaster
        # Sentinel节点地址列表
        nodes:
          - localhost:26379
          - localhost:26380
          - localhost:26381
        # Sentinel密码（如果设置了的话）
        password: 
      # Lettuce连接池配置
      lettuce:
        pool:
          max-active: 20
          max-wait: -1ms
          max-idle: 10
          min-idle: 0
        shutdown-timeout: 100ms
```

### 2. 配置参数说明

- `master`: Sentinel监控的主服务器名称，默认为"mymaster"
- `nodes`: Sentinel节点列表，至少需要3个节点以保证高可用
- `password`: Redis服务器密码
- `sentinel.password`: Sentinel节点密码（如果配置了认证）
- `database`: Redis数据库索引
- `timeout`: 连接超时时间

## Redis Sentinel 部署

### 1. 准备Redis实例

需要准备以下Redis实例：
- 1个主节点（Master）
- 1个或多个从节点（Slave）
- 3个或更多Sentinel节点（推荐奇数个）

### 2. Redis主节点配置 (redis-master.conf)

```conf
# 绑定地址
bind 0.0.0.0
# 端口
port 6379
# 后台运行
daemonize yes
# 日志文件
logfile "/var/log/redis/redis-master.log"
# 数据目录
dir /var/lib/redis
# 持久化
save 900 1
save 300 10
save 60 10000
# 密码（可选）
# requirepass your_password
```

### 3. Redis从节点配置 (redis-slave.conf)

```conf
# 绑定地址
bind 0.0.0.0
# 端口
port 6380
# 后台运行
daemonize yes
# 日志文件
logfile "/var/log/redis/redis-slave.log"
# 数据目录
dir /var/lib/redis-slave
# 主从复制
replicaof 127.0.0.1 6379
# 主节点密码（如果设置了）
# masterauth your_password
# 从节点密码（可选）
# requirepass your_password
```

### 4. Sentinel配置 (sentinel.conf)

```conf
# 端口
port 26379
# 后台运行
daemonize yes
# 日志文件
logfile "/var/log/redis/sentinel.log"
# 监控主节点
sentinel monitor mymaster 127.0.0.1 6379 2
# 主节点密码（如果设置了）
# sentinel auth-pass mymaster your_password
# 主节点故障判定时间（毫秒）
sentinel down-after-milliseconds mymaster 30000
# 故障转移超时时间
sentinel failover-timeout mymaster 180000
# 同时进行复制的从节点数量
sentinel parallel-syncs mymaster 1
```

### 5. 启动顺序

1. 启动Redis主节点：`redis-server redis-master.conf`
2. 启动Redis从节点：`redis-server redis-slave.conf`
3. 启动Sentinel节点：`redis-sentinel sentinel.conf`

## Docker Compose 部署示例

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  redis-master:
    image: redis:7-alpine
    container_name: redis-master
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-master-data:/data

  redis-slave:
    image: redis:7-alpine
    container_name: redis-slave
    ports:
      - "6380:6379"
    command: redis-server --appendonly yes --replicaof redis-master 6379
    depends_on:
      - redis-master
    volumes:
      - redis-slave-data:/data

  redis-sentinel-1:
    image: redis:7-alpine
    container_name: redis-sentinel-1
    ports:
      - "26379:26379"
    command: >
      sh -c "echo 'port 26379
      sentinel monitor mymaster redis-master 6379 2
      sentinel down-after-milliseconds mymaster 30000
      sentinel failover-timeout mymaster 180000
      sentinel parallel-syncs mymaster 1' > /tmp/sentinel.conf &&
      redis-sentinel /tmp/sentinel.conf"
    depends_on:
      - redis-master
      - redis-slave

  redis-sentinel-2:
    image: redis:7-alpine
    container_name: redis-sentinel-2
    ports:
      - "26380:26379"
    command: >
      sh -c "echo 'port 26379
      sentinel monitor mymaster redis-master 6379 2
      sentinel down-after-milliseconds mymaster 30000
      sentinel failover-timeout mymaster 180000
      sentinel parallel-syncs mymaster 1' > /tmp/sentinel.conf &&
      redis-sentinel /tmp/sentinel.conf"
    depends_on:
      - redis-master
      - redis-slave

  redis-sentinel-3:
    image: redis:7-alpine
    container_name: redis-sentinel-3
    ports:
      - "26381:26379"
    command: >
      sh -c "echo 'port 26379
      sentinel monitor mymaster redis-master 6379 2
      sentinel down-after-milliseconds mymaster 30000
      sentinel failover-timeout mymaster 180000
      sentinel parallel-syncs mymaster 1' > /tmp/sentinel.conf &&
      redis-sentinel /tmp/sentinel.conf"
    depends_on:
      - redis-master
      - redis-slave

volumes:
  redis-master-data:
  redis-slave-data:
```

启动命令：`docker-compose up -d`

## 监控接口

应用提供了以下监控接口：

### 1. 基本测试
- `GET /redis/test` - 测试Redis连接

### 2. Sentinel监控
- `GET /redis/sentinel/health` - 获取Sentinel健康状态
- `GET /redis/sentinel/master` - 获取主节点信息
- `GET /redis/sentinel/status` - 获取综合状态信息

### 3. 示例响应

```json
{
  "connectionFactoryType": "LettuceConnectionFactory",
  "masterName": "mymaster",
  "sentinelNodes": ["localhost:26379", "localhost:26380", "localhost:26381"],
  "database": 0,
  "connectionStatus": "CONNECTED",
  "healthy": true,
  "timestamp": 1673510400000
}
```

## 故障转移测试

1. 停止Redis主节点
2. 观察Sentinel日志，确认故障检测
3. 等待故障转移完成（通常30-60秒）
4. 验证应用仍能正常连接Redis
5. 重启原主节点，它将作为从节点加入集群

## 注意事项

1. **最少3个Sentinel**: 确保至少有3个Sentinel节点，推荐奇数个
2. **网络分区**: 考虑网络分区情况，合理配置quorum值
3. **监控告警**: 建议配置监控告警，及时发现故障
4. **定期备份**: 定期备份Redis数据
5. **性能监控**: 监控Redis性能指标，如内存使用、连接数等

## 故障排查

### 常见问题

1. **连接超时**: 检查网络连通性和防火墙设置
2. **认证失败**: 确认密码配置正确
3. **Sentinel无法发现主节点**: 检查Sentinel配置和网络
4. **故障转移失败**: 检查quorum设置和Sentinel数量

### 日志查看

```bash
# 查看Redis日志
tail -f /var/log/redis/redis-master.log

# 查看Sentinel日志
tail -f /var/log/redis/sentinel.log

# Docker环境查看日志
docker logs redis-sentinel-1
```