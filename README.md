# CodeManage 程序竞赛管理系统

> 基于 Spring Boot + Vue 3 + Judge0 的程序设计竞赛与教学评测平台，支持管理员 / 教师 / 学生多角色，覆盖题库管理、竞赛管理、报名审核、代码在线评测、榜单统计等功能。

---

## 1. 项目简介

CodeManage 面向高校程序设计课程与各类编程竞赛场景，提供「教学 + 竞赛」一体化的在线评测解决方案：

- **多角色门户**：管理员 / 教师 / 学生三套前端界面
- **题库管理**：支持题目难度、标签、多测试点配置
- **竞赛管理**：支持报名、审核、按班级批量参赛、排行榜
- **在线评测**：对接 Judge0，支持 C / C++ / Java / Python 多语言评测
- **多测试点评测**：按测试点统计通过情况与时间 / 内存

详细架构与业务模块说明见：`文档/architecture.md`。

---

## 2. 技术栈概览

**后端：**

- Java 21
- Spring Boot 3.3.x（Spring Web）
- MyBatis + PageHelper 分页插件
- Hutool 工具包
- MySQL 8.x
- Judge0 在线判题服务

**前端：**

- Vue 3 + Vite 4
- Vue Router 4
- Element Plus（按需自动引入）
- Axios
- MarkdownIt（题面 / 竞赛说明 Markdown 渲染）
- CodeMirror 6 + vue-codemirror（代码编辑器）

---

## 3. 目录结构

```text
.
├─ springboot/        # 后端 Spring Boot 项目
├─ vue/               # 前端 Vue 3 + Vite 项目
├─ bsoj.sql           # 数据库结构及初始数据
├─ files/             # 后端文件存储目录（头像、附件等，含示例数据）
└─ 文档/              # 架构与部署文档
```

---

## 4. 快速开始（开发环境）

### 4.1 环境准备

- JDK 21
- Maven 3.8+
- Node.js 16+ / npm
- MySQL 8.x
- 可访问的 Judge0 服务（本地或远程实例）

### 4.2 初始化数据库

1. 创建数据库：

   ```sql
   CREATE DATABASE bsoj CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 导入根目录下的 `bsoj.sql` 脚本（可用命令行或图形化工具）。

### 4.3 配置后端

数据库密码、Judge0 地址与 JWT 密钥等**环境相关值**统一放在 `application-local.yml`。
该文件已被 `.gitignore` 排除（不会提交），需从模板创建：

```bash
cd springboot/src/main/resources
cp application-local.yml.example application-local.yml
```

然后编辑 `application-local.yml`，至少确认：

- `spring.datasource.password` 是你的 MySQL 密码
- `judge0.base-url` 指向可访问的 Judge0 服务（例如 `http://127.0.0.1:2358`）
- `jwt.secret` 为不少于 32 字节的随机字符串（可用 `openssl rand -base64 48` 生成）

`application.yml` 默认激活 `local` profile。其他环境可用环境变量覆盖：
`SPRING_PROFILES_ACTIVE`、`DB_USERNAME` / `DB_PASSWORD` / `DB_HOST` / `DB_PORT`、
`JUDGE0_BASE_URL`、`JWT_SECRET` / `JWT_EXPIRE_HOURS`。

更多后端配置细节见：`文档/configuration.md`。

### 4.4 启动后端

在 `springboot/` 目录执行：

```bash
mvn spring-boot:run
```

默认监听：`http://localhost:9090`。

### 4.5 配置并启动前端

在 `vue/` 目录：

1. 配置环境变量 `.env.development`：

   ```dotenv
   VITE_BASE_URL = 'http://localhost:9090'
   ```

2. 安装依赖并启动：

   ```bash
   npm install
   npm run dev
   ```

默认访问：`http://localhost:5173`。

### 4.6 默认测试账号

导入 `bsoj.sql` 后，可使用内置账号登录：

- 管理员：`admin / admin`
- 教师：`teacher / 123456`
- 学生：`student / 123456`

登录页支持一键填充上述示例账号。

---

## 5. 更多文档

- 架构与业务模块说明：`文档/architecture.md`
- 环境配置与部署说明：`文档/configuration.md`
