package com.example.controller;

import com.example.auth.RequireRole;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.Teacher;
import com.example.exception.CustomException;
import com.example.exception.ForbiddenException;
import com.example.service.TeacherService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 教师前端操作接口
 *
 * <p>账号管理（新增、删除、重置密码、列表查询）仅管理员可用；
 * 教师本人可查看与修改自己的资料。</p>
 */
@RestController
@RequestMapping("/teacher")
@RequireRole("ADMIN")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    /** 新增 */
    @PostMapping("/add")
    public Result add(@RequestBody Teacher teacher) {
        teacherService.add(teacher);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        teacherService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     *
     * <p>教师只能修改本人资料；账号、角色、密码一律以数据库为准，
     * 防止通过资料接口提权，或把密码置空导致无法登录。</p>
     */
    @PutMapping("/update")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result updateById(@RequestBody Teacher teacher) {
        Integer targetId = "TEACHER".equals(UserContext.currentRole())
                ? UserContext.currentId()
                : teacher.getId();
        if (targetId == null) {
            throw new CustomException("缺少教师ID");
        }
        Teacher db = teacherService.selectById(targetId);
        if (db == null) {
            throw new CustomException("教师不存在");
        }
        teacher.setId(db.getId());
        teacher.setUsername(db.getUsername());
        teacher.setRole(db.getRole());
        teacher.setPassword(db.getPassword());
        teacherService.updateById(teacher);
        return Result.success();
    }

    /**
     * 根据ID查询
     *
     * <p>教师仅能查看本人信息。</p>
     */
    @GetMapping("/selectById/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result selectById(@PathVariable Integer id) {
        if ("TEACHER".equals(UserContext.currentRole())
                && !Objects.equals(id, UserContext.currentId())) {
            throw new ForbiddenException("无权查看其他教师信息");
        }
        Teacher teacher = teacherService.selectById(id);
        if (teacher != null) {
            teacher.setPassword(null);
        }
        return Result.success(teacher);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    public Result selectAll(Teacher teacher) {
        List<Teacher> list = teacherService.selectAll(teacher);
        list.forEach(item -> item.setPassword(null));
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    public Result selectPage(Teacher teacher,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Teacher> page = teacherService.selectPage(teacher, pageNum, pageSize);
        page.getList().forEach(item -> item.setPassword(null));
        return Result.success(page);
    }

    /**
     * 管理员重置教师密码为默认值 123456
     */
    @PutMapping("/resetPassword/{id}")
    public Result resetPassword(@PathVariable Integer id) {
        teacherService.resetPassword(id);
        return Result.success();
    }
}
