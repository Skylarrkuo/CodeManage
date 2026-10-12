## CodeManage 环境配置与部署说明

本文件聚焦于运行环境、后端和前端配置、Judge0 对接方式以及开发 / 生产环境部署步骤。

---

## 1. 环境要求

### 1.1 软件版本建议

- **JDK**：21
- **Maven**：3.8+
- **MySQL**：8.0.x
- **Node.js**：16+（建议使用长期支持版本）
- **npm / pnpm / yarn**：任选一种包管理工具
- **Judge0**：独立部署或已有实例，要求提供 HTTP 接口访问

### 1.2 服务器与端口规划（示例）

- 后端 Spring Boot：`http://localhost:9090`
- 前端 Vite 开发服务器：`http://localhost:5173`
- Judge0：例如 `http://localhost:2358` 或远程地址 `http://your-judge0-host:2358`

生产部署时，可将前后端统一部署在同一域名下，并通过反向代理解决跨域问题。

---

## 2. 后端配置（application.yml）

后端主配置文件：`springboot/src/main/resources/application.yml`。

### 2.1 基础配置

```yaml
server:
  port: 9090

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: ${DB_PASSWORD:}
    url: jdbc:mysql://localhost:3306/bsoj?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true

fileBaseUrl: http://localhost

judge0:
  base-url: http://localhost:2358
  cpu-time-limit: 2
  language-id:
    c: 50
    cpp: 54
    java: 62
    python: 71
```

请根据实际环境调整以下部分：

#### 2.1.1 数据库连接

- `spring.datasource.url`：
  - 修改主机名、端口、库名等，例如：
  - `jdbc:mysql://192.168.1.10:3306/bsoj?...`
- `spring.datasource.username` / `password`：
  - 修改为实际数据库账号密码

#### 2.1.2 Multipart 上传限制

- 默认允许单个文件最大 100MB，适用于较大附件 / 头像等场景
- 如需缩小限制，可调整：

  ```yaml
  spring:
    servlet:
      multipart:
        max-file-size: 20MB
        max-request-size: 20MB
  ```

### 2.2 MyBatis 与日志

- `mapper-locations: classpath:mapper/*.xml`：加载 `springboot/src/main/resources/mapper/` 下 Mapper XML 文件
- `map-underscore-to-camel-case: true`：
  - 数据表下划线字段自动映射到 Java 驼峰字段
  - 如 `student_no` → `studentNo`
- `log-impl: StdOutImpl`：
  - 将 SQL 日志输出到控制台，开发和调试时非常有用

### 2.3 文件访问与存储

配置项：

- `fileBaseUrl`：文件 URL 基础域名（不含端口和路径）
- `server.port`：与文件 URL 中端口组合使用

文件控制器：`FileController`：

- 存储路径：

  ```java
  private static final String filePath = System.getProperty("user.dir") + "/files/";
  ```

  - 在后端进程工作目录下自动创建 `files/` 目录

- 上传：`POST /files/upload`
  - 返回 URL 格式：`${fileBaseUrl}:${server.port}/files/download/{fileName}`

- 下载：`GET /files/download/{fileName}`

> 部署时建议：
> - 将 `files/` 目录映射为宿主机卷，方便备份与迁移；
> - 将 `fileBaseUrl` 配置为你的域名（如 `https://oj.example.com`）。

### 2.4 Judge0 在线评测配置

配置块：

```yaml
judge0:
  base-url: http://your-judge0-host:2358
  cpu-time-limit: 2
  language-id:
    c: 50
    cpp: 54
    java: 62
    python: 71
```

说明：

- `base-url`：Judge0 实例的基础地址，不包含 `/submissions` 路径
- `cpu-time-limit`：单次评测 CPU 时间限制（秒）
- `language-id.*`：Judge0 中各语言的 `language_id`，需与 `/languages` 接口返回值对应

`Judge0Client` 会将 `base-url` 拼接为：

```text
{base-url}/submissions/?base64_encoded=false&wait=true
```

> 注意：路径中 `/submissions/` 末尾带 `/` 是官方推荐写法，可避免某些代理导致 301/302 重定向而丢失请求体。

### 2.5 CORS 跨域配置

类：`common.CorsConfig`：

```java
corsConfiguration.addAllowedOrigin("*");
corsConfiguration.addAllowedHeader("*");
corsConfiguration.addAllowedMethod("*");
```

说明：

- 当前配置允许任意来源、任意方法、任意请求头访问所有接口，方便前后端在开发阶段跨域调用
- 生产环境中，如前后端同域部署，可将 `*` 替换为具体的域名列表，以增强安全性

---

## 3. Judge0 部署与接入说明

### 3.1 Judge0 部署（简要）

推荐使用官方 Docker 镜像部署，例如：

```bash
docker run -d \
  -p 2358:2358 \
  judge0/judge0:latest
```

实际部署时可参考 Judge0 官方文档，配置 Redis、数据库、编译沙箱等。

### 3.2 语言 ID 校验

部署后访问：`GET {base-url}/languages`，检查各语言的 `id`：

- C    → 通常为 50
- C++  → 通常为 54
- Java → 通常为 62
- Python3 → 通常为 71

如与你当前配置不同，请修改 `application.yml` 中的 `judge0.language-id.*`，或在 `Judge0Client.mapLanguageId` 中调整映射逻辑。

### 3.3 常见错误排查

- **调用异常：连接失败 / 超时**
  - 检查 `judge0.base-url` 是否正确（协议、主机、端口）
  - 检查服务是否启动 / 防火墙是否放行端口

- **返回 422 Unprocessable Entity**
  - 请求参数非法，例如 `language_id` 无效
  - 尝试查看后端日志中 `在线评测服务拒绝本次请求（参数不合法）` 后的内容

- **编译错误**
  - Judge0 状态码为 6
  - 前端在提交记录的详情处可查看 `compileOutput`

- **答案错误**
  - Judge0 状态码为 4
  - 多测试点模式下，在前端提交详情中查看各测试点 `input`、`expectedOutput` 和 `stdout`

---

## 4. 前端配置

### 4.1 环境变量

前端通过 Vite 的环境变量机制配置后端接口地址：

`vue/.env.development` 示例：

```dotenv
VITE_BASE_URL = 'http://localhost:9090'
```

`vue/.env.production` 示例：

```dotenv
VITE_BASE_URL = 'https://oj.example.com'
```

说明：

- 变量名必须以 `VITE_` 开头，才能被前端代码访问
- 在 `src/utils/request.js` 中通过 `import.meta.env.VITE_BASE_URL` 读取

### 4.2 Vite 配置

`vue/vite.config.js` 要点：

- 别名：

  ```js
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  ```

- Element Plus 按需引入：

  ```js
  import AutoImport from 'unplugin-auto-import/vite'
  import Components from 'unplugin-vue-components/vite'
  import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver({ importStyle: 'sass' })] }),
    Components({ resolvers: [ElementPlusResolver({ importStyle: 'sass' })] }),
  ]
  ```

- 全局 SCSS 样式注入：

  ```js
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/css/index.scss" as *;`,
      },
    },
  },
  ```

### 4.3 Axios 封装

`src/utils/request.js`：

- `baseURL` 指向 `VITE_BASE_URL`
- 统一设置 `Content-Type`
- 响应拦截中：
  - 兼容字符串响应
  - 当返回 `code === '401'` 时，弹出错误提示并跳转 `/login`

---

## 5. 开发与部署流程

### 5.1 开发环境

1. 初始化数据库并导入 `bsoj.sql`。
2. 配置后端 `application.yml` 中的数据库和 Judge0 地址。
3. 在 `springboot/` 启动后端：

   ```bash
   mvn spring-boot:run
   ```

4. 在 `vue/` 启动前端开发服务器：

   ```bash
   npm install
   npm run dev
   ```

5. 浏览器访问前端地址，使用示例账号登录体验。

### 5.2 生产环境（参考）

1. **后端打包与部署**

   ```bash
   cd springboot
   mvn clean package
   ```

   将生成的 `springboot-0.0.1-SNAPSHOT.jar`（名称视实际 artifactId 而定）部署到服务器：

   ```bash
   java -jar springboot-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

   可以通过配置 `application-prod.yml` 单独管理生产环境配置。

2. **前端构建与部署**

   ```bash
   cd vue
   npm install
   npm run build
   ```

   将 `dist/` 目录交给 Nginx 或其他静态资源服务器托管。

   Nginx 示例配置片段：

   ```nginx
   server {
     listen 80;
     server_name oj.example.com;

     root /var/www/code-manage/dist;
     index index.html;

     location / {
       try_files $uri $uri/ /index.html;
     }

     location /api/ {
       proxy_pass http://127.0.0.1:9090/;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
     }
   }
   ```

   注意：可以通过前端 `.env.production` 中的 `VITE_BASE_URL` 控制是否使用反向代理前缀。

3. **Judge0 部署与安全**

   - 建议将 Judge0 仅暴露在内网，由后端访问，不直接面对公网
   - 如需公网访问，需配合防火墙与访问控制策略

4. **数据备份与日志**

   - 定期备份 MySQL 数据库
   - 如启用了文件上传，定期备份 `files/` 目录
   - 通过日志轮转机制（logrotate 等）管理应用日志大小

---

## 6. 附录：默认测试数据

导入 `bsoj.sql` 后，系统包含一些示例数据：

- 管理员：`admin / admin`
- 教师：`teacher / 123456`
- 学生：`student / 123456` 及其他示例学生
- 一场示例竞赛（带 Markdown 说明）
- 一道示例题目（A + B Problem）及多组测试用例
- 多条示例提交记录（包括 AC 与编译错误等场景）

这些数据可用于快速体验系统功能与联调评测流程。
