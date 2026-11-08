# Project/Task REST 接口

## 约定

- 基础路径：`/api/v1`，请求和响应使用 JSON。
- 成功和错误响应使用统一的 `ApiResponse`：`{ "success": true, "data": {}, "error": null }`。
- 任务内容为 1–200 个字符；项目名称为 1–100 个字符；项目描述可选，最多 500 个字符。
- `page` 从 1 开始，`size` 范围为 1–100。分页结果包含 `current`、`size`、`total` 和 `records`。

## 任务接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/v1/tasks` | 新增独立任务或关联到项目的任务 |
| `GET` | `/api/v1/tasks/{id}` | 查询任务详情 |
| `GET` | `/api/v1/tasks` | 搜索并分页查询任务 |
| `PUT` | `/api/v1/tasks/{id}` | 修改任务内容和完成状态 |
| `DELETE` | `/api/v1/tasks/{id}` | 删除任务 |

新增任务请求示例：

```json
{
  "projectId": 1,
  "description": "复习 MyBatis-Plus"
}
```

任务列表支持 `page`、`size`、`projectId`、`completed`、`keyword` 参数，均可选；默认 `page=1`、`size=10`。

## 项目接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/v1/projects` | 新增项目 |
| `POST` | `/api/v1/projects/with-first-task` | 在同一事务中创建项目和首个任务 |
| `GET` | `/api/v1/projects/{id}` | 查询项目详情 |
| `GET` | `/api/v1/projects` | 搜索并分页查询项目 |
| `PUT` | `/api/v1/projects/{id}` | 修改项目 |
| `DELETE` | `/api/v1/projects/{id}` | 删除项目并级联删除其任务 |

新增项目请求示例：

```json
{
  "name": "MySQL 学习",
  "description": "第三周练习"
}
```

同时创建项目和首个任务：

```json
{
  "name": "MySQL 学习",
  "description": "第三周练习",
  "firstTaskDescription": "完成分页接口"
}
```

项目列表支持 `page`、`size`、`keyword` 参数，关键字匹配项目名称；分页默认值与任务列表相同。

## 状态码和错误

| HTTP 状态码 | 错误码示例 | 含义 |
| --- | --- | --- |
| `400 Bad Request` | `BAD_REQUEST` | 请求内容、页码或页大小不合法 |
| `404 Not Found` | `TASK_NOT_FOUND`、`PROJECT_NOT_FOUND` | 请求的资源不存在 |
| `405 Method Not Allowed` | `METHOD_NOT_ALLOWED` | 不支持该 HTTP 方法 |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | 请求不是 JSON 格式 |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | 服务端发生未预期错误 |

健康检查：`GET /api/health`。交互式接口文档：`/swagger-ui.html`。
