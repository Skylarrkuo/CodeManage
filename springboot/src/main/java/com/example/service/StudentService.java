package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.auth.PasswordUtil;
import com.example.entity.Account;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.mapper.StudentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生业务处理
 */
@Service
public class StudentService {

    @Resource
    private StudentMapper studentMapper;

    /** 新增 */
    public void add(Student student) {
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if (ObjectUtil.isNotNull(dbStudent)) {
            throw new CustomException("用户已存在");
        }
        // 默认密码明文 "123456"，入库前统一 BCrypt 加密
        String rawPassword = ObjectUtil.isEmpty(student.getPassword()) ? "123456" : student.getPassword();
        student.setPassword(PasswordUtil.encode(rawPassword));
        if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        student.setRole("STUDENT");
        studentMapper.insert(student);
    }

    /** 删除 */
    public void deleteById(Integer id) {
        studentMapper.deleteById(id);
    }

    /** 修改 */
    public void updateById(Student student) {
        studentMapper.updateById(student);
    }

    /** 根据ID查询 */
    public Student selectById(Integer id) {
        return studentMapper.selectById(id);
    }

    /** 查询所有 */
    public List<Student> selectAll(Student student) {
        return studentMapper.selectAll(student);
    }

    /** 分页查询 */
    public PageInfo<Student> selectPage(Student student, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Student> list = studentMapper.selectAll(student);
        return PageInfo.of(list);
    }

    /** 登录 */
    public Account login(Account account) {
        Account dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException("用户不存在");
        }
        if (!PasswordUtil.matches(account.getPassword(), dbStudent.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbStudent;
    }

    /** 修改密码 */
    public void updatePassword(Account account) {
        Student dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException("用户不存在");
        }
        if (!PasswordUtil.matches(account.getPassword(), dbStudent.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbStudent.setPassword(PasswordUtil.encode(account.getNewPassword()));
        studentMapper.updateById(dbStudent);
    }

    /**
     * 管理员重置学生密码为默认值 123456
     */
    public void resetPassword(Integer id) {
        Student dbStudent = studentMapper.selectById(id);
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException("用户不存在");
        }
        dbStudent.setPassword(PasswordUtil.encode("123456"));
        studentMapper.updateById(dbStudent);
    }
}
