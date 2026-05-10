-- =============================================
-- 社交分享平台 - 数据库初始化脚本
-- =============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS social_platform;
CREATE DATABASE social_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE social_platform;

-- 2. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `role` VARCHAR(20) DEFAULT 'ROLE_USER' COMMENT '角色',
    `avatar` VARCHAR(500) DEFAULT 'https://picsum.photos/200/200' COMMENT '头像',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 分享表
DROP TABLE IF EXISTS `share`;
CREATE TABLE `share` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分享ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '内容',
    `visibility` ENUM('PUBLIC', 'LOGIN_ONLY', 'PRIVATE') DEFAULT 'PUBLIC' COMMENT '可见性',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享表';

-- 4. 分享图片表
DROP TABLE IF EXISTS `share_image`;
CREATE TABLE `share_image` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    `share_id` BIGINT NOT NULL COMMENT '分享ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    FOREIGN KEY (`share_id`) REFERENCES `share`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享图片表';

-- 5. 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    `share_id` BIGINT NOT NULL COMMENT '分享ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID',
    `content` TEXT NOT NULL COMMENT '内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`share_id`) REFERENCES `share`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 6. 点赞记录表
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `share_id` BIGINT NOT NULL COMMENT '分享ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`share_id`) REFERENCES `share`(`id`) ON DELETE CASCADE,
    UNIQUE KEY uk_user_share (`user_id`, `share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 7. 插入测试数据
INSERT INTO `user` (`username`, `password_hash`, `role`) VALUES
('test', '$2a$10$N.Zq9eXkOq/n3Gz5Y5X5Y5X5Y5X5Y5X5Y5X5Y5X5Y5X5Y5X5Y5', 'ROLE_USER'),
('admin', '$2a$10$N.Zq9eXkOq/n3Gz5Y5X5Y5X5Y5X5Y5X5Y5X5Y5X5Y5X5Y5X5Y5', 'ROLE_ADMIN');

INSERT INTO `share` (`user_id`, `content`, `visibility`) VALUES
(1, '欢迎来到社交分享平台！🎉 这里可以分享你的想法和图片。', 'PUBLIC'),
(1, '今天天气真好，适合出去玩！☀️', 'PUBLIC'),
(2, '管理员公告：请遵守社区规范，文明发言。', 'PUBLIC');

-- 8. 验证
SELECT '数据库创建成功！' AS message;
SELECT COUNT(*) AS user_count FROM `user`;
SELECT COUNT(*) AS share_count FROM `share`;