package com.example.mapper;

import com.example.entity.ContestSignup;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作 contest_signup 表相关数据接口
 */
public interface ContestSignupMapper {

    /** 新增 */
    int insert(ContestSignup signup);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(ContestSignup signup);

    /** 根据ID查询 */
    ContestSignup selectById(Integer id);

    /** 条件查询所有 */
    List<ContestSignup> selectAll(ContestSignup signup);

    /** 根据竞赛和学生查询报名记录 */
    @Select("select * from `contest_signup` where contest_id = #{contestId} and student_id = #{studentId}")
    ContestSignup selectByContestAndStudent(@Param("contestId") Integer contestId,
                                           @Param("studentId") Integer studentId);
}
