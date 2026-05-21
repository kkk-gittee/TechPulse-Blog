-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    email VARCHAR(100),
    bio VARCHAR(500),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    use_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE IF NOT EXISTS article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    summary VARCHAR(500),
    cover_image VARCHAR(255),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    status INT DEFAULT 1,
    category_id BIGINT,
    tags VARCHAR(200),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status_create (status, create_time),
    INDEX idx_status_user (status, user_id),
    FULLTEXT INDEX idx_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    INDEX idx_article_id (article_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    reply_user_id BIGINT,
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_article_parent (article_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 关注表
CREATE TABLE IF NOT EXISTS follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    follow_user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_follow (user_id, follow_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_follow_user_id (follow_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收藏表
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_article (user_id, article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 浏览历史表
CREATE TABLE IF NOT EXISTS browse_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id),
    INDEX idx_create_time (create_time),
    INDEX idx_user_article (user_id, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认分类
INSERT INTO category (name, description) VALUES
('Java', 'Java技术相关'),
('Spring Boot', 'Spring Boot框架'),
('数据库', '数据库技术'),
('前端', '前端技术'),
('其他', '其他技术');

-- 插入默认标签
INSERT INTO tag (name, use_count) VALUES
('Java', 0),
('Spring', 0),
('MySQL', 0),
('Redis', 0),
('Docker', 0),
('Linux', 0),
('Git', 0),
('设计模式', 0),
('微服务', 0),
('并发编程', 0);
