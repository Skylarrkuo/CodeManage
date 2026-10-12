package com.example.mapper;

import com.example.entity.Contest;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 操作 contest 表相关数据接口
 */
public interface ContestMapper {

    /** 新增 */
    int insert(Contest contest);

    /** 删除 */
    int deleteById(Integer id);

    /** 修改 */
    int updateById(Contest contest);

    /** 仅更新状态 */
    int updateStatus(@Param("id") Integer id,
                     @Param("status") String status,
                     @Param("updateTime") Date updateTime);

    /** 根据ID查询 */
    Contest selectById(Integer id);

    /** 条件查询所有 */
    List<Contest> selectAll(Contest contest);
}

