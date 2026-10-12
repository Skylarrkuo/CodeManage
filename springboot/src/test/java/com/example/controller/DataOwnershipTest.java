package com.example.controller;

import com.example.auth.LoginUser;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.ContestSignup;
import com.example.entity.ProblemTestCase;
import com.example.entity.Student;
import com.example.entity.Submission;
import com.example.entity.Teacher;
import com.example.exception.ForbiddenException;
import com.example.service.ContestSignupService;
import com.example.service.ProblemTestCaseService;
import com.example.service.StudentService;
import com.example.service.SubmissionService;
import com.example.service.TeacherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 数据归属校验：身份一律以 token 为准，客户端传入的身份字段被忽略或覆盖。
 *
 * <p>用纯单元测试（不启 Spring、不连库）：直接 {@code new XxxController()} 注入 mock service，
 * 设置 {@link UserContext} 后调用方法，再用 {@link ArgumentCaptor} 断言
 * <b>真正传给 service 的入参已被改写</b>——这比经 JSON 往返断言更精确。</p>
 */
@ExtendWith(MockitoExtension.class)
class DataOwnershipTest {

    private static final Integer CURRENT_STUDENT_ID = 7;
    private static final Integer CURRENT_TEACHER_ID = 2;

    @Mock
    private StudentService studentService;
    @Mock
    private TeacherService teacherService;
    @Mock
    private SubmissionService submissionService;
    @Mock
    private ContestSignupService contestSignupService;
    @Mock
    private ProblemTestCaseService problemTestCaseService;

    private StudentController studentController;
    private TeacherController teacherController;
    private SubmissionController submissionController;
    private ContestSignupController contestSignupController;
    private ProblemTestCaseController problemTestCaseController;

    @BeforeEach
    void setUp() {
        studentController = new StudentController();
        ReflectionTestUtils.setField(studentController, "studentService", studentService);

        teacherController = new TeacherController();
        ReflectionTestUtils.setField(teacherController, "teacherService", teacherService);

        submissionController = new SubmissionController();
        ReflectionTestUtils.setField(submissionController, "submissionService", submissionService);

        contestSignupController = new ContestSignupController();
        ReflectionTestUtils.setField(contestSignupController, "contestSignupService", contestSignupService);

        problemTestCaseController = new ProblemTestCaseController();
        ReflectionTestUtils.setField(problemTestCaseController, "problemTestCaseService", problemTestCaseService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private static void loginAsStudent() {
        UserContext.set(new LoginUser(CURRENT_STUDENT_ID, "stu7", "学生七", "STUDENT"));
    }

    private static void loginAsTeacher() {
        UserContext.set(new LoginUser(CURRENT_TEACHER_ID, "t2", "教师二", "TEACHER"));
    }

    // ================= 提权防护：/student/update =================

    @Test
    void studentUpdateIsForcedToSelfAndDbIdentity() {
        loginAsStudent();

        Student dbStudent = new Student();
        dbStudent.setId(CURRENT_STUDENT_ID);
        dbStudent.setUsername("stu7");
        dbStudent.setRole("STUDENT");
        dbStudent.setPassword("$2a$hash");
        given(studentService.selectById(CURRENT_STUDENT_ID)).willReturn(dbStudent);

        // 客户端伪造：改别人 id、把 role 提成 ADMIN、篡改账号、清空密码
        Student forged = new Student();
        forged.setId(999);
        forged.setUsername("hacked");
        forged.setRole("ADMIN");
        forged.setPassword(null);
        forged.setName("新名字");

        studentController.updateById(forged);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService).updateById(captor.capture());
        Student saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(CURRENT_STUDENT_ID);
        assertThat(saved.getUsername()).isEqualTo("stu7");
        assertThat(saved.getRole()).isEqualTo("STUDENT");
        assertThat(saved.getPassword()).isEqualTo("$2a$hash");
        // 允许修改的字段保留
        assertThat(saved.getName()).isEqualTo("新名字");
    }

    @Test
    void studentCannotReadAnotherStudent() {
        loginAsStudent();

        assertThatThrownBy(() -> studentController.selectById(1))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void studentCanReadSelf() {
        loginAsStudent();
        Student dbStudent = new Student();
        dbStudent.setId(CURRENT_STUDENT_ID);
        dbStudent.setPassword("$2a$hash");
        given(studentService.selectById(CURRENT_STUDENT_ID)).willReturn(dbStudent);

        Result result = studentController.selectById(CURRENT_STUDENT_ID);

        assertThat(result.getCode()).isEqualTo("200");
        // 返回体里的密码哈希被剪裁
        assertThat(((Student) result.getData()).getPassword()).isNull();
    }

    // ================= 提权防护：/teacher/update =================

    @Test
    void teacherUpdateIsForcedToSelfAndDbIdentity() {
        loginAsTeacher();

        Teacher dbTeacher = new Teacher();
        dbTeacher.setId(CURRENT_TEACHER_ID);
        dbTeacher.setUsername("t2");
        dbTeacher.setRole("TEACHER");
        dbTeacher.setPassword("$2a$hash");
        given(teacherService.selectById(CURRENT_TEACHER_ID)).willReturn(dbTeacher);

        Teacher forged = new Teacher();
        forged.setId(999);
        forged.setUsername("hacked");
        forged.setRole("ADMIN");
        forged.setPassword(null);

        teacherController.updateById(forged);

        ArgumentCaptor<Teacher> captor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherService).updateById(captor.capture());
        Teacher saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(CURRENT_TEACHER_ID);
        assertThat(saved.getUsername()).isEqualTo("t2");
        assertThat(saved.getRole()).isEqualTo("TEACHER");
        assertThat(saved.getPassword()).isEqualTo("$2a$hash");
    }

    // ================= 提交归属 =================

    @Test
    void submissionSubmitIgnoresClientSuppliedStudentId() {
        loginAsStudent();

        Submission submitted = new Submission();
        submitted.setStudentId(999); // 试图以他人身份提交
        submitted.setProblemId(1);

        submissionController.submit(submitted);

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionService).submit(captor.capture());
        assertThat(captor.getValue().getStudentId()).isEqualTo(CURRENT_STUDENT_ID);
    }

    @Test
    void studentCannotReadAnotherStudentsSubmission() {
        loginAsStudent();

        Submission other = new Submission();
        other.setId(1);
        other.setStudentId(999);
        given(submissionService.selectById(1)).willReturn(other);

        assertThatThrownBy(() -> submissionController.selectById(1))
                .isInstanceOf(ForbiddenException.class);
    }

    // ================= 报名归属 =================

    @Test
    void signupApplyIgnoresClientSuppliedStudentId() {
        loginAsStudent();

        ContestSignup signup = new ContestSignup();
        signup.setContestId(1);
        signup.setStudentId(999); // 试图替他人报名

        contestSignupController.apply(signup);

        ArgumentCaptor<ContestSignup> captor = ArgumentCaptor.forClass(ContestSignup.class);
        verify(contestSignupService).apply(captor.capture());
        assertThat(captor.getValue().getStudentId()).isEqualTo(CURRENT_STUDENT_ID);
    }

    // ================= 隐藏测试点不可见 =================

    @Test
    @SuppressWarnings("unchecked")
    void studentOnlySeesSampleTestCases() {
        loginAsStudent();
        given(problemTestCaseService.listByProblemId(1))
                .willReturn(new ArrayList<>(List.of(testCase(1, 1), testCase(2, 1), testCase(3, 0))));

        Result result = problemTestCaseController.list(1);

        List<ProblemTestCase> visible = (List<ProblemTestCase>) result.getData();
        assertThat(visible).hasSize(2).allMatch(tc -> tc.getIsSample() == 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void adminSeesAllTestCases() {
        UserContext.set(new LoginUser(1, "admin", "管理员", "ADMIN"));
        given(problemTestCaseService.listByProblemId(1))
                .willReturn(new ArrayList<>(List.of(testCase(1, 1), testCase(2, 1), testCase(3, 0))));

        Result result = problemTestCaseController.list(1);

        List<ProblemTestCase> visible = (List<ProblemTestCase>) result.getData();
        assertThat(visible).hasSize(3);
    }

    private static ProblemTestCase testCase(int id, int isSample) {
        ProblemTestCase testCase = new ProblemTestCase();
        testCase.setId(id);
        testCase.setProblemId(1);
        testCase.setIsSample(isSample);
        return testCase;
    }
}
