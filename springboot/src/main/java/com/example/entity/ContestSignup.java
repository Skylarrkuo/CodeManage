package com.example.entity;

import java.util.Date;

/**
 * 竞赛报名信息
 */
public class ContestSignup {

    private Integer id;
    /** 竞赛ID */
    private Integer contestId;
    /** 学生ID */
    private Integer studentId;
    /** 报名状态：APPLIED/APPROVED/REJECTED */
    private String status;
    /** 报名时间 */
    private Date applyTime;
    /** 审核教师ID */
    private Integer auditTeacherId;
    /** 审核教师姓名 */
    private String auditTeacherName;
    /** 审核时间 */
    private Date auditTime;
    /** 备注 */
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
        this.contestId = contestId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
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

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
