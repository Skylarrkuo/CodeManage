## CodeManage 架构与业务模块说明

本文件主要说明系统整体架构、核心业务模块以及前后端主要目录结构，便于理解项目设计与二次开发。

---

## 1. 系统整体架构

### 1.1 分层结构

项目整体为一个前后端分离的单仓库：

- **后端（springboot/）**：
  - 提供 REST API，负责账户、题目、竞赛、报名、提交与评测等业务逻辑
  - 通过 MyBatis 访问 MySQL 数据库
  - 通过 `Judge0Client` 调用远程 Judge0 服务完成在线评测

- **前端（vue/）**：
  - 基于 Vue 3 + Vite + Element Plus
  - 提供管理员、教师、学生三套路由与界面
  - 通过 Axios 调用后端 REST 接口

- **外部依赖**：
  - MySQL 8.x：持久化用户、题库、竞赛、提交等业务数据
  - Judge0：独立的在线代码评测服务

### 1.2 主要业务流

以“学生做题并评测”为例：

1. 学生登录系统（前端 `/login` → 后端 `/login`）。
2. 在学生端题库或竞赛题目页面选择题目，进入「题目详情 + 代码编辑」页面。
3. 输入代码、选择语言，调用后端 `POST /submission/submit` 创建提交记录：
   - 后端写入一条 `submission` 记录，状态 `PENDING`。
   - 异步任务 `evaluateAsync` 将状态改为 `JUDGING`，并逐个测试点调用 Judge0。
4. Judge0 返回每个测试点的运行结果（时间、内存、状态、输出等）。
5. 后端统计通过情况，更新提交记录：
   - 测试点全部通过：`status = ACCEPTED`，`score = 100`；
   - 否则：`status = WRONG_ANSWER`，`score = 0`。
6. 前端通过轮询 `/submission/selectById/{id}` 刷新评测状态，并展示总分、耗时、内存及测试点详情。

---

## 2. 核心业务模块

### 2.1 账户与角色

涉及表：

- `admin`：管理员账号
- `teacher`：教师账号
- `student`：学生账号
- `clazz`：班级（年级 / 专业 / 班级信息）

典型字段（以 `student` 为例）：

- `username` / `password`：登录凭据
- `name` / `student_no`：姓名 / 学号
- `grade` / `major` / `clazz`：年级 / 专业 / 班级
- `role`：固定为 `STUDENT`

登录成功后，前端根据 `role` 跳转至不同首页：

- `ADMIN` → `/admin/home`
- `TEACHER` → `/teacher/home`
- `STUDENT` → `/student/home`

### 2.2 题库与题目测试用例

相关数据表：

- `problem`：编程题目
  - `title`：题目标题
  - `description`：题目描述（Markdown 文本）
  - `input_desc` / `output_desc`：输入 / 输出说明（Markdown）
  - `sample_input` / `sample_output`：样例输入输出（兼容旧逻辑）
  - `difficulty`：难度（简单 / 中等 / 困难）
  - `tags`：标签（字符串，可后续扩展为多标签）
  - `type`：`PRACTICE`（练习题）或 `CONTEST`（竞赛题）
  - `contest_id`：所属竞赛 ID（可空）
  - `is_public`：是否在公开题库展示（1 / 0）

- `problem_test_case`：测试用例（多测试点）
  - `problem_id`：关联题目 ID
  - `input_data` / `output_data`
  - `is_sample`：是否样例（1 表示在题面中展示）
  - `score`：测试点分值（当前实现整体 100 分，可扩展为按分值加权）
  - `order_no`：测试点顺序

后端相关类：

- `ProblemController`：`/problem` 路由，题目增删改查 + 分页
- `ProblemService` / `ProblemMapper`：题目业务逻辑与数据访问
- `ProblemTestCaseController` / `ProblemTestCaseService` / `ProblemTestCaseMapper`：测试用例管理

前端页面（学生端）：

- `views/student/Problem.vue`：题目列表
  - 支持练习模式和竞赛模式（通过 `contestId` 区分）
  - 练习模式下只展示 `is_public = 1` 的题目
- `views/student/ProblemDetail.vue`：题目详情 + 代码编辑 + 提交
  - 左侧：Markdown 渲染题面与样例
  - 右侧：CodeMirror 编辑器、语言选择、提交按钮、结果展示

Markdown 渲染由 `src/utils/markdown.js` 统一封装，禁用原始 HTML 以避免 XSS。

### 2.3 竞赛与报名

相关数据表：

- `contest`：竞赛
  - `name` / `description`（支持 Markdown）
  - `level`：校级 / 省级 / 国家级
  - `type`：个人 / 团队
  - `status`：`DRAFT` / `PUBLISHED` / `SIGNUP` / `RUNNING` / `FINISHED`
  - `sign_up_start_time` / `sign_up_end_time`：报名时间范围
  - `start_time` / `end_time`：比赛时间
  - `creator_id` / `creator_name`：创建人信息

- `contest_signup`：竞赛报名
  - `contest_id` / `student_id`
  - `status`：`APPLIED` / `APPROVED` / `REJECTED`
  - `apply_time` / `audit_time`
  - `audit_teacher_id` / `audit_teacher_name` / `remark`

后端控制器：

- `ContestController`：`/contest`
  - `/add` / `/delete/{id}` / `/update` / `/selectById/{id}` / `/selectPage`
  - `/assignClasses`：按班级批量添加参赛学生
  - `/rank/{contestId}`：获取竞赛排行榜（`ContestRankVO`）

- `ContestSignupController`：`/signup`
  - `/apply`：学生报名
  - `/approve/{id}` / `/reject/{id}`：单条审核
  - `/approveBatch` / `/rejectBatch`：批量审核

前端页面：

- 管理员端：`views/admin/Contest.vue` / `views/admin/Signup.vue`
- 教师端：`views/teacher/Contest.vue` / `views/teacher/Signup.vue`
- 学生端：`views/student/Contest.vue` / `views/student/ContestDetail.vue` / `views/student/Signup.vue`

### 2.4 提交与在线评测

关键数据表：

- `submission`：提交记录
  - `contest_id`：竞赛 ID（可空）
  - `problem_id` / `student_id`
  - `language`：C / C++ / Java / Python
  - `code`：源代码
  - `status`：`PENDING` / `JUDGING` / `ACCEPTED` / `WRONG_ANSWER` / `ERROR`
  - `score`：得分
  - `time_used` / `memory_used`
  - `result_msg`：结果说明（简要文本）
  - `detail`：测试点详情 JSON（多测试点时使用）
  - `submit_time` / `judge_time`

后端核心类：

- `SubmissionController`：`/submission`
  - `/submit`：提交代码（创建记录 + 异步评测）
  - `/rejudge/{id}`：重测
  - `/selectById/{id}`：查询单条提交（用于前端轮询）
  - `/selectPage`：分页查询提交记录

- `SubmissionService`：
  - `submit(Submission)`：创建提交记录，进行比赛合法性校验，状态置为 `PENDING`
  - `rejudge(Integer)`：将旧提交重置为 `PENDING`，再次进入评测队列
  - `@Async evaluateAsync(Integer)`：后台线程执行评测逻辑
  - `evaluateSubmission(Submission)`：
    - 根据 `problemId` 查题目
    - 如存在测试用例列表 → `judgeWithMultipleCases`
    - 否则使用 `Judge0Client.judge` 对样例进行单次评测
  - `judgeWithMultipleCases(Submission, List<ProblemTestCase>)`：
    - 遍历测试用例，调用 `Judge0Client.judgeOnce`
    - 统计通过数，记录最大耗时与内存
    - 构造测试点详情列表（状态、时间、内存、输入 / 期望输出、失败时的 stdout/stderr/compileOutput）
    - 全部通过 → `ACCEPTED` + `score = 100`；否则 → `WRONG_ANSWER` + `score = 0`

- `Judge0Client`：Judge0 HTTP 客户端
  - `judgeOnce`：对单个测试用例调用 `/submissions/?base64_encoded=false&wait=true`
  - `judge`：兼容旧逻辑，对单样例进行一次评测并直接回写 `Submission`
  - 内部通过配置文件注入 `base-url`、`cpu-time-limit`、各语言 `language-id`

前端页面（学生端）：

- `views/student/ProblemDetail.vue`：
  - 提交后保存返回的 `Submission` 对象
  - 以提交 ID 定时轮询 `/submission/selectById/{id}`
  - 在右侧结果栏展示状态、得分、耗时、内存、评测时间及 `result_msg`

- `views/student/Submission.vue`：
  - 按当前学生 ID 分页查询提交记录
  - 支持查看提交代码、各测试点详情
  - 支持对单条提交发起「重测」

---

## 3. 后端项目结构

后端主包：`springboot/src/main/java/com/example`，主要结构：

```text
com.example
├─ SpringbootApplication.java     # 启动类
├─ common/                        # 通用配置与响应封装
│  ├─ CorsConfig.java             # 全局跨域配置
│  └─ Result.java                 # 统一返回对象
├─ controller/                    # REST 控制器
├─ dto/                           # 排行榜等 DTO / VO
├─ entity/                        # 数据库实体类
├─ exception/                     # 自定义异常与全局异常处理
├─ judge/                         # 判题相关（Judge0 客户端等）
├─ mapper/                        # MyBatis Mapper 接口
└─ service/                       # 业务服务
```

关键点说明：

- **统一返回格式**：`common.Result` 封装 `code` / `msg` / `data`，前端统一解析。
- **分页支持**：通过 PageHelper 在 Service 中调用 `PageHelper.startPage` + `PageInfo.of`。
- **异常处理**：业务异常统一抛出 `CustomException`，由全局异常处理器转换为 `Result.error()` 响应。
- **跨域**：`CorsConfig` 允许所有源、所有方法、所有头部，开发调试方便；生产环境可按需收紧。

---

## 4. 前端项目结构

前端目录：`vue/src`，主要结构：

```text
src/
├─ main.js                  # 入口文件，创建应用、注册路由与 Element Plus
├─ App.vue
├─ router/
│  └─ index.js              # 路由配置（管理员 / 教师 / 学生）
├─ utils/
│  ├─ request.js            # Axios 封装（读取 VITE_BASE_URL）
│  └─ markdown.js           # Markdown 渲染封装
├─ assets/                  # 图片与样式
│  ├─ css/
│  └─ imgs/
└─ views/
   ├─ Login.vue
   ├─ Register.vue
   ├─ admin/...
   ├─ teacher/...
   ├─ student/...
   └─ common/ChangePassword.vue
```

路由划分（`router/index.js`）：

- `/admin`：管理员端布局 + 子路由（首页、账户管理、班级、竞赛、报名、题库、提交、改密等）
- `/teacher`：教师端布局 + 子路由（首页、竞赛、报名、题目、提交、学生、个人资料、改密）
- `/student`：学生端布局 + 子路由（首页、竞赛、竞赛详情、报名、题库、题目详情、提交记录、个人资料、改密）
- `/login` / `/register`：登录与注册

网络请求：

- 统一经 `utils/request.js` 创建的 Axios 实例：
  - `baseURL`：`import.meta.env.VITE_BASE_URL`
  - 统一设置 `Content-Type: application/json;charset=utf-8`
  - 统一处理 `401` 权限异常（跳转登录）

组件库：

- 通过 `unplugin-auto-import` + `unplugin-vue-components` + `ElementPlusResolver` 按需自动引入 Element Plus 组件和 API。

代码编辑器：

- 使用 `vue-codemirror` + CodeMirror 6 语言扩展（C++/Java/Python）实现题目代码编辑区。

---

## 5. 进一步阅读

- 项目环境与部署配置：`configuration.md`
