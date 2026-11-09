CREATE TABLE IF NOT EXISTS projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NULL,
    description VARCHAR(200) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    KEY idx_tasks_project_id (project_id),
    KEY idx_tasks_completed (completed),
    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_comments_task_id_id (task_id, id),
    CONSTRAINT fk_comments_task
        FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS finance_categories (
    id BIGINT NOT NULL,
    name VARCHAR(40) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_finance_categories_type_name (entry_type, name),
    CONSTRAINT chk_finance_categories_type CHECK (entry_type IN ('INCOME', 'EXPENSE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    occurred_on DATE NOT NULL,
    note VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_ledger_entries_date_id (occurred_on, id),
    KEY idx_ledger_entries_category_date_id (category_id, occurred_on, id),
    CONSTRAINT chk_ledger_entries_amount CHECK (amount > 0),
    CONSTRAINT fk_ledger_entries_category
        FOREIGN KEY (category_id) REFERENCES finance_categories (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO finance_categories (id, name, entry_type) VALUES
    (1, '工资', 'INCOME'),
    (2, '奖金', 'INCOME'),
    (3, '兼职', 'INCOME'),
    (4, '其他', 'INCOME'),
    (5, '饮食', 'EXPENSE'),
    (6, '交通', 'EXPENSE'),
    (7, '住房', 'EXPENSE'),
    (8, '购物', 'EXPENSE'),
    (9, '日用', 'EXPENSE'),
    (10, '生活缴费', 'EXPENSE'),
    (11, '娱乐', 'EXPENSE'),
    (12, '网络应用', 'EXPENSE'),
    (13, '其他', 'EXPENSE'),
    (14, '红包', 'INCOME')
ON DUPLICATE KEY UPDATE name = VALUES(name), entry_type = VALUES(entry_type);
