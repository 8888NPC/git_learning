# scaffold-v1 基线说明

## 技术基线

- Java 21
- Spring Boot 3.5.x
- Maven
- Spring MVC、Jakarta Bean Validation、Springdoc OpenAPI
- JUnit 5、Spring MockMvc

## 目录职责

| 位置 | 职责 |
| --- | --- |
| `com.eightsnpc.todo` | 领域对象、服务接口、服务实现和应用入口 |
| `com.eightsnpc.todo.api` | REST Controller 与 OpenAPI 配置，只处理 HTTP 映射和 DTO 转换 |
| `com.eightsnpc.todo.api.dto` | 请求 DTO、响应 DTO、统一响应包装 |
| `com.eightsnpc.todo.api.error` | 业务异常、错误模型和全局异常处理 |
| `src/main/resources/application.yml` | 应用名称、端口等外部配置 |
| `src/test/java` | JUnit 5 与 MockMvc 接口测试 |

## 请求处理约定

1. Controller 接收请求、执行 DTO 校验并调用 Service，不放主要业务逻辑。
2. Service 接口声明业务操作，具体实现负责规则和数据操作。
3. 成功与错误响应使用 `ApiResponse`；HTTP 状态码仍反映成功、请求错误或服务错误。
4. DTO 使用 Jakarta Bean Validation 注解声明字段约束；全局异常处理将校验错误转换为统一错误响应。
5. OpenAPI 由 Springdoc 从 Controller 和 DTO 生成，Swagger UI 用于查看和试用接口。
6. Controller 行为通过 MockMvc 覆盖正常请求、参数错误和资源不存在等情况。

## 常用命令

```powershell
mvn spring-boot:run
mvn test
mvn clean verify
```

交互式接口文档地址：`http://localhost:8080/swagger-ui.html`。

## 团队分支约定建议

- `main` 保存通过 `mvn clean verify` 的基线代码。
- 功能分支使用 `feature/<简短主题>`，修复分支使用 `fix/<简短主题>`。
- 团队仓库可将 `main` 设为受保护分支，禁止强制推送和直接删除，并要求通过 Pull Request 合并。
- 团队仓库的实际保护规则需由该仓库管理员配置；本地文档只记录建议，不代表远程设置已生效。
