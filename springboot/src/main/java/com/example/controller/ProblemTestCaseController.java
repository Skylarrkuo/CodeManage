package com.example.controller;

import com.example.auth.RequireRole;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.ProblemTestCase;
import com.example.service.ProblemTestCaseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目测试用例管理前端操作接口
 *
 * <p>写操作限管理员与教师；学生可读取测试用例，但只能拿到样例测试点，
 * 隐藏测试点不下发到浏览器（评测在服务端由 Judge0 完成，无需前端持有）。</p>
 */
@RestController
@RequestMapping("/problem-testcase")
public class ProblemTestCaseController {

    @Resource
    private ProblemTestCaseService problemTestCaseService;

    /** 按题目ID查询测试用例 */
    @GetMapping("/list")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result list(@RequestParam Integer problemId) {
        List<ProblemTestCase> list = problemTestCaseService.listByProblemId(problemId);
        if ("STUDENT".equals(UserContext.currentRole())) {
            list = list.stream()
                    .filter(tc -> tc.getIsSample() != null && tc.getIsSample() == 1)
                    .toList();
        }
        return Result.success(list);
    }

    /** 新增 */
    @PostMapping("/add")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result add(@RequestBody ProblemTestCase testCase) {
        problemTestCaseService.add(testCase);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result update(@RequestBody ProblemTestCase testCase) {
        problemTestCaseService.update(testCase);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result delete(@PathVariable Integer id) {
        problemTestCaseService.delete(id);
        return Result.success();
    }
}
