# 任务清单 REST API

这是一个 Java 21、Spring Boot 3、MyBatis-Plus 和 MySQL 项目，提供 Project/Task/Comment 三个业务模块、关联查询、分页搜索、任务状态修改，以及跨表事务接口。

## 环境

- Java 21
- Maven 3.10+
- MySQL 8.4+

## 启动本地 MySQL

Windows 本机已安装官方 MySQL ZIP 的情况下，在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-mysql.ps1
```

脚本会初始化外置数据目录（仅首次）、启动只监听本机回环地址的 MySQL，并创建 `task_api` 数据库。默认位置为：

- MySQL 程序：`D:\后端软件\mysql-8.4.13\mysql-8.4.13-winx64`
- 数据目录：`D:\mysql-data-task-api`
- 数据库：`task_api`

之后可从项目根目录启动应用：

```powershell
mvn spring-boot:run
```

如果终端没有配置 Java/Maven，可在 VS Code 集成终端使用：

```powershell
$env:JAVA_HOME = 'D:\后端软件\jdk-21\jdk-21.0.13+9'
$env:Path = "$env:JAVA_HOME\bin;D:\后端软件\apache-maven-3.10.0\bin;$env:Path"
mvn spring-boot:run
```

应用默认连接 `jdbc:mysql://localhost:3306/task_api`，本地开发默认 MySQL 用户为 `root`、密码为空。可用 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量覆盖连接设置。启动时 `src/main/resources/db/init.sql` 会创建缺失的表；已有数据不会被清空。

停止脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-local-mysql.ps1
```

## 数据模型

简单 ER 图见 [docs/er-diagram.md](docs/er-diagram.md)。`projects` 与 `tasks` 是一对多关系；任务可以没有所属项目。`tasks` 与 `comments` 也是一对多关系。删除项目会级联删除其任务和评论，删除任务会级联删除评论。建表语句见 [src/main/resources/db/init.sql](src/main/resources/db/init.sql)。

## 用户端接口

服务启动后可访问：

- Swagger UI：<http://localhost:8080/swagger-ui.html>
- OpenAPI JSON：<http://localhost:8080/v3/api-docs>
- 健康检查：<http://localhost:8080/api/health>

任务接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/v1/tasks` | 新增任务，可选 `projectId` |
| `POST` | `/api/v1/tasks/with-first-comment` | 在同一事务中创建任务和首条评论 |
| `GET` | `/api/v1/tasks/{id}` | 查询任务详情 |
| `GET` | `/api/v1/tasks` | 分页搜索任务 |
| `PUT` | `/api/v1/tasks/{id}` | 修改任务内容和完成状态 |
| `PATCH` | `/api/v1/tasks/{id}/status` | 单独修改任务完成状态 |
| `DELETE` | `/api/v1/tasks/{id}` | 删除任务 |

项目接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/v1/projects` | 新增项目 |
| `POST` | `/api/v1/projects/with-first-task` | 在同一事务中创建项目和首个任务 |
| `GET` | `/api/v1/projects/{id}` | 查询项目详情 |
| `GET` | `/api/v1/projects/{id}/tasks` | 查询项目下的任务 |
| `GET` | `/api/v1/projects` | 分页搜索项目 |
| `PUT` | `/api/v1/projects/{id}` | 修改项目 |
| `DELETE` | `/api/v1/projects/{id}` | 删除项目及其任务 |

评论接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/v1/tasks/{taskId}/comments` | 给任务新增评论 |
| `GET` | `/api/v1/tasks/{taskId}/comments` | 分页查询任务评论 |
| `GET` | `/api/v1/comments/{id}` | 查询评论详情 |
| `PUT` | `/api/v1/comments/{id}` | 修改评论 |
| `DELETE` | `/api/v1/comments/{id}` | 删除评论 |

分页参数：`page` 从 1 开始，`size` 范围为 1–100。任务支持 `projectId`、`completed`、`keyword` 筛选；项目支持 `keyword` 搜索；评论列表固定按任务关联并按 ID 倒序排列。成功和错误响应统一包装为 `{ success, data, error }`。

PowerShell 试用示例：

```powershell
$project = Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/v1/projects/with-first-task' `
  -ContentType 'application/json' `
  -Body '{"name":"MySQL 学习","description":"第三周练习","firstTaskDescription":"完成分页接口"}'

Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tasks?page=1&size=10&keyword=分页'
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/v1/projects/$($project.data.project.id)"
```

评论与状态操作示例：

```powershell
$task = Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/v1/tasks' `
  -ContentType 'application/json' `
  -Body '{"description":"检查综合项目流程"}'
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/tasks/$($task.data.id)/comments" `
  -ContentType 'application/json' `
  -Body '{"content":"已经完成初步检查"}'
Invoke-RestMethod -Method Patch `
  -Uri "http://localhost:8080/api/v1/tasks/$($task.data.id)/status" `
  -ContentType 'application/json' `
  -Body '{"completed":true}'
```

## 验证

```powershell
mvn clean verify
```

测试使用 H2 的 MySQL 兼容模式，覆盖 Project/Task/Comment CRUD、分页与关联查询、状态修改、参数错误、级联删除及事务回滚。测试数据库配置位于 `src/test/resources`，不会连接或清空本机 MySQL 数据库。SQL 技能审查记录见 [docs/week-04-skill-review.md](docs/week-04-skill-review.md)。

## 分支与本周产出

第三周个人分支与标签：`feature/week-03-mysql`、`week-03`。第四周个人分支与标签：`feature/week-04-comments`、`week-04`。团队 PR 审查与合并由团队流程完成。脚手架基线见 [docs/scaffold-v1.md](docs/scaffold-v1.md)。
