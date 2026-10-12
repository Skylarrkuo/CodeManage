package com.example.service;

import com.example.entity.Clazz;
import com.example.mapper.ClazzMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 班级信息业务处理
 */
@Service
public class ClazzService {

    @Resource
    private ClazzMapper clazzMapper;

    /** 新增 */
    public void add(Clazz clazz) {
        clazzMapper.insert(clazz);
    }

    /** 删除 */
    public void deleteById(Integer id) {
        clazzMapper.deleteById(id);
    }

    /** 修改 */
    public void updateById(Clazz clazz) {
        clazzMapper.updateById(clazz);
    }

    /** 根据ID查询 */
    public Clazz selectById(Integer id) {
        return clazzMapper.selectById(id);
    }

    /** 查询所有 */
    public List<Clazz> selectAll(Clazz clazz) {
        return clazzMapper.selectAll(clazz);
    }

    /** 分页查询 */
    public PageInfo<Clazz> selectPage(Clazz clazz, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Clazz> list = clazzMapper.selectAll(clazz);
        return PageInfo.of(list);
    }
}
