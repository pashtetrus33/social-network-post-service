-- Создание таблицы posts в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    time TIMESTAMP,
    time_changed TIMESTAMP,
    author_id UUID,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20),
    post_text TEXT NOT NULL,
    is_blocked BOOLEAN,
    is_deleted BOOLEAN,
    comments_count INT,
    like_amount INT,
    my_like BOOLEAN NOT NULL,
    image_path VARCHAR(500),
    publish_date TIMESTAMP NOT NULL
    );

-- Создание таблицы comments в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_type VARCHAR(20),
    time TIMESTAMP NOT NULL,
    time_changed TIMESTAMP NOT NULL,
    author_id UUID NOT NULL,
    parent_id UUID,
    comment_text TEXT NOT NULL,
    post_id UUID NOT NULL,
    is_blocked BOOLEAN NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    like_amount INT,
    my_like BOOLEAN,
    comments_count INT,
    image_path VARCHAR(500),
    FOREIGN KEY (post_id) REFERENCES schema_post.posts(id)
    );

-- Создание таблицы likes в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_id UUID NOT NULL,
    post_id UUID,
    comment_id UUID,
    type VARCHAR(50),
    reaction_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES schema_post.posts(id),
    FOREIGN KEY (comment_id) REFERENCES schema_post.comments(id)
    );


-- Создание таблицы post_tags в схеме schema_post
CREATE TABLE IF NOT EXISTS schema_post.post_tags (post_id UUID NOT NULL,tag VARCHAR(255) NOT NULL,
    FOREIGN KEY (post_id) REFERENCES schema_post.posts(id)
    );