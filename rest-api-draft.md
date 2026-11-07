# 任务清单 REST 接口草稿

## 约定

- 基础路径：`/api/v1`
- 数据格式：请求和响应均使用 `application/json; charset=utf-8`
- 任务字段：`id` 为服务端生成的整数；`description` 为 1–200 个字符的非空字符串；`completed` 为布尔值。
- 未找到指定任务时返回 `404 Not Found`；请求内容不合法时返回 `400 Bad Request`。

任务示例：

```json
{
  "id": 1,
  "description": "复习 ArrayList",
  "completed": false
}
```

## 1. 新增任务

`POST /api/v1/tasks`

请求：

```json
{
  "description": "复习 ArrayList"
}
```

成功返回 `201 Created`，响应体为新建的任务：

```json
{
  "id": 1,
  "description": "复习 ArrayList",
  "completed": false
}
```

如果 `description` 缺失、为空或只有空白字符，返回 `400 Bad Request`。

## 2. 查询任务

`GET /api/v1/tasks`

查询全部任务。成功返回 `200 OK`，响应体为任务数组；没有任务时返回空数组：

```json
[
  {
    "id": 1,
    "description": "复习 ArrayList",
    "completed": false
  }
]
```

## 3. 修改任务

`PUT /api/v1/tasks/{id}`

完整替换指定任务的可修改字段；`description` 和 `completed` 都必须提供。请求：

```json
{
  "description": "复习 Java 集合",
  "completed": true
}
```

成功返回 `200 OK`，响应体为修改后的任务。`{id}` 不存在时返回 `404 Not Found`；字段缺失或不合法时返回 `400 Bad Request`。

## 4. 删除任务

`DELETE /api/v1/tasks/{id}`

删除指定任务。成功返回 `204 No Content`，响应体为空。`{id}` 不存在时返回 `404 Not Found`。

## 错误响应格式

发生错误时使用统一的 JSON 格式，例如：

```json
{
  "code": "TASK_NOT_FOUND",
  "message": "未找到编号为 9 的任务"
}
```

| HTTP 状态码 | 含义 |
| --- | --- |
| `400 Bad Request` | 请求字段缺失或格式不合法 |
| `404 Not Found` | 指定任务不存在 |
| `415 Unsupported Media Type` | 请求不是 JSON 格式 |
| `500 Internal Server Error` | 服务端发生未预期错误 |
