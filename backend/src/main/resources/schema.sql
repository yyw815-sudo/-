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
    `medicines` TEXT COMMENT '药品信息（与处理意见分离）',
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

-- AI分析结果表
CREATE TABLE IF NOT EXISTS `ai_analysis_result` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `record_id` BIGINT NOT NULL,
    `risk_score` INT COMMENT '风险评分(0-100)',
    `risk_level` VARCHAR(20) COMMENT '风险等级',
    `total_drugs` INT COMMENT '药品总数',
    `has_conflict` INT DEFAULT 0 COMMENT '是否有冲突 0:无 1:有',
    `version` INT DEFAULT 1 COMMENT '分析版本',
    `conflicts` TEXT COMMENT '冲突详情JSON',
    `warnings` TEXT COMMENT '风险提示JSON数组',
    `analysis_time` DATETIME COMMENT '分析时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 药物相互作用表（由外部导入）
CREATE TABLE IF NOT EXISTS `druginteraction` (
    `interaction_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `drug_name_a` VARCHAR(100) NOT NULL COMMENT '药品A名称',
    `drug_name_b` VARCHAR(100) NOT NULL COMMENT '药品B名称',
    `level` VARCHAR(20) COMMENT '冲突级别(HIGH/MEDIUM/LOW)',
    `description` TEXT COMMENT '冲突描述',
    `suggestion` TEXT COMMENT '建议方案',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_drug_a` (`drug_name_a`),
    INDEX `idx_drug_b` (`drug_name_b`)
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

-- 插入测试药品数据（含适应症）
INSERT IGNORE INTO `medicine` (`medicine_id`, `medicine_name`, `indication`) VALUES
(1, '苯磺酸氨氯地平片', '用于高血压及心绞痛的治疗'),
(2, '盐酸二甲双胍片', '用于2型糖尿病的治疗'),
(3, '阿司匹林肠溶片', '用于预防血栓形成'),
(4, '阿托伐他汀钙片', '用于高脂血症的治疗'),
(5, '硝苯地平控释片', '用于高血压的治疗'),
(6, '格列美脲片', '用于2型糖尿病的治疗'),
(7, '硫酸氢氯吡格雷片', '用于预防动脉粥样硬化血栓形成'),
(8, '瑞舒伐他汀钙片', '用于高脂血症的治疗'),
(9, '阿司匹林肠溶片', '解热镇痛、抗血小板聚集'),
(10, '盐酸二甲双胍片', '2型糖尿病'),
(11, '氯雷他定片', '过敏性鼻炎、荨麻疹'),
(12, '奥美拉唑肠溶胶囊', '胃溃疡、反流性食管炎'),
(13, '硝苯地平控释片', '高血压、心绞痛'),
(14, '二甲双胍', '2型糖尿病'),
(15, '铝碳酸镁咀嚼片', '胃酸过多、胃溃疡'),
(16, '艾司西酞普兰片', '抑郁症、焦虑症'),
(17, '氟哌噻吨美利曲辛片', '轻中度抑郁、焦虑'),
(18, '乐盼片', '轻中度抑郁、焦虑'),
(19, '律康胶囊', '焦虑症'),
(20, '郝智片', '肌肉痉挛'),
(21, '思诺思片', '失眠'),
(22, '坦度螺酮胶囊（律康）', '焦虑症'),
(23, '巴氯芬片（郝智）', '肌肉痉挛'),
(24, '唑吡坦片（思诺思）', '失眠'),
(25, '低盐低脂饮食', '饮食控制、辅助降压降糖降脂'),
(26, '厄贝沙坦片', '高血压、2型糖尿病伴微量白蛋白尿'),
(27, '氟哌噻吨美利曲辛', '轻中度抑郁、焦虑'),
(28, '坦度螺酮', '焦虑症'),
(29, '巴氯芬', '肌肉痉挛'),
(30, '艾司西酞普兰', '抑郁症、焦虑症'),
(31, '唑吡坦', '失眠'),
(32, '乐盼', '轻中度抑郁、焦虑'),
(33, '坦度螺酮胶囊', '焦虑症'),
(34, '巴氯芬片', '肌肉痉挛'),
(35, '唑吡坦片', '失眠'),
(36, '阿卡波糖', '2型糖尿病');
