package com.eightsnpc.todo;

import java.util.Scanner;

public final class TodoCli {
    private final TaskService tasks = new InMemoryTaskService();

    public static void main(String[] args) {
        new TodoCli().run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running && scanner.hasNextLine()) {
                printMenu();
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> addTask(scanner);
                    case "2" -> listTasks();
                    case "3" -> completeTask(scanner);
                    case "4" -> deleteTask(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("选项无效，请重试。");
                }
            }
        }
        System.out.println("任务清单已退出。");
    }

    private void printMenu() {
        System.out.println("\n=== 命令行任务清单 ===");
        System.out.println("1. 新增任务");
        System.out.println("2. 查看任务");
        System.out.println("3. 完成任务");
        System.out.println("4. 删除任务");
        System.out.println("0. 退出");
        System.out.print("请选择：");
    }

    private void addTask(Scanner scanner) {
        System.out.print("任务内容：");
        String description = scanner.nextLine();
        try {
            Task task = tasks.add(description);
            System.out.println("已新增任务，编号：" + task.getId());
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void listTasks() {
        if (tasks.list().isEmpty()) {
            System.out.println("目前没有任务。");
            return;
        }

        for (Task task : tasks.list()) {
            String status = task.isCompleted() ? "已完成" : "未完成";
            System.out.printf("%d. [%s] %s%n", task.getId(), status, task.getDescription());
        }
    }

    private void completeTask(Scanner scanner) {
        Long id = readId(scanner, "要完成的任务编号：");
        if (id != null) {
            System.out.println(tasks.complete(id) ? "任务已完成。" : "没有找到这个编号。");
        }
    }

    private void deleteTask(Scanner scanner) {
        Long id = readId(scanner, "要删除的任务编号：");
        if (id != null) {
            System.out.println(tasks.delete(id) ? "任务已删除。" : "没有找到这个编号。");
        }
    }

    private Long readId(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException exception) {
            System.out.println("编号必须是数字。");
            return null;
        }
    }
}
