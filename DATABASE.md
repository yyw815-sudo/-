# 数据库设计文档

## 一、用户管理相关表

### 1. user 用户表
| 字段          | 类型           | 说明     |
| ----------- | ------------ | ------ |
| user_id     | bigint PK AUTO_INCREMENT | 用户ID（自增） |
| user_name   | varchar(50) UNIQUE COLLATE utf8mb4_bin | 用户名（唯一，区分大小写） |
| password    | varchar(255) | 密码（明文存储，开发环境） |
| realname    | varchar(50)  | 姓名     |
| gender      | tinyint      | 性别（1：男，2：女） |
| birthday    | date         | 出生日期   |
| phone       | varchar(20)  | 手机号    |
| create_time | datetime     | 创建时间   |
| update_time | datetime     | 更新时间   |

> **说明**：当前开发环境密码为明文存储，方便调试。生产环境需改用 BCrypt 加密。
> 
> **大小写敏感**：user_name 字段使用 utf8mb4_bin 排序规则，区分大小写。即 `KUsw`、`kusw`、`KUSW` 视为不同的用户名。

---

### 2. admin 管理员表
| 字段          | 类型           | 说明                 |
| ----------- | ------------ | ------------------ |
| admin_id    | bigint PK AUTO_INCREMENT | 管理员编号（主键，自增） |
| admin_name  | varchar(50) UNIQUE COLLATE utf8mb4_bin | 管理员登录账号（唯一，区分大小写） |
| password    | varchar(255) | 管理员登录密码（明文存储，开发环境） |
| realname    | varchar(50)  | 管理员真实姓名            |
| phone       | varchar(20)  | 管理员联系电话            |
| create_time | datetime     | 创建时间                |
| update_time | datetime     | 更新时间                |

> **说明**：当前开发环境密码为明文存储，方便调试。生产环境需改用 BCrypt 加密。
>
> **默认账号**：admin / admin123
>
> **大小写敏感**：admin_name 字段使用 utf8mb4_bin 排序规则，区分大小写。即 `admin`、`Admin`、`ADMIN` 视为不同的管理员账号。

---

## 二、病历与OCR相关表

> **重要说明**：本系统严格要求用药安全，所有用药计划必须通过正规病历处方生成，禁止用户自行输入处方。具体流程：
> 1. 用户上传病历（OCR识别或手动录入）→ 2. AI分析处方内容 → 3. 用户与AI沟通确认 → 4. AI生成规范的用药计划 → 5. 用户最终确认 → 6. 用药计划生效

### 3. medicalrecord 电子病历表
| 字段           | 类型           | 说明       |
| ------------ | ------------ | -------- |
| record_id    | bigint PK AUTO_INCREMENT | 病历编号（主键） |
| user_id      | bigint FK    | 所属用户ID   |
| disease_name | varchar(100) | 慢性病名称    |
| hospital     | varchar(100) | 就诊医院     |
| department   | varchar(50)  | 就诊科室     |
| doctor       | varchar(50)  | 主治医生     |
| diagnosis    | text         | 医生诊断结果   |
| prescription | text         | 医生处方内容（AI根据此生成用药计划） |
| record_date  | date         | 就诊日期     |
| create_time  | datetime     | 创建时间     |
| update_time  | datetime     | 更新时间     |

---

### 4. ocr_record OCR识别记录表
| 字段               | 类型           | 说明        |
| ---------------- | ------------ | --------- |
| ocr_id           | bigint PK AUTO_INCREMENT | OCR记录编号 |
| record_id        | bigint FK    | 对应病历编号    |
| image_url        | varchar(255) | 上传图片地址    |
| recognize_text   | text         | OCR识别原始文字 |
| recognize_result | text         | 提取出的药物信息（文本格式，方便AI解析） |
| accuracy         | decimal(5,2) | OCR识别准确率  |
| create_time      | datetime     | 上传时间      |

> **说明**：recognize_result 存储文本格式的药物信息（如："阿司匹林 50mg，每日3次；盐酸二甲双胍 500mg，每日2次"），便于AI直接解析生成用药计划。

---

## 三、药品管理相关表

### 5. medicine 药品信息表
| 字段               | 类型           | 说明     |
| ---------------- | ------------ | ------ |
| medicine_id      | bigint PK AUTO_INCREMENT | 药品编号   |
| medicine_name    | varchar(100) | 药品名称   |
| specification    | varchar(100) | 药品规格   |
| dosage_form      | varchar(50)  | 剂型     |
| manufacturer     | varchar(100) | 生产厂家   |
| indication       | text         | 适应症    |
| contraindication  | text         | 禁忌症    |
| precautions      | text         | 用药注意事项 |
| create_time      | datetime     | 创建时间   |
| update_time      | datetime     | 更新时间   |

---

### 6. medicineinteraction 药品冲突规则表
| 字段             | 类型          | 说明          |
| -------------- | ----------- | ----------- |
| interaction_id | bigint PK AUTO_INCREMENT | 冲突规则编号      |
| medicine_a     | bigint FK   | 药物A编号（medicine_id） |
| medicine_b     | bigint FK   | 药物B编号（medicine_id） |
| conflict_level | varchar(20) | 冲突等级（低/中/高） |
| description    | text        | 冲突说明        |
| suggestion     | text        | 用药建议        |
| create_time    | datetime    | 创建时间        |

---

## 四、用药计划与服药记录表

### 7. medicationplan 用药计划表
| 字段            | 类型           | 说明              |
| ------------- | ------------ | --------------- |
| plan_id       | bigint PK AUTO_INCREMENT | 用药计划编号（主键） |
| user_id       | bigint FK    | 用户ID            |
| record_id     | bigint FK    | 病历ID（必须通过病历处方生成） |
| medicine_id   | bigint FK    | 药品ID            |
| dosage        | varchar(50)  | 单次剂量（如：1片/2粒）  |
| frequency     | varchar(50)  | 用药频率（如：每日3次）  |
| times_per_day | int          | 每日服药次数         |
| interval_hours| int          | 服药间隔（小时），供AI参考  |
| start_date    | date         | 计划开始日期         |
| end_date      | date         | 计划结束日期（可为NULL） |
| purpose       | text         | 用药目的/说明        |
| status        | tinyint      | 计划状态（1：启用，0：停用） |
| create_time   | datetime     | 创建时间            |
| update_time   | datetime     | 更新时间            |

> **重要说明**：用药计划仅包含药品信息（吃什么药、吃多少、多久吃一次），由AI根据病历处方生成。提醒时间由用户根据自身作息习惯自行设置（不在此表中）。

---

### 8. plan_reminder_time AI提醒时间建议表
| 字段            | 类型           | 说明              |
| ------------- | ------------ | --------------- |
| time_id       | bigint PK AUTO_INCREMENT | 建议编号（主键） |
| plan_id       | bigint FK    | 用药计划ID          |
| suggested_time | time         | AI建议的提醒时间点（如：08:00:00） |
| reason        | varchar(100) | 建议理由（如：配合用餐、睡前服用等） |
| create_time   | datetime     | 创建时间            |

> **说明**：此表存储AI根据用药频率和间隔建议的提醒时间点，仅供参考。用户可参考后自行在reminder表设置实际提醒时间（因个人作息习惯不同，AI建议可能不完全适用）。

---

### 9. medicationrecord 服药记录表
| 字段        | 类型           | 说明              |
| --------- | ------------ | --------------- |
| take_id   | bigint PK AUTO_INCREMENT | 服药记录编号（主键） |
| plan_id   | bigint FK    | 用药计划ID          |
| user_id   | bigint FK    | 用户ID            |
| medicine_id| bigint FK   | 药品ID            |
| scheduled_time | datetime | 计划服药时间（由系统根据提醒时间生成） |
| take_time | datetime     | 实际服药时间          |
| photo_url | varchar(255) | 上传照片地址          |
| ai_result | text         | AI识别结果（识别出的药片） |
| ai_accuracy| decimal(5,2)| AI识别准确率         |
| status    | tinyint      | 服药状态（0：漏服，1：已服，2：延迟） |
| create_time | datetime   | 创建时间            |

> **说明**：服药记录通过定时任务每日自动生成（status=0待服药），用户拍照确认后更新状态。

---

## 五、提醒管理相关表

### 10. reminder 提醒配置表
| 字段            | 类型           | 说明               |
| ------------- | ------------ | ---------------- |
| reminder_id   | bigint PK AUTO_INCREMENT | 提醒配置编号（主键） |
| user_id       | bigint FK    | 用户ID             |
| plan_id       | bigint FK    | 用药计划ID（可为NULL，表示通用提醒） |
| reminder_time | time         | 提醒时间点（用户根据作息习惯自行设置） |
| channel       | varchar(20)  | 提醒渠道（APP/短信/电话） |
| enabled       | tinyint      | 是否启用（1：是，0：否）  |
| level         | int          | 提醒级别（1：一级，2：二级...） |
| create_time   | datetime     | 创建时间             |
| update_time   | datetime     | 更新时间             |

> **说明**：提醒时间由用户根据自身作息习惯自行设置，可参考AI在plan_reminder_time表中的建议时间。

---

### 11. reminderlog 提醒发送记录表
| 字段           | 类型           | 说明              |
| ------------ | ------------ | --------------- |
| log_id       | bigint PK AUTO_INCREMENT | 记录编号（主键） |
| reminder_id  | bigint FK    | 提醒配置ID           |
| user_id      | bigint FK    | 用户ID              |
| plan_id      | bigint FK    | 用药计划ID           |
| channel      | varchar(20)  | 发送渠道（APP/短信/电话） |
| content      | text         | 发送内容             |
| send_time    | datetime     | 发送时间             |
| receive_time | datetime     | 用户接收时间（可为NULL） |
| status       | varchar(20)  | 状态（待发送/已发送/已接收/已读/失败） |
| response     | text         | 用户响应（如：确认服药/推迟） |
| create_time  | datetime     | 创建时间             |

---

### 12. reminderstrategy 多级提醒策略表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| strategy_id  | bigint PK AUTO_INCREMENT | 策略编号（主键） |
| name         | varchar(100) | 策略名称（如：漏服提醒策略）   |
| type         | varchar(50)  | 策略类型（漏服提醒/连续漏服提醒） |
| level        | int          | 级别序号（1/2/3...）     |
| trigger_condition | varchar(50) | 触发条件（如：漏服X次）      |
| delay_minutes| int          | 延迟时间（分钟）          |
| channel      | varchar(20)  | 提醒渠道（APP/短信/电话）   |
| content      | text         | 提醒内容模板           |
| escalate_to  | varchar(50)  | 是否升级给家属（YES/NO）   |
| enabled      | tinyint      | 是否启用（1：是，0：否）    |
| create_time  | datetime     | 创建时间               |
| update_time  | datetime     | 更新时间               |

> **说明**：此策略为系统级别配置，所有用户统一使用同一套多级提醒策略。管理员可在后台配置策略内容（如：一级提醒APP弹窗 → 二级提醒短信 → 三级提醒电话 → 四级通知家属）。

---

## 六、家庭协作相关表

### 13. familymember 家属信息表
| 字段            | 类型           | 说明               |
| ------------- | ------------ | ---------------- |
| member_id     | bigint PK AUTO_INCREMENT | 家属记录编号（主键） |
| user_id       | bigint FK    | 患者用户ID          |
| family_user_id| bigint FK    | 家属用户ID（必须是已注册用户） |
| realname      | varchar(50)  | 家属姓名              |
| phone         | varchar(20)  | 家属手机号            |
| relation      | varchar(50)  | 与患者关系（如：子女/配偶/父母） |
| permission_level| varchar(20) | 授权查看级别（基础/完整/仅提醒） |
| status        | tinyint      | 状态（1：正常，0：解除关系）  |
| create_time   | datetime     | 创建时间              |
| update_time   | datetime     | 更新时间              |

> **重要约束**：family_user_id 必须关联已注册的 user 用户，家属必须先注册账号才能绑定。

---

### 14. familyauth 家庭隐私授权表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| auth_id      | bigint PK AUTO_INCREMENT | 授权记录编号（主键） |
| user_id      | bigint FK    | 患者用户ID          |
| member_id    | bigint FK    | 家属记录ID           |
| view_medical_record| tinyint | 是否可查看病历（1：是，0：否） |
| view_medication   | tinyint | 是否可查看用药情况       |
| view_statistics    | tinyint | 是否可查看统计数据       |
| receive_miss_alert | tinyint | 是否接收漏服提醒        |
| receive_emergency  | tinyint | 是否接收紧急提醒        |
| disconn_alert     | tinyint | 失联时是否启动接力提醒    |
| create_time  | datetime     | 创建时间              |
| update_time  | datetime     | 更新时间              |

---

## 七、数据分析与统计表

### 15. compliance_statistics 用药依从率统计表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| stat_id      | bigint PK AUTO_INCREMENT | 统计记录编号（主键） |
| user_id      | bigint FK    | 用户ID              |
| medicine_id  | bigint FK    | 药品ID（可为NULL表示全部）  |
| stat_date    | date         | 统计日期              |
| total_doses  | int          | 今日应服药次数           |
| taken_doses  | int          | 今日已服药次数           |
| missed_doses | int          | 今日漏服次数            |
| compliance_rate| decimal(5,2)| 依从率百分比           |
| create_time  | datetime     | 创建时间              |

> **说明**：通过定时任务每日凌晨汇总前一天的数据，追求查询速度。

---

### 16. health_trend 健康趋势数据表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| trend_id     | bigint PK AUTO_INCREMENT | 趋势记录编号（主键） |
| user_id      | bigint FK    | 用户ID              |
| data_type    | varchar(50)  | 数据类型（血压/血糖/心率等）   |
| value        | varchar(50)  | 测量值（如：120/80）     |
| measure_time | datetime     | 测量时间              |
| source       | varchar(50)  | 数据来源（手动录入）       |
| remarks      | text         | 备注说明              |
| create_time  | datetime     | 创建时间              |

> 注：暂不支持智能设备对接，只保留手动录入功能。设备对接可作为后续扩展功能。

---

## 八、系统管理相关表

### 17. system_announcement 系统公告表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| announce_id  | bigint PK AUTO_INCREMENT | 公告编号（主键） |
| title        | varchar(200) | 公告标题              |
| content      | text         | 公告内容              |
| type         | varchar(50)  | 公告类型（系统/维护/升级）   |
| target       | varchar(50)  | 推送对象（全部/指定用户角色）  |
| publish_time | datetime     | 发布时间              |
| end_time     | datetime     | 结束时间（可为NULL）      |
| status       | tinyint      | 状态（1：发布，0：草稿）    |
| create_time  | datetime     | 创建时间              |

---

### 18. operation_log 操作日志表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| log_id       | bigint PK AUTO_INCREMENT | 日志编号（主键） |
| user_id      | bigint       | 操作用户ID（可为NULL表示系统） |
| username     | varchar(50)  | 操作用户名             |
| operation    | varchar(100) | 操作类型              |
| module       | varchar(50)  | 操作模块              |
| detail       | text         | 操作详情              |
| ip_address   | varchar(50)  | IP地址              |
| user_agent   | varchar(255) | 浏览器/客户端信息        |
| create_time  | datetime     | 操作时间              |

---

### 19. data_backup 数据备份记录表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| backup_id    | bigint PK AUTO_INCREMENT | 备份编号（主键） |
| backup_name  | varchar(200) | 备份名称              |
| backup_type  | varchar(50)  | 备份类型（手动/自动）      |
| backup_path  | varchar(255) | 备份文件路径           |
| backup_size  | varchar(50)  | 备份文件大小           |
| status       | varchar(20)  | 状态（成功/失败/恢复中）    |
| backup_time  | datetime     | 备份时间              |
| restore_time | datetime     | 最近恢复时间（可为NULL）   |
| create_time  | datetime     | 创建时间              |

> 注：此功能为可选功能，时间紧张时可先不实现。建议只做手动备份（导出SQL文件）。

---

## 九、API配置表

### 20. api_config 第三方API配置表
| 字段           | 类型           | 说明               |
| ------------ | ------------ | ---------------- |
| config_id    | bigint PK AUTO_INCREMENT | 配置编号（主键） |
| api_name     | varchar(100) | API名称（百度AI/阿里云等）  |
| api_type     | varchar(50)  | API类型（OCR/语音/短信）   |
| endpoint     | varchar(255) | 接口地址              |
| app_key      | varchar(255) | 密钥/APP Key        |
| app_secret   | varchar(255) | 密钥/APP Secret     |
| status       | tinyint      | 状态（1：启用，0：禁用）    |
| remarks      | text         | 备注说明              |
| create_time  | datetime     | 创建时间              |
| update_time  | datetime     | 更新时间              |

> 注：学生项目密钥可明文存储，生产环境需加密存储。

---

## 表关系总览

```
user (1) ──────< (N) medicalrecord       # 一个用户可以有多个病历
medicalrecord (1) ──────< (N) ocr_record # 一个病历可以有多个OCR识别记录
medicalrecord (1) ──────< (N) medicationplan # 一个病历可以对应多个用药计划（追溯治疗哪种病）
medicine (1) ──────< (N) medicineinteraction >────── (1) medicine # 药品冲突规则关联两个药品
user (1) ──────< (N) medicationplan     # 一个用户可以有多个用药计划
medicationplan (1) ──────< (N) plan_reminder_time # 一个用药计划可以有多个提醒时间点
medicationplan (1) ──────< (N) medicationrecord # 一个用药计划可以有多条服药记录
user (1) ──────< (N) reminder           # 一个用户可以有多个提醒配置
reminder (1) ──────< (N) reminderlog    # 一个提醒配置可以有多条发送记录
user (1) ──────< (N) familymember       # 一个用户可以绑定多个家属
familymember (1) ──────< (1) familyauth # 一个家属关系对应一个隐私授权
user (1) ──────< (N) compliance_statistics # 一个用户可以有每日依从率统计
user (1) ──────< (N) health_trend       # 一个用户可以有多条健康趋势数据
```

---

## 数据库表总数

共计 **20 张表**：

| 类别 | 表数量 | 表名 |
|------|--------|------|
| 用户管理 | 2 | user, admin |
| 病历与OCR | 2 | medicalrecord, ocr_record |
| 药品管理 | 2 | medicine, medicineinteraction |
| 用药计划与记录 | 3 | medicationplan, plan_reminder_time, medicationrecord |
| 提醒管理 | 3 | reminder, reminderlog, reminderstrategy |
| 家庭协作 | 2 | familymember, familyauth |
| 数据分析 | 2 | compliance_statistics, health_trend |
| 系统管理 | 4 | system_announcement, operation_log, data_backup, api_config |

---

## 更新记录

| 日期 | 版本 | 更新内容 |
|------|------|---------|
| 2026-06-28 | v1.2 | user_name 和 admin_name 字段排序规则改为 utf8mb4_bin，区分大小写；更新字段说明 |
| 2026-06-28 | v1.1 | 密码字段改为明文存储（开发环境），user 表和 admin 表 password 字段说明更新 |
| 2026-06-28 | v1.0 | 初始版本，20 张数据表设计 |
