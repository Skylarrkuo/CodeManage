package com.example.controller;

import com.example.common.Result;
import com.example.entity.Problem;
import com.example.service.ProblemService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库管理前端操作接口
 */
@RestController
@RequestMapping("/problem")
public class ProblemController {

    @Resource
    private ProblemService problemService;

    /** 新增 */
    @PostMapping("/add")
    public Result add(@RequestBody Problem problem) {
        problemService.add(problem);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        problemService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    public Result updateById(@RequestBody Problem problem) {
        problemService.updateById(problem);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Problem problem = problemService.selectById(id);
        return Result.success(problem);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    public Result selectAll(Problem problem) {
        List<Problem> list = problemService.selectAll(problem);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    public Result selectPage(Problem problem,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Problem> page = problemService.selectPage(problem, pageNum, pageSize);
        return Result.success(page);
    }
}
