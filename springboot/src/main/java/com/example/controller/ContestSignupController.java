package com.example.controller;

import com.example.auth.RequireRole;
import com.example.auth.UserContext;
import com.example.common.Result;
import com.example.entity.ContestSignup;
import com.example.exception.ForbiddenException;
import com.example.service.ContestSignupService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 竞赛报名管理前端操作接口
 *
 * <p>报名归属与审核人身份一律取自 token，不接受客户端传入的身份字段。</p>
 */
@RestController
@RequestMapping("/signup")
public class ContestSignupController {

    @Resource
    private ContestSignupService contestSignupService;

    /** 学生报名 */
    @PostMapping("/apply")
    @RequireRole("STUDENT")
    public Result apply(@RequestBody ContestSignup signup) {
        // 报名人一律取当前登录学生，忽略客户端传入的 studentId
        signup.setStudentId(UserContext.currentId());
        contestSignupService.apply(signup);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result deleteById(@PathVariable Integer id) {
        contestSignupService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result updateById(@RequestBody ContestSignup signup) {
        contestSignupService.updateById(signup);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectById(@PathVariable Integer id) {
        ContestSignup signup = contestSignupService.selectById(id);
        if (signup != null && "STUDENT".equals(UserContext.currentRole())
                && !Objects.equals(signup.getStudentId(), UserContext.currentId())) {
            throw new ForbiddenException("无权查看他人报名信息");
        }
        return Result.success(signup);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectAll(ContestSignup signup) {
        // 学生只能看到本人的报名记录
        if ("STUDENT".equals(UserContext.currentRole())) {
            signup.setStudentId(UserContext.currentId());
        }
        List<ContestSignup> list = contestSignupService.selectAll(signup);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public Result selectPage(ContestSignup signup,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        if ("STUDENT".equals(UserContext.currentRole())) {
            signup.setStudentId(UserContext.currentId());
        }
        PageInfo<ContestSignup> page = contestSignupService.selectPage(signup, pageNum, pageSize);
        return Result.success(page);
    }

    /** 审核通过 */
    @PutMapping("/approve/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result approve(@PathVariable Integer id, @RequestBody ContestSignup signup) {
        // 审核人一律取当前登录用户，忽略客户端传入的 auditTeacherId / auditTeacherName
        contestSignupService.approve(id, UserContext.currentId(), UserContext.currentName(), signup.getRemark());
        return Result.success();
    }

    /** 审核拒绝 */
    @PutMapping("/reject/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result reject(@PathVariable Integer id, @RequestBody ContestSignup signup) {
        contestSignupService.reject(id, UserContext.currentId(), UserContext.currentName(), signup.getRemark());
        return Result.success();
    }

    /** 批量审核通过 */
    @PutMapping("/approveBatch")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result approveBatch(@RequestBody BatchAuditRequest request) {
        contestSignupService.approveBatch(request.getIds(), UserContext.currentId(),
                UserContext.currentName(), request.getRemark());
        return Result.success();
    }

    /** 批量审核拒绝 */
    @PutMapping("/rejectBatch")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result rejectBatch(@RequestBody BatchAuditRequest request) {
        contestSignupService.rejectBatch(request.getIds(), UserContext.currentId(),
                UserContext.currentName(), request.getRemark());
        return Result.success();
    }

    /**
     * 批量审核请求体。
     *
     * <p>不含审核人字段：审核人身份由服务端从 token 解析。
     * 前端若仍传入 auditTeacherId / auditTeacherName，会被 Jackson 忽略。</p>
     */
    public static class BatchAuditRequest {
        private List<Integer> ids;
        private String remark;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
