package com.example.mapper;

import com.example.entity.Student;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作 student 表相关数据接口
 */
public interface StudentMapper {

    /** 新增 */
    int insert(Student student);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(Student student);

    /** 根据ID查询 */
    Student selectById(Integer id);

    /** 条件查询所有 */
    List<Student> selectAll(Student student);

    /** 根据账号查询 */
    @Select("select * from `student` where username = #{username}")
    Student selectByUsername(String username);
}
