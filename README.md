| 字段          | 类型           | 说明     |
| ----------- | ------------ | ------ |
| user_id     | bigint PK    | 用户ID   |
| username    | varchar(50)  | 用户名    |
| password    | varchar(255) | 密码（加密） |
| real_name   | varchar(50)  | 姓名     |
| gender      | tinyint      | 性别     |
| birthday    | date         | 出生日期   |
| phone       | varchar(20)  | 手机号    |
| email       | varchar(100) | 邮箱     |
| avatar      | varchar(255) | 头像     |
| status      | tinyint      | 账号状态   |
| create_time | datetime     | 创建时间   |
| update_time | datetime     | 更新时间   |

