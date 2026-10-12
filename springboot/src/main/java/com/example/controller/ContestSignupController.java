package com.example.controller;

import com.example.common.Result;
import com.example.entity.ContestSignup;
import com.example.service.ContestSignupService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 竞赛报名管理前端操作接口
 */
@RestController
@RequestMapping("/signup")
public class ContestSignupController {

    @Resource
    private ContestSignupService contestSignupService;

    /** 学生报名 */
    @PostMapping("/apply")
    public Result apply(@RequestBody ContestSignup signup) {
        contestSignupService.apply(signup);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        contestSignupService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    public Result updateById(@RequestBody ContestSignup signup) {
        contestSignupService.updateById(signup);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ContestSignup signup = contestSignupService.selectById(id);
        return Result.success(signup);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    public Result selectAll(ContestSignup signup) {
        List<ContestSignup> list = contestSignupService.selectAll(signup);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    public Result selectPage(ContestSignup signup,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ContestSignup> page = contestSignupService.selectPage(signup, pageNum, pageSize);
        return Result.success(page);
    }

    /** 审核通过 */
    @PutMapping("/approve/{id}")
    public Result approve(@PathVariable Integer id, @RequestBody ContestSignup signup) {
        contestSignupService.approve(id, signup.getAuditTeacherId(), signup.getAuditTeacherName(), signup.getRemark());
        return Result.success();
    }

    /** 审核拒绝 */
    @PutMapping("/reject/{id}")
    public Result reject(@PathVariable Integer id, @RequestBody ContestSignup signup) {
        contestSignupService.reject(id, signup.getAuditTeacherId(), signup.getAuditTeacherName(), signup.getRemark());
        return Result.success();
    }

    /** 批量审核通过 */
    @PutMapping("/approveBatch")
    public Result approveBatch(@RequestBody BatchAuditRequest request) {
        contestSignupService.approveBatch(request.getIds(), request.getAuditTeacherId(),
                request.getAuditTeacherName(), request.getRemark());
        return Result.success();
    }

    /** 批量审核拒绝 */
    @PutMapping("/rejectBatch")
    public Result rejectBatch(@RequestBody BatchAuditRequest request) {
        contestSignupService.rejectBatch(request.getIds(), request.getAuditTeacherId(),
                request.getAuditTeacherName(), request.getRemark());
        return Result.success();
    }

    /** 批量审核请求体 */
    public static class BatchAuditRequest {
        private List<Integer> ids;
        private Integer auditTeacherId;
        private String auditTeacherName;
        private String remark;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public Integer getAuditTeacherId() {
            return auditTeacherId;
        }

        public void setAuditTeacherId(Integer auditTeacherId) {
            this.auditTeacherId = auditTeacherId;
        }

        public String getAuditTeacherName() {
            return auditTeacherName;
        }

        public void setAuditTeacherName(String auditTeacherName) {
            this.auditTeacherName = auditTeacherName;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
