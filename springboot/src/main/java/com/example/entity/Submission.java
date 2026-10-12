package com.example.entity;

import java.util.Date;

/**
 * 代码提交记录
 */
public class Submission {

    private Integer id;
    /** 竞赛ID，可为空，表示练习提交 */
    private Integer contestId;
    /** 题目ID */
    private Integer problemId;
    /** 学生ID */
    private Integer studentId;
    /** 编程语言 */
    private String language;
    /** 源代码 */
    private String code;
    /** 评测状态：PENDING/JUDGING/ACCEPTED/WRONG_ANSWER/ERROR 等 */
    private String status;
    /** 得分 */
    private Integer score;
    /** 耗时（ms） */
    private Integer timeUsed;
    /** 内存（KB） */
    private Integer memoryUsed;
    /** 结果说明 */
    private String resultMsg;
    /** 评测详情（JSON，包含各个测试用例的结果） */
    private String detail;
    /** 提交时间 */
    private Date submitTime;
    /** 评测时间 */
    private Date judgeTime;

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

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(Integer timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Integer getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Integer memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getJudgeTime() {
        return judgeTime;
    }

    public void setJudgeTime(Date judgeTime) {
        this.judgeTime = judgeTime;
    }
}
