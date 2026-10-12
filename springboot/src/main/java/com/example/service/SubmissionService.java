package com.example.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.entity.Contest;
import com.example.entity.ContestSignup;
import com.example.entity.Problem;
import com.example.entity.ProblemTestCase;
import com.example.entity.Student;
import com.example.entity.Submission;
import com.example.exception.CustomException;
import com.example.judge.Judge0Client;
import com.example.mapper.ContestMapper;
import com.example.mapper.ContestSignupMapper;
import com.example.mapper.ProblemTestCaseMapper;
import com.example.mapper.StudentMapper;
import com.example.mapper.SubmissionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提交评测业务处理（基于 Judge0 在线评测服务）。
 */
@Service
public class SubmissionService {

    /** 题目未配置测试点分值时使用的总分（此时每个测试点均分） */
    private static final int DEFAULT_TOTAL_SCORE = 100;

    /** Judge0 状态码：编译错误 */
    private static final int JUDGE0_COMPILATION_ERROR = 6;

    @Resource
    private SubmissionMapper submissionMapper;

    @Resource
    private ProblemService problemService;

    // 在线评测客户端（基于 Judge0）
    @Resource
    private Judge0Client judge0Client;

    @Resource
    private ContestMapper contestMapper;

    @Resource
    private ContestSignupMapper contestSignupMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ProblemTestCaseMapper problemTestCaseMapper;

    /**
     * 学生提交代码：调用 Judge0 在线评测服务进行编译运行和判题。
     *
     * <p>当前实现为【异步评测】：本方法只负责创建提交记录，将状态置为 PENDING（等待评测）并写入数据库，
     * 实际评测由 {@link #evaluateAsync(Integer)} 异步执行，状态流转为：
     * PENDING -> JUDGING -> (ACCEPTED / WRONG_ANSWER / ERROR ...)。</p>
     */
    public void submit(Submission submission) {
        // 如果是比赛提交，先进行比赛相关的校验
        if (submission.getContestId() != null) {
            validateContestSubmission(submission);
        }

        // 新提交：记录提交时间，先以 PENDING 状态写入数据库
        Date now = new Date();
        submission.setSubmitTime(now);
        submission.setStatus("PENDING");
        submission.setScore(0);
        submission.setTimeUsed(0);
        submission.setMemoryUsed(0);
        submission.setResultMsg("评测任务已创建，等待评测服务器空闲...");
        submission.setDetail(null);
        submission.setJudgeTime(null);

        submissionMapper.insert(submission);
    }

    /**
     * 对已有提交记录进行重新评测（重测）。
     *
     * 仅更新评测相关字段：status / score / timeUsed / memoryUsed / resultMsg / judgeTime，
     * 保留原有的代码、语言、提交时间等信息。
     */
    public void rejudge(Integer id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new CustomException("提交记录不存在");
        }

        // 重置评测相关字段，重新进入评测队列
        submission.setStatus("PENDING");
        submission.setScore(0);
        submission.setTimeUsed(0);
        submission.setMemoryUsed(0);
        submission.setResultMsg("重新评测任务已创建，等待评测服务器空闲...");
        submission.setDetail(null);
        submission.setJudgeTime(null);
        submissionMapper.updateById(submission);
    }

    /**
     * 异步评测任务：
     *
     * <p>根据提交 ID 从数据库中取出提交记录，先将状态置为 JUDGING（评测中），
     * 然后调用公共评测逻辑 {@link #evaluateSubmission(Submission)} 完成实际判题，
     * 最后将评测结果写回数据库。</p>
     */
    @Async("judgeExecutor")
    public void evaluateAsync(Integer submissionId) {
        if (submissionId == null) {
            return;
        }

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return;
        }

        try {
            // 标记为评测中
            submission.setStatus("JUDGING");
            submission.setResultMsg("评测中，请稍候...");
            submissionMapper.updateById(submission);

            // 调用公共评测逻辑（内部会根据结果设置状态、分数、时间等）
            evaluateSubmission(submission);
        } catch (Exception e) {
            // 兜底：任何未预料的异常都不该让提交永远停在 JUDGING
            e.printStackTrace();
            markError(submission, "评测过程中发生异常：" + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : " - " + e.getMessage()));
            submission.setJudgeTime(new Date());
        }

        // 将评测结果写回数据库
        submissionMapper.updateById(submission);
    }

    /**
     * 比赛提交的合法性校验：
     * - 比赛存在
     * - 在比赛时间范围内
     * - 学生存在且已报名并审核通过
     */
    private void validateContestSubmission(Submission submission) {
        Integer contestId = submission.getContestId();
        Integer studentId = submission.getStudentId();
        if (contestId == null) {
            return;
        }
        if (studentId == null) {
            throw new CustomException("比赛提交缺少学生信息");
        }

        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new CustomException("比赛不存在");
        }

        Date now = new Date();
        if (contest.getStartTime() != null && now.before(contest.getStartTime())) {
            throw new CustomException("比赛尚未开始，暂不能提交代码");
        }
        if (contest.getEndTime() != null && now.after(contest.getEndTime())) {
            throw new CustomException("比赛已结束，不能再提交代码");
        }

        ContestSignup signup = contestSignupMapper.selectByContestAndStudent(contestId, studentId);
        if (signup == null || !"APPROVED".equals(signup.getStatus())) {
            throw new CustomException("您尚未通过本场比赛报名审核，不能提交代码");
        }

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new CustomException("学生信息不存在，无法提交");
        }
    }

    /**
     * 公共评测逻辑：根据题目调用 Judge0 在线评测服务，填充 submission 中的评测结果字段。
     *
     * <p>该方法由 {@link #evaluateAsync(Integer)} 在后台线程中调用。</p>
     */
    void evaluateSubmission(Submission submission) {
        try {
            if (submission.getProblemId() == null) {
                markError(submission, "评测失败：未指定题目ID");
            } else {
                Problem problem = problemService.selectById(submission.getProblemId());
                if (problem == null) {
                    markError(submission, "评测失败：题目不存在（ID=" + submission.getProblemId() + ")");
                } else {
                    // 先尝试查找题目的测试用例列表
                    List<ProblemTestCase> cases = problemTestCaseMapper.selectByProblemId(problem.getId());

                    if (cases == null || cases.isEmpty()) {
                        // 兼容旧逻辑：没有配置测试用例，则使用题目样例输入/输出进行单次评测
                        judge0Client.judge(submission, problem);
                    } else {
                        // 使用多测试点方式进行评测
                        judgeWithMultipleCases(submission, cases);
                    }
                    if (submission.getStatus() == null) {
                        markError(submission, "评测失败：未知错误");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            markError(submission, "评测过程中发生异常：" + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : " - " + e.getMessage()));
        }

        submission.setJudgeTime(new Date());
    }

    /**
     * 使用多组测试用例进行评测。
     *
     * <p>计分规则：总分 = 通过的测试点分值之和。若该题所有测试点的分值之和 ≤ 0
     * （即尚未配置分值），则退化为均分——每个测试点按 100/N 计，全部通过仍是 100 分。</p>
     *
     * <p>状态：全过 ACCEPTED、部分通过 PARTIAL、一个都没过 WRONG_ANSWER、
     * 编译错误或服务异常 ERROR。</p>
     */
    private void judgeWithMultipleCases(Submission submission, List<ProblemTestCase> cases) {
        if (cases == null || cases.isEmpty()) {
            markError(submission, "评测失败：题目尚未配置测试用例");
            return;
        }

        int total = cases.size();

        // 该题是否已配置测试点分值；和 ≤ 0 视为未配置，退化为均分
        int configuredSum = 0;
        for (ProblemTestCase tc : cases) {
            configuredSum += caseScore(tc);
        }
        boolean equalSplit = configuredSum <= 0;
        int maxScore = equalSplit ? DEFAULT_TOTAL_SCORE : configuredSum;

        int passed = 0;
        int earned = 0;
        int maxTimeMs = 0;
        int maxMemKb = 0;

        // 每个测试点的评测详情列表，将最终序列化为 JSON 存入 submission.detail
        List<Map<String, Object>> detailList = new ArrayList<>();

        int index = 1;
        for (ProblemTestCase tc : cases) {
            // 该测试点的满分：已配置时取配置值，否则按均分累积取整
            int caseMax = equalSplit ? equalShare(index - 1, total) : caseScore(tc);

            Judge0Client.Judge0Result r = judge0Client.judgeOnce(
                    submission.getCode(),
                    submission.getLanguage(),
                    tc.getInputData(),
                    tc.getOutputData()
            );

            // 本地参数错误或服务异常
            if (r.getStatusId() != null && r.getStatusId() < 0) {
                markError(submission, "评测失败：" + r.getStatusDesc());
                return;
            }

            // 编译错误：后续测试点必然同样编译失败，没必要再调用 Judge0，直接短路
            if (r.getStatusId() != null && r.getStatusId() == JUDGE0_COMPILATION_ERROR) {
                String compileOutput = r.getCompileOutput();
                if (StrUtil.isBlank(compileOutput)) {
                    compileOutput = r.getStatusDesc();
                }
                markError(submission, "编译错误：\n" + safePreview(compileOutput, 4000));
                return;
            }

            int timeMs = r.getTime() != null ? (int) Math.round(r.getTime() * 1000) : 0;
            int memKb = r.getMemory() != null ? r.getMemory().intValue() : 0;
            maxTimeMs = Math.max(maxTimeMs, timeMs);
            maxMemKb = Math.max(maxMemKb, memKb);

            // 记录当前测试点的详细结果
            Map<String, Object> item = new HashMap<>();
            item.put("index", index);
            item.put("sample", tc.getIsSample() != null && tc.getIsSample() == 1);
            item.put("input", safePreview(tc.getInputData()));
            item.put("expectedOutput", safePreview(tc.getOutputData()));
            item.put("statusId", r.getStatusId());
            item.put("status", r.getStatusDesc());
            item.put("time", timeMs);
            item.put("memory", memKb);
            item.put("accepted", r.isAccepted());
            // 该测试点的满分与得分，供前端解释「为什么是这个分数」
            item.put("score", caseMax);
            item.put("earned", r.isAccepted() ? caseMax : 0);

            if (r.isAccepted()) {
                passed++;
                earned += caseMax;
            } else {
                // 仅对未通过的测试点记录输出信息，避免 JSON 过大
                item.put("stdout", safePreview(r.getStdout()));
                item.put("stderr", safePreview(r.getStderr()));
                item.put("compileOutput", safePreview(r.getCompileOutput()));
            }
            detailList.add(item);
            index++;
        }

        submission.setTimeUsed(maxTimeMs);
        submission.setMemoryUsed(maxMemKb);

        // 将测试点详情序列化为 JSON
        if (!detailList.isEmpty()) {
            submission.setDetail(JSONUtil.toJsonStr(detailList));
        } else {
            submission.setDetail(null);
        }

        if (passed == total) {
            submission.setStatus("ACCEPTED");
            // 全部通过一律给满分，避免均分取整带来的误差
            submission.setScore(maxScore);
            submission.setResultMsg("通过所有 " + total + " 个测试用例。");
        } else if (passed == 0) {
            submission.setStatus("WRONG_ANSWER");
            submission.setScore(0);
            submission.setResultMsg("通过 0 / " + total + " 个测试用例，详情请查看测试点结果。");
        } else {
            submission.setStatus("PARTIAL");
            submission.setScore(earned);
            submission.setResultMsg("通过 " + passed + " / " + total + " 个测试用例，得分 "
                    + earned + " / " + maxScore + "，详情请查看测试点结果。");
        }
    }

    /** 取测试点配置的分值，null 或负数记为 0 */
    private static int caseScore(ProblemTestCase tc) {
        if (tc.getScore() == null) {
            return 0;
        }
        return Math.max(tc.getScore(), 0);
    }

    /**
     * 均分模式下第 i 个（从 0 起）测试点的分值。
     *
     * <p>用累积取整保证 N 个测试点的分值之和恰好等于 {@link #DEFAULT_TOTAL_SCORE}，
     * 例如 3 个测试点时分别是 33 / 34 / 33。</p>
     */
    private static int equalShare(int i, int total) {
        return (int) Math.round(DEFAULT_TOTAL_SCORE * (i + 1.0) / total)
                - (int) Math.round(DEFAULT_TOTAL_SCORE * (double) i / total);
    }

    /** 统一把提交置为评测失败（ERROR）状态并清空评测详情 */
    private void markError(Submission submission, String resultMsg) {
        submission.setStatus("ERROR");
        submission.setScore(0);
        submission.setTimeUsed(0);
        submission.setMemoryUsed(0);
        submission.setResultMsg(resultMsg);
        submission.setDetail(null);
    }

    /**
     * 截断长文本，避免 result/detail 过大（默认上限 300 字符）。
     */
    private String safePreview(String s) {
        return safePreview(s, 300);
    }

    /** 截断长文本到指定长度 */
    private String safePreview(String s, int maxLength) {
        if (s == null) {
            return "";
        }
        if (s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength) + "...";
    }

    public void deleteById(Integer id) {
        submissionMapper.deleteById(id);
    }

    public void updateById(Submission submission) {
        submissionMapper.updateById(submission);
    }

    public Submission selectById(Integer id) {
        return submissionMapper.selectById(id);
    }

    public List<Submission> selectAll(Submission submission) {
        return submissionMapper.selectAll(submission);
    }

    public PageInfo<Submission> selectPage(Submission submission, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Submission> list = submissionMapper.selectAll(submission);
        return PageInfo.of(list);
    }
}

