package com.example.service;

import com.example.dto.ContestRankItemVO;
import com.example.dto.ContestRankProblemResultVO;
import com.example.dto.ContestRankProblemVO;
import com.example.dto.ContestRankVO;
import com.example.entity.Clazz;
import com.example.entity.Contest;
import com.example.entity.ContestSignup;
import com.example.entity.Problem;
import com.example.entity.Student;
import com.example.entity.Submission;
import com.example.exception.CustomException;
import com.example.mapper.ClazzMapper;
import com.example.mapper.ContestMapper;
import com.example.mapper.ContestSignupMapper;
import com.example.mapper.ProblemMapper;
import com.example.mapper.StudentMapper;
import com.example.mapper.SubmissionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 竞赛业务处理
 */
@Service
public class ContestService {

    /** 错误提交的罚时（分钟） */
    private static final int ACM_WRONG_PENALTY_MINUTES = 20;

    /** 每道题目的默认分值 */
    private static final int SCORE_PER_PROBLEM = 100;

    @Resource
    private ContestMapper contestMapper;

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private SubmissionMapper submissionMapper;

    @Resource
    private ContestSignupMapper contestSignupMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ClazzMapper clazzMapper;

    /** 新增 */
    public void add(Contest contest) {
        Date now = new Date();
        if (contest.getCreateTime() == null) {
            contest.setCreateTime(now);
        }
        contest.setUpdateTime(now);
        if (contest.getStatus() == null || "".equals(contest.getStatus())) {
            contest.setStatus("DRAFT");
        }
        contestMapper.insert(contest);
    }

    /** 删除 */
    public void deleteById(Integer id) {
        contestMapper.deleteById(id);
    }

    /** 修改 */
    public void updateById(Contest contest) {
        contest.setUpdateTime(new Date());
        contestMapper.updateById(contest);
    }

    /** 根据ID查询（会先刷新所有竞赛状态） */
    public Contest selectById(Integer id) {
        refreshContestStatus();
        return contestMapper.selectById(id);
    }

    /** 查询所有（会先刷新所有竞赛状态） */
    public List<Contest> selectAll(Contest contest) {
        refreshContestStatus();
        return contestMapper.selectAll(contest);
    }

    /** 分页查询（会先刷新所有竞赛状态） */
    public PageInfo<Contest> selectPage(Contest contest, Integer pageNum, Integer pageSize) {
        refreshContestStatus();
        PageHelper.startPage(pageNum, pageSize);
        List<Contest> list = contestMapper.selectAll(contest);
        return PageInfo.of(list);
    }

    /**
     * 为竞赛按班级批量添加参赛学生（课堂测验场景）
     */
    public void assignClasses(Integer contestId, List<Integer> classIds) {
        if (contestId == null || classIds == null || classIds.isEmpty()) {
            return;
        }
        // 确保竞赛存在
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new CustomException("竞赛不存在");
        }

        Date now = new Date();
        for (Integer classId : classIds) {
            if (classId == null) {
                continue;
            }
            Clazz clazz = clazzMapper.selectById(classId);
            if (clazz == null) {
                continue;
            }
            // 根据班级的年级/专业/班级信息筛选学生
            Student query = new Student();
            query.setGrade(clazz.getGrade());
            query.setMajor(clazz.getMajor());
            query.setClazz(clazz.getClazz());
            List<Student> students = studentMapper.selectAll(query);
            if (students == null || students.isEmpty()) {
                continue;
            }
            for (Student student : students) {
                if (student == null || student.getId() == null) {
                    continue;
                }
                // 避免重复报名
                ContestSignup exist = contestSignupMapper
                        .selectByContestAndStudent(contestId, student.getId());
                if (exist != null) {
                    continue;
                }
                ContestSignup signup = new ContestSignup();
                signup.setContestId(contestId);
                signup.setStudentId(student.getId());
                signup.setStatus("APPROVED");
                signup.setApplyTime(now);
                signup.setAuditTeacherName("系统自动添加（按班级）");
                signup.setAuditTime(now);
                contestSignupMapper.insert(signup);
            }
        }
    }

    /**
     * 根据时间自动刷新所有竞赛的状态：
     * DRAFT 保持不变；其余根据报名时间 / 比赛时间自动切换为
     * PUBLISHED / SIGNUP / RUNNING / FINISHED。
     */
    private void refreshContestStatus() {
        List<Contest> all = contestMapper.selectAll(new Contest());
        if (all == null || all.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (Contest contest : all) {
            String newStatus = computeStatus(contest, now);
            if (newStatus != null && !newStatus.equals(contest.getStatus())) {
                contestMapper.updateStatus(contest.getId(), newStatus, now);
                contest.setStatus(newStatus);
            }
        }
    }

    /**
     * 根据当前时间计算竞赛状态（不写入数据库）
     */
    private String computeStatus(Contest contest, Date now) {
        if (contest == null) {
            return null;
        }
        String status = contest.getStatus();
        if ("DRAFT".equals(status)) {
            // 草稿态保持不动
            return "DRAFT";
        }

        Date signStart = contest.getSignUpStartTime();
        Date signEnd = contest.getSignUpEndTime();
        Date start = contest.getStartTime();
        Date end = contest.getEndTime();

        // 未配置比赛时间，则只根据报名时间和原有状态做近似处理
        if (start == null || end == null) {
            if (signStart != null && signEnd != null) {
                if (now.before(signStart)) {
                    return status != null ? status : "PUBLISHED";
                } else if (!now.after(signEnd)) {
                    return "SIGNUP";
                } else {
                    return status != null ? status : "PUBLISHED";
                }
            }
            return status != null ? status : "PUBLISHED";
        }

        // 比赛尚未开始
        if (now.before(start)) {
            if (signStart != null && signEnd != null) {
                if (now.before(signStart)) {
                    return "PUBLISHED";
                } else if (!now.after(signEnd)) {
                    return "SIGNUP";
                } else {
                    // 报名结束，等待开赛
                    return "PUBLISHED";
                }
            }
            return "PUBLISHED";
        }

        // 比赛进行中
        if (!now.after(end)) {
            return "RUNNING";
        }

        // 比赛已结束
        return "FINISHED";
    }

    /**
     * 生成某场竞赛的 ACM/ICPC 风格排行榜
     */
    public ContestRankVO getContestRank(Integer contestId) {
        refreshContestStatus();
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new CustomException("竞赛不存在");
        }

        // 查询本场竞赛的题目（按创建时间 / ID 顺序即可，对应 A/B/C...）
        Problem problemQuery = new Problem();
        problemQuery.setContestId(contestId);
        problemQuery.setType("CONTEST");
        List<Problem> problemList = problemMapper.selectAll(problemQuery);

        List<ContestRankProblemVO> problemVOList = new ArrayList<>();
        Map<Integer, ContestRankProblemVO> problemVOMap = new HashMap<>();
        for (int i = 0; i < problemList.size(); i++) {
            Problem p = problemList.get(i);
            ContestRankProblemVO pvo = new ContestRankProblemVO();
            pvo.setProblemId(p.getId());
            pvo.setTitle(p.getTitle());
            pvo.setLabel(String.valueOf((char) ('A' + i)));
            problemVOList.add(pvo);
            problemVOMap.put(p.getId(), pvo);
        }

        // 查询报名（已通过）选手
        ContestSignup signupQuery = new ContestSignup();
        signupQuery.setContestId(contestId);
        signupQuery.setStatus("APPROVED");
        List<ContestSignup> signups = contestSignupMapper.selectAll(signupQuery);

        // 查询本场所有提交
        Submission submissionQuery = new Submission();
        submissionQuery.setContestId(contestId);
        List<Submission> submissions = submissionMapper.selectAll(submissionQuery);

        // 按提交时间升序排序，便于计算首 AC 和 WA 次数
        submissions.sort(Comparator.comparing(Submission::getSubmitTime, (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return a.compareTo(b);
        }));

        Map<Integer, Student> studentCache = new HashMap<>();
        Map<Integer, ContestRankItemVO> rankMap = new HashMap<>();
        List<ContestRankItemVO> rankList = new ArrayList<>();

        // 如果有报名通过记录，则以报名通过的选手为参赛选手
        if (signups != null && !signups.isEmpty()) {
            for (ContestSignup signup : signups) {
                Integer studentId = signup.getStudentId();
                if (studentId == null || rankMap.containsKey(studentId)) {
                    continue;
                }
                Student student = studentCache.computeIfAbsent(studentId, id -> studentMapper.selectById(id));
                if (student == null) {
                    continue;
                }
                ContestRankItemVO item = buildEmptyRankItem(student);
                rankMap.put(studentId, item);
                rankList.add(item);
            }
        } else {
            // 否则以本场有提交记录的学生为参赛选手
            Set<Integer> participantIds = new HashSet<>();
            for (Submission s : submissions) {
                if (s.getStudentId() != null) {
                    participantIds.add(s.getStudentId());
                }
            }
            for (Integer studentId : participantIds) {
                Student student = studentCache.computeIfAbsent(studentId, id -> studentMapper.selectById(id));
                if (student == null) {
                    continue;
                }
                ContestRankItemVO item = buildEmptyRankItem(student);
                rankMap.put(studentId, item);
                rankList.add(item);
            }
        }

        // 统计每个选手每道题的 AC 情况与错误次数
        Map<Integer, Map<Integer, ProblemStat>> studentProblemStats = new HashMap<>();
        Date startTime = contest.getStartTime();
        Date endTime = contest.getEndTime();

        for (Submission s : submissions) {
            Integer studentId = s.getStudentId();
            Integer problemId = s.getProblemId();
            if (studentId == null || problemId == null) {
                continue;
            }
            if (!rankMap.containsKey(studentId)) {
                // 非参赛选手的提交不计入排行榜
                continue;
            }

            Date submitTime = s.getSubmitTime();
            if (submitTime == null) {
                continue;
            }
            // 只统计比赛时间区间内的提交
            if (startTime != null && submitTime.before(startTime)) {
                continue;
            }
            if (endTime != null && submitTime.after(endTime)) {
                continue;
            }

            String status = s.getStatus();
            if (status == null) {
                continue;
            }

            Map<Integer, ProblemStat> problemStatMap =
                    studentProblemStats.computeIfAbsent(studentId, k -> new HashMap<>());
            ProblemStat stat = problemStatMap.computeIfAbsent(problemId, k -> new ProblemStat());

            if (stat.solved) {
                // 已经 AC 的题目后续提交不再计入
                continue;
            }

            if ("ACCEPTED".equals(status)) {
                stat.solved = true;
                stat.acceptedTime = submitTime;
            } else if ("WRONG_ANSWER".equals(status) || "ERROR".equals(status)) {
                // 简化处理：WA 和 ERROR 都按一次错误计入罚时
                stat.wrongAttempts++;
            }
        }

        // 汇总到���行榜条目
        for (ContestRankItemVO item : rankList) {
            Map<Integer, ProblemStat> problemStatMap = studentProblemStats.get(item.getStudentId());
            if (problemStatMap == null) {
                problemStatMap = new HashMap<>();
            }

            int solvedCount = 0;
            int penalty = 0;
            int totalScore = 0;

            // 按题目顺序构建每道题的结果
            for (Problem p : problemList) {
                ProblemStat stat = problemStatMap.get(p.getId());
                boolean solved = stat != null && stat.solved;
                int wrongAttempts = stat != null ? stat.wrongAttempts : 0;
                Integer timeMinutes = null;

                if (solved && stat.acceptedTime != null && startTime != null) {
                    long diffMillis = stat.acceptedTime.getTime() - startTime.getTime();
                    if (diffMillis < 0) {
                        diffMillis = 0;
                    }
                    timeMinutes = (int) (diffMillis / 1000 / 60);
                    penalty += timeMinutes + wrongAttempts * ACM_WRONG_PENALTY_MINUTES;
                    solvedCount++;
                    totalScore += SCORE_PER_PROBLEM;
                }

                ContestRankProblemResultVO pr = new ContestRankProblemResultVO();
                pr.setSolved(solved);
                pr.setWrongAttempts(wrongAttempts);
                pr.setTime(timeMinutes);
                if (item.getProblemResultMap() == null) {
                    item.setProblemResultMap(new HashMap<>());
                }
                item.getProblemResultMap().put(p.getId(), pr);
            }

            item.setSolvedCount(solvedCount);
            item.setPenalty(penalty);
            item.setTotalScore(totalScore);
        }

        // 排序：先按通过题数降序，再按罚时升序，再按学号/ID 升序
        rankList.sort((a, b) -> {
            int cmp = Integer.compare(b.getSolvedCount() == null ? 0 : b.getSolvedCount(),
                    a.getSolvedCount() == null ? 0 : a.getSolvedCount());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.getPenalty() == null ? 0 : a.getPenalty(),
                    b.getPenalty() == null ? 0 : b.getPenalty());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getStudentId(), b.getStudentId());
        });

        // 赋予名次
        int rank = 1;
        for (ContestRankItemVO item : rankList) {
            item.setRank(rank++);
        }

        ContestRankVO vo = new ContestRankVO();
        vo.setContest(contest);
        vo.setProblems(problemVOList);
        vo.setRanks(rankList);
        return vo;
    }

    /** 构建一个初始的排行榜条目 */
    private ContestRankItemVO buildEmptyRankItem(Student student) {
        ContestRankItemVO item = new ContestRankItemVO();
        item.setStudentId(student.getId());
        item.setStudentName(student.getName());
        item.setStudentNo(student.getStudentNo());
        item.setClazz(student.getClazz());
        item.setSolvedCount(0);
        item.setPenalty(0);
        item.setTotalScore(0);
        item.setProblemResultMap(new HashMap<>());
        return item;
    }

    /** 内部使用的数据结构：某人某题的统计信息 */
    private static class ProblemStat {
        boolean solved;
        int wrongAttempts;
        Date acceptedTime;
    }
}

