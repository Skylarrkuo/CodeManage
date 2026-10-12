package com.example.controller;

import com.example.auth.RequireRole;
import com.example.common.Result;
import com.example.entity.Problem;
import com.example.service.ProblemService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库管理前端操作接口
 *
 * <p>写操作限管理员与教师；查询三种角色均可访问（学生端题库需要读取题目）。</p>
 */
@RestController
@RequestMapping("/problem")
public class ProblemController {

    @Resource
    private ProblemService problemService;

    /** 新增 */
    @PostMapping("/add")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result add(@RequestBody Problem problem) {
        problemService.add(problem);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result deleteById(@PathVariable Integer id) {
        problemService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result updateById(@RequestBody Problem problem) {
        problemService.updateById(problem);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectById(@PathVariable Integer id) {
        Problem problem = problemService.selectById(id);
        return Result.success(problem);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectAll(Problem problem) {
        List<Problem> list = problemService.selectAll(problem);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectPage(Problem problem,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Problem> page = problemService.selectPage(problem, pageNum, pageSize);
        return Result.success(page);
    }
}
