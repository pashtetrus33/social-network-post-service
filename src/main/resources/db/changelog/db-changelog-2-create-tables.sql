-- Создание таблицы posts в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    time TIMESTAMP NOT NULL,
    time_changed TIMESTAMP,
    author_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    post_text TEXT NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    comments_count INT NOT NULL DEFAULT 0,
    reactions_count INT NOT NULL DEFAULT 0,
    image_path VARCHAR(500),
    publish_date TIMESTAMP NOT NULL
    );

-- Создание таблицы comments в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_type VARCHAR(20),
    time TIMESTAMP NOT NULL,
    time_changed TIMESTAMP,
    author_id UUID NOT NULL,
    parent_id UUID,
    comment_text TEXT NOT NULL,
    post_id UUID NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    like_amount INT NOT NULL DEFAULT 0,
    comments_count INT NOT NULL DEFAULT 0,
    image_path VARCHAR(500),
    FOREIGN KEY (post_id) REFERENCES schema_post.posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES schema_post.comments(id) ON DELETE CASCADE
    );

-- Создание таблицы reactions в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.reactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_id UUID NOT NULL,  -- Идентификатор автора реакции
    post_id UUID,  -- Идентификатор поста
    comment_id UUID,  -- Идентификатор комментария (если применимо)
    type VARCHAR(50) NOT NULL,  -- Тип реакции (например, "like", "dislike")
    reaction_type VARCHAR(50) NOT NULL,  -- Подтип реакции (например, "heart", "thumbs_up")
    reaction_count INT DEFAULT 1,  -- Количество реакций одного типа
    created_at TIMESTAMP,  -- Дата создания
    FOREIGN KEY (post_id) REFERENCES schema_post.posts(id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES schema_post.comments(id) ON DELETE CASCADE
    );


-- Создание таблицы post_tags в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.post_tags (
    post_id UUID NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (post_id, tag),
    FOREIGN KEY (post_id) REFERENCES schema_post.posts(id) ON DELETE CASCADE
    );