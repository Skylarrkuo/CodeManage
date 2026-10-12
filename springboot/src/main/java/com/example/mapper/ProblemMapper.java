package com.example.mapper;

import com.example.entity.Problem;

import java.util.List;

/**
 * 操作 problem 表相关数据接口
 */
public interface ProblemMapper {

    /** 新增 */
    int insert(Problem problem);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(Problem problem);

    /** 根据ID查询 */
    Problem selectById(Integer id);

    /** 条件查询所有 */
    List<Problem> selectAll(Problem problem);
}
