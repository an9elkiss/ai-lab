---
alwaysApply: true
---
1. 代码规范
   - 遵循 DDD 分层架构
   - 控制器层：@RestController
   - api接口的url以/api/v1开头
   - 服务层：@Service
   - 领域模型层：entity/DTO/VO
   - VO的Date类型字段标注上@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
   - Cmd类不需要序列化
   - 注入bean使用@Autowired
   - 日期形字段使用 java.util.Date
   - 使用cn.hutool.json.JSONUtil进行对象与JSON String之间的转换
   - 在pom中添加依赖时，注明用途
   - 为API类和每个接口方法添加 Swagger V3 注解
   - API返回值为统一的 ApiResponse<T>
   - com.an9elkiss.pockettp.entity / com.an9elkiss.pockettp.dto包下的类标注@Data
   - 记录日志使用@Slf4j
   - 数据操作层写在mapper包下。repository包将被废弃
   
2. SQL
   - 数据库类型为MySQL
   - 表名格式t_{模块}_{业务类型}
   - 主键字段为id,自增长
   - 包含create_time, update_time，并且为not null
   - mapper.xml中<符号使用转义符&lt;
   - 生成SQL语句时，如果是建表语句，必须包含以下2个字段：
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
   - insert / update 语句不需要包含create_time、update_time

## 最佳实践
1. 异常处理
   - 使用统一的全局异常处理
   - 详细的异常日志记录

2. 接口设计
   - RESTful API 规范
   - 统一响应格式
   - 请求参数验证

3. 性能优化
   - 合理使用缓存
   - 避免循环中进行数据库操作
   - 使用批处理处理大量数据
   - 异步处理耗时操作

Always respond in 中文