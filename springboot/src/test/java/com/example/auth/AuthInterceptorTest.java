package com.example.auth;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.auth.support.JwtTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuthInterceptor} 的行为测试。
 *
 * <p>用真实的 {@link HandlerMethod}（它的构造器是 public，无需 Mockito）配合
 * {@code MockHttpServletRequest/Response}，全程不依赖 Spring 上下文与数据库。</p>
 */
class AuthInterceptorTest {

    private final AuthInterceptor interceptor = new AuthInterceptor();

    @BeforeAll
    static void initJwt() {
        JwtTestSupport.init();
    }

    @AfterAll
    static void restoreJwt() {
        JwtTestSupport.init();
    }

    @BeforeEach
    void clearContext() {
        UserContext.clear();
    }

    // ================= 夹具：用于构造真实 HandlerMethod =================

    /** 类级 @Public，方法级 @RequireRole 覆盖 */
    @Public
    public static class PublicClassFixture {
        public void open() {
        }

        @RequireRole("STUDENT")
        public void studentOnly() {
        }
    }

    /** 类级 @RequireRole("ADMIN")，含方法级覆盖 */
    @RequireRole("ADMIN")
    public static class AdminClassFixture {
        public void adminOnly() {
        }

        @Public
        public void openOverride() {
        }

        @RequireRole("STUDENT")
        public void studentOverride() {
        }
    }

    /** 无任何注解 → 任意已登录角色即可 */
    public static class PlainFixture {
        public void anyAuthenticated() {
        }
    }

    private static HandlerMethod handlerFor(Object bean, String methodName) throws Exception {
        Method method = bean.getClass().getMethod(methodName);
        return new HandlerMethod(bean, method);
    }

    private static MockHttpServletRequest get(String uri) {
        return new MockHttpServletRequest("GET", uri);
    }

    private static String bearer(String role, int id) {
        return "Bearer " + JwtUtil.createToken(new LoginUser(id, "u" + id, "n" + id, role));
    }

    private static String codeOf(MockHttpServletResponse response) throws Exception {
        JSONObject body = JSONUtil.parseObj(response.getContentAsString(StandardCharsets.UTF_8));
        return body.getStr("code");
    }

    // ================= 放行 =================

    @Test
    void allowsCorsPreflightRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/admin/selectAll");
        request.addHeader("Origin", "http://localhost:5173");
        request.addHeader("Access-Control-Request-Method", "GET");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response,
                handlerFor(new AdminClassFixture(), "adminOnly"));

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200); // 未写出任何错误响应
    }

    @Test
    void doesNotTreatIncompleteOptionsRequestAsPreflight() throws Exception {
        // 只有 OPTIONS 与 Origin，缺 Access-Control-Request-Method → 不算预检
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/admin/selectAll");
        request.addHeader("Origin", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response,
                handlerFor(new AdminClassFixture(), "adminOnly"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void allowsNonHandlerMethod() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 静态资源等非 Controller 方法直接放行
        assertThat(interceptor.preHandle(get("/static/app.js"), response, new Object())).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void allowsMethodLevelPublicWithoutToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(get("/teacher/selectById/1"), response,
                handlerFor(new AdminClassFixture(), "openOverride"))).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void allowsClassLevelPublicWithoutToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(get("/files/download/a.png"), response,
                handlerFor(new PublicClassFixture(), "open"))).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void allowsAnyAuthenticatedRoleWhenNoRequireRolePresent() throws Exception {
        MockHttpServletRequest request = get("/files/upload");
        request.addHeader("Authorization", bearer("STUDENT", 3));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response,
                handlerFor(new PlainFixture(), "anyAuthenticated"))).isTrue();
        assertThat(UserContext.get()).isNotNull();
        assertThat(UserContext.currentRole()).isEqualTo("STUDENT");
    }

    // ================= 401 =================

    @Test
    void rejectsWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(get("/admin/selectAll"), response,
                handlerFor(new AdminClassFixture(), "adminOnly"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=utf-8");
        assertThat(codeOf(response)).isEqualTo("401");
    }

    @Test
    void rejectsHeaderWithoutBearerPrefix() throws Exception {
        MockHttpServletRequest request = get("/admin/selectAll");
        request.addHeader("Authorization", "not-a-bearer-header");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response,
                handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(codeOf(response)).isEqualTo("401");
    }

    @Test
    void rejectsGarbageToken() throws Exception {
        MockHttpServletRequest request = get("/admin/selectAll");
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response,
                handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(codeOf(response)).isEqualTo("401");
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        JwtTestSupport.init(JwtTestSupport.SECRET, -1);
        try {
            MockHttpServletRequest request = get("/admin/selectAll");
            request.addHeader("Authorization", bearer("ADMIN", 1));
            MockHttpServletResponse response = new MockHttpServletResponse();

            assertThat(interceptor.preHandle(request, response,
                    handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(codeOf(response)).isEqualTo("401");
        } finally {
            JwtTestSupport.init();
        }
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() throws Exception {
        JwtTestSupport.init("another-secret-key-0123456789abcdef", 12);
        String foreignToken;
        try {
            foreignToken = JwtUtil.createToken(new LoginUser(1, "a", "A", "ADMIN"));
        } finally {
            JwtTestSupport.init();
        }

        MockHttpServletRequest request = get("/admin/selectAll");
        request.addHeader("Authorization", "Bearer " + foreignToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response,
                handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    // ================= 403 =================

    @Test
    void rejectsRoleMismatchWithForbidden() throws Exception {
        MockHttpServletRequest request = get("/admin/selectAll");
        request.addHeader("Authorization", bearer("STUDENT", 5));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response,
                handlerFor(new AdminClassFixture(), "adminOnly"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(codeOf(response)).isEqualTo("403");
    }

    @Test
    void methodLevelRequireRoleOverridesClassLevel() throws Exception {
        // 类级是 ADMIN，方法级是 STUDENT → 学生可过、管理员被拒
        MockHttpServletRequest studentRequest = get("/teacher/selectById/1");
        studentRequest.addHeader("Authorization", bearer("STUDENT", 5));
        assertThat(interceptor.preHandle(studentRequest, new MockHttpServletResponse(),
                handlerFor(new AdminClassFixture(), "studentOverride"))).isTrue();

        MockHttpServletRequest adminRequest = get("/teacher/selectById/1");
        adminRequest.addHeader("Authorization", bearer("ADMIN", 1));
        MockHttpServletResponse adminResponse = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(adminRequest, adminResponse,
                handlerFor(new AdminClassFixture(), "studentOverride"))).isFalse();
        assertThat(adminResponse.getStatus()).isEqualTo(403);
    }

    @Test
    void classLevelPublicShortCircuitsMethodLevelRequireRole() throws Exception {
        // 固化优先级：@Public 在 @RequireRole 之前无条件判定并返回，
        // 因此类上标了 @Public 时，其方法上的 @RequireRole 不会生效。
        // 当前生产代码没有类级 @Public 的 Controller（@Public 只打在方法上），
        // 这里写明是为了避免将来误以为"方法级注解一定优先"。
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(get("/x"), response,
                handlerFor(new PublicClassFixture(), "studentOnly"))).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    // ================= ThreadLocal 清理 =================

    @Test
    void clearsThreadLocalAtEntrySoStaleIdentityNeverLeaks() throws Exception {
        UserContext.set(new LoginUser(999, "stale", "残留身份", "ADMIN"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(get("/admin/selectAll"), response,
                handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();

        assertThat(UserContext.get()).isNull();
    }

    @Test
    void forbiddenRequestDoesNotLeakIdentityIntoNextRequest() throws Exception {
        // 学生访问 ADMIN 专属接口 → 403，且 preHandle 已 set 过身份
        MockHttpServletRequest forbidden = get("/admin/selectAll");
        forbidden.addHeader("Authorization", bearer("STUDENT", 5));
        assertThat(interceptor.preHandle(forbidden, new MockHttpServletResponse(),
                handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();

        // 下一个请求即使不带 token，也不应读到上一个请求的身份
        assertThat(interceptor.preHandle(get("/admin/selectAll"), new MockHttpServletResponse(),
                handlerFor(new AdminClassFixture(), "adminOnly"))).isFalse();
        assertThat(UserContext.get()).isNull();
    }

    @Test
    void afterCompletionClearsThreadLocal() throws Exception {
        MockHttpServletRequest request = get("/admin/selectAll");
        request.addHeader("Authorization", bearer("ADMIN", 1));
        interceptor.preHandle(request, new MockHttpServletResponse(),
                handlerFor(new AdminClassFixture(), "adminOnly"));
        assertThat(UserContext.get()).isNotNull();

        interceptor.afterCompletion(request, new MockHttpServletResponse(),
                handlerFor(new AdminClassFixture(), "adminOnly"), null);

        assertThat(UserContext.get()).isNull();
    }
}
