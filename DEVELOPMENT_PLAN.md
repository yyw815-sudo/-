# 慢性病用药智能提醒系统 - 开发计划

## 项目概述

本项目旨在构建一个集个性化用药计划、多级提醒、服药追踪、数据分析和家庭协同于一体的慢性病用药管理系统。

---

## 一、技术栈选型

> ✅ 已确定的技术方案

| 层级 | 技术方案 | 说明 |
|------|---------|------|
| 后端框架 | Spring Boot (Java) | 企业级框架，稳定可靠 |
| 前端框架 | Vue 3 + Element Plus + Vite | 组件丰富，开发效率高 |
| 数据库 | MySQL 8.0 | 成熟稳定，适合关系型数据 |
| 开发工具 | IntelliJ IDEA (后端) + VS Code (前端) | 官方推荐工具 |
| AI服务 | **百度智能云全家桶** | 免费额度最多，学生首选 |
| 提醒推送 | 模拟实现 | APP弹窗/短信/电话（数据库记录） |

### 百度智能云服务详情

| AI能力 | 具体服务 | 免费额度 | 用途 |
|--------|---------|---------|------|
| **OCR识别** | 通用文字识别/手写识别 | 5万次/月 | 处方图片识别、药片识别 |
| **大模型** | 文心一言 ERNIE 4.5 Turbo | 2000万tokens/年 | AI分析处方、生成用药计划、AI对话 |

> 💰 **费用预估**：整个项目开发+测试期间，OCR和AI调用量都在免费额度内，**几乎不用花钱**

---

## 二、团队分工

### 成员组成

| 成员 | 负责模块 | 包含功能 |
|------|----------|----------|
| **同学A** | 用户档案管理 | 用户注册/登录、电子病历（OCR录入）、药品冲突校验 |
| **同学B** | 用药管理 + 服药反馈 | 用药计划（AI分析）、服药记录、AI药片识别、异常预警 |
| **同学C（你）** | 提醒管理 + 家庭协作 + 管理员后台 | 多级提醒、家属授权、管理员数据统计、系统管理 |

---

## 三、开发阶段

### 第一阶段：项目初始化 ⭐ 当前阶段

#### 任务清单

- [x] 1.1 确定技术栈（已确定：Spring Boot + Vue 3 + 百度智能云）
- [x] 1.2 注册百度智能云账号（领取免费额度）✅ 已完成
- [ ] 1.3 初始化后端项目（Spring Boot）⏳ 待完成
- [ ] 1.4 初始化前端项目（Vue 3 + Vite）⏳ 待完成
- [x] 1.5 创建数据库（执行 DATABASE.md 中的建表语句）✅ 已完成
  - 数据库 `medical_system` 已创建
  - 20张数据表已建立
  - 默认管理员账号：admin / admin123
- [ ] 1.6 练习 Git 协作流程（3人都要跑通）⏳ 进行中
  - 已创建 `feature/database` 分支
  - 待推送分支并合并

#### 输出物

- 📄 `DATABASE.md`（数据库设计文档）✅ 已完成
- 📄 `DEVELOPMENT_PLAN.md`（本文件）✅ 已完成
- 📄 `backend/src/main/resources/schema.sql`（建表脚本）✅ 已完成
- ⏳ 项目骨架代码（待初始化）
- ⏳ Git协作流程演练（待完成）

---

### 第二阶段：基础功能开发

#### 同学A - 用户档案管理模块

**后端任务**
- [ ] 实现用户注册接口
- [ ] 实现用户登录接口（含JWT认证）
- [ ] 实现用户信息修改/删除接口
- [ ] 实现电子病历CRUD接口
- [ ] 实现OCR识别接口（调用百度OCR API）
- [ ] 实现药品冲突检测接口

**前端任务**
- [ ] 用户注册/登录页面
- [ ] 用户个人中心页面
- [ ] 电子病历管理页面
- [ ] OCR上传识别页面（对接百度OCR）
- [ ] 药品冲突提示弹窗

---

#### 同学B - 用药管理 + 服药反馈模块

**后端任务**
- [ ] 实现用药计划CRUD接口
- [ ] 实现服药记录CRUD接口
- [ ] 实现AI药片识别接口
- [ ] 实现依从率统计接口
- [ ] 实现连续漏服检测接口

**前端任务**
- [ ] 用药计划管理页面
- [ ] 今日用药页面
- [ ] 服药确认页面（拍照上传）
- [ ] 服药历史记录页面
- [ ] 数据分析图表页面

---

#### 同学C - 提醒管理 + 家庭协作 + 管理员后台

**后端任务**
- [ ] 实现提醒配置CRUD接口
- [ ] 实现APP提醒推送（模拟）
- [ ] 实现短信发送接口（模拟）
- [ ] 实现电话提醒接口（模拟）
- [ ] 实现多级递进提醒逻辑
- [ ] 实现家属关系管理接口
- [ ] 实现管理员登录接口
- [ ] 实现数据统计接口

**前端任务**
- [ ] 提醒设置页面
- [ ] 提醒历史记录页面
- [ ] 家属管理页面
- [ ] 管理员登录页面
- [ ] 管理员Dashboard
- [ ] 系统管理页面

---

### 第三阶段：联调与测试

- [ ] 接口联调测试
- [ ] 数据库关联测试
- [ ] 功能流程测试（用户注册 → 添加病历 → 创建用药计划 → 提醒 → 服药记录）
- [ ] 性能测试
- [ ] 安全性测试（数据加密、权限控制）

---

### 第四阶段：部署与交付

- [ ] 项目部署文档
- [ ] 用户使用手册
- [ ] 项目演示

---

## 四、Git协作流程

### 分支命名规范

```
feature/用户档案-同学A
feature/用药管理-同学B
feature/提醒管理-同学C
feature/xxx具体功能
bugfix/xxx问题修复
```

### 开发流程

```bash
# 1. 每天开始工作前：拉取最新代码
git pull origin main

# 2. 创建或切换到自己的功能分支
git checkout -b feature/xxx

# 3. 开发完成后：提交代码
git add .
git commit -m "完成了XXX功能"

# 4. 切换到主分支，合并代码
git checkout main
git pull origin main
git merge feature/xxx

# 5. 推送到远程
git push origin main

# 6. 如果有冲突：
# - 打开冲突文件，手动解决冲突标记
# - git add .
# - git commit -m "解决冲突"
# - git push origin main
```

### 注意事项

1. **禁止在 main 分支上直接开发**
2. **每次合并前先 pull 最新代码**
3. **每天至少 commit 一次**
4. **提交信息要清晰说明做了什么**
5. **遇到冲突不要慌，先沟通再解决**

---

## 五、项目目录结构

```
medical-reminder-system/
├── backend/                          # 后端代码
│   ├── src/main/java/              # Java源代码
│   │   └── com/medical/
│   │       ├── controller/         # 控制器层
│   │       ├── service/           # 服务层
│   │       ├── repository/        # 数据访问层
│   │       ├── entity/            # 实体类
│   │       ├── dto/               # 数据传输对象
│   │       └── config/            # 配置类
│   ├── src/main/resources/         # 配置文件
│   │   └── application.yml
│   └── pom.xml                     # Maven配置
│
├── frontend/                         # 前端代码
│   ├── src/
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面视图
│   │   │   ├── user/               # 用户模块
│   │   │   ├── medication/         # 用药模块
│   │   │   ├── reminder/           # 提醒模块
│   │   │   ├── family/             # 家庭协作模块
│   │   │   └── admin/              # 管理员模块
│   │   ├── api/                    # API接口封装
│   │   ├── router/                 # 路由配置
│   │   ├── store/                  # 状态管理
│   │   └── utils/                  # 工具函数
│   ├── public/                     # 静态资源
│   └── package.json
│
├── database/                        # 数据库相关
│   ├── schema.sql                  # 建表脚本
│   └── seed.sql                    # 初始数据
│
├── docs/                            # 文档
│   ├── DATABASE.md                 # 数据库设计文档
│   ├── API.md                      # 接口文档
│   └── DEPLOY.md                   # 部署文档
│
└── README.md                        # 项目说明
```

---

## 六、下一步行动

### 已完成的工作 ✅

1. **环境准备**
   - ✅ JDK 24.0.2 已安装（满足要求）
   - ✅ Node.js v18.19.0 已安装（满足要求）
   - ✅ MySQL 8.0.35 已安装并配置
   - ✅ IDEA + Trae AI 开发环境已就绪
   - ✅ Git 2.54.0 已安装

2. **百度智能云账号**
   - ✅ 已注册百度智能云账号
   - ✅ 已创建应用（获取API Key和Secret Key）
   - ✅ OCR服务已开通（5万次/月免费）
   - ✅ 文心一言API已开通

3. **数据库**
   - ✅ 数据库 `medical_system` 已创建
   - ✅ 20张数据表已建立
   - ✅ 默认管理员账号已初始化（admin / admin123）
   - ✅ 建表脚本已保存在 `backend/src/main/resources/schema.sql`

4. **文档**
   - ✅ DATABASE.md 已完成
   - ✅ DEVELOPMENT_PLAN.md 已完成

### 待完成的工作 ⏳

1. **项目初始化**（分配任务）
   - ⏳ 同学A：负责初始化后端项目（Spring Boot骨架）
   - ⏳ 同学B：负责初始化前端项目（Vue 3 + Vite）
   - ⏳ 同学C（你）：负责Git分支推送和合并

2. **Git协作演练**
   - ⏳ 推送 `feature/database` 分支到远程
   - ⏳ 合并分支到主分支
   - ⏳ 3人都要跑通：创建分支 → 提交 → 合并 → 推送流程

3. **配置文件**
   - ⏳ 配置 `backend/src/main/resources/application.yml`
     - 数据库连接（用户名、密码）
     - 百度AI密钥（API Key、Secret Key）

---

## 七、联系方式

| 成员 | 角色 | 负责模块 |
|------|------|----------|
| 同学A | 开发 | 用户档案管理 |
| 同学B | 开发 | 用药管理 + 服药反馈 |
| 同学C（你）| 项目负责人 | 提醒管理 + 家庭协作 + 管理员后台 |

---

## 八、百度智能云接入指南

### 8.1 注册与领取免费额度

1. **访问百度智能云官网**
   - 网址：https://console.bce.baidu.com/ai
   - 使用百度账号登录

2. **开通OCR服务**
   - 控制台 → 人工智能 → 文字识别
   - 点击"领取免费资源包"
   - 选择"通用文字识别"（5万次/月免费）

3. **开通文心一言API**
   - 控制台 → 人工智能 → 模型服务
   - 开通 ERNIE 4.5 Turbo
   - 新用户赠送2000万tokens

4. **获取密钥**
   - 进入"应用列表"
   - 创建应用，获取 `API Key` 和 `Secret Key`
   - **妥善保管，后续写入配置文件**

---

### 8.1 运行环境要求

#### Java JDK（后端）

| 项目 | 要求 | 你当前的版本 |
|------|------|-------------|
| **最低版本** | JDK 11+ | ✅ JDK 24.0.2（完全满足） |
| **推荐版本** | JDK 17 LTS | ✅ 你的24更新 |
| **下载地址** | https://adoptium.net/ 或 https://www.oracle.com/java/technologies/downloads/ | |

> 💡 **注意**：JDK 24是最新版本，功能最新。你当前的24.0.2版本完全够用，无需更换！

#### Node.js（前端）

| 项目 | 要求 | 你当前的版本 |
|------|------|-------------|
| **最低版本** | Node 14+ | ✅ v18.19.0（完全满足） |
| **推荐版本** | Node 18 LTS | ✅ 你的18.19正好是18LTS |
| **下载地址** | https://nodejs.org/ | |

> 💡 **注意**：你当前的 v18.19.0 正好是 Node 18 LTS 版本，非常好！无需更换！

#### MySQL（数据库）

| 项目 | 要求 |
|------|------|
| **最低版本** | MySQL 5.7+ |
| **推荐版本** | MySQL 8.0 |
| **下载地址** | https://dev.mysql.com/downloads/mysql/ |

---

### 8.2 如何更换或安装JDK/Node版本（可选）

> ⚠️ **重要提示**：你当前的JDK 24和Node 18.19.0已经满足要求，**不需要更换**！以下内容仅供需要更换版本的同学参考。

#### 方法1：直接下载安装（推荐）

**安装JDK：**
1. 访问 https://adoptium.net/
2. 下载 JDK 17 LTS（Windows x64 Installer）
3. 运行安装程序，一路下一步
4. 配置环境变量：
   ```powershell
   # 打开"系统属性" → "环境变量"
   # 新建 JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x
   # 编辑 Path 添加 %JAVA_HOME%\bin
   ```
5. 重启 IDEA，打开 File → Project Structure → SDKs，添加新的JDK

**安装Node.js：**
1. 访问 https://nodejs.org/
2. 下载 LTS 版本（18.x.x）
3. 运行安装程序，一路下一步
4. 重启电脑

#### 方法2：使用SDKMAN管理多版本（高级）

如果你想同时保留多个版本的JDK/Node，可以用SDKMAN：

```bash
# 安装SDKMAN（Windows需要WSL或Git Bash）
curl -s "https://get.sdkman.io" | bash

# 安装JDK 17
sdk install java 17.0.9-tem

# 安装Node 18
sdk install nodejs 18.19.0

# 切换版本
sdk use java 17.0.9-tem
sdk use nodejs 18.19.0
```

> 💡 **学生项目建议**：直接安装即可，不需要多版本管理！

---

### 8.3 Spring Boot接入示例

#### 添加依赖 (pom.xml)

```xml
<dependency>
    <groupId>com.baidu.aip</groupId>
    <artifactId>java-sdk</artifactId>
    <version>4.16.3</version>
</dependency>
```

#### 配置文件 (application.yml)

```yaml
baidu:
  ocr:
    app-id: your-app-id
    api-key: your-api-key
    secret-key: your-secret-key
  ai:
    api-key: your-ernie-api-key
    secret-key: your-ernie-secret-key
```

#### OCR服务调用示例

```java
@Service
public class BaiDuOcrService {
    
    public String recognizeText(String imagePath) {
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        // 调用通用文字识别
        Result result = client.basicGeneral(imagePath);
        return result.toString();
    }
}
```

#### 文心一言调用示例

```java
@Service
public class WenXinService {
    
    public String analyzePrescription(String prescription) {
        // 调用文心一言分析处方
        // 返回用药计划建议
    }
}
```

---

### 8.3 费用控制建议

| 月调用量 | OCR费用 | 大模型费用 | 总费用 |
|---------|--------|-----------|--------|
| 100次/月 | 0元 | 0元 | **0元** |
| 500次/月 | 0元 | 0元 | **0元** |
| 1000次/月 | 0元 | 约1元 | **约1元** |

> 💡 **建议**：学生项目每月使用量在500次以内，完全免费

---

*最后更新：2026-06-28*

---

## 九、feature/login-api 分支开发记录

### 9.1 分支概述

- **分支名称**：`feature/login-api`
- **开发内容**：用户登录接口、管理员登录接口、登录页面美化、手机号登录
- **开发时间**：2026-06-27 ~ 2026-06-28

### 9.2 新增/修改文件清单

#### 后端文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `backend/src/main/java/com/medical/controller/AdminController.java` | 新增 | 管理员登录控制器 |
| `backend/src/main/java/com/medical/controller/AuthController.java` | 新增 | 认证控制器（发送验证码、手机号登录） |
| `backend/src/main/java/com/medical/service/AdminService.java` | 新增 | 管理员登录业务逻辑 |
| `backend/src/main/java/com/medical/dto/AdminLoginDTO.java` | 新增 | 管理员登录请求DTO |
| `backend/src/main/java/com/medical/dto/PhoneLoginDTO.java` | 新增 | 手机号登录请求DTO |
| `backend/src/main/java/com/medical/dto/ResetPasswordDTO.java` | 新增 | 重置密码请求DTO |
| `backend/src/main/java/com/medical/config/GlobalExceptionHandler.java` | 修改 | 新增参数校验异常处理，返回具体错误信息 |
| `backend/src/main/java/com/medical/service/UserService.java` | 修改 | 新增 getUserByPhone、resetPassword 方法 |

#### 前端文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `frontend/src/views/user/Login.vue` | 修改 | 登录页面全面美化，新增角色选择、昼夜模式、粒子动画、手机号登录等 |
| `frontend/src/views/user/Register.vue` | 修改 | 删除邮箱字段，完善表单验证（用户名、密码、姓名、手机号） |
| `frontend/src/views/user/ForgotPassword.vue` | 新增 | 忘记密码页面（发送验证码→重置密码） |
| `frontend/src/stores/user.js` | 修改 | 新增 adminLogin 方法，支持管理员角色 |
| `frontend/src/api/user.js` | 修改 | 新增 adminLogin、getAdminInfo 接口封装 |
| `frontend/src/api/auth.js` | 修改 | 新增 sendCode、phoneLogin、resetPassword 接口封装 |
| `frontend/src/router/index.js` | 修改 | 新增 /forgot-password 路由 |

### 9.3 功能特性

#### 后端功能
- ✅ 用户账号密码登录（BCrypt密码加密）
- ✅ 管理员账号密码登录（BCrypt密码加密）
- ✅ 发送短信验证码（模拟实现，开发环境返回验证码）
- ✅ 手机号验证码登录
- ✅ 忘记密码（手机号+验证码重置密码）

#### 前端功能
- ✅ 角色选择（用户登录 / 管理员登录）
- ✅ 账号密码登录
- ✅ 手机号验证码登录
- ✅ 忘记密码（发送验证码→重置密码）
- ✅ 昼夜模式切换（深色/浅色主题）
- ✅ 粒子动画背景
- ✅ 动态渐变背景
- ✅ 浮动图标装饰
- ✅ 记住密码功能（localStorage持久化）
- ✅ 系统功能介绍区域
- ✅ 响应式设计（适配移动端）
- ✅ 底部波浪装饰

### 9.4 测试账号

| 角色 | 账号 | 密码 | 手机号 | 说明 |
|------|------|------|--------|------|
| 管理员 | admin | admin123 | 13800138000 | 系统默认管理员账号 |

> **注意**：用户账号需通过注册页面创建，数据库已清空测试数据。

### 9.5 遇到的问题与解决方案

| 问题描述 | 原因分析 | 解决方案 | 状态 |
|---------|---------|---------|------|
| MySQL数据目录中文路径导致启动失败 | MySQL不支持中文路径的数据目录 | 将数据目录复制到纯英文路径 `E:\mysql_data` | ✅ 已解决 |
| MySQL root密码不匹配 | 复制数据目录后密码不一致 | 用 --skip-grant-tables 启动MySQL，重置root密码 | ✅ 已解决 |
| 数据库不存在（medical_system） | 新数据目录没有业务数据库 | 执行 schema.sql 初始化数据库和表结构 | ✅ 已解决 |
| 登录页面输入框图标遮挡点击 | input-icon 的 z-index 为10且未禁用点击事件 | 添加 `pointer-events: none` 让图标不阻挡点击 | ✅ 已解决 |
| 手机号登录只有前端mock无后端接口 | 后端缺少手机号登录和验证码发送接口 | 新增 `/user/send-code` 和 `/user/phone-login` 接口 | ✅ 已解决 |
| 验证码输入框与按钮排版不对齐 | 使用 absolute 定位，按钮高度与输入框不匹配 | 改为 flex 布局，输入框和按钮并排对齐 | ✅ 已解决 |
| 注册页面表单验证不完善 | 只有用户名和密码必填验证 | 补充手机号、邮箱、姓名、密码长度等验证规则 | ✅ 已解决 |
| UserController职责耦合 | 手机号登录验证码功能放在UserController（同学A模块） | 新建AuthController统一管理认证相关功能 | ✅ 已解决 |
| 参数校验异常返回系统错误 | @Valid校验失败被全局ExceptionHandler捕获成"系统错误" | 新增MethodArgumentNotValidException处理器，返回具体校验错误 | ✅ 已解决 |
| 管理员登录无参数校验 | AdminController用Map接收参数，空提交直接返回密码错误 | 新增AdminLoginDTO，使用@Valid校验 | ✅ 已解决 |
| 注册页面邮箱字段多余 | 注册页面包含邮箱输入，需求不需要 | 删除注册页面的邮箱字段及验证 | ✅ 已解决 |
| HikariCP连接池连接失效 | MySQL服务重启后连接池中的连接失效 | 配置连接池自动检测失效连接（待优化） | ⚠️ 待优化 |
| 登录页面过度美化导致报错 | CSS/动画效果过于复杂 | 简化效果，逐步添加功能 | ✅ 已解决 |

### 9.6 白盒测试报告

#### 前端代码审查 (Login.vue + Register.vue)

| 测试项 | 测试内容 | 结果 |
|-------|---------|------|
| 响应式数据 | loginForm、phoneForm、rememberPwd 等数据定义 | ✅ 通过 |
| 账号登录表单验证 | 用户名必填、密码至少6位 | ✅ 通过 |
| 手机号登录表单验证 | 手机号必填、11位、格式正则、验证码6位数字 | ✅ 通过 |
| 注册表单验证 | 用户名3-20位、密码6-20位、姓名2-20位、手机号11位、邮箱格式 | ✅ 通过 |
| 边界条件 | 空账号、空密码、短密码、错误手机号格式、错误邮箱格式 | ✅ 通过 |
| 角色切换 | 切换角色时清空表单和验证状态 | ✅ 通过 |
| 登录模式切换 | 账号/手机号模式切换，v-if 正确渲染 | ✅ 通过 |
| 验证码倒计时 | 60秒倒计时，期间禁用按钮 | ✅ 通过 |
| 登录处理 | 区分用户/管理员角色，调用不同API | ✅ 通过 |
| 异常处理 | try-catch 包裹异步请求 | ✅ 通过 |
| 记住密码 | localStorage 读写正确 | ✅ 通过 |
| 粒子动画 | onMounted 初始化，onUnmounted 销毁 | ✅ 通过 |
| 内存泄漏 | animationFrame 正确取消 | ✅ 通过 |

#### 后端代码审查 (UserController / AdminController / AuthController)

| 测试项 | 测试内容 | 结果 |
|-------|---------|------|
| 参数校验 | @Valid 注解校验 DTO | ✅ 通过 |
| 密码加密 | BCrypt 加密存储和验证 | ✅ 通过 |
| 接口路径 | RESTful 设计规范 | ✅ 通过 |
| 返回格式 | 统一 Result 封装 | ✅ 通过 |
| 空值处理 | 用户不存在时返回错误信息 | ✅ 通过 |
| 验证码逻辑 | 生成、存储、验证、删除流程 | ✅ 通过 |
| 事务管理 | Service 层事务注解 | ✅ 通过 |
| 安全考虑 | 密码不返回前端 | ✅ 通过 |

#### 潜在问题（白盒发现）

1. **验证码存储在内存中**：重启服务后验证码失效，且集群环境不共享
   - 建议：后续改用 Redis 存储
2. **无登录频率限制**：可能被暴力破解
   - 建议：添加验证码次数限制和IP限流
3. **Token 为 mock 形式**：未实现真正的 JWT 认证
   - 建议：后续集成 JWT

### 9.7 黑盒测试报告

#### 功能测试（后端接口实测）

| 测试用例 | 测试步骤 | 预期结果 | 实际结果 | 状态 |
|---------|---------|---------|---------|------|
| 用户注册-正常注册（无邮箱） | 调用 /user/register，传入用户名/密码/姓名/手机号 | 返回200，注册成功 | 返回200，userId=2 | ✅ 通过 |
| 用户登录-空提交 | 调用 /user/login，传空body | 返回500，提示"用户名不能为空；密码不能为空" | 返回正确提示 | ✅ 通过 |
| 用户登录-正确账号 | 调用 /user/login，testuser4/test123 | 返回200+token | 返回200，token=mock-token-1 | ✅ 通过 |
| 用户登录-错误密码 | 调用 /user/login，testuser4/wrongpwd | 返回500，提示密码错误 | 返回500，提示用户名或密码错误 | ✅ 通过 |
| 管理员登录-空提交 | 调用 /admin/login，传空body | 返回500，提示"请输入管理员账号；请输入管理员密码" | 返回正确提示 | ✅ 通过 |
| 管理员登录-正确账号 | 调用 /admin/login，admin/admin123 | 返回200+token | 返回200，token=admin-token-1 | ✅ 通过 |
| 管理员登录-错误密码 | 调用 /admin/login，admin/wrongpwd | 返回500，提示密码错误 | 返回500，提示管理员账号或密码错误 | ✅ 通过 |
| 发送验证码-空手机号 | 调用 /auth/send-code，传空body | 返回500，提示手机号格式错误 | 返回500，提示请输入正确的手机号 | ✅ 通过 |
| 发送验证码-正确手机号 | 调用 /auth/send-code，phone=13800138001 | 返回200+6位验证码 | 返回200，验证码为6位数字 | ✅ 通过 |
| 发送验证码-错误手机号格式 | 调用 /auth/send-code，phone=12345 | 返回500，提示格式错误 | 返回500，提示请输入正确的手机号 | ✅ 通过 |
| 手机号登录-正确验证码 | 先发送验证码，再调用 /auth/phone-login | 返回200+token | 返回200，token=mock-phone-token-1 | ✅ 通过 |
| 手机号登录-错误验证码 | 先发送验证码，再用错误验证码登录 | 返回500，验证码错误 | 返回500，提示验证码错误 | ✅ 通过 |
| 手机号登录-未注册手机号 | 给未注册手机号发验证码后登录 | 返回500，手机号未注册 | 返回500，提示该手机号未注册 | ✅ 通过 |
| 重置密码-空提交 | 调用 /auth/reset-password，传空body | 返回500，提示手机号/验证码/密码不能为空 | 返回500，提示格式错误 | ✅ 通过 |
| 重置密码-错误验证码 | 发送验证码后用错误验证码重置 | 返回500，验证码错误 | 返回500，提示验证码错误或已过期 | ✅ 通过 |
| 重置密码-正确流程 | 发送验证码+正确验证码+新密码重置 | 返回200，密码重置成功 | 返回200，重置成功 | ✅ 通过 |
| 重置密码-验证新密码 | 用新密码登录测试用户4 | 返回200，登录成功 | 返回200，登录成功 | ✅ 通过 |

#### 功能测试（前端表单验证）

| 测试用例 | 测试步骤 | 预期结果 | 状态 |
|---------|---------|---------|------|
| 登录-空账号 | 不输入用户名，点击登录 | 提示"请输入账号" | ✅ 通过 |
| 登录-短密码 | 输入少于6位密码，点击登录 | 提示"密码长度至少6位" | ✅ 通过 |
| 手机号登录-空手机号 | 不输入手机号，点击获取验证码 | 提示"请输入手机号" | ✅ 通过 |
| 手机号登录-不足11位 | 输入10位手机号，失去焦点 | 提示"手机号必须为11位" | ✅ 通过 |
| 手机号登录-超过11位 | 输入12位手机号，失去焦点 | 提示"手机号必须为11位" | ✅ 通过 |
| 手机号登录-格式错误 | 输入2开头的11位手机号 | 提示"请输入正确的手机号格式" | ✅ 通过 |
| 验证码-不足6位 | 输入5位验证码，失去焦点 | 提示"验证码为6位数字" | ✅ 通过 |
| 验证码-非数字 | 输入字母验证码，失去焦点 | 提示"验证码必须是6位数字" | ✅ 通过 |
| 注册-用户名太短 | 输入2位用户名 | 提示"用户名长度为3-20位" | ✅ 通过 |
| 注册-密码太短 | 输入5位密码 | 提示"密码长度为6-20位" | ✅ 通过 |
| 注册-姓名太短 | 输入1个字 | 提示"姓名长度为2-20位" | ✅ 通过 |
| 注册-无邮箱字段 | 注册页面不显示邮箱输入框 | 不显示邮箱输入框 | ✅ 通过 |
| 角色切换 | 点击"用户登录"/"管理员登录"按钮 | placeholder变化，底部提示变化 | ✅ 通过 |
| 登录模式切换 | 点击"使用手机号登录" | 切换到手机号登录表单 | ✅ 通过 |
| 昼夜模式切换 | 点击右上角月亮/太阳按钮 | 页面主题切换为深色/浅色 | ✅ 通过 |
| 记住密码 | 勾选记住密码并登录，下次打开页面 | 自动填充账号密码 | ✅ 通过 |

#### UI/UX 测试

| 测试项 | 结果 |
|-------|------|
| 页面整体视觉效果 | ✅ 渐变背景+粒子动画+浮动图标，美观 |
| 登录卡片设计 | ✅ 圆角毛玻璃效果，悬浮动效 |
| 按钮交互反馈 | ✅ hover/active 状态明显 |
| 表单验证提示 | ✅ Element Plus 默认样式 |
| 验证码按钮与输入框对齐 | ✅ flex布局，高度一致对齐 |
| 图标与文字对齐 | ✅ 垂直居中对齐 |
| 昼夜模式提示 | ✅ tooltip 提示功能说明 |

#### 响应式测试

| 屏幕尺寸 | 布局表现 | 结果 |
|---------|---------|------|
| 桌面端 (1920px) | 居中卡片，左右留白 | ✅ 正常 |
| 平板 (768px) | 自适应宽度，角色按钮竖排 | ✅ 正常 |
| 手机 (480px) | 紧凑布局，字号减小 | ✅ 正常 |

### 9.8 待优化事项

1. 后端集成真正的 JWT 认证
2. 验证码改用 Redis 存储
3. 添加登录频率限制（防暴力破解）
4. 手机号登录对接真实短信服务
5. ~~忘记密码功能实现~~ ✅ 已完成
6. ~~用户注册页面实现~~ ✅ 已完成
7. HikariCP 连接池 maxLifetime 配置优化

---

## 10. feature/admin-management 分支开发记录

### 10.1 分支概述

**分支名称**: `feature/admin-management`

**开发目标**: 完善管理员功能，实现用户管理和管理员管理的完整 CRUD 操作

**开发时间**: 2026-06-28

### 10.2 新增文件清单

#### 后端新增文件

| 文件路径 | 说明 |
|---------|------|
| `backend/src/main/java/com/medical/dto/AdminAddDTO.java` | 管理员新增/编辑数据传输对象 |

#### 后端修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `backend/src/main/java/com/medical/entity/User.java` | 添加 @JsonProperty 注解，支持驼峰命名；添加 getAge() 计算方法 |
| `backend/src/main/java/com/medical/dto/AdminUserDTO.java` | 替换 age 字段为 birthday 字段 |
| `backend/src/main/java/com/medical/controller/AdminController.java` | 新增管理员管理接口（列表、新增、编辑、删除、重置密码） |
| `backend/src/main/java/com/medical/service/AdminService.java` | 新增分页查询和重置密码方法 |
| `backend/src/main/java/com/medical/repository/AdminRepository.java` | 新增分页查询方法 |
| `backend/src/main/java/com/medical/repository/UserRepository.java` | 新增分页查询方法 |
| `backend/src/main/java/com/medical/controller/AuthController.java` | 修正字段名 getRealName() → getRealname() |

#### 前端新增文件

无

#### 前端修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `frontend/src/views/admin/UserManage.vue` | 年龄改为日期选择器；弹窗添加可移动；美化表格样式；添加用户头像 |
| `frontend/src/views/admin/AdminProfile.vue` | 新增"管理员管理"标签页；完善个人信息展示；美化界面 |
| `frontend/src/api/admin.js` | 新增管理员管理相关 API |

### 10.3 新增功能

#### 10.3.1 用户管理功能增强

| 功能 | 说明 |
|------|------|
| 日期选择器 | 新增/编辑用户时使用日期选择器选择出生日期，自动计算年龄 |
| 弹窗可移动 | 所有弹窗支持拖动 |
| 用户头像 | 表格中显示用户头像（用户名首字母） |
| 性别标签 | 使用彩色标签显示性别 |
| 用户总数统计 | 页面显示用户总数徽章 |
| 搜索优化 | 支持用户ID/用户名/姓名/手机号搜索 |

#### 10.3.2 管理员管理功能

| 功能 | 说明 |
|------|------|
| 管理员列表 | 分页展示所有管理员 |
| 新增管理员 | 创建新管理员账号 |
| 编辑管理员 | 修改管理员信息（姓名、手机号、密码） |
| 删除管理员 | 删除指定管理员（不能删除当前登录账号） |
| 重置密码 | 重置管理员密码 |
| 搜索管理员 | 支持ID/账号/姓名/手机号搜索 |

### 10.4 测试数据

#### 管理员测试账号

| 账号 | 密码 | 真实姓名 | 手机号 |
|------|------|---------|--------|
| admin | admin123 | Administrator | 13800138003 |
| admin2 | admin123 | 管理员2 | 13800138002 |

#### 用户测试数据

| 用户ID | 用户名 | 密码 | 姓名 | 性别 | 出生日期 | 年龄 | 手机号 |
|--------|--------|------|------|------|---------|------|--------|
| 8 | user1 | 123456 | 小明 | 女 | 2019-06-28 | 7 | 14752291641 |
| 9 | GOGO | 123456 | 小红 | 女 | 2004-06-28 | 22 | 14852249532 |
| 10 | user3 | 123456 | 小刚 | 女 | 2020-06-28 | 6 | 18374838384 |
| 11 | testuser6 | 123456 | 测试用户6 | 男 | 2000-01-15 | 26 | 13900139006 |

### 10.5 问题记录与解决方案

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 用户姓名/性别/年龄看不见 | 前后端字段名不匹配（realname vs realName） | 在 User 实体添加 @JsonProperty 注解 |
| 用户ID搜索返回多个结果 | JPQL 查询中 CAST 字符串为 long 时失败，所有条件都会执行 | 使用 CONCAT 替代 % 操作符 |
| 新增用户 birthday 为空 | AdminUserDTO 使用 age 字段而非 birthday | 修改 DTO 为 birthday 字段 |
| AuthController 编译错误 | 调用不存在的 getRealName() 方法 | 修正为 getRealname() |
| JPA REGEXP 不支持 | JPQL 不支持 MySQL REGEXP 语法 | 移除 REGEXP 判断逻辑 |

### 10.6 黑盒测试报告

#### 接口测试

| 接口 | 方法 | 测试内容 | 结果 |
|------|------|---------|------|
| /api/admin/login | POST | 管理员登录（admin/admin123） | ✅ 成功 |
| /api/admin/login | POST | 管理员登录（admin2/admin123） | ✅ 成功 |
| /api/admin/users | GET | 获取用户列表（分页） | ✅ 成功 |
| /api/admin/users | POST | 新增用户（含 birthday） | ✅ 成功 |
| /api/admin/users/{id} | GET | 获取用户详情 | ✅ 成功 |
| /api/admin/users/{id} | PUT | 更新用户信息 | ✅ 成功 |
| /api/admin/users/{id} | DELETE | 删除用户 | ✅ 成功 |
| /api/admin/users | GET | 搜索用户（keyword） | ✅ 成功 |
| /api/admin/admins | GET | 获取管理员列表 | ✅ 成功 |
| /api/admin/admins | POST | 新增管理员 | ✅ 成功 |
| /api/admin/admins/{id} | PUT | 更新管理员信息 | ✅ 成功 |
| /api/admin/admins/{id} | DELETE | 删除管理员 | ✅ 成功 |
| /api/admin/admins/{id}/reset-password | PUT | 重置管理员密码 | ✅ 成功 |

#### 前端功能测试

| 测试项 | 结果 |
|------|------|
| 管理员登录 | ✅ 成功跳转管理后台 |
| 用户管理页面展示 | ✅ 表格正常显示 |
| 新增用户（日期选择器） | ✅ 出生日期选择，年龄自动计算 |
| 编辑用户 | ✅ 表单数据回显正确 |
| 删除用户 | ✅ 二次确认，删除成功 |
| 搜索用户 | ✅ 支持多条件搜索 |
| 弹窗可移动 | ✅ draggable 属性生效 |
| 个人中心信息展示 | ✅ 显示管理员编号、账号、姓名等 |
| 修改个人信息 | ✅ 保存成功 |
| 管理员管理列表 | ✅ 分页展示 |
| 新增管理员 | ✅ 创建成功 |
| 编辑管理员 | ✅ 更新成功 |
| 删除管理员 | ✅ 删除成功（排除当前账号） |
| 重置管理员密码 | ✅ 密码修改成功 |

### 10.7 白盒测试报告

#### 代码质量分析

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 编译通过 | ✅ | Maven compile 成功 |
| 字段命名一致性 | ✅ | 数据库/实体/DTO 字段名统一 |
| 密码安全性 | ✅ | BCrypt 加密存储 |
| JSON 序列化 | ✅ | @JsonProperty 注解正确 |
| 空值处理 | ✅ | age 计算处理 null birthday |
| 异常处理 | ✅ | GlobalExceptionHandler 统一处理 |
| 分页逻辑 | ✅ | PageRequest 正确配置 |
| SQL 注入防护 | ✅ | 使用 JPQL 参数化查询 |

#### 潜在风险

| 风险 | 等级 | 说明 |
|------|------|------|
| JPA CAST 字符串转 long | 中 | 非数字字符串会导致 CAST 失败 |
| 密码明文传输 | 低 | 开发环境 HTTP，生产需 HTTPS |
| 无操作日志 | 中 | 管理员操作无审计追踪 |

### 10.8 待优化事项

1. 优化搜索查询，使用 Criteria API 替代 JPQL
2. 添加管理员操作日志（审计追踪）
3. 删除用户时处理关联数据
4. 批量删除功能
5. 导出用户/管理员列表（Excel）
6. 添加更多筛选条件（注册时间范围等）
8. 忘记密码验证码添加5分钟过期时间
9. 补充单元测试和集成测试

---

## 十、feature/admin-user-management 分支开发记录

### 10.1 分支概述

- **分支名称**：`feature/admin-user-management`
- **开发内容**：管理员后台 - 用户管理模块（增删改查、重置密码、数据概览）
- **开发时间**：2026-06-28

### 10.2 新增/修改文件清单

#### 后端文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `backend/src/main/java/com/medical/controller/AdminController.java` | 修改 | 新增用户管理接口（列表/详情/新增/编辑/删除/重置密码） |
| `backend/src/main/java/com/medical/dto/AdminUserDTO.java` | 新增 | 管理员操作用户的请求DTO（带分组校验） |
| `backend/src/main/java/com/medical/service/UserService.java` | 修改 | 新增 `getUserPage` 分页查询方法 |
| `backend/src/main/java/com/medical/repository/UserRepository.java` | 修改 | 新增 `findByKeyword` 分页+关键字搜索方法 |

#### 前端文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `frontend/src/views/admin/AdminLayout.vue` | 新增 | 管理员后台布局（侧边栏导航 + 顶部栏） |
| `frontend/src/views/admin/Dashboard.vue` | 新增 | 管理员数据概览页（统计卡片 + 最近用户 + 系统信息） |
| `frontend/src/views/admin/UserManage.vue` | 新增 | 用户管理页面（表格、搜索、分页、增删改查、重置密码） |
| `frontend/src/api/admin.js` | 新增 | 管理员模块API封装（用户管理相关接口） |
| `frontend/src/router/index.js` | 修改 | 新增管理员后台路由组，增加管理员权限守卫 |
| `frontend/src/stores/user.js` | 修改 | 新增 `userName` getter，适配管理员和用户双角色 |

### 10.3 功能特性

#### 后端功能
- ✅ 用户列表分页查询（支持按用户名/姓名/手机号关键字搜索）
- ✅ 用户详情查询
- ✅ 管理员新增用户（用户名、密码、姓名、性别、年龄、手机号）
- ✅ 管理员编辑用户信息
- ✅ 管理员删除用户
- ✅ 管理员重置用户密码
- ✅ 查询结果密码字段脱敏（不返回前端）

#### 前端功能
- ✅ 管理员后台布局（左侧导航栏 + 顶部面包屑 + 用户下拉菜单）
- ✅ 数据概览 Dashboard（用户总数统计卡片、最近注册用户列表、系统信息）
- ✅ 用户管理页面
  - 搜索功能（支持用户名/姓名/手机号模糊搜索）
  - 分页功能（支持切换每页条数：10/20/50/100）
  - 新增用户（弹窗表单，带前端校验）
  - 编辑用户（弹窗表单，用户名不可修改）
  - 删除用户（二次确认弹窗）
  - 重置密码（弹窗，两次密码确认校验）
- ✅ 管理员路由守卫（未登录或非管理员自动跳转登录页）
- ✅ 管理员退出登录功能

### 10.4 接口列表

| 接口 | 方法 | 说明 |
|------|------|------|
| `/admin/users` | GET | 分页查询用户列表（参数：pageNum, pageSize, keyword） |
| `/admin/users/{userId}` | GET | 查询用户详情 |
| `/admin/users` | POST | 新增用户 |
| `/admin/users/{userId}` | PUT | 编辑用户 |
| `/admin/users/{userId}` | DELETE | 删除用户 |
| `/admin/users/{userId}/reset-password` | PUT | 重置用户密码 |

### 10.5 白盒测试报告

#### 后端代码审查 (AdminController + UserService + UserRepository)

| 测试项 | 测试内容 | 结果 |
|-------|---------|------|
| 分页查询逻辑 | PageRequest 页码从0开始，前端传1时减1处理 | ✅ 通过 |
| 搜索功能 | JPQL 动态查询，keyword 为空时不过滤 | ✅ 通过 |
| 新增用户 | 检查用户名重复，密码 BCrypt 加密 | ✅ 通过 |
| 编辑用户 | 非空字段才更新，保留原有数据 | ✅ 通过 |
| 删除用户 | 先检查用户是否存在，不存在返回错误 | ✅ 通过 |
| 重置密码 | 新密码长度校验（至少6位），BCrypt 加密存储 | ✅ 通过 |
| 密码脱敏 | 查询接口返回前将 password 设为 null | ✅ 通过 |
| 参数校验 | AdminUserDTO 使用 @Valid + 分组校验（新增/编辑不同规则） | ✅ 通过 |
| 异常处理 | 捕获 RuntimeException 返回友好提示 | ✅ 通过 |
| 排序 | 按 createTime 倒序排列，最新注册的在前 | ✅ 通过 |

#### 前端代码审查 (AdminLayout + UserManage + Dashboard)

| 测试项 | 测试内容 | 结果 |
|-------|---------|------|
| 响应式数据 | tableData、total、pageNum、pageSize、searchKeyword 定义完整 | ✅ 通过 |
| 分页逻辑 | 页码变化、每页条数变化时重新请求数据 | ✅ 通过 |
| 搜索功能 | 回车/点击搜索/清空都触发搜索，重置为第1页 | ✅ 通过 |
| 新增表单验证 | 用户名必填3-20位、密码必填6-20位、手机号格式校验 | ✅ 通过 |
| 编辑表单验证 | 用户名禁用、密码字段隐藏、其他字段校验 | ✅ 通过 |
| 重置密码验证 | 新密码必填、确认密码两次一致校验 | ✅ 通过 |
| 删除操作 | ElMessageBox 二次确认，防止误删 | ✅ 通过 |
| 路由守卫 | requiresAdmin 判断，非管理员跳转登录 | ✅ 通过 |
| 菜单激活 | 根据当前路由计算 activeMenu，高亮正确 | ✅ 通过 |
| 加载状态 | v-loading 绑定 loading，防止重复提交 | ✅ 通过 |
| 性别显示转换 | 1=男，2=女，其他=- 显示正确 | ✅ 通过 |
| 时间格式化 | createTime 转成本地时间字符串 | ✅ 通过 |
| 退出登录 | 清空 store 和 localStorage，跳转登录页 | ✅ 通过 |

#### 潜在问题（白盒发现）

1. **无管理员接口鉴权**：所有 `/admin` 接口没有 token 校验，任何人都可以调用
   - 建议：后续集成 JWT + 拦截器，验证管理员 token 和角色
2. **删除用户无关联数据处理**：直接删除用户，未处理用户关联的病历、用药计划等数据
   - 建议：添加外键约束或级联删除，或删除前检查关联数据
3. **无操作日志**：管理员的增删改操作没有记录审计日志
   - 建议：添加操作日志表，记录管理员操作
4. **前端权限仅靠路由守卫**：用户可通过修改 localStorage 绕过
   - 建议：配合后端接口鉴权双重保障

### 10.6 黑盒测试报告

#### 功能测试（后端接口用例）

| 测试用例 | 测试步骤 | 预期结果 | 状态 |
|---------|---------|---------|------|
| 查询用户列表-默认分页 | GET /admin/users，不传参 | 返回第1页10条数据，total正确 | ✅ 通过（代码审查） |
| 查询用户列表-指定页码 | GET /admin/users?pageNum=2&pageSize=5 | 返回第2页5条数据 | ✅ 通过（代码审查） |
| 查询用户列表-关键字搜索 | GET /admin/users?keyword=test | 返回用户名/姓名/手机号含test的用户 | ✅ 通过（代码审查） |
| 查询用户详情-存在 | GET /admin/users/1 | 返回对应用户信息，password为null | ✅ 通过（代码审查） |
| 查询用户详情-不存在 | GET /admin/users/9999 | 返回500，提示"用户不存在" | ✅ 通过（代码审查） |
| 新增用户-正常 | POST /admin/users，传完整字段 | 返回200，用户创建成功 | ✅ 通过（代码审查） |
| 新增用户-用户名重复 | POST /admin/users，用户名已存在 | 返回500，提示"用户名已存在" | ✅ 通过（代码审查） |
| 新增用户-用户名太短 | POST /admin/users，用户名2位 | 返回500，参数校验失败 | ✅ 通过（代码审查） |
| 新增用户-手机号格式错 | POST /admin/users，手机号格式错误 | 返回500，提示手机号格式错误 | ✅ 通过（代码审查） |
| 编辑用户-正常 | PUT /admin/users/1，修改姓名/手机号 | 返回200，更新成功 | ✅ 通过（代码审查） |
| 编辑用户-不存在 | PUT /admin/users/9999 | 返回500，提示"用户不存在" | ✅ 通过（代码审查） |
| 删除用户-正常 | DELETE /admin/users/1 | 返回200，删除成功 | ✅ 通过（代码审查） |
| 删除用户-不存在 | DELETE /admin/users/9999 | 返回500，提示"用户不存在" | ✅ 通过（代码审查） |
| 重置密码-正常 | PUT /admin/users/1/reset-password，newPassword=123456 | 返回200，重置成功 | ✅ 通过（代码审查） |
| 重置密码-密码太短 | PUT /admin/users/1/reset-password，newPassword=123 | 返回500，提示"新密码至少6位" | ✅ 通过（代码审查） |
| 重置密码-用户不存在 | PUT /admin/users/9999/reset-password | 返回500，提示"用户不存在" | ✅ 通过（代码审查） |

#### 功能测试（前端表单验证）

| 测试用例 | 测试步骤 | 预期结果 | 状态 |
|---------|---------|---------|------|
| 新增-空用户名 | 不填用户名，点击确定 | 提示"请输入用户名" | ✅ 通过 |
| 新增-用户名太短 | 输入2位用户名 | 提示"用户名长度为3-20位" | ✅ 通过 |
| 新增-空密码 | 不填密码，点击确定 | 提示"请输入密码" | ✅ 通过 |
| 新增-密码太短 | 输入5位密码 | 提示"密码长度为6-20位" | ✅ 通过 |
| 新增-手机号格式错 | 输入12345 | 提示"请输入正确的手机号" | ✅ 通过 |
| 编辑-用户名禁用 | 打开编辑弹窗 | 用户名输入框禁用，不可修改 | ✅ 通过 |
| 编辑-无密码字段 | 打开编辑弹窗 | 不显示密码输入框 | ✅ 通过 |
| 重置密码-空新密码 | 不填新密码，点击确定 | 提示"请输入新密码" | ✅ 通过 |
| 重置密码-两次不一致 | 新密码123456，确认密码123457 | 提示"两次输入的密码不一致" | ✅ 通过 |
| 删除-二次确认 | 点击删除按钮 | 弹出确认对话框，需再次确认 | ✅ 通过 |

#### UI/UX 测试

| 测试项 | 结果 |
|-------|------|
| 左侧导航栏 | ✅ 深色主题，悬停高亮，点击跳转正确 |
| 顶部面包屑 | ✅ 显示当前页面位置，首页可点击 |
| 用户管理表格 | ✅ 斑马纹、边框、固定右侧操作列 |
| 分页组件 | ✅ 显示总数、页数切换、跳转指定页 |
| 弹窗表单 | ✅ 居中显示、点击遮罩不关闭、底部操作按钮 |
| 搜索框 | ✅ 带搜索图标、清空按钮、回车搜索 |
| 数据概览卡片 | ✅ 彩色图标背景、数字醒目、hover阴影效果 |
| 整体风格 | ✅ 简洁专业的管理后台风格，配色协调 |

### 10.7 待优化事项

1. 后端接口添加管理员鉴权（JWT + 拦截器）
2. 删除用户时处理关联数据（病历、用药计划、提醒等）
3. 添加管理员操作日志（审计追踪）
4. 数据概览 Dashboard 补充更多统计（用药计划数、提醒数、服药记录数）
5. 用户列表支持更多筛选条件（性别、注册时间范围）
6. 批量删除用户功能
7. 导出用户列表（Excel）

### 10.8 密码明文存储与弹窗可拖动（追加修改）

#### 修改说明
根据需求，将密码存储方式改为明文（开发环境），同时为所有弹窗添加可拖动功能。

#### 修改文件清单

| 文件路径 | 修改内容 |
|---------|---------|
| `backend/src/main/java/com/medical/service/UserService.java` | 移除 BCrypt 加密，createUser/login/resetPassword 改用明文比较 |
| `backend/src/main/java/com/medical/service/AdminService.java` | 移除 BCrypt 加密，createAdmin/login/updateAdmin/resetPassword 改用明文比较 |
| `backend/src/main/java/com/medical/config/DataInitializer.java` | 默认管理员密码改为明文 admin123 |
| `backend/src/main/java/com/medical/controller/AdminController.java` | 移除密码字段清空逻辑，返回密码明文 |
| `frontend/src/views/admin/UserManage.vue` | 密码列显示明文，替换掩码 `••••••` |
| `frontend/src/views/admin/AdminProfile.vue` | 管理员列表新增密码列，所有弹窗添加 draggable 属性 |

#### 功能特性（追加）
- ✅ 用户密码明文存储（开发环境）
- ✅ 管理员密码明文存储
- ✅ 用户列表接口返回密码明文
- ✅ 管理员列表接口返回密码明文
- ✅ 登录验证使用字符串相等比较（equals）
- ✅ 用户管理表格显示密码明文
- ✅ 管理员管理表格显示密码明文
- ✅ 新增用户弹窗可拖动
- ✅ 编辑用户弹窗可拖动
- ✅ 重置密码弹窗可拖动
- ✅ 新增管理员弹窗可拖动

#### 测试账号

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 管理员 | admin | admin123 | 系统默认管理员 |
| 管理员 | admin2 | 123456 | 测试管理员 |
| 用户 | testuser6 | 123456 | 测试用户 |

#### 问题记录与解决方案

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 密码显示为 `••••••` | 前端表格中写死了掩码 | 替换为绑定 `row.password` |
| 管理员列表没有密码列 | 管理员列表缺少密码字段展示 | 新增 password 列 |
| 登录失败（密码不匹配） | 数据库中旧数据是 BCrypt 加密，新代码用明文比较 | 将数据库中所有用户/管理员密码更新为明文 `123456` |

#### 密码明文相关黑盒测试

| 接口 | 方法 | 测试内容 | 预期结果 | 状态 |
|------|------|---------|---------|------|
| /api/admin/login | POST | 管理员登录（admin/admin123） | 返回200+token | ✅ 通过 |
| /api/user/login | POST | 用户登录（testuser6/123456） | 返回200+token | ✅ 通过 |
| /api/admin/users | GET | 获取用户列表 | 返回用户列表+password 明文 | ✅ 通过 |
| /api/admin/admins | GET | 获取管理员列表 | 返回管理员列表+password 明文 | ✅ 通过 |
| /api/admin/users | POST | 新增用户（密码明文） | 返回200，密码明文存储 | ✅ 通过 |
| /api/admin/admins | POST | 新增管理员（密码明文） | 返回200，密码明文存储 | ✅ 通过 |

#### 弹窗可拖动测试

| 测试项 | 预期结果 | 状态 |
|-------|---------|------|
| 新增用户弹窗可拖动 | 按住标题栏可拖动 | ✅ 通过 |
| 编辑用户弹窗可拖动 | 按住标题栏可拖动 | ✅ 通过 |
| 重置密码弹窗可拖动 | 按住标题栏可拖动 | ✅ 通过 |
| 新增管理员弹窗可拖动 | 按住标题栏可拖动 | ✅ 通过 |

#### 安全注意事项（白盒发现）

> ⚠️ **重要提示**：明文密码仅适用于开发/演示环境，生产环境必须使用 BCrypt 加密

| 风险项 | 等级 | 说明 |
|-------|------|------|
| 密码明文存储 | 高 | 数据库泄露会导致所有用户密码暴露 |
| 密码明文传输 | 中 | HTTP 传输可被抓包，生产需 HTTPS |
| 管理员密码可见 | 中 | 管理员可看到所有用户明文密码 |
| 无登录限流 | 中 | 可能被暴力破解 |

### 10.9 用户名/账号大小写敏感（追加修改）

#### 修改说明
根据需求，搜索和用户名唯一性校验需要区分大小写，即 `KUsw`、`kusw`、`KUSW` 视为不同的用户名。

#### 修改文件清单

| 文件路径 | 修改内容 |
|---------|---------|
| `backend/src/main/java/com/medical/repository/UserRepository.java` | findByUserName/existsByUserName/findByKeyword 改用原生SQL + BINARY 关键字 |
| `backend/src/main/java/com/medical/repository/AdminRepository.java` | findByAdminName/existsByAdminName/findByKeyword 改用原生SQL + BINARY 关键字 |
| `backend/src/main/java/com/medical/service/UserService.java` | existsByUserName 返回类型改为 Integer 判断 |
| `backend/src/main/java/com/medical/service/AdminService.java` | existsByAdminName 返回类型改为 Integer 判断 |
| `backend/src/main/java/com/medical/config/DataInitializer.java` | existsByAdminName 判断逻辑适配 Integer 返回值 |
| 数据库表 | user.user_name 和 admin.admin_name 排序规则改为 utf8mb4_bin |

#### 功能特性（追加）
- ✅ 用户搜索区分大小写
- ✅ 管理员搜索区分大小写
- ✅ 用户用户名唯一性校验区分大小写
- ✅ 管理员账号唯一性校验区分大小写

#### 问题记录与解决方案

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| HQL 不支持 BINARY 关键字 | JPA HQL 语法限制 | 改用原生SQL查询（nativeQuery = true） |
| existsByUserName 返回 Long 而非 Boolean | 原生SQL COUNT(*) 返回类型问题 | 返回 Integer，判断值 > 0 |
| 数据库唯一约束不区分大小写 | 表排序规则为 utf8mb4_unicode_ci | 修改列排序规则为 utf8mb4_bin |
| 分页排序报错 | 原生SQL使用实体属性名 createTime | 改为数据库列名 create_time |

#### 大小写敏感黑盒测试

| 测试项 | 测试内容 | 预期结果 | 状态 |
|-------|---------|---------|------|
| 用户搜索-KUsw | 搜索关键字 KUsw | 返回1条结果（KUsw） | ✅ 通过 |
| 用户搜索-kusw | 搜索关键字 kusw | 返回0条结果 | ✅ 通过 |
| 用户搜索-KUSW | 搜索关键字 KUSW | 返回0条结果 | ✅ 通过 |
| 新增用户-kuE | 创建用户名 kuE（与 KUe 不同） | 创建成功 | ✅ 通过 |
| 新增用户-KUe | 创建用户名 KUe（已存在） | 返回错误"用户名已存在" | ✅ 通过 |
| 管理员搜索-admin | 搜索关键字 admin | 返回3条结果 | ✅ 通过 |
| 管理员搜索-ADMIN | 搜索关键字 ADMIN | 返回0条结果 | ✅ 通过 |

### 10.10 分支完整测试（最终验证）

#### 新增文件

| 文件路径 | 说明 |
|---------|------|
| `backend/src/main/java/com/medical/dto/AdminUpdateDTO.java` | 管理员编辑DTO，密码字段非必填 |

#### 修复问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 编辑管理员时密码不能为空 | AdminAddDTO 中 password 使用 @NotBlank 注解 | 创建 AdminUpdateDTO，密码字段使用 @Size 而非 @NotBlank |
| 管理员详情接口返回500 | 数据库中无 admin_id=1 的记录 | 使用正确的 admin_id 测试 |

#### 完整后端接口测试

| 序号 | 接口 | 方法 | 测试内容 | 状态 |
|------|------|------|---------|------|
| 1 | /api/admin/login | POST | 管理员登录（admin/admin123） | ✅ 通过 |
| 2 | /api/user/login | POST | 用户登录（testuser6/123456） | ✅ 通过 |
| 3 | /api/admin/users | GET | 用户列表查询 | ✅ 通过 |
| 4 | /api/admin/admins | GET | 管理员列表查询 | ✅ 通过 |
| 5 | /api/admin/users?keyword=KUsw | GET | 用户搜索大小写敏感 | ✅ 通过 |
| 6 | /api/admin/admins?keyword=ADMIN | GET | 管理员搜索大小写敏感 | ✅ 通过 |
| 7 | /api/admin/users | POST | 新增用户 | ✅ 通过 |
| 8 | /api/admin/admins | POST | 新增管理员 | ✅ 通过 |
| 9 | /api/admin/users/{id} | PUT | 编辑用户 | ✅ 通过 |
| 10 | /api/admin/users/{id}/reset-password | PUT | 重置用户密码 | ✅ 通过 |
| 11 | /api/admin/users/{id} | GET | 用户详情 | ✅ 通过 |
| 12 | /api/admin/{adminId} | GET | 管理员详情 | ✅ 通过 |
| 13 | /api/admin/admins/{adminId} | PUT | 编辑管理员 | ✅ 通过 |
| 14 | /api/admin/admins/{adminId}/reset-password | PUT | 重置管理员密码 | ✅ 通过 |
| 15 | /api/admin/admins/{adminId} | DELETE | 删除管理员 | ✅ 通过 |
| 16 | /api/user/register | POST | 用户注册 | ✅ 通过 |

#### 前端功能测试

| 测试项 | 状态 |
|-------|------|
| 前端页面加载正常 | ✅ 通过 |
| 管理员登录页面 | ✅ 通过 |
| 用户登录页面 | ✅ 通过 |
| 用户管理页面（密码明文显示） | ✅ 通过 |
| 管理员管理页面（密码明文显示） | ✅ 通过 |
| 弹窗可拖动功能 | ✅ 通过 |

#### 分支文件清单

**修改文件（共16个）**

| 文件路径 | 修改说明 |
|---------|---------|
| `DATABASE.md` | 更新 user/admin 表字段说明，添加大小写敏感说明 |
| `DEVELOPMENT_PLAN.md` | 更新开发计划，记录10.8-10.11章节内容 |
| `backend/pom.xml` | 添加 Lombok 依赖 |
| `backend/src/main/java/com/medical/config/DataInitializer.java` | 默认管理员密码改为明文，适配 existsByAdminName 返回值变化 |
| `backend/src/main/java/com/medical/controller/AdminController.java` | 新增用户/管理员CRUD接口，修改编辑管理员使用 AdminUpdateDTO |
| `backend/src/main/java/com/medical/controller/AuthController.java` | 用户登录改用明文密码比较 |
| `backend/src/main/java/com/medical/entity/User.java` | 添加 birthday 字段映射 |
| `backend/src/main/java/com/medical/repository/AdminRepository.java` | 搜索和查询改用原生SQL + BINARY 关键字 |
| `backend/src/main/java/com/medical/repository/UserRepository.java` | 搜索和查询改用原生SQL + BINARY 关键字 |
| `backend/src/main/java/com/medical/service/AdminService.java` | 密码改用明文存储，适配 existsByAdminName 返回值 |
| `backend/src/main/java/com/medical/service/UserService.java` | 密码改用明文存储，适配 existsByUserName 返回值 |
| `backend/src/main/resources/application.yml` | 配置 MySQL 连接 |
| `frontend/src/router/index.js` | 添加管理员路由守卫 |
| `frontend/src/stores/user.js` | 添加 Pinia 状态管理 |
| `frontend/src/views/user/Login.vue` | 手机号登录模式、昼夜模式切换 |

**新增文件（共9个）**

| 文件路径 | 说明 |
|---------|------|
| `backend/src/main/java/com/medical/dto/AdminAddDTO.java` | 新增管理员DTO（密码必填） |
| `backend/src/main/java/com/medical/dto/AdminProfileDTO.java` | 管理员个人信息DTO |
| `backend/src/main/java/com/medical/dto/AdminUpdateDTO.java` | 编辑管理员DTO（密码非必填） |
| `backend/src/main/java/com/medical/dto/AdminUserDTO.java` | 用户管理DTO |
| `frontend/src/api/admin.js` | 管理员相关API封装 |
| `frontend/src/views/admin/AdminProfile.vue` | 管理员个人中心页面 |
| `frontend/src/views/admin/Dashboard.vue` | 数据概览Dashboard页面 |
| `frontend/src/views/admin/UserManage.vue` | 用户管理页面 |
| `frontend/src/views/admin/Index.vue` | 管理员首页框架 |

### 10.11 更新待优化事项

1. ✅ 密码明文存储（已完成，开发环境）
2. ✅ 弹窗可拖动（已完成）
3. ✅ 用户名/账号大小写敏感（已完成）
4. ✅ 分支完整测试（已完成）
5. 生产环境改回 BCrypt 密码加密
6. 后端接口添加管理员鉴权（JWT + 拦截器）
7. 删除用户时处理关联数据（病历、用药计划、提醒等）
8. 添加管理员操作日志（审计追踪）
9. 数据概览 Dashboard 补充更多统计（用药计划数、提醒数、服药记录数）
10. 用户列表支持更多筛选条件（性别、注册时间范围）
11. 批量删除用户功能
12. 导出用户列表（Excel）
13. 添加登录频率限制（防暴力破解）
14. HTTPS 传输加密

---

## 11. feature/family-collaboration 分支开发记录

### 11.1 功能特性

| 功能 | 说明 |
|------|------|
| 添加家属信息 | 用户可以添加其他用户作为家属，设置关系和权限级别 |
| 设置家属权限 | 授权家属查看病历、用药计划、统计数据，接收漏服/紧急/断联提醒 |
| 查看家属列表 | 查看所有已添加的家属成员及状态 |
| 修改家属信息 | 修改家属姓名、电话、关系、权限级别 |
| 删除家属信息 | 删除已添加的家属成员，同时删除关联的权限记录 |
| 查看权限详情 | 查看每个家属的权限配置 |

### 11.2 新增文件

| 文件路径 | 说明 |
|---------|------|
| `backend/src/main/java/com/medical/entity/FamilyMember.java` | 家属成员实体类 |
| `backend/src/main/java/com/medical/entity/FamilyAuth.java` | 家属权限实体类 |
| `backend/src/main/java/com/medical/dto/FamilyMemberDTO.java` | 家属成员DTO |
| `backend/src/main/java/com/medical/dto/FamilyAuthDTO.java` | 家属权限DTO |
| `backend/src/main/java/com/medical/repository/FamilyMemberRepository.java` | 家属成员数据访问层 |
| `backend/src/main/java/com/medical/repository/FamilyAuthRepository.java` | 家属权限数据访问层 |
| `backend/src/main/java/com/medical/service/FamilyService.java` | 家庭协作业务逻辑层 |
| `backend/src/main/java/com/medical/controller/FamilyController.java` | 家庭协作控制器 |
| `frontend/src/api/family.js` | 家庭协作API封装 |
| `frontend/src/views/family/Family.vue` | 家庭协作前端页面 |

### 11.3 修改文件

| 文件路径 | 修改说明 |
|---------|---------|
| `frontend/src/router/index.js` | 添加家庭协作用户端和管理员端路由 |

### 11.4 数据库表

**familymember 表**

| 字段 | 类型 | 说明 |
|------|------|------|
| member_id | bigint | 主键，自增 |
| user_id | bigint | 用户ID（主用户） |
| family_user_id | bigint | 家属用户ID |
| realname | varchar(50) | 家属姓名 |
| phone | varchar(20) | 联系电话 |
| relation | varchar(50) | 关系（父母/配偶/子女/兄弟姐妹/其他） |
| permission_level | varchar(20) | 权限级别（basic/advanced） |
| status | tinyint | 状态（1正常/0禁用） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**familyauth 表**

| 字段 | 类型 | 说明 |
|------|------|------|
| auth_id | bigint | 主键，自增 |
| user_id | bigint | 用户ID |
| member_id | bigint | 家属成员ID |
| view_medical_record | tinyint | 查看病历记录（0否/1是） |
| view_medication | tinyint | 查看用药计划（0否/1是） |
| view_statistics | tinyint | 查看统计数据（0否/1是） |
| receive_miss_alert | tinyint | 接收漏服提醒（0否/1是） |
| receive_emergency | tinyint | 接收紧急通知（0否/1是） |
| disconn_alert | tinyint | 接收断联提醒（0否/1是） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 11.5 测试报告

#### 后端接口测试

| 序号 | 接口 | 方法 | 测试内容 | 状态 |
|------|------|------|---------|------|
| 1 | /family/members | POST | 添加家属（用户1添加用户2为子女） | ✅ 通过 |
| 2 | /family/members | GET | 获取家属列表 | ✅ 通过 |
| 3 | /family/members/{id} | PUT | 修改家属信息（姓名、关系、权限级别） | ✅ 通过 |
| 4 | /family/auth/{memberId} | GET | 获取权限详情 | ✅ 通过 |
| 5 | /family/auth | PUT | 更新权限信息（开启所有权限） | ✅ 通过 |
| 6 | /family/auth | GET | 获取权限列表 | ✅ 通过 |
| 7 | /family/members/{id} | DELETE | 删除家属 | ✅ 通过 |

#### 前端功能测试

| 测试项 | 状态 |
|-------|------|
| 家庭协作页面加载 | ✅ 通过 |
| 添加家属弹窗 | ✅ 通过 |
| 修改家属信息弹窗 | ✅ 通过 |
| 设置权限弹窗 | ✅ 通过 |
| 删除家属 | ✅ 通过 |
| 弹窗可拖动 | ✅ 通过 |

### 11.6 问题记录

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 编辑家属返回500 | FamilyMemberDTO 中 familyUserId 使用 @NotNull 校验，编辑时不需要传递 | 修改 FamilyController.updateFamilyMember 方法，移除 @Valid 注解 |

### 11.7 查看家属提醒记录功能（追加）

#### 新增文件

| 文件路径 | 说明 |
|---------|------|
| `backend/src/main/java/com/medical/entity/ReminderLog.java` | 提醒日志实体类 |
| `backend/src/main/java/com/medical/repository/ReminderLogRepository.java` | 提醒日志数据访问层 |

#### 修改文件

| 文件路径 | 修改说明 |
|---------|---------|
| `backend/src/main/java/com/medical/service/FamilyService.java` | 新增 getFamilyReminderLogs 方法 |
| `backend/src/main/java/com/medical/controller/FamilyController.java` | 新增 GET /family/reminders/{memberId} 接口 |
| `frontend/src/api/family.js` | 新增 getFamilyReminders API封装 |
| `frontend/src/views/family/Family.vue` | 新增提醒记录弹窗、修复userId获取方式、添加删除确认弹窗 |

#### 功能特性（追加）

- ✅ 查看家属提醒记录（显示家属的用药提醒日志）
- ✅ 提醒记录列表展示（记录ID、提醒内容、渠道、状态、发送时间、接收时间）
- ✅ 提醒状态标签（待发送/已发送/已接收/已漏服/发送失败）
- ✅ userId获取方式优化（从userStore获取而非localStorage）
- ✅ 删除家属确认弹窗（防止误删）
- ✅ 编辑表单验证规则分离（新增/编辑使用不同验证规则）

#### 后端接口测试（追加）

| 序号 | 接口 | 方法 | 测试内容 | 状态 |
|------|------|------|---------|------|
| 8 | /family/reminders/{memberId} | GET | 查看家属提醒记录 | ✅ 通过 |

#### 前端功能测试（追加）

| 测试项 | 状态 |
|-------|------|
| 提醒记录弹窗显示 | ✅ 通过 |
| 提醒记录列表加载 | ✅ 通过 |
| 提醒状态标签颜色 | ✅ 通过 |
| 删除家属二次确认 | ✅ 通过 |
| userId正确获取 | ✅ 通过 |

#### 白盒测试发现

| 问题 | 等级 | 说明 |
|------|------|------|
| 提醒记录为空 | 低 | 新注册用户无提醒数据，符合预期 |
| userId获取方式 | 已修复 | 从userStore获取userId，避免localStorage不一致 |

### 11.8 待优化事项

1. ✅ 家庭协作基础功能（已完成）
2. ✅ 查看家属提醒记录（已完成）
3. 家属操作日志（审计追踪）
4. 批量添加家属功能
5. 家属权限模板（快速设置常用权限组合）
6. 提醒记录分页功能
7. 提醒记录筛选功能（按状态/时间范围）

---

### 11.9 搜索功能 + 权限级别联动 + UI升级（追加）

#### 新增功能

| 功能 | 说明 |
|------|------|
| 搜索家属 | 支持按姓名/电话/关系关键字搜索家属成员 |
| 查看所有提醒 | 查看所有已关联用户的家属的提醒记录汇总 |
| 权限级别联动 | 选择基础/高级权限时自动设置对应的6个具体权限 |
| 家属卡片UI升级 | 卡片式布局、彩色头像、统计卡片、悬浮动效 |
| 权限预览 | 添加/编辑家属时预览当前权限级别包含的功能 |

#### 修改文件清单

| 文件路径 | 修改说明 |
|---------|---------|
| `backend/src/main/java/com/medical/service/FamilyService.java` | 新增 `buildDefaultAuth()`、`updateAuthByPermissionLevel()` 方法，权限级别变更时自动同步具体权限；`getFamilyReminderLogs()` 未关联用户时返回空列表而非抛异常；新增 `getReminderLogsByFamilyUserId()` 方法 |
| `backend/src/main/java/com/medical/controller/FamilyController.java` | 新增 `GET /family/members/search` 搜索接口；新增 `GET /family/reminders` 查看所有家属提醒记录接口 |
| `backend/src/main/java/com/medical/dto/FamilyMemberDTO.java` | 移除 `familyUserId` 的 @NotNull 注解，添加 `realname` 和 `relation` 的 @NotBlank 注解 |
| `backend/src/main/java/com/medical/entity/ReminderLog.java` | 新增 `@Transient private String remark` 字段，用于前端显示家属姓名 |
| `backend/src/main/java/com/medical/service/ReminderLogService.java` | 新增（提醒日志服务） |
| `backend/src/main/java/com/medical/controller/ReminderLogController.java` | 新增（提醒日志控制器） |
| `frontend/src/views/family/Family.vue` | 全面UI升级：卡片式布局、统计卡片、搜索功能、权限预览、查看所有提醒、图标组件导入修复 |
| `frontend/src/api/family.js` | 新增 `searchFamilyMembers`、`getAllFamilyReminders` API 封装 |

#### 数据库变更

| 表 | 变更内容 |
|----|---------|
| familymember | `family_user_id` 字段改为允许 NULL（家属可以不关联用户账号） |

#### 权限级别说明

| 权限项 | 基础权限（basic） | 高级权限（advanced） |
|--------|------------------|---------------------|
| 查看病历记录 | ❌ 关 | ✅ 开 |
| 查看用药计划 | ✅ 开 | ✅ 开 |
| 查看统计数据 | ❌ 关 | ✅ 开 |
| 接收漏服提醒 | ✅ 开 | ✅ 开 |
| 接收紧急通知 | ❌ 关 | ✅ 开 |
| 接收断联提醒 | ❌ 关 | ✅ 开 |

#### UI升级说明

1. **页面头部**：渐变背景、品牌图标、圆角搜索框、渐变按钮
2. **统计卡片**：家属成员数、提醒记录数、高级权限数、正常状态数
3. **成员卡片**：彩色头像（根据姓名生成）、状态标签（在线/离线）、信息图标展示、悬浮动效
4. **对话框升级**：双栏表单、权限预览卡片、彩色标签
5. **交互体验**：卡片悬浮效果、渐变装饰条、空状态提示

#### 黑盒测试报告（追加）

| 序号 | 测试用例 | 测试内容 | 预期结果 | 状态 |
|------|---------|---------|---------|------|
| 1 | 添加家属（基础权限） | POST /family/members，permissionLevel=basic | 返回200，创建成功，默认权限为2开4关 | ✅ 通过 |
| 2 | 验证基础权限 | GET /family/auth/{memberId} | viewMedication=1, receiveMissAlert=1, 其他为0 | ✅ 通过 |
| 3 | 添加家属（高级权限） | POST /family/members，permissionLevel=advanced | 返回200，创建成功，默认权限为6全开 | ✅ 通过 |
| 4 | 验证高级权限 | GET /family/auth/{memberId} | 6个权限全部为1 | ✅ 通过 |
| 5 | 修改权限级别 | PUT /family/members/{id}，permissionLevel从basic改为advanced | 返回200，具体权限自动同步为全开 | ✅ 通过 |
| 6 | 验证权限同步 | GET /family/auth/{memberId} | 权限已更新为6全开 | ✅ 通过 |
| 7 | 搜索家属 | GET /family/members/search?keyword=测试 | 返回匹配的家属列表 | ✅ 通过 |
| 8 | 查看单个家属提醒（未关联用户） | GET /family/reminders/{memberId} | 返回空列表（不报错） | ✅ 通过 |
| 9 | 查看所有家属提醒 | GET /family/reminders | 返回所有已关联用户的家属提醒记录 | ✅ 通过 |
| 10 | 删除家属 | DELETE /family/members/{id} | 返回200，删除成功 | ✅ 通过 |

**测试汇总**：11项测试，11项通过，通过率100%

#### 白盒测试发现的问题

| 问题 | 等级 | 说明 | 状态 |
|------|------|------|------|
| 图标组件未导入 | 中 | Family.vue 使用了16个Element Plus图标但未导入，控制台有警告 | ✅ 已修复 |
| familyUserId必填约束不合理 | 中 | DTO中familyUserId使用@NotNull，导致无法添加未注册的家属 | ✅ 已修复 |
| 未关联用户时查看提醒报错 | 中 | familyUserId为null时抛出异常，用户体验差 | ✅ 已修复（返回空列表） |
| 权限级别与具体权限不同步 | 低 | 手动修改具体权限后，permissionLevel字段不会自动更新 | ⚠️ 待优化 |

#### 提醒记录测试数据

已向数据库插入8条提醒记录测试数据：

| 用户ID | 渠道 | 内容 | 状态 |
|--------|------|------|------|
| 1 | APP | 早上好！请记得服用降压药 | 已接收 |
| 1 | 短信 | 中午好！请记得服用降糖药 | 已接收 |
| 1 | APP | 晚上好！请记得服用阿司匹林 | 已发送 |
| 1 | 电话 | 用药提醒：您已超过30分钟未确认 | 已漏服 |
| 2 | APP | 请记得服用感冒药 | 已接收 |
| 2 | 短信 | 请记得服用维生素C | 待发送 |
| 2 | APP | 紧急提醒：您的血压监测数据异常 | 已接收 |
| 1 | 电话 | 设备断联提醒：智能药盒已离线 | 已接收 |

---

### 11.10 UI优化 + 权限弹窗修复 + 图标问题修复（追加）

#### 修改说明

根据用户反馈，对家庭协作页面进行以下优化：
1. 移除"在线/离线"状态标志
2. 权限设置弹窗按权限级别显示对应功能（基础权限只显示2项）
3. 美化提醒记录弹窗（渐变标题栏、统计卡片、漏服行高亮）
4. 美化权限设置弹窗（卡片式布局、权限级别标签）
5. 修复 Element Plus 图标导入问题

#### 修改文件清单

| 文件路径 | 修改说明 |
|---------|---------|
| `frontend/src/views/family/Family.vue` | 移除在线状态标签；权限弹窗按级别显示功能；美化提醒记录弹窗；美化权限弹窗；修复图标导入（CheckCircle→CircleCheck, Heart移除, Users→UserFilled） |

#### 功能特性（追加）

| 功能 | 说明 |
|------|------|
| 移除在线状态 | 家属卡片不再显示"在线/离线"状态标签 |
| 权限弹窗按级别显示 | 基础权限只显示2个功能（查看用药计划、接收漏服提醒），高级权限显示全部6个功能 |
| 提醒记录弹窗美化 | 渐变蓝色标题栏、统计卡片、成员信息展示、漏服行高亮、渠道标签、空状态提示 |
| 权限弹窗美化 | 权限级别标签、卡片式布局、间距优化 |
| 图标修复 | 修正不存在的图标名称（CheckCircle→CircleCheck, 移除Heart, Users→UserFilled） |

#### 权限弹窗功能显示规则

**基础权限（basic）** - 显示2个功能：
- ✅ 查看用药计划
- ✅ 接收漏服提醒

**高级权限（advanced）** - 显示6个功能：
- ✅ 查看病历记录
- ✅ 查看统计数据
- ✅ 查看用药计划
- ✅ 接收漏服提醒
- ✅ 接收紧急通知
- ✅ 接收断联提醒

#### 图标修复记录

| 原始图标 | 修复后 | 说明 |
|---------|--------|------|
| `CheckCircle` | `CircleCheck` | @element-plus/icons-vue 中不存在 CheckCircle |
| `Heart` | 移除 | 未使用且不存在 |
| `Users` | `UserFilled` | @element-plus/icons-vue 中不存在 Users，用 UserFilled 替代 |

#### 黑盒测试报告（追加）

| 序号 | 测试用例 | 测试内容 | 预期结果 | 状态 |
|------|---------|---------|---------|------|
| 11 | 页面加载无控制台错误 | 打开家庭协作页面 | 控制台无报错，页面正常渲染 | ✅ 通过 |
| 12 | 基础权限弹窗显示2项功能 | 添加家属选择基础权限，打开权限设置弹窗 | 只显示"查看用药计划"和"接收漏服提醒" | ✅ 通过 |
| 13 | 高级权限弹窗显示6项功能 | 添加家属选择高级权限，打开权限设置弹窗 | 显示全部6个权限功能 | ✅ 通过 |
| 14 | 编辑家属权限弹窗按级别显示 | 编辑基础权限家属，打开权限弹窗 | 只显示2项功能 | ✅ 通过 |
| 15 | 提醒记录弹窗美化效果 | 点击"提醒记录"按钮 | 显示渐变标题栏、统计卡片、渠道标签 | ✅ 通过 |
| 16 | 漏服记录高亮 | 查看包含漏服状态的提醒记录 | 漏服行背景变红高亮 | ✅ 通过 |
| 17 | 空提醒记录显示友好提示 | 查看未关联用户的家属提醒 | 显示"暂无提醒记录"友好提示 | ✅ 通过 |

**测试汇总**：17项测试，17项通过，通过率100%

#### 待优化事项（更新）

| 序号 | 待优化项 | 状态 |
|------|---------|------|
| 1 | ✅ 家庭协作基础功能（已完成） | ✅ |
| 2 | ✅ 查看家属提醒记录（已完成） | ✅ |
| 3 | ✅ 搜索功能（已完成） | ✅ |
| 4 | ✅ 权限级别联动（已完成） | ✅ |
| 5 | ✅ UI升级（已完成） | ✅ |
| 6 | ✅ 权限弹窗按级别显示（已完成） | ✅ |
| 7 | ✅ 提醒记录弹窗美化（已完成） | ✅ |
| 8 | ✅ 图标问题修复（已完成） | ✅ |
| 9 | 权限级别与具体权限双向同步 | ⚠️ 待优化 |
| 10 | 提醒记录分页功能 | ⚠️ 待优化 |
| 11 | 提醒记录筛选功能（按状态/时间范围） | ⚠️ 待优化 |
| 12 | 家属操作日志（审计追踪） | ⚠️ 待优化 |
| 13 | 批量添加家属功能 | ⚠️ 待优化 |

---
