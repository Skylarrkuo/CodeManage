package com.example.service;

import com.example.entity.Problem;
import com.example.mapper.ProblemMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 题库业务处理
 */
@Service
public class ProblemService {

    @Resource
    private ProblemMapper problemMapper;

    public void add(Problem problem) {
        if (problem.getCreateTime() == null) {
            problem.setCreateTime(new Date());
        }
        if (problem.getType() == null || "".equals(problem.getType())) {
            problem.setType("PRACTICE");
        }
        // 默认公开：0-隐藏，1-公开。若未指定则默认公开。
        if (problem.getIsPublic() == null) {
            problem.setIsPublic(1);
        }
        problemMapper.insert(problem);
    }

    public void deleteById(Integer id) {
        problemMapper.deleteById(id);
    }

    public void updateById(Problem problem) {
        problemMapper.updateById(problem);
    }

    public Problem selectById(Integer id) {
        return problemMapper.selectById(id);
    }

    public List<Problem> selectAll(Problem problem) {
        return problemMapper.selectAll(problem);
    }

    public PageInfo<Problem> selectPage(Problem problem, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Problem> list = problemMapper.selectAll(problem);
        return PageInfo.of(list);
    }
}
