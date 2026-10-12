package com.example.dto;

/**
 * 排行榜中某位选手在某道题目的评测结果
 */
public class ContestRankProblemResultVO {

    /** 是否通过该题 */
    private boolean solved;

    /** 错误提交次数（计入罚时） */
    private Integer wrongAttempts;

    /**
     * 通过该题所用时间（分钟），以比赛开始时间为基准。
     * 未通过时为 null。
     */
    private Integer time;

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public Integer getWrongAttempts() {
        return wrongAttempts;
    }

    public void setWrongAttempts(Integer wrongAttempts) {
        this.wrongAttempts = wrongAttempts;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
