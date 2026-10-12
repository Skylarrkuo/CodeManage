package com.example.controller;

import com.example.auth.Public;
import com.example.auth.RequireRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * fail-closed 守卫：每个接口都必须显式声明自己的鉴权规则。
 *
 * <p>拦截器的默认策略是"未标注 @RequireRole 即任意已登录角色可访问"。
 * 本测试遍历所有 Controller 的映射方法，要求每个方法都被方法级或类级的
 * {@link Public} / {@link RequireRole} 覆盖——将来新增接口忘了打注解会在这里失败。</p>
 *
 * <p>不设白名单：{@code WebController.updatePassword} 与 {@code FileController.upload}
 * 这类"任意已登录即可"的接口也已经改成显式标注，规则没有例外。</p>
 */
class ControllerAuthCoverageTest {

    private static boolean isRequestMapped(Method method) {
        return method.isAnnotationPresent(RequestMapping.class)
                || method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class)
                || method.isAnnotationPresent(PatchMapping.class);
    }

    @Test
    void everyMappedEndpointDeclaresItsAuthRule() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> offenders = new ArrayList<>();
        int mappedMethodCount = 0;

        for (BeanDefinition definition : scanner.findCandidateComponents("com.example.controller")) {
            Class<?> controller = Class.forName(definition.getBeanClassName());
            boolean classCovered = controller.isAnnotationPresent(Public.class)
                    || controller.isAnnotationPresent(RequireRole.class);

            for (Method method : controller.getDeclaredMethods()) {
                if (!isRequestMapped(method)) {
                    continue;
                }
                mappedMethodCount++;
                boolean covered = classCovered
                        || method.isAnnotationPresent(Public.class)
                        || method.isAnnotationPresent(RequireRole.class);
                if (!covered) {
                    offenders.add(controller.getSimpleName() + "#" + method.getName());
                }
            }
        }

        // 反空扫：万一扫描失效导致一个方法都没扫到，也不该"假绿"
        assertThat(mappedMethodCount)
                .as("扫描到的映射方法数（防止扫描失效）")
                .isGreaterThanOrEqualTo(60);
        assertThat(offenders)
                .as("缺少 @Public / @RequireRole 的接口")
                .isEmpty();
    }
}
