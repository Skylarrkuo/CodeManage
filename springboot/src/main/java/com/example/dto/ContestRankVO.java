package com.example.dto;

import com.example.entity.Contest;

import java.util.List;

/**
 * 单场竞赛排行榜整体返回结构
 */
public class ContestRankVO {

    /** 竞赛基本信息 */
    private Contest contest;

    /** 本场竞赛的题目列表（顺序即为 A/B/C... 的顺序） */
    private List<ContestRankProblemVO> problems;

    /** 排行榜条目 */
    private List<ContestRankItemVO> ranks;

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public List<ContestRankProblemVO> getProblems() {
        return problems;
    }

    public void setProblems(List<ContestRankProblemVO> problems) {
        this.problems = problems;
    }

    public List<ContestRankItemVO> getRanks() {
        return ranks;
    }

    public void setRanks(List<ContestRankItemVO> ranks) {
        this.ranks = ranks;
    }
}
