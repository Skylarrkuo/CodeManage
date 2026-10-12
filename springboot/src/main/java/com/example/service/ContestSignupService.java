package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Contest;
import com.example.entity.ContestSignup;
import com.example.exception.CustomException;
import com.example.mapper.ContestMapper;
import com.example.mapper.ContestSignupMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 竞赛报名业务处理
 */
@Service
public class ContestSignupService {

    @Resource
    private ContestSignupMapper contestSignupMapper;

    @Resource
    private ContestMapper contestMapper;

    /** 学生报名 */
    public void apply(ContestSignup signup) {
        if (signup.getContestId() == null || signup.getStudentId() == null) {
            throw new CustomException("竞赛ID和学生ID不能为空");
        }

        // 校验竞赛是否存在、是否在允许报名的时间范围内
        Contest contest = contestMapper.selectById(signup.getContestId());
        if (contest == null) {
            throw new CustomException("竞赛不存在");
        }
        Date now = new Date();
        if (contest.getStartTime() != null && now.after(contest.getStartTime())) {
            throw new CustomException("比赛已开始或已结束，无法报名");
        }
        if (contest.getSignUpStartTime() != null && now.before(contest.getSignUpStartTime())) {
            throw new CustomException("报名尚未开始");
        }
        if (contest.getSignUpEndTime() != null && now.after(contest.getSignUpEndTime())) {
            throw new CustomException("报名已结束");
        }

        ContestSignup db = contestSignupMapper.selectByContestAndStudent(signup.getContestId(), signup.getStudentId());
        if (ObjectUtil.isNotNull(db)) {
            throw new CustomException("您已报名该竞赛，请勿重复报名");
        }
        signup.setStatus("APPLIED");
        signup.setApplyTime(now);
        contestSignupMapper.insert(signup);
    }

    public void deleteById(Integer id) {
        contestSignupMapper.deleteById(id);
    }

    public void updateById(ContestSignup signup) {
        contestSignupMapper.updateById(signup);
    }

    public ContestSignup selectById(Integer id) {
        return contestSignupMapper.selectById(id);
    }

    public List<ContestSignup> selectAll(ContestSignup signup) {
        return contestSignupMapper.selectAll(signup);
    }

    public PageInfo<ContestSignup> selectPage(ContestSignup signup, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ContestSignup> list = contestSignupMapper.selectAll(signup);
        return PageInfo.of(list);
    }

    /** 审核通过 */
    public void approve(Integer id, Integer teacherId, String teacherName, String remark) {
        ContestSignup signup = contestSignupMapper.selectById(id);
        if (signup == null) {
            throw new CustomException("报名信息不存在");
        }
        signup.setStatus("APPROVED");
        signup.setAuditTeacherId(teacherId);
        signup.setAuditTeacherName(teacherName);
        signup.setAuditTime(new Date());
        signup.setRemark(remark);
        contestSignupMapper.updateById(signup);
    }

    /** 审核拒绝 */
    public void reject(Integer id, Integer teacherId, String teacherName, String remark) {
        ContestSignup signup = contestSignupMapper.selectById(id);
        if (signup == null) {
            throw new CustomException("报名信息不存在");
        }
        signup.setStatus("REJECTED");
        signup.setAuditTeacherId(teacherId);
        signup.setAuditTeacherName(teacherName);
        signup.setAuditTime(new Date());
        signup.setRemark(remark);
        contestSignupMapper.updateById(signup);
    }

    /** 批量审核通过 */
    public void approveBatch(List<Integer> ids, Integer teacherId, String teacherName, String remark) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("请选择要审核的报名记录");
        }
        for (Integer id : ids) {
            // 复用单条审核逻辑，保证规则一致
            approve(id, teacherId, teacherName, remark);
        }
    }

    /** 批量审核拒绝 */
    public void rejectBatch(List<Integer> ids, Integer teacherId, String teacherName, String remark) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("请选择要审核的报名记录");
        }
        for (Integer id : ids) {
            // 复用单条审核逻辑，保证规则一致
            reject(id, teacherId, teacherName, remark);
        }
    }
}

