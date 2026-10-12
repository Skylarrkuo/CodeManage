package com.example.mapper;

import com.example.entity.Submission;

import java.util.List;

/**
 * 操作 submission 表相关数据接口
 */
public interface SubmissionMapper {

    /** 新增 */
    int insert(Submission submission);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(Submission submission);

    /** 根据ID查询 */
    Submission selectById(Integer id);

    /** 条件查询所有 */
    List<Submission> selectAll(Submission submission);
}
