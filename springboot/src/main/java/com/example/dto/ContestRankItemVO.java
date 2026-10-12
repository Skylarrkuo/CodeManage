package com.example.dto;

import java.util.Map;

/**
 * 排行榜中每位选手的汇总信息
 */
public class ContestRankItemVO {

    /** 排名（1 开始） */
    private Integer rank;

    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String clazz;

    /** 通过题数 */
    private Integer solvedCount;

    /** 总罚时（分钟） */
    private Integer penalty;

    /** 总分（当前实现为通过题目数量 * 100） */
    private Integer totalScore;

    /** 每道题目的评测情况，key 为 problemId */
    private Map<Integer, ContestRankProblemResultVO> problemResultMap;

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Integer getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(Integer solvedCount) {
        this.solvedCount = solvedCount;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Map<Integer, ContestRankProblemResultVO> getProblemResultMap() {
        return problemResultMap;
    }

    public void setProblemResultMap(Map<Integer, ContestRankProblemResultVO> problemResultMap) {
        this.problemResultMap = problemResultMap;
    }
}
