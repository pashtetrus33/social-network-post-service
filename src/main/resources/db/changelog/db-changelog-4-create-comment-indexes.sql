-- Индекс для ускорения поиска по author_id
CREATE INDEX idx_comment_author ON schema_post.comments (author_id);

-- Индекс для ускорения поиска по post_id
CREATE INDEX idx_comment_post ON schema_post.comments (post_id);

-- Составной индекс для author_id и post_id
CREATE INDEX idx_comment_author_post ON schema_post.comments (author_id, post_id);

-- Индекс для parent_id
CREATE INDEX idx_comment_parent ON schema_post.comments (parent_id);

-- Обычный индекс на is_blocked и is_deleted неэффективен, лучше составной индекс:
CREATE INDEX idx_comment_status ON schema_post.comments (is_blocked, is_deleted);