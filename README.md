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
