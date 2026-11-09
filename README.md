# 小事清单 · 任务管理

这是一个带用户操作页面的任务清单和个人账本项目。页面可管理项目、任务和评论，也可记录收支并查看分类统计；Java 21、Spring Boot 3、MyBatis-Plus 与 MySQL 在后台提供数据和接口。

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

## 使用任务页面

服务启动后打开任务工作台：

- 用户页面：<http://localhost:8080/>

页面可以创建和筛选任务、归入项目、切换完成状态、编辑或删除任务，并查看、添加和维护评论。所有操作通过后台 API 保存到 MySQL。

## 使用个人账本

服务启动后打开 <http://localhost:8080/finance.html>，或从任务清单左侧的“账目与分类统计”入口进入。页面可记录收入和支出，编辑、删除账目，按日期、类型和分类筛选，并查看收入、支出、结余及分类统计。支出分类含饮食、生活缴费、娱乐、网络应用、交通、住房、购物、日用、其他；收入分类含工资、红包、奖金、兼职、其他。

本模块新增两张表和 6 个接口：`POST /api/v1/ledger/entries`、`GET /api/v1/ledger/entries`、`GET /api/v1/ledger/entries/{id}`、`PUT /api/v1/ledger/entries/{id}`、`DELETE /api/v1/ledger/entries/{id}`、`GET /api/v1/ledger/summary`。完整设计、校验规则和验证结果见 [个人记账与分类统计系统报告](docs/personal-finance-system-report.md)。

快速新增一笔支出：

```powershell
Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/v1/ledger/entries' `
  -ContentType 'application/json' `
  -Body '{"categoryId":5,"amount":35.80,"occurredOn":"2026-11-09","note":"午餐"}'
```

## 开发调试入口

以下地址面向开发调试，普通用户使用页面时无需打开：

- Swagger UI：<http://localhost:8080/developer/swagger-ui.html>
- OpenAPI JSON：<http://localhost:8080/developer/openapi.json>
- 健康检查：<http://localhost:8080/api/health>

### API 路由参考

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

测试使用 H2 的 MySQL 兼容模式，覆盖 Project/Task/Comment 和记账接口 CRUD、分页与关联查询、汇总、参数错误、级联删除及事务回滚。测试数据库配置位于 `src/test/resources`，不会连接或清空本机 MySQL 数据库。SQL 技能审查记录见 [docs/week-04-skill-review.md](docs/week-04-skill-review.md) 和 [记账系统报告](docs/personal-finance-system-report.md)。

## 分支与本周产出

第三周个人分支与标签：`feature/week-03-mysql`、`week-03`。第四周个人分支与标签：`feature/week-04-comments`、`week-04`。团队 PR 审查与合并由团队流程完成。脚手架基线见 [docs/scaffold-v1.md](docs/scaffold-v1.md)。
