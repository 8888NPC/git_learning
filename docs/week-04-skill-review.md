# Week 04 SQL 技能审查记录

## 使用记录

- 使用技能：`sql-code-review`
- 审查范围：`src/main/resources/db/init.sql`、测试用 `src/test/resources/schema.sql`，以及 MyBatis-Plus 条件查询。
- 目的：检查关联约束、索引、输入参数处理和 schema 初始化方式。

## 审查结果与采纳项

1. DDL 是固定建表语句，没有把用户输入拼进 SQL；MyBatis-Plus 查询条件使用 LambdaQueryWrapper 绑定参数，保留这一实现。
2. `tasks.project_id` 有索引并带项目外键；保留外键的 `ON DELETE CASCADE`，确保删除项目时清理任务。
3. 新增 `comments.task_id` 非空外键，并建立 `(task_id, id)` 复合索引，用于按任务读取评论并按 ID 稳定排序；删除任务时评论级联删除。
4. 任务描述和评论内容都是普通文本。现有关键字搜索采用包含匹配，B-tree 索引无法有效加速 `%keyword%`；当前学习项目数据规模较小，因此保留包含匹配语义，不添加误导性的文本索引。
5. `init.sql` 是可重复执行的空库建表脚本；新增 Comment 表采用 `CREATE TABLE IF NOT EXISTS`，不会清空现有数据。未来修改既有列或索引时应增加版本化迁移脚本。

## 人工复核结论

技能建议按实际访问路径取舍：评论列表固定按 `task_id` 查询并以 `id` 排序，因此复合索引被采纳；对任意子串搜索增加普通 B-tree 索引无效，未采纳。没有发现 SQL 注入或引用完整性缺口。

| 维度 | 评分 | 依据 |
| --- | ---: | --- |
| 安全性 | 9/10 | 静态 DDL、参数绑定、非空外键 |
| 查询性能 | 8/10 | 关联列有索引；小规模 contains 搜索保留原语义 |
| 可维护性 | 9/10 | 统一命名、外键和索引有明确用途 |
| 结构质量 | 9/10 | Project/Task/Comment 分表表达一对多关系 |

## 验证

- `mvn clean verify`：28 项测试通过，0 失败、0 错误、0 跳过。
- 使用全新的 MySQL 8.4 测试库启动应用，自动创建 `projects`、`tasks`、`comments` 三张表。
- 实际调用 Project→Task、Task→Comment 关联接口，验证评论 CRUD、任务状态过滤和级联删除。
- 用测试触发器让评论插入失败；事务接口返回 HTTP 500，创建前后的三个表行数完全一致，确认任务插入已回滚。测试库随后删除。
