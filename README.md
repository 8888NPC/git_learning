# 任务清单 REST API

一个基于 Java 21 和 Spring Boot 3 的内存版任务清单项目，提供健康检查和任务增删改查接口。

## 环境要求

- Java 21
- Maven 3.10 或更高版本

环境记录见 [environment-record.md](environment-record.md)，接口详细约定见 [rest-api-draft.md](rest-api-draft.md)。

## 启动项目

在项目根目录运行：

```powershell
mvn spring-boot:run
```

也可以先打包，再运行 JAR：

```powershell
mvn clean package
java -jar target/java-task-list-1.0-SNAPSHOT.jar
```

启动成功后，服务监听 `http://localhost:8080`。

## 统一响应

所有成功和错误响应都使用统一结构：

```json
{
  "success": true,
  "data": { "status": "UP" },
  "error": null
}
```

错误示例：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "TASK_NOT_FOUND",
    "message": "未找到编号为 9 的任务"
  }
}
```

健康检查：`GET http://localhost:8080/api/health`

交互式接口文档：[Swagger UI](http://localhost:8080/swagger-ui.html)。OpenAPI JSON：`http://localhost:8080/v3/api-docs`。

## Task API

所有任务接口的基础路径为 `/api/v1/tasks`，请求和响应使用 JSON。

| 方法 | 路径 | 说明 | 成功状态码 |
| --- | --- | --- | --- |
| `POST` | `/api/v1/tasks` | 新增任务 | `201 Created` |
| `GET` | `/api/v1/tasks/{id}` | 查询任务详情 | `200 OK` |
| `GET` | `/api/v1/tasks` | 查询任务列表 | `200 OK` |
| `PUT` | `/api/v1/tasks/{id}` | 修改任务 | `200 OK` |
| `DELETE` | `/api/v1/tasks/{id}` | 删除任务 | `200 OK` |

### PowerShell 示例

启动服务后，在另一个 PowerShell 终端按顺序执行以下命令。

新增任务：

```powershell
$task = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/tasks" -ContentType "application/json" -Body '{"description":"复习 Spring Boot"}'
$task.data
```

查询列表和详情：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/tasks"
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/tasks/$($task.data.id)"
```

修改任务：

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/v1/tasks/$($task.data.id)" -ContentType "application/json" -Body '{"description":"完成 Spring Boot 练习","completed":true}'
```

删除任务：

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/v1/tasks/$($task.data.id)"
```

任务数据保存在内存中，应用重启后会清空。
