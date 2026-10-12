/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : bsoj

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 15/03/2026 19:30:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$BW.YIh5nuKW9tCpPfLjxr.AFczpgC3XVv1kF2a1mpOuEK6/FUgrla', '管理员', 'http://localhost:9090/files/download/avatar.png', 'ADMIN');

-- ----------------------------
-- Table structure for clazz
-- ----------------------------
DROP TABLE IF EXISTS `clazz`;
CREATE TABLE `clazz`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `major` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `clazz` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级信息（年级/专业/班级）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of clazz
-- ----------------------------
INSERT INTO `clazz` VALUES (1, '2024', '计算机科学与技术', '计科1班', '2024级 计科1班');
INSERT INTO `clazz` VALUES (2, '2024', '计算机科学与技术', '计科2班', '2024级 计科2班');
INSERT INTO `clazz` VALUES (3, '2023', '软件工程', '软工1班', '2023级 软工1班');

-- ----------------------------
-- Table structure for contest
-- ----------------------------
DROP TABLE IF EXISTS `contest`;
CREATE TABLE `contest`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '竞赛名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '竞赛简介',
  `level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '级别：校级/省级/国家级',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型：个人/团队',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态：DRAFT/PUBLISHED/SIGNUP/RUNNING/FINISHED',
  `sign_up_start_time` datetime NULL DEFAULT NULL COMMENT '报名开始时间',
  `sign_up_end_time` datetime NULL DEFAULT NULL COMMENT '报名结束时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '比赛开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '比赛结束时间',
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '程序设计竞赛' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contest
-- ----------------------------
INSERT INTO `contest` VALUES (1, '校级程序设计竞赛示例', '# 🧩 测试竞赛简介\n\n> 本比赛为 **系统联调与功能测试** 所设计，不计入正式排名，仅用于验证 **评测、提交、榜单、权限、接口** 等功能的可用性。\n\n---\n\n## 📘 比赛说明\n\n- **比赛名称**：系统联调示例赛（System Integration Contest）  \n- **比赛类型**：内部测试赛 / 样例竞赛  \n- **开始时间**：2026-03-01 00:00:00  \n- **结束时间**：长期开放（用于重复测试）\n- **参赛权限**：仅限管理员与测试账号加入\n\n---\n\n## 🧠 题目结构（示例）\n\n| 题号 | 题目名称 | 难度 | 分值 | 测试重点 |\n|:----:|:-----------|:------:|:------:|:----------|\n| A | Hello, OJ! | 🟢 简单 | 100 | 基础编译与输入输出测试 |\n| B | Simple Sort | 🟡 中等 | 100 | 时限测试 / 数组输入 |\n| C | Random Query | 🔴 困难 | 100 | 批量评测与数据范围验证 |\n\n---\n\n## 🧪 功能测试点\n\n- [x] 代码提交与编译结果显示  \n- [x] 评测机结果同步  \n- [x] 实时榜单刷新  \n- [x] 判题队列状态监控  \n- [ ] 测试异常处理（超时 / 运行时错误 / 编译错误）  \n- [ ] 用户权限与比赛状态切换  \n\n---\n\n## 📂 样例题展示\n\n### 示例题：Hello, OJ!\n\n#### 题目描述\n读取一行输入字符串并原样输出。\n\n**输入样例**\n```\nHello OJ\n```\n\n**输出样例**\n```\nHello OJ\n```\n\n---\n\n## 🧾 注意事项\n\n1. 提交前请确认语言配置与编译参数是否正确。  \n2. 比赛期间的评测数据可能清空或重置，不保证成绩可保存。  \n3. 此比赛中的题目仅作为 **系统联调示例**，不计入正式积分。  \n4. 可用于 UI、API、Webhook 测试验证。\n\n---\n\n## 🖇️ 版本信息\n- 文档版本：`v1.0.0`\n- 最后更新：2026-03-01  \n- 适用系统：OJ 平台测试环境 / 集成验证环境', '校级', '个人', 'SIGNUP', '2026-03-15 16:12:33', '2026-03-22 16:12:33', '2026-03-25 16:12:33', '2026-03-26 16:12:33', 1, '管理员', '2026-03-15 16:12:33', '2026-03-15 18:09:15');

-- ----------------------------
-- Table structure for contest_signup
-- ----------------------------
DROP TABLE IF EXISTS `contest_signup`;
CREATE TABLE `contest_signup`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `contest_id` int NOT NULL COMMENT '竞赛ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'APPLIED' COMMENT '报名状态：APPLIED/APPROVED/REJECTED',
  `apply_time` datetime NULL DEFAULT NULL COMMENT '报名时间',
  `audit_teacher_id` int NULL DEFAULT NULL COMMENT '审核教师ID',
  `audit_teacher_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核教师姓名',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contest_student`(`contest_id` ASC, `student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '竞赛报名信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contest_signup
-- ----------------------------
INSERT INTO `contest_signup` VALUES (1, 1, 1, 'APPLIED', '2026-03-15 18:08:12', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for problem
-- ----------------------------
DROP TABLE IF EXISTS `problem`;
CREATE TABLE `problem`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题目描述',
  `input_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输入描述',
  `output_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输出描述',
  `sample_input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '样例输入',
  `sample_output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '样例输出',
  `difficulty` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '难度',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `contest_id` int NULL DEFAULT NULL COMMENT '所属竞赛ID，为空表示练习题',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PRACTICE' COMMENT '类型：PRACTICE/CONTEST',
  `is_public` int NULL DEFAULT 1 COMMENT '是否在公开题库展示：1-展示，0-隐藏',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '编程题目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of problem
-- ----------------------------
INSERT INTO `problem` VALUES (1, 'A + B Problem', '给定两个整数 A 和 B，输出它们的和。', '输入包含一行，包括两个整数 A 和 B（-10^9 <= A,B <= 10^9）。', '输出一个整数，表示 A + B 的结果。', '1 2', '3', '简单', '入门,数学', NULL, 'PRACTICE', 1, '2026-03-15 16:12:33');

-- ----------------------------
-- Table structure for problem_test_case
-- ----------------------------
DROP TABLE IF EXISTS `problem_test_case`;
CREATE TABLE `problem_test_case`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `problem_id` int NOT NULL COMMENT '题目ID',
  `input_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输入数据',
  `output_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '期望输出',
  `is_sample` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否样例：1-样例，0-隐藏',
  `score` int NULL DEFAULT 0 COMMENT '测试点分值',
  `order_no` int NULL DEFAULT 1 COMMENT '显示/评测顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_problem`(`problem_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '编程题测试用例' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of problem_test_case
-- ----------------------------
INSERT INTO `problem_test_case` VALUES (1, 1, '1 2', '3', 1, 0, 1, '2026-03-15 16:12:33', '2026-03-15 17:10:40');
INSERT INTO `problem_test_case` VALUES (2, 1, '5 6', '11', 1, NULL, 2, '2026-03-15 18:07:02', '2026-03-15 18:07:01');
INSERT INTO `problem_test_case` VALUES (3, 1, '555 444', '999', 1, NULL, 3, '2026-03-15 18:07:02', '2026-03-15 18:07:01');
INSERT INTO `problem_test_case` VALUES (4, 1, '0 0', '0', 1, NULL, 4, '2026-03-15 18:07:21', '2026-03-15 18:07:21');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `major` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `clazz` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `student_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, 'student', '$2a$10$Te/rgZpIXEw6OgMnLMQqaeRA8tttIqpAybxkQd7a8o.CNVKbDLQ56', '学生用户', 'http://localhost:9090/files/download/1773573013503-a073c4fa-09cf-4c18-8360-a056afd5e66a_1747553792953195825~tplv-a9rns2rl98-web-watermark-v2.png', '20240001', '2024', '计算机科学与技术', '计科1班', 'STUDENT');
INSERT INTO `student` VALUES (2, 'stu2024a', '$2a$10$Te/rgZpIXEw6OgMnLMQqaeRA8tttIqpAybxkQd7a8o.CNVKbDLQ56', '张三', NULL, '20240002', '2024', '计算机科学与技术', '计科1班', 'STUDENT');
INSERT INTO `student` VALUES (3, 'stu2024b', '$2a$10$Te/rgZpIXEw6OgMnLMQqaeRA8tttIqpAybxkQd7a8o.CNVKbDLQ56', '李四', NULL, '20240003', '2024', '计算机科学与技术', '计科2班', 'STUDENT');
INSERT INTO `student` VALUES (4, 'stu2023a', '$2a$10$Te/rgZpIXEw6OgMnLMQqaeRA8tttIqpAybxkQd7a8o.CNVKbDLQ56', '王五', NULL, '20230001', '2023', '软件工程', '软工1班', 'STUDENT');

-- ----------------------------
-- Table structure for submission
-- ----------------------------
DROP TABLE IF EXISTS `submission`;
CREATE TABLE `submission`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `contest_id` int NULL DEFAULT NULL COMMENT '竞赛ID，可为空，表示练习提交',
  `problem_id` int NOT NULL COMMENT '题目ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
  `code` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '源代码',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评测状态',
  `score` int NULL DEFAULT NULL COMMENT '得分',
  `time_used` int NULL DEFAULT NULL COMMENT '耗时(ms)',
  `memory_used` int NULL DEFAULT NULL COMMENT '内存(KB)',
  `result_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '结果说明',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评测详情（JSON，包含各测试点结果）',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `judge_time` datetime NULL DEFAULT NULL COMMENT '评测时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代码提交记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of submission
-- ----------------------------
INSERT INTO `submission` VALUES (1, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ACCEPTED', 100, 4, 832, '评测通过（Judge0）：Accepted', NULL, '2026-03-15 16:14:03', '2026-03-15 16:57:21');
INSERT INTO `submission` VALUES (2, NULL, 1, 1, 'Python', 'A, B = map(int, input().split())\nprintf(A + B)\n', 'ACCEPTED', 100, 26, 3356, '评测通过（Judge0）：Accepted', NULL, '2026-03-15 16:15:07', '2026-03-15 16:57:19');
INSERT INTO `submission` VALUES (3, NULL, 1, 1, 'C', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ERROR', 0, 339, 0, '编译错误：\nmain.c:1:10: fatal error: iostream: No such file or directory\n    1 | #include <iostream>\n      |          ^~~~~~~~~~\ncompilation terminated.\n', NULL, '2026-03-15 16:20:36', '2026-03-15 16:57:23');
INSERT INTO `submission` VALUES (4, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ACCEPTED', 100, 5, 848, '评测通过（Judge0）：Accepted', NULL, '2026-03-15 16:20:48', '2026-03-15 16:57:13');
INSERT INTO `submission` VALUES (5, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ACCEPTED', 100, 5, 928, '通过所有 1 个测试用例。', '[{\"input\":\"1 2\",\"memory\":928,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 16:37:20', '2026-03-15 18:06:04');
INSERT INTO `submission` VALUES (6, NULL, 1, 1, 'C', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ERROR', 0, 545, 0, '编译错误：\nmain.c:1:10: fatal error: iostream: No such file or directory\n    1 | #include <iostream>\n      |          ^~~~~~~~~~\ncompilation terminated.\n', NULL, '2026-03-15 16:37:29', '2026-03-15 16:57:05');
INSERT INTO `submission` VALUES (7, NULL, 1, 1, 'C', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ERROR', 0, 545, 0, '编译错误：\nmain.c:1:10: fatal error: iostream: No such file or directory\n    1 | #include <iostream>\n      |          ^~~~~~~~~~\ncompilation terminated.\n', NULL, '2026-03-15 16:37:29', '2026-03-15 16:57:05');
INSERT INTO `submission` VALUES (8, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ACCEPTED', 100, 6, 976, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":928,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":6,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":816,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":976,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":876,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:06:26', '2026-03-15 18:07:32');
INSERT INTO `submission` VALUES (9, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}\n', 'ACCEPTED', 100, 5, 924, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":884,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":924,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":824,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":816,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:07:40', '2026-03-15 18:07:43');
INSERT INTO `submission` VALUES (10, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}', 'ACCEPTED', 100, 5, 924, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":924,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":904,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":924,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":884,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:15:44', '2026-03-15 18:15:49');
INSERT INTO `submission` VALUES (11, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}', 'ACCEPTED', 100, 5, 948, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":924,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":888,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":928,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":948,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:25:45', '2026-03-15 18:27:27');
INSERT INTO `submission` VALUES (12, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}', 'ACCEPTED', 100, 6, 924, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":812,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":6,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":924,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":904,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":820,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:27:39', '2026-03-15 18:31:37');
INSERT INTO `submission` VALUES (13, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}', 'ACCEPTED', 100, 6, 924, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":892,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":924,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":856,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":816,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":6,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:31:47', '2026-03-15 18:31:50');
INSERT INTO `submission` VALUES (14, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}', 'ACCEPTED', 100, 6, 924, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":832,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":6,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":920,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":924,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":820,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":4,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:31:58', '2026-03-15 18:37:01');
INSERT INTO `submission` VALUES (15, NULL, 1, 1, 'C++', '#include <iostream>\nusing namespace std;\n\nint main() {\n    int A, B;\n    cin >> A >> B;       // 从标准输入读取两个整数\n    cout << A + B << endl;  // 输出它们的和\n    return 0;\n}', 'ACCEPTED', 100, 5, 924, '通过所有 4 个测试用例。', '[{\"input\":\"1 2\",\"memory\":924,\"statusId\":3,\"index\":1,\"accepted\":true,\"expectedOutput\":\"3\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"5 6\",\"memory\":924,\"statusId\":3,\"index\":2,\"accepted\":true,\"expectedOutput\":\"11\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"555 444\",\"memory\":924,\"statusId\":3,\"index\":3,\"accepted\":true,\"expectedOutput\":\"999\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"},{\"input\":\"0 0\",\"memory\":924,\"statusId\":3,\"index\":4,\"accepted\":true,\"expectedOutput\":\"0\",\"time\":5,\"sample\":true,\"status\":\"Accepted\"}]', '2026-03-15 18:37:12', '2026-03-15 19:06:23');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `teacher_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, 'teacher', '$2a$10$Te/rgZpIXEw6OgMnLMQqaeRA8tttIqpAybxkQd7a8o.CNVKbDLQ56', '教师用户', 'http://localhost:9090/files/download/1773573002182-0e08cefbe8869d1518272aad8bfbc940.jpg', '讲师', '13800000000', 'teacher@example.com', 'TEACHER');

SET FOREIGN_KEY_CHECKS = 1;
