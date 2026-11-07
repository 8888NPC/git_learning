# Week 00 环境记录

记录日期：2026-11-07

## 开发环境

| 项目 | 版本或位置 |
| --- | --- |
| 操作系统 | Windows 11，x64 |
| Java | Eclipse Temurin 21.0.13 LTS |
| Maven | Apache Maven 3.10.0 |
| Git | 2.56.0.windows.4 |
| Git 分支 | `main` |

VS Code 集成终端通过 `.vscode/settings.json` 设置 `JAVA_HOME` 和 Maven 路径。

## 验收命令与结果

| 命令 | 结果 |
| --- | --- |
| `java -version` | OpenJDK 21.0.13，运行正常 |
| `mvn -version` | Maven 3.10.0，使用 Java 21.0.13 |
| `mvn clean package` | `BUILD SUCCESS` |
| Git 提交记录 | 已检查，当前 `main` 与 `origin/main` 同步 |

构建产物：`target/java-task-list-1.0-SNAPSHOT.jar`

本次 Maven 构建未发现测试源文件，提示 `No tests to run.`。
