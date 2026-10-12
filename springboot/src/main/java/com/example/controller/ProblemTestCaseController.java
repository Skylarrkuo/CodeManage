package com.example.controller;

import com.example.common.Result;
import com.example.entity.ProblemTestCase;
import com.example.service.ProblemTestCaseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目测试用例管理前端操作接口
 */
@RestController
@RequestMapping("/problem-testcase")
public class ProblemTestCaseController {

    @Resource
    private ProblemTestCaseService problemTestCaseService;

    /** 按题目ID查询全部测试用例 */
    @GetMapping("/list")
    public Result list(@RequestParam Integer problemId) {
        List<ProblemTestCase> list = problemTestCaseService.listByProblemId(problemId);
        return Result.success(list);
    }

    /** 新增 */
    @PostMapping("/add")
    public Result add(@RequestBody ProblemTestCase testCase) {
        problemTestCaseService.add(testCase);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    public Result update(@RequestBody ProblemTestCase testCase) {
        problemTestCaseService.update(testCase);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        problemTestCaseService.delete(id);
        return Result.success();
    }
}
