package com.example.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.entity.Problem;
import com.example.entity.ProblemTestCase;
import com.example.entity.Submission;
import com.example.judge.Judge0Client;
import com.example.mapper.ProblemTestCaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 评测计分逻辑测试。
 *
 * <p>直接调包级可见的 {@link SubmissionService#evaluateSubmission(Submission)}——
 * 这是私有的 {@code judgeWithMultipleCases} 唯一可测的接缝。只 mock 掉
 * `ProblemService` / `ProblemTestCaseMapper` / `Judge0Client`，不连数据库、不调真实 Judge0。</p>
 */
@ExtendWith(MockitoExtension.class)
class SubmissionScoringTest {

    private static final Integer PROBLEM_ID = 1;

    @Mock
    private ProblemService problemService;
    @Mock
    private Judge0Client judge0Client;
    @Mock
    private ProblemTestCaseMapper problemTestCaseMapper;

    private SubmissionService submissionService;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionService();
        ReflectionTestUtils.setField(submissionService, "problemService", problemService);
        ReflectionTestUtils.setField(submissionService, "judge0Client", judge0Client);
        ReflectionTestUtils.setField(submissionService, "problemTestCaseMapper", problemTestCaseMapper);
    }

    // ===================== 已配置分值的加权计分 =====================

    @Test
    void allPassedWithConfiguredScoresGivesFullMarks() {
        givenCases(testCase(30), testCase(30), testCase(40));
        givenJudgeReturns(accepted(), accepted(), accepted());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("ACCEPTED");
        assertThat(s.getScore()).isEqualTo(100);
        assertThat(s.getJudgeTime()).isNotNull();
    }

    @Test
    void partialCreditSumsOnlyPassedCaseScores() {
        givenCases(testCase(30), testCase(30), testCase(40));
        givenJudgeReturns(accepted(), accepted(), wrongAnswer());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("PARTIAL");
        assertThat(s.getScore()).isEqualTo(60);
    }

    @Test
    void singlePassedCaseOfThreeScoresItsOwnWeight() {
        givenCases(testCase(30), testCase(30), testCase(40));
        givenJudgeReturns(accepted(), wrongAnswer(), wrongAnswer());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("PARTIAL");
        assertThat(s.getScore()).isEqualTo(30);
    }

    @Test
    void nonePassedIsWrongAnswerWithZero() {
        givenCases(testCase(30), testCase(30), testCase(40));
        givenJudgeReturns(wrongAnswer(), wrongAnswer(), wrongAnswer());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("WRONG_ANSWER");
        assertThat(s.getScore()).isZero();
    }

    // ===================== 未配置分值时的均分兜底 =====================

    @Test
    void unconfiguredScoresFallBackToEqualSplit() {
        // 4 个测试点、分值全为 null，过 2 个 → 各 25 分 → 50
        givenCases(testCase(null), testCase(null), testCase(null), testCase(null));
        givenJudgeReturns(accepted(), accepted(), wrongAnswer(), wrongAnswer());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("PARTIAL");
        assertThat(s.getScore()).isEqualTo(50);
    }

    @Test
    void unconfiguredAllPassedGivesExactlyFullMarks() {
        // 3 个测试点均分是 33/34/33，全过必须恰好 100（不能因取整变成 99 或 101）
        givenCases(testCase(null), testCase(null), testCase(null));
        givenJudgeReturns(accepted(), accepted(), accepted());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("ACCEPTED");
        assertThat(s.getScore()).isEqualTo(100);
    }

    @Test
    void unconfiguredPartialUsesCumulativeRounding() {
        // 3 个测试点：33 / 34 / 33，只过第一个 → 33
        givenCases(testCase(null), testCase(null), testCase(null));
        givenJudgeReturns(accepted(), wrongAnswer(), wrongAnswer());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("PARTIAL");
        assertThat(s.getScore()).isEqualTo(33);
    }

    @Test
    void zeroAndNullScoresAreBothTreatedAsUnconfigured() {
        // 混合 0 与 null，合计仍为 0 → 走均分
        givenCases(testCase(0), testCase(null));
        givenJudgeReturns(accepted(), wrongAnswer());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("PARTIAL");
        assertThat(s.getScore()).isEqualTo(50);
    }

    // ===================== 错误短路 =====================

    @Test
    void compilationErrorShortCircuitsAndStopsCallingJudge0() {
        givenCases(testCase(30), testCase(30), testCase(40));
        given(judge0Client.judgeOnce(anyString(), anyString(), anyString(), anyString()))
                .willReturn(compilationError());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("ERROR");
        assertThat(s.getScore()).isZero();
        assertThat(s.getResultMsg()).contains("编译错误");
        assertThat(s.getResultMsg()).contains("main.c:1: error");
        // 关键：编译错误后不再调用 Judge0
        verify(judge0Client, times(1)).judgeOnce(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void judgeServiceErrorMarksSubmissionAsError() {
        givenCases(testCase(30));
        givenJudgeReturns(serviceError());

        Submission s = evaluate();

        assertThat(s.getStatus()).isEqualTo("ERROR");
        assertThat(s.getScore()).isZero();
        assertThat(s.getResultMsg()).contains("调用在线评测服务异常");
    }

    // ===================== 详情与聚合字段 =====================

    @Test
    void detailJsonCarriesPerCaseScoreAndEarned() {
        givenCases(testCase(30), testCase(70));
        givenJudgeReturns(accepted(), wrongAnswer());

        Submission s = evaluate();

        JSONArray detail = JSONUtil.parseArray(s.getDetail());
        assertThat(detail).hasSize(2);

        JSONObject first = detail.getJSONObject(0);
        assertThat(first.getInt("score")).isEqualTo(30);
        assertThat(first.getInt("earned")).isEqualTo(30);
        assertThat(first.getBool("accepted")).isTrue();

        JSONObject second = detail.getJSONObject(1);
        assertThat(second.getInt("score")).isEqualTo(70);
        assertThat(second.getInt("earned")).isZero();
        assertThat(second.getBool("accepted")).isFalse();
    }

    @Test
    void timeAndMemoryAreTheMaximumAcrossCases() {
        givenCases(testCase(50), testCase(50));
        givenJudgeReturns(
                result(3, 0.01, 1024L),
                result(3, 0.25, 8192L));

        Submission s = evaluate();

        assertThat(s.getTimeUsed()).isEqualTo(250);
        assertThat(s.getMemoryUsed()).isEqualTo(8192);
    }

    // ===================== 夹具 =====================

    private void givenCases(ProblemTestCase... cases) {
        Problem problem = new Problem();
        problem.setId(PROBLEM_ID);
        given(problemService.selectById(PROBLEM_ID)).willReturn(problem);
        given(problemTestCaseMapper.selectByProblemId(PROBLEM_ID)).willReturn(Arrays.asList(cases));
    }

    private void givenJudgeReturns(Judge0Client.Judge0Result... results) {
        given(judge0Client.judgeOnce(anyString(), anyString(), anyString(), anyString()))
                .willReturn(results[0], Arrays.copyOfRange(results, 1, results.length));
    }

    private Submission evaluate() {
        Submission submission = new Submission();
        submission.setId(100);
        submission.setProblemId(PROBLEM_ID);
        submission.setStudentId(1);
        submission.setLanguage("C");
        submission.setCode("int main() { return 0; }");

        submissionService.evaluateSubmission(submission);
        return submission;
    }

    private static ProblemTestCase testCase(Integer score) {
        ProblemTestCase testCase = new ProblemTestCase();
        testCase.setProblemId(PROBLEM_ID);
        testCase.setInputData("1 2");
        testCase.setOutputData("3");
        testCase.setIsSample(1);
        testCase.setScore(score);
        return testCase;
    }

    private static Judge0Client.Judge0Result accepted() {
        return result(3, 0.01, 1024L);
    }

    private static Judge0Client.Judge0Result wrongAnswer() {
        Judge0Client.Judge0Result r = result(4, 0.02, 2048L);
        r.setStdout("bad output");
        return r;
    }

    private static Judge0Client.Judge0Result compilationError() {
        Judge0Client.Judge0Result r = new Judge0Client.Judge0Result();
        r.setStatusId(6);
        r.setStatusDesc("Compilation Error");
        r.setCompileOutput("main.c:1: error: expected ';'");
        return r;
    }

    private static Judge0Client.Judge0Result serviceError() {
        Judge0Client.Judge0Result r = new Judge0Client.Judge0Result();
        r.setStatusId(-1);
        r.setStatusDesc("调用在线评测服务异常：连接超时");
        return r;
    }

    private static Judge0Client.Judge0Result result(int statusId, double seconds, Long memoryKb) {
        Judge0Client.Judge0Result r = new Judge0Client.Judge0Result();
        r.setStatusId(statusId);
        r.setStatusDesc(statusId == 3 ? "Accepted" : "Wrong Answer");
        r.setTime(seconds);
        r.setMemory(memoryKb);
        return r;
    }
}
