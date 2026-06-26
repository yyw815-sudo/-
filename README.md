表格

1. user（用户表）
字段	类型	说明
user_id	bigint PK	用户ID
username	varchar(50)	用户名
password	varchar(255)	密码（加密）
real_name	varchar(50)	姓名
gender	tinyint	性别
birthday	date	出生日期
phone	varchar(20)	手机号
email	varchar(100)	邮箱
avatar	varchar(255)	头像
status	tinyint	账号状态
create_time	datetime	创建时间
update_time	datetime	更新时间


3. admin（管理员表）
字段	类型	说明
admin_id	bigint PK	
username	varchar(50)	
password	varchar(255)	
real_name	varchar(50)	
phone	varchar(20)	
role	varchar(20)	
status	tinyint	
create_time	datetime
