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
- [ ] 1.2 注册百度智能云账号（领取免费额度）
- [ ] 1.3 初始化后端项目（Spring Boot）
- [ ] 1.4 初始化前端项目（Vue 3 + Vite）
- [ ] 1.5 创建数据库（执行 DATABASE.md 中的建表语句）
- [ ] 1.6 练习 Git 协作流程（3人都要跑通）

#### 输出物

- 📄 `DATABASE.md`（数据库设计文档）✅ 已完成
- 📄 `DEVELOPMENT_PLAN.md`（本文件）✅ 已完成
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

### 今天要完成的事

1. **注册百度智能云账号**（必须）
   - 访问：https://console.bce.baidu.com/ai
   - 开通OCR服务：https://ai.baidu.com/tech/ocr
   - 开通文心一言API
   - 保存 API Key 和 Secret

2. **环境准备**
   - 安装 IntelliJ IDEA (后端开发)
   - 安装 VS Code (前端开发)
   - 安装 JDK 17+
   - 安装 MySQL 8.0
   - 安装 Node.js 18 LTS（你当前已是18.19.0，无需更换）

3. **项目初始化**（分配任务）
   - 同学A：负责初始化后端项目（Spring Boot骨架）
   - 同学B：负责初始化前端项目（Vue 3 + Vite）
   - 同学C（你）：负责创建数据库、提交到GitHub仓库

4. **Git协作演练**
   - 3人都要跑通：创建分支 → 提交 → 合并 → 推送流程

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

*最后更新：2026-06-26*
