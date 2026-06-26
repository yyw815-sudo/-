user
| 字段          | 类型           | 说明     |
| ----------- | ------------ | ------ |
| user_id     | bigint PK    | 用户ID   |
| user_name    | varchar(50)  | 用户名    |
| password    | varchar(255) | 密码（加密） |
| realname   | varchar(50)  | 姓名     |
| gender      | tinyint      | 性别     |
| birthday    | date         | 出生日期   |
| phone       | varchar(20)  | 手机号    |


admin
| 字段          | 类型           | 说明                 |
| ----------- | ------------ | ------------------ |
| admin_id    | bigint       | 管理员唯一编号（主键，自增）     |
| admin_name    | varchar(50)  | 管理员登录账号（唯一）        |
| password    | varchar(255) | 管理员登录密码            |
| realname   | varchar(50)  | 管理员真实姓名            |
| phone       | varchar(20)  | 管理员联系电话            |


medicalrecord
| 字段           | 类型           | 说明       |
| ------------ | ------------ | -------- |
| record_id    | bigint       | 病历编号（主键） |
| user_id      | bigint       | 所属用户     |
| disease_name | varchar(100) | 慢性病名称    |
| hospital     | varchar(100) | 就诊医院     |
| department   | varchar(50)  | 就诊科室     |
| doctor       | varchar(50)  | 主治医生     |
| diagnosis    | text         | 医生诊断结果   |
| prescription | text         | 医生处方内容   |
| record_date  | date         | 就诊日期     |


ocr_record
| 字段               | 类型           | 说明        |
| ---------------- | ------------ | --------- |
| ocr_id           | bigint       | OCR记录编号   |
| record_id        | bigint       | 对应病历编号    |
| image_url        | varchar(255) | 上传图片地址    |
| recognize_text   | text         | OCR识别原始文字 |
| recognize_result | text         | 提取出的药物信息  |
| accuracy         | decimal(5,2) | OCR识别准确率  |
| create_time      | datetime     | 上传时间      |


medicine
| 字段               | 类型           | 说明     |
| ---------------- | ------------ | ------ |
| medicine_id      | bigint       | 药品编号   |
| medicine_name    | varchar(100) | 药品名称   |
| specification    | varchar(100) | 药品规格   |
| dosage_form      | varchar(50)  | 剂型     |
| manufacturer     | varchar(100) | 生产厂家   |
| indication       | text         | 适应症    |
| contraindication | text         | 禁忌症    |
| precautions      | text         | 用药注意事项 |


medicineinteraction
| 字段             | 类型          | 说明          |
| -------------- | ----------- | ----------- |
| interaction_id | bigint      | 冲突规则编号      |
| medicine_a     | bigint      | 药物A编号       |
| medicine_b     | bigint      | 药物B编号       |
| conflict_level | varchar(20) | 冲突等级（低/中/高） |
| description    | text        | 冲突说明        |
| suggestion     | text        | 用药建议        |


medicationrecord
| 字段        | 类型           | 说明              |
| --------- | ------------ | --------------- |
| take_id   | bigint       | 服药记录编号          |
| plan_id   | bigint       | 用药计划编号          |
| user_id   | bigint       | 用户编号            |
| take_time | datetime     | 实际服药时间          |
| photo_url | varchar(255) | 上传照片            |
| ai_result | varchar(255) | AI识别结果          |
| status    | tinyint      | 服药状态（0：漏服，1：已服） |

