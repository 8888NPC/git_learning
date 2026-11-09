CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    description VARCHAR(200) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id)
        REFERENCES projects (id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id)
        REFERENCES tasks (id) ON DELETE CASCADE
);

CREATE TABLE finance_categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    CONSTRAINT uk_finance_categories_type_name UNIQUE (entry_type, name),
    CONSTRAINT chk_finance_categories_type CHECK (entry_type IN ('INCOME', 'EXPENSE'))
);

CREATE TABLE ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    occurred_on DATE NOT NULL,
    note VARCHAR(255),
    CONSTRAINT chk_ledger_entries_amount CHECK (amount > 0),
    CONSTRAINT fk_ledger_entries_category FOREIGN KEY (category_id)
        REFERENCES finance_categories (id) ON DELETE RESTRICT
);

CREATE INDEX idx_ledger_entries_date_id ON ledger_entries (occurred_on, id);
CREATE INDEX idx_ledger_entries_category_date_id ON ledger_entries (category_id, occurred_on, id);

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
    (14, '红包', 'INCOME');
