# CodeCollab OJ 后端

基于 Spring Boot 3.x 的在线实时协作评测系统后端服务。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **Java版本**: 17+
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.5
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **实时通信**: WebSocket (STOMP)

## 项目结构

```
src/main/java/com/codecollab/oj/
├── config/              # 配置类
│   ├── WebSocketConfig.java
│   └── RabbitMQConfig.java
├── controller/          # 控制器
│   ├── ProblemController.java
│   └── SubmitController.java
├── service/             # 服务层
│   ├── QuestionService.java
│   ├── JudgeService.java
│   └── impl/
├── mapper/              # 数据访问层
│   ├── QuestionMapper.java
│   └── QuestionSubmitMapper.java
├── model/               # 数据模型
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   └── vo/              # 视图对象
└── common/              # 通用类
    └── BaseResponse.java
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+

### 2. 数据库配置

创建数据库：
```sql
CREATE DATABASE codecollab_oj CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行建表脚本：
```bash
mysql -u root -p codecollab_oj < src/main/resources/db/schema.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库、Redis、RabbitMQ配置。

### 4. 运行项目

```bash
mvn spring-boot:run
```

或者使用IDE直接运行 `CodeCollabOjApplication.java`

### 5. 访问接口

- 获取题目: `GET http://localhost:8080/api/problem/{id}`
- 提交代码: `POST http://localhost:8080/api/submit`
- 获取判题结果: `GET http://localhost:8080/api/submit/{submitId}`
- WebSocket: `ws://localhost:8080/ws`

## API 接口

### 获取题目

```http
GET /api/problem/{id}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "两数之和",
    "content": "...",
    "tags": ["数组", "哈希表"],
    "submitNum": 1200,
    "acceptedNum": 856,
    "judgeConfig": {
      "timeLimit": 1000,
      "memoryLimit": 256
    }
  }
}
```

### 提交代码

```http
POST /api/submit
Content-Type: application/json

{
  "problemId": 1,
  "code": "public class Solution {...}",
  "language": "java"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": 0,
    "judgeInfo": null
  }
}
```

### 获取判题结果

```http
GET /api/submit/{submitId}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": 2,
    "judgeInfo": {
      "time": 100,
      "memory": 5000,
      "message": "Accepted"
    }
  }
}
```

## 功能说明

### 当前实现（Demo版本）

- ✅ 题目查询
- ✅ 代码提交
- ✅ Mock判题（简单判断代码是否包含main方法）
- ✅ WebSocket配置（基础框架）
- ✅ RabbitMQ配置（消息队列）

### 待实现功能

- [ ] Docker沙箱判题
- [ ] 真实代码执行和测试用例对比
- [ ] WebSocket实时协作
- [ ] AI诊断接口
- [ ] 用户认证（JWT）
- [ ] 房间管理

## 注意事项

1. **判题服务**：当前是Mock版本，实际应该使用Docker沙箱执行代码
2. **WebSocket**：已配置基础框架，需要实现具体的协作逻辑
3. **消息队列**：已配置，判题任务会发送到队列，但消费者需要单独实现
4. **数据库**：需要先创建数据库并执行建表脚本

## 开发计划

- Phase 1: 基础CRUD ✅
- Phase 2: 判题核心（Docker沙箱）
- Phase 3: 实时协作（WebSocket）
- Phase 4: AI辅助

