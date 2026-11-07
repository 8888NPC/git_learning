# 任务清单 REST 接口草稿

## 约定

- 基础路径：`/api/v1`
- 请求格式：`application/json; charset=utf-8`
- 任务字段：`id` 为服务端生成的整数；`description` 为 1–200 个字符的非空字符串；`completed` 为布尔值。
- 成功和错误响应都使用统一的 `ApiResponse` 结构，HTTP 状态码仍表示请求结果类别。

统一成功响应：

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

统一错误响应：

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

## 1. 新增任务

`POST /api/v1/tasks`

请求：

```json
{
  "description": "复习 ArrayList"
}
```

成功返回 `201 Created`，响应示例：

```json
{
  "success": true,
  "data": {
    "id": 1,
    "description": "复习 ArrayList",
    "completed": false
  },
  "error": null
}
```

如果 `description` 缺失、为空或超过 200 个字符，返回 `400 Bad Request`。

## 2. 查询任务列表

`GET /api/v1/tasks`

成功返回 `200 OK`，`data` 是任务数组；没有任务时是空数组：

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "复习 ArrayList",
      "completed": false
    }
  ],
  "error": null
}
```

## 3. 查询任务详情

`GET /api/v1/tasks/{id}`

成功返回 `200 OK`，`data` 是指定任务。任务不存在时返回 `404 Not Found`。

## 4. 修改任务

`PUT /api/v1/tasks/{id}`

完整替换指定任务的可修改字段；`description` 和 `completed` 都必须提供。请求：

```json
{
  "description": "复习 Java 集合",
  "completed": true
}
```

成功返回 `200 OK`，`data` 是修改后的任务。任务不存在时返回 `404 Not Found`；字段缺失或不合法时返回 `400 Bad Request`。

## 5. 删除任务

`DELETE /api/v1/tasks/{id}`

成功返回 `200 OK`：

```json
{
  "success": true,
  "data": null,
  "error": null
}
```

任务不存在时返回 `404 Not Found`。

## 健康检查

`GET /api/health`

服务正常时返回 `200 OK`，`data.status` 为 `UP`。

## 错误状态码

| HTTP 状态码 | 错误码示例 | 含义 |
| --- | --- | --- |
| `400 Bad Request` | `BAD_REQUEST` | 请求字段缺失或格式不合法 |
| `404 Not Found` | `TASK_NOT_FOUND` | 指定任务不存在 |
| `405 Method Not Allowed` | `METHOD_NOT_ALLOWED` | 请求方法不支持 |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | 请求不是 JSON 格式 |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | 服务端发生未预期错误 |
