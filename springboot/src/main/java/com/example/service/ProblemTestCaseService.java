package com.example.service;

import com.example.entity.ProblemTestCase;
import com.example.mapper.ProblemTestCaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 题目测试用例业务处理
 */
@Service
public class ProblemTestCaseService {

    @Resource
    private ProblemTestCaseMapper problemTestCaseMapper;

    public List<ProblemTestCase> listByProblemId(Integer problemId) {
        return problemTestCaseMapper.selectByProblemId(problemId);
    }

    public void add(ProblemTestCase testCase) {
        if (testCase.getCreateTime() == null) {
            testCase.setCreateTime(new Date());
        }
        if (testCase.getIsSample() == null) {
            testCase.setIsSample(0);
        }
        if (testCase.getOrderNo() == null) {
            testCase.setOrderNo(1);
        }
        problemTestCaseMapper.insert(testCase);
    }

    public void update(ProblemTestCase testCase) {
        problemTestCaseMapper.updateById(testCase);
    }

    public void delete(Integer id) {
        problemTestCaseMapper.deleteById(id);
    }
}
