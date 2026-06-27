CREATE DATABASE IF NOT EXISTS medical_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medical_system;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS system_announcement;
DROP TABLE IF EXISTS data_backup;
DROP TABLE IF EXISTS api_config;
DROP TABLE IF EXISTS health_trend;
DROP TABLE IF EXISTS compliance_statistics;
DROP TABLE IF EXISTS familyauth;
DROP TABLE IF EXISTS familymember;
DROP TABLE IF EXISTS reminderstrategy;
DROP TABLE IF EXISTS reminderlog;
DROP TABLE IF EXISTS reminder;
DROP TABLE IF EXISTS medicationrecord;
DROP TABLE IF EXISTS plan_reminder_time;
DROP TABLE IF EXISTS medicationplan;
DROP TABLE IF EXISTS medicineinteraction;
DROP TABLE IF EXISTS medicine;
DROP TABLE IF EXISTS ocr_record;
DROP TABLE IF EXISTS medicalrecord;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(50),
    gender TINYINT,
    birthday DATE,
    phone VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(50),
    phone VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicalrecord (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    disease_name VARCHAR(100),
    hospital VARCHAR(100),
    department VARCHAR(50),
    doctor VARCHAR(50),
    diagnosis TEXT,
    prescription TEXT,
    record_date DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicalrecord_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ocr_record (
    ocr_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    image_url VARCHAR(255),
    recognize_text TEXT,
    recognize_result TEXT,
    accuracy DECIMAL(5,2),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ocrrecord_medicalrecord FOREIGN KEY (record_id) REFERENCES medicalrecord(record_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicine (
    medicine_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_name VARCHAR(100) NOT NULL,
    specification VARCHAR(100),
    dosage_form VARCHAR(50),
    manufacturer VARCHAR(100),
    indication TEXT,
    contraindication TEXT,
    precautions TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicineinteraction (
    interaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_a BIGINT NOT NULL,
    medicine_b BIGINT NOT NULL,
    conflict_level VARCHAR(20),
    description TEXT,
    suggestion TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interaction_medicine_a FOREIGN KEY (medicine_a) REFERENCES medicine(medicine_id) ON DELETE CASCADE,
    CONSTRAINT fk_interaction_medicine_b FOREIGN KEY (medicine_b) REFERENCES medicine(medicine_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicationplan (
    plan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    record_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    times_per_day INT NOT NULL,
    interval_hours INT,
    start_date DATE NOT NULL,
    end_date DATE,
    purpose TEXT,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicationplan_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_medicationplan_record FOREIGN KEY (record_id) REFERENCES medicalrecord(record_id) ON DELETE CASCADE,
    CONSTRAINT fk_medicationplan_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_reminder_time (
    time_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    suggested_time TIME NOT NULL,
    reason VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_planreminder_plan FOREIGN KEY (plan_id) REFERENCES medicationplan(plan_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicationrecord (
    take_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    scheduled_time DATETIME NOT NULL,
    take_time DATETIME,
    photo_url VARCHAR(255),
    ai_result TEXT,
    ai_accuracy DECIMAL(5,2),
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicationrecord_plan FOREIGN KEY (plan_id) REFERENCES medicationplan(plan_id) ON DELETE CASCADE,
    CONSTRAINT fk_medicationrecord_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_medicationrecord_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reminder (
    reminder_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    reminder_time TIME NOT NULL,
    channel VARCHAR(20) DEFAULT 'APP',
    enabled TINYINT DEFAULT 1,
    level INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_plan FOREIGN KEY (plan_id) REFERENCES medicationplan(plan_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reminderlog (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reminder_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    channel VARCHAR(20),
    content TEXT,
    send_time DATETIME,
    receive_time DATETIME,
    status VARCHAR(20) DEFAULT 'pending',
    response TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminderlog_reminder FOREIGN KEY (reminder_id) REFERENCES reminder(reminder_id) ON DELETE CASCADE,
    CONSTRAINT fk_reminderlog_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reminderlog_plan FOREIGN KEY (plan_id) REFERENCES medicationplan(plan_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reminderstrategy (
    strategy_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    level INT NOT NULL,
    trigger_condition VARCHAR(50),
    delay_minutes INT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    content TEXT,
    escalate_to VARCHAR(50) DEFAULT 'NO',
    enabled TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE familymember (
    member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    family_user_id BIGINT NOT NULL,
    realname VARCHAR(50),
    phone VARCHAR(20),
    relation VARCHAR(50),
    permission_level VARCHAR(20) DEFAULT 'basic',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_familymember_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_familymember_family FOREIGN KEY (family_user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE familyauth (
    auth_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    view_medical_record TINYINT DEFAULT 0,
    view_medication TINYINT DEFAULT 1,
    view_statistics TINYINT DEFAULT 1,
    receive_miss_alert TINYINT DEFAULT 1,
    receive_emergency TINYINT DEFAULT 1,
    disconn_alert TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_familyauth_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_familyauth_member FOREIGN KEY (member_id) REFERENCES familymember(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE compliance_statistics (
    stat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    medicine_id BIGINT,
    stat_date DATE NOT NULL,
    total_doses INT DEFAULT 0,
    taken_doses INT DEFAULT 0,
    missed_doses INT DEFAULT 0,
    compliance_rate DECIMAL(5,2) DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_compliance_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_compliance_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE health_trend (
    trend_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    value VARCHAR(50),
    measure_time DATETIME,
    source VARCHAR(50) DEFAULT 'manual',
    remarks TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_healthtrend_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_announcement (
    announce_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(50) DEFAULT 'system',
    target VARCHAR(50) DEFAULT 'all',
    publish_time DATETIME,
    end_time DATETIME,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100) NOT NULL,
    module VARCHAR(50),
    detail TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operationlog_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE data_backup (
    backup_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    backup_name VARCHAR(200) NOT NULL,
    backup_type VARCHAR(50) DEFAULT 'manual',
    backup_path VARCHAR(255),
    backup_size VARCHAR(50),
    status VARCHAR(20) DEFAULT 'success',
    backup_time DATETIME,
    restore_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE api_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_name VARCHAR(100) NOT NULL,
    api_type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(255),
    app_key VARCHAR(255),
    app_secret VARCHAR(255),
    status TINYINT DEFAULT 1,
    remarks TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO admin (admin_name, password, realname, phone) VALUES ('admin', 'admin123', 'Administrator', '13800138000');