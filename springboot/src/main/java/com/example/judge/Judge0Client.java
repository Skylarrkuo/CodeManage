package com.example.judge;

import com.example.entity.Problem;
import com.example.entity.Submission;
import com.example.entity.ProblemTestCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用 Judge0 在线评测服务进行代码判题的客户端。
 *
 * <p>不再依赖本机 gcc/javac/python 等命令，
 * 统一通过 HTTP 调用远程 Judge0 服务来完成编译与运行。</p>
 */
@Component
public class Judge0Client {

    /**
     * Judge0 服务基础地址，例如：
     *   http://localhost:2358
     *
     * 注意：末尾不要带 /submissions，下面会自动拼接 /submissions?...
     */
    @Value("${judge0.base-url}")
    private String baseUrl;

    /** 单次评测的时间限制（秒） */
    @Value("${judge0.cpu-time-limit:2}")
    private double cpuTimeLimitSeconds;

    // 各语言在 Judge0 实例中的 language_id，支持通过 application.yml 配置覆盖
    @Value("${judge0.language-id.c:50}")
    private Integer cLanguageId;

    @Value("${judge0.language-id.cpp:54}")
    private Integer cppLanguageId;

    @Value("${judge0.language-id.java:62}")
    private Integer javaLanguageId;

    @Value("${judge0.language-id.python:71}")
    private Integer pythonLanguageId;

    private final RestTemplate restTemplate = new RestTemplate();

    /** 评测通过的默认得分 */
    private static final int FULL_SCORE = 100;

    /**
     * 使用 Judge0 对单个测试用例进行评测，返回精简结果。
     */
    public Judge0Result judgeOnce(String sourceCode,
                                  String language,
                                  String stdin,
                                  String expectedOutput) {
        Judge0Result result = new Judge0Result();

        if (language == null || language.trim().isEmpty()) {
            result.setStatusId(-1);
            result.setStatusDesc("未指定编程语言");
            return result;
        }
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            result.setStatusId(-1);
            result.setStatusDesc("代码不能为空");
            return result;
        }

        Integer languageId = mapLanguageId(language);
        if (languageId == null) {
            result.setStatusId(-1);
            result.setStatusDesc("不支持的语言类型：" + language);
            return result;
        }

        String endpoint = buildSubmissionsEndpoint();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("source_code", sourceCode);
        form.add("language_id", String.valueOf(languageId));
        form.add("stdin", stdin == null ? "" : stdin);
        form.add("expected_output", expectedOutput == null ? "" : expectedOutput);
        form.add("cpu_time_limit", String.valueOf(cpuTimeLimitSeconds));
        form.add("redirect_stderr_to_stdout", "true");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        long start = System.currentTimeMillis();
        Judge0Response resp;
        try {
            ResponseEntity<Judge0Response> responseEntity = restTemplate.postForEntity(
                    endpoint,
                    entity,
                    Judge0Response.class
            );
            resp = responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 422) {
                String bodyText = e.getResponseBodyAsString();
                result.setStatusId(422);
                result.setStatusDesc("在线评测服务拒绝本次请求（参数不合法）：" + safeLimit(bodyText));
            } else {
                result.setStatusId(e.getStatusCode().value());
                result.setStatusDesc("调用在线评测服务异常：" + e.getStatusCode().value() + " " + e.getStatusText());
            }
            return result;
        } catch (RestClientException e) {
            result.setStatusId(-1);
            result.setStatusDesc("调用在线评测服务异常：" + e.getMessage());
            return result;
        }
        long end = System.currentTimeMillis();

        if (resp == null) {
            result.setStatusId(-1);
            result.setStatusDesc("在线评测服务返回空结果");
            return result;
        }

        if (resp.time != null) {
            result.setTime(resp.time);
        } else {
            result.setTime((end - start) / 1000.0);
        }
        result.setMemory(resp.memory);

        if (resp.status != null) {
            result.setStatusId(resp.status.id);
            result.setStatusDesc(resp.status.description);
        } else {
            result.setStatusId(-1);
            result.setStatusDesc("在线评测服务未返回状态");
        }

        result.setStdout(resp.stdout);
        result.setStderr(resp.stderr);
        result.setCompileOutput(resp.compile_output);
        result.setMessage(resp.message);

        return result;
    }

    /**
     * 兼容旧逻辑：使用题目上的样例输入/输出进行单次评测，并直接回写 submission。
     */
    public void judge(Submission submission, Problem problem) {
        submission.setStatus("ERROR");
        submission.setScore(0);
        submission.setTimeUsed(0);
        submission.setMemoryUsed(0);

        Judge0Result r = judgeOnce(
                submission.getCode(),
                submission.getLanguage(),
                problem == null ? "" : problem.getSampleInput(),
                problem == null ? "" : problem.getSampleOutput()
        );

        // 如果是本地参数错误
        if (r.getStatusId() != null && r.getStatusId() == -1) {
            submission.setResultMsg("评测失败：" + r.getStatusDesc());
            return;
        }

        int timeMs = r.getTime() != null ? (int) Math.round(r.getTime() * 1000) : 0;
        int memKb = r.getMemory() != null ? r.getMemory().intValue() : 0;
        submission.setTimeUsed(timeMs);
        submission.setMemoryUsed(memKb);

        Integer statusId = r.getStatusId();
        String statusDesc = r.getStatusDesc();
        if (statusId == null) {
            submission.setResultMsg("评测失败：" + statusDesc);
            return;
        }

        switch (statusId) {
            case 3: // Accepted
                submission.setStatus("ACCEPTED");
                submission.setScore(FULL_SCORE);
                submission.setResultMsg("评测通过（Judge0）：" + safeLimit(statusDesc));
                break;
            case 4: // Wrong Answer
                submission.setStatus("WRONG_ANSWER");
                submission.setScore(0);
                submission.setResultMsg("答案错误：\n期望输出：\n" +
                        safeLimit(problem == null ? "" : problem.getSampleOutput()) +
                        "\n实际输出：\n" + safeLimit(r.getStdout()));
                break;
            case 5: // Time Limit Exceeded
                submission.setStatus("ERROR");
                submission.setScore(0);
                submission.setResultMsg("运行超时：" + safeLimit(statusDesc));
                break;
            case 6: // Compilation Error
                submission.setStatus("ERROR");
                submission.setScore(0);
                submission.setResultMsg("编译错误：\n" + safeLimit(r.getCompileOutput()));
                break;
            default:
                submission.setStatus("ERROR");
                submission.setScore(0);
                String detail = r.getStderr() != null ? r.getStderr() : r.getMessage();
                if (detail == null || detail.isEmpty()) {
                    detail = statusDesc;
                }
                submission.setResultMsg("运行错误/系统错误（" + statusDesc + ")：\n" + safeLimit(detail));
                break;
        }
    }

    /**
     * Judge0 语言 ID 映射。
     *
     * 这里使用的是 Judge0 默认的部分语言 ID：
     * C (GCC)      -> 50
     * C++ (G++)    -> 54
     * Java (OpenJDK)-> 62
     * Python 3     -> 71
     * 如你的 Judge0 配置有所不同，请在此处调整对应的 ID。
     */
    private Integer mapLanguageId(String language) {
        if (language == null) {
            return null;
        }
        String lang = language.trim().toLowerCase();
        if ("c".equals(lang)) {
            return cLanguageId; // C
        } else if ("c++".equals(lang) || "cpp".equals(lang) || "c/c++".equals(lang)) {
            return cppLanguageId; // C++
        } else if ("java".equals(lang)) {
            return javaLanguageId; // Java
        } else if (lang.startsWith("python")) {
            return pythonLanguageId; // Python
        }
        return null;
    }

    /**
     * 拼接 /submissions/?base64_encoded=false&wait=true 端点。
     *
     * 按 Judge0 官方文档推荐使用末尾带 "/" 的 /submissions/ 路径，
     * 以避免在某些反向代理或网关下产生 301/302 重定向，从而导致请求体丢失，
     * 进而让 Judge0 认为 source_code / language_id 为空。
     */
    private String buildSubmissionsEndpoint() {
        String trimmed = baseUrl == null ? "" : baseUrl.trim();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        // 标准 Judge0 路径为 /submissions/，必要时可根据实际部署情况改为 /judge0/submissions/ 等
        return trimmed + "/submissions/?base64_encoded=false&wait=true";
    }

    private String safeLimit(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() <= 8000) {
            return s;
        }
        return s.substring(0, 8000) + "\n...";
    }

    /**
     * 用于接收 Judge0 的 JSON 响应。
     */
    public static class Judge0Response {
        public String stdout;
        public String stderr;
        public String compile_output;
        public Double time; // 秒
        public Long memory; // KB
        public Status status;
        public String message;

        public static class Status {
            public Integer id;
            public String description;
        }
    }

    /**
     * 多测试点评测时使用的精简结果封装。
     */
    public static class Judge0Result {
        private Integer statusId;
        private String statusDesc;
        private String stdout;
        private String stderr;
        private String compileOutput;
        private Double time;
        private Long memory;
        private String message;

        public boolean isAccepted() {
            return statusId != null && statusId == 3;
        }

        public Integer getStatusId() {
            return statusId;
        }

        public void setStatusId(Integer statusId) {
            this.statusId = statusId;
        }

        public String getStatusDesc() {
            return statusDesc;
        }

        public void setStatusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
        }

        public String getStdout() {
            return stdout;
        }

        public void setStdout(String stdout) {
            this.stdout = stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public void setStderr(String stderr) {
            this.stderr = stderr;
        }

        public String getCompileOutput() {
            return compileOutput;
        }

        public void setCompileOutput(String compileOutput) {
            this.compileOutput = compileOutput;
        }

        public Double getTime() {
            return time;
        }

        public void setTime(Double time) {
            this.time = time;
        }

        public Long getMemory() {
            return memory;
        }

        public void setMemory(Long memory) {
            this.memory = memory;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
