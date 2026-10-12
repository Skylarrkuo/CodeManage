package com.example.mapper;

import com.example.entity.ProblemTestCase;

import java.util.List;

/**
 * 操作 problem_test_case 表相关数据接口
 */
public interface ProblemTestCaseMapper {

    /** 新增 */
    int insert(ProblemTestCase testCase);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(ProblemTestCase testCase);

    /** 根据题目ID查询全部测试用例 */
    List<ProblemTestCase> selectByProblemId(Integer problemId);
}
