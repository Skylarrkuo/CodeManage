package com.example.controller;

import com.example.auth.RequireRole;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.exception.ForbiddenException;
import com.example.service.StudentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 学生前端操作接口
 *
 * <p>学生列表仅管理员与教师可查（学生端不提供「查看学生列表」功能）；
 * 学生本人可查看/修改自己的资料。自助注册走公开接口 {@code POST /register}。</p>
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Resource
    private StudentService studentService;

    /** 新增 */
    @PostMapping("/add")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result deleteById(@PathVariable Integer id) {
        studentService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     *
     * <p>学生只能修改本人资料；账号、角色、密码一律以数据库为准，
     * 防止通过资料接口把 role 改成 ADMIN 提权，或把密码置空导致无法登录。</p>
     */
    @PutMapping("/update")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result updateById(@RequestBody Student student) {
        Integer targetId = "STUDENT".equals(UserContext.currentRole())
                ? UserContext.currentId()
                : student.getId();
        if (targetId == null) {
            throw new CustomException("缺少学生ID");
        }
        Student db = studentService.selectById(targetId);
        if (db == null) {
            throw new CustomException("学生不存在");
        }
        student.setId(db.getId());
        student.setUsername(db.getUsername());
        student.setRole(db.getRole());
        student.setPassword(db.getPassword());
        studentService.updateById(student);
        return Result.success();
    }

    /**
     * 根据ID查询
     *
     * <p>学生仅能查看本人信息。</p>
     */
    @GetMapping("/selectById/{id}")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectById(@PathVariable Integer id) {
        if ("STUDENT".equals(UserContext.currentRole())
                && !Objects.equals(id, UserContext.currentId())) {
            throw new ForbiddenException("无权查看其他学生信息");
        }
        Student student = studentService.selectById(id);
        if (student != null) {
            student.setPassword(null);
        }
        return Result.success(student);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result selectAll(Student student) {
        List<Student> list = studentService.selectAll(student);
        list.forEach(item -> item.setPassword(null));
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result selectPage(Student student,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Student> page = studentService.selectPage(student, pageNum, pageSize);
        page.getList().forEach(item -> item.setPassword(null));
        return Result.success(page);
    }

    /**
     * 管理员重置学生密码为默认值 123456
     */
    @PutMapping("/resetPassword/{id}")
    @RequireRole("ADMIN")
    public Result resetPassword(@PathVariable Integer id) {
        studentService.resetPassword(id);
        return Result.success();
    }
}
