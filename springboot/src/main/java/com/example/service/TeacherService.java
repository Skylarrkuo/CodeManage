package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.auth.PasswordUtil;
import com.example.entity.Account;
import com.example.entity.Teacher;
import com.example.exception.CustomException;
import com.example.mapper.TeacherMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师业务处理
 */
@Service
public class TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    /** 新增 */
    public void add(Teacher teacher) {
        Teacher dbTeacher = teacherMapper.selectByUsername(teacher.getUsername());
        if (ObjectUtil.isNotNull(dbTeacher)) {
            throw new CustomException("用户已存在");
        }
        // 默认密码明文 "123456"，入库前统一 BCrypt 加密
        String rawPassword = ObjectUtil.isEmpty(teacher.getPassword()) ? "123456" : teacher.getPassword();
        teacher.setPassword(PasswordUtil.encode(rawPassword));
        if (ObjectUtil.isEmpty(teacher.getName())) {
            teacher.setName(teacher.getUsername());
        }
        teacher.setRole("TEACHER");
        teacherMapper.insert(teacher);
    }

    /** 删除 */
    public void deleteById(Integer id) {
        teacherMapper.deleteById(id);
    }

    /** 修改 */
    public void updateById(Teacher teacher) {
        teacherMapper.updateById(teacher);
    }

    /** 根据ID查询 */
    public Teacher selectById(Integer id) {
        return teacherMapper.selectById(id);
    }

    /** 查询所有 */
    public List<Teacher> selectAll(Teacher teacher) {
        return teacherMapper.selectAll(teacher);
    }

    /** 分页查询 */
    public PageInfo<Teacher> selectPage(Teacher teacher, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Teacher> list = teacherMapper.selectAll(teacher);
        return PageInfo.of(list);
    }

    /** 登录 */
    public Account login(Account account) {
        Account dbTeacher = teacherMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbTeacher)) {
            throw new CustomException("用户不存在");
        }
        if (!PasswordUtil.matches(account.getPassword(), dbTeacher.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbTeacher;
    }

    /** 修改密码 */
    public void updatePassword(Account account) {
        Teacher dbTeacher = teacherMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbTeacher)) {
            throw new CustomException("用户不存在");
        }
        if (!PasswordUtil.matches(account.getPassword(), dbTeacher.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbTeacher.setPassword(PasswordUtil.encode(account.getNewPassword()));
        teacherMapper.updateById(dbTeacher);
    }

    /**
     * 管理员重置教师密码为默认值 123456
     */
    public void resetPassword(Integer id) {
        Teacher dbTeacher = teacherMapper.selectById(id);
        if (ObjectUtil.isNull(dbTeacher)) {
            throw new CustomException("用户不存在");
        }
        dbTeacher.setPassword(PasswordUtil.encode("123456"));
        teacherMapper.updateById(dbTeacher);
    }
}
