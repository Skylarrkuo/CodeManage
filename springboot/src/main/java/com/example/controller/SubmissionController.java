package com.example.controller;

import com.example.common.Result;
import com.example.entity.Submission;
import com.example.service.SubmissionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码提交与评测前端操作接口。
 */
@RestController
@RequestMapping("/submission")
public class SubmissionController {

    @Resource
    private SubmissionService submissionService;

    /**
     * 提交代码并异步进行评测。
     *
     * 接口返回时：
     * - 已在数据库中创建一条提交记录，状态为 PENDING（排队中）
     * - 随后评测服务会在后台线程中将状态更新为 JUDGING/ACCEPTED/WRONG_ANSWER/ERROR 等
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody Submission submission) {
        submissionService.submit(submission);
        // 异步触发评测流程（不会阻塞当前请求）
        submissionService.evaluateAsync(submission.getId());
        // 返回当前提交对象（此时 status=PENDING，resultMsg 提示排队中）
        return Result.success(submission);
    }

    /**
     * 对单条提交记录进行重新评测（重测）。
     *
     * 教师、管理员可以在前端列表中触发该操作，学生也可以在自己的提交记录中发起重测（视前端权限控制而定）。
     */
    @PostMapping("/rejudge/{id}")
    public Result rejudge(@PathVariable Integer id) {
        submissionService.rejudge(id);
        // 重新异步评测
        submissionService.evaluateAsync(id);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        submissionService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    public Result updateById(@RequestBody Submission submission) {
        submissionService.updateById(submission);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Submission submission = submissionService.selectById(id);
        return Result.success(submission);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    public Result selectAll(Submission submission) {
        List<Submission> list = submissionService.selectAll(submission);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    public Result selectPage(Submission submission,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Submission> page = submissionService.selectPage(submission, pageNum, pageSize);
        return Result.success(page);
    }
}
