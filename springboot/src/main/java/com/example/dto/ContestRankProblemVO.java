package com.example.dto;

/**
 * 排行榜中每道题目的基础信息
 */
public class ContestRankProblemVO {

    /** 题目ID */
    private Integer problemId;
    /** 题目标号：A/B/C... */
    private String label;
    /** 题目标题 */
    private String title;

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
