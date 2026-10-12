package com.example.controller;

import com.example.auth.AuthInterceptor;
import com.example.auth.JwtUtil;
import com.example.auth.LoginUser;
import com.example.auth.support.JwtTestSupport;
import com.example.entity.Student;
import com.example.entity.Teacher;
import com.example.exception.GlobalExceptionHandler;
import com.example.service.AdminService;
import com.example.service.ClazzService;
import com.example.service.StudentService;
import com.example.service.SubmissionService;
import com.example.service.TeacherService;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 角色授权规则矩阵（standalone MockMvc）。
 *
 * <p>用 {@link MockMvcBuilders#standaloneSetup} 起一个<b>不带 Spring 上下文</b>的 MockMvc：
 * 没有自动配置、没有 {@code @MapperScan}、没有 DataSource，但真实执行真实 Controller、
 * 真实 {@link AuthInterceptor}、真实 {@link GlobalExceptionHandler}。</p>
 *
 * <p>重点覆盖那些"用路径前缀推角色会判错"的接口：{@code /student/selectPage} 学生不可访问
 * 但教师可以；{@code /clazz/selectAll} 教师可用而 {@code /clazz/selectPage} 不行；
 * {@code /teacher/update} 教师可改自己。</p>
 */
class RoleRulesWebTest {

    private static MockMvc mvc;
    private static TeacherService teacherService;
    private static StudentService studentService;

    @BeforeAll
    static void setUp() {
        JwtTestSupport.init();

        AdminController adminController = new AdminController();
        ReflectionTestUtils.setField(adminController, "adminService", mock(AdminService.class));

        ClazzController clazzController = new ClazzController();
        ReflectionTestUtils.setField(clazzController, "clazzService", mock(ClazzService.class));

        teacherService = mock(TeacherService.class);
        TeacherController teacherController = new TeacherController();
        ReflectionTestUtils.setField(teacherController, "teacherService", teacherService);

        studentService = mock(StudentService.class);
        StudentController studentController = new StudentController();
        ReflectionTestUtils.setField(studentController, "studentService", studentService);

        SubmissionController submissionController = new SubmissionController();
        ReflectionTestUtils.setField(submissionController, "submissionService", mock(SubmissionService.class));

        FileController fileController = new FileController();

        mvc = MockMvcBuilders.standaloneSetup(
                        adminController, clazzController, teacherController,
                        studentController, submissionController, fileController)
                .addInterceptors(new AuthInterceptor())
                // standalone 不会自动注册 @ControllerAdvice，必须显式挂上
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static String token(String role, int id) {
        return "Bearer " + JwtUtil.createToken(new LoginUser(id, "u" + id, "n" + id, role));
    }

    // ================= 401：未登录 =================

    @Test
    void anonymousRequestIsRejectedWith401() throws Exception {
        mvc.perform(get("/admin/selectAll"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    // ================= /admin/** 仅 ADMIN =================

    @Test
    void adminEndpointsAreAdminOnly() throws Exception {
        mvc.perform(get("/admin/selectAll").header("Authorization", token("STUDENT", 5)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));

        mvc.perform(get("/admin/selectAll").header("Authorization", token("TEACHER", 2)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));

        mvc.perform(get("/admin/selectAll").header("Authorization", token("ADMIN", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    // ================= /student/**：列表仅 ADMIN + TEACHER，学生反而不可 =================

    @Test
    void studentListIsHiddenFromStudentsButVisibleToTeacher() throws Exception {
        // 学生不能查学生列表——用"前缀=角色"的规则会判错的地方
        mvc.perform(get("/student/selectPage").header("Authorization", token("STUDENT", 5)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));

        given(studentService.selectPage(any(), anyInt(), anyInt()))
                .willReturn(PageInfo.of(new ArrayList<Student>()));

        mvc.perform(get("/student/selectPage").header("Authorization", token("TEACHER", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void studentResetPasswordIsAdminOnly() throws Exception {
        // 教师端没有这个功能，只有管理员能重置密码
        mvc.perform(put("/student/resetPassword/3").header("Authorization", token("TEACHER", 2)))
                .andExpect(status().isForbidden());

        mvc.perform(put("/student/resetPassword/3").header("Authorization", token("STUDENT", 5)))
                .andExpect(status().isForbidden());
    }

    // ================= /teacher/update：类级 ADMIN，但方法级放行教师改自己 =================

    @Test
    void teacherUpdateIsAllowedForTeacherButNotForStudent() throws Exception {
        Teacher dbTeacher = new Teacher();
        dbTeacher.setId(2);
        dbTeacher.setUsername("u2");
        dbTeacher.setRole("TEACHER");
        dbTeacher.setPassword("$2a$hash");
        given(teacherService.selectById(2)).willReturn(dbTeacher);

        mvc.perform(put("/teacher/update")
                        .header("Authorization", token("TEACHER", 2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"name\":\"新名字\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        mvc.perform(put("/teacher/update")
                        .header("Authorization", token("STUDENT", 5))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"name\":\"越权\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherListIsAdminOnly() throws Exception {
        mvc.perform(get("/teacher/selectPage").header("Authorization", token("TEACHER", 2)))
                .andExpect(status().isForbidden());

        mvc.perform(get("/teacher/selectPage").header("Authorization", token("ADMIN", 1)))
                .andExpect(status().isOk());
    }

    // ================= /clazz/**：教师只能读下拉，不能管理 =================

    @Test
    void clazzSelectAllIsOpenToTeacherButSelectPageIsAdminOnly() throws Exception {
        mvc.perform(get("/clazz/selectAll").header("Authorization", token("TEACHER", 2)))
                .andExpect(status().isOk());

        mvc.perform(get("/clazz/selectPage").header("Authorization", token("TEACHER", 2)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));

        mvc.perform(get("/clazz/selectPage").header("Authorization", token("ADMIN", 1)))
                .andExpect(status().isOk());
    }

    // ================= /submission/submit 仅 STUDENT =================

    @Test
    void submissionSubmitIsStudentOnly() throws Exception {
        mvc.perform(post("/submission/submit")
                        .header("Authorization", token("ADMIN", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));

        mvc.perform(post("/submission/submit")
                        .header("Authorization", token("STUDENT", 5))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // ================= /files/download/** 免登录 =================

    @Test
    void fileDownloadIsPubliclyAccessible() throws Exception {
        // 头像以 <img src> 方式访问，浏览器不会带 Authorization 头。
        // 未被拦截即为通过；文件不存在时控制器抛出的异常由异常处理器转成 200 + 错误码，
        // 所以这里只断言"不是 401"。
        mvc.perform(get("/files/download/nonexistent.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not("401")));
    }
}
