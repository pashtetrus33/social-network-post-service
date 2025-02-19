-- Индекс для ускорения поиска по author_id
CREATE INDEX idx_post_author ON schema_post.posts (author_id);

-- Индекс для ускорения сортировки и поиска по publish_date
CREATE INDEX idx_post_publish_date ON schema_post.posts (publish_date DESC);

-- Составной индекс для author_id и publish_date (заменяет два отдельных индекса, если часто используются вместе)
CREATE INDEX idx_post_author_publish ON schema_post.posts (author_id, publish_date DESC);

-- Составной индекс для isBlocked и isDeleted (лучше, чем два отдельных индекса)
CREATE INDEX idx_post_status ON schema_post.posts (is_blocked, is_deleted);