package com.example.controller;

import com.example.auth.JwtUtil;
import com.example.auth.LoginUser;
import com.example.auth.Public;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.exception.UnauthorizedException;
import com.example.service.AdminService;
import com.example.service.StudentService;
import com.example.service.TeacherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private TeacherService teacherService;

    @Resource
    private StudentService studentService;


    /**
     * 默认请求接口
     */
    @Public
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     *
     * <p>请求体中的 role 仅用于选择账号表；签发 token 时一律使用数据库中的真实角色。</p>
     */
    @Public
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac;
        if ("ADMIN".equals(account.getRole())) {
            ac = adminService.login(account);
        } else if ("TEACHER".equals(account.getRole())) {
            ac = teacherService.login(account);
        } else if ("STUDENT".equals(account.getRole())) {
            ac = studentService.login(account);
        } else {
            throw new CustomException("非法的登录角色");
        }

        // 使用数据库中的角色签发 token
        ac.setToken(JwtUtil.createToken(
                new LoginUser(ac.getId(), ac.getUsername(), ac.getName(), ac.getRole())));
        // 不要把密码（含 BCrypt 哈希）回传给前端
        ac.setPassword(null);
        return Result.success(ac);
    }

    /**
     * 学生自助注册（公开接口）。
     *
     * <p>只接受学生基本信息，角色由后端强制为 STUDENT；
     * 不复用客户端传入的 id / role / avatar，避免注册即提权。</p>
     */
    @Public
    @PostMapping("/register")
    public Result register(@RequestBody Student student) {
        Student toCreate = new Student();
        toCreate.setUsername(student.getUsername());
        toCreate.setPassword(student.getPassword());
        toCreate.setName(student.getName());
        toCreate.setStudentNo(student.getStudentNo());
        toCreate.setGrade(student.getGrade());
        toCreate.setMajor(student.getMajor());
        toCreate.setClazz(student.getClazz());
        studentService.add(toCreate);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * <p>身份一律取自 token，覆盖客户端传入的 username / role，防止改他人密码。</p>
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        LoginUser current = UserContext.get();
        if (current == null) {
            throw new UnauthorizedException("未登录");
        }
        account.setUsername(current.getUsername());
        account.setRole(current.getRole());

        if ("ADMIN".equals(current.getRole())) {
            adminService.updatePassword(account);
        } else if ("TEACHER".equals(current.getRole())) {
            teacherService.updatePassword(account);
        } else if ("STUDENT".equals(current.getRole())) {
            studentService.updatePassword(account);
        }
        return Result.success();
    }

}
