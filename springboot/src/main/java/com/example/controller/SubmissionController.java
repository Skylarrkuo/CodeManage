package com.example.controller;

import com.example.auth.RequireRole;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.Submission;
import com.example.exception.ForbiddenException;
import com.example.service.SubmissionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 代码提交与评测前端操作接口。
 *
 * <p>提交人与查询范围一律以 token 中的身份为准：学生只能提交、查看、重测自己的记录，
 * 不能通过修改 URL 参数访问他人数据。</p>
 */
@RestController
@RequestMapping("/submission")
public class SubmissionController {

    @Resource
    private SubmissionService submissionService;

    /**
     * 提交代码并异步进行评测。
     *
     * <p>接口返回时提交记录状态为 PENDING（排队中），随后后台线程更新为
     * JUDGING / ACCEPTED / WRONG_ANSWER / ERROR。</p>
     */
    @PostMapping("/submit")
    @RequireRole("STUDENT")
    public Result submit(@RequestBody Submission submission) {
        // 提交人一律取当前登录学生，忽略客户端传入的 studentId
        submission.setStudentId(UserContext.currentId());
        submissionService.submit(submission);
        // 异步触发评测流程（不会阻塞当前请求）
        submissionService.evaluateAsync(submission.getId());
        return Result.success(submission);
    }

    /**
     * 对单条提交记录进行重新评测（重测）。
     */
    @PostMapping("/rejudge/{id}")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result rejudge(@PathVariable Integer id) {
        ensureOwnSubmissionIfStudent(id, "无权重测他人提交");
        submissionService.rejudge(id);
        // 重新异步评测
        submissionService.evaluateAsync(id);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    @RequireRole("ADMIN")
    public Result deleteById(@PathVariable Integer id) {
        submissionService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    @RequireRole("ADMIN")
    public Result updateById(@RequestBody Submission submission) {
        submissionService.updateById(submission);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectById(@PathVariable Integer id) {
        Submission submission = submissionService.selectById(id);
        if (submission != null && "STUDENT".equals(UserContext.currentRole())
                && !Objects.equals(submission.getStudentId(), UserContext.currentId())) {
            throw new ForbiddenException("无权查看他人提交");
        }
        return Result.success(submission);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectAll(Submission submission) {
        // 学生只能看到本人的提交记录
        if ("STUDENT".equals(UserContext.currentRole())) {
            submission.setStudentId(UserContext.currentId());
        }
        List<Submission> list = submissionService.selectAll(submission);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectPage(Submission submission,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        if ("STUDENT".equals(UserContext.currentRole())) {
            submission.setStudentId(UserContext.currentId());
        }
        PageInfo<Submission> page = submissionService.selectPage(submission, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 若当前用户是学生，则校验该提交记录确属本人，否则抛出 403。
     * 管理员与教师不受此限制。
     */
    private void ensureOwnSubmissionIfStudent(Integer submissionId, String message) {
        if (!"STUDENT".equals(UserContext.currentRole())) {
            return;
        }
        Submission db = submissionService.selectById(submissionId);
        if (db == null || !Objects.equals(db.getStudentId(), UserContext.currentId())) {
            throw new ForbiddenException(message);
        }
    }
}
