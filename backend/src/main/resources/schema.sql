-- 建表语句 - 慢性病用药智能提醒系统
-- 数据库：medical_system

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_name` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `realname` VARCHAR(50),
    `gender` INT,
    `birthday` DATE,
    `phone` VARCHAR(20),
    `role` INT DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_name` (`user_name`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 电子病历表
-- 【修改记录】2026-06-28: 新增gender, age, past_history, present_history, chief_complaint, treatment字段
CREATE TABLE IF NOT EXISTS `medicalrecord` (
    `record_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `disease_name` VARCHAR(100) NOT NULL,
    `diagnosis` TEXT,
    `prescription` TEXT,
    `doctor` VARCHAR(50),
    `hospital` VARCHAR(100),
    `record_date` DATE,
    `gender` VARCHAR(10) COMMENT '性别',
    `age` INT COMMENT '年龄',
    `past_history` TEXT COMMENT '既往病史',
    `present_history` TEXT COMMENT '现病史',
    `chief_complaint` TEXT COMMENT '主诉',
    `treatment` TEXT COMMENT '处理意见',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 药品信息表
CREATE TABLE IF NOT EXISTS `medicine` (
    `medicine_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `medicine_name` VARCHAR(100) NOT NULL,
    `specification` VARCHAR(100),
    `dosage_form` VARCHAR(50),
    `manufacturer` VARCHAR(100),
    `indication` TEXT,
    `contraindication` TEXT,
    `precautions` TEXT,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_medicine_name` (`medicine_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用药计划表
CREATE TABLE IF NOT EXISTS `medicationplan` (
    `plan_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `record_id` BIGINT,
    `medicine_id` BIGINT,
    `dosage` VARCHAR(50) COMMENT '每次用量',
    `frequency` VARCHAR(50) COMMENT '用药频率',
    `times_per_day` INT COMMENT '每日次数',
    `interval_hours` INT COMMENT '间隔小时数',
    `start_date` DATE,
    `end_date` DATE,
    `purpose` VARCHAR(255) COMMENT '用药目的',
    `status` INT DEFAULT 1 COMMENT '1:进行中 0:已停用 2:已完成',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 每日服药记录表
CREATE TABLE IF NOT EXISTS `medicationrecord` (
    `take_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `plan_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `medicine_id` BIGINT,
    `scheduled_time` DATETIME NOT NULL COMMENT '计划服药时间',
    `take_time` DATETIME COMMENT '实际服药时间',
    `photo_url` VARCHAR(500) COMMENT '药片照片',
    `ai_result` VARCHAR(500) COMMENT 'AI识别结果',
    `ai_accuracy` DECIMAL(5,2) COMMENT 'AI识别准确率',
    `status` INT DEFAULT 0 COMMENT '0:待服药 1:已服药 2:漏服',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_scheduled_time` (`scheduled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 药品冲突表
CREATE TABLE IF NOT EXISTS `medicineinteraction` (
    `interaction_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `medicine_id_a` BIGINT NOT NULL,
    `medicine_id_b` BIGINT NOT NULL,
    `severity` VARCHAR(20) COMMENT '严重程度',
    `description` TEXT COMMENT '冲突描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_medicine_a` (`medicine_id_a`),
    INDEX `idx_medicine_b` (`medicine_id_b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 计划提醒时间表
CREATE TABLE IF NOT EXISTS `plan_reminder_time` (
    `time_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `plan_id` BIGINT NOT NULL,
    `suggested_time` TIME NOT NULL COMMENT '建议服药时间',
    `reason` VARCHAR(255) COMMENT '时间建议原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试用户（密码: 123456）
INSERT IGNORE INTO `user` (`user_name`, `password`, `realname`, `role`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 1),
('test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '测试用户', 0);
