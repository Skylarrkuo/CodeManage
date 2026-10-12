package com.example.service;

import com.example.entity.Submission;
import com.example.mapper.SubmissionMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步评测任务服务：
 *
 * <p>负责从数据库中取出 Submission，更新评测状态（JUDGING 等），
 * 并调用 {@link SubmissionService#evaluateSubmission(Submission)} 完成实际评测逻辑。</p>
 */
@Service
public class SubmissionJudgeTaskService {

    @Resource
    private SubmissionMapper submissionMapper;

    @Resource
    private SubmissionService submissionService;

    /**
     * 异步执行评测任务。
     * 状态流转：PENDING -> JUDGING -> (ACCEPTED / WRONG_ANSWER / ERROR ...)
     */
    @Async
    public void evaluateAsync(Integer submissionId) {
        if (submissionId == null) {
            return;
        }

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return;
        }

        // 标记为评测中
        submission.setStatus("JUDGING");
        submission.setResultMsg("评测中，请稍候...");
        submissionMapper.updateById(submission);

        // 调用公共评测逻辑（内部会根据结果设置状态、分数、时间等）
        submissionService.evaluateSubmission(submission);

        // 将评测结果写回数据库
        submissionMapper.updateById(submission);
    }
}
