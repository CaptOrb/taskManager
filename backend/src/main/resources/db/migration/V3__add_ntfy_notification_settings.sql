ALTER TABLE `user`
  ADD COLUMN ntfy_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN ntfy_topic VARCHAR(128) NULL;

ALTER TABLE task
  MODIFY COLUMN due_date DATETIME NULL,
  MODIFY COLUMN created_date DATETIME NOT NULL,
  ADD COLUMN reminder_sent_at DATETIME NULL,
  ADD INDEX idx_task_reminder_lookup (reminder_sent_at, due_date, status);