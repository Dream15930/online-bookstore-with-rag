-- ============================================
-- 网上图书销售系统 - 完整数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS bookstore_new DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookstore_new;

-- 删除旧表（按依赖顺序）
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `order_items_backup`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `books`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT 'super_admin | admin | member',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '默认地址（兼容旧版）',
  `status` INT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 收货地址表
-- ============================================
CREATE TABLE `addresses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `address` VARCHAR(500) NOT NULL,
  `is_default` INT NOT NULL DEFAULT 0 COMMENT '1=默认 0=非默认',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 图书分类表
-- ============================================
CREATE TABLE `categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 图书表
-- ============================================
CREATE TABLE `books` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `author` VARCHAR(100) NOT NULL,
  `isbn` VARCHAR(20) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `category_id` BIGINT DEFAULT NULL,
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL，为空则使用CSS生成封面',
  `description` TEXT,
  `status` INT NOT NULL DEFAULT 1 COMMENT '1=上架 0=下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 购物车表
-- ============================================
CREATE TABLE `cart_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `book_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 订单表
-- ============================================
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `order_no` VARCHAR(30) NOT NULL,
  `address` VARCHAR(500) NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending | paid | shipped | completed',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 订单明细表
-- ============================================
CREATE TABLE `order_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `book_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 评价表
-- ============================================
CREATE TABLE `reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `rating` INT NOT NULL COMMENT '1-5星',
  `admin_reply` TEXT NULL COMMENT '管理员回复',
  `replied_at` DATETIME NULL COMMENT '回复时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 初始数据
-- ============================================

-- 管理员: admin / 123456   会员: testuser / 123456
INSERT INTO `users` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'super_admin', 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'member', 1);

-- 分类
INSERT INTO `categories` (`id`, `name`) VALUES
(1, '计算机'),
(2, '文学小说'),
(3, '历史'),
(4, '经济管理'),
(5, '科普读物');

-- 图书（cover_url 为空则前端用CSS渐变封面）
INSERT INTO `books` (`title`, `author`, `isbn`, `price`, `stock`, `category_id`, `cover_url`, `description`, `status`) VALUES
('Java编程思想', 'Bruce Eckel', '9787111213826', 79.00, 50, 1, '', 'Java程序设计语言的权威指南，深入理解Java面向对象编程思想。', 1),
('深入理解Java虚拟机', '周志明', '9787111421900', 76.00, 30, 1, '', '详解JVM内存管理、类加载、垃圾回收等核心技术。', 1),
('Spring实战', 'Craig Walls', '9787115417305', 89.00, 20, 1, '', 'Spring框架权威指南，涵盖Spring Boot与Spring Cloud。', 1),
('活着', '余华', '9787530215531', 35.00, 100, 2, '', '讲述了农村人福贵悲惨的人生遭遇，展现生命的韧性。', 1),
('三体', '刘慈欣', '9787536692930', 62.00, 80, 2, '', '中国科幻文学的里程碑之作，获雨果奖最佳长篇小说。', 1),
('人类简史', '尤瓦尔·赫拉利', '9787508647357', 49.80, 60, 3, '', '从认知革命到科学革命，重述整个人类的历史。', 1),
('万历十五年', '黄仁宇', '9787108009821', 26.00, 45, 3, '', '以1587年万历十五年为切入点，剖析大明帝国衰落的深层原因。', 1),
('经济学原理', '曼昆', '9787301150894', 88.00, 25, 4, '', '全球最受欢迎的经济学入门教材，通俗易懂。', 1),
('时间简史', '史蒂芬·霍金', '9787535732309', 31.00, 40, 5, '', '霍金科普经典之作，探讨宇宙起源、黑洞与时间本质。', 1),
('重构：改善既有代码的设计', 'Martin Fowler', '9787115543097', 99.00, 15, 1, '', '经典软件开发方法论，教你如何在不改变外部行为的前提下优化代码。', 1);
