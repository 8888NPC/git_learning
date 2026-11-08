# Project/Task ER 图

```mermaid
erDiagram
    PROJECTS ||--o{ TASKS : contains
    PROJECTS {
        BIGINT id PK
        VARCHAR name
        VARCHAR description
    }
    TASKS {
        BIGINT id PK
        BIGINT project_id FK "nullable for standalone tasks"
        VARCHAR description
        BOOLEAN completed
    }
```

一个项目可以包含零个或多个任务。`tasks.project_id` 可空，支持不属于项目的独立任务；删除项目时，数据库通过外键级联删除其任务。
