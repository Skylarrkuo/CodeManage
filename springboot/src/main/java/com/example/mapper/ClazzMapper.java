package com.example.mapper;

import com.example.entity.Clazz;

import java.util.List;

/**
 * 操作 clazz 表相关数据接口
 */
public interface ClazzMapper {

    /** 新增 */
    int insert(Clazz clazz);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(Clazz clazz);

    /** 根据ID查询 */
    Clazz selectById(Integer id);

    /** 条件查询所有 */
    List<Clazz> selectAll(Clazz clazz);
}
