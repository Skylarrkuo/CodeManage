package com.example.entity;

import java.util.Date;

/**
 * 编程题测试用例（支持多测试点评测）
 */
public class ProblemTestCase {

    private Integer id;
    /** 题目ID */
    private Integer problemId;
    /** 输入数据 */
    private String inputData;
    /** 期望输出 */
    private String outputData;
    /** 是否为样例：1-样例（对学生可见），0-隐藏测试点 */
    private Integer isSample;
    /** 测试点分值（当前可不使用，预留字段） */
    private Integer score;
    /** 显示/评测顺序 */
    private Integer orderNo;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getOutputData() {
        return outputData;
    }

    public void setOutputData(String outputData) {
        this.outputData = outputData;
    }

    public Integer getIsSample() {
        return isSample;
    }

    public void setIsSample(Integer isSample) {
        this.isSample = isSample;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
