package com.example.auth;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.example.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证与授权拦截器。
 *
 * <p>流程：放行 CORS 预检与非 Controller 请求 → 放行 {@link Public} → 校验 token，
 * 失败返回 401 → 校验 {@link RequireRole}，失败返回 403。</p>
 *
 * <p>这里直接写响应而不抛异常：{@code GlobalExceptionHandler} 是基于
 * {@code basePackages} 的 {@code @ControllerAdvice}，对 preHandle 阶段抛出的异常匹配行为不确定。</p>
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 每个请求先清一次：preHandle 返回 false 时 Spring 不会回调 afterCompletion
        // （HandlerExecutionChain.applyPreHandle 判定失败时 interceptorIndex 仍为 -1），
        // 这里清一次可确保本线程不会沿用上一个请求残留的身份。
        UserContext.clear();

        // CORS 预检请求不携带业务身份，直接放行
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        // 非 Controller 方法（静态资源等）不做鉴权
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // @Public 接口免登录
        if (handlerMethod.hasMethodAnnotation(Public.class)
                || handlerMethod.getBeanType().isAnnotationPresent(Public.class)) {
            return true;
        }

        // 认证：解析 Bearer token
        LoginUser user = JwtUtil.parse(resolveToken(request));
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "登录已失效，请重新登录");
            return false;
        }
        UserContext.set(user);

        // 授权：方法级 @RequireRole 优先，其次类级
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !ArrayUtil.contains(requireRole.value(), user.getRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限执行该操作");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理，否则线程复用会导致后续请求沿用上一个用户的身份
        UserContext.clear();
    }

    /** 从 Authorization 头中取出 Bearer token */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    /** 直接写出指定 HTTP 状态码与统一 Result 结构 */
    private void writeError(HttpServletResponse response, int status, String msg) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(String.valueOf(status), msg)));
    }
}
